package com.muratcangzm.nerva

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform