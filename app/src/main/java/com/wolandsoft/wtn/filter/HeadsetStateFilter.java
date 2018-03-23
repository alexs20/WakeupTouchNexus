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

import android.content.Context;
import android.media.AudioManager;

import com.wolandsoft.wtn.adapter.ProximityStateAdapter.ProximityStateListener;
import com.wolandsoft.wtn.adapter.ProximityStateEvent;
import com.wolandsoft.wtn.utils.ILog;

public class HeadsetStateFilter implements ProximityStateListener {
	private ProximityStateListener mListener;
	private AudioManager mAudioManager;
	private boolean mIsEnabled = false;
	private boolean mIsEventAccepted = false;

	public HeadsetStateFilter(ProximityStateListener listener, Context context) {
		mListener = listener;
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onProximityStateChanged(ProximityStateEvent event) {
		int timePast = (int) (System.currentTimeMillis() - event.getTimeStamp());
		ILog.d("isCovered=", event.isCovered(), " timePast=", timePast);

		if (!mIsEnabled) {
			mIsEventAccepted = true;
		} else {
			if (event.isCovered()) {
				mIsEventAccepted = !mAudioManager.isWiredHeadsetOn();
			}
		}
		if (mIsEventAccepted) {
			mListener.onProximityStateChanged(event);
		} else {
			event.releaseWakeLock();
		}
	}

	public boolean isEnabled() {
		return mIsEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.mIsEnabled = isEnabled;
	}

}
