/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nkt.geomessenger.service;

import java.text.DecimalFormat;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.nkt.geomessenger.GeoMessenger;
import com.nkt.geomessenger.R;
import com.nkt.geomessenger.model.GeoMessage;
import com.nkt.geomessenger.utils.Utils;
import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.notification.NotificationUtil;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfoHelper;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * The sample extension service handles extension registration and inserts data
 * into the notification database.
 */
public class SampleExtensionService extends ExtensionService {

	/**
	 * Extensions specific id for the source
	 */
	public static final String EXTENSION_SPECIFIC_ID = "EXTENSION_SPECIFIC_ID_GEOMESSENGER_NOTIFICATION";

	/**
	 * Extension key
	 */
	public static final String EXTENSION_KEY = "com.sonymobile.smartconnect.extension.notificationsample.key";

	/**
	 * Log tag
	 */
	public static final String LOG_TAG = "SampleNotificationExtension";

	/**
	 * Starts periodic insert of data handled in onStartCommand()
	 */
	public static final String INTENT_ACTION_START = "com.sonymobile.smartconnect.extension.notificationsample.action.start";

	/**
	 * Stop periodic insert of data, handled in onStartCommand()
	 */
	public static final String INTENT_ACTION_STOP = "com.sonymobile.smartconnect.extension.notificationsample.action.stop";

	private Handler handler;

	private Runnable swSender;

	public SampleExtensionService() {
		super(EXTENSION_KEY);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "onCreate");

		handler = new Handler();
		swSender = new Runnable() {

			@Override
			public void run() {

				for (GeoMessage gm : GeoMessenger.geoMessages.getResult()) {
					if (!GeoMessenger.messagesSentToWatch.contains(gm)) {
						addData(gm);
						GeoMessenger.messagesSentToWatch.add(gm);
					}
				}

				handler.postDelayed(swSender,1000);
			}
		};
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onStartCommand()
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int retVal = super.onStartCommand(intent, flags, startId);
		if (intent != null) {
			if (INTENT_ACTION_START.equals(intent.getAction())) {
				Log.d(LOG_TAG, "onStart action: INTENT_ACTION_START");
				startAddData();
				stopSelfCheck();
			} else if (INTENT_ACTION_STOP.equals(intent.getAction())) {
				Log.d(LOG_TAG, "onStart action: INTENT_ACTION_STOP");
				stopAddData();
				stopSelfCheck();
			}
		}

		return retVal;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy");
	}

	/**
	 * Start periodic data insertion into event table
	 */
	private void startAddData() {
		handler.post(swSender);
	}

	/**
	 * Cancel scheduled data insertion
	 */
	private void stopAddData() {
		handler.removeCallbacks(swSender);
	}

	/**
	 * Add geomessages to db to be picked up by app
	 */
	private void addData(GeoMessage gm) {

		if (gm == null)
			return;

		String name = gm.getFromName();
		double distance = Utils.haversine(
				GeoMessenger.customerLocation.getLatitude(),
				GeoMessenger.customerLocation.getLongitude(),
				Double.parseDouble(gm.getLatitude()),
				Double.parseDouble(gm.getLongitude()))*1000;
		
		DecimalFormat df = new DecimalFormat("#.##");

		String message = name + " left a message for you " + df.format(distance)
				+ " meters away saying ->" + gm.getGeoMessage();
		long time = System.currentTimeMillis();
		long sourceId = NotificationUtil.getSourceId(this,
				EXTENSION_SPECIFIC_ID);
		if (sourceId == NotificationUtil.INVALID_ID) {
			Log.e(LOG_TAG, "Failed to insert data");
			return;
		}

		String profileImage = ExtensionUtils.getUriString(this,
				R.drawable.widget_default_userpic_bg);

		ContentValues eventValues = new ContentValues();
		eventValues.put(Notification.EventColumns.EVENT_READ_STATUS, false);
		eventValues.put(Notification.EventColumns.DISPLAY_NAME, name);
		eventValues.put(Notification.EventColumns.MESSAGE, message);
		eventValues.put(Notification.EventColumns.PERSONAL, 1);
		eventValues.put(Notification.EventColumns.PROFILE_IMAGE_URI,
				profileImage);
		eventValues.put(Notification.EventColumns.PUBLISHED_TIME, time);
		eventValues.put(Notification.EventColumns.SOURCE_ID, sourceId);

		try {
			getContentResolver().insert(Notification.Event.URI, eventValues);
		} catch (IllegalArgumentException e) {
			Log.e(LOG_TAG, "Failed to insert event", e);
		} catch (SecurityException e) {
			Log.e(LOG_TAG,
					"Failed to insert event, is Live Ware Manager installed?",
					e);
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Failed to insert event", e);
		}
	}

	@Override
	protected void onViewEvent(Intent intent) {
		String action = intent
				.getStringExtra(Notification.Intents.EXTRA_ACTION);
		String hostAppPackageName = intent
				.getStringExtra(Registration.Intents.EXTRA_AHA_PACKAGE_NAME);
		boolean advancedFeaturesSupported = DeviceInfoHelper
				.isSmartWatch2ApiAndScreenDetected(this, hostAppPackageName);

		int eventId = intent.getIntExtra(Notification.Intents.EXTRA_EVENT_ID,
				-1);
		if (Notification.SourceColumns.ACTION_1.equals(action)) {
			doAction1(eventId);
		} else if (Notification.SourceColumns.ACTION_2.equals(action)) {
			// Here we can take different actions depending on the device.
			if (advancedFeaturesSupported) {
				Toast.makeText(this, "Action 2 API level 2", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "Action 2", Toast.LENGTH_LONG).show();
			}
		} else if (Notification.SourceColumns.ACTION_3.equals(action)) {
			Toast.makeText(this, "Action 3", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onRefreshRequest() {
		// Do nothing here, only relevant for polling extensions, this
		// extension is always up to date
	}

	/**
	 * Show toast with event information
	 * 
	 * @param eventId
	 *            The event id
	 */
	public void doAction1(int eventId) {
		Log.d(LOG_TAG, "doAction1 event id: " + eventId);
		Cursor cursor = null;
		try {
			String name = "";
			String message = "";
			cursor = getContentResolver()
					.query(Notification.Event.URI, null,
							Notification.EventColumns._ID + " = " + eventId,
							null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int nameIndex = cursor
						.getColumnIndex(Notification.EventColumns.DISPLAY_NAME);
				int messageIndex = cursor
						.getColumnIndex(Notification.EventColumns.MESSAGE);
				name = cursor.getString(nameIndex);
				message = cursor.getString(messageIndex);
			}

		} catch (SQLException e) {
			Log.e(LOG_TAG, "Failed to query event", e);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Failed to query event", e);
		} catch (IllegalArgumentException e) {
			Log.e(LOG_TAG, "Failed to query event", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * Called when extension and sources has been successfully registered.
	 * Override this method to take action after a successful registration.
	 */
	@Override
	public void onRegisterResult(boolean result) {
		super.onRegisterResult(result);
		Log.d(LOG_TAG, "onRegisterResult");

		// Start adding data if extension is active in preferences
		if (result)
			startAddData();
	}

	@Override
	protected RegistrationInformation getRegistrationInformation() {
		return new SampleRegistrationInformation(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.aef.util.ExtensionService#
	 * keepRunningWhenConnected()
	 */
	@Override
	protected boolean keepRunningWhenConnected() {
		return true;
	}
}
