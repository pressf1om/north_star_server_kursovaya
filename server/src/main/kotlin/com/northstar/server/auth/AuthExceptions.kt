package com.northstar.server.auth

class InvalidCredentialsException : RuntimeException("Неверный логин или пароль")
class UsernameAlreadyExistsException(val username: String) : RuntimeException("Логин уже занят")
class UserNotFoundException : RuntimeException("Пользователь не найден")
class ValidationException(message: String) : RuntimeException(message)
