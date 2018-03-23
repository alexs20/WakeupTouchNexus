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
package com.wolandsoft.wtn.filter;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.wolandsoft.wtn.adapter.ProximityStateAdapter.ProximityStateListener;
import com.wolandsoft.wtn.adapter.ProximityStateEvent;
import com.wolandsoft.wtn.utils.ILog;

public class OrientationFilter implements ProximityStateListener {
	private final double PI_OF_180 = Math.PI / 180;

	private ProximityStateListener mListener;
	private SensorManager mSensorManager;
	private SensorEventListener mGravityListener;
	private Queue<ProximityStateEvent> mStoredEvents;

	private boolean mIsEnabled = false;
	private int mMaxBackwardAngle = -180;
	private int mMaxForwardAngle = 180;
	private int mMaxLeftAngle = -180;
	private int mMaxRightAngle = 180;

	private boolean mIsEventAccepted = false;
	private final int X_CELL = 0;
	private final int Y_CELL = 1;
	private final int Z_CELL = 2;

	public OrientationFilter(ProximityStateListener listener, Context context) {
		mListener = listener;
		mStoredEvents = new LinkedList<ProximityStateEvent>();
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		mGravityListener = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				calculatePosition(event);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		};
	}

	@Override
	public void onProximityStateChanged(ProximityStateEvent event) {
		int timePast = (int) (System.currentTimeMillis() - event.getTimeStamp());
		ILog.d("isCovered=", event.isCovered(), " timePast=", timePast);

		if (!mIsEnabled) {
			mListener.onProximityStateChanged(event);
			return;
		}

		if (mStoredEvents.isEmpty()) {
			if (event.isCovered()) {
				mStoredEvents.add(event);
				Sensor gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(mGravityListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
			} else if (mIsEventAccepted) {
				mListener.onProximityStateChanged(event);
			} else {
				event.releaseWakeLock();
			}
		} else {
			mStoredEvents.add(event);
		}
	}

	private void calculatePosition(SensorEvent event) {
		if (!mStoredEvents.isEmpty()) {
			mSensorManager.unregisterListener(mGravityListener);

			double rawX = event.values[X_CELL];
			double rawY = event.values[Y_CELL];
			double rawZ = event.values[Z_CELL];

			int aX = (int) (Math.atan2(rawX, rawY) / PI_OF_180 * (-1));
			int aY = (int) (Math.atan2(rawZ, rawY) / PI_OF_180 * (-1));
			int aZ = (int) (Math.atan2(rawX, rawZ) / PI_OF_180 * (-1));

			int fbAngle = aY;
			int lrAngle = aX;
			if ((aY < -45 && aY > -135) || (aY > 45 && aY < 135)) {
				lrAngle = aZ;
			}

			if (lrAngle > 90) {
				lrAngle -= -180;
			} else if (lrAngle < -90) {
				lrAngle += 180;
			}

			boolean isBfMatch = isAngleBetween(fbAngle, mMaxBackwardAngle, mMaxForwardAngle);
			boolean isLrMatch = isAngleBetween(lrAngle, mMaxLeftAngle, mMaxRightAngle);
			mIsEventAccepted = isBfMatch && isLrMatch;
			while (!mStoredEvents.isEmpty()) {
				ProximityStateEvent psEvent = mStoredEvents.poll();
				if (mIsEventAccepted) {
					mListener.onProximityStateChanged(psEvent);
				} else {
					psEvent.releaseWakeLock();
				}
			}
		}
	}

	// private void onAccelerometerSensorChangedOld(SensorEvent event) {
	//
	// OrientationAngles angles = getAnglesA(event);
	// if (IS_LOG) {
	// PLog.d(LTAG, "old alg x=" + angles.x + " y=" + angles.y + " z=" +
	// angles.z);
	// }
	//
	// if (mProbeIdx < MAX_PROBES) {
	// if (IS_LOG) {
	// PLog.d(LTAG, "mProbeIdxOld=" + mProbeIdx);
	// }
	// mAnglesOld[mProbeIdx][X_CELL] = angles.x;
	// mAnglesOld[mProbeIdx][Y_CELL] = angles.y;
	// mAnglesOld[mProbeIdx][Z_CELL] = angles.z;
	// mProbeIdxOld++;
	// if (mProbeIdxOld == MAX_PROBES) {
	// int distance = Integer.MAX_VALUE;
	// int bestAngleIndex = 0;
	// for (int i = 0; i < MAX_PROBES; i++) {
	// int j = i + 1;
	// if (j == MAX_PROBES) {
	// j = 0;
	// }
	// int localDistance = Math.abs(mAnglesOld[i][X_CELL] -
	// mAnglesOld[j][X_CELL]) + Math.abs(mAnglesOld[i][Y_CELL] -
	// mAnglesOld[j][Y_CELL])
	// + Math.abs(mAnglesOld[i][Z_CELL] - mAnglesOld[j][Z_CELL]);
	// if (distance > localDistance) {
	// distance = localDistance;
	// bestAngleIndex = i;
	// }
	// }
	// if (IS_LOG) {
	// PLog.d(LTAG, "bestAngleIndexOld=" + bestAngleIndex);
	// }
	// int angleX = mAnglesOld[bestAngleIndex][X_CELL];
	// int angleY = mAnglesOld[bestAngleIndex][Y_CELL];
	// int angleZ = mAnglesOld[bestAngleIndex][Z_CELL];
	//
	// int fbAngle = angleY;
	// int lrAngle = angleX;
	// if ((angleY < -45 && angleY > -135) || (angleY > 45 && angleY < 135)) {
	// lrAngle = angleZ;
	// }
	//
	// if (lrAngle > 90) {
	// lrAngle -= -180;
	// } else if (lrAngle < -90) {
	// lrAngle += 180;
	// }
	//
	// boolean isBfMatch = isAngleBetween(fbAngle, mMaxBackwardAngle,
	// mMaxForwardAngle);
	// if (IS_LOG) {
	// PLog.d(LTAG, "isBfMatchOld=" + isBfMatch + " fbAngleOld=" + fbAngle +
	// " mMaxBackwardAngle=" + mMaxBackwardAngle + " mMaxForwardAngle="
	// + mMaxForwardAngle);
	// }
	// boolean isLrMatch = isAngleBetween(lrAngle, mMaxLeftAngle,
	// mMaxRightAngle);
	// if (IS_LOG) {
	// PLog.d(LTAG, "isLrMatchOld=" + isLrMatch + " lrAngleOld=" + lrAngle +
	// " mMaxLeftAngle=" + mMaxLeftAngle + " mMaxRightAngle=" + mMaxRightAngle);
	// }
	// boolean isEventAcceptedOld = isBfMatch && isLrMatch;
	// if (IS_LOG) {
	// PLog.d(LTAG, "isEventAcceptedOld=" + isEventAcceptedOld);
	// }
	// }
	// }
	// }

	// private OrientationAngles getAnglesB(SensorEvent event) {
	// OrientationAngles ret = new OrientationAngles();
	// double rawX = event.values[X_CELL];
	// double rawY = event.values[Y_CELL];
	// double rawZ = event.values[Z_CELL];
	//
	// ret.x = (int) (Math.atan2(rawX, rawY) / PI_OF_180 * (-1));
	// ret.y = (int) (Math.atan2(rawZ, rawY) / PI_OF_180 * (-1));
	// ret.z = (int) (Math.atan2(rawX, rawZ) / PI_OF_180 * (-1));
	//
	// return ret;
	// }

	// private OrientationAngles getAnglesA(SensorEvent event) {
	// OrientationAngles ret = new OrientationAngles();
	// double rawX = event.values[X_CELL];
	// double rawY = event.values[Y_CELL];
	// double rawZ = event.values[Z_CELL];
	//
	// double mag = Math.sqrt(rawX * rawX + rawY * rawY + rawZ * rawZ);
	// double nX = rawX / mag;
	// double nY = rawY / mag;
	// double nZ = rawZ / mag;
	// double vm = Math.sqrt(nX * nX + nY * nY + nZ * nZ);
	// double av = 180.0 / Math.PI;
	//
	// double cos = nX / vm;
	// int x = (int) Math.round(Math.acos(cos) * av);
	// ret.x = x;
	//
	// cos = nY / vm;
	// int y = (int) Math.round(Math.acos(cos) * av);
	// ret.y = y;
	//
	// cos = nZ / vm;
	// int z = (int) Math.round(Math.acos(cos) * av);
	// ret.z = z;
	//
	// if (z > 0 && z < 90) {
	// ret.y = y * -1;
	// }
	//
	// if (y > 0 && y < 90) {
	// ret.x = x - 90;
	// } else {
	// if (x > 90) {
	// ret.x = 270 - x;
	// } else {
	// ret.x = (x + 90) * -1;
	// }
	// }
	//
	// if (x > 0 && x < 90) {
	// ret.z = z * -1;
	// }
	// return ret;
	// }

	private boolean isAngleBetween(int val, int limit1, int limit2) {
		boolean isPass = false;
		if (limit1 < limit2) {
			isPass = (limit1 < val && val < limit2);
		} else {
			isPass = (limit2 < val && val < limit1);
		}
		return isPass;
	}

	public boolean isEnabled() {
		return mIsEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.mIsEnabled = isEnabled;
	}

	public int getMaxBackwardAngle() {
		return mMaxBackwardAngle;
	}

	public void setMaxBackwardAngle(int maxBackwardAngle) {
		this.mMaxBackwardAngle = maxBackwardAngle;
	}

	public int getMaxForwardAngle() {
		return mMaxForwardAngle;
	}

	public void setMaxForwardAngle(int maxForwardAngle) {
		this.mMaxForwardAngle = maxForwardAngle;
	}

	public int getMaxLeftAngle() {
		return mMaxLeftAngle;
	}

	public void setMaxLeftAngle(int maxLeftAngle) {
		mMaxLeftAngle = maxLeftAngle;
	}

	public int getMaxRightAngle() {
		return mMaxRightAngle;
	}

	public void setMaxRightAngle(int maxRightAngle) {
		mMaxRightAngle = maxRightAngle;
	}
}
