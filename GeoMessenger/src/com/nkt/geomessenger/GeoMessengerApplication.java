package com.nkt.geomessenger;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;

import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;

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
		
	    // Create the CrittercismConfig instance.
	    CrittercismConfig config = new CrittercismConfig();
	    boolean shouldIncludeVersionCode = true;
	    // Set the custom version name.
	    config.setVersionCodeToBeIncludedInVersionString(shouldIncludeVersionCode);
	    // Initialize.
	    Crittercism.initialize(getApplicationContext(),
	        "52de3c6846b7c24fb2000007", config);
	    
	    GeoMessenger.isFirstTime = true;
	}
}
