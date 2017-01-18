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

#include "dbus_service.h"

DBusService::DBusService(
    brillo::dbus_utils::ExportedObjectManager* object_manager)
    : dbus_object_(object_manager, object_manager->GetBus(),
                   com::android::LEDService::ServiceAdaptor::GetObjectPath()) {
}

void DBusService::Start(brillo::dbus_utils::AsyncEventSequencer* sequencer) {
  dbus_adaptor_.RegisterWithDBusObject(&dbus_object_);
  dbus_object_.RegisterAsync(
      sequencer->GetHandler("DBusService::Start failed", true));
}

void DBusService::SetLED(int32_t led, bool on) {
  leds_.SetLedStatus(led, on);
}

bool DBusService::GetLED(int32_t led) {
  return leds_.IsLedOn(led);
}

std::vector<bool> DBusService::GetAllLEDs() {
  return leds_.GetStatus();
}

int32_t DBusService::GetMoistureReading() {
  return leds_.GetMoistureSensorReading();
}
