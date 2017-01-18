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

#ifndef LEDFLASHER_SRC_LEDSERVICE_LEDSTATUS_H_
#define LEDFLASHER_SRC_LEDSERVICE_LEDSTATUS_H_

#include <map>
#include <vector>

#include <base/macros.h>
#include <hardware/lights.h>
#include <unistd.h>
#include "grovemoisture.h"

class LedStatus final {
 public:
  LedStatus();

  std::vector<bool> GetStatus() const;
  int32_t GetMoistureSensorReading() const;
  bool IsLedOn(size_t index) const;
  void SetLedStatus(size_t index, bool on);

  static const size_t num_leds = 4;

 private:
  upm::GroveMoisture* moisture;
  const hw_module_t* lights_hal_{nullptr};
  // Maps the lights available to the hal to numbers 1 - 4. It is possible for
  // multiple numbers to be mapped to the same led on the board.
  std::map<size_t, std::string> hal_led_map_;
  // Since the hal doesn't have a way to track the led status, we maintain that
  // info here.
  std::vector<bool> hal_led_status_;

  DISALLOW_COPY_AND_ASSIGN(LedStatus);
};

#endif  // LEDFLASHER_SRC_LEDSERVICE_LEDSTATUS_H_
