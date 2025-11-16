package com.ranjan.data.util

class SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}