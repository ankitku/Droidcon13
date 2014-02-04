package com.nkt.geomessenger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
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

public class MapActivity extends GMActivity implements OnItemSelectedListener {

	class UploadPicAsyncTask extends AsyncTask<String, Integer, Double> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressUpload.setVisibility(View.VISIBLE);
		}

		@Override
		protected Double doInBackground(String... params) {

			AmazonS3Client s3Client = new AmazonS3Client(
					new BasicAWSCredentials(Constants.A_A_K, Constants.A_S_K));

			PutObjectRequest por = null;
			try {
				File compressedImageFile = Utils.convertBitmapToFile(
						MapActivity.this, selectedBitmap, selectedImageName);

				por = new PutObjectRequest(GeoMessenger.getPictureBucket(),
						selectedImageName, compressedImageFile);
			} catch (IOException ioe) {

			}

			PutObjectResult pors = s3Client.putObject(por);
			if (pors.getContentMd5() != null)
				return 1d;
			else
				return 0d;
		}

		@Override
		protected void onPostExecute(Double result) {
			super.onPostExecute(result);
			if (result == 1d) {
				mProgressUpload.setVisibility(View.GONE);
				sendMessage();
			} else {
				// error in image upload
			}
		}

	}

	private LinearLayout bottomViewLayout, sendOptionsLayout,
			uploadImageLayout;
	private FlowLayout friendsPickerLayout;
	private Handler handler;
	private EditText messageText;
	private Button saveButton;
	private View mResult, mProgress, mProgressUpload;
	private CheckBox selfCheckBox;
	private String selectedImageName;
	private Bitmap selectedBitmap;
	private ImageView uploadPic;
	private Spinner spinner;
	private RatingBar ratingBar;

	private boolean fieldsVisible, isCentered, isMsgPublic;
	private Hashtable<String, GeoMessage> markers = new Hashtable<String, GeoMessage>();

	private static final int GET_SELECTED_FRIENDS = 1;
	private static final int PHOTO_SELECTED = 2;
	private static final int CAM_PIC = 3;

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

	protected MenuItem action_signout;

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
		mProgressUpload = findViewById(R.id.pg_uploading);
		mResult = findViewById(R.id.ly_result);

		mProgress.setVisibility(View.VISIBLE);
		mResult.setVisibility(View.GONE);

		bottomViewLayout = (LinearLayout) findViewById(R.id.bottom_view);
		friendsPickerLayout = (FlowLayout) findViewById(R.id.friends_picker);
		sendOptionsLayout = (LinearLayout) findViewById(R.id.send_options);
		selfCheckBox = (CheckBox) findViewById(R.id.check_self);

		messageText = (EditText) findViewById(R.id.msg_text);
		saveButton = (Button) findViewById(R.id.btn_save);

		uploadImageLayout = (LinearLayout) findViewById(R.id.upload_image_layout);

		uploadPic = (ImageView) findViewById(R.id.upload_pic);
		ratingBar = (RatingBar) findViewById(R.id.rating_bar);

		spinner = (Spinner) findViewById(R.id.privacy_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.spinner_options_array,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

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

		isMsgPublic = true;
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

			loadImage(gm.getFromUserPic(), null);

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

		if (GeoMessenger.isFirstTime)
			GeoMessenger.isFirstTime = false;
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
				if (Utils.isEmpty(selectedImageName))
					sendMessage();
				else
					(new UploadPicAsyncTask()).execute();
			}
		});

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						sendOptionsLayout.setVisibility(View.VISIBLE);
						uploadImageLayout.setVisibility(View.GONE);
						mProgressUpload.setVisibility(View.GONE);
						bottomViewLayout.startAnimation(animateBottomViewIn);
					}
				});
			}
		}, 200);
	}

	public void hideFields() {
		fieldsVisible = false;
		bottomViewLayout.startAnimation(animateBottomViewOut);

		GeoMessenger.getSelectedUsers().clear();
		friendsPickerLayout.removeAllViews();
		messageText.setText("");
		removePic(messageText);

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
		}, 200);
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

	public void removePic(View v) {
		uploadImageLayout.setVisibility(View.GONE);
		uploadPic.setImageBitmap(null);
		selectedImageName = null;
		selectedBitmap = null;
	}

	public void sendPic() {
		(new UploadPicAsyncTask()).execute();
	}

	private void sendMessage() {
		mProgress.setVisibility(View.VISIBLE);

		String msg = messageText.getText().toString();
		if(Utils.isEmpty(msg))
		{
			new AlertDialog.Builder(MapActivity.this).setTitle("Empty message")
			.setMessage("Can't save empty message!")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).show();
			return;
		}

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

				if (ratingBar.isShown())
					gm.put("rating", ratingBar.getRating());
				if (!Utils.isEmpty(selectedImageName))
					gm.put("picName", selectedImageName);

				geoMessages.put(gm);
			}

			JSONObject gm = new JSONObject();
			gm.put("loc", new JSONArray(Arrays.toString(loc)));
			gm.put("message", msg);
			gm.put("fromUserId", GeoMessenger.userId);
			gm.put("fromUserName", "MySelf");

			if (ratingBar.isShown())
				gm.put("rating", ratingBar.getRating());
			if (!Utils.isEmpty(selectedImageName))
				gm.put("picName", selectedImageName);

			if (!isMsgPublic) {
				if (selfCheckBox.isChecked()) {
					gm.put("toUserId", GeoMessenger.userId);
					gm.put("toUserName", "MySelf");
					geoMessages.put(gm);
				}
			} else {
				gm.put("toUserId", "public");
				gm.put("toUserName", "public");
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

	public void picSource(View v) {
		AlertDialog.Builder sourceSelection = new AlertDialog.Builder(this);

		final CharSequence[] items = { "Take Photo", "Attach Photo" };

		sourceSelection.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 1)
							albumPic();
						else
							camPic();
						dialog.dismiss();
					}
				});

		AlertDialog alert = sourceSelection.create();
		alert.show();

	}

	private void camPic() {
		final Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAM_PIC);
	}

	private void albumPic() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, PHOTO_SELECTED);
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

		if (requestCode == PHOTO_SELECTED) {
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();

				String selectedImagePath = Utils.getRealPathFromURI(
						MapActivity.this, selectedImage);

				selectedImageName = GeoMessenger.userId + "_"
						+ System.currentTimeMillis();
				selectedBitmap = Utils
						.decodeSampledBitmapFromFile(selectedImagePath);

				uploadPic.setImageBitmap(selectedBitmap);
				uploadImageLayout.setVisibility(View.VISIBLE);
			}
		}

		if (requestCode == CAM_PIC) {
			if (resultCode == RESULT_OK) {

				selectedImageName = GeoMessenger.userId + "_"
						+ System.currentTimeMillis();
				selectedBitmap = (Bitmap) data.getExtras().get("data");

				uploadPic.setImageBitmap(selectedBitmap);
				uploadImageLayout.setVisibility(View.VISIBLE);

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
		if (GeoMessenger.customerLocation == null)
			return;

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

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		if (pos == 2) {
			isMsgPublic = false;
			showFriendsPicker(view);
			friendsPickerLayout.setVisibility(View.VISIBLE);
			selfCheckBox.setVisibility(View.VISIBLE);
			selfCheckBox.setChecked(false);
			ratingBar.setVisibility(View.GONE);
		} else if (pos == 3) {
			isMsgPublic = false;
			selfCheckBox.setVisibility(View.VISIBLE);
			GeoMessenger.getSelectedUsers().clear();
			friendsPickerLayout.setVisibility(View.GONE);
			selfCheckBox.setVisibility(View.GONE);
			selfCheckBox.setChecked(true);
			ratingBar.setVisibility(View.GONE);
		} else if (pos == 0) {
			isMsgPublic = true;
			GeoMessenger.getSelectedUsers().clear();
			friendsPickerLayout.setVisibility(View.GONE);
			selfCheckBox.setVisibility(View.GONE);
			selfCheckBox.setChecked(false);
			ratingBar.setVisibility(View.GONE);
		} else {
			isMsgPublic = true;
			GeoMessenger.getSelectedUsers().clear();
			friendsPickerLayout.setVisibility(View.GONE);
			selfCheckBox.setVisibility(View.GONE);
			selfCheckBox.setChecked(false);
			ratingBar.setVisibility(View.VISIBLE);
			ratingBar.setRating(0f);
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}
}