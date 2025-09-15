package at.mocode

expect object ApiConfig {
    val baseUrl: String
    val pingEndpoint: String
}
