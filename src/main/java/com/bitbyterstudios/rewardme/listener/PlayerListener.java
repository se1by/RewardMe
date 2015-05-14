package com.bitbyterstudios.rewardme.listener;

import com.bitbyterstudios.rewardme.Reward;
import com.bitbyterstudios.rewardme.RewardMe;
import com.bitbyterstudios.rewardme.Util;
import com.puzlinc.messenger.Messenger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.HashMap;

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

        if (!plugin.getConfigManager()
                .getNameConverterConfig().getString(player.getName(), "").equals(player.getUniqueId().toString())) {
            plugin.getConfigManager().getNameConverterConfig().setAndSave(player.getName(), player.getUniqueId().toString());
        }

        if (player.hasPermission("RewardMe.givePoints") && plugin.shouldNotify()) {
            player.sendMessage(ChatColor.GREEN + "There is a new update for RewardMe!");
        }

        if (!player.hasPermission("RewardMe.Daily")) {
			return;
		}
		
		int lastLogin = plugin.getConfigManager().getPlayerConfig().
				getInt(player.getUniqueId().toString() + ".LastLogin");
		
		if (lastLogin == date) {
			return;
		}

		loggedIn.put(player.getName(), true);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(loggedIn.get(player.getName())){
					giveLoginReward(player);
					loggedIn.remove(player.getName());
				}
			}
		}.runTaskLater(plugin, plugin.getConfig().getInt("DailyLogin.Delay") * 1200L);
    }

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		loggedIn.remove(event.getPlayer().getName());
	}
	
	protected void giveLoginReward(Player player) {
		String cmd = plugin.getConfig().getString("DailyLogin.Command");
		if (cmd == null) {
			cmd = "give %USER log 10";
			plugin.getLogger().warning("You enabled the daily login reward, but didn't set a command!");
			plugin.getLogger().warning("Please set a command at \"DailyLogin\" -> \"Command\"");
		}
		cmd = Util.replaceUser(cmd, player);
		
		String message = plugin.getConfig().getString("DailyLogin.Message");
		if (message == null) {
			message = "LoginReward given!";
            plugin.getLogger().warning("You enabled the daily login reward, but didn't set a message!");
            plugin.getLogger().warning("Please set a message at \"DailyLogin\" -> \"Message\"");
		}
		message = Util.replaceUser(message, player);
		
		Util.executeCmd(cmd);
        player.sendMessage(ChatColor.GREEN + "[RewardMe] " + ChatColor.GOLD + message);

		plugin.getConfigManager().getPlayerConfig().setAndSave(player.getUniqueId().toString() + ".LastLogin", date);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		if (!event.getClickedBlock().getType().equals(Material.WALL_SIGN) && !event.getClickedBlock().getType().equals(Material.SIGN_POST)) {
			return;
		}
		Sign sign = (Sign) event.getClickedBlock().getState();
		if (!Util.strip(sign.getLine(0)).equals("[RewardMe]")) {
			return;
		}
		if (!event.getPlayer().hasPermission("RewardMe.sign.use")) {
			plugin.getMessenger().send(Messenger.REWARD_NO_PERM, event.getPlayer());
			return;
		}

		if (!plugin.getConfigManager().getSignConfig().getBoolean(sign.getLocation().toString() + ".multi_use", false) &&
				plugin.getConfigManager().getSignConfig().contains(
						sign.hashCode() + "." + event.getPlayer().getUniqueId().toString())) {
			plugin.getMessenger().send(Messenger.REWARD_SIGN_USED, event.getPlayer());
			return;
		}

		String rewardName = Util.strip(sign.getLine(1));
		if (!plugin.getRewardManager().hasReward(rewardName)) {
			plugin.getLogger().warning("Sign at " + sign.getLocation() + " references reward \"" + rewardName + "\", but that reward doesn't exist!");
			plugin.getMessenger().send(Messenger.REWARD_UNKNOWN, event.getPlayer(), rewardName);
			return;
		}

		Reward reward = plugin.getRewardManager().getReward(rewardName);
		reward.buy(event.getPlayer(), "noperm".equals(sign.getLine(2)));

		plugin.getConfigManager().getSignConfig().setAndSave(
				sign.hashCode() + "." + event.getPlayer().getUniqueId().toString(), true);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if ("[RewardMe]".equals(Util.strip(event.getLine(0))) && !event.getPlayer().hasPermission("RewardMe.sign.create")) {
			event.setCancelled(true);
			plugin.getMessenger().send(Messenger.SIGN_CREATE_NO_PERM, event.getPlayer());
		}
	}

}
