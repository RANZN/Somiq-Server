package com.ranjan.domain.common.exceptions

class ForbiddenException(message: String) : RuntimeException(message)
class ResourceNotFoundException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)