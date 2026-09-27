package net.derfruhling.serenity.compiler.fir

import net.derfruhling.serenity.compiler.Names
import net.derfruhling.serenity.compiler.Predicates
import net.derfruhling.serenity.compiler.hashFunctionName
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildNamedFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.*
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.isContextParameter
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.platform.PotentiallyWebPlatform
import org.jetbrains.kotlin.types.ConstantValueKind

class FirPageDeclarationGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Predicates.isPage)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*> {
        val targetCallable = session.symbolProvider.getTopLevelCallableSymbols(
            classId.packageFqName,
            classId.shortClassName
        ).first {
            it.hasAnnotationWithClassId(Names.pageClass, session)
        }

        val isFactory =
            targetCallable is FirFunctionSymbol<*> && targetCallable.valueParameterSymbols.isNotEmpty()

        return if (isFactory) {
            generatePageFactoryClass(classId).symbol
        } else {
            generatePageClass(classId, false).symbol
        }
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    private fun generatePageFactoryClass(classId: ClassId): FirRegularClass = createTopLevelClass(
        classId,
        FirPageGenerated.PageFactoryClass,
        classKind = ClassKind.OBJECT
    ) {
        val instanceId = classId.createNestedClassId(Name.identifier("Instance"))
        superType(
            session.symbolProvider.getClassLikeSymbolByClassId(Names.pageFactoryClass)!!
                .constructType(
                    arrayOf(
                        session.symbolProvider.getClassLikeSymbolByClassId(Names.pageContextClass)!!
                            .defaultType(),
                        instanceId.constructClassLikeType()
                    )
                )
        )

        superType(
            session.symbolProvider.getClassLikeSymbolByClassId(Names.pageSerializerProviderClass)!!
                .constructType(
                    arrayOf(
                        instanceId.constructClassLikeType()
                    )
                )
        )
    }

    private fun generatePageClass(classId: ClassId, isSubClass: Boolean) =
        buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            symbol = FirRegularClassSymbol(classId)
            moduleData = session.moduleData
            origin = FirDeclarationOrigin.Plugin(FirPageGenerated.PageClass(isSubClass))
            name = classId.shortClassName
            scopeProvider = session.kotlinScopeProvider
            classKind = if (isSubClass) ClassKind.CLASS else ClassKind.OBJECT
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                EffectiveVisibility.Public
            )

            superTypeRefs += session.symbolProvider.getClassLikeSymbolByClassId(Names.pageHolderClass)!!
                .constructType(
                    arrayOf(
                        classId.constructClassLikeType()
                    )
                )
                .toFirResolvedTypeRef()

            if (!isSubClass) {
                superTypeRefs += session.symbolProvider.getClassLikeSymbolByClassId(Names.pageSerializerProviderClass)!!
                    .constructType(
                        arrayOf(
                            classId.constructClassLikeType()
                        )
                    )
                    .toFirResolvedTypeRef()
            }
        }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        return if (context.owner.classKind.isObject) {
            emptyList()
        } else {
            listOf(generateConstructor(context.owner).symbol)
        }
    }

    private fun generateConstructor(owner: FirClassSymbol<*>): FirConstructor {
        val classId = owner.classId.outerClassId!!
        val targetCallable = session.symbolProvider.getTopLevelCallableSymbols(
            classId.packageFqName,
            classId.shortClassName
        ).first {
            it.hasAnnotationWithClassId(Names.pageClass, session)
        }

        return createConstructor(
            owner,
            FirPageGenerated.PageConstructor,
            isPrimary = true
        ) {
            visibility = Visibilities.Internal

            valueParameter(
                Name.identifier($$"serenity$identifier"),
                session.builtinTypes.unitType.coneType
            )

            if (targetCallable is FirFunctionSymbol<*>) {
                for (param in targetCallable.valueParameterSymbols) {
                    valueParameter(
                        param.name,
                        param.resolvedReturnType,
                        isVararg = param.isVararg,
                        hasDefaultValue = param.hasDefaultValue
                    )
                }
            }
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        return when ((owner.origin as? FirDeclarationOrigin.Plugin ?: return null).key) {
            FirPageGenerated.PageFactoryClass ->
                generatePageClass(owner.classId.createNestedClassId(name), true).symbol

            is FirPageGenerated.PageClass ->
                generateSerializerClass(owner, name).symbol

            else -> null
        }
    }

    private fun generateSerializerClass(
        owner: FirClassSymbol<*>,
        name: Name
    ): FirRegularClass = createNestedClass(
        owner,
        name,
        FirPageGenerated.PageSerializerClass,
        classKind = ClassKind.OBJECT
    ) {
        visibility = Visibilities.Private

        superType(
            Names.kSerializerClass.constructClassLikeType(
                typeArguments = arrayOf(
                    owner.defaultType()
                )
            )
        )
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        if (context != null) {
            val targetClass = context.owner.classId.outermostClassId
            val targetCallable = session.symbolProvider.getTopLevelCallableSymbols(
                targetClass.packageFqName,
                targetClass.shortClassName
            ).first {
                it.hasAnnotationWithClassId(Names.pageClass, session)
            }

            return when (callableId.callableName.identifier) {
                "Main" -> listOf(generateMainContentFunction(callableId, context).symbol)

                "create" -> listOf(
                    generateFactoryCreateFunction(
                        callableId,
                        context,
                        targetClass
                    ).symbol
                )

                "of" -> listOf(
                    generateFactoryOfFunction(
                        callableId,
                        context,
                        targetClass,
                        targetCallable
                    ).symbol
                )

                "serializer" -> listOf(
                    generateSerializerGetterFunction(
                        context,
                        callableId,
                        targetCallable
                    ).symbol
                )

                "serialize" -> listOf(generateSerializeFunction(context, callableId).symbol)
                "deserialize" -> listOf(generateDeserializeFunction(context, callableId).symbol)

                else -> emptyList()
            }
        } else {
            val name = callableId.callableName
            return if (name.isSpecial) {
                val entrypointFunction = generateEntrypointFunction(name, callableId)
                listOf(entrypointFunction.symbol)
            } else emptyList()
        }
    }

    private fun generateEntrypointFunction(
        name: Name,
        callableId: CallableId
    ): FirNamedFunction {
        val callableName = name.asString().removeSurrounding("<page ", " entrypoint>")
        val actualCallableId =
            CallableId(callableId.packageName, Name.identifier(callableName))
        val actualCallable = session.symbolProvider.getTopLevelFunctionSymbols(
            actualCallableId.packageName,
            actualCallableId.callableName
        )
            .single { it.hasAnnotation(Names.pageClass, session) }
        val id = hashFunctionName(actualCallableId)

        val entrypointFunction = buildNamedFunction {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            symbol = FirNamedFunctionSymbol(callableId)
            moduleData = session.moduleData
            origin = FirDeclarationOrigin.Plugin(
                FirPageGenerated.PageEntrypointFun(actualCallableId)
            )
            this.name = callableId.callableName
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                EffectiveVisibility.Public
            )
            returnTypeRef = session.builtinTypes.unitType
            isLocal = false

            if (actualCallable.valueParameterSymbols.isNotEmpty()) {
                valueParameters += buildValueParameter {
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    symbol = FirValueParameterSymbol()
                    moduleData = session.moduleData
                    origin = FirDeclarationOrigin.Plugin(FirPageGenerated.Parameter)
                    this.name = Name.identifier("obj")
                    returnTypeRef = session.symbolProvider.getClassLikeSymbolByClassId(
                        JsStandardClassIds.JsAny
                    )!!.defaultType().toFirResolvedTypeRef()
                    containingDeclarationSymbol = this@buildNamedFunction.symbol
                }
            }

            annotations += buildAnnotation {
                annotationTypeRef = buildResolvedTypeRef {
                    coneType =
                        session.symbolProvider.getClassLikeSymbolByClassId(
                            JsStandardClassIds.Annotations.JsExport
                        )!!.defaultType()
                }

                argumentMapping = buildAnnotationArgumentMapping {}
            }

            annotations += buildAnnotation {
                annotationTypeRef = buildResolvedTypeRef {
                    coneType =
                        session.symbolProvider.getClassLikeSymbolByClassId(
                            JsStandardClassIds.Annotations.JsName
                        )!!.defaultType()
                }

                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[Name.identifier("name")] = buildLiteralExpression(
                        null,
                        ConstantValueKind.String,
                        id,
                        setType = true
                    )
                }
            }
        }
        return entrypointFunction
    }

    private fun generateDeserializeFunction(
        context: MemberGenerationContext,
        callableId: CallableId
    ): FirNamedFunction = createMemberFunction(
        context.owner,
        FirPageGenerated.PageDeserializeFun,
        callableId.callableName,
        context.owner.classId.outerClassId!!.constructClassLikeType()
    ) {
        valueParameter(Name.identifier("decoder"), Names.decoderClass.constructClassLikeType())

        status {
            isOverride = true
        }
    }

    private fun generateSerializeFunction(
        context: MemberGenerationContext,
        callableId: CallableId
    ): FirNamedFunction = createMemberFunction(
        context.owner,
        FirPageGenerated.PageSerializeFun,
        callableId.callableName,
        session.builtinTypes.unitType.coneType
    ) {
        valueParameter(Name.identifier("encoder"), Names.encoderClass.constructClassLikeType())
        valueParameter(
            Name.identifier("value"),
            context.owner.classId.outerClassId!!.constructClassLikeType()
        )

        status {
            isOverride = true
        }
    }

    private fun generateSerializerGetterFunction(
        context: MemberGenerationContext,
        callableId: CallableId,
        targetCallable: FirCallableSymbol<*>
    ): FirNamedFunction = createMemberFunction(
        context.owner,
        FirPageGenerated.PageSerializerGetter,
        callableId.callableName,
        Names.kSerializerClass.constructClassLikeType(
            typeArguments = arrayOf(
                if (targetCallable is FirFunctionSymbol<*> && targetCallable.valueParameterSymbols.isNotEmpty()) {
                    context.owner.classId.createNestedClassId(Name.identifier("Instance"))
                } else {
                    context.owner.classId
                }.constructClassLikeType()
            )
        )
    ) {
        status {
            isOverride = true
        }
    }

    private fun generateFactoryOfFunction(
        callableId: CallableId,
        context: MemberGenerationContext,
        targetClass: ClassId,
        targetCallable: FirCallableSymbol<*>
    ): FirNamedFunction = buildNamedFunction {
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
            coneType = targetClass.createNestedClassId(Name.identifier("Instance"))
                .constructClassLikeType()
        }

        isLocal = false

        if (targetCallable is FirFunctionSymbol<*>) {
            for (param in targetCallable.valueParameterSymbols) {
                valueParameters += buildValueParameter {
                    moduleData = session.moduleData
                    origin = FirDeclarationOrigin.Plugin(FirPageGenerated.Parameter)
                    returnTypeRef = param.resolvedReturnTypeRef
                    name = param.name
                    symbol = FirValueParameterSymbol()
                    containingDeclarationSymbol = this@buildNamedFunction.symbol
                    defaultValue = param.resolvedDefaultValue
                    isVararg = param.isVararg
                    valueParameterKind = when (param.isContextParameter()) {
                        true -> FirValueParameterKind.ContextParameter
                        false -> FirValueParameterKind.Regular
                    }
                }
            }
        }
    }

    private fun generateFactoryCreateFunction(
        callableId: CallableId,
        context: MemberGenerationContext,
        targetClass: ClassId
    ): FirNamedFunction = buildNamedFunction {
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
            coneType = targetClass.createNestedClassId(Name.identifier("Instance"))
                .constructClassLikeType()
        }

        isLocal = false

        valueParameters += buildValueParameter {
            moduleData = session.moduleData
            origin = FirDeclarationOrigin.Plugin(FirPageGenerated.Parameter)
            returnTypeRef = buildResolvedTypeRef {
                coneType =
                    session.symbolProvider.getClassLikeSymbolByClassId(Names.pageContextClass)!!
                        .defaultType()
            }
            name = Name.identifier("ctx")
            symbol = FirValueParameterSymbol()
            containingDeclarationSymbol = this@buildNamedFunction.symbol
        }
    }

    private fun generateMainContentFunction(
        callableId: CallableId,
        context: MemberGenerationContext
    ): FirNamedFunction = buildNamedFunction {
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
                coneType =
                    session.symbolProvider.getClassLikeSymbolByClassId(Names.androidxComposableClass)!!
                        .defaultType()
            }

            argumentMapping = buildAnnotationArgumentMapping {}
        }

        annotations += buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                coneType =
                    session.symbolProvider.getClassLikeSymbolByClassId(Names.htmlComposableClass)!!
                        .defaultType()
            }

            argumentMapping = buildAnnotationArgumentMapping {}
        }
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
        if (context != null) {
            val (actualOrigin, type) = when (callableId.callableName.identifier) {
                "id" -> FirPageGenerated.PageIdProperty to session.builtinTypes.stringType.coneType
                "path" -> FirPageGenerated.PagePathProperty to session.builtinTypes.stringType.coneType
                "details" -> FirPageGenerated.PageDetailsProperty to session.symbolProvider.getClassLikeSymbolByClassId(
                    Names.pageDetailsClass
                )!!
                    .defaultType()

                "extensions" -> {
                    FirPageGenerated.PageExtensionsProperty to derivedExtensionMapType
                }

                "descriptor" -> FirPageGenerated.PageDescriptorProperty to session.symbolProvider.getClassLikeSymbolByClassId(
                    Names.serialDescriptorClass
                )!!.defaultType()

                else -> return emptyList()
            }

            return listOf(
                createMemberProperty(
                    context.owner,
                    actualOrigin,
                    callableId.callableName,
                    type
                ).symbol
            )
        } else return emptyList()
    }

    private val derivedExtensionMapType: ConeClassLikeType by lazy {
        val type =
            session.symbolProvider.getClassLikeSymbolByClassId(Names.abstractPageExtensionClass)!!
                .constructType(
                    arrayOf(
                        ConeStarProjection
                    )
                )

        val extensionsType = session.symbolProvider.getClassLikeSymbolByClassId(
            Names.mapClass
        )!!
            .constructType(
                arrayOf(
                    session.symbolProvider.getClassLikeSymbolByClassId(Names.kClassClass)!!
                        .constructType(
                            arrayOf(
                                ConeKotlinTypeProjectionOut(
                                    type
                                )
                            )
                        ),
                    type
                )
            )

        extensionsType
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
                    Name.identifier("serializer"),
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
                    Name.identifier("serializer"),
                )
            }

            FirDeclarationOrigin.Plugin(FirPageGenerated.PageSerializerClass) -> {
                setOf(
                    Name.identifier("descriptor"),
                    Name.identifier("serialize"),
                    Name.identifier("deserialize"),
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
        return when ((classSymbol.origin as? FirDeclarationOrigin.Plugin
            ?: return emptySet()).key) {
            FirPageGenerated.PageFactoryClass -> setOf(Name.identifier("Instance"))
            is FirPageGenerated.PageClass -> setOf(Name.identifier("Serializer"))
            else -> emptySet()
        }
    }

    private val symbols: List<FirFunctionSymbol<*>> by lazy {
        session.predicateBasedProvider.getSymbolsByPredicate(Predicates.isPage)
            .filterIsInstance<FirFunctionSymbol<*>>()
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelClassIds(): Set<ClassId> {
        return symbols
            .map { sym ->
                val id = sym.callableId
                ClassId(id.packageName, id.callableName)
            }
            .toSet()
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelCallableIds(): Set<CallableId> {
        return if (session.moduleData.platform.all { it is PotentiallyWebPlatform && it.isWeb }) {
            symbols
                .map { sym ->
                    val id = sym.callableId
                    CallableId(
                        id.packageName,
                        Name.special("<page ${id.callableName.asString()} entrypoint>")
                    )
                }
                .toSet()
        } else {
            emptySet()
        }
    }
}