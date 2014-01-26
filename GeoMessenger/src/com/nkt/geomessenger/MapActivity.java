package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nkt.geomessenger.constants.Constants;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.map.CustomerLocationUpdater;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.model.GsonConvertibleObject;
import com.nkt.geomessenger.model.QueryGeoMessagesResult;
import com.nkt.geomessenger.service.PollGeoMessagesService;
import com.nkt.geomessenger.utils.Utils;
import com.nkt.views.FlowLayout;

public class MapActivity extends GMActivity {

	private LinearLayout bottomViewLayout, sendOptionsLayout;
	private FlowLayout friendsPickerLayout;
	private Handler handler;
	private EditText messageText;
	private Button saveButton;
	private View mResult, mProgress;
	private CheckBox selfCheckBox;

	private boolean fieldsVisible, isCentered;
	private Hashtable<String, GeoMessage> markers = new Hashtable<String, GeoMessage>();

	private static final int GET_SELECTED_FRIENDS = 1;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int resultCode = bundle.getInt(PollGeoMessagesService.RESULT);
				if (isCentered) {
					if (resultCode == RESULT_OK) {
						mapFragment.clear();

						if (GeoMessenger.geoMessages != null)
							drawMarkers();
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

		mProgress = findViewById(R.id.pg_loading);
		mResult = findViewById(R.id.ly_result);

		mProgress.setVisibility(View.VISIBLE);
		mResult.setVisibility(View.GONE);

		bottomViewLayout = (LinearLayout) findViewById(R.id.bottom_view);
		friendsPickerLayout = (FlowLayout) findViewById(R.id.friends_picker);
		sendOptionsLayout = (LinearLayout) findViewById(R.id.send_options);
		selfCheckBox = (CheckBox) findViewById(R.id.check_self);

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

		GeoMessenger.customerLocationUpdateHandler.start(MapActivity.this);

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
		setContentView(R.layout.main_activity);

		makeFBRequests(Session.getActiveSession());

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
			mapFragment
					.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						@Override
						public void onInfoWindowClick(Marker marker) {
							GeoMessenger.selectedGeoMessage = markers
									.get(marker.getId());
							GeoMessenger.selectedGeoMessage.setSeen(true);
							Intent intent = new Intent(MapActivity.this,
									MessageDetailsActivity.class);
							startActivity(intent);
						}
					});

			initAnimations();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(
				PollGeoMessagesService.NOTIFICATION));

		mapFragment.clear();

		if (GeoMessenger.geoMessages != null)
			drawMarkers();

		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!isCentered) {
					centerMap();
					handler.postDelayed(this, Constants.MILLIS_IN_A_SECOND / 2);
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
		mapFragment.clear();
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
		Log.d("PollGMService", "" + startService(intent));
		GeoMessenger.isPollServiceStarted = true;
	}

	private void stopService() {
		Intent intent = new Intent(this, PollGeoMessagesService.class);
		Log.d("PollGMService", "" + stopService(intent));
		GeoMessenger.isPollServiceStarted = false;
	}

	private void drawMarkers() {
		for (final GeoMessage gm : GeoMessenger.geoMessages.getResult()) {

			LatLng p = new LatLng(gm.getLoc()[0], gm.getLoc()[1]);

			final Marker m = mapFragment
					.addMarker(new MarkerOptions()
							.position(p)
							.title(gm.getFromUserName())
							.icon(BitmapDescriptorFactory.defaultMarker(gm
									.isSeen() ? BitmapDescriptorFactory.HUE_BLUE
									: BitmapDescriptorFactory.HUE_RED))
							.snippet(
									Utils.getHumanReadableTime(gm
											.getTimestamp())));

			if (!gm.isSeen())
				animateMarker(m);

			markers.put(m.getId(), gm);
		}
	}

	private void animateMarker(final Marker marker) {
		// Make the marker bounce
		final Handler handler = new Handler();

		final long startTime = SystemClock.uptimeMillis();
		final long duration = 2000;

		Projection proj = mapFragment.getProjection();
		final LatLng markerLatLng = marker.getPosition();
		Point startPoint = proj.toScreenLocation(markerLatLng);
		startPoint.offset(0, -100);
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);

		final Interpolator interpolator = new BounceInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - startTime;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * markerLatLng.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * markerLatLng.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				}
			}
		});
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
			intent.setClass(MapActivity.this, PickerActivity.class);
			startActivityForResult(intent, GET_SELECTED_FRIENDS);
		}
	}

	private void sendMessage() {
		mProgress.setVisibility(View.VISIBLE);

		String msg = messageText.getText().toString();

		JSONObject jsonObjectRequest = new JSONObject();

		JSONArray geoMessages = new JSONArray();

		double loc[] = { GeoMessenger.customerLocation.getLatitude(),
				GeoMessenger.customerLocation.getLongitude() };
		try {
			for (GraphUser g : GeoMessenger.getSelectedUsers()) {
				JSONObject gm = new JSONObject();
				gm.put("loc", new JSONArray(Arrays.toString(loc)));
				gm.put("message", msg);
				gm.put("fromUserId", GeoMessenger.userId);
				gm.put("fromUserName", GeoMessenger.userName);
				gm.put("toUserId", g.getId());
				gm.put("toUserName", g.getName());

				geoMessages.put(gm);
			}

			if (selfCheckBox.isChecked()) {
				JSONObject gm = new JSONObject();
				gm.put("loc", new JSONArray(Arrays.toString(loc)));
				gm.put("message", msg);
				gm.put("fromUserId", GeoMessenger.userId);
				gm.put("fromUserName", "MySelf");
				gm.put("toUserId", GeoMessenger.userId);
				gm.put("toUserName", "MySelf");

				geoMessages.put(gm);

			}

			jsonObjectRequest.put("geo_messages", geoMessages);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonObjectRequest jsonGeoMessagesRequest = new JsonObjectRequest(
				Method.POST, UrlConstants.getCreateGMUrl(), jsonObjectRequest,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						hideFields();
						showAlertDialog();
						messageText.setText("");
						mProgress.setVisibility(View.GONE);
						fetchUpdatedMessages();
						
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
						mProgress.setVisibility(View.GONE);
					}

				});

		GeoMessenger.queue.add(jsonGeoMessagesRequest);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_SELECTED_FRIENDS) {
			if (resultCode == RESULT_OK) {
				for (GraphUser g : GeoMessenger.getSelectedUsers()) {
					TextView tv = new TextView(MapActivity.this);
					tv.setText(g.getName());
					tv.setTextColor(getResources().getColor(R.color.white));
					tv.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.blue_round_corners));
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

	private void makeFBRequests(final Session session) {

		final Request meRequest = Request.newMeRequest(session,
				new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user,
							com.facebook.Response response) {

						if (session == Session.getActiveSession()) {
							if (user != null) {
								GeoMessenger.userId = user.getId();
								GeoMessenger.userName = user.getName();

								mProgress.setVisibility(View.GONE);
								mResult.setVisibility(View.VISIBLE);

								fetchUpdatedMessages();
							}
						}
						if (response.getError() != null) {
							handleError(response.getError());
						}

					}
				});

		meRequest.executeAsync();

	}

	private void showAlertDialog() {
		new AlertDialog.Builder(MapActivity.this).setTitle("Message saved")
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

	private void fetchUpdatedMessages() {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("loc[]", Double
				.toString(GeoMessenger.customerLocation.getLatitude())));
		list.add(new BasicNameValuePair("loc[]", Double
				.toString(GeoMessenger.customerLocation.getLongitude())));
		list.add(new BasicNameValuePair("user_id", GeoMessenger.userId));
		list.add(new BasicNameValuePair("radius_in_metres", "1000"));

		JsonObjectRequest jsonGeoMessagesRequest = new JsonObjectRequest(
				Method.GET, Utils.getFilledUrl(
						UrlConstants.getNearGeoMsgsUrl(), list), null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						String jsonrep = response.toString();
						Log.d("MapActivity", jsonrep);

						GeoMessenger.geoMessages = GsonConvertibleObject
								.getObjectFromJson(jsonrep,
										QueryGeoMessagesResult.class);

						drawMarkers();
					}
				}, null);
		GeoMessenger.queue.add(jsonGeoMessagesRequest);
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