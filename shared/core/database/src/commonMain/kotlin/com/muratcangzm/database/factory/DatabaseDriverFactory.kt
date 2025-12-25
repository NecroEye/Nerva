package com.muratcangzm.database.factory

import app.cash.sqldelight.db.SqlDriver
import com.muratcangzm.common.PlatformContext

expect class DatabaseDriverFactory(platformContext: PlatformContext) {
    fun createDriver(): SqlDriver
}