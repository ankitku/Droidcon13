package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.nkt.geomessenger.constants.GMConstants;
import com.nkt.geomessenger.model.ListItemWithIcon;
import com.nkt.geomessenger.model.ListviewAdapter;
import com.nkt.geomessenger.utils.ImageCacheManager;

public class GMActivity extends SherlockFragmentActivity {

	protected LinearLayout fullLayout;
	protected FrameLayout actContent;

	protected ProgressDialog progressDialog;
	protected DrawerLayout mDrawerLayout;
	protected ListView mDrawerList;
	protected ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	private String childActivities[] = { "LoginActivity",
			"MessageDetailsActivity" };
	private List<String> childActivitiesList = Arrays.asList(childActivities);

	private static final Uri M_FACEBOOK_URL = Uri
			.parse("http://m.facebook.com");

	protected static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 10;
	protected static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
	protected static int DISK_IMAGECACHE_QUALITY = 100; // PNG is lossless so
														// quality is ignored
														// but must be provided
	private Handler handler = new Handler();

	public boolean isRunning() {
		return GeoMessenger.isRunning;
	}

	@Override
	protected void onResume() {
		super.onResume();

		GeoMessenger.isRunning = true;
	}

	@Override
	protected void onPause() {
		super.onPause();

		GeoMessenger.isRunning = false;
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
		tabs.add(new ListItemWithIcon(0, "Nearby Messages", null));
		tabs.add(new ListItemWithIcon(0, "Sent", null));
		tabs.add(new ListItemWithIcon(0, "Invite Friends", null));
		tabs.add(new ListItemWithIcon(0, "Feedback", null));
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

		if (childActivitiesList.contains(getActivityLabel())) {
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}

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
		case FEEDBACK:
			sendFeedback();

		}

		if (!"MapActivity".equals(getActivityLabel().toString()))
			finish();
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
			if (Session.getActiveSession() != null)
				Session.getActiveSession().closeAndClearTokenInformation();
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// Creates an Intent to bring back the MainActivity from the stack
			Intent intent = new Intent(getApplicationContext(),
					LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);

			finish();
		}
	}

	public void sendFeedback() {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		String emailTo[] = { GeoMessenger.emailFeedback };
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailTo);

		String version = GeoMessenger.versionName.substring(0, 5);

		intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on GeoMessenger "
				+ version + " for Android");

		// Device Data
		String phoneModel = android.os.Build.MODEL;
		String phoneDevice = android.os.Build.DEVICE;
		String androidVersion = android.os.Build.VERSION.RELEASE;

		intent.putExtra(Intent.EXTRA_TEXT,
				"\n\n\n-------------------------\nGeoMessenger Android "
						+ version + " on " + phoneModel + " " + phoneDevice
						+ " running Android " + androidVersion + " user_id : "
						+ GeoMessenger.userId);

		intent.setType("plain/text");
		startActivity(Intent.createChooser(intent, "Send your email from:"));
	}

	protected void handleError(FacebookRequestError error) {
		DialogInterface.OnClickListener listener = null;
		String dialogBody = null;

		if (error == null) {
			dialogBody = getString(R.string.generic_error);
		} else {
			switch (error.getCategory()) {
			case AUTHENTICATION_RETRY:
				// tell the user what happened by getting the message id, and
				// retry the operation later
				String userAction = (error.shouldNotifyUser()) ? ""
						: getString(error.getUserActionMessageId());
				dialogBody = getString(R.string.error_authentication_retry,
						userAction);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Intent intent = new Intent(Intent.ACTION_VIEW,
								M_FACEBOOK_URL);
						startActivity(intent);
					}
				};
				break;

			case AUTHENTICATION_REOPEN_SESSION:
				// close the session and reopen it.
				dialogBody = getString(R.string.error_authentication_reopen);
				listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						Session session = Session.getActiveSession();
						if (session != null && !session.isClosed()) {
							session.closeAndClearTokenInformation();
						}
					}
				};
				break;

			case SERVER:
			case THROTTLING:
				// this is usually temporary, don't clear the fields, and
				// ask the user to try again
				dialogBody = getString(R.string.error_server);
				break;

			case BAD_REQUEST:
				// this is likely a coding error, ask the user to file a bug
				dialogBody = getString(R.string.error_bad_request,
						error.getErrorMessage());
				break;

			case OTHER:
			case CLIENT:
			default:
				// an unknown issue occurred, this could be a code error, or
				// a server side issue, log the issue, and either ask the
				// user to retry, or file a bug
				dialogBody = getString(R.string.error_unknown,
						error.getErrorMessage());
				break;
			}
		}

		if (isRunning())
			new AlertDialog.Builder(this)
					.setPositiveButton(R.string.okay, listener)
					.setTitle(R.string.generic_error).setMessage(dialogBody)
					.show();
	}

	protected void createImageCache() {
		ImageCacheManager.getInstance().init(this, this.getPackageCodePath(),
				DISK_IMAGECACHE_SIZE, DISK_IMAGECACHE_COMPRESS_FORMAT,
				DISK_IMAGECACHE_QUALITY);
	}

	protected void loadImage(final String imageUrl, final ImageView iv) {
		ImageCacheManager.getInstance().getImage(imageUrl, new ImageListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				if(iv == null)
					return;
				
				iv.setImageResource(R.drawable.placeholder_contact);
			}

			@Override
			public void onResponse(ImageContainer response, boolean isImmediate) {
				if(iv == null)
					return;
				
				iv.setImageBitmap(response.getBitmap());
			}
		});
	}

	private boolean isConnectivityAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();

		return (info != null) ? info.isConnected() : false;
	}
}
