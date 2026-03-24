package com.ranjan.domain.auth.util

private val USER_ID_REGEX = Regex("^[a-zA-Z0-9_]+$")

fun validatePublicUserId(userId: String) {
    require(userId.length >= 3) { "USER_ID_TOO_SHORT" }
    require(userId.length <= 50) { "USER_ID_TOO_LONG" }
    require(userId.matches(USER_ID_REGEX)) { "USER_ID_INVALID_FORMAT" }
}
