LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := a05_spatialite
LOCAL_SRC_FILES := a05_spatialite.cpp

include $(BUILD_SHARED_LIBRARY)
