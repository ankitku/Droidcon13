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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
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
import com.facebook.model.GraphUser;
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
import com.nkt.views.FlowLayout;

public class CallbackFragmentActivity extends GMActivity {

	private LinearLayout bottomViewLayout, sendOptionsLayout;
	private FlowLayout friendsPickerLayout;
	private Handler handler;
	private EditText messageText;
	private Button saveButton;
	private boolean fieldsVisible, isCentered;
	private Hashtable<String, GeoMessage> markers = new Hashtable<String, GeoMessage>();

	private static final int GET_SELECTED_FRIENDS = 1;

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
					if (resultCode == RESULT_OK) {
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
		bottomViewLayout = (LinearLayout) findViewById(R.id.bottom_view);
		friendsPickerLayout = (FlowLayout) findViewById(R.id.friends_picker);
		sendOptionsLayout = (LinearLayout) findViewById(R.id.send_options);

		messageText = (EditText) findViewById(R.id.msg_text);
		saveButton = (Button) findViewById(R.id.btn_save);

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
		getSupportActionBar().setTitle("Nearby Messages");
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
		bottomViewLayout.startAnimation(animateBottomViewOut);

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
						sendOptionsLayout.setVisibility(View.VISIBLE);
						bottomViewLayout.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 400);
	}

	public void hideFields() {
		fieldsVisible = false;
		bottomViewLayout.startAnimation(animateBottomViewOut);

		GeoMessenger.getSelectedUsers().clear();
		friendsPickerLayout.removeAllViews();
		messageText.setText("");

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						sendOptionsLayout.setVisibility(View.GONE);
						bottomViewLayout.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 400);
	}

	public void showFriendsPicker(View v) {
		if (Session.getActiveSession() != null
				&& Session.getActiveSession().isOpened()) {

			GeoMessenger.getSelectedUsers().clear();
			friendsPickerLayout.removeAllViews();

			Intent intent = new Intent();
			intent.setClass(CallbackFragmentActivity.this, PickerActivity.class);
			startActivityForResult(intent, GET_SELECTED_FRIENDS);
		}
	}

	private void sendMessage() {

		String email = "lol";
		String msg = messageText.getText().toString();

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
						messageText.setText("");
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_SELECTED_FRIENDS) {
			if (resultCode == RESULT_OK) {
				for (GraphUser g : GeoMessenger.getSelectedUsers()) {
					TextView tv = new TextView(CallbackFragmentActivity.this);
					tv.setText(g.getName());
					tv.setTextColor(getResources().getColor(R.color.white));
					tv.setBackgroundColor(getResources().getColor(R.color.blue));
					tv.setPadding(5, 2, 5, 5);

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(5, 0, 5, 5);
					tv.setLayoutParams(layoutParams);

					friendsPickerLayout.addView(tv);
				}

				if (GeoMessenger.getSelectedUsers().size() > 0) {
					if (messageText.requestFocus()) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
								InputMethodManager.HIDE_IMPLICIT_ONLY);
					}
				}
			}
		}
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