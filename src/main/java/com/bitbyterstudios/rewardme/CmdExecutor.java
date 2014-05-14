package com.bitbyterstudios.rewardme;

import java.util.*;

import com.evilmidget38.UUIDFetcher;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdExecutor implements CommandExecutor {
	
	private RewardMe plugin;
	
	public  CmdExecutor(RewardMe plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("points")) {
			handlePoints(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("rewards")) {
			showRewards(sender);
		} else if (cmd.getName().equalsIgnoreCase("reward")) {
			handleReward(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("generateredeem")){
			handleGenRedeem(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("useredeem")) {
			handleUseRedeem(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("rewardme")) {
            if (args.length == 1 && "convert".equalsIgnoreCase(args[0])) {
                convert(sender);
            } else if (args.length == 1 && "update".equalsIgnoreCase(args[0])) {
                update(sender);
            } else {
                showHelp(sender);
            }
		} else {
			return false;
		}
		return true;
	}

	private void handlePoints(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				showPoints(sender, sender.getName());
			} else {
				plugin.getLogger().info("Seems as the console doesn't have any points");
				plugin.getLogger().info("Poor console :(");
			}
		} else if (args.length == 1) {
			showPoints(sender, args[0]);
		} else if (args.length == 2) {
			String user = args[0];
			int amount = Integer.parseInt(args[1]);
			givePoints(sender, user, amount);
		}
	}

	private void showPoints(CommandSender sendTo, String toShow) {
		String pre = toShow;
		if(toShow.equals(sendTo.getName())){
			pre = "You";
		}
        UUID toShowUUID = plugin.uuidFromName(toShow);
        if (toShowUUID == null) {
            RewardMe.sendMessage(sendTo, toShow + "'s UUID was not cached. Please try again in a few seconds.");
            return;
        }
		int pPoints = plugin.getPointsConfig().getInt(toShowUUID.toString());
		if ((pPoints == 1) || (pPoints == -1)) {
			RewardMe.sendMessage(sendTo,pre + " got " + pPoints + " point");
		} else {
			RewardMe.sendMessage(sendTo,pre + " got " + pPoints + " points");
		}
	}

	private void givePoints(CommandSender sender, String userString, int amount) {
        if (!sender.hasPermission("RewardMe.givePoints") && !sender.isOp()) {
            RewardMe.sendMessage(sender, "Insufficient permissions!");
            return;
        }

        UUID userUUID = plugin.uuidFromName(userString);
        if (userUUID == null) {
            RewardMe.sendMessage(sender, userString + "'s UUID was not cached. Please try again in a few seconds.");
            return;
        }
		Player user = Bukkit.getPlayerExact(userString);
		int oldPoints = plugin.getPointsConfig().getInt(userUUID.toString());
		int newPoints = oldPoints + amount;

	    plugin.getPointsConfig().set(userUUID.toString(), newPoints);
		plugin.savePointsConfig();
			
		RewardMe.sendMessage(sender, amount + " points given!");
        if (user != null) {
            RewardMe.sendMessage(user, "You received " + amount + " points from " + sender.getName());
        }
	}

	private void showRewards(CommandSender sender) {
		Set<String> allRewards = plugin.getRewardsConfig().getKeys(false);
		for (String reward : allRewards) {
			String descr = plugin.getRewardsConfig().getString(reward + ".Description");
			int price = plugin.getRewardsConfig().getInt(reward + ".Price");
			RewardMe.sendMessage(sender, "Type in /reward buy " + reward
					+ " to get " + descr + ".");
			RewardMe.sendMessage(sender, "Price: " + price + " points.");
		}
	}

	private void handleReward(CommandSender sender, String[] args) {
		if ((args.length == 2) && (args[0].equalsIgnoreCase("buy"))) {
			if (sender instanceof Player) {
				buyReward((Player) sender, args[1]);
			} else {
				RewardMe.sendMessage(sender, "The console refused the reward :O");
			}
		} else {
			showHelp(sender);
		}
	}

	private void buyReward(Player player, String item) {
		String command = plugin.getRewardsConfig().getString(item + ".Command");
		command = RewardMe.replaceUser(command, player);
		int price = plugin.getRewardsConfig().getInt(item + ".Price");
		int pPoints = plugin.getPointsConfig().getInt(player.getUniqueId().toString());

		if (pPoints >= price) {
			boolean success = RewardMe.executeCmd(command);

			if (success) {
				plugin.getPointsConfig().set(player.getUniqueId().toString(),
						Integer.valueOf(pPoints - price));
				plugin.savePointsConfig();

				RewardMe.sendMessage(player, "Reward " + item + " given!");
			} else {
				RewardMe.sendMessage(player, "An error occured!");
			}
		} else {
			RewardMe.sendMessage(player, "You don't have enough points!");
		}
	}

	private void handleGenRedeem(CommandSender sender, String[] args) {
		if (!((args.length == 2) && (sender.hasPermission("RewardMe.Redeem")))) {
			return;
		}
		String name = args[0];
		if (plugin.getRedeemConfig().contains(name)) {
			RewardMe.sendMessage(sender, "This name is already taken!");
			return;
		}
		Redeem r = new Redeem(name, plugin);
		UUID code = null;
		if (args[1].equalsIgnoreCase("once")) {
			code = r.generateCode();
		} else {
			int duration = Integer.parseInt(args[1]);
			code = r.generateCode("", duration);
		}
		RewardMe.sendMessage(sender, "Your redeemcode is " + code.toString());
		RewardMe.sendMessage(sender, "Please change the default reward-command in Redeem.yml");
	}

	private void handleUseRedeem(CommandSender sender, String[] args) {
		if (args.length != 1) {
			return;
		}
		if (sender instanceof Player) {
			useRedeem((Player) sender, args[0]);
		} else {
			RewardMe.sendMessage(sender, "The console doesn't want that :O");
		}
	}

	private void useRedeem(Player player, String code) {
		Redeem redeem = new Redeem(UUID.fromString(code), plugin);
		String command = redeem.useCode(player);
		command = RewardMe.replaceUser(command, player);
		if (command.equalsIgnoreCase("used")) {
			RewardMe.sendMessage(player, "You already used that code!");
		} else if (command.equalsIgnoreCase("outdated")) {
			RewardMe.sendMessage(player, "Outdated code!");
		} else if (command.equalsIgnoreCase("error")) {
			RewardMe.sendMessage(player, "An error occurred!");
			RewardMe.sendMessage(player,  "Please contact an admin!");
		} else if (command.equalsIgnoreCase("unknown")) {
			RewardMe.sendMessage(player, "Unknown code!");
		} else {
			boolean success = RewardMe.executeCmd(command);
			if (!success) {
				RewardMe.sendMessage(player, "An error occurred!");
			}
		}
	}

    private void update(CommandSender sender) {
        if (!sender.hasPermission("RewardMe.givePoints") && !sender.isOp()) {
            RewardMe.sendMessage(sender, "Insufficient permissions!");
            return;
        }
        Updater updater = new Updater(plugin, 33420, plugin.getPluginFile(), Updater.UpdateType.DEFAULT, false);
        if (updater.getResult().equals(Updater.UpdateResult.SUCCESS)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully updated to " + updater.getLatestName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to update: " + updater.getResult().name());
        }
    }

	private void showHelp(CommandSender sender) {
		RewardMe.sendMessage(sender, "Help");
		RewardMe.sendMessage(sender, "/Points to show your current Points");
		if (sender.hasPermission("RewardMe.givePoints")) {
			RewardMe.sendMessage(sender, "/Points <user> <amount> to give <user> <amount> Points ");
		}
		RewardMe.sendMessage(sender, "/rewards to show all accessible rewards");
		RewardMe.sendMessage(sender, "/reward <name> to buy the reward <name>");
		
	}

    private void convert(final CommandSender sender) {
        if (!sender.hasPermission("RewardMe.givePoints")) {
            RewardMe.sendMessage(sender, "Insufficient permissions!");
            return;
        }

        Set<String> names = plugin.getPlayersConfig().getKeys(false);
        names.addAll(plugin.getPointsConfig().getKeys(false));
        for (String key : plugin.getRedeemConfig().getKeys(false)) {
            names.addAll(plugin.getRedeemConfig().getConfigurationSection(key + ".UsedBy").getKeys(false));
        }
        names = removeUUIDs(names);
        final UUIDFetcher fetcher = new UUIDFetcher(new ArrayList<String>(names));
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    final Map<String, UUID> converted = new HashMap<String, UUID>(fetcher.call());
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (String key : plugin.getPlayersConfig().getKeys(false)) {
                                if (RewardMe.isUUID(key)) {
                                    String convertedKey = getKey(converted, key);
                                    if (convertedKey == null) { continue; }
                                    for (String block : plugin.getPlayersConfig().getConfigurationSection(key + ".MinedBlocks").getKeys(false)) {
                                        int newValue = plugin.getPlayersConfig().getInt(convertedKey + ".MinedBlocks." + block) +
                                                plugin.getPlayersConfig().getInt(key + ".MinedBlocks." + block);
                                        plugin.getPlayersConfig().set(key + ".MinedBlocks." + block, newValue);
                                    }
                                    for (String kill : plugin.getPlayersConfig().getConfigurationSection(key + ".Kills").getKeys(false)) {
                                        int newValue = plugin.getPlayersConfig().getInt(convertedKey + ".Kills." + kill) +
                                                plugin.getPlayersConfig().getInt(key + ".Kills." + kill);
                                        plugin.getPlayersConfig().set(key + ".Kills." + kill, newValue);
                                    }
                                    plugin.getPlayersConfig().set(convertedKey, null);
                                } else {
                                    plugin.getPlayersConfig().set(converted.get(key).toString(), plugin.getPlayersConfig().getConfigurationSection(key));
                                    plugin.getPlayersConfig().set(key, null);
                                }
                            }
                            for (String key : plugin.getPointsConfig().getKeys(false)) {
                                if (RewardMe.isUUID(key)) {
                                    String convertedKey = getKey(converted, key);
                                    if (convertedKey == null) { continue; }
                                    plugin.getPointsConfig().set(key, plugin.getPointsConfig().getInt(key) + plugin.getPointsConfig().getInt(convertedKey));
                                } else {
                                    plugin.getPointsConfig().set(converted.get(key).toString(), plugin.getPointsConfig().getInt(key));
                                    plugin.getPointsConfig().set(key, null);
                                }
                            }
                            for (String key : plugin.getRedeemConfig().getKeys(false)) {
                                for (String userKey : plugin.getRedeemConfig().getConfigurationSection(key + ".UsedBy").getKeys(false)) {
                                    if (RewardMe.isUUID(userKey)) { continue; }
                                    plugin.getRedeemConfig().set(key + ".UsedBy." + converted.get(userKey),
                                            plugin.getRedeemConfig().getConfigurationSection(key + ".UsedBy." + userKey));
                                    plugin.getRedeemConfig().set(key + ".UsedBy." + userKey, null);
                                }
                            }
                            plugin.savePlayersConfig();
                            plugin.savePointsConfig();
                            plugin.saveRedeemConfig();
                            RewardMe.sendMessage(sender, "Conversion done!");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Set<String> removeUUIDs(Set<String> collection) {
        for (String s : collection) {
            if (RewardMe.isUUID(s)) {
                collection.remove(s);
            }
        }
        return collection;
    }

    private String getKey(Map<String, ?> map, Object value) {
        for (Map.Entry<String, ?> e : map.entrySet()) {
            if (e.getValue().equals(value)) {
                return e.getKey();
            }
        }
        return null;
    }
}
