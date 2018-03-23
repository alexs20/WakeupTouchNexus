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
import android.os.PowerManager.WakeLock;

public class ProximityStateEvent {

	private boolean mIsCovered;
	private long mTimeStamp;
	private WakeLock mWakeLock;
	private Handler mHandler;
	
	private long WAKE_LOCK_TIMEOUT = 3000;
    private final Runnable mReleaser = new Runnable() {
        public void run() {
            releaseWakeLock();
        }
    };

	public ProximityStateEvent(boolean isCovered, long timeStamp, WakeLock wakeLock, Handler handler) {
		super();
		this.mIsCovered = isCovered;
		this.mTimeStamp = timeStamp;
		this.mWakeLock = wakeLock;
		this.mHandler = handler;
		mWakeLock.acquire();
		mHandler.postDelayed(mReleaser, WAKE_LOCK_TIMEOUT);
	}

	public boolean isCovered() {
		return mIsCovered;
	}

	public long getTimeStamp() {
		return mTimeStamp;
	}

	public synchronized void releaseWakeLock(){
		if(mWakeLock != null){
			mHandler.removeCallbacks(mReleaser);
			mWakeLock.release();
			mWakeLock = null;
		}
	}
}
