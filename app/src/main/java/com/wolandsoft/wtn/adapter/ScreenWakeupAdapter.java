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

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.wolandsoft.wtn.AppConstants;
import com.wolandsoft.wtn.receiver.AppDeviceAdminReceiver;
import com.wolandsoft.wtn.utils.ILog;
import com.wolandsoft.wtn.utils.SafeRunnable;

public class ScreenWakeupAdapter implements AppConstants {
	private PowerManager mPowerManager;
	private KeyguardManager mKeyguardManager;
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mDeviceAdmin;
	private Handler mHandler;
	private DelayedTask mScheduledTask;
	private int mKeepScreenOnMsec = 0;

	public ScreenWakeupAdapter(Context context, Handler handler) {
		mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(context, AppDeviceAdminReceiver.class);
		mHandler = handler;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("Wakelock")
	public void wakeup() {
		if (mScheduledTask != null) {
			mHandler.removeCallbacks(mScheduledTask);
			mScheduledTask.release();
			mScheduledTask = null;
			ILog.d("Scheduled screen off canceled");
		}
		WakeLock wakeLock = mPowerManager.newWakeLock(
				(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
		wakeLock.acquire();
		mScheduledTask = new DelayedTask(wakeLock);
		mHandler.postDelayed(mScheduledTask, mKeepScreenOnMsec);
	}

	private class DelayedTask extends SafeRunnable {
		WakeLock lmWakeLock;

		public DelayedTask(WakeLock wakeLock) {
			lmWakeLock = wakeLock;
		}

		public void release() {
			lmWakeLock.release();
		}

		@SuppressWarnings("deprecation")
		@Override
		public void runSafe() {
			release();
			if (mKeepScreenOnMsec > 0) {
				if (mPowerManager.isScreenOn() && mKeyguardManager.inKeyguardRestrictedInputMode()) {
					if (mDevicePolicyManager.isAdminActive(mDeviceAdmin)) {
						mDevicePolicyManager.lockNow();
					}
				}
			}
			mScheduledTask = null;
		}
	}

	public int getKeepScreenOnMsec() {
		return mKeepScreenOnMsec;
	}

	public void setKeepScreenOnMsec(int keepScreenOnMsec) {
		this.mKeepScreenOnMsec = keepScreenOnMsec;
	}
}
