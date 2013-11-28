package com.intel.identity.webview.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

/**
 * Class that manages all the http requests and responses related to the Intel
 * Identity Services to get access token, refresh token and user profile.
 * 
 * @author durantea
 * 
 */
public class OAuthSyncManager {

	private static final String CLIENT_ID = "6y7XHV4uMkobt2GQZs9ZbmzEzoWa5h1K";
	private static final String CLIENT_SECRET = "fS1CA2010Sc7rPWE";
	public static final String REDIRECT_URI = "intent://com.intel.identity.webview";
	public static final String IDENTITY_AUTH_URL = "https://api.intel.com/identityui/v2/auth?client_id="
			+ CLIENT_ID
			+ "&redirect_uri="
			+ REDIRECT_URI
			+ "&state=myAppSyncMode"
			+ "&scope=profile:full+user:details"
			+ "&auto_register=true"
			+ "&response_type=code";

	private Context mContext;

	public OAuthSyncManager(Context mContext) {
		this.mContext = mContext;
	}

	/**
	 * Gets the access token given the access code from parameter.
	 * 
	 * @param code
	 */
	public void getAccessToken(String code) {
		// STEP #3: call the /token API to get the access token

		// Set the parameters to the post request, required from Intel
		// Identity to getting the access token
		final List<NameValuePair> entityParams = new ArrayList<NameValuePair>();
		entityParams.add(new BasicNameValuePair("code", code));
		entityParams.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
		entityParams.add(new BasicNameValuePair("grant_type",
				"authorization_code"));
		entityParams.add(new BasicNameValuePair("client_id", CLIENT_ID));
		entityParams.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));

		// Executes the HTTP Post request
		final HttpResponse response = doPost(
				"https://api.intel.com/identity/v2/token", entityParams);

		try {
			// Parse the response looking for the access_token
			parseAccessTokenFromResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receives an HttpResponse object from parameter and parses it to get the
	 * access token, refresh token and expires in value. It saves the
	 * information in preferences. The expires in value stored in preferences is
	 * calculated as the current date (in long) plus the expires in value from
	 * the response.
	 * 
	 * @param response
	 * @throws ParseException
	 * @throws IOException
	 * @throws JSONException
	 */
	private void parseAccessTokenFromResponse(HttpResponse response)
			throws ParseException, IOException, JSONException {
		// Get the response from server and print it to the system log
		final String responseFromServer = EntityUtils.toString(response
				.getEntity());
		final int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			final JSONObject jsonResponse = new JSONObject(responseFromServer);

			final long expiresInValue = (jsonResponse.getInt("expires_in") * 1000)
					+ Calendar.getInstance().getTimeInMillis();

			// Save the access_token and refresh_token in preferences
			final AuthDataPreferences preferences = AuthDataPreferences
					.getInstance(mContext);

			preferences.setAccessToken(jsonResponse.getString("access_token"));
			preferences
					.setRefreshToken(jsonResponse.getString("refresh_token"));
			preferences.setExpiresIn(String.valueOf(expiresInValue));
		}

		Log.d("OAuthManager", "getAccessToken = " + responseFromServer
				+ " - status code = " + statusCode);
	}

	/**
	 * Make an Http request to the server to get the refresh token.
	 */
	public void getRefreshAccessToken() {
		// Get the refresh token saved in preferences
		final String refreshToken = AuthDataPreferences.getInstance(mContext)
				.getRefreshToken();

		// Set the parameters to the post request, required from Intel
		// Identity to getting the access token
		final List<NameValuePair> entityParams = new ArrayList<NameValuePair>();
		entityParams.add(new BasicNameValuePair("refresh_token", refreshToken));
		entityParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
		entityParams.add(new BasicNameValuePair("client_id", CLIENT_ID));
		entityParams.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));

		// Execute the HTTP Post request to /token API
		final HttpResponse response = doPost(
				"https://api.intel.com/identity/v2/token", entityParams);

		// Parse the response looking for the access_token
		try {
			parseAccessTokenFromResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Make an Http request to the server to get the User Details given the
	 * access token by parameter.
	 * 
	 * @param accessToken
	 * @return JSONObject user details from the response
	 */
	public JSONObject getUserDetails(String accessToken) {
		// STEP #4: consuming /user API
		final HttpResponse response = doGet(
				"https://api.intel.com/identity/v2/user", accessToken);

		// Parse the response looking for the access_token
		try {
			final String responseFromServer = EntityUtils.toString(response
					.getEntity());
			final int statusCode = response.getStatusLine().getStatusCode();

			Log.d("OAuthManager", "parseEmailFromResponse  = "
					+ responseFromServer + " - status code = " + statusCode);

			if (statusCode == HttpStatus.SC_OK) {
				final JSONObject jsonResponse = new JSONObject(
						responseFromServer);

				return jsonResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Make an Http request to the server to get the User Profile given the
	 * access token from parameters.
	 * 
	 * @param accessToken
	 * @return JSONObject user profile from the response
	 */
	public JSONObject getUserProfile(String accessToken) {
		// STEP #5: consuming /fullprofile based on the userId
		final HttpResponse response = doGet(
				"https://api.intel.com/profile/v2/users/me/fullprofile",
				accessToken);

		try {
			// Get the response from server and print it to the system log
			final String responseFromServer = EntityUtils.toString(response
					.getEntity());

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				final JSONObject jsonResponse = new JSONObject(
						responseFromServer);

				Log.d("OAuthManager", "getUserProfile = " + responseFromServer
						+ " - status code = " + statusCode);

				return jsonResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Executes all the steps to get the user details and profile: get the
	 * access token when the access code comes from parameters (first time the
	 * user authorizes the application, the server callback sends the access
	 * code), check if the access token expires so it can refresh the token when
	 * needed 3, get the access token from preferences so the application can
	 * get user information from the details and profile, get some information
	 * from the user profile and returns it.
	 * 
	 * @param authorizationCode
	 * @return String user details and profile information
	 */
	public String getUserProfileThreeStep(String authorizationCode) {
		// 1: Get the access token and save it to preferences (first time call
		// to this method)
		if (authorizationCode != null && authorizationCode.trim().length() != 0) {
			getAccessToken(authorizationCode);
		} else if (isAccessTokenExpired()) {
			getRefreshAccessToken();
		}

		final String accessToken = AuthDataPreferences.getInstance(mContext)
				.getAccessToken();

		// 2: Get the user details JSON object
		final JSONObject detailsJson = getUserDetails(accessToken);

		// 3: Get the user profile JSON object
		final JSONObject profileJson = getUserProfile(accessToken);

		try {
			if (detailsJson != null && profileJson != null) {
				// 4: Prepare the result

				// Get the basic profile from the JSONObject that comes from the
				// response
				final JSONObject basicProfile = profileJson
						.getJSONObject("basic");

				final StringBuilder userInfo = new StringBuilder();

				// Get the user email from User Details and, first name and last
				// name from the Basic Profile
				userInfo.append(String.valueOf(detailsJson.getJSONArray(
						"emails").get(0)));
				userInfo.append("\n");
				userInfo.append(basicProfile.getString("firstName"));
				userInfo.append(" ");
				userInfo.append(basicProfile.getString("lastName"));

				return userInfo.toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Validates if the access token stored on preferences expires. It compares
	 * the expiration date saved in preferences against the current date that
	 * the request to the server is been done.
	 * 
	 * @return true if the access token is expired, false if the access token is
	 *         not expired
	 */
	private boolean isAccessTokenExpired() {
		final long expiresIn = Long.parseLong(AuthDataPreferences.getInstance(
				mContext).getExpiresIn());

		final long currentTime = Calendar.getInstance().getTimeInMillis();
		final long timeLeft = expiresIn - currentTime;

		return (timeLeft <= 0);
	}

	/**
	 * Method that executes an HTTP Post request.
	 * 
	 * @param url
	 *            identity url
	 * @param entityParams
	 *            List<NameValuePair> entity params
	 * @return HttpResponse
	 */
	private HttpResponse doPost(String url,
			final List<NameValuePair> entityParams) {
		try {
			final HttpClient client = new DefaultHttpClient();
			final HttpPost request = new HttpPost(url);

			// Set the corresponding headers
			request.addHeader("Accept", "application/json");
			request.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			if (entityParams != null) {
				// Set the entity with the parameters
				request.setEntity(new UrlEncodedFormEntity(entityParams));
			}

			// Execute the HTTP Post request to the URL
			final HttpResponse response = client.execute(request);
			client.getConnectionManager().shutdown();

			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Method that executes HTTP Get request.
	 * 
	 * @param url
	 *            identity url
	 * @param accessToken
	 *            the access token
	 * @return HttpResponse
	 */
	private HttpResponse doGet(String url, String accessToken) {
		try {
			final HttpClient client = new DefaultHttpClient();
			final HttpGet request = new HttpGet(url);

			// Set the corresponding headers
			request.addHeader("Accept", "application/json");
			request.addHeader("Authorization", "Bearer " + accessToken);

			// Execute the HTTP Get request to the URL
			final HttpResponse response = client.execute(request);
			client.getConnectionManager().shutdown();

			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
