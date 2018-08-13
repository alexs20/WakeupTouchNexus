/*
    Copyright 2014-2018 Alexander Shulgin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.wolandsoft.wtn.pref;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import com.wolandsoft.wtn.R;
import com.wolandsoft.wtn.receiver.AppDeviceAdminReceiver;
import com.wolandsoft.wtn.service.SensorMonitorService;

public class GeneralPreferenceFragment extends BasePreferenceFragment {

	private DevicePolicyManager mDPM;
	private ComponentName mDeviceAdmin;
	private CheckBoxPreference mAdminCheckbox;
	private final int REQUEST_CODE_ENABLE_ADMIN = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init(R.xml.preference_general);

		Context appCtx = getApplicationContext();
		mDPM = (DevicePolicyManager) appCtx.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(appCtx, AppDeviceAdminReceiver.class);

		mAdminCheckbox = (CheckBoxPreference) findPreference(getString(R.string.pref_device_admin_enabled_key));
		mAdminCheckbox.setChecked(isActiveAdmin());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CheckBoxPreference bgService = (CheckBoxPreference) findPreference(getString(R.string.pref_run_service_background_key));
			bgService.setChecked(false);
			bgService.setEnabled(false);
			ListPreference notifAction = (ListPreference) findPreference(getString(R.string.pref_notification_action_key));
			notifAction.setDependency(null);
		}
	}

	private boolean isActiveAdmin() {
		return mDPM.isAdminActive(mDeviceAdmin);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (getString(R.string.pref_device_admin_enabled_key).equals(preference.getKey())) {
			boolean toActivate = (Boolean) newValue;
			if (toActivate) {
				// Launch the activity to have the user enable our admin.
				Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
				intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.receiver_device_admin_description));
				startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
				// return false - don't update checkbox until we're really
				// active
				return false;
			} else {
				mDPM.removeActiveAdmin(mDeviceAdmin);
			}
		}

		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdminCheckbox.setChecked(isActiveAdmin());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (getString(R.string.pref_service_enabled_key).equals(key)) {
			boolean isServiceEnabled = sharedPreferences.getBoolean(getString(R.string.pref_service_enabled_key),
					getResources().getBoolean(R.bool.pref_service_enabled_value));
			if (!isServiceEnabled) {
				Context appCtx = getApplicationContext();
				appCtx.stopService(new Intent(appCtx, SensorMonitorService.class));
			}
		} else if (getString(R.string.pref_notification_action_key).equals(key)) {
			String summary = getString(R.string.pref_notification_action_summary);
			String value = sharedPreferences.getString(key, getString(R.string.pref_notification_action_value));
			String[] itemValues = getResources().getStringArray(R.array.pref_notification_action_values);
			String[] itemNames = getResources().getStringArray(R.array.pref_notification_action_titles);
			for (int i = 0; i < itemNames.length; i++) {  
			    if (itemValues[i].equals(value)) {  
			    	summary = String.format(summary, itemNames[i]);  
			    	break;
			    }  
			} 						
			ListPreference prefControl = (ListPreference) getPreferenceScreen().findPreference(key);
			prefControl.setSummary(summary);
		} 	
	}

}
