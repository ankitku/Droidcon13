package com.nkt.geomessenger;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.intel.identity.webview.service.AuthDataPreferences;
import com.nkt.geomessenger.map.CustomerLocationUpdater;

public class LoginFragmentActivity extends FragmentActivity {

	private ImageButton btnSignIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (GeoMessenger.customerLocationUpdateHandler == null)
			GeoMessenger.customerLocationUpdateHandler = new CustomerLocationUpdater();

		GeoMessenger.customerLocationUpdateHandler.start(LoginFragmentActivity.this);

		btnSignIn = (ImageButton) findViewById(R.id.btn_sign_in);
		btnSignIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isConnectivityAvailable()) {
					doValidationAndNavigation();
				} else {
					Toast.makeText(LoginFragmentActivity.this,
							R.string.message_no_internet, Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}

	/**
	 * Open the activity that holds a webview to display the web pages from the
	 * authentication and authorization flow.
	 */
	private void showOAuthWebView() {
		// STEP #1: start the activity that holds a webview to allow the
		// user to login with an Intel or social account and authorizes the
		// application
		final Intent intent = new Intent(this, WebViewFragmentActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		// Start the activity
		startActivity(intent);
	}

	/**
	 * Open the CallbackActivity.
	 */
	private void showCallbackActivity() {
		final Intent intent = new Intent(this, CallbackFragmentActivity.class);
		intent.setData(Uri.parse("intent://com.intel.identity.webview/"));

		// Start the activity
		startActivity(intent);
	}

	/**
	 * Check if there is an access token saved in preferences, if any, open the
	 * callback activity, otherwise display the authentication web page in a
	 * WebView inside a fragment activity.
	 */
	private void doValidationAndNavigation() {
		final String accessToken = AuthDataPreferences.getInstance(this)
				.getAccessToken();

		if (accessToken.trim().length() == 0) {
			showOAuthWebView();
		} else {
			showCallbackActivity();
		}
	}

	/**
	 * Check if the device is connected to a network. In the case that the user
	 * has more than one network enable, such as wifi and data (3g, 4g), it
	 * checks the active network connectivity state.
	 * 
	 * @return boolean true if the active network is connected, false otherwise
	 */
	private boolean isConnectivityAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();

		return (info != null) ? info.isConnected() : false;
	}
}
