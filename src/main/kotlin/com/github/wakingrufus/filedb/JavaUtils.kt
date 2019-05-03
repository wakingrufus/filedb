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
