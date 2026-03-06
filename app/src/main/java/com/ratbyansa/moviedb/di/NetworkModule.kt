package com.ratbyansa.moviedb.di

import android.util.Log
import com.ratbyansa.moviedb.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

fun provideKtorClient() : HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorNetwork", message)
                }
            }
            level = (if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE)
        }

        defaultRequest {
            url(BuildConfig.baseUrl)
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${BuildConfig.accessToken}")
            // url.parameters.append("api_key", BuildConfig.apiKey)
        }
    }
}

val networkModule = module {
    single { provideKtorClient() }
}