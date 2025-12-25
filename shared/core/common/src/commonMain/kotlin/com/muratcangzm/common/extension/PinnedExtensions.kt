package com.muratcangzm.common.extension


fun Long.toPinnedBoolean(): Boolean = this == 1L

fun Boolean.toPinnedLong(): Long = if (this) 1L else 0L