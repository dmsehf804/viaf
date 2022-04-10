LOCAL_PATH := $(call my-dir)

# Prebuilt 된 FFMpeg Shared 라이브러리 빌드.
include $(CLEAR_VARS)
LOCAL_MODULE := avcodec
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libavcodec.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avdevice
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libavdevice.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avfilter
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libavfilter.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avformat
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libavformat.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avutil
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libavutil.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swresample
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libswresample.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swscale
LOCAL_SRC_FILES := ffmpeg-lib/$(TARGET_ARCH_ABI)/lib/libswscale.so
LOCAL_EXPORT_C_INCLUDES := ffmpeg-lib/$(TARGET_ARCH_ABI)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := x264v157
LOCAL_SRC_FILES := x264-lib/$(TARGET_ARCH_ABI)/libx264v157.so
LOCAL_EXPORT_C_INCLUDES := x264-lib/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := soundtouch
LOCAL_SRC_FILES := soundtouch-lib/$(TARGET_ARCH_ABI)/libsoundtouch.so
LOCAL_EXPORT_C_INCLUDES := soundtouch-lib/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := people
LOCAL_SRC_FILES := soundtouch-lib/$(TARGET_ARCH_ABI)/libpeople.so
include $(PREBUILT_SHARED_LIBRARY)


$(info LOCAL_PATH : $(LOCAL_PATH))
$(info TARGET_ARCH_ABI : $(TARGET_ARCH_ABI))

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libjacksplayer_jni

# Andorid 어플리케이션에서 참조하기 위해 레퍼클래스 및 Player 관련소스 지정.
LOCAL_SRC_FILES := \
    $(LOCAL_PATH)/media-src/ffplayer.c \
    $(LOCAL_PATH)/media-src/ffrender.c \
    $(LOCAL_PATH)/media-src/pktqueue.c \
    $(LOCAL_PATH)/media-src/snapshot.c \
    $(LOCAL_PATH)/media-src/recorder.c \
    $(LOCAL_PATH)/media-src/adev-cmn.c \
    $(LOCAL_PATH)/media-src/vdev-cmn.c \
    $(LOCAL_PATH)/media-src/adev-android.cpp \
    $(LOCAL_PATH)/media-src/vdev-android.cpp \
    jacksplayer_jni.cpp

#LOCAL_C_INCLUDES += $(LOCAL_PATH)/soundtouch-lib/include $(LOCAL_PATH)/soundtouch-lib/SoundStretch

LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/ffmpeg-lib/include \
    $(LOCAL_PATH)/soundtouch-lib/include \
    $(LOCAL_PATH)/media-src/include


LOCAL_CFLAGS   += -DANDROID -DNDEBUG -D__STDC_CONSTANT_MACROS -Os -mfpu=neon-vfpv4 -mfloat-abi=softfp
LOCAL_CXXFLAGS += -DHAVE_PTHREADS
LOCAL_LDLIBS   += -lz -llog -landroid

# 위에서 빌드한 라이브러리 설정.
LOCAL_SHARED_LIBRARIES += people soundtouch avformat avcodec avdevice avfilter swresample swscale avutil x264v157

LOCAL_MULTILIB := 32

include $(BUILD_SHARED_LIBRARY)
