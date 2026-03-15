package com.akiisx.luckyblock.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticleConfig {
    private boolean enabled;
    private String effect;
    private int amount;
    private double offsetX;
    private double offsetY;
    private double offsetZ;
}