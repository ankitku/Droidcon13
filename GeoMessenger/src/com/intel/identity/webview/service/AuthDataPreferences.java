package com.intel.identity.webview.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AuthDataPreferences {

	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final String REFRESH_TOKEN = "refresh_token";

	private static AuthDataPreferences _instance;
	private SharedPreferences sharedPreferences;
	private Context mContext;

	private AuthDataPreferences(Context context) { 
		this.mContext = context;
		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
	}

	public static AuthDataPreferences getInstance(Context context) {
		if (_instance == null) {
			_instance = new AuthDataPreferences(context);
		}

		return _instance;
	}

	public String getAccessToken() {
		return sharedPreferences.getString(ACCESS_TOKEN, "");
	}

	public String getExpiresIn() {
		return sharedPreferences.getString(EXPIRES_IN, "");
	}

	public String getRefreshToken() {
		return sharedPreferences.getString(REFRESH_TOKEN, "");
	}

	public void setAccessToken(String accessToken) {
		SharedPreferences.Editor edit = sharedPreferences.edit();
		edit.putString(ACCESS_TOKEN, accessToken);
		edit.commit();
	}

	public void setExpiresIn(String expiresIn) {
		SharedPreferences.Editor edit = sharedPreferences.edit();
		edit.putString(EXPIRES_IN, expiresIn);
		edit.commit();
	}

	public void setRefreshToken(String refreshToken) {
		SharedPreferences.Editor edit = sharedPreferences.edit();
		edit.putString(REFRESH_TOKEN, refreshToken);
		edit.commit();
	}
}
