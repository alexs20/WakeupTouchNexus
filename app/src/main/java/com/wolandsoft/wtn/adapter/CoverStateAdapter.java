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

import android.os.Handler;

import com.wolandsoft.wtn.adapter.ProximityStateAdapter.ProximityStateListener;
import com.wolandsoft.wtn.utils.ILog;
import com.wolandsoft.wtn.utils.SafeRunnable;

public class CoverStateAdapter implements ProximityStateListener {
	private CoverageStateAdapterListener mListener;
	private Handler mHandler;
	private TimeoutTask mScheduledTask;
	private int mCoverTimeoutMsec = 0;

	public CoverStateAdapter(CoverageStateAdapterListener listener, Handler handler) {
		mListener = listener;
		mHandler = handler;
	}

	@Override
	public void onProximityStateChanged(final ProximityStateEvent event) {

		int timePast = (int) (System.currentTimeMillis() - event.getTimeStamp());
		ILog.d("isCovered=", event.isCovered(), " timePast=", timePast);

		if (event.isCovered()) {
			mScheduledTask = new TimeoutTask(event);
			mHandler.postDelayed(mScheduledTask, mCoverTimeoutMsec);
			mListener.onCover();
		} else {
			if (mScheduledTask != null) {
				mHandler.removeCallbacks(mScheduledTask);
				mScheduledTask.release();
				mScheduledTask = null;
				mListener.onUncover();
			}
			event.releaseWakeLock();
		}
	}

	public int getCoverTimeoutMsec() {
		return mCoverTimeoutMsec;
	}

	public void setCoverTimeoutMsec(int timeout) {
		this.mCoverTimeoutMsec = timeout;
	}

	private class TimeoutTask extends SafeRunnable {
		ProximityStateEvent lmEvent;

		public TimeoutTask(ProximityStateEvent event) {
			lmEvent = event;
		}

		public void release() {
			lmEvent.releaseWakeLock();
		}

		@Override
		public void runSafe() {
			mScheduledTask = null;
			mListener.onTimeout();
			release();
		}
	}

	public interface CoverageStateAdapterListener {

		public void onCover();

		public void onUncover();

		public void onTimeout();
	}
}
