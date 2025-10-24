package com.example.cos.lifecycle

import javax.inject.Inject

interface TimeProvider {
    fun tickMillis(): Long = 1_000L
}

class DefaultTimeProvider @Inject constructor() : TimeProvider
