package com.nkt.geomessenger;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.intel.identity.webview.service.OAuthSyncManager;

public class WebViewFragmentActivity extends FragmentActivity {

	private static final String TAG = "OAuthFragmentActivity";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.oauth_fragment);

		WebView mWebView = (WebView) findViewById(R.id.oauth_webview);

		final FragmentActivity activity = this;

		// set the web client
		mWebView.setWebViewClient(new IdentityWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);

		// show progress and custom title
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				activity.setProgress(progress * 100);

				final int titleId = (progress == 100) ? R.string.webview_progress_title
						: R.string.webview_progress;
				activity.setTitle(titleId);
			}

		});

		// navigate the WebView to the authentication URL with its parameters
		mWebView.loadUrl(OAuthSyncManager.IDENTITY_AUTH_URL);
	}

	/**
	 * Custom implementation of WebViewClient to check the URLs navigation
	 * during the authentication and authorization process to intercept the
	 * redirect_uri from the server when the authorization code is ready, and
	 * then starts the CallbackActivity that parses the authorization code.
	 * 
	 * @author durantea
	 * 
	 */
	private class IdentityWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "The callback url is = " + url);

			// Final step in the Authentication flow: when the url matches with
			// the redirect_uri configured in the Dashboard, the
			// activity that process the authorization code starts
			if (url != null && url.startsWith(OAuthSyncManager.REDIRECT_URI)) {
				// Clear the cookies from the webview instance
				CookieManager.getInstance().removeAllCookie();
				// Start the CallbackActivity that parses the authorization code
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));

				startActivity(intent);
			} else {
				view.loadUrl(url);
			}

			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			// As the WebView doesn't notify to the user about certifications
			// errors and let him/her to continue or view the certificate, we
			// override onReceivedSsslError from the WebViewClient and set to
			// the SslErrorHandler to proceed the flow.
			handler.proceed();
		}

	}
}
