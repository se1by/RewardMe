package com.bitbyterstudios.rewardme.listener;

import java.util.Calendar;
import java.util.HashMap;

import com.bitbyterstudios.rewardme.RewardMe;
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

        if (!plugin.getNameConverterConfig().getString(player.getName(), "").equals(player.getUniqueId().toString())) {
            plugin.getNameConverterConfig().set(player.getName(), player.getUniqueId().toString());
            plugin.saveNameConverterConfig();
        }

        if (!player.hasPermission("RewardMe.Daily")) {
			return;
		}
		
		int lastLogin = plugin.getPlayersConfig().
				getInt(player.getUniqueId().toString() + ".LastLogin");
		
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
			plugin.getLogger().warning("You enabled the daily login reward, but didn't set a command!");
			plugin.getLogger().warning("Please set a command at \"DailyLogin\" -> \"Command\"");
		}
		cmd = RewardMe.replaceUser(cmd, player);
		
		String message = plugin.getConfig().getString("DailyLogin.Message");
		if (message == null) {
			message = "LoginReward given!";
            plugin.getLogger().warning("You enabled the daily login reward, but didn't set a message!");
            plugin.getLogger().warning("Please set an command at \"DailyLogin\" -> \"Message\"");
		}
		message = RewardMe.replaceUser(message, player);
		
		RewardMe.executeCmd(cmd);
		RewardMe.sendMessage(player, message);
		
		plugin.getPlayersConfig().set(player.getUniqueId().toString() + ".LastLogin", date);
		plugin.savePlayersConfig();
	}

}
