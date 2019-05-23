package com.github.wakingrufus.filedb

import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

suspend fun <T> waitFor(work: () -> T,
                        condition: (T?) -> Boolean,
                        delay: Duration = Duration.ofMillis(100),
                        timeout: Duration = Duration.ofSeconds(10)): T? {
    val start = Instant.now()
    var result: T? = work()
    while (!condition(result) && Instant.now().isBefore(start.plus(timeout))) {
        result = work()
        delay(delay.toMillis())
    }
    return result

}

suspend fun <T> waitUntil(value: T,
                          delay: Duration = Duration.ofMillis(100),
                          timeout: Duration = Duration.ofSeconds(10),
                          work: () -> T): T? {
    return waitFor(work, { it == value }, delay, timeout)
}

suspend fun <T> waitForNotNull(delay: Duration = Duration.ofMillis(100),
                               timeout: Duration = Duration.ofSeconds(10),
                               work: () -> T): T? {
    return waitFor(work, { it != null }, delay, timeout)
}