package de.christinecoenen.code.zapp.utils.api

import android.os.Build
import de.christinecoenen.code.zapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds a custom `User-Agent` header to OkHttp requests.
 */
class UserAgentInterceptor : Interceptor {

	private val userAgent: String = "Zapp/${BuildConfig.VERSION_NAME} (Linux; Android ${Build.VERSION.RELEASE})"

	override fun intercept(chain: Interceptor.Chain): Response {
		val userAgentRequest = chain.request()
			.newBuilder()
			.header("User-Agent", userAgent)
			.build()

		return chain.proceed(userAgentRequest)
	}

}
