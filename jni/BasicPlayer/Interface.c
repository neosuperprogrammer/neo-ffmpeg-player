/*
 * jni for Android
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#include <jni.h>
#include <android/bitmap.h>
#include <cpu-features.h>
#include "BasicPlayer.h"
#include "BasicPlayerJni.h"
#include "mySDL.h"

JNIEnv *g_Env = NULL;
jobject g_thiz = NULL;
JNIEnv *g_AudioEnv = NULL;
jobject g_Audiothiz = NULL;
JNIEnv *g_VideoEnv = NULL;
jobject g_Videothiz = NULL;


JNIEXPORT jint JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniOpenVideo(JNIEnv *env, jobject thiz, jstring jFilePath)
{
	int result;
	const char *filePath;

	g_Env = env;
	g_thiz = thiz;
	

//	if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM && (android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0) {
	if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM) {
		LOGE("jniOpenVideo() is called!!!");
		filePath = (*env)->GetStringUTFChars(env, jFilePath, NULL);

		result = openVideo(filePath);

		(*env)->ReleaseStringUTFChars(env, jFilePath, filePath);
		
		
		return result;
	}
	else {
		LOG(ANDROID_LOG_DEBUG, LOG_TAG, "family not match!!!");	
		return -1;
	}
}

JNIEXPORT void JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniCloseVideo(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, "MoviePlayView closeMovie()");	
	closeVideo();
}

JNIEXPORT void JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniClose(JNIEnv *env, jobject thiz)
{
	LOG(ANDROID_LOG_DEBUG, LOG_TAG, "MoviePlayView close()");	
	exitFFmpeg();
}

JNIEXPORT void JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniDecode(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	LOGE("jniDecode() is called!!!");
	decode();
}


JNIEXPORT jint JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniGetVideoWidth(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	return getWidth();
}

JNIEXPORT jint JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniGetVideoHeight(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	return getHeight();
}

JNIEXPORT jint JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniVideoThread(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;
	videoThread();
}

JNIEXPORT jint JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniAudioThread(JNIEnv *env, jobject thiz)
{
//	g_Env = env;
//	g_thiz = thiz;
	g_AudioEnv = env;
	g_Audiothiz = thiz;
	audioThread();
}

JNIEXPORT void JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniRefreshVideo(JNIEnv *env, jobject thiz, jobject bitmap)
{
//	g_Env = env;
//	g_thiz = thiz;
	g_VideoEnv = env;
	g_Videothiz = thiz;

	video_refresh_timer(bitmap);
}

JNIEXPORT void JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniStreamSeek(JNIEnv *env, jobject thiz, jint inc)
{
	g_Env = env;
	g_thiz = thiz;

	streamSeek(inc);
}

JNIEXPORT void JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniStreamAbsSeek(JNIEnv *env, jobject thiz, jint pos)
{
	g_Env = env;
	g_thiz = thiz;

	streamAbsSeek(pos);
}


JNIEXPORT int JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniGetDuration(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;

	return getDuration();
}

JNIEXPORT int JNICALL Java_com_neox_ffmpeg_FFmpegCodec_jniGetCurrentTime(JNIEnv *env, jobject thiz)
{
	g_Env = env;
	g_thiz = thiz;

	return getCurrentTime();
}


