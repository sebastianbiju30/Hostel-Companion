package com.example.hostel_companion.data

import com.example.hostel_companion.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.ktor.client.engine.okhttp.OkHttp
import kotlin.time.Duration.Companion.seconds


object SupabaseProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            httpEngine = OkHttp.create {
                config {
                    retryOnConnectionFailure(true)
                    followRedirects(true)
                    followSslRedirects(true)
                }
            }
            requestTimeout = 60.seconds
            install(Auth)
            install(Postgrest)
            install(Realtime)

        }
    }

    val auth get() = client.auth
    val db get() = client.postgrest
    val realtime get() = client.realtime

}

fun parseSupabaseErrorMessage(e: Throwable): String {
    e.printStackTrace()
    val rawMessage = e.message ?: e.localizedMessage ?: e.toString()
    val lower = rawMessage.lowercase()

    return when {
        lower.contains("invalid_grant") || lower.contains("invalid login credentials") || lower.contains("invalid_credentials") ->
            "Invalid email or password. Please check your credentials and try again."

        lower.contains("email_not_confirmed") || lower.contains("email not confirmed") ->
            "Email not confirmed. Please check your inbox or confirm your email address."

        lower.contains("user_already_exists") || lower.contains("already registered") ->
            "An account with this email address already exists. Try signing in instead."

        lower.contains("password should be at least") ->
            "Password must be at least 6 characters long."

        lower.contains("request timeout") || lower.contains("request_timeout") ||
        lower.contains("sockettimeoutexception") || lower.contains("httprequesttimeoutexception") ||
        lower.contains("timeout") ->
            "Connection timed out while reaching Supabase. Please check your network connection and try again."

        lower.contains("unknownhostexception") || lower.contains("unable to resolve host") ->
            "DNS Error: Unable to resolve host 'ilppisiwapbrsvewbsgc.supabase.co'. Please check your emulator/device DNS or internet connection."

        lower.contains("connectexception") || lower.contains("failed to connect") ->
            "Connection Error: Server refused connection or unreachable (${e.javaClass.simpleName})."

        lower.contains("sslhandshakeexception") || lower.contains("certpathvalidatingexception") ->
            "SSL/Security Error: Could not establish secure HTTPS connection (${e.javaClass.simpleName})."

        else -> {
            var cleaned = rawMessage
            if (cleaned.contains("failed with message:")) {
                cleaned = cleaned.substringAfter("failed with message:").trim()
            }
            cleaned = cleaned.trim('{', '}', '"', '\n', '\r', ' ')
            if (cleaned.isBlank()) {
                "Unable to connect to Supabase server (${e.javaClass.simpleName}: ${e.message ?: "No response"})."
            } else {
                cleaned
            }
        }
    }
}


