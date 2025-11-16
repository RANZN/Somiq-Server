package com.ranjan.data.common.extension

fun List<String>.toDbString() = joinToString(",")
fun String.toMediaUrls() = split(",").filter { it.isNotBlank() }