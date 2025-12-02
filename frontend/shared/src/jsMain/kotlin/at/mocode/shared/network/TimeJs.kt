package at.mocode.shared.network

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date().getTime().toLong()
