package com.somni.database

import app.cash.sqldelight.db.SqlDriver

expect fun createSqlDriver(
    databaseName: String,
    passphrase: ByteArray?,
): SqlDriver
