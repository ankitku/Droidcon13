package com.nkt.geomessenger;

import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.nkt.geomessenger.map.CustomerLocationUpdater;

public class LoginActivity extends GMActivity {

	private LoginButton FBLoginButton;

	private UiLifecycleHelper uiHelper;
	
	private Session session;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		String[] perms = { "email" };
		FBLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
		FBLoginButton.setReadPermissions(Arrays.asList(perms));

		session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, callback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(callback));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		updateActivity(session, session.getState());

		if (GeoMessenger.customerLocationUpdateHandler == null)
			GeoMessenger.customerLocationUpdateHandler = new CustomerLocationUpdater();

		GeoMessenger.customerLocationUpdateHandler
				.start(LoginActivity.this);
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(callback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(callback);
	}

	@Override
	protected void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	private synchronized void onSessionStateChange(Session session,
			SessionState state, Exception exception) { // facebook state changes
		updateActivity(session, state);
	}

	private void updateActivity(Session session, SessionState state) {
		if (isRunning()) {
			if (SessionState.OPENED.equals(state)) {
				this.session = session;
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, MapActivity.class);
				startActivity(intent);
				finish();
			} else if (Session.getActiveSession() != null
					&& Session.getActiveSession().isClosed()) {
				return;
			}
		}
	}
}
