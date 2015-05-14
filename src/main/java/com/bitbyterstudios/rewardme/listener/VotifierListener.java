package com.bitbyterstudios.rewardme.listener;

import com.bitbyterstudios.rewardme.RewardMe;
import com.bitbyterstudios.rewardme.Util;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.event.Listener;

public class VotifierListener implements Listener {

	private final RewardMe plugin;
	
	public VotifierListener(RewardMe plugin){
		this.plugin = plugin;
	}
	
	public void voteMade(VotifierEvent event) {
		if (!plugin.getConfig().getBoolean("Vote.Enabled")) {
			return;
		}
		final String user = event.getVote().getUsername();
		String cmd = plugin.getConfig().getString("Vote.Command");
		cmd = cmd.replaceAll("%USER", user);
		Util.executeCmd(cmd);
	}

}
