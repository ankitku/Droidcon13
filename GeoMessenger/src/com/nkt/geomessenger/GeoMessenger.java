package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.location.Location;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nkt.geomessenger.map.CustomerLocationUpdater;
import com.nkt.geomessenger.model.FBFriend;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.model.Result;

public class GeoMessenger{

	public final static String TAG = CallbackFragmentActivity.class
			.getSimpleName();
	
	public static RequestQueue queue;
	
	public static String userName;
	
	public static String userId;
	
	public static List<FBFriend> userFriends = new ArrayList<FBFriend>();
	
	public static Gson gson = new GsonBuilder().create();

	public static boolean isGooglePlayServicesAvailable;
	
	public static Location customerLocation;

	public static CustomerLocationUpdater customerLocationUpdateHandler;
	
	public static Result geoMessages;
	
	public static HashSet<GeoMessage> messagesSentToWatch = new HashSet<GeoMessage>();
}