package com.bitbyterstudios.RewardMe;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EListener implements Listener {
	
	private RewardMe plugin;
	
	public EListener(RewardMe plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event){
		if(!plugin.getConfig().getBoolean("KillReward.Enabled")){
			return;
		}
		
		LivingEntity le = event.getEntity();
		
		if(!(le.getKiller() instanceof Player)){
			return;
		}
		
		Player killer = le.getKiller();
		String leName = le.getType().toString();
		String command = plugin.getConfig().getString("KillReward." + leName + ".Command");
		if(command != null){
			int neededKills = plugin.getConfig().getInt("KillReward." + leName + ".NeededKills");
			int kills = plugin.getPlayersConfig().getInt(killer.getName() + ".Kills." + leName) + 1;
			
			if(kills >= neededKills){
				command = RewardMe.replaceUser(command, killer);
				RewardMe.executeCmd(command);
				plugin.getPlayersConfig().set(killer.getName() + ".Kills." + leName, 0);
			}
			else{
				plugin.getPlayersConfig().set(killer.getName() + ".Kills." + leName, kills);
			}
			plugin.savePlayersConfig();
		}
		
	}

}
