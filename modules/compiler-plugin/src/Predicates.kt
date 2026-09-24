package net.derfruhling.serenity.compiler

import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.name.FqName

object Predicates {
    val isPage = LookupPredicate.create {
        annotated(FqName("net.derfruhling.serenity.annotations.Page"))
    }
}