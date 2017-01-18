/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "ledstatus.h"

#include <string>

#include <base/files/file_path.h>
#include <base/format_macros.h>
#include <base/logging.h>
#include <base/strings/stringprintf.h>
#include <base/strings/string_number_conversions.h>
#include <base/strings/string_util.h>
#include <brillo/streams/file_stream.h>
#include <brillo/streams/stream_utils.h>

namespace {

brillo::StreamPtr GetLEDDataStream(size_t index, bool write) {
  CHECK(index < LedStatus::num_leds);
  std::string led_path;
  if( index == 3 ) {
      led_path = "/sys/class/leds/boot/brightness";
  } else {
      led_path = base::StringPrintf("/sys/class/leds/led%" PRIuS "/brightness",
                                    index + 1);
  }
  base::FilePath dev_path{led_path};
  auto access_mode = brillo::stream_utils::MakeAccessMode(!write, write);
  return brillo::FileStream::Open(
      dev_path, access_mode, brillo::FileStream::Disposition::OPEN_EXISTING,
      nullptr);
}

}  // anonymous namespace

LedStatus::LedStatus() {
  moisture = new upm::GroveMoisture(0);
  // Try to open the lights hal.
  int ret = hw_get_module(LIGHTS_HARDWARE_MODULE_ID, &lights_hal_);
  if (ret) {
    LOG(ERROR) << "Failed to load the lights HAL.";
    return;
  }
  CHECK(lights_hal_);
  LOG(INFO) << "Loaded lights HAL.";

  // If we can open the hal, then we map each number from 1 - 4 to one of the
  // leds available on the board. Note, multiple numbers could be mapped to the
  // same led.
  const std::initializer_list<const char*> kLogicalLights = {
    LIGHT_ID_BACKLIGHT, LIGHT_ID_KEYBOARD, LIGHT_ID_BUTTONS, LIGHT_ID_BATTERY,
    LIGHT_ID_NOTIFICATIONS, LIGHT_ID_ATTENTION, LIGHT_ID_BLUETOOTH,
    LIGHT_ID_WIFI};
  size_t led_index = 0;
  for (const char* light_name : kLogicalLights) {
    light_device_t* light_device = nullptr;
    ret = lights_hal_->methods->open(
        lights_hal_, light_name,
        reinterpret_cast<hw_device_t**>(&light_device));
    // If a given light device couldn't be opened, don't map it to a number.
    if (ret || !light_device) {
      continue;
    }
    hal_led_map_.emplace(led_index, light_name);
    led_index++;
    if (led_index == num_leds) {
      // We have already mapped all num_leds LEDs.
      break;
    }
  }

  // If the size of the map is zero, then the lights hal doesn't have any valid
  // leds.
  if (hal_led_map_.empty()) {
    LOG(ERROR) << "Unable to open any light devices using the hal.";
    lights_hal_ = nullptr;
    return;
  }

  // If not all 4 numbers have been mapped, then we map them to the first led
  // mapped.
  for (size_t i = hal_led_map_.size(); i < num_leds; i++) {
    hal_led_map_.emplace(i, hal_led_map_[0]);
  }
  hal_led_status_.resize(num_leds);
}

std::vector<bool> LedStatus::GetStatus() const {
  if (lights_hal_)
    return hal_led_status_;

  std::vector<bool> leds(num_leds);
  for (size_t index = 0; index < num_leds; index++)
    leds[index] = IsLedOn(index);
  return leds;
}

int32_t LedStatus::GetMoistureSensorReading() const {
  int32_t val = moisture->value();
  return val;
}

bool LedStatus::IsLedOn(size_t index) const {
  brillo::StreamPtr stream = GetLEDDataStream(index, false);
  if (!stream)
    return false;

  char buffer[10];
  size_t size_read = 0;
  if (!stream->ReadNonBlocking(buffer, sizeof(buffer), &size_read, nullptr,
                               nullptr)) {
    return false;
  }

  std::string value{buffer, size_read};
  base::TrimWhitespaceASCII(value, base::TrimPositions::TRIM_ALL, &value);
  int brightness = 0;
  if (!base::StringToInt(value, &brightness))
    return false;
  return brightness > 0;
}

void LedStatus::SetLedStatus(size_t index, bool on) {
  if (lights_hal_) {
    light_state_t state = {};
    state.color = on;
    state.flashMode = LIGHT_FLASH_NONE;
    state.flashOnMS = 0;
    state.flashOffMS = 0;
    state.brightnessMode = BRIGHTNESS_MODE_USER;
    light_device_t* light_device = nullptr;
    int rc = lights_hal_->methods->open(
        lights_hal_, hal_led_map_[index].c_str(),
        reinterpret_cast<hw_device_t**>(&light_device));
    if (rc) {
      LOG(ERROR) << "Unable to open " << hal_led_map_[index];
      return;
    }
    CHECK(light_device);
    rc = light_device->set_light(light_device, &state);
    if (rc) {
      LOG(ERROR) << "Unable to set " << hal_led_map_[index];
      return;
    }
    hal_led_status_[index] = on;
    light_device->common.close(
        reinterpret_cast<hw_device_t*>(light_device));
    if (rc) {
      LOG(ERROR) << "Unable to close " << hal_led_map_[index];
      return;
    }
    return;
  }

  brillo::StreamPtr stream = GetLEDDataStream(index, true);
  if (!stream)
    return;

  std::string brightness = on ? "255" : "0";
  stream->WriteAllBlocking(brightness.data(), brightness.size(), nullptr);
}
