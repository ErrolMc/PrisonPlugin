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
	
    @Override
    public String toString() 
    {
        return String.format(x + "," + y + "," + z);
    }
}

class Mine
{
	public String name;
	public ArrayList<Vector3Int> blocks;
	
	public Mine(String name) 
	{
		this.name = name;
		this.blocks = null;
	}
	
	public Mine(String name, ArrayList<Vector3Int> blocks) 
	{
		this.name = name;
		this.blocks = blocks;
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
        		Mine mine = new Mine(config.getString("m" + i + "name"));
        		
            	int numBlocks = config.getInt("m" + i + "numblocks");
            	mine.blocks = new ArrayList<Vector3Int>();
            	
            	for (int j = 0; j < numBlocks; j++)
        			mine.blocks.add(new Vector3Int(config.getString("m" + i + "b" + j)));
            	
            	mines.add(mine);
        	}	
        	

        	Bukkit.getLogger().info("nummines " + numMines);
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
        	config.set("m" + i + "numblocks", mine.blocks.size());
        	
        	for (int j = 0; j < mine.blocks.size(); j++)
        		config.set("m" + i + "b" + j, mine.blocks.get(i).toString());
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

		Bukkit.getLogger().info("blocks " + mine.blocks.size());
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
							Location min = selection.getMinimumPoint();
							Location max = selection.getMaximumPoint();

							player.sendMessage(min.toString() + " + " + max.toString());
							
							ArrayList<Vector3Int> blocks = new ArrayList<Vector3Int>();
							for (int x = min.getBlockX(); x <= max.getBlockX(); x++) 
							{
								for (int y = min.getBlockY(); y <= max.getBlockY(); y++) 
								{
									for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) 
									{
										Block block = world.getBlockAt(x, y, z);
										Material mat = block.getType();
										
										if (mat == Material.GOLD_BLOCK) 
											blocks.add(new Vector3Int(x, y, z));
									}
								}
							}

							Mine mine = new Mine(name, blocks);
							mine.blocks = blocks;
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
					res.AddLine(mine.name + ": " + mine.blocks.size() + " blocks");
				}
				
				res.Add("-------------");
				
				player.sendMessage(res.Text());
			}
			// define mine with gold blocks and worldedit
			// mine with hard coded values
			// /sell
			// resets on timer or %
			// /mine reset in region or /mine reset a
		}
	}
	
	/*
	Mines GetMinesFromDisk() 
	{
		try
		{
			File logger = new File("/plugins/Prison/mines.txt");
			if (logger.exists())
			{
	            FileReader fr = new FileReader(logger.getAbsoluteFile());
	            BufferedReader br = new BufferedReader(fr);
	            
	            StringBuilder content = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) 
	            {
	                content.append(line);
	                content.append(System.lineSeparator());
	            }
	            
	            br.close();
	            String json = content.toString();
	                                  
	            ObjectMapper mapper = new ObjectMapper();
	    		try
	    		{
	    			Mines _mines = mapper.readValue(json, Mines.class);
	    			return _mines;
	    		}
	    		catch (JsonProcessingException e) 
	    		{
	    			return new Mines();
	    		}
			}
		} 
		catch (IOException e) 
		{
			return new Mines();
		}
		
		return new Mines();
	}
	
	boolean WriteMinesToDisk(Mines _mines) 
	{
        File dir = new File("plugins/Prison");
        if (!dir.exists())
        	dir.mkdir();
        
        ObjectMapper mapper = new ObjectMapper();
		try
		{
			String json = mapper.writeValueAsString(_mines);
			
	        try
	        {
	            File logger = new File("/plugins/Prison/mines.txt");
	            if (!logger.exists())
	                logger.createNewFile();

	            FileWriter fw = new FileWriter(logger.getAbsoluteFile());
	            BufferedWriter bw = new BufferedWriter(fw);
	            
	            bw.write(json);
	            bw.close();
	            
	            return true;
	        } 
	        catch (IOException e) 
	        {
	        	return false;
	        }
		}
		catch (JsonProcessingException e)
		{
			return false;
		}
	}
	*/
}
