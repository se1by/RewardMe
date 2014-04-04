package com.bitbyterstudios.rewardme.listener;

import com.bitbyterstudios.rewardme.RewardMe;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListener implements Listener {
	
	private final RewardMe plugin;
	
	public EntityListener(RewardMe plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		final LivingEntity le = event.getEntity();
		
		if (le.getKiller() == null) {
			return;
		}
		
		final Player killer = le.getKiller();
		final String leName = le.getType().toString();
		String command = plugin.getConfig().getString("KillReward." + leName + ".Command");
		if (command != null) {
			final int neededKills = plugin.getConfig().getInt("KillReward." + leName + ".NeededKills");
			final int kills = plugin.getPlayersConfig().getInt(killer.getUniqueId().toString() + ".Kills." + leName) + 1;
			
			if (kills >= neededKills) {
				command = RewardMe.replaceUser(command, killer);
				RewardMe.executeCmd(command);
				plugin.getPlayersConfig().set(killer.getUniqueId().toString() + ".Kills." + leName, 0);
			} else {
				plugin.getPlayersConfig().set(killer.getUniqueId().toString() + ".Kills." + leName, kills);
			}
			plugin.savePlayersConfig();
		}
		
	}

}
