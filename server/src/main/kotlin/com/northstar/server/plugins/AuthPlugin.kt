package com.northstar.server.plugins

import com.northstar.server.auth.GetCurrentUserUseCase
import com.northstar.server.auth.JwtService
import com.northstar.server.auth.LoginUserUseCase
import com.northstar.server.auth.RegisterUserUseCase
import com.northstar.server.auth.configureAuthRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

data class UserPrincipal(
    val userId: Int,
    val status: String,
) : Principal

fun Application.configureAuth(
    registerUserUseCase: RegisterUserUseCase,
    loginUserUseCase: LoginUserUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "north-star"
            verifier(JwtService.verifier())
            validate { credential ->
                val userId = JwtService.userId(credential.payload) ?: return@validate null
                val status = credential.payload.getClaim("status").asString() ?: return@validate null
                UserPrincipal(userId, status)
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "unauthorized"),
                )
            }
        }
    }
    configureAuthRoutes(registerUserUseCase, loginUserUseCase, getCurrentUserUseCase)
}
