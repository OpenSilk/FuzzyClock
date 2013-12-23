LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME  := FuzzyClock
# We have a nonstandard directory stucture
LOCAL_SRC_FILES     := $(call all-java-files-under, src/main/java)
LOCAL_SRC_FILES     += $(call all-java-files-under, src/tapas/java)
LOCAL_MANIFEST_FILE := src/main/AndroidManifest.xml
# Include an extra overlay to rename the app
LOCAL_RESOURCE_DIR  := $(LOCAL_PATH)/src/tapas/res $(LOCAL_PATH)/src/main/res
# Rename package so we can also install market version
LOCAL_MANIFEST_PACKAGE_NAME := com.evervolv.fuzzyclock
LOCAL_SDK_VERSION   := 19
LOCAL_CERTIFICATE   := shared

include $(BUILD_PACKAGE)
