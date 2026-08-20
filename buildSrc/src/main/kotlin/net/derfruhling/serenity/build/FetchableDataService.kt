package net.derfruhling.serenity.build

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.process.ExecOperations
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.inject.Inject

abstract class FetchableDataParameters : BuildServiceParameters {
    abstract val dataLocation: DirectoryProperty
}

abstract class FetchableDataService : BuildService<FetchableDataParameters> {
    val httpClient = HttpClient.newBuilder().build()!!

    @get:Inject
    abstract val execOps: ExecOperations

    val browserCompatData by lazy {
        val dir = parameters.dataLocation.dir("browser-compat-data").get()



        dir
    }
}