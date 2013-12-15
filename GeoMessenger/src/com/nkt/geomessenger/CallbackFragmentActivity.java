package com.nkt.geomessenger;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap.CompressFormat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nkt.geomessenger.constants.Constants;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.map.CustomerLocationUpdater;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.service.PollGeoMessagesService;
import com.nkt.geomessenger.utils.ImageCacheManager;

/**
 * Callback Activity that is started from the web view activity or manually. It
 * handles the user profile once the access code is given.
 * 
 * @author durantea
 * 
 */
public class CallbackFragmentActivity extends GMActivity {

	private LinearLayout bottomView;
	private Handler handler;
	private EditText msgText;
	private Button saveButton, friendsPicker;
	private TextView t1, t2;
	private boolean fieldsVisible, isCentered;
	private Hashtable<String, GeoMessage> markers = new Hashtable<String, GeoMessage>();

	private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 10;
	private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	private static int DISK_IMAGECACHE_QUALITY = 100; // PNG is lossless so
														// quality is ignored
														// but must be provided

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int resultCode = bundle.getInt(PollGeoMessagesService.RESULT);
				if (isCentered) {
					if (resultCode == 0) {
						mapFragment.clear();
						if (GeoMessenger.geoMessages != null) {
							for (final GeoMessage gm : GeoMessenger.geoMessages
									.getResult()) {

								LatLng p = new LatLng(gm.getLoc()[0],
										gm.getLoc()[1]);

								final Marker m = mapFragment
										.addMarker(new MarkerOptions()
												.position(p)
												.title(gm.getFromUserName())
												.snippet(gm.getTimestamp() + ""));

								markers.put(m.getId(), gm);
							}
						}
					}
				} else
					centerMap();
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

	public static class ErrorDialogFragment extends DialogFragment {
		protected Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	protected boolean servicesConnected() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			int errorCode = resultCode;
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
			return false;
		}
	}

	private class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private View view;

		public CustomInfoWindowAdapter() {
			view = getLayoutInflater().inflate(R.layout.custom_info_window,
					null);
		}

		@Override
		public View getInfoContents(Marker marker) {

			if (marker != null && marker.isInfoWindowShown()) {
				marker.hideInfoWindow();
				marker.showInfoWindow();
			}
			return null;
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			String url = null;

			if (marker.getId() != null && markers != null && markers.size() > 0) {
				if (markers.get(marker.getId()) != null
						&& markers.get(marker.getId()) != null) {
					url = markers.get(marker.getId()).getFromUserPic();
				}
			}
			final ImageView icon = ((ImageView) view.findViewById(R.id.badge));

			if (url != null && !url.equalsIgnoreCase("null")
					&& !url.equalsIgnoreCase("")) {
				loadImage(url, icon);
			}

			final String title = marker.getTitle();
			final TextView titleUi = ((TextView) view.findViewById(R.id.title));
			if (title != null) {
				titleUi.setText(title);
			} else {
				titleUi.setText("");
			}

			final String snippet = marker.getSnippet();
			final TextView snippetUi = ((TextView) view
					.findViewById(R.id.snippet));
			if (snippet != null) {
				snippetUi.setText(snippet);
			} else {
				snippetUi.setText("");
			}

			return view;
		}
	}

	private void createUIElements() {
		bottomView = (LinearLayout) findViewById(R.id.bottom_view);

		friendsPicker = (Button) findViewById(R.id.friends_picker);
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
		createImageCache();

		GeoMessenger.isGooglePlayServicesAvailable = servicesConnected();

		if (GeoMessenger.isGooglePlayServicesAvailable) { // activity specific
															// map needs
			setUpMapIfNeeded();

			mapFragment.getUiSettings().setCompassEnabled(true);
			mapFragment.setMyLocationEnabled(true);
			mapFragment.setIndoorEnabled(true);
			mapFragment.getUiSettings().setMyLocationButtonEnabled(true);
			mapFragment.getUiSettings().setAllGesturesEnabled(true);

			mapFragment.setInfoWindowAdapter(new CustomInfoWindowAdapter());
			initAnimations();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(
				PollGeoMessagesService.NOTIFICATION));

		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!isCentered) {
					centerMap();
					handler.postDelayed(this, 2 * Constants.MILLIS_IN_A_SECOND);
				} else {
					handler.removeCallbacks(this);
					if (!GeoMessenger.isPollServiceStarted)
						startService();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// GeoMessenger.customerLocationUpdateHandler.stop();
		unregisterReceiver(receiver);
	}

	private void createImageCache() {
		ImageCacheManager.getInstance().init(this, this.getPackageCodePath(),
				DISK_IMAGECACHE_SIZE, DISK_IMAGECACHE_COMPRESS_FORMAT,
				DISK_IMAGECACHE_QUALITY);
	}

	private void loadImage(String imageUrl, final ImageView iv) {
		ImageCacheManager.getInstance().getImage(imageUrl, new ImageListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				iv.setImageResource(R.drawable.placeholder_contact);
			}

			@Override
			public void onResponse(ImageContainer response, boolean isImmediate) {
				iv.setImageBitmap(response.getBitmap());
			}
		});
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

	private void startService() {
		Intent intent = new Intent(this, PollGeoMessagesService.class);
		startService(intent);
		GeoMessenger.isPollServiceStarted = true;
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
						friendsPicker.setVisibility(View.VISIBLE);
						msgText.setVisibility(View.VISIBLE);

						bottomView.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 400);
	}

	class LogoutAsync extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Session.getActiveSession().closeAndClearTokenInformation();
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
						friendsPicker.setVisibility(View.GONE);
						msgText.setVisibility(View.GONE);

						bottomView.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 400);
	}

	public void showFriendsPicker(View v) {
		if (Session.getActiveSession() != null
				&& Session.getActiveSession().isOpened()) {

			Intent intent = new Intent();
			intent.setClass(CallbackFragmentActivity.this, PickerActivity.class);
			startActivityForResult(intent, 1);
		}
	}

	private void sendMessage() {

		String email = "lol";
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
			mapFragment.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 16.0f));
			isCentered = true;
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