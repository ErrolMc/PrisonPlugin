package com.errol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

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
import java.time.Instant;
import java.time.LocalDateTime;
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
	class BlockChance implements Comparable<BlockChance>
	{
		public Material block;
		public float chance;
		
		public BlockChance(Material block, float chance) 
		{
			this.block = block;
			this.chance = chance;
		}

		@Override
		public int compareTo(BlockChance other)
		{
			if (chance == other.chance)
				return 0;
			if (chance > other.chance)
				return 1;
			return -1;
		}
	}
	
	// publics
	public String name;
	public Vector3Int min;
	public Vector3Int max;
	public long timeBetweenResets = 20;
	public ArrayList<BlockChance> blockChances;
	
	// privates
	private boolean resetting;
	private long lastResetTime;

	public Mine(String name) 
	{
		this.name = name;
		this.min = Vector3Int.Zero();
		this.max = Vector3Int.Zero();
		
		this.resetting = false;
		this.lastResetTime = Mines.seconds;
		InitChances();
	}
	
	void InitChances() 
	{
		blockChances = new ArrayList<BlockChance>();
		blockChances.add(new BlockChance(Material.STONE, 0.85f));
		blockChances.add(new BlockChance(Material.COBBLESTONE, 0.05f));
		blockChances.add(new BlockChance(Material.COAL_ORE, 0.10f));
	}
	
	public Mine(String name, Vector3Int min, Vector3Int max) 
	{
		this.name = name;
		this.min = min;
		this.max = max;
		
		lastResetTime = Mines.seconds;
		InitChances();
	}
	
	public boolean CanReset() 
	{
		if (resetting)
			return false;
		return Mines.seconds > lastResetTime + timeBetweenResets;
	}
	
	public void Reset() 
	{
		resetting = true;
		World world = Bukkit.getServer().getWorlds().get(0);

		int totalBlocks = NumBlocks();
		int blocksLeft = totalBlocks;
		ArrayList<Material> blocks = new ArrayList<Material>();
		
		Collections.sort(blockChances);
		
		int b = 0;
		for (int i = 0; i < blockChances.size(); i++) 
		{
			BlockChance blockChance = blockChances.get(i);
			int cur = (int)Math.ceil(totalBlocks * blockChance.chance);
			
			if (cur > blocksLeft)
				cur = blocksLeft;
			
			for (int numToSet = b + cur; b < numToSet; b++) 
				blocks.add(blockChance.block);
		}
		
		Collections.shuffle(blocks);

		b = 0;
		for (int x = min.x; x <= max.x; x++) 
		{
			for (int y = min.y; y <= max.y; y++) 
			{
				for (int z = min.z; z <= max.z; z++) 
				{
					Block block = world.getBlockAt(x, y, z);
					block.setType(blocks.get(b));
					b++;
				}
			}
		}
		
		lastResetTime = Mines.seconds;
		resetting = false;
		
		Bukkit.getServer().broadcastMessage("[Mines] Mine " + name + " has been reset");
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
	private Main plugin;
	
	public ArrayList<Mine> mines;
	
	public static long seconds = 0;
	private int checkInterval = 1;
	
	private BukkitScheduler scheduler;
	
    public Mines(Main plugin)
    {
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

    	// update the mines every second
    	scheduler = Bukkit.getServer().getScheduler();
    	scheduler.runTaskTimer(plugin, new Runnable() 
    	{
			@Override
			public void run()
			{
				seconds++;
				Update();
			}
    	}, 0, 20 * checkInterval);
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
	
	public boolean ResetMine(String name) 
	{
		for (Mine mine : mines) 
		{
			if (mine.name.equalsIgnoreCase(name)) 
			{
				mine.Reset();
				return true;
			}
		}
		return false;
	}

	public void Update()
	{
		for (Mine mine : mines) 
		{
			if (mine.CanReset()) 
			{
				mine.Reset();
			}
		}
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
				mines = new Mines(plugin);
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
			// mine with hard coded values
			// /sell
			// resets on timer or %
			// /mine reset in region or /mine reset a
		}
	}
}
