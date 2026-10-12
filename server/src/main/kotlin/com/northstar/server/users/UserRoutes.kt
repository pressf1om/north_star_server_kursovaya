package com.northstar.server.users

import com.northstar.server.plugins.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureUserRoutes(listUsersUseCase: ListUsersUseCase) {
    routing {
        authenticate("auth-jwt") {
            get("/api/users") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    call.respond(listUsersUseCase.execute(principal.status))
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Только для администратора"),
                    )
                }
            }
        }
    }
}
