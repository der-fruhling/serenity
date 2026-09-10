package net.derfruhling.serenity.gradle.resources

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.*
import java.io.Serializable
import kotlin.reflect.KClass

@CacheableTask
abstract class SerenityComposeManifestTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFragments: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputManifest: RegularFileProperty

    @get:Input
    abstract val prettyJson: Property<Boolean>

    @get:ServiceReference("resourceVendor")
    abstract val vendorService: Property<ResourceVendorService>

    @get:Input
    abstract val modifiers: ListProperty<Action<ManifestEntry>>

    @get:Input
    abstract val extraValues: MapProperty<KClass<out ManifestEntry>, ManifestEntry>

    @TaskAction
    fun generate() {
        val json = vendorService.get().createJson(prettyJson.get())
        val fragments = sourceFragments.map { json.decodeFromString<ManifestEntry>(it.readText()) }.toMutableList()

        if(extraValues.get().isNotEmpty()) {
            outer@ for((key, value) in extraValues.get()) {
                for((i, existing) in fragments.withIndex()) {
                    if(key.isInstance(existing)) {
                        fragments[i] = value
                        break@outer
                    }
                }
            }
        }

        if(modifiers.get().isNotEmpty()) {
            for (it in modifiers.get()) {
                for(fragment in fragments) {
                    it.execute(fragment)
                }
            }
        }

        outputManifest.asFile.get().writeText(json.encodeToString(fragments))
    }

    fun include(vararg paths: Any?) {
        sourceFragments.from(*paths)
    }

    fun each(fn: Action<ManifestEntry>) {
        modifiers.add(fn)
    }

    fun <T> add(clazz: KClass<T>, value: T) where T : ManifestEntry, T : Serializable {
        extraValues.put(clazz, value)
    }

    fun <T : ManifestEntry> withType(clazz: Class<out T>, fn: Action<T>) {
        each {
            @Suppress("UNCHECKED_CAST")
            if(clazz.isInstance(this)) fn.execute(this as T)
        }
    }

    fun <T : ManifestEntry> withType(clazz: KClass<out T>, fn: (T) -> Unit) {
        each {
            @Suppress("UNCHECKED_CAST")
            if(clazz.isInstance(this)) fn(this as T)
        }
    }

    inline fun <reified T : ManifestEntry> withType(noinline fn: (T) -> Unit) {
        withType(T::class, fn)
    }
}
