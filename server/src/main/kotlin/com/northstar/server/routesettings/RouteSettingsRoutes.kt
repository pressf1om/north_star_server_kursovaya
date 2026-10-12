package com.northstar.server.routesettings

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
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

fun Application.configureRouteSettingsRoutes(
    getRouteSettingsUseCase: GetRouteSettingsUseCase,
    updateRouteSettingsUseCase: UpdateRouteSettingsUseCase,
) {
    routing {
        authenticate("auth-jwt") {
            get("/api/route-settings") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    call.respond(getRouteSettingsUseCase.execute(principal.status))
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Только для администратора"),
                    )
                }
            }

            put("/api/route-settings") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<UpdateRouteSettingsRequest>()
                    call.respond(updateRouteSettingsUseCase.execute(principal.status, request))
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Только для администратора"),
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
