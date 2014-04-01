package com.bitbyterstudios.RewardMe;

import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.VotifierEvent;

public class VotifierListener implements Listener{

	private RewardMe plugin;
	
	public VotifierListener(RewardMe plugin){
		this.plugin = plugin;
	}
	
	public void voteMade(VotifierEvent event) {
		if (!plugin.getConfig().getBoolean("Vote.Enabled")) {
			return;
		}
		String user = event.getVote().getUsername();
		String cmd = plugin.getConfig().getString("Vote.Command");
		cmd = cmd.replaceAll("%USER", user);
		RewardMe.executeCmd(cmd);
	}

}
