#ifndef _SVA_JNI_H
#define _SVA_JNI_H

#include <android/log.h>

#define _ANDROID_LOG_	// 디버그 로그를 찍는다.

#define LOG_TAG	"ffmpeg-jni" // 로그 출력을 위한 태그

#ifdef _ANDROID_LOG_
	#define LOG		__android_log_write
	#define PRINT	__android_log_print

	#define LOGD(...) {__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);}
	#define LOGI(...) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
	#define LOGW(...) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}
	#define LOGE(...) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}	
#else 
	#define LOG
	#define PRINT

	#define LOGD(...) {}
	#define LOGI(...) {}
	#define LOGW(...) {}
	#define LOGE(...) {}	
#endif

#endif // _SVA_JNI_H
