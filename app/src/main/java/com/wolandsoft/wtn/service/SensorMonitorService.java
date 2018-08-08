package com.wolandsoft.wtn.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.wolandsoft.wtn.AppConstants;
import com.wolandsoft.wtn.R;
import com.wolandsoft.wtn.activity.ScreenOffActivity;
import com.wolandsoft.wtn.adapter.CoverStateAdapter;
import com.wolandsoft.wtn.adapter.CoverStateAdapter.CoverageStateAdapterListener;
import com.wolandsoft.wtn.adapter.LedNotifyAdapter;
import com.wolandsoft.wtn.adapter.ProximityStateAdapter;
import com.wolandsoft.wtn.adapter.ScreenWakeupAdapter;
import com.wolandsoft.wtn.adapter.VibratorAdapter;
import com.wolandsoft.wtn.filter.CallStateFilter;
import com.wolandsoft.wtn.filter.HeadsetStateFilter;
import com.wolandsoft.wtn.filter.OrientationFilter;
import com.wolandsoft.wtn.filter.ProximityNoiseFilter;
import com.wolandsoft.wtn.filter.ScreenStateFilter;
import com.wolandsoft.wtn.pref.AppPreferenceActivity;
import com.wolandsoft.wtn.utils.ILog;

public class SensorMonitorService extends Service implements CoverageStateAdapterListener, AppConstants {
	private final int SERVICE_NOTIFICATION_ID = 1;
	private final String NOTIFICATION_CHANNEL_ID = "WTL";
	private final String NOTIFICATION_CHANNEL_NAME = "WTL Notification Channel";

	private SharedPreferences mSharedPref;

	private Handler mHandler;
	private ProximityStateAdapter mProximityStateAdapter;
	private OrientationFilter mOrientationFilter;
	private ProximityNoiseFilter mProximityNoiseFilter;
	private ScreenStateFilter mScreenStateFilter;
	private CallStateFilter mCallStateFilter;
	private HeadsetStateFilter mHeadsetStateFilter;
	private CoverStateAdapter mCoverageStateAdapter;
	private VibratorAdapter mVibratorAdapter;
	private LedNotifyAdapter mLedNotifyAdapter;
	private ScreenWakeupAdapter mScreenWakeupAdapter;
	private BroadcastReceiver mScreenOnOffReceiver;

	private boolean mIsKeepSensorActive = true;
	private boolean mIsForeground = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mHandler = new Handler();
		mSharedPref = getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS);

		mVibratorAdapter = new VibratorAdapter(this);
		mLedNotifyAdapter = new LedNotifyAdapter(this);
		mScreenWakeupAdapter = new ScreenWakeupAdapter(this, mHandler);

		mCoverageStateAdapter = new CoverStateAdapter(this, mHandler);

		mProximityNoiseFilter = new ProximityNoiseFilter(mCoverageStateAdapter, mHandler);
		mOrientationFilter = new OrientationFilter(mProximityNoiseFilter, this);
		mHeadsetStateFilter = new HeadsetStateFilter(mOrientationFilter, this);
		mCallStateFilter = new CallStateFilter(mHeadsetStateFilter, this);
		mScreenStateFilter = new ScreenStateFilter(mCallStateFilter, this);
		mScreenStateFilter.setEnabled(true);
		mProximityStateAdapter = new ProximityStateAdapter(mScreenStateFilter, this, mHandler);

		mScreenOnOffReceiver = new BroadcastReceiver() {

			// When Event is published, onReceive method is called
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!mIsKeepSensorActive) {
					if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
						if (mProximityStateAdapter.isEnabled()) {
							ILog.d("De-registering proximity sensor");
							mProximityStateAdapter.setEnabled(false);
						}
					} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
						if (!mProximityStateAdapter.isEnabled()) {
							ILog.d("Registering proximity sensor");
							mProximityStateAdapter.setEnabled(true);
						}
					}
				}
			}
		};

		registerReceiver(mScreenOnOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(mScreenOnOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mSharedPref = getSharedPreferences(TAG, Context.MODE_MULTI_PROCESS);
		boolean isServiceEnabled = getBool(R.string.pref_service_enabled_key, R.bool.pref_service_enabled_value);
		boolean isBackgroundModeEnabled = getBool(R.string.pref_run_service_background_key, R.bool.pref_run_service_background_value);

		if (!isServiceEnabled) {
			stopSelf();
		} else {
			if (mIsForeground && isBackgroundModeEnabled) {
				stopForeground(true);
				mIsForeground = false;
			} else if (!isBackgroundModeEnabled) {
				startForeground(SERVICE_NOTIFICATION_ID, buildStatusbarNotification());
				mIsForeground = true;
			}
			applySettings();

		}

		return START_STICKY;
	}

	private void applySettings() {
		int coverDelayMsec = getInt(R.string.pref_sensor_cover_delay_msec_key, R.integer.pref_sensor_cover_delay_msec_value);
		int uncoverDelayMsec = getInt(R.string.pref_sensor_uncover_delay_msec_key, R.integer.pref_sensor_uncover_delay_msec_value);
		mProximityNoiseFilter.setCoverDelayMsec(coverDelayMsec);
		mProximityNoiseFilter.setUncoverDelayMsec(uncoverDelayMsec);

		boolean isSuspendOnCall = getBool(R.string.pref_suspend_on_calling_key, R.bool.pref_suspend_on_calling_value);
		mCallStateFilter.setEnabled(isSuspendOnCall);

		boolean isSuspendOnHeadset = getBool(R.string.pref_suspend_on_headset_key, R.bool.pref_suspend_on_headset_value);
		mHeadsetStateFilter.setEnabled(isSuspendOnHeadset);

		boolean isAngleControlEnabled = getBool(R.string.pref_gvector_enabled_key, R.bool.pref_gvector_enabled_value);
		mOrientationFilter.setEnabled(isAngleControlEnabled);

		int backwardAngle = getInt(R.string.pref_gvector_side_view_backward_angle_key, R.integer.pref_gvector_side_view_backward_angle_value);
		int forwardAngle = getInt(R.string.pref_gvector_side_view_forward_angle_key, R.integer.pref_gvector_side_view_forward_angle_value);
		mOrientationFilter.setMaxBackwardAngle(backwardAngle);
		mOrientationFilter.setMaxForwardAngle(forwardAngle);

		int leftAngle = getInt(R.string.pref_gvector_bottom_view_left_angle_key, R.integer.pref_gvector_bottom_view_left_angle_value);
		int rightAngle = getInt(R.string.pref_gvector_bottom_view_right_angle_key, R.integer.pref_gvector_bottom_view_right_angle_value);
		mOrientationFilter.setMaxLeftAngle(leftAngle);
		mOrientationFilter.setMaxRightAngle(rightAngle);

		int windowMsec = getInt(R.string.pref_sensor_acceptance_window_msec_key, R.integer.pref_sensor_acceptance_window_msec_value);
		windowMsec += uncoverDelayMsec;
		mCoverageStateAdapter.setCoverTimeoutMsec(windowMsec);

		boolean isCoverVibrateEnabled = getBool(R.string.pref_sensor_cover_vibrate_enable_key, R.bool.pref_sensor_cover_vibrate_enable_value);
		int coverVibrateMsec = getInt(R.string.pref_sensor_cover_vibrate_msec_key, R.integer.pref_sensor_cover_vibrate_msec_value);
		mVibratorAdapter.setCoverVibrateEnabled(isCoverVibrateEnabled);
		mVibratorAdapter.setCoverVibrateDurationMsec(coverVibrateMsec);

		boolean isUncoverVibrateEnabled = getBool(R.string.pref_sensor_uncover_vibrate_enable_key, R.bool.pref_sensor_uncover_vibrate_enable_value);
		int uncoverVibrateMsec = getInt(R.string.pref_sensor_uncover_vibrate_msec_key, R.integer.pref_sensor_uncover_vibrate_msec_value);
		mVibratorAdapter.setUncoverVibrateEnabled(isUncoverVibrateEnabled);
		mVibratorAdapter.setUncoverVibrateDurationMsec(uncoverVibrateMsec);

		boolean isLEDEnabled = getBool(R.string.pref_sensor_acceptance_window_led_enable_key, R.bool.pref_sensor_acceptance_window_led_enable_value);
		int yellowLevel = getInt(R.string.pref_sensor_acceptance_window_led_yellow_level_key, R.integer.pref_sensor_acceptance_window_led_yellow_level_value);
		int redLevel = getInt(R.string.pref_sensor_acceptance_window_led_red_level_key, R.integer.pref_sensor_acceptance_window_led_red_level_value);
		mLedNotifyAdapter.setEnabled(isLEDEnabled);
		mLedNotifyAdapter.setYellowLevel(yellowLevel);
		mLedNotifyAdapter.setRedLevel(redLevel);

		int wakelockSec = getInt(R.string.pref_wakelock_hold_sec_key, R.integer.pref_wakelock_hold_sec_value);
		mScreenWakeupAdapter.setKeepScreenOnMsec(wakelockSec * 1000);

		mIsKeepSensorActive = getBool(R.string.pref_sensor_keep_ready_key, R.bool.pref_sensor_keep_ready_value);
		mProximityStateAdapter.setEnabled(true);
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		unregisterReceiver(mScreenOnOffReceiver);
		mProximityStateAdapter.setEnabled(false);
		mOrientationFilter.setEnabled(false);
		mLedNotifyAdapter.reset();
		super.onDestroy();
	}

	@Override
	public void onCover() {
		mLedNotifyAdapter.lightup();
		mVibratorAdapter.coverVibrate();
	}

	@SuppressLint("Wakelock")
	@Override
	public void onUncover() {
		mLedNotifyAdapter.reset();
		mScreenWakeupAdapter.wakeup();
		mVibratorAdapter.uncoverVibrate();
	}

	@Override
	public void onTimeout() {
		mLedNotifyAdapter.reset();
	}

	private Notification buildStatusbarNotification() {
		String key = getString(R.string.pref_notification_action_key);
		String defVal = getString(R.string.pref_notification_action_value);
		String notifActivityIdx = mSharedPref.getString(key, defVal);
		Class<?> activity = AppPreferenceActivity.class;
		int iconResId = R.drawable.ic_launcher_config;
		String intentDesc = getString(R.string.activity_config_label);
		boolean isTurnOff = !defVal.equals(notifActivityIdx);
		if (isTurnOff) {
			activity = ScreenOffActivity.class;
			iconResId = R.drawable.ic_launcher_screen_off;
			intentDesc = getString(R.string.activity_screen_off_label);
		}
		Intent showIntent = new Intent(this, activity);
		showIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, showIntent, 0);
		// Notification.Builder builder = new Notification.Builder(this);
		Notification.Builder builder;
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
					isTurnOff ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(notificationChannel);
			builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
		} else {
			builder = new Notification.Builder(this);
		}
		builder.setContentTitle(getText(R.string.app_name));
		builder.setContentText(intentDesc);
		builder.setSmallIcon(R.drawable.ic_notif);
		Bitmap icon = BitmapFactory.decodeResource(getResources(), iconResId);
		builder.setLargeIcon(icon);
		builder.setContentIntent(contentIntent);
		builder.setWhen(0);
		builder.setOngoing(true);
		try {
			Method setPriority = Notification.Builder.class.getMethod("setPriority", int.class);
			Field prioriyHigh = Notification.class.getField(isTurnOff ? "PRIORITY_MAX": "PRIORITY_MIN");
			setPriority.invoke(builder, prioriyHigh.getInt(null));
		} catch (Exception e) {
		}
		@SuppressWarnings("deprecation")
		Notification notif = builder.getNotification();
		return notif;
	}

	private int getInt(int key, int def) {
		return mSharedPref.getInt(getString(key), getResources().getInteger(def));
	}

	private boolean getBool(int key, int def) {
		return mSharedPref.getBoolean(getString(key), getResources().getBoolean(def));
	}

}
