package com.errol.mines;

import com.errol.*;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.*;

public class MineShop
{
	public String name;
	
	public ArrayList<MineShopBlock> blocks;
	private HashMap<String, MineShopBlock> blockMap;
	
	public MineShop(String name) 
	{
		this.name = name;
		this.blocks = new ArrayList<MineShopBlock>();
		this.blockMap = new HashMap<String, MineShopBlock>();
	}
	
	public boolean AddBlock(MineShopBlock block) 
	{
		if (blockMap.containsKey(block.material.toString()) == false) 
		{
			blocks.add(block);
			blockMap.put(block.material.toString(), block);
			return true;
		}
		return false;
	}
	
	public boolean RemoveBlock(String blockName) 
	{
		if (blockMap.containsKey(blockName) == true) 
		{
			blockMap.remove(blockName);
			for (int i = 0; i < blocks.size(); i++) 
			{
				if (blocks.get(i).Equals(blockName))
				{
					blocks.remove(i);
					break;
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean ContainsBlock(Material block) 
	{
		return blockMap.containsKey(block.toString());
	}
	
	public boolean Sell(Player player) 
	{
		if (blocks.size() > 0) 
		{
			long moneyGained = 0;
			int itemsSold = 0;
			
			PlayerInventory inventory = player.getInventory();
			int index = 0;
			for (ItemStack stack : inventory) 
			{
				if (stack != null) 
				{
					String key = stack.getType().toString();
					if (blockMap.containsKey(key)) 
					{
						MineShopBlock block = blockMap.get(key);
						int amount = stack.getAmount();
						
						long money = block.price * amount;
						moneyGained += money;
						itemsSold += amount;
						
						inventory.setItem(index, new ItemStack(Material.AIR));
					}	
				}
				index++;
			}
			
			if (moneyGained > 0) 
			{
				player.sendMessage("[Mines] Sold " + itemsSold + " items for $" + moneyGained + " to " + name + "!");
				Main.instance.moneyManager.AddMoney(player.getUniqueId(), moneyGained);	
				return true;
			}

			player.sendMessage("[Mines] You dont have any items to sell to " + name + "!");
		}
		else 
		{
			player.sendMessage("[Mines] " + name + " doesnt have a shop!");
		}

		return false;
	}
	
	public boolean SellSingleItem(Player player, Material mat) 
	{
		if (blockMap.containsKey(mat.toString())) 
		{
			MineShopBlock shopBlock = blockMap.get(mat.toString());
			
			long moneyGained = 0;
			int itemsSold = 0;
			
			PlayerInventory inventory = player.getInventory();
			for (ItemStack stack : inventory) 
			{
				if (stack != null && stack.getType() == mat) 
				{
					int amount = stack.getAmount();
					long money = shopBlock.price * amount;
					moneyGained += money;
					itemsSold += amount;
				}
			}	
			
			inventory.remove(mat);
			
			if (moneyGained > 0) 
			{
				player.sendMessage("[Mines] Sold " + itemsSold + " " + mat + " for $" + moneyGained + " to " + name + "!");
				Main.instance.moneyManager.AddMoney(player.getUniqueId(), moneyGained);	
				return true;
			}

			player.sendMessage("[Mines] You dont have any " + mat + " to sell to " + name + "!");
		}
		else
			player.sendMessage("[Mines] Error while trying to sell " + mat + " to " + name);
		
		return false;
	}
	
	public void ShowItemsToPlayer(Player player) 
	{
		Inventory inv = Bukkit.getServer().createInventory(player, 18, "Mine Shop (" + name + ")");
		for (int i = 0; i < blocks.size(); i++)
		{
			MineShopBlock shopBlock = blocks.get(i);
			ItemStack stack = new ItemStack(shopBlock.material, 1);

			ArrayList<String> lore = new ArrayList<String>();
			lore.add("Sell 1 for $" + shopBlock.price);
			lore.add("Sell 64 for $" + (shopBlock.price * 64));
			
			ItemMeta meta = stack.getItemMeta();
			meta.setLore(lore);
			stack.setItemMeta(meta);
			
			inv.setItem(i, stack);
		}
		
		player.openInventory(inv);
	}
	
	public boolean ClickItem(InventoryClickEvent event) 
	{
        Player player = (Player)event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item != null) 
        {
        	Material mat = item.getType();
        	if (blockMap.containsKey(mat.toString())) 
        		SellSingleItem(player, mat);
        }
		return false;
	}
}
