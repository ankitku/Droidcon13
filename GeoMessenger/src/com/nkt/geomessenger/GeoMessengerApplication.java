package com.nkt.geomessenger;

import android.app.Application;
import android.graphics.Typeface;

import com.android.volley.toolbox.Volley;

public class GeoMessengerApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		GeoMessenger.queue = Volley.newRequestQueue(this);
		GeoMessenger.robotoThin = Typeface.createFromAsset(getApplicationContext().getAssets(), "Roboto-Thin.ttf");
	}
}
