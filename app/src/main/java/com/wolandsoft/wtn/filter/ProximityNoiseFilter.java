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

import android.os.Handler;

import com.wolandsoft.wtn.adapter.ProximityStateAdapter.ProximityStateListener;
import com.wolandsoft.wtn.adapter.ProximityStateEvent;
import com.wolandsoft.wtn.utils.ILog;
import com.wolandsoft.wtn.utils.SafeRunnable;

public class ProximityNoiseFilter implements ProximityStateListener {
	private ProximityStateListener mListener;
	private Handler mHandler;
	private DelayedTask mScheduledTask;
	private int mCoverDelayMsec = 0;
	private int mUncoverDelayMsec = 0;

	// private Object mLock = new Object();

	public ProximityNoiseFilter(ProximityStateListener listener, Handler handler) {
		mListener = listener;
		mHandler = handler;
	}

	@Override
	public void onProximityStateChanged(final ProximityStateEvent event) {
		int timePast = (int) (System.currentTimeMillis() - event.getTimeStamp());
		int delayTime = (event.isCovered() ? mCoverDelayMsec : mUncoverDelayMsec) - timePast;
		if (delayTime < 0) {
			delayTime = 0;
		}
		ILog.d("isCovered=", event.isCovered(), " timePast=", timePast, " delayTime=", delayTime);

		boolean isPassThrough = true;
		if (mScheduledTask != null) {
			mHandler.removeCallbacks(mScheduledTask);
			mScheduledTask.release();
			mScheduledTask = null;
			isPassThrough = false;
			ILog.d("Delayed event canceled");
		}
		if (isPassThrough) {
			if (delayTime > 0) {
				mScheduledTask = new DelayedTask(event);
				mHandler.postDelayed(mScheduledTask, delayTime);
			} else {
				mListener.onProximityStateChanged(event);
			}
		}
	}

	private class DelayedTask extends SafeRunnable {
		ProximityStateEvent lmEvent;

		public DelayedTask(ProximityStateEvent event) {
			lmEvent = event;
		}

		public void release() {
			lmEvent.releaseWakeLock();
		}

		@Override
		public void runSafe() {
			mListener.onProximityStateChanged(lmEvent);
			mScheduledTask = null;
		}
	}

	public int getCoverDelayMsec() {
		return mCoverDelayMsec;
	}

	public void setCoverDelayMsec(int coverDelayMsec) {
		this.mCoverDelayMsec = coverDelayMsec;
	}

	public int getUncoverDelayMsec() {
		return mUncoverDelayMsec;
	}

	public void setUncoverDelayMsec(int uncoverDelayMsec) {
		this.mUncoverDelayMsec = uncoverDelayMsec;
	}

}
