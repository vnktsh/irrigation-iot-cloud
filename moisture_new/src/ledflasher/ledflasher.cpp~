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

#include <string>
#include <sysexits.h>

#include <base/bind.h>
#include <base/command_line.h>
#include <base/macros.h>
#include <brillo/daemons/dbus_daemon.h>
#include <brillo/syslog_logging.h>
#include <libweaved/device.h>

#include "animation.h"
#include "ledservice/dbus-proxies.h"

using com::android::LEDService::ServiceProxy;

class Daemon final : public brillo::DBusDaemon {
 public:
  Daemon() = default;

 protected:
  int OnInit() override;

 private:
  void OnLEDServiceConnected(ServiceProxy* service);
  void OnLEDServiceDisconnected(const dbus::ObjectPath& object_path);

  // Particular command handlers for various commands.
  void OnSet(const std::weak_ptr<weaved::Command>& cmd);
  void OnToggle(const std::weak_ptr<weaved::Command>& cmd);
  void OnAnimate(const std::weak_ptr<weaved::Command>& cmd);

  // Helper methods to propagate device state changes to Buffet and hence to
  // the cloud server or local clients.
  void UpdateDeviceState();

  std::unique_ptr<weaved::Device> device_;

  // LEDService's D-Bus object manager.
  std::unique_ptr<com::android::LEDService::ObjectManagerProxy> led_object_mgr_;

  // Device state variables.
  std::string status_{"idle"};

  // LED service interface.
  ServiceProxy* led_service_{nullptr};

  // Current animation;
  std::unique_ptr<Animation> animation_;

  DISALLOW_COPY_AND_ASSIGN(Daemon);
};

int Daemon::OnInit() {
  int return_code = brillo::DBusDaemon::OnInit();
  if (return_code != EX_OK)
    return return_code;

  device_ = weaved::Device::CreateInstance(
      bus_, base::Bind(&Daemon::UpdateDeviceState, base::Unretained(this)));
  device_->AddCommandHandler(
      "_ledflasher._set",
      base::Bind(&Daemon::OnSet, base::Unretained(this)));
  device_->AddCommandHandler(
      "_ledflasher._toggle",
      base::Bind(&Daemon::OnToggle, base::Unretained(this)));
  device_->AddCommandHandler(
      "_ledflasher._animate",
      base::Bind(&Daemon::OnAnimate, base::Unretained(this)));

  led_object_mgr_.reset(new com::android::LEDService::ObjectManagerProxy{bus_});
  led_object_mgr_->SetServiceAddedCallback(
      base::Bind(&Daemon::OnLEDServiceConnected, base::Unretained(this)));
  led_object_mgr_->SetServiceRemovedCallback(
      base::Bind(&Daemon::OnLEDServiceDisconnected, base::Unretained(this)));

  LOG(INFO) << "Waiting for commands...";
  return EX_OK;
}

void Daemon::OnLEDServiceConnected(ServiceProxy* service) {
  led_service_ = service;
  UpdateDeviceState();
}

void Daemon::OnLEDServiceDisconnected(const dbus::ObjectPath& object_path) {
  animation_.reset();
  led_service_ = nullptr;
}

void Daemon::OnSet(const std::weak_ptr<weaved::Command>& cmd) {
  auto command = cmd.lock();
  if (!command)
    return;

  if (!led_service_) {
    CHECK(command->Abort("system_error", "ledservice unavailable", nullptr));
    return;
  }

  int index = command->GetParameter<int>("_led");
  CHECK_GT(index, 0);
  bool on = command->GetParameter<bool>("_on");
  brillo::ErrorPtr error;
  if (!led_service_->SetLED(index - 1, on, &error)) {
    CHECK(command->Abort(error->GetCode(), error->GetMessage(), nullptr));
    return;
  }
  animation_.reset();
  status_ = "idle";
  UpdateDeviceState();
  CHECK(command->Complete({}, nullptr));
}

void Daemon::OnToggle(const std::weak_ptr<weaved::Command>& cmd) {
  auto command = cmd.lock();
  if (!command)
    return;

  if (!led_service_) {
    CHECK(command->Abort("system_error", "ledservice unavailable", nullptr));
    return;
  }

  int index = command->GetParameter<int>("_led");
  CHECK_GT(index, 0);
  index--;
  bool on = false;
  brillo::ErrorPtr error;
  if(!led_service_->GetLED(index, &on, &error) ||
     !led_service_->SetLED(index, !on, &error)) {
    CHECK(command->Abort(error->GetCode(), error->GetMessage(), nullptr));
    return;
  }
  animation_.reset();
  status_ = "idle";
  UpdateDeviceState();
  CHECK(command->Complete({}, nullptr));
}

void Daemon::OnAnimate(const std::weak_ptr<weaved::Command>& cmd) {
  auto command = cmd.lock();
  if (!command)
    return;

  if (!led_service_) {
    CHECK(command->Abort("system_error", "ledservice unavailable", nullptr));
    return;
  }

  double duration = command->GetParameter<double>("_duration");
  std::string type = command->GetParameter<std::string>("_type");
  animation_ = Animation::Create(led_service_, type,
                                 base::TimeDelta::FromSecondsD(duration));
  if (animation_) {
    status_ = "animating";
    animation_->Start();
  } else {
    status_ = "idle";
  }
  UpdateDeviceState();
  CHECK(command->Complete({}, nullptr));
}

void Daemon::UpdateDeviceState() {
  if (!led_service_)
    return;

  std::vector<bool> leds;
  if (!led_service_->GetAllLEDs(&leds, nullptr))
    return;

  brillo::VariantDictionary state_change{
    {"_ledflasher._status", status_},
    {"_ledflasher._leds", leds},
  };
  // TODO: Come up with a design for ledflasher.cpp such that this call never
  // fails.
  device_->SetStateProperties(state_change, nullptr);
}

int main(int argc, char* argv[]) {
  base::CommandLine::Init(argc, argv);
  brillo::InitLog(brillo::kLogToSyslog | brillo::kLogHeader);
  Daemon daemon;
  return daemon.Run();
}
