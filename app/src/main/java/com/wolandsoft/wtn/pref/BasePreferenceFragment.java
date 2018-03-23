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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.wolandsoft.wtn.AppConstants;
import com.wolandsoft.wtn.R;
import com.wolandsoft.wtn.service.SensorMonitorService;

public abstract class BasePreferenceFragment extends PreferenceFragment implements AppConstants, OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

	private OnSharedPreferenceChangeListener listener;

	protected Context getApplicationContext() {
		return getActivity().getApplicationContext();
	}

	protected SharedPreferences getSharedPreferences() {
		return getActivity().getApplicationContext().getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS);
	}

	protected void init(int resId) {
		getPreferenceManager().setSharedPreferencesName(TAG);
		addPreferencesFromResource(resId);
		listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				sharedPreferences.edit().commit();
				BasePreferenceFragment.this.onSharedPreferenceChanged(sharedPreferences, key);
				boolean isServiceEnabled = sharedPreferences.getBoolean(getString(R.string.pref_service_enabled_key),
						getResources().getBoolean(R.bool.pref_service_enabled_value));
				if (isServiceEnabled) {
					Context appCtx = getApplicationContext();
					appCtx.startService(new Intent(appCtx, SensorMonitorService.class));
				}
			}
		};

		SharedPreferences shrPrefs = getSharedPreferences();
		shrPrefs.registerOnSharedPreferenceChangeListener(listener);
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			Preference pref = getPreferenceScreen().getPreference(i);
			if (pref instanceof PreferenceCategory) {
				PreferenceCategory cat = (PreferenceCategory) pref;
				for (int j = 0; j < cat.getPreferenceCount(); j++) {
					pref = cat.getPreference(j);
					onSharedPreferenceChanged(shrPrefs, pref.getKey());
					pref.setOnPreferenceChangeListener(this);
				}
			} else {
				onSharedPreferenceChanged(shrPrefs, pref.getKey());
				pref.setOnPreferenceChangeListener(this);
			}
		}
	}

	@Override
	public void onDestroy() {
		SharedPreferences shrPrefs = getSharedPreferences();
		shrPrefs.unregisterOnSharedPreferenceChangeListener(listener);
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			Preference pref = getPreferenceScreen().getPreference(i);
			if (pref instanceof PreferenceCategory) {
				PreferenceCategory cat = (PreferenceCategory) pref;
				for (int j = 0; j < cat.getPreferenceCount(); j++) {
					pref = cat.getPreference(j);
					pref.setOnPreferenceChangeListener(null);
				}
			} else {
				pref.setOnPreferenceChangeListener(null);
			}
		}
		super.onDestroy();
	}
}
