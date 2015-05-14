package com.bitbyterstudios.rewardme;

import com.bitbyterstudios.rewardme.listener.BlockListener;
import com.bitbyterstudios.rewardme.listener.EntityListener;
import com.bitbyterstudios.rewardme.listener.PlayerListener;
import com.bitbyterstudios.rewardme.listener.VotifierListener;
import com.evilmidget38.UUIDFetcher;
import com.puzlinc.messenger.Messenger;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class RewardMe extends JavaPlugin {
	
	private ConfigManager configManager;
    private RewardManager rewardManager;
    private boolean shouldNotify;
    private File pluginFile;

    private Messenger messenger;
	
	public void onEnable(){
		saveDefaultConfig();
        configManager = new ConfigManager(this);
        rewardManager = new RewardManager(configManager.getRewardConfig(), this);

        messenger = new Messenger(this);
        if (getConfig().contains("locale")) {
            messenger.setFileName("messages_" + getConfig().getString("locale") + ".yml");
        } else {
            saveResource("messages_EN.yml", false);
            getConfig().set("locale", "EN");
            saveConfig();
            messenger.setFileName("messages_EN.yml");
        }
        messenger.load();
		
		CmdExecutor cmdExec = new CmdExecutor(this);
		getCommand("rewardme").setExecutor(cmdExec);
		getCommand("reward").setExecutor(cmdExec);
		getCommand("points").setExecutor(cmdExec);
		getCommand("rewards").setExecutor(cmdExec);
		getCommand("generateredeem").setExecutor(cmdExec);
		getCommand("useredeem").setExecutor(cmdExec);

        if (getConfig().getBoolean("MiningReward.Enabled")) {
            getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        }
        if (getConfig().getBoolean("KillReward.Enabled")) {
            getServer().getPluginManager().registerEvents(new EntityListener(this), this);
        }
        if (getConfig().getBoolean("DailyLogin.Enabled")) {
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        }
        if (getConfig().getBoolean("Vote.Enabled")) {
            getServer().getPluginManager().registerEvents(new VotifierListener(this), this);
        }

        if (!getConfig().contains("Updater.Enabled")) {
            getConfig().set("Updater.Enabled", true);
            getConfig().set("Updater.AutoUpdate", false);
            getConfig().set("Updater.Notify", true);
            saveConfig();
            getLogger().info("Updater was enabled as the config didn't contain the \"Updater.Enabled\" path.\n" +
                    "You can disable it by setting it's value to false.");
        }

        if (getConfig().getBoolean("Updater.Enabled")) {
            Updater updater = null;
            if (getConfig().getBoolean("Updater.AutoUpdate")) {
                updater = new Updater(this, 33420, this.getFile(), Updater.UpdateType.DEFAULT, false);
            } else {
                updater = new Updater(this, 33420, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
            }
            if ((updater.getResult().equals(Updater.UpdateResult.UPDATE_AVAILABLE)
                    || updater.getResult().equals(Updater.UpdateResult.SUCCESS))
                    && getConfig().getBoolean("Updater.Notify")) {
                shouldNotify = true;
            }
        }

        if (!getConfig().contains("Metrics.Enabled")) {
            getConfig().set("Metrics.Enabled", true);
            saveConfig();
            getLogger().info("Metrics were enabled as the config didn't contain the \"Metrics.Enabled\" path.\n" +
                    "You can disable it by setting it's value to false."); //To get info from upgrading users
        }

        if (getConfig().getBoolean("Metrics.Enabled")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                //Metrics disabled?
            }
        }

        pluginFile = this.getFile();
	}

    public Messenger getMessenger() {
        return messenger;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public boolean shouldNotify() {
        return shouldNotify;
    }

    public File getPluginFile() {
        return pluginFile;
    }

    public UUID uuidFromName(final String name) {
        if (Bukkit.getPlayerExact(name) != null) {
            return Bukkit.getPlayerExact(name).getUniqueId();
        }
        if (configManager.getNameConverterConfig().contains(name)) {
            return UUID.fromString(configManager.getNameConverterConfig().getString(name));
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                ArrayList<String> names = new ArrayList<String>();
                names.add(name);
                UUIDFetcher fetcher = new UUIDFetcher(names);
                try {
                    final UUID uuid = fetcher.call().get(name);
                    Bukkit.getScheduler().runTask(RewardMe.this, new Runnable() {
                        @Override
                        public void run() {
                            configManager.getNameConverterConfig().setAndSave(name, uuid.toString());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }

}
