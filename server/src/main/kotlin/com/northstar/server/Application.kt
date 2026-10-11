package com.northstar.server

import com.northstar.server.config.AppConfig
import com.northstar.server.db.DatabaseFactory
import com.northstar.server.plugins.configureFlyway
import com.northstar.server.plugins.configureRouting
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun main() {
    val server = embeddedServer(
        Netty,
        port = AppConfig.port,
        host = AppConfig.host,
        module = Application::module,
    )
    server.start(wait = true)
    runCatching { DatabaseFactory.close() }
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }
    configureFlyway()
    DatabaseFactory.init()
    configureRouting()
}
