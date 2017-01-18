# Copyright 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := ledservice
LOCAL_INIT_RC := ledservice.rc

LOCAL_SRC_FILES := \
	dbus_bindings/dbus-service-config.json \
	dbus_bindings/com.android.LEDService.Service.dbus-xml \
	dbus_service.cpp \
	ledservice.cpp \
	ledstatus.cpp \

LOCAL_SHARED_LIBRARIES := \
	libbrillo \
	libbrillo-dbus \
	libbrillo-stream \
	libchrome \
	libchrome-dbus \
	libdbus \
	libhardware \
	libutils \
    libc \
    libbase \
    libmraa \
    libupm

LOCAL_C_INCLUDES := external/gtest/include
LOCAL_CFLAGS := -Wall -Werror -Wno-sign-promo -Wno-missing-field-initializers
LOCAL_RTTI_FLAG := -frtti

include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_MODULE := libledservice-client
LOCAL_DBUS_PROXY_PREFIX := ledservice

LOCAL_SRC_FILES := \
	dbus_bindings/dbus-service-config.json \
	dbus_bindings/com.android.LEDService.Service.dbus-xml \

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := com.android.LEDService.conf
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/dbus-1
LOCAL_SRC_FILES := etc/dbus-1/com.android.LEDService.conf
include $(BUILD_PREBUILT)
