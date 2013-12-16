package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Session;
import com.nkt.geomessenger.constants.GMConstants;
import com.nkt.geomessenger.model.ListItemWithIcon;
import com.nkt.geomessenger.model.ListviewAdapter;

public class GMActivity extends SherlockFragmentActivity {

	private volatile boolean isRunning;
	protected LinearLayout fullLayout;
	protected FrameLayout actContent;

	protected ProgressDialog progressDialog;
	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	protected ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	private Handler handler = new Handler();

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

	@Override
	public void setContentView(final int layoutResID) {
		fullLayout = (LinearLayout) getLayoutInflater().inflate(
				R.layout.parent_layout, null); // Your base layout here
		actContent = (FrameLayout) fullLayout.findViewById(R.id.act_content);
		// Setting content of layout your provided to the act_content frame
		getLayoutInflater().inflate(layoutResID, actContent, true);
		super.setContentView(fullLayout);
		// here you can get your drawer buttons and define how they should
		// behave and what must they do, so you won't be needing to repeat it in
		// every activity class

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		populateDrawer();
	}

	private void populateDrawer() {
		mTitle = getSupportActionBar().getTitle();
		mDrawerTitle = "GeoMessenger";
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		final List<ListItemWithIcon> tabs = new ArrayList<ListItemWithIcon>();
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact,
				"Nearby Messages", null));
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact, "Sent",
				null));
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact,
				"Invite Friends", null));
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact,
				"Feedback", null));
		ListviewAdapter menuAdapter = new ListviewAdapter(GMActivity.this, tabs);

		mDrawerList.setAdapter(menuAdapter);
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				mDrawerLayout.closeDrawer(mDrawerList);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						startActivityWithName(GMConstants.fromString(tabs.get(
								position).getTitle()));
					}
				}, 250);
			}
		});

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(GMActivity.this,
				mDrawerLayout, R.drawable.ic_drawer, R.string.app_name,
				R.string.app_name) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void startActivityWithName(GMConstants name) {
		switch (name) {
		case SENT_MSGS: {
			Intent intent = new Intent(getApplicationContext(),
					MessagesActivity.class);
			startActivity(intent);
			break;
		}
		}
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_signout: {
			LogoutAsync async = new LogoutAsync();
			async.execute();
			break;
		}
		case R.id.menu_legalnotices: {
			String LicenseInfo = "HI";
			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(
					GMActivity.this);
			LicenseDialog.setTitle("Legal Notices");
			LicenseDialog.setMessage(LicenseInfo);
			LicenseDialog.show();
			return true;
		}
		case android.R.id.home:
			if (mDrawerToggle.isDrawerIndicatorEnabled()) {
				if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
					mDrawerLayout.closeDrawer(mDrawerList);
				} else {
					mDrawerLayout.openDrawer(mDrawerList);
				}
			} else
				finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class LogoutAsync extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Session.getActiveSession().closeAndClearTokenInformation();
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Creates an Intent to bring back the MainActivity from the stack
			Intent intent = new Intent(getApplicationContext(),
					LoginFragmentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

			finish();
		}
	}

	private boolean isConnectivityAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();

		return (info != null) ? info.isConnected() : false;
	}
}
