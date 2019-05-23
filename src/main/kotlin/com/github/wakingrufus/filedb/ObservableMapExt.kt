package com.github.wakingrufus.filedb

import com.github.wakingrufus.filedb.common.EntityAccessor
import javafx.collections.ObservableMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds.*

fun <V> ObservableMap<String, V>.bindTo(entityAccessor: EntityAccessor<V>) {
    entityAccessor.allWithIds().forEach {
        this.put(it.first, it.second)
    }
    val watchService = FileSystems.getDefault().newWatchService()
    val watchKey = entityAccessor.getRootFile().toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
    GlobalScope.launch {
        while (this.isActive) {
            watchKey.pollEvents()
                    .also {
                        if (it.size > 0) {
                            logger.debug("processing ${it.size} events")
                        }
                    }
                    .groupBy { it.context() }
                    .map { it.value.last() }
                    .forEach {
                        if (it.context() != ".tmp") {
                            logger.info("FS event detected: $it")
                            val id = it.context().toString()
                            when (it.kind()) {
                                ENTRY_CREATE -> it
                                        .also { logger.info("Create event: $id") }
                                        .let { waitForNotNull { entityAccessor.get(id) } }
                                        .also { logger.info("Read object $it") }
                                        .takeIf { it != null }
                                        .run { put(id, this) }
                                ENTRY_DELETE -> remove(id)
                                ENTRY_MODIFY -> it
                                        .also { logger.info("Update event: $id") }
                                        .let { waitForNotNull { entityAccessor.get(id) } }
                                        .also { logger.info("Read object $it") }
                                        .takeIf { it != null }
                                        .run { put(id, this) }
                                else -> {
                                }
                            }
                        }
                    }
        }
    }
}