package com.bitbyterstudios.rewardme;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.HashMap;

public class RewardManager {
    private RewardMe plugin;
    private HashMap<String, Reward> rewards;

    public RewardManager(FileConfiguration config, RewardMe plugin) {
        this.plugin = plugin;
        rewards = new HashMap<String, Reward>();
        load(config);
    }

    private void load(FileConfiguration config) {
        for (String s : config.getKeys(false)) {
            rewards.put(s, new Reward(plugin, s, config.getConfigurationSection(s)));
        }
    }

    public Reward getReward(String name) {
        return rewards.get(name);
    }

    public boolean hasReward(String name) {
        return rewards.containsKey(name);
    }

    public Collection<Reward> getRewards() {
        return rewards.values();
    }

}
