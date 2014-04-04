package com.bitbyterstudios.rewardme;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Redeem {
	
	private String name;
	private RewardMe plugin;

    public Redeem(String name, RewardMe plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    public Redeem(UUID uuid, RewardMe plugin) {
        this.name = getName(uuid);
        this.plugin = plugin;
    }

	public UUID generateCode(String cmd, int duration) {
		UUID uuid = UUID.randomUUID();
		String exDate = dateCalc(duration);

		plugin.getRedeemConfig().set(name + ".Code", uuid.toString());
		plugin.getRedeemConfig().set(name + ".Expiration", exDate);
		plugin.getRedeemConfig().set(name + ".Command", "give %USER 1 1");
		plugin.saveRedeemConfig();
		return uuid;
	}
	
	public UUID generateCode() {
		UUID uuid = UUID.randomUUID();
		plugin.getRedeemConfig().set(name + ".Code", uuid.toString());
		plugin.getRedeemConfig().set(name + ".Expiration", "once");
		plugin.getRedeemConfig().set(name + ".Command", "give %USER 1 1");
		plugin.saveRedeemConfig();
		return uuid;
	}
	
	public String useCode(Player p) {
		FileConfiguration redeemCfg = plugin.getRedeemConfig();

		String date = redeemCfg.getString(name + ".Expiration");
		if (date == null) {
			return "unknown";
		} else if(date.equalsIgnoreCase("once")) {
			String cmd = redeemCfg.getString(name + ".Command");
			redeemCfg.set(name, null);
			plugin.saveRedeemConfig();
			return cmd;
		}
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date today = Calendar.getInstance().getTime();
		Date todayFormat = null;
		
		Date expDate = null;
		
		try {
			expDate = df.parse(date);
			todayFormat = df.parse(df.format(today));
		} catch (ParseException e) {
			e.printStackTrace();
			return "error";
		}
		int result = todayFormat.compareTo(expDate);

		if (!redeemCfg.getBoolean(name + ".UsedBy." + p.getUniqueId().toString())) {
			if (result <= 0) {
				redeemCfg.set(name + ".UsedBy." + p.getUniqueId().toString(), true);
				plugin.saveRedeemConfig();
				return redeemCfg.getString(name + ".Command");
			} else {
				return "outdated";
			}
		} else {
			return "used";
		}
	}
	
	private String dateCalc(int duration) {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH +1);
		int year = cal.get(Calendar.YEAR);
		int maxDays;
		
		if (month %2 == 0) {
			if (month == 2) {
				maxDays = 29;
			} else {
				maxDays = 30;
			}
		} else {
			maxDays = 31;
		}

		int exDay = day + duration;
		int exMonth = month;
		int exYear = year;

		while (exDay > maxDays) {
			exDay = exDay - maxDays;
			exMonth++;
		}
		while (exMonth > 12) {
			exMonth = exMonth - 12;
			exYear++;
		}
		String date = new StringBuilder(exDay).append("/").append(exMonth).append("/").append(exYear).toString();
		return date;
	}

	public String getName(UUID code){
		FileConfiguration redeemCfg = YamlConfiguration.loadConfiguration(new File("plugins/rewardme/redeem.yml"));
		for (String key : redeemCfg.getKeys(false)) {
			if (redeemCfg.getString(key + ".Code").equals(code.toString())) {
				return key;
			}
		}
		return "";
	}
}
