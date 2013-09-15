LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libbasicplayer
LOCAL_SRC_FILES := BasicPlayer/BasicPlayer.c BasicPlayer/Interface.c BasicPlayer/mySDL.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/BasicPlayer/ffmpeg/ \
                    $(LOCAL_PATH)/BasicPlayer/ffmpeg/libavcodec \
                    $(LOCAL_PATH)/BasicPlayer/ffmpeg/libavformat \
                    $(LOCAL_PATH)/BasicPlayer/ffmpeg/libswscale
					

					
LOCAL_CFLAGS := $(COMMON_CFLAGS)
					
LOCAL_STATIC_LIBRARIES := cpufeatures

LOCAL_LDLIBS := -lz -llog -ljnigraphics -lavutil -lavfilter -lswscale -lavformat -lavcodec -lavutil

LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
