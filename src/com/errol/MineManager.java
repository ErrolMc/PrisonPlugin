package com.errol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.sk89q.worldedit.bukkit.*;
import com.sk89q.worldedit.bukkit.selections.*;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;

import org.bukkit.Location;

class Vector3Int
{
	public int x;
	public int y;
	public int z;
	
	public Vector3Int(int x, int y, int z) 
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3Int(String str) 
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
		
		String[] arr = str.split(",");
		if (arr.length > 0) 
			this.x = StaticUtils.ParseInt(arr[0], 0);
		if (arr.length > 1) 
			this.y = StaticUtils.ParseInt(arr[1], 0);
		if (arr.length > 2) 
			this.z = StaticUtils.ParseInt(arr[2], 0);
	}
	
	public Vector3Int(Location loc) 
	{
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
	}
	
    @Override
    public String toString() 
    {
        return String.format(x + "," + y + "," + z);
    }
    
    public static Vector3Int Zero()
    { 
		return new Vector3Int(0, 0, 0);
    }
}

class Mine
{
	public String name;
	public Vector3Int min;
	public Vector3Int max;
	
	public Mine(String name) 
	{
		this.name = name;
		this.min = Vector3Int.Zero();
		this.max = Vector3Int.Zero();
	}
	
	public Mine(String name, Vector3Int min, Vector3Int max) 
	{
		this.name = name;
		this.min = min;
		this.max = max;
	}
	
	public int NumBlocks() 
	{
		int x = max.x - min.x + 1;
		int y = max.y - min.y + 1;
		int z = max.z - min.z + 1;
		
		return Math.abs(x) * Math.abs(y) * Math.abs(z);
	}
}

class Mines
{
	public ArrayList<Mine> mines;
	
    public Mines()
    {
    	Bukkit.getLogger().info("Mines");
    	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("mines.yml"));
    	mines = new ArrayList<Mine>();
    	
    	if (config.contains("numMines")) 
    	{
        	int numMines = config.getInt("numMines");
        	
        	for (int i = 0; i < numMines; i++) 
        	{
        		String name = config.getString("m" + i + "name");
        		Vector3Int min = new Vector3Int(config.getString("m" + i + "min"));
        		Vector3Int max = new Vector3Int(config.getString("m" + i + "max"));

            	mines.add(new Mine(name, min, max));
        	}	
    	}
    }
    
    public boolean SaveToDisk()
    {
    	YamlConfiguration config = new YamlConfiguration();
    	
    	config.set("numMines", mines.size());
    	for (int i = 0; i < mines.size(); i++) 
    	{
    		Mine mine = mines.get(i);
    		
    		config.set("m" + i + "name", mine.name);
        	config.set("m" + i + "min", mine.min.toString());
        	config.set("m" + i + "max", mine.max.toString());
    	}
    	
    	File file = new File("mines.yml");
    	
        try
        {
        	config.save(file);
        	return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
    
    public boolean ContainsMine(String name) 
    {
    	for (Mine mine : mines) 
    	{
    		if (mine.name.equalsIgnoreCase(name))
    			return true;
    	}
    	return false;
    }
    
	public boolean AddMine(Mine mine) 
	{
		if (ContainsMine(mine.name)) 
			return false;

		Bukkit.getLogger().info("blocks " + mine.NumBlocks());
		mines.add(mine);
		return true;
	}
}

public class MineManager
{
	private Main plugin;
	private WorldEditPlugin worldEdit;
	
	private Mines mines;
	
	public MineManager(Main plugin) 
	{
		this.plugin = plugin;
		worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		mines = new Mines();
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
							World world = selection.getWorld();
							Location minLoc = selection.getMinimumPoint();
							Location maxLoc = selection.getMaximumPoint();

							Vector3Int min = new Vector3Int(minLoc);
							Vector3Int max = new Vector3Int(maxLoc);

							Mine mine = new Mine(name, min, max);
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
				mines = new Mines();
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
			
			// mine with hard coded values
			// /sell
			// resets on timer or %
			// /mine reset in region or /mine reset a
		}
	}
}
