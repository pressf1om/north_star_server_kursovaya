package com.northstar.server.applications

import com.northstar.server.auth.ValidationException
import com.northstar.server.plugins.UserPrincipal
import com.northstar.server.users.ForbiddenException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun Application.configureCompletedApplicationRoutes(
    listCompletedApplicationsUseCase: ListCompletedApplicationsUseCase,
) {
    routing {
        authenticate("auth-jwt") {
            get("/api/completed_applications") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    call.respond(listCompletedApplicationsUseCase.listAll(principal.status))
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Недостаточно прав"),
                    )
                }
            }

            get("/api/completed_applications/{carNumber}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val raw = call.parameters["carNumber"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val carNumber = URLDecoder.decode(raw, StandardCharsets.UTF_8)
                try {
                    call.respond(
                        listCompletedApplicationsUseCase.listByCar(
                            callerStatus = principal.status,
                            callerUserId = principal.userId,
                            carNumber = carNumber,
                        ),
                    )
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
                }
            }
        }
    }
}
