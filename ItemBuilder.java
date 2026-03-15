package com.akiisx.luckyblock.command;

import com.akiisx.luckyblock.LuckyBlock;
import com.akiisx.luckyblock.data.LuckyBlockType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LuckyBlockCommand implements CommandExecutor, TabCompleter {
    private final LuckyBlock plugin;
    
    public LuckyBlockCommand(LuckyBlock plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGive(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("luckyblock.give")) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.no-permission"));
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("commands.usage-give"));
            return true;
        }
        
        String typeId = args[1];
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.amount-must-be-positive"));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.invalid-amount", "%amount%", args[2]));
            return true;
        }
        
        String playerName = args[3];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.player-not-found", "%player%", playerName));
            return true;
        }
        
        LuckyBlockType type = plugin.getLuckyBlockManager().getLuckyBlockType(typeId);
        if (type == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.type-not-found", "%type%", typeId));
            return true;
        }
        
        ItemStack luckyBlock = plugin.getLuckyBlockManager().getLuckyBlockItem(typeId, amount);
        if (luckyBlock == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.creation-failed"));
            return true;
        }
        
        target.getInventory().addItem(luckyBlock);
        sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("admin.give-sender", 
            "%amount%", String.valueOf(amount), 
            "%type%", typeId, 
            "%player%", target.getName()));
        target.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("admin.give-receiver", 
            "%amount%", String.valueOf(amount)));
        
        plugin.getDataLogger().logAdminGive(
            sender.getName(),
            target.getName(),
            typeId,
            amount
        );
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("luckyblock.reload")) {
            sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("errors.no-permission"));
            return true;
        }
        
        plugin.reload();
        sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("commands.reload-success"));
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.help-header"));
        sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("commands.help-give"));
        sender.sendMessage(plugin.getMessagesManager().getMessageWithPrefix("commands.help-reload"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("give");
            completions.add("reload");
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return new ArrayList<>(plugin.getLuckyBlockManager().getAllLuckyBlocks().keySet()).stream()
                .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1", "5", "10", "64");
        }
        
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}