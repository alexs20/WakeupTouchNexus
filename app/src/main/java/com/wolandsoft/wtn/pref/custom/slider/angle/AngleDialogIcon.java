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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.wolandsoft.wtn.pref.custom.slider.SliderPreference.SliderIcon;

public abstract class AngleDialogIcon extends View implements SliderIcon {

	private final int PIXEL_SIZE = 2;

	private int mAngleFrom;
	private int mAngleTo;
	private Bitmap mIconBmp;
	private Paint mPaint;
	private Bitmap mTransformedBmp;
	private Paint mAcceptedAreaBorder;
	private Paint mAcceptedAreaFill;
	private Paint mUnacceptedAreaFillAndBorder;
	private RectF mRectF;
	private boolean mIsMirrored = false;

	public AngleDialogIcon(Context context) {
		super(context);

		mIconBmp = BitmapFactory.decodeResource(context.getResources(), getIconDrawableId());
		setLayoutParams(new LayoutParams(mIconBmp.getWidth(), mIconBmp.getHeight()));

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setDither(true);

		mAcceptedAreaBorder = new Paint();
		mAcceptedAreaBorder.setAntiAlias(true);
		mAcceptedAreaBorder.setStyle(Paint.Style.STROKE);
		mAcceptedAreaBorder.setStrokeWidth(PIXEL_SIZE);
		mAcceptedAreaBorder.setColor(Color.GRAY);

		mAcceptedAreaFill = new Paint();
		mAcceptedAreaFill.setAntiAlias(true);
		mAcceptedAreaFill.setStyle(Paint.Style.FILL);
		mAcceptedAreaFill.setStrokeWidth(PIXEL_SIZE);
		mAcceptedAreaFill.setColor(Color.GREEN);

		mUnacceptedAreaFillAndBorder = new Paint();
		mUnacceptedAreaFillAndBorder.setAntiAlias(true);
		mUnacceptedAreaFillAndBorder.setStyle(Paint.Style.FILL_AND_STROKE);
		mUnacceptedAreaFillAndBorder.setStrokeWidth(PIXEL_SIZE);
		mUnacceptedAreaFillAndBorder.setColor(Color.LTGRAY);

		mRectF = new RectF(PIXEL_SIZE, PIXEL_SIZE, mIconBmp.getWidth() - PIXEL_SIZE * 2, mIconBmp.getHeight() - PIXEL_SIZE * 2);
	}

	public abstract int getIconDrawableId();

	protected void setIconAngle(int angle) {
		Matrix matrix = new Matrix();
		matrix.setRotate(angle);
		mTransformedBmp = Bitmap.createBitmap(mIconBmp, 0, 0, mIconBmp.getWidth(), mIconBmp.getHeight(), matrix, true);
		int xSizeChange = mTransformedBmp.getWidth() - mIconBmp.getWidth();
		int ySizeChange = mTransformedBmp.getHeight() - mIconBmp.getHeight();
		mTransformedBmp = Bitmap.createBitmap(mTransformedBmp, xSizeChange / 2, ySizeChange / 2, mIconBmp.getWidth(), mIconBmp.getHeight(), null, true);
	}

	protected void setArcAngleFrom(int angle) {
		mAngleFrom = angle;
	}

	protected void setArcAngleTo(int angle) {
		mAngleTo = angle;
	}

	public void setMirrored(boolean isMirrored) {
		this.mIsMirrored = isMirrored;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), mRectF.height() / 2 - PIXEL_SIZE * 2, mUnacceptedAreaFillAndBorder);

		int angle = mAngleTo - mAngleFrom;
		int shift = mAngleFrom - 90;
		canvas.drawArc(mRectF, shift, angle, true, mAcceptedAreaFill);
		canvas.drawArc(mRectF, shift, angle, true, mAcceptedAreaBorder);
		if (mIsMirrored) {
			shift = mAngleFrom - 270;
			canvas.drawArc(mRectF, shift, angle, true, mAcceptedAreaFill);
			canvas.drawArc(mRectF, shift, angle, true, mAcceptedAreaBorder);
		}

		canvas.drawBitmap(mTransformedBmp, 0, 0, mPaint);
	}

}
