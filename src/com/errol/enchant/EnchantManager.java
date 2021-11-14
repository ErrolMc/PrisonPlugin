package com.errol.enchant;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.errol.Main;
import com.errol.utils.StaticUtils;

public class EnchantManager
{
	private Main plugin;
	
	public EnchantManager(Main plugin) 
	{
		this.plugin = plugin;
	}
	
	public void HandleCommand(Player player, String[] args) 
	{
		ItemStack item = player.getItemInHand();
		if (item != null) 
		{
			if (args.length > 1) 
			{
				int level = StaticUtils.ParseInt(args[1]);
				org.bukkit.enchantments.Enchantment enchantment = GetEnchantmentFromString(args[0]);

				if (enchantment != null) 
				{
					if (item.getEnchantments().containsKey(enchantment))
						item.removeEnchantment(enchantment);
					if (level > 0) 
					{
						item.addUnsafeEnchantment(enchantment, level);
						player.sendMessage("Server > Enchanted " + item.getType().toString() + " with " + enchantment.getName() + " " + level);
					}
					else
						player.sendMessage("Server > Removed " + enchantment.getName() + " from " + item.getType().toString());
				}
				else
					player.sendMessage("Server > Enchantment name is null");
			}
		}
		else
			player.sendMessage("Server > No item in hand!");
	}

	public static org.bukkit.enchantments.Enchantment GetEnchantmentFromString(String str)
	{
		switch (str.toLowerCase()) 
		{
			case "protection": 				return org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL;
			case "fireprotection": 			return org.bukkit.enchantments.Enchantment.PROTECTION_FIRE;
			case "fallprotection": 			return org.bukkit.enchantments.Enchantment.PROTECTION_FALL;
		    case "explosiveprotection": 	return org.bukkit.enchantments.Enchantment.PROTECTION_EXPLOSIONS;
			case "projectileprotection": 	return org.bukkit.enchantments.Enchantment.PROTECTION_PROJECTILE;
			case "respiration": 			return org.bukkit.enchantments.Enchantment.OXYGEN;
			case "watermining": 			return org.bukkit.enchantments.Enchantment.WATER_WORKER; //
			case "thorns": 					return org.bukkit.enchantments.Enchantment.THORNS;
			case "depthstrider": 			return org.bukkit.enchantments.Enchantment.DEPTH_STRIDER;
			case "sharpness":				return org.bukkit.enchantments.Enchantment.DAMAGE_ALL;
			case "damageundead": 			return org.bukkit.enchantments.Enchantment.DAMAGE_UNDEAD;
			case "baneofarthropods":  		return org.bukkit.enchantments.Enchantment.DAMAGE_ARTHROPODS;
			case "knockback": 				return org.bukkit.enchantments.Enchantment.KNOCKBACK;
			case "fireaspect": 				return org.bukkit.enchantments.Enchantment.FIRE_ASPECT;
			case "looting": 				return org.bukkit.enchantments.Enchantment.LOOT_BONUS_MOBS;
			case "efficiency": 				return org.bukkit.enchantments.Enchantment.DIG_SPEED;
			case "silktouch": 				return org.bukkit.enchantments.Enchantment.SILK_TOUCH;
			case "unbreaking": 				return org.bukkit.enchantments.Enchantment.DURABILITY;
			case "fortune": 				return org.bukkit.enchantments.Enchantment.LOOT_BONUS_BLOCKS;
			case "power": 					return org.bukkit.enchantments.Enchantment.ARROW_DAMAGE;
			case "punch": 					return org.bukkit.enchantments.Enchantment.ARROW_KNOCKBACK;
			case "flame": 					return org.bukkit.enchantments.Enchantment.ARROW_FIRE;
			case "infinity": 				return org.bukkit.enchantments.Enchantment.ARROW_INFINITE;
			case "luckofthesea":  			return org.bukkit.enchantments.Enchantment.LUCK;
			case "lure": 					return org.bukkit.enchantments.Enchantment.LURE;
		}
		return null;
	}

}
