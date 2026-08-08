package net.derfruhling.serenity.test

import io.kotest.core.test.AbstractTestScope
import io.kotest.core.test.TestScope

class CompositionTestScope(delegate: TestScope) : AbstractTestScope(delegate) {

}