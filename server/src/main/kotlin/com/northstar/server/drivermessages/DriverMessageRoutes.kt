package com.northstar.server.drivermessages

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

fun Application.configureDriverMessageRoutes(
    createDriverMessageUseCase: CreateDriverMessageUseCase,
    listDriverMessagesUseCase: ListDriverMessagesUseCase,
) {
    routing {
        authenticate("auth-jwt") {
            post("/api/driver-messages") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)
                try {
                    val request = call.receive<CreateDriverMessageRequest>()
                    val created = createDriverMessageUseCase.execute(
                        callerStatus = principal.status,
                        callerUserId = principal.userId,
                        request = request,
                    )
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
                }
            }

            get("/api/driver-messages") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val carNumber = call.request.queryParameters["car_number"]
                try {
                    call.respond(
                        listDriverMessagesUseCase.execute(
                            callerStatus = principal.status,
                            carNumber = carNumber,
                        ),
                    )
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Недостаточно прав"),
                    )
                }
            }
        }
    }
}
