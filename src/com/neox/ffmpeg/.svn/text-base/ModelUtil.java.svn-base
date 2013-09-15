package com.neox.ffmpeg;

import java.io.File;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

public final class ModelUtil {
	private static final String LOG_TAG = ModelUtil.class.getSimpleName();
	
	static final long maxMemory = Runtime.getRuntime().maxMemory();

	public static String getModelName() {
		return Build.MODEL;
	}
	
	public static Boolean isCurrentModel(final String model) {
		Log.d(LOG_TAG, "ModelUtil CurrentModel :" + Build.MODEL);
		return Build.MODEL.equals(model);
	}
	public static String currentModelDownloadDirectoryPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "download";
	}

	public static boolean isSmallMemoryModel() {
		if (maxMemory < (64 * 1024 * 1024))
		{
			//LogUtil.w(LOG_TAG, "maxMemory: " + Util.getFileSizeStr(maxMemory) + "  isSmallMemoryModel: true");
			return true;
		}
		//LogUtil.w(LOG_TAG, "maxMemory: " + Util.getFileSizeStr(maxMemory) + "  isSmallMemoryModel: false");
		return false;
	}
}
