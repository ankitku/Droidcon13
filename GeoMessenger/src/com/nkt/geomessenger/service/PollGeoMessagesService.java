package com.nkt.geomessenger.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkt.geomessenger.GeoMessenger;
import com.nkt.geomessenger.MapActivity;
import com.nkt.geomessenger.R;
import com.nkt.geomessenger.constants.Constants;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.model.GsonConvertibleObject;
import com.nkt.geomessenger.model.QueryGeoMessagesResult;
import com.nkt.geomessenger.utils.Utils;

public class PollGeoMessagesService extends IntentService {

	private int result;
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "com.nkt.geomessenger.service";
	public static final String PREFS_NAME = "PreferencesFile";

	private static Handler handler = new Handler();

	public PollGeoMessagesService() {
		super("PollGeoMessagesService");
	}
	
	private Runnable geoMessagesFetcher =  new Runnable() {

		@Override
		public void run() {
			result = Activity.RESULT_CANCELED;

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
							result = Activity.RESULT_OK;
							String jsonrep = response.toString();
							Log.d("PollGMService", jsonrep);

							GeoMessenger.geoMessages = GsonConvertibleObject
									.getObjectFromJson(jsonrep,
											QueryGeoMessagesResult.class);
							publishResults(result);

							if (isNewMessage()) {
								if (!GeoMessenger.isRunning)
									notificationAlert();

								addNewNotifiedMessageIds();
							}

						}

					}, new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							publishResults(result);
						}
					});

			GeoMessenger.queue.add(jsonGeoMessagesRequest);
			handler.postDelayed(this, 30 * Constants.MILLIS_IN_A_SECOND);
		}
	};

	// will be called asynchronously by Android
	@Override
	protected void onHandleIntent(Intent intent) {
		handler.post(geoMessagesFetcher);
	}

	private void addNewNotifiedMessageIds() {
		SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, 0);

		Set<String> set = null;
		set = sharedPrefs.getStringSet("NotifiedGeoMessageIds",
				new HashSet<String>());

		for (GeoMessage gm : GeoMessenger.geoMessages.getResult())
			if (!set.contains(gm.getId())) {
				set.add(gm.getId());
				GeoMessenger.msgsForSW.add(gm);
			}

		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putStringSet("NotifiedGeoMessageIds", set);
		editor.commit();
	}

	private boolean isNewMessage() {
		SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
		boolean flag = false;

		Set<String> set = null;
		set = sharedPrefs.getStringSet("NotifiedGeoMessageIds",
				new HashSet<String>());

		for (GeoMessage gm : GeoMessenger.geoMessages.getResult())
			if (!set.contains(gm.getId())) {
				flag = true;
				break;
			}
		return flag;
	}

	private void notificationAlert() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				PollGeoMessagesService.this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("New GeoMessages");
		int NOTIFICATION_ID = 12345;

		Intent targetIntent = new Intent(PollGeoMessagesService.this,
				MapActivity.class);
		targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(
				PollGeoMessagesService.this, 0, targetIntent, 0);

		builder.setContentIntent(contentIntent);
		builder.setAutoCancel(true);
		builder.setLights(Color.BLUE, 500, 500);

		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		builder.setSound(alarmSound);

		long[] pattern = { 500, 500, 500, 500, 500, 500, 500 };
		builder.setVibrate(pattern);

		NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nManager.notify(NOTIFICATION_ID, builder.build());
	}

	private void publishResults(int result) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}
}