package com.somni.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createSqlDriver(
    databaseName: String,
    passphrase: ByteArray?,
): SqlDriver {
    return NativeSqliteDriver(
        schema = SomniDatabase.Schema,
        name = databaseName,
    )
}
