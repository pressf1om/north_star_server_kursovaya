package com.northstar.server.applications

import com.northstar.server.auth.ValidationException
import com.northstar.server.plugins.UserPrincipal
import com.northstar.server.users.ForbiddenException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.configureApplicationRoutes(
    createApplicationUseCase: CreateApplicationUseCase,
    listApplicationsUseCase: ListApplicationsUseCase,
) {
    routing {
        authenticate("auth-jwt") {
            get("/api/applications") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    call.respond(listApplicationsUseCase.execute(principal.status))
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Недостаточно прав"),
                    )
                }
            }

            post("/api/applications") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<CreateApplicationRequest>()
                    val created = createApplicationUseCase.execute(principal.status, request)
                    call.respond(HttpStatusCode.Created, created)
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Недостаточно прав"),
                    )
                } catch (e: ValidationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "validation_error", "message" to (e.message ?: "")),
                    )
                } catch (_: CarNotAvailableException) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "car_busy", "message" to "Машина уже занята"),
                    )
                }
            }
        }
    }
}
