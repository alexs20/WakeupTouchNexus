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
package com.wolandsoft.wtn.pref.custom.slider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wolandsoft.wtn.AppConstants;
import com.wolandsoft.wtn.R;

/**
 * A {@link Preference} that allows for string input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText attributes
 * on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes}.
 */
public class SliderPreference extends DialogPreference implements AppConstants {

	private final int DEFAULT_MIN_VALUE = 0;
	private final int DEFAULT_MAX_VALUE = 100;
	private int mValue;
	private int mDialogValue;
	private int mFromValue;
	private int mToValue;
	private String mFromValueKey;
	private String mToValueKey;
	private String mMessage;
	private Drawable mIcon;
	private Constructor<?> mIconView;
	private Context mContext;

	/**
	 * @param context
	 * @param attrs
	 */
	public SliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SliderPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		mContext = context;
		setDialogLayoutResource(R.layout.custom_preference_slider_dialog);
		TypedArray res = getContext().obtainStyledAttributes(attrs, R.styleable.SliderPreference);
		mFromValue = res.getInt(R.styleable.SliderPreference_sliderValueFrom, DEFAULT_MIN_VALUE);
		mToValue = res.getInt(R.styleable.SliderPreference_sliderValueTo, DEFAULT_MAX_VALUE);
		mFromValueKey = res.getString(R.styleable.SliderPreference_sliderValueFromByKey);
		mToValueKey = res.getString(R.styleable.SliderPreference_sliderValueToByKey);

		mMessage = res.getString(R.styleable.SliderPreference_sliderDialogMessage);
		mIcon = res.getDrawable(R.styleable.SliderPreference_sliderDialogIconDrawable);
		if (mIcon == null) {
			String className = res.getString(R.styleable.SliderPreference_sliderDialogIconView);
			if (className != null) {
				try {
					Class<?> iconViewClass = Class.forName(className);
					if (View.class.isAssignableFrom(iconViewClass) && SliderIcon.class.isAssignableFrom(iconViewClass)) {
						mIconView = iconViewClass.getConstructor(Context.class);
					}
				} catch (ClassNotFoundException e) {
				} catch (NoSuchMethodException e) {
				}
			}
		}
		res.recycle();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		String defValue = a.getString(index);
		return Integer.valueOf(defValue);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setValue(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
	}

	public int getValue() {
		return mValue;
	}

	public void setValue(int value) {
		if (shouldPersist()) {
			persistInt(value);
		}
		if (value != mValue) {
			mValue = value;
			notifyChanged();
		}
	}

	@Override
	protected View onCreateDialogView() {
		View view = super.onCreateDialogView();

		if (mFromValueKey != null || mToValueKey != null) {
			SharedPreferences pref = mContext.getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS);
			if (mFromValueKey != null) {
				mFromValue = pref.getInt(mFromValueKey, DEFAULT_MIN_VALUE);
			}
			if (mToValueKey != null) {
				mToValue = pref.getInt(mToValueKey, DEFAULT_MAX_VALUE);
			}
		}

		View customIconView = null;
		if (mIcon != null) {
			LinearLayout imageHolder = (LinearLayout) view.findViewById(R.id.image_holder);
			ImageView imgView = new ImageView(getContext());
			imgView.setImageDrawable(mIcon);
			imageHolder.addView(imgView);
		} else if (mIconView != null) {
			try {
				customIconView = (View) mIconView.newInstance(getContext());
				LinearLayout imageHolder = (LinearLayout) view.findViewById(R.id.image_holder);
				imageHolder.addView(customIconView);
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (InvocationTargetException e) {
			}
		}
		final SliderIcon valueIconView = (SliderIcon) customIconView;
		if (valueIconView != null) {
			valueIconView.setFromValue(mFromValue);
			valueIconView.setToValue(mToValue);
			valueIconView.setValue(mValue);
		}

		final TextView message = (TextView) view.findViewById(R.id.message);
		String msg = String.format(mMessage, mValue);
		message.setText(msg);

		SeekBar slider = (SeekBar) view.findViewById(R.id.slider);
		slider.setMax(Math.abs(mToValue - mFromValue));
		slider.setProgress(Math.abs(mValue - mFromValue));
		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mDialogValue = mFromValue + progress * (mFromValue <= mToValue ? 1 : -1);
					String msg = String.format(mMessage, mDialogValue);
					message.setText(msg);
					if (valueIconView != null) {
						valueIconView.setValue(mDialogValue);
					}
				}
			}
		});
		mDialogValue = mValue;
		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult && callChangeListener(mDialogValue)) {
			setValue(mDialogValue);
		}
		super.onDialogClosed(positiveResult);
	}

	public interface SliderIcon {

		public void setValue(int value);

		public void setFromValue(int value);

		public void setToValue(int value);

	}

}
