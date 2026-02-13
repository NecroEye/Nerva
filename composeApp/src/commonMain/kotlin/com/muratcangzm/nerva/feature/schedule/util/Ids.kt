package com.muratcangzm.nerva.feature.schedule.util

import kotlin.random.Random

private val Alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

fun newScheduleId(random: Random = Random): String {
    val sb = StringBuilder(20)
    repeat(20) { sb.append(Alphabet[random.nextInt(Alphabet.length)]) }
    return sb.toString()
}