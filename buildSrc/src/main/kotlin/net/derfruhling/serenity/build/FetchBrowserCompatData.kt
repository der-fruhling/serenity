package net.derfruhling.serenity.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Not applicable")
abstract class FetchBrowserCompatData : DefaultTask() {
    @get:OutputDirectory
    abstract val targetDir: DirectoryProperty

    @get:Inject
    abstract val execOps: ExecOperations

    @get:Inject
    abstract val providers: ProviderFactory

    init {
        outputs.upToDateWhen {
            val result = providers.exec {
                commandLine(
                    "git",
                    "fetch",
                    "--porcelain"
                )

                workingDir(targetDir)
            }

            result.result.get().rethrowFailure().assertNormalExitValue()
            result.standardOutput.asText.get().isBlank() && result.standardError.asText.get().isBlank()
        }
    }

    @TaskAction
    fun fetch() {
        val dir = targetDir.get()
        if(!dir.asFile.exists() || dir.asFile.listFiles().isNullOrEmpty()) {
            execOps.exec {
                commandLine(
                    "git",
                    "clone",
                    "https://github.com/mdn/browser-compat-data.git",
                    dir.asFile.absolutePath
                )
            }.rethrowFailure().assertNormalExitValue()
        } else {
            execOps.exec {
                commandLine("git", "pull")
                workingDir(dir)
            }.rethrowFailure().assertNormalExitValue()
        }
    }
}
