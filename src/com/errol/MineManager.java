package com.errol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;

import org.bukkit.Location;

public class MineManager
{
	private Main plugin;
	private WorldEditPlugin worldEdit;
	
	private Mines mines;
	
	public MineManager(Main plugin) 
	{
		this.plugin = plugin;
		worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		mines = new Mines(plugin);
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

							Mine mine = new Mine(name, min, max);
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
				mines = new Mines(plugin);
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
			
			// /sell
			// resets on %
			// /mine reset in region
		}
	}
}
