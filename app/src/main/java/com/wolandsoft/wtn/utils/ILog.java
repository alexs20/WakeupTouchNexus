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
package com.wolandsoft.wtn.utils;

import android.util.Log;

import com.wolandsoft.wtn.BuildConfig;

public class ILog {
	public static final String PKG = BuildConfig.class.getPackage().getName();
	public static final String TAG = PKG.substring(PKG.lastIndexOf(".") + 1);
	public static final boolean IS_DEBUG = BuildConfig.DEBUG;
	public static final boolean IS_LOG_SRC = true;

	public static void d(Object... args) {
		if (IS_DEBUG) {
			StringBuilder sb = getStringBuilderWithHeader();
			for (int i = 0; i < args.length; i++) {
				if (i == args.length - 1 && args[i] instanceof Throwable) {
					Log.d(TAG, sb.toString(), (Throwable) args[i]);
					return;
				}
				sb.append(args[i]);
			}
			Log.d(TAG, sb.toString());
		}
	}

	public static void w(Object... args) {
		StringBuilder sb = getStringBuilderWithHeader();
		for (int i = 0; i < args.length; i++) {
			if (i == args.length - 1 && args[i] instanceof Throwable) {
				Log.w(TAG, sb.toString(), (Throwable) args[i]);
				return;
			}
			sb.append(args[i]);
		}
		Log.w(TAG, sb.toString());
	}

	public static void e(Object... args) {
		StringBuilder sb = getStringBuilderWithHeader();
		for (int i = 0; i < args.length; i++) {
			if (i == args.length - 1 && args[i] instanceof Throwable) {
				Log.e(TAG, sb.toString(), (Throwable) args[i]);
				return;
			}
			sb.append(args[i]);
		}
		Log.e(TAG, sb.toString());
	}

	private static StringBuilder getStringBuilderWithHeader() {
		StringBuilder sb = new StringBuilder();
		if (IS_LOG_SRC) {
			Throwable th = new Throwable();
			StackTraceElement ste = th.getStackTrace()[2];
			String clsName = ste.getClassName();
			sb.append(clsName.substring(PKG.length())).append(".").append(ste.getMethodName()).append("(").append(ste.getFileName()).append(":")
					.append(ste.getLineNumber()).append(")").append("\n");
		}
		return sb;
	}
}
