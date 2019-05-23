package com.github.wakingrufus.filedb

import com.github.wakingrufus.filedb.common.EntityAccessor
import com.github.wakingrufus.filedb.common.EntityType
import mu.KLogging
import java.io.File
import java.util.*

class JavaEntityAccessor<T>(rootDir: File, val type: EntityType<T>) : EntityAccessor<T> {
    companion object : KLogging()

   private val entityTypeDir = File(rootDir, type.name).apply {
        this.mkdir()
    }

    override fun all(): List<T> {
        return entityTypeDir.listFiles().map {
            type.deserializer(it.readText())
        }
    }

    override fun allWithIds(): List<Pair<String, T>> {
        return entityTypeDir.listFiles().map {
            it.name.toString() to type.deserializer(it.readText())
        }
    }

    override fun get(id: String): T? {
        return File(entityTypeDir, id)
                .takeIf { it.exists() }
                .let { it?.readText() }
                .takeIf { it?.isNotEmpty() ?: false }
                ?.let { type.deserializer(it) }
    }

    override fun getRootFile(): File {
        return entityTypeDir
    }

    override fun create(data: T): String? {
        val id = UUID.randomUUID()
        val newFile = File(entityTypeDir, id.toString())
        if (!newFile.exists()) {
            newFile.createNewFile()
            newFile.writeText(type.serializer(data))
            return id.toString()
        }
        return null

    }

    override fun update(id: String, data: T) {
        val file = File(entityTypeDir, id)
        file.writeText(type.serializer(data))
    }
}
