package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nkt.geomessenger.constants.UrlConstants;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.model.GsonConvertibleObject;
import com.nkt.geomessenger.model.QueryGeoMessagesResult;
import com.nkt.geomessenger.utils.Utils;

public class MessagesActivity extends GMActivity {
	
	private QueryGeoMessagesResult sentMessagesResult;
	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setTitle("Sent messages");
		setContentView(R.layout.messages_list);

		listview = (ListView) findViewById(R.id.list_view);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("user_id", GeoMessenger.userId));

		JsonObjectRequest jsonSentMessagesRequest = new JsonObjectRequest(
				Method.GET, Utils.getFilledUrl(
						UrlConstants.getUserGeoMsgsUrl(), list), null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						String jsonrep = response.toString();

						sentMessagesResult = GsonConvertibleObject
								.getObjectFromJson(jsonrep,
										QueryGeoMessagesResult.class);
						
						populateListView();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});
		
		GeoMessenger.queue.add(jsonSentMessagesRequest);
	}

	private void populateListView() {
		listview.setAdapter(new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				GeoMessage entry = getItem(position);
				if (convertView == null) {
					LayoutInflater inflater = (LayoutInflater) MessagesActivity.this
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.list_item, null);
				}

				TextView nameText = (TextView) convertView.findViewById(R.id.titleText);
				nameText.setText(entry.getToUserName());
				nameText.setTypeface(GeoMessenger.robotoThin);

				TextView subtitleText = (TextView) convertView
						.findViewById(R.id.subtitleText);
				if (entry.getTimestamp() != 0L) {
					subtitleText.setText(entry.getMessage() + " at " + Long.toString(entry.getTimestamp()));
				} else
					subtitleText.setVisibility(View.GONE);

				ImageView picIcon = (ImageView) convertView.findViewById(R.id.pic);

				String url = entry.getToUserPic();
				if (url != null && !url.equalsIgnoreCase("null")
						&& !url.equalsIgnoreCase("")) {
					loadImage(url, picIcon);
				}

				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public GeoMessage getItem(int position) {
				if(position < 0 || position > getCount())
					position = 0;
				
				return sentMessagesResult.getResult().get(position);
			}
			
			@Override
			public int getCount() {
				return sentMessagesResult.getResult().size();
			}
		});
	}
}
