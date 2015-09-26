package com.bitbyterstudios.rewardme;

import com.puzlinc.messenger.Messenger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Reward {
    private RewardMe plugin;

    private String name;
    private String description;
    private String perm;
    private String[] commands;
    private int price;

    public Reward(RewardMe plugin, String name, ConfigurationSection config) {
        this.plugin = plugin;
        this.name = name;
        this.description = config.getString("Description");
        this.perm = config.getString("perm", "");
        this.commands = config.getString("Command", "").split(",");
        this.price = config.getInt("Price");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getCommands() {
        return commands;
    }

    public int getPrice() {
        return price;
    }

    public boolean canAfford(Player player) {
        return price <= plugin.getConfigManager().getPointConfig().getInt(player.getUniqueId().toString());
    }

    public boolean buy(Player player, boolean ignorePerm, boolean isFree) {
        if (!ignorePerm && !perm.isEmpty() && !player.hasPermission(perm)) {
            plugin.getMessenger().send(Messenger.REWARD_NO_PERM, player);
            return false;
        }

        int pPoints = plugin.getConfigManager().getPointConfig().getInt(player.getUniqueId().toString());

        if (pPoints >= price || isFree) {
            for (String command : commands) {
                boolean result = plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        command.replace("%USER", player.getName()));
                if (!result) {
                    plugin.getLogger().warning("Could not properly execute command " + command);
                }
            }
            if (!isFree) {
                plugin.getConfigManager().getPointConfig().setAndSave(player.getUniqueId().toString(), pPoints - price);
            }
            plugin.getMessenger().send(Messenger.REWARD_GIVEN, player, name);
            return true;
        } else {
            plugin.getMessenger().send(Messenger.REWARD_NOT_ENOUGH_POINTS, player);
            return false;
        }
    }

    public String getPerm() {
        return "".equals(perm) ? "none" : perm;
    }
}
