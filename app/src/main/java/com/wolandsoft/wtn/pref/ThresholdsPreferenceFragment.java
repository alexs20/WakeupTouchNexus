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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;

import com.wolandsoft.wtn.R;

public class ThresholdsPreferenceFragment extends BasePreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init(R.xml.preference_thresholds);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (getString(R.string.pref_wakelock_hold_sec_key).equals(key)) {
			String summary = String.format(getResources().getString(R.string.pref_wakelock_hold_sec_summary),
					sharedPreferences.getInt(key, getResources().getInteger(R.integer.pref_wakelock_hold_sec_value)));
			Preference prefControl = getPreferenceScreen().findPreference(key);
			prefControl.setSummary(summary);
		} else if (getString(R.string.pref_sensor_cover_delay_msec_key).equals(key)) {
			String summary = String.format(getResources().getString(R.string.pref_sensor_cover_delay_msec_summary),
					sharedPreferences.getInt(key, getResources().getInteger(R.integer.pref_sensor_cover_delay_msec_value)));
			Preference prefControl = getPreferenceScreen().findPreference(key);
			prefControl.setSummary(summary);
		} else if (getString(R.string.pref_sensor_acceptance_window_msec_key).equals(key)) {
			String summary = String.format(getResources().getString(R.string.pref_sensor_acceptance_window_msec_summary),
					sharedPreferences.getInt(key, getResources().getInteger(R.integer.pref_sensor_acceptance_window_msec_value)));
			Preference prefControl = getPreferenceScreen().findPreference(key);
			prefControl.setSummary(summary);
		} else if (getString(R.string.pref_sensor_uncover_delay_msec_key).equals(key)) {
			String summary = String.format(getResources().getString(R.string.pref_sensor_uncover_delay_msec_summary),
					sharedPreferences.getInt(key, getResources().getInteger(R.integer.pref_sensor_uncover_delay_msec_value)));
			Preference prefControl = getPreferenceScreen().findPreference(key);
			prefControl.setSummary(summary);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		return true;
	}

}
