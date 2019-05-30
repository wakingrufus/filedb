package com.github.wakingrufus.filedb.jackson

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.wakingrufus.filedb.*

object mapper {
    val mapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
}

@FileDbDsl
inline fun <reified T> jacksonEntityType(name: String): EntityType<T> {
    return EntityType(
            name = name,
            deserializer = { mapper.mapper.readValue(it, T::class.java) },
            serializer = mapper.mapper::writeValueAsString)
}

@FileDbDsl
inline fun <reified T> FileDb.jacksonEntity(name: String): EntityAccessor<T> {
    return entity<T>(baseDir, jacksonEntityType(name)).also { accessorMap[T::class] = it }
}