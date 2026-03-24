package com.ranjan.domain.exception

/** Invalid user id in route or query (HTTP 400). */
class InvalidUserIdException : RuntimeException()

/** Auth required but missing or invalid (HTTP 401). */
class UnauthorizedException : RuntimeException()

/** Entity does not exist (HTTP 404). */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/** Action not allowed for this principal (HTTP 403). */
class ForbiddenException(message: String) : RuntimeException(message)

/** Input validation failed (HTTP 400). */
class ValidationException(message: String) : RuntimeException(message)
