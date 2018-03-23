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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.wolandsoft.wtn.AppConstants;
import com.wolandsoft.wtn.utils.ILog;

public class ProximityStateAdapter implements SensorEventListener, AppConstants {
	private ProximityStateListener mProximityStateListener;
	private PowerManager mPowerManager;
	private SensorManager mSensorManager;
	private Handler mHandler;
	private boolean mIsEnabled = false;

	public ProximityStateAdapter(ProximityStateListener listener, Context context, Handler handler) {
		mProximityStateListener = listener;
		mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mHandler = handler;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		boolean isCovered = event.values[0] == 0;
		long timeStampSensor = event.timestamp / 1000000;
		long timeStamp = System.currentTimeMillis();

		int timePast = (int) (System.currentTimeMillis() - timeStampSensor);
		ILog.d("isCovered=", isCovered, " timePast(sensor)=", timePast);

		WakeLock wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		ProximityStateEvent psEvent = new ProximityStateEvent(isCovered, timeStamp, wakeLock, mHandler);
		mProximityStateListener.onProximityStateChanged(psEvent);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public interface ProximityStateListener {
		public void onProximityStateChanged(ProximityStateEvent event);
	}

	public boolean isEnabled() {
		return mIsEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		if (isEnabled && !mIsEnabled) {
			Sensor proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
		} else if (!isEnabled && mIsEnabled) {
			mSensorManager.unregisterListener(this);
		}
		this.mIsEnabled = isEnabled;
	}
}
