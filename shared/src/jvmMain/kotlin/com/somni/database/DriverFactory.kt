package com.somni.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual fun createSqlDriver(
    databaseName: String,
    passphrase: ByteArray?,
): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    SomniDatabase.Schema.create(driver)
    return driver
}
