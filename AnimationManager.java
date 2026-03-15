package com.akiisx.luckyblock.config;

import com.akiisx.luckyblock.LuckyBlock;
import com.akiisx.luckyblock.data.LuckyBlockType;
import com.akiisx.luckyblock.data.ParticleConfig;
import com.akiisx.luckyblock.data.Reward;
import com.akiisx.luckyblock.data.SoundConfig;
import com.akiisx.luckyblock.util.ItemBuilder;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuckyBlockManager {
    private final LuckyBlock plugin;
    private final Map<String, LuckyBlockType> luckyBlocks;
    private final File luckyBlocksFolder;
    
    public LuckyBlockManager(LuckyBlock plugin) {
        this.plugin = plugin;
        this.luckyBlocks = new HashMap<>();
        this.luckyBlocksFolder = new File(plugin.getDataFolder(), "LuckyBlocks");
        
        if (!luckyBlocksFolder.exists()) {
            luckyBlocksFolder.mkdirs();
            saveDefaultLuckyBlock();
        }
        
        loadAllLuckyBlocks();
    }
    
    public void reload() {
        this.luckyBlocks.clear();
        loadAllLuckyBlocks();
    }
    
    private void saveDefaultLuckyBlock() {
        File exampleFile = new File(luckyBlocksFolder, "example.yml");
        if (!exampleFile.exists()) {
            try (InputStream in = plugin.getResource("LuckyBlocks/example.yml")) {
                if (in != null) {
                    Files.copy(in, exampleFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save example.yml: " + e.getMessage());
            }
        }
    }
    
    private void loadAllLuckyBlocks() {
        File[] files = luckyBlocksFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        
        for (File file : files) {
            loadLuckyBlock(file);
        }
        
        plugin.getLogger().info("Loaded " + luckyBlocks.size() + " Lucky Block type(s)");
    }
    
    private void loadLuckyBlock(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        String id = file.getName().replace(".yml", "");
        String name = config.getString("Name", id);
        
        LuckyBlockType type = new LuckyBlockType();
        type.setId(id);
        type.setName(name);
        
        String displayName = plugin.getConfigManager().getDisplayName();
        String skullOwner = plugin.getConfigManager().getSkullOwner();
        List<String> lore = plugin.getConfigManager().getLore();
        
        type.setDisplayName(displayName);
        type.setSkullOwner(skullOwner);
        type.setLore(lore != null ? lore : new ArrayList<>());
        
        List<Reward> rewards = new ArrayList<>();
        ConfigurationSection rewardsSection = config.getConfigurationSection("Rewards");
        if (rewardsSection != null) {
            for (String key : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(key);
                if (rewardSection != null) {
                    Reward reward = new Reward();
                    reward.setId(key);
                    reward.setChance(rewardSection.getDouble("Chance", 0.0));
                    reward.setCommands(rewardSection.getStringList("Commands"));
                    reward.setBroadcast(rewardSection.getString("Broadcast", ""));
                    reward.setRewardName(rewardSection.getString("RewardName", key));
                    rewards.add(reward);
                }
            }
        }
        type.setRewards(rewards);
        
        type.setParticles(plugin.getConfigManager().getParticleConfig());
        type.setSounds(plugin.getConfigManager().getSoundConfig());
        
        ItemStack item = createLuckyBlockItem(type);
        type.setItemStack(item);
        
        this.luckyBlocks.put(id, type);
    }
    
    private ItemStack createLuckyBlockItem(LuckyBlockType type) {
        ItemBuilder builder = new ItemBuilder(XMaterial.PLAYER_HEAD);
        builder.setDisplayName(type.getDisplayName());
        builder.setLore(type.getLore());
        builder.setSkullTexture(type.getSkullOwner());
        builder.addNBTTag("luckyblock_type", type.getId());
        return builder.build();
    }
    
    public LuckyBlockType getLuckyBlockType(String id) {
        return this.luckyBlocks.get(id);
    }
    
    public Map<String, LuckyBlockType> getAllLuckyBlocks() {
        return new HashMap<>(this.luckyBlocks);
    }
    
    public ItemStack getLuckyBlockItem(String id, int amount) {
        LuckyBlockType type = getLuckyBlockType(id);
        if (type == null) return null;
        
        ItemStack item = type.getItemStack().clone();
        item.setAmount(amount);
        return item;
    }
    
    public boolean isLuckyBlock(ItemStack item) {
        if (item == null) return false;
        ItemBuilder builder = new ItemBuilder(item);
        return builder.hasNBTTag("luckyblock_type");
    }
    
    public String getLuckyBlockTypeFromItem(ItemStack item) {
        if (!isLuckyBlock(item)) return null;
        ItemBuilder builder = new ItemBuilder(item);
        return builder.getNBTTag("luckyblock_type");
    }
}