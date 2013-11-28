package com.nkt.geomessenger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.geo.GeoDataManager;
import com.amazonaws.geo.GeoDataManagerConfiguration;
import com.amazonaws.geo.model.GeoPoint;
import com.amazonaws.geo.model.PutPointRequest;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.intel.identity.webview.service.AuthDataPreferences;
import com.intel.identity.webview.service.OAuthSyncManager;

/**
 * Callback Activity that is started from the web view activity or manually. It
 * handles the user profile once the access code is given.
 * 
 * @author durantea
 * 
 */
public class CallbackFragmentActivity extends FragmentActivity {

	private View mResult;
	private View mProgress;
	private TextView txtvProfile;
	private Button btnLogout;
	
	private GeoDataManager geoDataManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.callback_activity);

		mProgress = findViewById(R.id.pg_loading);
		mResult = findViewById(R.id.ly_result);

		txtvProfile = (TextView) findViewById(R.id.txtv_log);

		btnLogout = (Button) findViewById(R.id.btn_logout);
		btnLogout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LogoutAsync async = new LogoutAsync();
				async.execute();
			}

		});

		final Uri uri = getIntent().getData();
		String authorizationCode = null;

		// check if was started after the Intel Identity sign in
		if (uri != null) {
			// STEP #2: receive the /auth and get the authorization code in the
			// callback
			authorizationCode = uri.getQueryParameter("code");

			Log.d("CallbackActivity", uri.toString());
		}
		// else, the activity was started directly from the MainActivity because
		// there's an access token saved in preferences

		// get the user profile
		GetProfileAsync async = new GetProfileAsync();
		async.execute(authorizationCode);
	}

	/**
	 * Custom AsyncTask implementation to get the user profile in the background
	 * and update the UI when needed.
	 * 
	 * @author durantea
	 * 
	 */
	class GetProfileAsync extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... args) {
			final OAuthSyncManager mOAuth = new OAuthSyncManager(
					getApplicationContext());

			return mOAuth.getUserProfileThreeStep(args[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			final String text = (result != null) ? result.toString()
					: getString(R.string.message_result_is_null);
			txtvProfile.setText(text);
			setupTable();

			mResult.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.GONE);

			super.onPostExecute(result);
		}
	}

	/**
	 * Custom AsyncTask implementation to reset/delete the values setted in
	 * preferences related to the access token, refresh and time that expires
	 * the token.
	 * 
	 * @author durantea
	 * 
	 */
	class LogoutAsync extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			final Context mContext = getApplicationContext();

			AuthDataPreferences.getInstance(mContext).setAccessToken("");
			AuthDataPreferences.getInstance(mContext).setRefreshToken("");
			AuthDataPreferences.getInstance(mContext).setExpiresIn("");

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Creates an Intent to bring back the MainActivity from the stack
			Intent intent = new Intent(getApplicationContext(),
					LoginFragmentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

			finish();
		}
	}
	
	private void setupTable()
	{
		AmazonDynamoDBClient ddb = new AmazonDynamoDBClient(new ClasspathPropertiesFileCredentialsProvider());
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		ddb.setRegion(usWest2);

		ClientConfiguration clientConfiguration = new ClientConfiguration().withMaxErrorRetry(5);
		ddb.setConfiguration(clientConfiguration);

		GeoDataManagerConfiguration config = new GeoDataManagerConfiguration(ddb, "geo-messages");
		geoDataManager = new GeoDataManager(config);
	}
	
	public void saveGeoMessage(View v)
	{
		GeoPoint geoPoint = new GeoPoint(47.6456, -122.3350);
		AttributeValue rangeKeyValue = new AttributeValue().withS("POI_00001");
		AttributeValue titleValue = new AttributeValue().withS("Gas Works Park");
		 
		PutPointRequest putPointRequest = new PutPointRequest(geoPoint, rangeKeyValue);
		putPointRequest.getPutItemRequest().getItem().put("title", titleValue);
		 
		geoDataManager.putPoint(putPointRequest);	
	}
}
