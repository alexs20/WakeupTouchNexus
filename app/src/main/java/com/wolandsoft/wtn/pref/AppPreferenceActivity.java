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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.wolandsoft.wtn.AppConstants;
import com.wolandsoft.wtn.R;
import com.wolandsoft.wtn.activity.CreditActivity;
import com.wolandsoft.wtn.service.SensorMonitorService;

import java.util.List;

public class AppPreferenceActivity extends PreferenceActivity implements AppConstants {

    private String mAboutTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasHeaders()) {

            Context appCtx = getApplicationContext();
            try {
                PackageInfo appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                mAboutTxt = (String) getText(R.string.app_about);
                mAboutTxt = String.format(mAboutTxt, appInfo.versionName);
            } catch (NameNotFoundException e) {
                // ignore
            }

            Button btnAbout = new Button(this);
            btnAbout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AppPreferenceActivity.this, CreditActivity.class);
                    startActivity(intent);
                }
            });
            btnAbout.setText(R.string.app_about_label);
            btnAbout.setGravity(Gravity.CENTER);
            setListFooter(btnAbout);

            // default values
            PreferenceManager.setDefaultValues(appCtx, TAG, MODE_MULTI_PROCESS, R.xml.preference_general, false);
            PreferenceManager.setDefaultValues(appCtx, TAG, MODE_MULTI_PROCESS, R.xml.preference_thresholds, false);
            PreferenceManager.setDefaultValues(appCtx, TAG, MODE_MULTI_PROCESS, R.xml.preference_led, false);
            PreferenceManager.setDefaultValues(appCtx, TAG, MODE_MULTI_PROCESS, R.xml.preference_vibrate, false);

            SharedPreferences sharedPref = appCtx.getSharedPreferences(TAG, MODE_MULTI_PROCESS);
            boolean isAutoStartDef = getResources().getBoolean(R.bool.pref_service_enabled_value);
            boolean isAutoStart = sharedPref.getBoolean(getString(R.string.pref_service_enabled_key), isAutoStartDef);
            if (isAutoStart) {
                Intent intent = new Intent(appCtx, SensorMonitorService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        }

    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName) ||
                GVectorPreferenceFragment.class.getName().equals(fragmentName) ||
                LedPreferenceFragment.class.getName().equals(fragmentName) ||
                ThresholdsPreferenceFragment.class.getName().equals(fragmentName) ||
                VibratePreferenceFragment.class.getName().equals(fragmentName);
    }
}
