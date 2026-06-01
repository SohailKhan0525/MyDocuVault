package com.mydocvault.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GlobalErrorBus {
    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val errors = _errors.asSharedFlow()

    fun emit(message: String) {
        _errors.tryEmit(message)
    }

    fun emit(throwable: Throwable, prefix: String? = null) {
        val fullMessage = buildString {
            if (!prefix.isNullOrBlank()) {
                append(prefix)
                append('\n')
            }
            append(throwable.stackTraceToString())
        }
        _errors.tryEmit(fullMessage)
    }
}
