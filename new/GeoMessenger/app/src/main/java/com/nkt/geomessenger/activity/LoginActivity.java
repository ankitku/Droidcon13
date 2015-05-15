package com.nkt.geomessenger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by ankitku on 04/05/15.
 */
public class LoginActivity extends BaseActivity {

    private CallbackManager mCallbackManager;

    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Profile profile = Profile.getCurrentProfile();

            System.out.println(">>>>>>>>>>>>>>>>>>>>" + profile.getFirstName());
            gotoActivity(LoginActivity.this, MainActivity.class);
        }

        @Override
        public void onCancel() {
            Log.d(getLogTag(), "onCancel");
        }

        @Override
        public void onError(FacebookException e) {
            Log.d(getLogTag(), "onError : " + e);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(LoginActivity.this);

        setContentView(R.layout.activity_login);

        mCallbackManager = CallbackManager.Factory.create();
        setupTokenTracker();
        setupProfileTracker();

        mTokenTracker.startTracking();
        mProfileTracker.startTracking();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        String[] perms = {"email", "user_friends"};
        loginButton.setReadPermissions(perms);
        loginButton.registerCallback(mCallbackManager, mCallBack);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AccessToken.getCurrentAccessToken()!=null)
            gotoActivity(getApplicationContext(), MainActivity.class);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
