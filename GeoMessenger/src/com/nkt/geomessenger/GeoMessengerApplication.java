package com.nkt.geomessenger;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;

import com.android.volley.toolbox.Volley;

public class GeoMessengerApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		GeoMessenger.queue = Volley.newRequestQueue(this);
		GeoMessenger.robotoThin = Typeface.createFromAsset(
				getApplicationContext().getAssets(), "Roboto-Thin.ttf");

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			GeoMessenger.versionCode = packageInfo.versionCode;
			GeoMessenger.versionName = packageInfo.versionName;
		} catch (NameNotFoundException nnfe) {
		}

	}
}
