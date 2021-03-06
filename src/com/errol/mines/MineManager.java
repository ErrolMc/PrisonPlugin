package com.errol.mines;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.errol.Main;
import com.errol.utils.*;
import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;

import org.bukkit.Location;
import org.bukkit.Server;

public class MineManager
{
	private Main plugin;
	private WorldEditPlugin worldEdit;
	
	private World world;
	private Mines mines;
	
	public MineManager(Main plugin) 
	{
		Server server = Bukkit.getServer();
		this.plugin = plugin;
		
		world = server.getWorlds().get(0);
		worldEdit = (WorldEditPlugin)server.getPluginManager().getPlugin("WorldEdit");
		mines = new Mines(plugin, world);
	}
	
	public void HandleCommand(Player player, String[] args) 
	{
		if (args.length > 0) 
		{
			if (args[0].equalsIgnoreCase("create"))
			{
				if (args.length > 1) 
				{
					String name = args[1];

					if (!mines.ContainsMine(name)) 
					{
						Selection selection = worldEdit.getSelection(player);
						if (selection != null) 
						{
							Location minLoc = selection.getMinimumPoint();
							Location maxLoc = selection.getMaximumPoint();

							Vector3Int min = new Vector3Int(minLoc);
							Vector3Int max = new Vector3Int(maxLoc);

							Mine mine = new Mine(name, min, max, world);
							mine.SetDefaultChances();
							mine.Reset(false);
							mines.AddMine(mine);

							boolean res = mines.SaveToDisk();
							if (res)
								player.sendMessage("Server > Successfully created new mine " + name);
							else
								player.sendMessage("Server > Failed to save mine " + name);
						} 
						else 
						{
							player.sendMessage("Please have a selection");
						}	
					}
					else 
					{
						player.sendMessage("Mine " + name + " already exists!");
					}
				}
			}
			
			if (args[0].equalsIgnoreCase("reload"))
			{
				if (mines != null)
					mines.Cleanup();
				mines = new Mines(plugin, world);
				mines.ResetAll();
				player.sendMessage("Reloaded mines! (" + mines.mines.size() + ")");
			}
			
			if (args[0].equalsIgnoreCase("mines"))
			{
				int numMines = mines.mines.size();
				
				EStringBuilder res = new EStringBuilder();
				res.AddLine("--- Mines ---");
				res.AddLine("Amount: " + numMines);
				
				for (int i = 0; i < numMines; i++) 
				{
					Mine mine = mines.mines.get(i);
					res.AddLine(mine.name + ": " + mine.NumBlocks() + " blocks");
				}
				
				res.Add("-------------");
				
				player.sendMessage(res.Text());
			}
			
			if (args[0].equalsIgnoreCase("reset"))
			{
				if (args.length > 1) 
				{
					String name = args[1];
					if (mines.ResetMine(name))
						player.sendMessage("Resetting " + name + " mine!");
					else
						player.sendMessage("Mine " + name + " doesnt exist!");
				}
				else
					player.sendMessage("Please enter a mine to reset!");
			}
			
			if (args[0].equalsIgnoreCase("resetall"))
			{
				mines.ResetAll();
				player.sendMessage("Resetting all mines! (" + mines.mines.size() + ")");
			}
			
			if (args[0].equalsIgnoreCase("delete"))
			{
				if (args.length > 1) 
				{
					String name = args[1];
					if (mines.DeleteMine(name)) 
					{
						mines.SaveToDisk();
						player.sendMessage("Deleting " + name + " mine!");
					}
					else
						player.sendMessage("Mine " + name + " doesnt exist!");
				}
				else
					player.sendMessage("Please enter a mine to delete!");
			}
			
			if (args[0].equalsIgnoreCase("addshopblock"))
			{
				if (args.length > 3) 
				{
					String name = args[1];
					String block = args[2];
					String price = args[3];
					
					if (mines.AddBlock(name, block, price, player))
						player.sendMessage("Added " + block + " to " + name + " for $" + price);
				}
				else
					player.sendMessage("Please enter a mine, block, price!");
			}
			
			if (args[0].equalsIgnoreCase("removeshopblock"))
			{
				if (args.length > 2) 
				{
					String name = args[1];
					String block = args[2];
					
					if (mines.RemoveBlock(name, block))
						player.sendMessage("Removed " + block + " from " + name + "!");
					else
						player.sendMessage("Failed to remove " + block + " from " + name + "!");
				}
				else
					player.sendMessage("Please enter a mine, block!");
			}
			
			if (args[0].equalsIgnoreCase("addsign")) 
			{
				@SuppressWarnings("deprecation")
				Block block = player.getTargetBlock((HashSet<Byte>) null, 100);
				
				if (StaticUtils.IsSign(block.getType())) 
				{
					if (!mines.SignExists(new Vector3Int(block.getLocation()))) 
					{
						Sign sign = (Sign)block.getState();
						
						if (args.length > 2) 
						{
							String type = args[1];
							String name = args[2];
							if (type.equalsIgnoreCase("sell")) 
							{
								player.sendMessage("sell");
								if (mines.AddSignToMine(name, MineSign.SignType.Sell, sign)) 
								{
									mines.SaveToDisk();	
									player.sendMessage("[Mines] Added " + type + " sign to " + name);
								}
								else 
									player.sendMessage("[Mines] Couldnt add " + type + " sign for some reason");
							}
							else if (type.equalsIgnoreCase("percent")) 
							{
								if (mines.AddSignToMine(name, MineSign.SignType.PercentLeft, sign)) 
								{
									mines.SaveToDisk();	
									player.sendMessage("[Mines] Added " + type + " sign to " + name);
								}
								else 
									player.sendMessage("[Mines] Couldnt add " + type + " sign for some reason");
							}
							else if (type.equalsIgnoreCase("time")) 
							{
								if (mines.AddSignToMine(name, MineSign.SignType.TimeLeft, sign)) 
								{
									mines.SaveToDisk();	
									player.sendMessage("[Mines] Added " + type + " sign to " + name);
								}
								else 
									player.sendMessage("[Mines] Couldnt add " + type + " sign for some reason");
							}
							else
								player.sendMessage("[Mines] Couldnt find type " + type);
						}
						else
							player.sendMessage("Please enter a sign type and mine name");
					}
					else 
						player.sendMessage("[Mines] Sign already exists here");
				}
				else
					player.sendMessage("Please look at a sign");
			}
			// /sell
			// resets on %
			// /mine reset in region
		}
	}
	
	public boolean DeleteSign(Vector3Int position) 
	{
		if (mines.DeleteSign(position)) 
		{
			mines.SaveToDisk();
			return true;
		}
		return false;
	}
	
	public boolean ClickSign(Vector3Int position, Player player, boolean right) 
	{
		return mines.ClickSign(position, player, right);
	}
	
	public boolean ClickShopItem(String mineName, InventoryClickEvent event) 
	{
		return mines.ClickShopItem(mineName, event);
	}
	
	public void HandleSell(Player player, String commandName, String[] args) 
	{
		if (commandName.equalsIgnoreCase("sell"))
		{
			if (args.length > 0) 
			{
				String name = args[0];
				mines.SellMine(name, player);
			}
			else 
			{
				String name = mines.GetMineFromPlayer(player);
				if (name.isEmpty() == false)
					mines.SellMine(name, player);
				else
					player.sendMessage("[Mines] You are not standing in the bounds of a mine!");
			}
		}
	}
}
