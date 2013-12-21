/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nkt.geomessenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.FacebookException;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;

/**
 * The PickerActivity enhances the Friend or Place Picker by adding a title and
 * a Done button. The selection results are saved in the ScrumptiousApplication
 * instance.
 */
public class PickerActivity extends SherlockFragmentActivity {

	private FriendPickerFragment friendPickerFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pickers);
		getSupportActionBar().hide();

		Bundle args = getIntent().getExtras();
		FragmentManager manager = getSupportFragmentManager();

		if (savedInstanceState == null) {
			friendPickerFragment = new FriendPickerFragment(args);
		} else {
			friendPickerFragment = (FriendPickerFragment) manager
					.findFragmentById(R.id.picker_fragment);
		}

		friendPickerFragment
				.setOnErrorListener(new PickerFragment.OnErrorListener() {
					@Override
					public void onError(PickerFragment<?> fragment,
							FacebookException error) {
						PickerActivity.this.onError(error);
					}
				});
		friendPickerFragment
				.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
					@Override
					public void onDoneButtonClicked(PickerFragment<?> fragment) {
						finishActivity();
					}
				});
		manager.beginTransaction()
				.replace(R.id.picker_fragment, friendPickerFragment).commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			friendPickerFragment.loadData(false);
		} catch (Exception ex) {
			onError(ex);
		}
	}

	private void onError(Exception error) {
		String text = getString(R.string.exception, error.getMessage());
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		toast.show();
	}

	private void onError(String error, final boolean finishActivity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error_dialog_title)
				.setMessage(error)
				.setPositiveButton(R.string.error_dialog_button_text,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialogInterface, int i) {
								if (finishActivity) {
									finishActivity();
								}
							}
						});
		builder.show();
	}

	private void finishActivity() {
		if (friendPickerFragment != null) {
			GeoMessenger.setSelectedUsers(friendPickerFragment.getSelection());
		}
		setResult(RESULT_OK, null);
		finish();
	}
}
