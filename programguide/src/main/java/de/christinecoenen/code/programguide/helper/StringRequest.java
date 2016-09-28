package de.christinecoenen.code.programguide.helper;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 * @see "http://stackoverflow.com/a/22694622/3012757"
 */
@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue", "JavaDoc"})
public class StringRequest extends Request<String> {
	private final Response.Listener<String> mListener;


	/**
	 * the parse charset.
	 */
	private String charset = null;

	/**
	 * Creates a new request with the given method.
	 *
	 * @param method the request {@link Method} to use
	 * @param url URL to fetch the string at
	 * @param listener Listener to receive the String response
	 * @param errorListener Error listener, or null to ignore errors
	 */
	public StringRequest(int method, String url, Response.Listener<String> listener,
						 Response.ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
	}

	/**
	 * Creates a new GET request.
	 *
	 * @param url URL to fetch the string at
	 * @param listener Listener to receive the String response
	 * @param errorListener Error listener, or null to ignore errors
	 */
	public StringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}

	/**
	 * Creates a new GET request with the given Charset.
	 *
	 * @param url URL to fetch the string at
	 * @param listener Listener to receive the String response
	 * @param errorListener Error listener, or null to ignore errors
	 */
	public StringRequest(String url, String charset, Response.Listener<String> listener, Response.ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
		this.charset = charset;
	}

	@Override
	protected void deliverResponse(String response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed;
		try {
			if(charset != null) {
				parsed = new String(response.data, charset);
			} else {
				parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			}
		} catch (UnsupportedEncodingException e) {
			parsed = new String(response.data);
		}
		return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
	}

	/**
	 * @return the Parse Charset Encoding
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * set the Parse Charset Encoding
	 * @param charset
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

}
