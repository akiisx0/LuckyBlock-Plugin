package com.akiisx.luckyblock.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@Setter
public class LuckyBlockType {
    private String id;
    private String name;
    private String displayName;
    private String skullOwner;
    private String mode;
    private List<String> lore;
    private List<Reward> rewards;
    private ParticleConfig particles;
    private SoundConfig sounds;
    private ItemStack itemStack;
}