package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.utils.Utils;

public class MessageDetailsActivity extends GMActivity {

	private TextView fromText, toText, messageText, timeText;
	private boolean isSent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle("Your message");
		setContentView(R.layout.message_details);

		toText = (TextView) findViewById(R.id.toText);
		fromText = (TextView) findViewById(R.id.fromText);
		messageText = (TextView) findViewById(R.id.messageText);
		timeText = (TextView) findViewById(R.id.timeText);

		// if message is FOR me, then send ack
		if (Utils.isEmpty(GeoMessenger.selectedGeoMessage.getToUserName()))
			sendAck();
		else
			isSent = true;

		populateViews();
	}

	private void sendAck() {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", GeoMessenger.selectedGeoMessage
				.getId()));

		JsonObjectRequest jsonSentMessagesRequest = new JsonObjectRequest(
				Method.GET, Utils.getFilledUrl(UrlConstants.getGMAckedUrl(),
						list), null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						String jsonrep = response.toString();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});

		GeoMessenger.queue.add(jsonSentMessagesRequest);
	}
	
	public void deleteGeoMessage(View v) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("id", GeoMessenger.selectedGeoMessage
				.getId()));

		JsonObjectRequest jsonSentMessagesRequest = new JsonObjectRequest(
				Method.GET, Utils.getFilledUrl(UrlConstants.getGMDelUrl(),
						list), null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						String jsonrep = response.toString();
						
						List<GeoMessage> msgs = GeoMessenger.geoMessages.getResult();
						msgs.remove(msgs.indexOf(GeoMessenger.selectedGeoMessage));
						finish();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});

		GeoMessenger.queue.add(jsonSentMessagesRequest);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void populateViews() {
		GeoMessage entry = GeoMessenger.selectedGeoMessage;

		if (isSent)
			toText.setText("To: " + entry.getToUserName());
		else
			fromText.setText("From: " + entry.getFromUserName());

		messageText.setText(entry.getMessage());
		timeText.setText(Utils.getHumanReadableTime(entry.getTimestamp()));
	}
}
