package net.derfruhling.serenity.compiler

import net.derfruhling.serenity.compiler.runners.AbstractJsBoxTest
import net.derfruhling.serenity.compiler.runners.AbstractJsDiagnosticTest
import net.derfruhling.serenity.compiler.runners.AbstractJvmBoxTest
import net.derfruhling.serenity.compiler.runners.AbstractJvmDiagnosticTest
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

fun main(args: Array<String>) {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testsRoot = args[0], testDataRoot = args[1]) {
            testClass<AbstractJvmDiagnosticTest> {
                model("diagnostics")
            }
            testClass<AbstractJsDiagnosticTest> {
                model("diagnostics")
            }

            testClass<AbstractJvmBoxTest> {
                model("box")
            }
            testClass<AbstractJsBoxTest> {
                model("box")
            }
        }
    }
}