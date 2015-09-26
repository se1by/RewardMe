package com.bitbyterstudios.rewardme;

import com.evilmidget38.UUIDFetcher;
import com.puzlinc.messenger.Messenger;
import net.gravitydevelopment.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

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
        UUID toShowUUID = plugin.uuidFromName(toShow);
        if (toShowUUID == null) {
            plugin.getMessenger().send(Messenger.NOT_CACHED, sendTo, toShow);
            return;
        }
		int pPoints = plugin.getConfigManager().getPointConfig().getInt(toShowUUID.toString());
		plugin.getMessenger().send(Messenger.POINTS_INFO, sendTo, toShow, pPoints);
	}

	private void givePoints(CommandSender sender, String userString, int amount) {
        if (!sender.hasPermission("RewardMe.givePoints") && !sender.isOp()) {
            plugin.getMessenger().send(Messenger.INSUFFICIENT_PERMISSIONS, sender);
            return;
        }

        UUID userUUID = plugin.uuidFromName(userString);
        if (userUUID == null) {
            plugin.getMessenger().send(Messenger.NOT_CACHED, sender, userString);
            return;
        }
		Player user = Bukkit.getPlayerExact(userString);
		int oldPoints = plugin.getConfigManager().getPointConfig().getInt(userUUID.toString());
		int newPoints = oldPoints + amount;

	    plugin.getConfigManager().getPointConfig().setAndSave(userUUID.toString(), newPoints);

		plugin.getMessenger().send(Messenger.POINTS_GIVEN, sender, String.valueOf(amount), userString);
        if (user != null) {
            plugin.getMessenger().send(Messenger.POINTS_RECEIVED, user, String.valueOf(amount), sender.getName());
        }
	}

	private void showRewards(CommandSender sender) {
		Collection<Reward> rewards = plugin.getRewardManager().getRewards();
		for (Reward reward : rewards) {
            plugin.getMessenger().send(Messenger.REWARD_INFO,sender, reward.getName(),
                    reward.getDescription(), String.valueOf(reward.getPrice()), reward.getPerm());
		}
	}

	private void handleReward(CommandSender sender, String[] args) {
		if ((args.length == 2) && (args[0].equalsIgnoreCase("buy"))) {
			if (sender instanceof Player) {
				buyReward((Player) sender, args[1]);
			} else {
				plugin.getLogger().info("The console refused the reward :O");
			}
		} else {
			showHelp(sender);
		}
	}

	private void buyReward(Player player, String item) {
        if (!plugin.getRewardManager().hasReward(item)) {
            plugin.getMessenger().send(Messenger.REWARD_UNKNOWN, player, item);
            return;
        }
		plugin.getRewardManager().getReward(item).buy(player, false, false);
	}

	private void handleGenRedeem(CommandSender sender, String[] args) {
		if (!((args.length == 2) && (sender.hasPermission("RewardMe.Redeem")))) {
			return;
		}
		String name = args[0];
		if (plugin.getConfigManager().getRedeemConfig().contains(name)) {
			plugin.getMessenger().send(Messenger.REDEEM_NAME_TAKEN, sender);
			return;
		}
		Redeem r = new Redeem(name, plugin);
		UUID code;
		if (args[1].equalsIgnoreCase("once")) {
			code = r.generateCode();
		} else {
			int duration = Integer.parseInt(args[1]);
			code = r.generateCode("", duration);
		}
		plugin.getMessenger().send(Messenger.REDEEM_CODE_INFO, sender, code.toString());
	}

	private void handleUseRedeem(CommandSender sender, String[] args) {
		if (args.length != 1) {
			return;
		}
		if (sender instanceof Player) {
			useRedeem((Player) sender, args[0]);
		} else {
			plugin.getLogger().info("The console doesn't want that :O");
		}
	}

	private void useRedeem(Player player, String code) {
        if (!Util.isUUID(code)) {
            plugin.getMessenger().send(Messenger.REDEEM_ERROR, player);
            return;
        }
		Redeem redeem = new Redeem(UUID.fromString(code), plugin);
		String command = redeem.useCode(player);
		command = Util.replaceUser(command, player);
		if (command.equalsIgnoreCase("used")) {
            plugin.getMessenger().send(Messenger.REDEEM_USED, player);
		} else if (command.equalsIgnoreCase("outdated")) {
            plugin.getMessenger().send(Messenger.REDEEM_OUTDATED, player);
		} else if (command.equalsIgnoreCase("error")) {
            plugin.getMessenger().send(Messenger.REDEEM_ERROR, player);
		} else if (command.equalsIgnoreCase("unknown")) {
            plugin.getMessenger().send(Messenger.REDEEM_UNKNOWN, player);
		} else {
			boolean success = Util.executeCmd(command);
			if (!success) {
                plugin.getMessenger().send(Messenger.REDEEM_ERROR, player);
			}
		}
	}

    private void update(CommandSender sender) {
        if (!sender.hasPermission("RewardMe.givePoints") && !sender.isOp()) {
            plugin.getMessenger().send(Messenger.INSUFFICIENT_PERMISSIONS, sender);
            return;
        }
        Updater updater = new Updater(plugin, 33420, plugin.getPluginFile(), Updater.UpdateType.DEFAULT, false);
        if (updater.getResult().equals(Updater.UpdateResult.SUCCESS)) {
            plugin.getMessenger().send(Messenger.UPDATE_SUCCESS, sender, updater.getLatestName());
        } else {
            plugin.getMessenger().send(Messenger.UPDATE_FAIL, sender, updater.getResult().name());
        }
    }

	private void showHelp(CommandSender sender) {
        plugin.getMessenger().send(Messenger.HELP_USER, sender);
		if (sender.hasPermission("RewardMe.givePoints")) {
            plugin.getMessenger().send(Messenger.HELP_ADMIN, sender);
		}
	}

    //Damn bitchy monster method
    //Might use java8 and streams for that
    private void convert(final CommandSender sender) {
        if (!sender.hasPermission("RewardMe.givePoints")) {
            plugin.getMessenger().send(Messenger.INSUFFICIENT_PERMISSIONS, sender);
            return;
        }

        Set<String> names = plugin.getConfigManager().getPlayerConfig().getKeys(false);
        names.addAll(plugin.getConfigManager().getPointConfig().getKeys(false));
        for (String key : plugin.getConfigManager().getRedeemConfig().getKeys(false)) {
            names.addAll(plugin.getConfigManager().getRedeemConfig().getConfigurationSection(key + ".UsedBy").getKeys(false));
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
                            for (String key : plugin.getConfigManager().getPlayerConfig().getKeys(false)) {
                                if (Util.isUUID(key)) {
                                    String convertedKey = getKey(converted, key);
                                    if (convertedKey == null) { continue; }
                                    for (String block : plugin.getConfigManager().getPlayerConfig()
                                            .getConfigurationSection(key + ".MinedBlocks").getKeys(false)) {
                                        int newValue = plugin.getConfigManager().getPlayerConfig()
                                                .getInt(convertedKey + ".MinedBlocks." + block) +
                                                plugin.getConfigManager().getPlayerConfig().getInt(key + ".MinedBlocks." + block);
                                        plugin.getConfigManager().getPlayerConfig()
                                                .setAndSave(key + ".MinedBlocks." + block, newValue);
                                    }
                                    for (String kill : plugin.getConfigManager().getPlayerConfig()
                                            .getConfigurationSection(key + ".Kills").getKeys(false)) {
                                        int newValue = plugin.getConfigManager().getPlayerConfig()
                                                .getInt(convertedKey + ".Kills." + kill) +
                                                plugin.getConfigManager().getPlayerConfig().getInt(key + ".Kills." + kill);
                                        plugin.getConfigManager().getPlayerConfig().setAndSave(key + ".Kills." + kill, newValue);
                                    }
                                    plugin.getConfigManager().getPlayerConfig().setAndSave(convertedKey, null);
                                } else {
                                    plugin.getConfigManager().getPlayerConfig()
                                            .setAndSave(converted.get(key).toString(),
                                                    plugin.getConfigManager().getPlayerConfig().getConfigurationSection(key));
                                    plugin.getConfigManager().getPlayerConfig().setAndSave(key, null);
                                }
                            }
                            for (String key : plugin.getConfigManager().getPointConfig().getKeys(false)) {
                                if (Util.isUUID(key)) {
                                    String convertedKey = getKey(converted, key);
                                    if (convertedKey == null) { continue; }
                                    plugin.getConfigManager().getPointConfig().setAndSave(
                                            key, plugin.getConfigManager().getPointConfig().getInt(key)
                                                    + plugin.getConfigManager().getPointConfig().getInt(convertedKey));
                                } else {
                                    plugin.getConfigManager().getPointConfig()
                                            .setAndSave(converted.get(key).toString(),
                                                    plugin.getConfigManager().getPointConfig().getInt(key));
                                    plugin.getConfigManager().getPointConfig().setAndSave(key, null);
                                }
                            }
                            for (String key : plugin.getConfigManager().getRedeemConfig().getKeys(false)) {
                                for (String userKey : plugin.getConfigManager().getRedeemConfig()
                                        .getConfigurationSection(key + ".UsedBy").getKeys(false)) {
                                    if (Util.isUUID(userKey)) { continue; }
                                    plugin.getConfigManager().getRedeemConfig().setAndSave(key + ".UsedBy." + converted.get(userKey),
                                            plugin.getConfigManager().getRedeemConfig().getConfigurationSection(key + ".UsedBy." + userKey));
                                    plugin.getConfigManager().getRedeemConfig().setAndSave(key + ".UsedBy." + userKey, null);
                                }
                            }
                            plugin.getMessenger().send(Messenger.CONVERSION_DONE, sender);
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
            if (Util.isUUID(s)) {
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
