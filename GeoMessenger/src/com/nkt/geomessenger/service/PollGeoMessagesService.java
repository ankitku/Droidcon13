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
import com.nkt.geomessenger.model.Result;

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
					request.put("lat", GeoMessenger.customerLocation.getLatitude());
					request.put("lng", GeoMessenger.customerLocation.getLongitude());
					request.put("radiusInMeter", 1000);
					request.put("userEmail", GeoMessenger.userEmail);

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
								String jsonrep = response.toString();
								GeoMessenger.geoMessages = GsonConvertibleObject.getObjectFromJson(jsonrep, Result.class);
								publishResults(result);
							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								publishResults(result);
							}

						});
				
				GeoMessenger.queue.add(jsonGeoMessagesRequest);

				handler.postDelayed(this, 5 * Constants.MILLIS_IN_A_SECOND);
			}
		});

	}

	private void publishResults(int result) {
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra(RESULT, result);
		sendBroadcast(intent);
	}
}