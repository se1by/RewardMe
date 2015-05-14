package com.bitbyterstudios.rewardme;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingYamlConfig extends YamlConfiguration {
    private File file;
    private Logger logger;

    public LoggingYamlConfig(String fileName, Logger logger) {
        this(new File(fileName), logger);
    }

    public LoggingYamlConfig(File file, Logger logger) {
        super();
        this.file = file;
        try {
            if (file.exists()) {
                load(file);
            } else {
                logger.warning("File  " + file.getAbsolutePath() + " does not exist!");
            }
        } catch (IOException e) {
            logger.severe("Could not load " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            logger.severe("Invalid configuration for file " + file.getAbsolutePath());
            e.printStackTrace();
        }
        this.logger = logger;
    }

    public void setAndSave(String path, Object value) {
        set(path, value);
        save();
    }

    public void save() {
        try {
            save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save config " + file.getName(), e);
        }
    }
}
