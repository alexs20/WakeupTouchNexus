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
import android.os.Vibrator;

public class VibratorAdapter {
	private Vibrator mVibratorManager;
	private boolean mIsCoverVibrateEnabled = false;
	private int mCoverVibrateDurationMsec = 0;
	private boolean mIsUncoverVibrateEnabled = false;
	private int mUncoverVibrateDurationMsec = 0;

	public VibratorAdapter(Context context) {
		mVibratorManager = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void coverVibrate() {
		if (mIsCoverVibrateEnabled) {
			mVibratorManager.vibrate(mCoverVibrateDurationMsec);
		}
	}

	public void uncoverVibrate() {
		if (mIsUncoverVibrateEnabled) {
			mVibratorManager.vibrate(mUncoverVibrateDurationMsec);
		}
	}

	public int getCoverVibrateDurationMsec() {
		return mCoverVibrateDurationMsec;
	}

	public void setCoverVibrateDurationMsec(int coverVibrateDurationMsec) {
		this.mCoverVibrateDurationMsec = coverVibrateDurationMsec;
	}

	public int getUncoverVibrateDurationMsec() {
		return mUncoverVibrateDurationMsec;
	}

	public void setUncoverVibrateDurationMsec(int uncoverVibrateDurationMsec) {
		this.mUncoverVibrateDurationMsec = uncoverVibrateDurationMsec;
	}

	public boolean isCoverVibrateEnabled() {
		return mIsCoverVibrateEnabled;
	}

	public void setCoverVibrateEnabled(boolean isEnabled) {
		this.mIsCoverVibrateEnabled = isEnabled;
	}

	public boolean isUncoverVibrateEnabled() {
		return mIsUncoverVibrateEnabled;
	}

	public void setUncoverVibrateEnabled(boolean isEnabled) {
		this.mIsUncoverVibrateEnabled = isEnabled;
	}

}
