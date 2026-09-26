package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.Predicates
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceElementOffsetStrategy
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirValueParameterKind
import org.jetbrains.kotlin.fir.declarations.builder.buildNamedFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.hasAnnotationWithClassId
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.isContextParameter
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjectionOut
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.realElement
import kotlin.reflect.full.companionObjectInstance

class FirPageDeclarationGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Predicates.isPage)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*> {
        val targetCallable = session.symbolProvider.getTopLevelCallableSymbols(classId.packageFqName, classId.shortClassName).first {
            it.hasAnnotationWithClassId(Names.pageClass, session)
        }

        val targetAnnotation = targetCallable.getAnnotationByClassId(Names.pageClass, session)!!
        val isFactory = targetCallable is FirFunctionSymbol<*> && targetCallable.valueParameterSymbols.isNotEmpty()
        val pluginGenerated = KtFakeSourceElementKind.PluginGenerated::class

        val fakeElement = (if(pluginGenerated.isSealed) {
            pluginGenerated.nestedClasses.find { it.simpleName == "Default" }!!.objectInstance!!
        } else pluginGenerated.objectInstance!!) as KtFakeSourceElementKind

        return if(isFactory) {
            createTopLevelClass(classId, FirPageGenerated.PageFactoryClass, classKind = ClassKind.OBJECT) {
                superType(session.symbolProvider.getClassLikeSymbolByClassId(Names.pageFactoryClass)!!
                    .constructType(arrayOf(
                        session.symbolProvider.getClassLikeSymbolByClassId(Names.pageContextClass)!!.defaultType(),
                        classId.createNestedClassId(Name.identifier("Instance")).constructClassLikeType()
                    )))

                //source = targetAnnotation.source?.fakeElement(fakeElement)
            }.symbol
        } else {
            createTopLevelClass(classId, FirPageGenerated.PageClass(false), classKind = ClassKind.OBJECT) {
                superType(session.symbolProvider.getClassLikeSymbolByClassId(Names.pageHolderClass)!!
                    .constructType(arrayOf(
                        classId.constructClassLikeType()
                    )))

                //source = targetAnnotation.source?.fakeElement(fakeElement)
            }.symbol
        }
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        return if(context.owner.classKind.isObject) {
            emptyList()
        } else {
            val classId = context.owner.classId.outerClassId!!
            val targetCallable = session.symbolProvider.getTopLevelCallableSymbols(classId.packageFqName, classId.shortClassName).first {
                it.hasAnnotationWithClassId(Names.pageClass, session)
            }

            listOf(createConstructor(context.owner, FirPageGenerated.PageConstructor, isPrimary = true) {
                visibility = Visibilities.Internal

                valueParameter(Name.identifier($$"serenity$identifier"), session.builtinTypes.unitType.coneType)

                if(targetCallable is FirFunctionSymbol<*>) {
                    for(param in targetCallable.valueParameterSymbols) {
                        valueParameter(param.name, param.resolvedReturnType, isVararg = param.isVararg, hasDefaultValue = param.hasDefaultValue)
                    }
                }
            }.symbol)
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        if(owner.origin != FirDeclarationOrigin.Plugin(FirPageGenerated.PageFactoryClass))
            return null

        return createNestedClass(context.owner, name, FirPageGenerated.PageClass(true)) {
            superType(session.symbolProvider.getClassLikeSymbolByClassId(Names.pageHolderClass)!!.constructType(arrayOf(
                owner.classId.createNestedClassId(name).constructClassLikeType()
            )))
        }.symbol
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        if(context != null) {
            val targetClass = context.owner.classId.outerClassId ?: context.owner.classId
            val targetCallable = session.symbolProvider.getTopLevelCallableSymbols(targetClass.packageFqName, targetClass.shortClassName).first {
                it.hasAnnotationWithClassId(Names.pageClass, session)
            }

            return when(callableId.callableName.identifier) {
                "Main" -> listOf(buildNamedFunction {
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    symbol = FirNamedFunctionSymbol(callableId)
                    dispatchReceiverType = context.owner.defaultType()
                    moduleData = session.moduleData
                    origin = FirDeclarationOrigin.Plugin(FirPageGenerated.PageMainFun)
                    name = Name.identifier("Main")
                    status = FirResolvedDeclarationStatusImpl(
                        Visibilities.Public,
                        Modality.FINAL,
                        EffectiveVisibility.Public
                    ).also { it.isOverride = true }
                    returnTypeRef = session.builtinTypes.unitType
                    isLocal = false

                    annotations += buildAnnotation {
                        annotationTypeRef = buildResolvedTypeRef {
                            coneType = session.symbolProvider.getClassLikeSymbolByClassId(Names.androidxComposableClass)!!.defaultType()
                        }

                        argumentMapping = buildAnnotationArgumentMapping {}
                    }

                    annotations += buildAnnotation {
                        annotationTypeRef = buildResolvedTypeRef {
                            coneType = session.symbolProvider.getClassLikeSymbolByClassId(Names.htmlComposableClass)!!.defaultType()
                        }

                        argumentMapping = buildAnnotationArgumentMapping {}
                    }
                }.symbol)

                "create" -> listOf(buildNamedFunction {
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    symbol = FirNamedFunctionSymbol(callableId)
                    dispatchReceiverType = context.owner.defaultType()
                    moduleData = session.moduleData
                    origin = FirDeclarationOrigin.Plugin(FirPageGenerated.PageFactoryCreateFun)
                    name = Name.identifier("create")
                    status = FirResolvedDeclarationStatusImpl(
                        Visibilities.Public,
                        Modality.FINAL,
                        EffectiveVisibility.Public
                    ).also { it.isOverride = true }

                    returnTypeRef = buildResolvedTypeRef {
                        coneType = targetClass.createNestedClassId(Name.identifier("Instance")).constructClassLikeType()
                    }

                    isLocal = false

                    valueParameters += buildValueParameter {
                        moduleData = session.moduleData
                        origin = FirDeclarationOrigin.Plugin(FirPageGenerated.Parameter)
                        returnTypeRef = buildResolvedTypeRef {
                            coneType = session.symbolProvider.getClassLikeSymbolByClassId(Names.pageContextClass)!!.defaultType()
                        }
                        name = Name.identifier("ctx")
                        symbol = FirValueParameterSymbol()
                        containingDeclarationSymbol = this@buildNamedFunction.symbol
                    }
                }.symbol)

                "of" -> listOf(buildNamedFunction {
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    symbol = FirNamedFunctionSymbol(callableId)
                    dispatchReceiverType = context.owner.defaultType()
                    moduleData = session.moduleData
                    origin = FirDeclarationOrigin.Plugin(FirPageGenerated.PageFactoryOfFun)
                    name = Name.identifier("of")
                    status = FirResolvedDeclarationStatusImpl(
                        Visibilities.Public,
                        Modality.FINAL,
                        EffectiveVisibility.Public
                    ).also { it.isOverride = true }

                    returnTypeRef = buildResolvedTypeRef {
                        coneType = targetClass.createNestedClassId(Name.identifier("Instance")).constructClassLikeType()
                    }

                    isLocal = false

                    if(targetCallable is FirFunctionSymbol<*>) {
                        for(param in targetCallable.valueParameterSymbols) {
                            valueParameters += buildValueParameter {
                                moduleData = session.moduleData
                                origin = FirDeclarationOrigin.Plugin(FirPageGenerated.Parameter)
                                returnTypeRef = param.resolvedReturnTypeRef
                                name = param.name
                                symbol = FirValueParameterSymbol()
                                containingDeclarationSymbol = this@buildNamedFunction.symbol
                                defaultValue = param.resolvedDefaultValue
                                isVararg = param.isVararg
                                valueParameterKind = when(param.isContextParameter()) {
                                    true -> FirValueParameterKind.ContextParameter
                                    false -> FirValueParameterKind.Regular
                                }
                            }
                        }
                    }
                }.symbol)

                else -> emptyList()
            }
        } else {
            return emptyList()
        }
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        if(context != null) {
            val (actualOrigin, type) = when (callableId.callableName.identifier) {
                "id" -> FirPageGenerated.PageIdProperty to session.builtinTypes.stringType.coneType
                "path" -> FirPageGenerated.PagePathProperty to session.builtinTypes.stringType.coneType
                "details" -> FirPageGenerated.PageDetailsProperty to session.symbolProvider.getClassLikeSymbolByClassId(Names.pageDetailsClass)!!
                    .defaultType()

                "extensions" -> FirPageGenerated.PageExtensionsProperty to session.symbolProvider.getClassLikeSymbolByClassId(Names.mapClass)!!
                    .constructType(
                        arrayOf(
                            session.symbolProvider.getClassLikeSymbolByClassId(Names.kClassClass)!!
                                .constructType(
                                    arrayOf(
                                        ConeKotlinTypeProjectionOut(
                                            session.builtinTypes.annotationType.coneType
                                        )
                                    )
                                ),
                            session.symbolProvider.getClassLikeSymbolByClassId(Names.abstractPageExtensionClass)!!
                                .defaultType()
                        )
                    )

                else -> return emptyList()
            }

            return listOf(createMemberProperty(context.owner, actualOrigin, callableId.callableName, type).symbol)
        } else return emptyList()
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        return when (classSymbol.origin) {
            FirDeclarationOrigin.Plugin(FirPageGenerated.PageClass(false)) -> {
                setOf(
                    Name.identifier("id"),
                    Name.identifier("path"),
                    Name.identifier("details"),
                    Name.identifier("extensions"),
                    Name.identifier("Main"),
                )
            }

            FirDeclarationOrigin.Plugin(FirPageGenerated.PageClass(true)) -> {
                setOf(
                    SpecialNames.INIT,
                    Name.identifier("id"),
                    Name.identifier("path"),
                    Name.identifier("details"),
                    Name.identifier("extensions"),
                    Name.identifier("Main"),
                )
            }

            FirDeclarationOrigin.Plugin(FirPageGenerated.PageFactoryClass) -> {
                setOf(
                    Name.identifier("id"),
                    Name.identifier("path"),
                    Name.identifier("create"),
                    Name.identifier("of"),
                )
            }

            else -> {
                emptySet()
            }
        }
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return if(classSymbol.origin == FirDeclarationOrigin.Plugin(FirPageGenerated.PageFactoryClass)) {
            setOf(Name.identifier("Instance"))
        } else emptySet()
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelClassIds(): Set<ClassId> {
        return session.predicateBasedProvider.getSymbolsByPredicate(Predicates.isPage)
            .filterIsInstance<FirFunctionSymbol<*>>()
            .map { sym ->
                val id = sym.callableId
                ClassId(id.packageName, id.callableName)
            }
            .toSet()
    }
}