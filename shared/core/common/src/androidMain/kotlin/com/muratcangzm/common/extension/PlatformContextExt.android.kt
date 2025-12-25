package com.muratcangzm.common.extension

import android.content.Context
import com.muratcangzm.common.PlatformContext

/**
 * Other modules can access the Android application context safely
 * without exposing internal storage details.
 */
val PlatformContext.androidAppContext: Context
    get() = appContext