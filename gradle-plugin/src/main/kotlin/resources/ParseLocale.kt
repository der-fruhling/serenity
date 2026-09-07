package net.derfruhling.serenity.gradle.resources

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

abstract class ParseLocaleParameters : WorkParameters {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFile: RegularFileProperty

    @get:Input
    abstract val config: Property<LocalizationConfig>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty
}

abstract class ParseLocaleAction : WorkAction<ParseLocaleParameters> {
    companion object {
        @ExperimentalSerializationApi
        val cbor by lazy {
            Cbor {
                preferCborLabelsOverNames = true
                useDefiniteLengthEncoding = true
            }
        }
    }

    override fun execute() {
        val localeFile = parameters.inputFile.get().asFile
        val outputFile = parameters.outputFile.get().asFile
        val languageTag = localeFile.nameWithoutExtension
        val mutableLocalizationResource = MutableLocalizationResource(languageTag)

        try {
            mutableLocalizationResource.parseFromXml(localeFile)
        } catch (e: Exception) {
            throw InvalidUserDataException("Error in locale part $localeFile", e)
        }

        @OptIn(ExperimentalSerializationApi::class)
        outputFile.writeBytes(cbor.encodeToByteArray(mutableLocalizationResource))
    }
}


abstract class CreateAvailableLocalizationsParameters : WorkParameters {
    @get:Input
    abstract val config: Property<LocalizationConfig>

    @get:Input
    abstract val languages: ListProperty<String>

    @get:Input
    abstract val baseUri: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val prettyJson: Property<Boolean>

    @get:Internal
    abstract val resourceVendorService: Property<ResourceVendorService>
}

abstract class CreateAvailableLocalizationsAction : WorkAction<CreateAvailableLocalizationsParameters> {
    override fun execute() {
        val outputFile = parameters.outputFile.get().asFile
        val baseUri = parameters.baseUri.get()
        val languages = mutableMapOf<String, String>()

        for (language in parameters.languages.get()) {
            languages[language] = "$baseUri/$language.dat"
        }

        val json = parameters.resourceVendorService.get().createJson(parameters.prettyJson.get())
        outputFile.writeText(
            json.encodeToString<ManifestEntry>(
                AvailableLocalizations(
                    parameters.config.get().baseLanguage,
                    languages
                )
            )
        )
    }
}

abstract class ParseLocale : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configFile: RegularFileProperty

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Input
    abstract val sourceDir: Property<String>

    @get:Input
    abstract val baseDir: Property<String>

    @get:OutputFile
    abstract val configOutput: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val prettyJson: Property<Boolean>

    @get:Inject
    abstract val executor: WorkerExecutor

    @get:ServiceReference("resourceVendor")
    abstract val resourceVendorService: Property<ResourceVendorService>

    @TaskAction
    fun parseLocales() {
        val config = try {
            Toml.decodeFromString<LocalizationConfig>(configFile.get().asFile.readText())
        } catch (e: Exception) {
            throw InvalidUserDataException("Error in ${configFile.get().asFile}", e)
        }

        val queue = executor.noIsolation()!!
        val languages = mutableListOf<String>()

        for (localeFile in sources) {
            check(localeFile.extension == "xml") { "Source files must have extension 'xml' (found '$localeFile')" }
            queue.submit(ParseLocaleAction::class) {
                inputFile.set(localeFile)
                outputFile.set(
                    outputDir.file(
                        localeFile.relativeTo(File(sourceDir.get())).let {
                            it.path.substringBeforeLast(".xml") + ".dat"
                        }
                    )
                )
                this.config.set(config)
            }

            languages.add(localeFile.nameWithoutExtension)
        }

        queue.submit(CreateAvailableLocalizationsAction::class) {
            this.config.set(config)
            outputFile.set(configOutput)
            resourceVendorService.set(this@ParseLocale.resourceVendorService)
            baseUri.set("/" + File(sourceDir.get()).toRelativeString(File(baseDir.get())))
            this.languages.set(languages)
            prettyJson.set(this@ParseLocale.prettyJson)
        }
    }
}