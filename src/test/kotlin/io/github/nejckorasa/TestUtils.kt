package io.github.nejckorasa

import kotlin.concurrent.thread

fun runConcurrently(numOfThreads: Int, block: () -> Unit) = 1.rangeTo(numOfThreads)
    .map { thread { block() } }
    .forEach { it.join() }
