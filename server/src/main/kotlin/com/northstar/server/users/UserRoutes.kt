package com.northstar.server.users

import com.northstar.server.auth.UserNotFoundException
import com.northstar.server.auth.ValidationException
import com.northstar.server.plugins.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.routing

fun Application.configureUserRoutes(
    listUsersUseCase: ListUsersUseCase,
    updateUserRoleUseCase: UpdateUserRoleUseCase,
) {
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

            patch("/api/users/{id}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "bad_request", "message" to "Некорректный id"),
                    )
                try {
                    val request = call.receive<UpdateUserRoleRequest>()
                    val updated = updateUserRoleUseCase.execute(
                        callerStatus = principal.status,
                        userId = userId,
                        request = request,
                    )
                    call.respond(HttpStatusCode.OK, updated)
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
                } catch (_: UserNotFoundException) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "not_found", "message" to "Пользователь не найден"),
                    )
                }
            }
        }
    }
}
