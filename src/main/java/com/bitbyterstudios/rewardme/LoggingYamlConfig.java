package com.bitbyterstudios.rewardme;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingYamlConfig extends YamlConfiguration {
    private FileConfiguration config;
    private File file;
    private Logger logger;

    public LoggingYamlConfig(String fileName, Logger logger) {
        this(new File(fileName), logger);
    }

    public LoggingYamlConfig(File file, Logger logger) {
        this.file = file;
        config = YamlConfiguration.loadConfiguration(file);
        this.logger = logger;
    }

    public void setAndSave(String path, Object value) {
        set(path, value);
        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save config " + file.getName(), e);
        }
    }
}
