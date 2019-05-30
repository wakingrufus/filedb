package com.github.wakingrufus.filedb

import java.io.File
import kotlin.reflect.KClass

class EntityType<T>(val name: String,
                    val deserializer: (String) -> T,
                    val serializer: (T) -> String)

@FileDbDsl
class FileDb(val baseDir: File) {
   val accessorMap: MutableMap<KClass<*>, EntityAccessor<*>> = mutableMapOf()
    inline fun <reified T> entity(entityType: EntityType<T>): EntityAccessor<T> {
        return entity(baseDir, entityType).also{ accessorMap[T::class] = it }
    }

    inline fun <reified T> getAccessor(): EntityAccessor<T> {
        return accessorMap[T::class] as EntityAccessor<T>
    }
}