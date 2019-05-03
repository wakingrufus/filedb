package com.github.wakingrufus.filedb

import com.github.wakingrufus.filedb.common.EntityAccessor
import com.github.wakingrufus.filedb.common.EntityMap
import com.github.wakingrufus.filedb.common.EntityType
import com.github.wakingrufus.filedb.common.FileDbDsl
import java.io.File
import kotlin.reflect.KClass


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
