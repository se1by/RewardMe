package com.bitbyterstudios.RewardMe;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
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
		} else if (cmd.getName().equalsIgnoreCase("generateredeem")){ // removed "|| cmd.getName().equalsIgnoreCase("genred")" for testing (alias working?)
			handleGenRedeem(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("useredeem")) {
			handleUseRedeem(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("rewardme")) {
			showHelp(sender);
		} else {
			return false;
		}
		return true;
	}

	private void handlePoints(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (sender instanceof Player) {
				showPoints((Player) sender, sender.getName());
			} else {
				RewardMe.info("Seems as the console doesn't have any points");
				RewardMe.info("Poor console :(");
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
		int pPoints = plugin.getPointsConfig().getInt(toShow);
		if ((pPoints == 1) || (pPoints == -1)) {
			RewardMe.sendMessage(sendTo,pre + " got " + pPoints + " point");
		} else if ((pPoints != 1) || (pPoints != -1)) {
			RewardMe.sendMessage(sendTo,pre + " got " + pPoints + " points");
		} else {
			RewardMe.sendMessage(sendTo, "I don't know this player!");
		}
	}

	private void givePoints(CommandSender sender, String userString, int amount) {
		Player user = Bukkit.getPlayer(userString);
		int oldPoints = plugin.getPointsConfig().getInt(userString);
		int newPoints = oldPoints + amount;

		if ((sender.hasPermission("RewardMe.givePoints")) || (sender.isOp())) {
			plugin.getPointsConfig().set(userString, newPoints);
			plugin.savePointsConfig();
			
			RewardMe.sendMessage(sender, amount + " points given!");
			RewardMe.sendMessage(user, "You received " + amount
					+ " points from " + sender.getName());
		} else {
			RewardMe.sendMessage(sender, "Insufficient permissions!");
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
		int pPoints = plugin.getPointsConfig().getInt(player.getName());

		if (pPoints >= price) {
			boolean success = RewardMe.executeCmd(command);

			if (success) {
				plugin.getPointsConfig().set(player.getName(),
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
		if(!((args.length == 2) && (sender.hasPermission("RewardMe.Redeem")))){
			return;
		}
		String name = args[0];
		if(plugin.getRedeemConfig().contains(name)){
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
		if(args.length != 1){
			return;
		}
		if (sender instanceof Player) {
			useRedeem((Player) sender, args[0]);
		} else {
			RewardMe.sendMessage(sender, "The console doesn't want that :O");
		}
	}

	private void useRedeem(Player player, String code) {
		Redeem redeem = new Redeem(Redeem.getName(UUID.fromString(code)), plugin);
		String command = redeem.useCode(player);
		command = RewardMe.replaceUser(command, player);
		if (command.equalsIgnoreCase("used")) {
			RewardMe.sendMessage(player, "You already used that code!");
		} else if (command.equalsIgnoreCase("outdated")) {
			RewardMe.sendMessage(player, "Outdated code!");
		} else if (command.equalsIgnoreCase("error")) {
			RewardMe.sendMessage(player, "An error occured!");
			RewardMe.sendMessage(player,  "Please contact an admin!");
		} else if (command.equalsIgnoreCase("unknown")) {
			RewardMe.sendMessage(player, "Unknown code!");
		} else {
			boolean success = RewardMe.executeCmd(command);
			if (!success) {
				RewardMe.sendMessage(player, "An error occured!");
			}
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

}
