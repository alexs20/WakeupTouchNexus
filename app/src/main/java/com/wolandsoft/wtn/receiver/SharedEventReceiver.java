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
package com.wolandsoft.wtn.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.wolandsoft.wtn.service.SensorMonitorService;

public class SharedEventReceiver extends BroadcastReceiver {

	private final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private final String PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Context appCtx = context.getApplicationContext();
		if (BOOT_COMPLETED.equals(action) || (PACKAGE_REPLACED.equals(action) && intent.getDataString().contains(appCtx.getPackageName()))) {
			Intent myIntent = new Intent(appCtx, SensorMonitorService.class);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				appCtx.startForegroundService(myIntent);
			} else {
				appCtx.startService(myIntent);
			}
		}
	}
}