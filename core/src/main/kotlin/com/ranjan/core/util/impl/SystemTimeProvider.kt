package com.ranjan.core.util.impl

import com.ranjan.core.util.TimeProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
    override fun now(): Instant = Clock.System.now()
}