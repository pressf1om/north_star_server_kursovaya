package com.northstar.server.auth

class RegisterUserUseCase(
    private val repository: UserAuthRepository,
) {
    suspend fun execute(request: RegisterRequest): AuthResponse {
        val username = request.username.trim()
        val email = request.email.trim()
        validateUsername(username)
        validateEmail(email)
        validatePassword(request.password)
        if (repository.findByUsername(username) != null) {
            throw UsernameAlreadyExistsException(username)
        }
        val user = repository.create(
            email = email,
            username = username,
            passwordHash = PasswordHasher.hash(request.password),
            status = UserStatus.PENDING,
        )
        return AuthResponse(
            token = JwtService.issue(user.id, user.username, user.status),
            user = user.toPublicDto(),
        )
    }
}

class LoginUserUseCase(
    private val repository: UserAuthRepository,
) {
    suspend fun execute(request: LoginRequest): AuthResponse {
        val username = request.username.trim()
        val user = repository.findByUsername(username) ?: throw InvalidCredentialsException()
        if (!PasswordHasher.verify(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        val car = if (user.status == UserStatus.DRIVER) {
            repository.findCarByDriverId(user.id)
        } else {
            null
        }
        return AuthResponse(
            token = JwtService.issue(user.id, user.username, user.status),
            user = user.toPublicDto(car),
        )
    }
}

class GetCurrentUserUseCase(
    private val repository: UserAuthRepository,
) {
    suspend fun execute(userId: Int): MeResponse {
        val user = repository.findById(userId) ?: throw UserNotFoundException()
        val car = if (user.status == UserStatus.DRIVER) {
            repository.findCarByDriverId(user.id)
        } else {
            null
        }
        return MeResponse(user = user.toPublicDto(car))
    }
}

private fun validateUsername(username: String) {
    if (username.length < 3) {
        throw ValidationException("Логин должен быть не короче 3 символов")
    }
}

private fun validateEmail(email: String) {
    if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
        throw ValidationException("Укажите корректную почту")
    }
}

private fun validatePassword(password: String) {
    if (password.length < 6) {
        throw ValidationException("Пароль должен быть не короче 6 символов")
    }
}
