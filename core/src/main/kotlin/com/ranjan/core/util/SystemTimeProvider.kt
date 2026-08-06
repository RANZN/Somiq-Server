package com.ranjan.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
    override fun now(): Instant = Clock.System.now()
}