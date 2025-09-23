package at.mocode

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform