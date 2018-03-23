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
package com.wolandsoft.wtn.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.wolandsoft.wtn.R;

public class CreditActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credit_view);

		try {
			PackageInfo appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String aboutTxt = (String) getText(R.string.app_about);
			aboutTxt = String.format(aboutTxt, appInfo.versionName);
			TextView txtCredit = (TextView) findViewById(R.id.txtCredit);
			txtCredit.setText(aboutTxt);
		} catch (NameNotFoundException e) {
			// ignore
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

}
