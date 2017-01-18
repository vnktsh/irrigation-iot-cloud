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

#include "dbus_service.h"

using brillo::dbus_utils::AsyncEventSequencer;

namespace {

const char kServiceName[] = "com.android.LEDService";
const char kRootServicePath[] = "/com/android/LEDService";

}  // anonymous namespace

class Daemon final : public brillo::DBusServiceDaemon {
 public:
  Daemon() : DBusServiceDaemon(kServiceName, kRootServicePath) {}

 protected:
  void RegisterDBusObjectsAsync(AsyncEventSequencer* sequencer) override {
    led_service_.reset(new DBusService{object_manager_.get()});
    led_service_->Start(sequencer);
  }

 private:
  std::unique_ptr<DBusService> led_service_;

  DISALLOW_COPY_AND_ASSIGN(Daemon);
};

int main(int argc, char* argv[]) {
  base::CommandLine::Init(argc, argv);
  brillo::InitLog(brillo::kLogToSyslog | brillo::kLogHeader);
  Daemon daemon;
  return daemon.Run();
}
