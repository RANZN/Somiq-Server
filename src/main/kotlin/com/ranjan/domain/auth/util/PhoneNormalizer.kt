package com.ranjan.domain.auth.util

/** Normalizes to digits only (e.g. +1 234-567-8901 → 12345678901). */
fun normalizePhone(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    require(digits.length >= 10) { "PHONE_INVALID" }
    return digits
}
