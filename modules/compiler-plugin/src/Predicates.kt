package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate.BuilderContext.metaAnnotated
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.FqName

object Predicates {
    val isPage = LookupPredicate.create {
        annotated(FqName("net.derfruhling.serenity.annotations.Page"))
    }

    val isIntrinsic = DeclarationPredicate.create {
        annotated(FqName("net.derfruhling.serenity.annotations.Intrinsic"))
    }

    val isClientFun = DeclarationPredicate.create {
        metaAnnotated(Names.clientClass.asSingleFqName(), includeItself = true)
    }

    val isServerFun = DeclarationPredicate.create {
        metaAnnotated(Names.serverClass.asSingleFqName(), includeItself = true)
    }
}