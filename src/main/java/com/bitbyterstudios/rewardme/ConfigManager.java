package com.bitbyterstudios.rewardme;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {
    private LoggingYamlConfig playerConfig;
    private LoggingYamlConfig pointConfig;
    private LoggingYamlConfig rewardConfig;
    private LoggingYamlConfig signConfig;
    private LoggingYamlConfig redeemConfig;
    private LoggingYamlConfig nameConverterConfig;

    public ConfigManager(JavaPlugin plugin) {
        playerConfig = new LoggingYamlConfig(new File(plugin.getDataFolder(), "players.yml"), plugin.getLogger());
        pointConfig = new LoggingYamlConfig(new File(plugin.getDataFolder(), "points.yml"), plugin.getLogger());
        rewardConfig = new LoggingYamlConfig(new File(plugin.getDataFolder(), "rewards.yml"), plugin.getLogger());
        signConfig = new LoggingYamlConfig(new File(plugin.getDataFolder(), "signs.yml"), plugin.getLogger());
        redeemConfig = new LoggingYamlConfig(new File(plugin.getDataFolder(), "redeem.yml"), plugin.getLogger());
        nameConverterConfig = new LoggingYamlConfig(new File(plugin.getDataFolder(), "nameConverter.yml"), plugin.getLogger());
    }

    public LoggingYamlConfig getPlayerConfig() {
        return playerConfig;
    }

    public LoggingYamlConfig getPointConfig() {
        return pointConfig;
    }

    public LoggingYamlConfig getRewardConfig() {
        return rewardConfig;
    }

    public LoggingYamlConfig getSignConfig() {
        return signConfig;
    }

    public LoggingYamlConfig getRedeemConfig() {
        return redeemConfig;
    }

    public LoggingYamlConfig getNameConverterConfig() {
        return nameConverterConfig;
    }
}
