package com.nkt.geomessenger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class GMActivity extends SherlockFragmentActivity{
	
	private volatile boolean isRunning;

	public boolean isRunning() {
		return isRunning;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		isRunning = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		isRunning = false;
	}
	
	public String getActivityLabel() {
		return this.getClass().getSimpleName();
	}
	
	private boolean isConnectivityAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();

		return (info != null) ? info.isConnected() : false;
	}
}
