/*
 * com.btb.tcloud.common.util.LogUtil
 *
 * Created on 2010. 10. 4.
 * 
 * Copyright (c) 2002-2010 BTBSolution Co., Ltd. All Rights Reserved.
 */

package com.neox.ffmpeg;

import android.util.Log;

/**
 * 
 * 
 * Create Date		2010. 10. 4.
 * @version			1.00		2010. 10. 4.
 * @since			1.00
 * @see
 * @author			swnam (swnam@btbsolution.co.kr)
 *
 * Revision History
 * who			when				what
 * swnam		2010. 10. 4.			최초 작성
 * swnam		2010. 10. 4.			StringBuffer를 이용하여 약간의 성능 개선
 */

/**
 * 중복 코드가 많지만, 각 method 별로 다르게 출력해야 할지도 모른다는 가정하에 허용함.
 */
public final class LogUtil {
	private static final String ONE_LOG_TAG = "ffmpeg";

	private static void _v(final String tag, final String msg, final Throwable tr) {
		if (CommonSetting.Log.IS_SHOW) {
			String strTag = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strTag = ONE_LOG_TAG;
			}
			else {
				strTag = tag;
			}

			String strVerbose = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strVerbose = "[" + tag + "]";
			}
			else {
				strVerbose = "";
			}
			if (CommonSetting.Log.IS_VERBOSE) {
				final Thread current = Thread.currentThread();
				final long tid = current.getId();
				final StackTraceElement[] stack = current.getStackTrace();
				final String methodName = stack[4].getMethodName();
				// Prepend current thread ID and name of calling method to the
				// message.
				strVerbose += "[" + tid + "] [" + methodName + "] ";
			}

			final StringBuffer b = new StringBuffer();
			b.append(strVerbose);
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.v(strTag, b.toString(), tr);
			}
			else {
				Log.v(strTag, b.toString());
			}
		}
	}

	public static void v(final String tag, final String msg, final Throwable tr) {
		_v(tag, msg, tr);
	}

	public static void v(final String tag, final String msg) {
		_v(tag, msg, null);
	}

	public static void v(final String tag, final Throwable tr) {
		_v(tag, "", tr);
	}

	private static void _d(final String tag, final String msg, final Throwable tr) {
		if (CommonSetting.Log.IS_SHOW) {
			String strTag = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strTag = ONE_LOG_TAG;
			}
			else {
				strTag = tag;
			}

			String strVerbose = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strVerbose = "[" + tag + "]";
			}
			else {
				strVerbose = "";
			}
			if (CommonSetting.Log.IS_VERBOSE) {
				final Thread current = Thread.currentThread();
				final long tid = current.getId();
				final StackTraceElement[] stack = current.getStackTrace();
				final String methodName = stack[4].getMethodName();
				// Prepend current thread ID and name of calling method to the
				// message.
				strVerbose += "[" + tid + "] [" + methodName + "] ";
			}

			final StringBuffer b = new StringBuffer();
			b.append(strVerbose);
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.d(strTag, b.toString(), tr);
			}
			else {
				Log.d(strTag, b.toString());
			}
		}
	}

	public static void d(final String tag, final String msg, final Throwable tr) {
		_d(tag, msg, tr);
	}

	public static void d(final String tag, final String msg) {
		_d(tag, msg, null);
	}

	public static void d(final String tag, final Throwable tr) {
		_d(tag, "", tr);
	}

	private static void _i(final String tag, final String msg, final Throwable tr) {
		if (CommonSetting.Log.IS_SHOW) {
			String strTag = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strTag = ONE_LOG_TAG;
			}
			else {
				strTag = tag;
			}

			String strVerbose = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strVerbose = "[" + tag + "]";
			}
			else {
				strVerbose = "";
			}
			if (CommonSetting.Log.IS_VERBOSE) {
				final Thread current = Thread.currentThread();
				final long tid = current.getId();
				final StackTraceElement[] stack = current.getStackTrace();
				final String methodName = stack[4].getMethodName();
				// Prepend current thread ID and name of calling method to the
				// message.
				strVerbose += "[" + tid + "] [" + methodName + "] ";
			}

			final StringBuffer b = new StringBuffer();
			b.append(strVerbose);
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.i(strTag, b.toString(), tr);
			}
			else {
				Log.i(strTag, b.toString());
			}
		}
		// else {
		// StringBuffer b = new StringBuffer();
		// b.append("[");
		// b.append(msg);
		// b.append("]");
		//			
		// if(tr!=null) {
		// Log.i(tag, b.toString(), tr);
		// }
		// else {
		// Log.i(tag, b.toString());
		// }
		// }
	}

	public static void i(final String tag, final String msg, final Throwable tr) {
		_i(tag, msg, tr);
	}

	public static void i(final String tag, final String msg) {
		_i(tag, msg, null);
	}

	public static void i(final String tag, final Throwable tr) {
		_i(tag, "", tr);
	}

	private static void _w(final String tag, final String msg, final Throwable tr) {
		if (CommonSetting.Log.IS_SHOW) {
			String strTag = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strTag = ONE_LOG_TAG;
			}
			else {
				strTag = tag;
			}

			String strVerbose = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strVerbose = "[" + tag + "]";
			}
			else {
				strVerbose = "";
			}
			if (CommonSetting.Log.IS_VERBOSE) {
				final Thread current = Thread.currentThread();
				final long tid = current.getId();
				final StackTraceElement[] stack = current.getStackTrace();
				final String methodName = stack[4].getMethodName();
				// Prepend current thread ID and name of calling method to the
				// message.
				strVerbose += "[" + tid + "] [" + methodName + "] ";
			}

			final StringBuffer b = new StringBuffer();
			b.append(strVerbose);
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.w(strTag, b.toString(), tr);
			}
			else {
				Log.w(strTag, b.toString());
			}
		}
		else {
			final StringBuffer b = new StringBuffer();
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.w(tag, b.toString(), tr);
			}
			else {
				Log.w(tag, b.toString());
			}
		}
	}

	public static void w(final String tag, final String msg, final Throwable tr) {
		_w(tag, msg, tr);
	}

	public static void w(final String tag, final String msg) {
		_w(tag, msg, null);
	}

	public static void w(final String tag, final Throwable tr) {
		_w(tag, "", tr);
	}

	private static void _e(final String tag, final String msg, final Throwable tr) {
		if (CommonSetting.Log.IS_SHOW) {
			String strTag = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strTag = ONE_LOG_TAG;
			}
			else {
				strTag = tag;
			}

			String strVerbose = null;
			if (CommonSetting.Log.IS_ONE_TAG) {
				strVerbose = "[" + tag + "]";
			}
			else {
				strVerbose = "";
			}
			if (CommonSetting.Log.IS_VERBOSE) {
				final Thread current = Thread.currentThread();
				final long tid = current.getId();
				final StackTraceElement[] stack = current.getStackTrace();
				final String methodName = stack[4].getMethodName();
				// Prepend current thread ID and name of calling method to the
				// message.
				strVerbose += "[" + tid + "] [" + methodName + "] ";
			}

			final StringBuffer b = new StringBuffer();
			b.append(strVerbose);
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.e(strTag, b.toString(), tr);
			}
			else {
				Log.e(strTag, b.toString());
			}
		}
		else {
			final StringBuffer b = new StringBuffer();
			b.append("[");
			b.append(msg);
			b.append("]");

			if (tr != null) {
				Log.e(tag, b.toString(), tr);
			}
			else {
				Log.e(tag, b.toString());
			}
		}
	}

	public static void e(final String tag, final String msg, final Throwable tr) {
		_e(tag, msg, tr);
	}

	public static void e(final String tag, final String msg) {
		_e(tag, msg, null);
	}

	public static void e(final String tag, final Throwable tr) {
		_e(tag, "", tr);
	}

}
