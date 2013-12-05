package com.nkt.geomessenger;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nkt.geomessenger.constants.Constants;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.map.CustomerLocationUpdater;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.service.PollGeoMessagesService;

/**
 * Callback Activity that is started from the web view activity or manually. It
 * handles the user profile once the access code is given.
 * 
 * @author durantea
 * 
 */
public class CallbackFragmentActivity extends GMActivity {

	private View mResult;
	private View mProgress;
	private LinearLayout bottomView;
	private Handler handler;
	private EditText emailText, msgText;
	private Button saveButton;
	private TextView t1, t2;
	private boolean fieldsVisible;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				//String string = bundle.getString(PollGeoMessagesService.FILEPATH);
				int resultCode = bundle.getInt(PollGeoMessagesService.RESULT);
				if (resultCode == RESULT_OK) {
					mapFragment.clear();
					if (GeoMessenger.geoMessages != null) {
						for (GeoMessage gm : GeoMessenger.geoMessages
								.getResult()) {
							LatLng p = new LatLng(Double.parseDouble(gm
									.getLatitude()), Double.parseDouble(gm
									.getLongitude()));
							Marker m = mapFragment
									.addMarker(new MarkerOptions()
											.position(p)
											.title(gm.getFromName() + " ( "
													+ gm.getFromEmail() + " ) "
													+ " says: ")
											.snippet(gm.getGeoMessage()));
							m.showInfoWindow();
						}
					}
				}
			}
		}
	};

	protected MenuItem action_signout, menu_legalnotices;

	protected GoogleMap mapFragment;
	Animation animateBottomViewOut, animateBottomViewIn;

	// Global constants
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	protected final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		protected Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	protected boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Get the error code
			int errorCode = resultCode;
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
			return false;
		}
	}

	private void createUIElements() {
		mProgress = findViewById(R.id.pg_loading);
		mResult = findViewById(R.id.ly_result);
		bottomView = (LinearLayout) findViewById(R.id.bottom_view);

		emailText = (EditText) findViewById(R.id.email_text);
		msgText = (EditText) findViewById(R.id.msg_text);
		saveButton = (Button) findViewById(R.id.btn_save);

		t1 = (TextView) findViewById(R.id.t1);
		t2 = (TextView) findViewById(R.id.t2);

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showFields();
			}
		});

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		handler = new Handler();

		if (GeoMessenger.customerLocationUpdateHandler == null)
			GeoMessenger.customerLocationUpdateHandler = new CustomerLocationUpdater();

		GeoMessenger.customerLocationUpdateHandler
				.start(CallbackFragmentActivity.this);

	}

	private void initAnimations() {
		animateBottomViewOut = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.slide_down);
		animateBottomViewIn = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.slide_up);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.callback_activity);
		createUIElements();

		GeoMessenger.isGooglePlayServicesAvailable = servicesConnected();

		if (GeoMessenger.isGooglePlayServicesAvailable) { // activity specific
															// map needs
			setUpMapIfNeeded();

			mapFragment.getUiSettings().setCompassEnabled(true);
			mapFragment.setMyLocationEnabled(true);
			mapFragment.getUiSettings().setMyLocationButtonEnabled(false);
			mapFragment.getUiSettings().setAllGesturesEnabled(true);

			initAnimations();
		}

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

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerReceiver(receiver, new IntentFilter(
				PollGeoMessagesService.NOTIFICATION));

		// update messages on map
		handler.post(new Runnable() {

			@Override
			public void run() {

				centerMap();
				handler.postDelayed(this, 4 * Constants.MILLIS_IN_A_SECOND);
			}
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// GeoMessenger.customerLocationUpdateHandler.stop();
		unregisterReceiver(receiver);
	}

	protected void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mapFragment == null) {
			// Try to obtain the map from the SupportMapFragment.
			mapFragment = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
		}
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
//			final OAuthSyncManager mOAuth = new OAuthSyncManager(
//					getApplicationContext());
//
//			return mOAuth.getUserProfileThreeStep(args[0]);
			
			return "boo";
					
		}

		@Override
		protected void onPostExecute(String result) {
			final String text = (result != null) ? result.toString()
					: getString(R.string.message_result_is_null);

			if (!getString(R.string.message_result_is_null).equals(text)) {
				String[] details = text.split("\n");
				GeoMessenger.userId = details[0];
				GeoMessenger.userName = details[1];

				startService();
			}

			// setupTable();

			mResult.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.GONE);

			super.onPostExecute(result);
		}
	}

	private void startService() {
		Intent intent = new Intent(this, PollGeoMessagesService.class);
		startService(intent);
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

//			AuthDataPreferences.getInstance(mContext).setAccessToken("");
//			AuthDataPreferences.getInstance(mContext).setRefreshToken("");
//			AuthDataPreferences.getInstance(mContext).setExpiresIn("");
//
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

	public void showFields() {
		fieldsVisible = true;
		bottomView.startAnimation(animateBottomViewOut);

		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						t1.setVisibility(View.VISIBLE);
						t2.setVisibility(View.VISIBLE);
						emailText.setVisibility(View.VISIBLE);
						msgText.setVisibility(View.VISIBLE);

						bottomView.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 400);
	}

	public void hideFields() {
		fieldsVisible = false;
		bottomView.startAnimation(animateBottomViewOut);

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						t1.setVisibility(View.GONE);
						t2.setVisibility(View.GONE);
						emailText.setVisibility(View.GONE);
						msgText.setVisibility(View.GONE);

						bottomView.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 400);
	}

	private void sendMessage() {

		String email = emailText.getText().toString();
		String msg = msgText.getText().toString();

		JSONObject jsonObjectRequest = new JSONObject();
		JSONObject request = new JSONObject();
		try {
			request.put("lat", GeoMessenger.customerLocation.getLatitude());
			request.put("lng", GeoMessenger.customerLocation.getLongitude());
			request.put("geoMsg", msg);
			request.put("fromEmail", GeoMessenger.userId);
			request.put("fromName", GeoMessenger.userName);
			request.put("toEmail", email);

			jsonObjectRequest.put("action", "put-point");
			jsonObjectRequest.put("request", request);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonObjectRequest jsonGeoMessagesRequest = new JsonObjectRequest(
				Method.POST, UrlConstants.getBaseUrl(), jsonObjectRequest,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						hideFields();
						showAlertDialog();
						emailText.setText("");
						msgText.setText("");
						saveButton.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								showFields();
							}
						});
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
					}

				});

		GeoMessenger.queue.add(jsonGeoMessagesRequest);
	}

	private void showAlertDialog() {
		new AlertDialog.Builder(CallbackFragmentActivity.this)
				.setTitle("Message saved")
				.setMessage("Your message for has been saved")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	protected void centerMap() {
		if (GeoMessenger.customerLocation != null) {
			LatLng p = new LatLng(GeoMessenger.customerLocation.getLatitude(),
					GeoMessenger.customerLocation.getLongitude());
			mapFragment.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 16));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.action_main, menu);
		action_signout = (MenuItem) menu.findItem(R.id.action_signout);
		menu_legalnotices = (MenuItem) menu.findItem(R.id.menu_legalnotices);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_signout:
			LogoutAsync async = new LogoutAsync();
			async.execute();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (fieldsVisible) {
			saveButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					showFields();
				}
			});
			hideFields();
		} else
			super.onBackPressed();
	}
}
