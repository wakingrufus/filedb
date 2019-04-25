package com.github.wakingrufus.filedb

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.wakingrufus.filedb.common.EntityAccessor
import com.github.wakingrufus.filedb.common.EntityMap
import com.github.wakingrufus.filedb.common.EntityType
import com.github.wakingrufus.filedb.common.FileDbDsl
import mu.KLogging
import java.io.File
import kotlin.reflect.KClass

val logger = KLogging().logger("javaUtil")

object mapper {
    val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
}

inline fun <reified T> jacksonEntity(name: String): EntityType<T> {
    return EntityType(
            name = name,
            deserializer = { mapper.mapper.readValue(it, T::class.java) },
            serializer = mapper.mapper::writeValueAsString)
}

@FileDbDsl
fun fileDb(baseDir: File, schema: EntityMapBuilder.() -> Unit): EntityMap {
    return EntityMapBuilder(baseDir)
//            .apply {
//                entityBuilder = {jacksonEntity(it)}
//                accessorBuilder = {EntityAccessor(baseDir, it)}
//            }
            .apply(schema)()
}

@FileDbDsl
class EntityMapBuilder(val baseDir: File) {
    val map: MutableMap<KClass<*>, EntityType<*>> = mutableMapOf()
    val accessorMap: MutableMap<KClass<*>, EntityAccessor<*>> = mutableMapOf()
    inline fun <reified T> entity(entityType: EntityType<T>) {
        map[T::class] = entityType
        accessorMap[T::class] = JavaEntityAccessor(baseDir, entityType)
    }

    inline fun <reified T> entity(name: String) {
        map[T::class] = jacksonEntity<T>(name)
        accessorMap[T::class] = JavaEntityAccessor<T>(baseDir, jacksonEntity(name))
    }

    operator fun invoke(): EntityMap {
        return EntityMap(map.toMap(), accessorMap.toMap())
    }
}
