package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.facebook.model.GraphUser;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nkt.geomessenger.map.CustomerLocationUpdater;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.model.QueryGeoMessagesResult;

public class GeoMessenger {

	public final static String TAG = MapActivity.class
			.getSimpleName();

	public static RequestQueue queue;

	public static String userName;

	public static String userId;

	public static Gson gson = new GsonBuilder().setFieldNamingPolicy(
			FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

	public static boolean isGooglePlayServicesAvailable;

	public static Location customerLocation;

	public static CustomerLocationUpdater customerLocationUpdateHandler;

	public static QueryGeoMessagesResult geoMessages;

	private static List<GraphUser> selectedUsers = new ArrayList<GraphUser>();

	public static List<GraphUser> getSelectedUsers() {
		return selectedUsers;
	}

	public static void setSelectedUsers(List<GraphUser> users) {
		selectedUsers = users;
	}
	
	public static GeoMessage selectedGeoMessage;

	public static List<GeoMessage> msgsForSW  = new ArrayList<GeoMessage>();
	
	public static boolean isPollServiceStarted;
	
	public static Typeface robotoThin;

	public static String emailFeedback = "ankitkumar.itbhu@gmail.com";

	public static String versionName;

	public static int versionCode;
	
	public static volatile boolean isRunning;
}