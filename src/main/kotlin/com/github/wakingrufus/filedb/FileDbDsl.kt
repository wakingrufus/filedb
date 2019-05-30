package com.github.wakingrufus.filedb

import java.io.File

@DslMarker
annotation class FileDbDsl

@FileDbDsl
fun fileDb(baseDir: File, schema: FileDb.() -> Unit): FileDb {
    return FileDb(baseDir).apply(schema)
}

@FileDbDsl
inline fun <reified T> entity(baseDir: File, entityType: EntityType<T>): EntityAccessor<T> {
    return JavaEntityAccessor(baseDir, entityType)
}

