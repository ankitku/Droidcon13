package com.nkt.geomessenger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkt.geomessenger.constants.Constants;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.utils.Utils;

public class MessageDetailsActivity extends GMActivity {

	private TextView fromText, toText, messageText, timeText;
	private ImageView messagePic;
	private boolean isSent;
	private RatingBar ratingBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setTitle("Your message");
		setContentView(R.layout.message_details);

		toText = (TextView) findViewById(R.id.toText);
		fromText = (TextView) findViewById(R.id.fromText);
		messageText = (TextView) findViewById(R.id.messageText);
		timeText = (TextView) findViewById(R.id.timeText);
		messagePic = (ImageView) findViewById(R.id.pic);
		ratingBar = (RatingBar) findViewById(R.id.rating_bar);

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
				Method.GET,
				Utils.getFilledUrl(UrlConstants.getGMDelUrl(), list), null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						String jsonrep = response.toString();

						List<GeoMessage> msgs = GeoMessenger.geoMessages
								.getResult();
						if (msgs.indexOf(GeoMessenger.selectedGeoMessage) >= 0)
							msgs.remove(msgs
									.indexOf(GeoMessenger.selectedGeoMessage));
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

		if (entry.getRating() > 0) {
			ratingBar.setRating(entry.getRating());
			ratingBar.setVisibility(View.VISIBLE);
		}

		if (!Utils.isEmpty(entry.getPicName())) {
			AmazonS3Client s3Client = new AmazonS3Client(
					new BasicAWSCredentials(Constants.A_A_K, Constants.A_S_K));

			ResponseHeaderOverrides override = new ResponseHeaderOverrides();
			override.setContentType("image/jpeg");

			GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
					GeoMessenger.getPictureBucket(), entry.getPicName());
			urlRequest.setExpiration(new Date(System.currentTimeMillis()
					+ Constants.MILLIS_IN_AN_HOUR));
			urlRequest.setResponseHeaders(override);

			URL url = s3Client.generatePresignedUrl(urlRequest);

			loadImage(url.toString(), messagePic);
		}

		messageText.setText(entry.getMessage());
		timeText.setText(Utils.getHumanReadableTime(entry.getTimestamp()));
	}
}
