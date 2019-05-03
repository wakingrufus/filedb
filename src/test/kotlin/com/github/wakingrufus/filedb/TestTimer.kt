package com.github.wakingrufus.filedb

import java.time.Duration
import java.time.Instant

fun time(work: () -> Unit): Duration {
    val start = Instant.now()
    work()
    val end = Instant.now()
    return Duration.between(start, end)
}