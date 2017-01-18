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

#include "animation.h"
#include "animation_blink.h"
#include "animation_marquee.h"

#include <base/bind.h>
#include <base/message_loop/message_loop.h>

Animation::Animation(com::android::LEDService::ServiceProxy* service_proxy,
                     const base::TimeDelta& step_duration)
  : service_proxy_{service_proxy}, step_duration_{step_duration} {
}

void Animation::Start() {
  DoAnimationStep();
  base::MessageLoop::current()->PostDelayedTask(
      FROM_HERE,
      base::Bind(&Animation::Start, weak_ptr_factory_.GetWeakPtr()),
      step_duration_);
}

void Animation::Stop() {
  weak_ptr_factory_.InvalidateWeakPtrs();
}

bool Animation::GetLED(size_t index) const {
  bool on = false;
  service_proxy_->GetLED(index, &on, nullptr);
  return on;
}

void Animation::SetLED(size_t index, bool on) {
  service_proxy_->SetLED(index, on, nullptr);
}

void Animation::SetAllLEDs(bool on) {
  for (size_t i = 0; i < num_leds; i++)
    SetLED(i, on);
}

std::unique_ptr<Animation> Animation::Create(
    com::android::LEDService::ServiceProxy* service_proxy,
    const std::string& type,
    const base::TimeDelta& duration) {
  std::unique_ptr<Animation> animation;
  if (type == "blink") {
    animation.reset(new AnimationBlink{service_proxy, duration});
  } else if (type == "marquee_left") {
    animation.reset(new AnimationMarquee{service_proxy, duration,
                                         AnimationMarquee::Direction::Left});
  } else if (type == "marquee_right") {
    animation.reset(new AnimationMarquee{service_proxy, duration,
                                         AnimationMarquee::Direction::Right});
  }
  return animation;
}
