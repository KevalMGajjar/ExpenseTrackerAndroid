package com.example.splitwiseclone.rest_api.security

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()

        val invocation = originalRequest.tag(Invocation::class.java)
        val requiresAuth = invocation?.method()?.isAnnotationPresent(RequiresAuth::class.java) == true

        if(requiresAuth) {
            val token = tokenManager.getAccessToken()
            if (token != null) {
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        return chain.proceed(originalRequest)
    }


}