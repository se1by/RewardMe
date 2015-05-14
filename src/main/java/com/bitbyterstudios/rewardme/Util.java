package com.bitbyterstudios.rewardme;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Util {

    public static String strip(String s) {
        return s != null ? ChatColor.stripColor(s) : "";
    }

	public static String replaceUser(String msg, Player player){
        return msg.replace("%USER", player.getName());
    }

	public static boolean executeCmd(String commands){
        try{
            if (commands.contains(",,")) {
                System.err.println("Commands \"" + commands + "\" contain double comma's, please change them to single!");
                commands = commands.replaceAll(",,", ",");
            }
            String[] cmdSplit = commands.split(",");
            for (String command : cmdSplit) {
                CommandSender cs = Bukkit.getServer().getConsoleSender();
                Bukkit.getServer().dispatchCommand(cs, command);
            }
            return true;
          }catch(Exception e){
            e.printStackTrace();
          }
        return false;
    }

	public static boolean isUUID(String s) {
        return s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
