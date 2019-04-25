package com.github.wakingrufus.filedb.common

import kotlin.reflect.KClass

class EntityType<T>(val name: String,
                    val deserializer: (String) -> T,
                    val serializer: (T) -> String)

class EntityMap(val map: Map<KClass<*>, EntityType<*>>,
                val accessorMap: Map<KClass<*>, EntityAccessor<*>>) {
    inline fun <reified T> entityType(): EntityType<T> {
        return map[T::class] as EntityType<T>
    }

    inline fun <reified T> getAccessor(): EntityAccessor<T> {
        return accessorMap[T::class] as EntityAccessor<T>
    }
}

//expect class EntityAccessor<T> {
//    fun allLatest(): List<T>
//    fun allVersions(id: String): List<T>
//    fun latest(id: String): T?
//    fun create(data: T): String?
//    fun update(id: String, data: T)
//}

interface EntityAccessor<T> {
    fun allLatest(): List<T>
    fun allVersions(id: String): List<T>
    fun latest(id: String): T?
    fun create(data: T): String?
    fun update(id: String, data: T)
}

@DslMarker
annotation class FileDbDsl

//@FileDbDsl
//class EntityMapBuilder {
//     var entityBuilder : ((name:String) -> EntityType<Any>)? = null
//     var accessorBuilder : ((entityType: EntityType<*>) ->EntityAccessor<*>)? = null
//
//    val map: MutableMap<KClass<*>, EntityType<*>> = mutableMapOf()
//    val accessorMap: MutableMap<KClass<*>, EntityAccessor<*>> = mutableMapOf()
//
//    inline fun <reified T> entity(entityType: EntityType<T>) {
//        map[T::class] = entityType
//        accessorBuilder?.run {
//            accessorMap[T::class] = this(entityType)
//        }
//    }
//
//    inline fun <reified T> entity(name: String) {
//        entityBuilder?.also{
//            val entityType = it(name)
//            map[T::class] = entityType
//            accessorBuilder?.run {
//                accessorMap[T::class] = this(entityType)
//            }
//        }
//    }
//
//    operator fun invoke(): EntityMap {
//        return EntityMap(map.toMap(), accessorMap.toMap())
//    }
//}