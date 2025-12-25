package com.muratcangzm.database

import app.cash.sqldelight.db.SqlDriver
import com.muratcangzm.common.PlatformContext
import com.muratcangzm.database.factory.DatabaseDriverFactory

class NervaDatabaseProvider(
    private val platformContext: PlatformContext
) {
    fun create(): NervaDatabase {
        val driver: SqlDriver = DatabaseDriverFactory(platformContext).createDriver()
        return NervaDatabase(driver = driver)
    }
}
