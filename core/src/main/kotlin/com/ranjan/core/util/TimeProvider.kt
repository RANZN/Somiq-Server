package com.ranjan.core.util

import kotlinx.datetime.Instant

interface TimeProvider {
    fun nowMillis (): Long
    fun now(): Instant
}