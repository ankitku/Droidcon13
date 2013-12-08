package com.nkt.geomessenger;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.nkt.geomessenger.map.CustomerLocationUpdater;
import com.nkt.geomessenger.model.FBFriend;

public class LoginFragmentActivity extends GMActivity {

	private LoginButton FBLoginButton;

	private static final Uri M_FACEBOOK_URL = Uri
			.parse("http://m.facebook.com");

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private Session session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (GeoMessenger.customerLocationUpdateHandler == null)
			GeoMessenger.customerLocationUpdateHandler = new CustomerLocationUpdater();

		GeoMessenger.customerLocationUpdateHandler
				.start(LoginFragmentActivity.this);

		String[] perms = { "email" };
		FBLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
		FBLoginButton.setReadPermissions(Arrays.asList(perms));

		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

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

				// makeFBRequests(session);
				Intent intent = new Intent();
				intent.setClass(LoginFragmentActivity.this,
						CallbackFragmentActivity.class);
				startActivity(intent);

			} else if (Session.getActiveSession() != null
					&& Session.getActiveSession().isClosed()) {
				return;
			}
		}
	}

	private void makeFBRequests(final Session session) {

		String fqlQuery = "SELECT uid,name,pic_square FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())";

		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		final Request myFriendsRequest = new Request(session, "/fql", params,
				HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						Log.i("Login", "Result: " + response.toString());

						try {
							GraphObject graphObject = response.getGraphObject();
							JSONObject jsonObject = graphObject
									.getInnerJSONObject();
							Log.d("data", jsonObject.toString(0));

							JSONArray array = jsonObject.getJSONArray("data");
							for (int i = 0; i < array.length(); i++) {

								JSONObject friend = array.getJSONObject(i);

								FBFriend f = new FBFriend(friend
										.getString("uid"), friend
										.getString("name"), friend
										.getString("pic_square"));

								GeoMessenger.userFriends.add(f);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

		final Request meRequest = Request.newMeRequest(session,
				new Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {
							if (user != null) {

								GeoMessenger.userId = user.getId();
								Request.executeBatchAsync(myFriendsRequest);
							}
						}
						if (response.getError() != null) {
							handleError(response.getError());
						}
					}
				});

		meRequest.executeAsync();

	}

	private void handleError(FacebookRequestError error) {
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

		new AlertDialog.Builder(this)
				.setPositiveButton(R.string.okay, listener)
				.setTitle(R.string.generic_error).setMessage(dialogBody).show();
	}
}
