package com.akiisx.luckyblock.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnimationConfig {
    private boolean enabled;
    private int durationTicks;
    private boolean freezePlayer;
    private ParticleConfig particles;
    private SoundConfig sounds;
}