package com.northstar.server.auth

import com.northstar.server.plugins.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Route.authRoutes(
    registerUserUseCase: RegisterUserUseCase,
    loginUserUseCase: LoginUserUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    post("/register") {
        try {
            val request = call.receive<RegisterRequest>()
            call.respond(HttpStatusCode.Created, registerUserUseCase.execute(request))
        } catch (e: UsernameAlreadyExistsException) {
            call.respond(
                HttpStatusCode.Conflict,
                mapOf("error" to "username_taken", "message" to "Этот логин уже занят"),
            )
        } catch (e: ValidationException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "validation_error", "message" to (e.message ?: "")),
            )
        }
    }

    post("/login") {
        try {
            val request = call.receive<LoginRequest>()
            call.respond(HttpStatusCode.OK, loginUserUseCase.execute(request))
        } catch (e: InvalidCredentialsException) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "invalid_credentials", "message" to "Неверный логин или пароль"),
            )
        }
    }

    post("/logout") {
        call.respond(HttpStatusCode.OK, MessageResponse("Выход выполнен"))
    }

    authenticate("auth-jwt") {
        get("/me") {
            val userId = call.principal<UserPrincipal>()?.userId
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            try {
                call.respond(getCurrentUserUseCase.execute(userId))
            } catch (e: UserNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "not_found"))
            }
        }
    }
}

fun Application.configureAuthRoutes(
    registerUserUseCase: RegisterUserUseCase,
    loginUserUseCase: LoginUserUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    routing {
        route("/api/auth") {
            authRoutes(registerUserUseCase, loginUserUseCase, getCurrentUserUseCase)
        }
    }
}
