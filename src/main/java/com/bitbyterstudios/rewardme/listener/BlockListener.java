package com.bitbyterstudios.rewardme.listener;

import com.bitbyterstudios.rewardme.RewardMe;
import org.bukkit.GameMode;
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
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

		String blockType = event.getBlock().getType().toString();			
		Player player = event.getPlayer();
		int minedBlocks = plugin.getConfigManager().getPlayerConfig().getInt(player.getUniqueId().toString() + ".MinedBlocks."
				+ blockType) + 1;
		int neededBlocks = plugin.getConfig().getInt("MiningReward." + blockType
				+ ".NeededBlocks");

		if (minedBlocks >= neededBlocks && neededBlocks != 0) {
			String command = plugin.getConfig().getString("MiningReward." + blockType
					+ ".Command");

			if (command != null) {
				command = RewardMe.replaceUser(command, player);
				RewardMe.executeCmd(command);
				plugin.getConfigManager().getPlayerConfig().setAndSave(
						player.getUniqueId().toString() + ".MinedBlocks." + blockType, 0);
			}
		} else {
			plugin.getConfigManager().getPlayerConfig().setAndSave(player.getUniqueId().toString() + ".MinedBlocks." + blockType,
					minedBlocks);
		}
    }

}
