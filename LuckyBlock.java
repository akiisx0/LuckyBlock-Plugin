package com.akiisx.luckyblock.config;

import com.akiisx.luckyblock.LuckyBlock;
import com.akiisx.luckyblock.data.AnimationConfig;
import com.akiisx.luckyblock.data.ParticleConfig;
import com.akiisx.luckyblock.data.SoundConfig;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ConfigManager {
    private final LuckyBlock plugin;
    private FileConfiguration config;
    
    private String prefix;
    private String displayName;
    private String skullOwner;
    private String mode;
    private List<String> lore;
    private List<String> disabledWorlds;
    private boolean disableAllBuiltInSurprises;
    private boolean disableAllCustomSurprises;
    
    private boolean permissionEnabled;
    private String permission;
    private boolean preventBreak;
    private boolean permissionMessageEnabled;
    private String permissionMessage;
    
    private ParticleConfig particleConfig;
    private SoundConfig soundConfig;
    private AnimationConfig animationConfig;
    
    private Map<XMaterial, Double> miningChances;
    
    public ConfigManager(LuckyBlock plugin) {
        this.plugin = plugin;
        this.miningChances = new HashMap<>();
        reload();
    }
    
    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        loadSettings();
        loadPermissions();
        loadParticles();
        loadSounds();
        loadAnimation();
        loadMiningChances();
    }
    
    private void loadSettings() {
        ConfigurationSection settings = config.getConfigurationSection("Settings");
        if (settings != null) {
            this.prefix = settings.getString("Prefix", "&e&lLB &7» ");
            this.displayName = settings.getString("DisplayName", "&e&lLucky Block");
            this.skullOwner = settings.getString("SkullOwner", "");
            this.mode = settings.getString("Mode", "break");
            this.lore = settings.getStringList("Lore");
            this.disabledWorlds = settings.getStringList("DisabledWorlds");
            this.disableAllBuiltInSurprises = settings.getBoolean("DisableAllBuiltInSurprises", true);
            this.disableAllCustomSurprises = settings.getBoolean("DisableAllCustomSurprises", false);
        }
    }
    
    private void loadPermissions() {
        ConfigurationSection perm = config.getConfigurationSection("Settings.Permission");
        if (perm != null) {
            this.permissionEnabled = perm.getBoolean("Enabled", false);
            this.permission = perm.getString("Permission", "superluckyblock.luckyblock.default");
            this.preventBreak = perm.getBoolean("PreventBreak", false);
            
            ConfigurationSection msg = perm.getConfigurationSection("Message");
            if (msg != null) {
                this.permissionMessageEnabled = msg.getBoolean("Enabled", true);
                this.permissionMessage = msg.getString("Message", "You cannot open this lucky block.");
            }
        }
    }
    
    private void loadParticles() {
        ConfigurationSection particles = config.getConfigurationSection("Settings.Particles");
        if (particles != null) {
            this.particleConfig = new ParticleConfig();
            this.particleConfig.setEnabled(particles.getBoolean("Enabled", true));
            this.particleConfig.setEffect(particles.getString("Effect", "FLAME"));
            this.particleConfig.setAmount(particles.getInt("Amount", 150));
            
            ConfigurationSection offset = particles.getConfigurationSection("Offset");
            if (offset != null) {
                this.particleConfig.setOffsetX(offset.getDouble("X", 0.5));
                this.particleConfig.setOffsetY(offset.getDouble("Y", 0.25));
                this.particleConfig.setOffsetZ(offset.getDouble("Z", 0.5));
            }
        }
    }
    
    private void loadSounds() {
        ConfigurationSection sounds = config.getConfigurationSection("Settings.Sounds");
        if (sounds != null) {
            this.soundConfig = new SoundConfig();
            this.soundConfig.setEnabled(sounds.getBoolean("Enabled", true));
            this.soundConfig.setSound(sounds.getString("Sound", "BLOCK_NOTE_BLOCK_PLING"));
            this.soundConfig.setVolume((float) sounds.getDouble("Volume", 1.0));
            this.soundConfig.setPitch((float) sounds.getDouble("Pitch", 1.0));
        }
    }
    
    private void loadAnimation() {
        ConfigurationSection anim = config.getConfigurationSection("Animation");
        if (anim != null) {
            this.animationConfig = new AnimationConfig();
            this.animationConfig.setEnabled(anim.getBoolean("Enabled", true));
            this.animationConfig.setDurationTicks(anim.getInt("DurationTicks", 40));
            this.animationConfig.setFreezePlayer(anim.getBoolean("FreezePlayer", false));
            
            ConfigurationSection animParticles = anim.getConfigurationSection("Particles");
            if (animParticles != null) {
                ParticleConfig pc = new ParticleConfig();
                pc.setEnabled(animParticles.getBoolean("Enabled", true));
                pc.setEffect(animParticles.getString("Effect", "FLAME"));
                pc.setAmount(animParticles.getInt("Amount", 100));
                pc.setOffsetX(0.5);
                pc.setOffsetY(0.5);
                pc.setOffsetZ(0.5);
                this.animationConfig.setParticles(pc);
            }
            
            ConfigurationSection animSounds = anim.getConfigurationSection("Sounds");
            if (animSounds != null) {
                SoundConfig sc = new SoundConfig();
                sc.setEnabled(animSounds.getBoolean("Enabled", true));
                sc.setSound(animSounds.getString("Sound", "ENTITY_PLAYER_LEVELUP"));
                sc.setVolume((float) animSounds.getDouble("Volume", 1.0));
                sc.setPitch((float) animSounds.getDouble("Pitch", 1.0));
                this.animationConfig.setSounds(sc);
            }
        }
    }
    
    private void loadMiningChances() {
        this.miningChances.clear();
        List<String> chances = config.getStringList("Chances");
        for (String entry : chances) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                XMaterial.matchXMaterial(parts[0]).ifPresent(material -> {
                    try {
                        double chance = Double.parseDouble(parts[1]);
                        this.miningChances.put(material, chance);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid chance value for " + parts[0] + ": " + parts[1]);
                    }
                });
            }
        }
    }
    
    public double getMiningChance(XMaterial material) {
        return this.miningChances.getOrDefault(material, 0.0);
    }
}