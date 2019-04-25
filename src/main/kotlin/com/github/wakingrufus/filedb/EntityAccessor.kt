package com.github.wakingrufus.filedb

import com.github.wakingrufus.filedb.common.EntityAccessor
import com.github.wakingrufus.filedb.common.EntityType
import mu.KLogging
import java.io.File
import java.util.*

class JavaEntityAccessor<T>(rootDir: File, val type: EntityType<T>) : EntityAccessor<T> {
    companion object : KLogging()

    val entityTypeDir = File(rootDir, type.name).apply {
        this.mkdir()
    }

    override fun allLatest(): List<T> {
        return entityTypeDir.listFiles()
                .mapNotNull { it.listFiles().maxBy { it.name } }
                .map { type.deserializer(it.readText()) }
    }

    override fun allVersions(id: String): List<T> {
        val entityDir = File(entityTypeDir, id)
        return if (entityDir.exists()) {
            entityDir.listFiles().map { type.deserializer(it.readText()) }
        } else {
            emptyList()
        }
    }

    override fun latest(id: String): T? {
        val entityDir = File(entityTypeDir, id)
        return if (entityDir.exists()) {
            entityDir.listFiles().maxBy { it.name }
                    ?.let { type.deserializer(it.readText()) }
        } else {
            null
        }
    }

    override fun create(data: T): String? {
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

    override fun update(id: String, data: T) {
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
