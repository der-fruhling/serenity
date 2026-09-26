package net.derfruhling.serenity.compiler.services

import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

class JavaScriptRuntimeClassProvider(testServices: TestServices) : RuntimeClasspathProvider(
    testServices
) {
    override fun runtimeClassPaths(module: TestModule): List<File> {
        return listOf()
    }
}