package com.muratcangzm.database.factory

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import com.muratcangzm.common.PlatformContext
import com.muratcangzm.common.extension.androidAppContext
import com.muratcangzm.database.NervaDatabase

actual class DatabaseDriverFactory actual constructor(
    private val platformContext: PlatformContext
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = NervaDatabase.Schema,
            context = platformContext.androidAppContext,
            name = "nerva.db"
        )
    }
}