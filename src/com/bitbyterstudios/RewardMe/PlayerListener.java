package com.bitbyterstudios.RewardMe;

import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
	
	private RewardMe plugin;
	private int date;
	private HashMap<String, Boolean> loggedIn;
	
	public PlayerListener(RewardMe plugin){
		this.plugin = plugin;
		date = Calendar.getInstance().get(5);
		loggedIn = new HashMap<String, Boolean>();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		final Player player = event.getPlayer();
		
		if (!player.hasPermission("RewardMe.Daily")) {
			return;
		}
		if (!plugin.getConfig().getBoolean("DailyLogin.Enabled")) {
			return;
		}
		
		int lastLogin = plugin.getPlayersConfig().
				getInt(player.getName() + "LastLogin");
		
		if (lastLogin == date) {
			return;
		}
		
		loggedIn.put(player.getName(), true);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(loggedIn.get(player.getName())){
					givLoginReward(player);
					loggedIn.remove(player.getName());
				}
			}
		}.runTaskLater(plugin, plugin.getConfig().getInt("DailyLogin.Delay") * 1200L);
		
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		loggedIn.remove(event.getPlayer().getName());
	}
	
	protected void givLoginReward(Player player) {
		String cmd = plugin.getConfig().getString("DailyLogin.Command");
		if (cmd == null) {
			cmd = "give %USER log 10";
			RewardMe.warn("You enabled the daily login reward, but didn't set a command!");
			RewardMe.warn("Please set a command at \"DailyLogin\" -> \"Command\"");
		}
		cmd = RewardMe.replaceUser(cmd, player);
		
		String message = plugin.getConfig().getString("DailyLogin.Message");
		if (message == null) {
			message = "LoginReward given!";
			RewardMe.warn("You enabled the daily login reward, but didn't set a message!");
			RewardMe.warn("Please set an command at \"DailyLogin\" -> \"Message\"");
		}
		message = RewardMe.replaceUser(message, player);
		
		RewardMe.executeCmd(cmd);
		RewardMe.sendMessage(player, message);
		
		plugin.getPlayersConfig().set(player.getName() + ".LastLogin", date);
		plugin.savePlayersConfig();
	}

}
