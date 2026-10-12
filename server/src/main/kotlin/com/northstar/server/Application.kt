package com.northstar.server

import com.northstar.server.auth.ExposedUserAuthRepository
import com.northstar.server.auth.GetCurrentUserUseCase
import com.northstar.server.auth.LoginUserUseCase
import com.northstar.server.auth.RegisterUserUseCase
import com.northstar.server.config.AppConfig
import com.northstar.server.db.DatabaseFactory
import com.northstar.server.plugins.configureAuth
import com.northstar.server.plugins.configureFlyway
import com.northstar.server.plugins.configureRouting
import com.northstar.server.cars.ExposedCarAdminRepository
import com.northstar.server.cars.CreateCarUseCase
import com.northstar.server.cars.ListCarsUseCase
import com.northstar.server.cars.configureCarRoutes
import com.northstar.server.users.ExposedUserAdminRepository
import com.northstar.server.users.ListUsersUseCase
import com.northstar.server.users.UpdateUserRoleUseCase
import com.northstar.server.users.configureUserRoutes
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

    val userAuthRepository = ExposedUserAuthRepository()
    val registerUserUseCase = RegisterUserUseCase(userAuthRepository)
    val loginUserUseCase = LoginUserUseCase(userAuthRepository)
    val getCurrentUserUseCase = GetCurrentUserUseCase(userAuthRepository)

    configureAuth(registerUserUseCase, loginUserUseCase, getCurrentUserUseCase)

    val userAdminRepository = ExposedUserAdminRepository()
    val carAdminRepository = ExposedCarAdminRepository()
    val listUsersUseCase = ListUsersUseCase(userAdminRepository)
    val updateUserRoleUseCase = UpdateUserRoleUseCase(userAdminRepository, carAdminRepository)
    val listCarsUseCase = ListCarsUseCase(carAdminRepository)
    val createCarUseCase = CreateCarUseCase(carAdminRepository)
    configureUserRoutes(listUsersUseCase, updateUserRoleUseCase)
    configureCarRoutes(listCarsUseCase, createCarUseCase)

    configureRouting()
}
