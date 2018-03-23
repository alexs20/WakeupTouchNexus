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
package com.wolandsoft.wtn.adapter;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.wolandsoft.wtn.R;

public class LedNotifyAdapter {
	private final int LED_NOTIFICATION_ID = 2;
	private NotificationManager mNotifyMgr;
	private Context mContext;
	private boolean mIsEnabled = false;
	private int mRedLevel = 0;
	private int mYellowLevel = 0;

	public LedNotifyAdapter(Context context) {
		mContext = context;
		mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void lightup() {
		if (mIsEnabled) {
			int batteryLevel = getBatteryLevel();
			int argb = 0xFF00FF00;
			if (batteryLevel <= mRedLevel) {
				argb = 0xFFFF0000;
			} else if (batteryLevel <= mYellowLevel) {
				argb = 0xFFFFFF00;
			}
			Notification.Builder builder = new Notification.Builder(mContext);
			builder.setLights(argb, Integer.MAX_VALUE, 0);
			builder.setSmallIcon(R.drawable.ic_notif);
			@SuppressWarnings("deprecation")
			Notification notif = builder.getNotification();
			notif.flags = Notification.FLAG_SHOW_LIGHTS;
			mNotifyMgr.notify(LED_NOTIFICATION_ID, notif);
		}
	}

	public void reset() {
		mNotifyMgr.cancel(LED_NOTIFICATION_ID);
	}

	private int getBatteryLevel() {
		IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = mContext.registerReceiver(null, iFilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		return level * 100 / scale;
	}

	public boolean isEnabled() {
		return mIsEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.mIsEnabled = isEnabled;
	}

	public int getRedLevel() {
		return mRedLevel;
	}

	public void setRedLevel(int redLevel) {
		this.mRedLevel = redLevel;
	}

	public int getYellowLevel() {
		return mYellowLevel;
	}

	public void setYellowLevel(int yellowLevel) {
		this.mYellowLevel = yellowLevel;
	}

}
