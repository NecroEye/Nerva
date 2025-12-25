package com.muratcangzm.database.factory

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.muratcangzm.common.PlatformContext
import com.muratcangzm.database.NervaDatabase

actual class DatabaseDriverFactory actual constructor(
    private val platformContext: PlatformContext
) {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = NervaDatabase.Schema,
            name = "nerva.db"
        )
    }
}