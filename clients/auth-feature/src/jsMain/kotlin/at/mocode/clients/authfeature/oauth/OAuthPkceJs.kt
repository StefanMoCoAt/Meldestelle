package at.mocode.clients.authfeature.oauth

import at.mocode.clients.shared.AppConfig
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise
import kotlin.random.Random

private var currentPkce: PkceState? = null

private fun base64UrlFromBytes(bytes: ByteArray): String {
    // Build binary string from bytes
    val sb = StringBuilder(bytes.size)
    for (b in bytes) sb.append(b.toInt().toChar())
    val b64 = window.btoa(sb.toString())
    return b64.replace("+", "-").replace("/", "_").trimEnd('=')
}

private fun base64UrlFromArrayBuffer(buf: ArrayBuffer): String {
    val arr = Uint8Array(buf)
    var binary = ""
    val len = arr.length
    for (i in 0 until len) {
        val v = (arr.asDynamic()[i] as Number).toInt()
        binary += fromCharCode(v)
    }
    val b64 = window.btoa(binary)
    return b64.replace("+", "-").replace("/", "_").trimEnd('=')
}

private fun randomUrlSafe(length: Int): String {
    val bytes = Random.Default.nextBytes(length)
    // Use base64url for entropy; ensure URL-safe by replacing padding removed already
    return base64UrlFromBytes(bytes)
}

private fun sha256(input: String): Promise<ArrayBuffer> {
    val enc: dynamic = js("new TextEncoder()")
    val data = enc.encode(input)
    val subtle: dynamic = window.asDynamic().crypto.subtle
    return subtle.digest("SHA-256", data) as Promise<ArrayBuffer>
}

actual object OAuthPkceService {
    actual suspend fun startAuth(): PkceState {
        val codeVerifier = randomUrlSafe(64)
        val challengeBuf = sha256(codeVerifier).await()
        val codeChallenge = base64UrlFromArrayBuffer(challengeBuf)
        val state = randomUrlSafe(16)
        val pkce = PkceState(state, codeVerifier, codeChallenge)
        currentPkce = pkce
        return pkce
    }

    actual fun current(): PkceState? = currentPkce

    actual fun clear() { currentPkce = null }

    actual fun buildAuthorizeUrl(state: PkceState, redirectUri: String): String {
        val params = listOf(
            "response_type" to OAuthParams.RESPONSE_TYPE,
            "client_id" to AppConfig.KEYCLOAK_CLIENT_ID,
            "redirect_uri" to redirectUri,
            "scope" to OAuthParams.SCOPE,
            "state" to state.state,
            "code_challenge" to state.codeChallenge,
            "code_challenge_method" to state.method
        ).joinToString("&") { (k, v) -> "$k=" + encodeURIComponent(v) }
        return AppConfig.authorizeEndpoint() + "?" + params
    }
}

@Suppress("UnsafeCastFromDynamic")
private fun encodeURIComponent(value: String): String = js("encodeURIComponent")(value)

@Suppress("UnsafeCastFromDynamic")
private fun fromCharCode(code: Int): String = js("String.fromCharCode")(code)
