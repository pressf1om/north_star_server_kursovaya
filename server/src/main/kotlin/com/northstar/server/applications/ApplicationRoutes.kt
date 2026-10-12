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
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun Application.configureApplicationRoutes(
    createApplicationUseCase: CreateApplicationUseCase,
    listApplicationsUseCase: ListApplicationsUseCase,
    deleteApplicationUseCase: DeleteApplicationUseCase,
    getApplicationByCarUseCase: GetApplicationByCarUseCase,
    updateApplicationStatusUseCase: UpdateApplicationStatusUseCase,
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

            get("/api/applications/by-car/{carNumber}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val raw = call.parameters["carNumber"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val carNumber = URLDecoder.decode(raw, StandardCharsets.UTF_8)
                try {
                    val app = getApplicationByCarUseCase.execute(
                        callerStatus = principal.status,
                        callerUserId = principal.userId,
                        carNumber = carNumber,
                    )
                    call.respond(app)
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Недостаточно прав"),
                    )
                } catch (_: ApplicationNotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "not_found", "message" to "Назначенных заявок нет"),
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

            patch("/api/applications/{id}/status") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)
                try {
                    val request = call.receive<UpdateApplicationStatusRequest>()
                    val updated = updateApplicationStatusUseCase.execute(
                        callerStatus = principal.status,
                        callerUserId = principal.userId,
                        applicationId = id,
                        request = request,
                    )
                    if (updated == null) {
                        call.respond(
                            UpdateApplicationStatusResponse(archived = true, application = null),
                        )
                    } else {
                        call.respond(
                            UpdateApplicationStatusResponse(archived = false, application = updated),
                        )
                    }
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Недостаточно прав"),
                    )
                } catch (_: ApplicationNotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "not_found", "message" to "Заявка не найдена"),
                    )
                } catch (_: InvalidStatusTransitionException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "invalid_transition", "message" to "Недопустимый переход статуса"),
                    )
                } catch (e: ValidationException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "validation_error", "message" to (e.message ?: "")),
                    )
                }
            }

            delete("/api/applications/{id}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                try {
                    deleteApplicationUseCase.execute(principal.status, id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (_: ForbiddenException) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "forbidden", "message" to "Только для администратора"),
                    )
                } catch (_: ApplicationNotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "not_found", "message" to "Заявка не найдена"),
                    )
                }
            }
        }
    }
}
