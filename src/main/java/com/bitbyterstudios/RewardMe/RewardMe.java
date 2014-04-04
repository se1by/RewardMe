package com.bitbyterstudios.rewardme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitbyterstudios.rewardme.listener.BlockListener;
import com.bitbyterstudios.rewardme.listener.EntityListener;
import com.bitbyterstudios.rewardme.listener.PlayerListener;
import com.bitbyterstudios.rewardme.listener.VotifierListener;
import com.evilmidget38.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RewardMe extends JavaPlugin {
	
	private FileConfiguration players;
	private FileConfiguration points;
	private FileConfiguration redeem;
	private FileConfiguration rewards;
    private FileConfiguration nameConverter;
	private File playersFile;
	private File pointsFile;
	private File redeemFile;
	private File rewardsFile;
    private File nameConverterFile;
	
	public void onEnable(){
		saveDefaultConfig();
		
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

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			//Metrics disabled?
		}
	}
	
	public FileConfiguration getPlayersConfig(){
		if (players == null) {
			playersFile = new File(getDataFolder(), "players.yml");
			if (!playersFile.exists()) {
				saveResource("players.yml", false);
			}
			players = YamlConfiguration.loadConfiguration(playersFile);
		}
		return players;
	}
	
	public void savePlayersConfig(){
		try {
            getPlayersConfig().save(playersFile);
			players = YamlConfiguration.loadConfiguration(playersFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + playersFile, ex);
        }
	}
	
	public FileConfiguration getPointsConfig(){
		if (points == null) {
			pointsFile = new File(getDataFolder(), "points.yml");
			if (!pointsFile.exists()) {
				saveResource("points.yml", false);
			}
			points = YamlConfiguration.loadConfiguration(pointsFile);
		}
		return points;
	}
	
	public void savePointsConfig(){
		try {
            getPointsConfig().save(pointsFile);
			points = YamlConfiguration.loadConfiguration(pointsFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + pointsFile, ex);
        }
	}
	
	public FileConfiguration getRedeemConfig(){
		if (redeem == null) {
			redeemFile = new File(getDataFolder(), "redeem.yml");
			if(!redeemFile.exists()){
				saveResource("redeem.yml", false);
			}
			redeem = YamlConfiguration.loadConfiguration(redeemFile);
		}
		return redeem;
	}

    public void saveRedeemConfig(){
        try {
            getRedeemConfig().save(redeemFile);
            redeem = YamlConfiguration.loadConfiguration(redeemFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + redeemFile, ex);
        }
    }

    public FileConfiguration getRewardsConfig(){
        if (rewards == null) {
            rewardsFile = new File(getDataFolder(), "rewards.yml");
            if(!rewardsFile.exists()){
                saveResource("rewards.yml", false);
            }
            rewards = YamlConfiguration.loadConfiguration(rewardsFile);
        }
        return rewards;
    }

    public void saveRewardsConfig(){
        try {
            getRewardsConfig().save(rewardsFile);
            rewards = YamlConfiguration.loadConfiguration(rewardsFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + rewardsFile, ex);
        }
    }

    public FileConfiguration getNameConverterConfig(){
        if (nameConverter == null) {
            nameConverterFile = new File(getDataFolder(), "nameConverter.yml");
            if(!nameConverterFile.exists()){
                saveResource("nameConverter.yml", false);
            }
            nameConverter = YamlConfiguration.loadConfiguration(nameConverterFile);
        }
        return nameConverter;
    }

    public void saveNameConverterConfig(){
        try {
            getNameConverterConfig().save(nameConverterFile);
            nameConverter = YamlConfiguration.loadConfiguration(nameConverterFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + nameConverterFile, ex);
        }
    }
	
	public static String replaceUser(String msg, Player player){
		return msg.replace("%USER", player.getName());
	}
	
	public static boolean executeCmd(String commands){
		try{
			String[] cmdSplit = commands.split(",,");
			for (String command : cmdSplit) {
				CommandSender cs = Bukkit.getServer().getConsoleSender();
			    Bukkit.getServer().dispatchCommand(cs, command);
			}
			return true;
		  }catch(Exception e){
			e.printStackTrace();
		  }
		return false;
	}
	
	public static void sendMessage(CommandSender sender, String msg){
		sender.sendMessage(new StringBuilder(25).append(ChatColor.GREEN).append("[RewardMe] ").append(ChatColor.GOLD).append(msg).toString());
	}

    public UUID uuidFromName(final String name) {
        if (Bukkit.getPlayerExact(name) != null) {
            return Bukkit.getPlayerExact(name).getUniqueId();
        }
        if (getNameConverterConfig().contains(name)) {
            return UUID.fromString(getNameConverterConfig().getString(name));
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
                            getNameConverterConfig().set(name, uuid.toString());
                            saveNameConverterConfig();
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
