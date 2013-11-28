package com.nkt.geomessenger.http;

import java.io.UnsupportedEncodingException;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;
import com.nkt.geomessenger.model.GsonConvertibleObject;

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by
 * Gson.
 */
public class GSONObjectRequest<T extends GsonConvertibleObject> extends
		Request<T> {
	private final Class<T> clazz;
	private final Listener<T> listener;

	/**
	 * Make a GET request and return a parsed object from JSON.
	 * 
	 * @param url
	 *            URL of the request to make
	 * @param clazz
	 *            Relevant class object, for Gson's reflection
	 * @param headers
	 *            Map of request headers
	 */
	public GSONObjectRequest(String url, Class<T> clazz, Listener<T> listener,
			ErrorListener errorListener) {
		super(Method.GET, url, errorListener);
		this.clazz = clazz;
		this.listener = listener;
	}

	@Override
	protected void deliverResponse(T response) {
		listener.onResponse(response);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			return Response.success(
					GsonConvertibleObject.getObjectFromJson(json, clazz),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException uee) {
			return Response.error(new ParseError(uee));
		} catch (JsonSyntaxException jsone) {
			return Response.error(new ParseError(jsone));
		}
	}
}