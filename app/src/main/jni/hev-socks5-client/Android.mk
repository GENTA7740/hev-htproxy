# Copyright (C) 2017 The Android Open Source Project
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
#

TOP_PATH := $(call my-dir)

LOCAL_PATH = $(TOP_PATH)
include $(CLEAR_VARS)
LOCAL_MODULE    := hev-socks5-client
LOCAL_SRC_FILES := libs/$(TARGET_ARCH_ABI)/libhev-socks5-client.so
include $(PREBUILT_SHARED_LIBRARY)

