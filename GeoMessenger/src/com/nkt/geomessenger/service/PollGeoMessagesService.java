package com.nkt.geomessenger.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkt.geomessenger.GeoMessenger;
import com.nkt.geomessenger.constants.Constants;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.model.GsonConvertibleObject;
import com.nkt.geomessenger.model.QueryGeoMessagesResult;

public class PollGeoMessagesService extends IntentService {

	private int result = Activity.RESULT_CANCELED;
	public static final String URL = "urlpath";
	public static final String FILENAME = "filename";
	public static final String FILEPATH = "filepath";
	public static final String RESULT = "result";
	public static final String NOTIFICATION = "com.nkt.geomessenger.service";

	private static Handler handler = new Handler();

	public PollGeoMessagesService() {
		super("DownloadService");
	}

	// will be called asynchronously by Android
	@Override
	protected void onHandleIntent(Intent intent) {

		handler.post(new Runnable() {

			@Override
			public void run() {
				result = Activity.RESULT_CANCELED;

				JSONObject jsonObjectRequest = new JSONObject();
				JSONObject request = new JSONObject();
				try {
					if (GeoMessenger.customerLocation != null)
					{
						request.put("lat",
								GeoMessenger.customerLocation.getLatitude());
						request.put("lng",
								GeoMessenger.customerLocation.getLongitude());
					}
					request.put("radiusInMeter", 10000);
					request.put("userEmail", GeoMessenger.userId);

					jsonObjectRequest.put("action", "query-radius-user");
					jsonObjectRequest.put("request", request);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JsonObjectRequest jsonGeoMessagesRequest = new JsonObjectRequest(
						Method.POST, UrlConstants.getBaseUrl(),
						jsonObjectRequest, new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								result = Activity.RESULT_OK;
								// String jsonrep = response.toString();
								// GeoMessenger.geoMessages =
								// GsonConvertibleObject.getObjectFromJson(jsonrep,
								// QueryGeoMessagesResult.class);

								// dummy code
								String jsonrep = "{\"status\":\"SUCCESS\",\"request_type\":\"GET_GM\",\"geo_messages\":[{\"id\":\"52aa3a57666972115d020000\",\"timestamp\":1386887767,\"loc\":[12.937091, 77.613657],\"message\":\"Love the idli here at Shanti Sagar\",\"from_user_name\":\"Ankit\",\"from_user_pic\":\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-ash2/1118435_574458322_1969312793_q.jpg\"},{\"id\":\"52aa3a57666972115d000000\",\"timestamp\":1386887767,\"loc\":[12.937091, 77.613658],\"message\":\"Love the dosa here at Shanti Sagar\",\"from_user_name\":\"Ankit\",\"from_user_pic\":\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-ash2/1118435_574458322_1969312793_q.jpg\"}]}";

								GeoMessenger.geoMessages = GsonConvertibleObject
										.getObjectFromJson(jsonrep,
												QueryGeoMessagesResult.class);

								publishResults(result);
							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								
								String jsonrep = "{\"status\":\"SUCCESS\",\"request_type\":\"GET_GM\",\"geo_messages\":[{\"id\":\"52aa3a57666972115d020000\",\"timestamp\":1386887767,\"loc\":[12.937091, 77.613657],\"message\":\"Love the idli here at Shanti Sagar\",\"from_user_name\":\"Ankit\",\"from_user_pic\":\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-ash2/1118435_574458322_1969312793_q.jpg\"},{\"id\":\"52aa3a57666972115d000000\",\"timestamp\":1386887767,\"loc\":[12.937091, 77.613758],\"message\":\"Love the dosa here at Shanti Sagar\",\"from_user_name\":\"Ankit\",\"from_user_pic\":\"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-ash2/1118435_574458322_1969312793_q.jpg\"}]}";

								GeoMessenger.geoMessages = GsonConvertibleObject
										.getObjectFromJson(jsonrep,
												QueryGeoMessagesResult.class);
								
								publishResults(result);
							}
						});

				GeoMessenger.queue.add(jsonGeoMessagesRequest);

				handler.postDelayed(this, 30 * Constants.MILLIS_IN_A_SECOND);
			}
		});

	}

	private void publishResults(int result) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}
}