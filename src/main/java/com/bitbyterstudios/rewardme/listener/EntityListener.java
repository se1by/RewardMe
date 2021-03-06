package com.bitbyterstudios.rewardme.listener;

import com.bitbyterstudios.rewardme.RewardMe;
import com.bitbyterstudios.rewardme.Util;
import org.bukkit.GameMode;
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
		
		if (le.getKiller() == null || le.getKiller().getGameMode() == GameMode.CREATIVE) {
			return;
		}
		
		final Player killer = le.getKiller();
		final String leName = le.getType().toString();
		String command = plugin.getConfig().getString("KillReward." + leName + ".Command");
		if (command != null) {
			final int neededKills = plugin.getConfig().getInt("KillReward." + leName + ".NeededKills");
			final int kills = plugin.getConfigManager().getPlayerConfig()
                    .getInt(killer.getUniqueId().toString() + ".Kills." + leName) + 1;
			
			if (kills >= neededKills) {
				command = Util.replaceUser(command, killer);
				Util.executeCmd(command);
				plugin.getConfigManager().getPlayerConfig()
                        .setAndSave(killer.getUniqueId().toString() + ".Kills." + leName, 0);
			} else {
				plugin.getConfigManager().getPlayerConfig()
                        .setAndSave(killer.getUniqueId().toString() + ".Kills." + leName, kills);
			}
		}
		
	}

}
