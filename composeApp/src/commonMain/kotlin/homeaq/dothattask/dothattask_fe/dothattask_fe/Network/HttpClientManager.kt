package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import homeaq.dothattask.dothattask_fe.dothattask_fe.Config.Environment
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AuthState
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private fun scheme(): URLProtocol = when (Environment.SCHEME) {
    "https" -> URLProtocol.HTTPS
    else -> URLProtocol.HTTP
}

fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            isLenient = true
            coerceInputValues = true
            ignoreUnknownKeys = true
        })
    }
    defaultRequest {
        AuthState.token?.let { token -> header("Authorization", "Basic $token") }
        url {
            protocol = scheme()
            host = Environment.HOST
            port = Environment.PORT
        }
    }
}

fun createHttpClient(token: String?) = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            isLenient = true
            coerceInputValues = true
            ignoreUnknownKeys = true
        })
    }
    defaultRequest {
        token?.let { header("Authorization", "Basic $it") }
        url {
            protocol = scheme()
            host = Environment.HOST
            port = Environment.PORT
        }
    }
}
