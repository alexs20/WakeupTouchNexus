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
package com.wolandsoft.wtn.pref.custom.slider.angle;

import android.content.Context;

import com.wolandsoft.wtn.R;

public class SideViewForwardDialogIcon extends AngleDialogIcon {

	private int mAngleFrom;

	public SideViewForwardDialogIcon(Context context) {
		super(context);
	}

	public int getIconDrawableId() {
		return R.drawable.ic_device_side_view;
	}

	@Override
	public void setValue(int value) {
		setIconAngle(value);
		setArcAngleFrom(mAngleFrom);
		setArcAngleTo(value);
		invalidate();
	}

	@Override
	public void setFromValue(int value) {
		mAngleFrom = value;
	}

	@Override
	public void setToValue(int value) {

	}

}
