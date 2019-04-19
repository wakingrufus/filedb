package com.github.wakingrufus.filedb

import mu.KLogging
import java.io.File
import java.util.*

actual class EntityAccessor<T>(rootDir: File, val type: EntityType<T>) {
    companion object : KLogging()

    val entityTypeDir = File(rootDir, type.name).apply {
        this.mkdir()
    }

    actual fun allLatest(): List<T> {
        return entityTypeDir.listFiles()
                .mapNotNull { it.listFiles().maxBy { it.name } }
                .map { type.deserializer(it.readText()) }
    }

    actual fun allVersions(id: String): List<T> {
        val entityDir = File(entityTypeDir, id)
        return if (entityDir.exists()) {
            entityDir.listFiles().map { type.deserializer(it.readText()) }
        } else {
            emptyList()
        }
    }

    actual fun latest(id: String): T? {
        val entityDir = File(entityTypeDir, id)
        return if (entityDir.exists()) {
            entityDir.listFiles().maxBy { it.name }
                    ?.let { type.deserializer(it.readText()) }
        } else {
            null
        }
    }

    actual fun create(data: T): String? {
        val id = UUID.randomUUID()
        val entityDir = File(entityTypeDir, id.toString())
        if (!entityDir.exists()) {
            entityDir.mkdir()
            val newFile = File(entityDir, "1")
            newFile.createNewFile()
            newFile.writeText(type.serializer(data))
            return id.toString()
        }
        return null

    }

    actual fun update(id: String, data: T) {
        val entityDir = File(entityTypeDir, id)
        if (!entityDir.exists()) {
            entityDir.mkdir()
        }
        val latestVersion = entityDir.list().mapNotNull { it.toIntOrNull() }.max() ?: 1
        val newFile = File(entityDir, (latestVersion + 1).toString())
        newFile.createNewFile()
        newFile.writeText(type.serializer(data))
    }
}
