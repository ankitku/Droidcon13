package com.nkt.geomessenger;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GeoMessenger {

	public final static String TAG = CallbackFragmentActivity.class
			.getSimpleName();
	
	public static RequestQueue queue;
	
	public static String userName;
	
	public static String userEmail;
	
	public static Gson gson = new GsonBuilder().create();

	public static boolean isGooglePlayServicesAvailable;
}