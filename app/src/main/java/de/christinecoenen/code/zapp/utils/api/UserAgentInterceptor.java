package de.christinecoenen.code.zapp.utils.api;

import android.os.Build;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Locale;

import de.christinecoenen.code.zapp.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds a custom {@code User-Agent} header to OkHttp requests.
 */
public class UserAgentInterceptor implements Interceptor {

	private final String userAgent;

	public UserAgentInterceptor() {
		this(String.format(Locale.US,
			"Zapp/%s (Linux; Android %s)",
			BuildConfig.VERSION_NAME,
			Build.VERSION.RELEASE));
	}

	private UserAgentInterceptor(String userAgent) {
		this.userAgent = userAgent;
	}

	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException {
		Request userAgentRequest = chain.request()
			.newBuilder()
			.header("User-Agent", userAgent)
			.build();
		return chain.proceed(userAgentRequest);
	}
}
