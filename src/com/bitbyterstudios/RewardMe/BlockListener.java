package com.bitbyterstudios.RewardMe;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {
	
	private RewardMe plugin;
	
	public BlockListener(RewardMe plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if (!plugin.getConfig().getBoolean("MiningReward.Enabled")) {
			return;
		}
		
		String blockType = event.getBlock().getType().toString();			
		Player p = event.getPlayer();
		int minedBlocks = plugin.getPlayersConfig().getInt(p.getName() + ".MinedBlocks."
				+ blockType) + 1;
		int neededBlocks = plugin.getConfig().getInt("MiningReward." + blockType
				+ ".NeededBlocks");

		if (minedBlocks >= neededBlocks && neededBlocks != 0) {
			String command = plugin.getConfig().getString("MiningReward." + blockType
					+ ".Command");

			if (command != null) {
				command = RewardMe.replaceUser(command, p);
				RewardMe.executeCmd(command);
				plugin.getPlayersConfig().set(
						p.getName() + ".MinedBlocks." + blockType, 0);
			}
		} else {
			plugin.getPlayersConfig().set(p.getName() + ".MinedBlocks." + blockType,
					minedBlocks);
		}
		
		plugin.savePlayersConfig();
	}

}
