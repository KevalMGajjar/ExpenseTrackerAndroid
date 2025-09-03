package com.example.splitwiseclone.rest_api.security

import com.example.splitwiseclone.rest_api.RefreshTokenRequest
import com.example.splitwiseclone.rest_api.RestApiService
import javax.inject.Provider
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import retrofit2.Retrofit

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val retrofitProvider: Provider<Retrofit>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        synchronized(this) {
            val currentAccessToken = tokenManager.getAccessToken()
            val requestThatFailed = response.request

            val authHeader = requestThatFailed.header("Authorization")
            if (authHeader != null && authHeader == "Bearer $currentAccessToken") {

                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken == null) {
                    tokenManager.clearTokens()
                    return null
                }

                val newAccessToken = performTokenRefresh(refreshToken)

                if (newAccessToken == null) {
                    tokenManager.clearTokens()
                    return null
                }

                return requestThatFailed.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${tokenManager.getAccessToken()}")
            .build()
    }


    private fun performTokenRefresh(refreshToken: String): String? {

        val authApiService = retrofitProvider.get().create(RestApiService::class.java)

        return try {
            val refreshResponse = authApiService.refreshToken(RefreshTokenRequest(refreshToken)).execute()

            if (refreshResponse.isSuccessful) {
                val newTokens = refreshResponse.body()
                if (newTokens != null) {
                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                    newTokens.accessToken
                } else {
                    null // Successful response but empty body
                }
            } else {
                null // The refresh API call was not successful
            }
        } catch (e: Exception) {
            null // A network error occurred
        }
    }
}