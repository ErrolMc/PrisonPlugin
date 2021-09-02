package com.errol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;

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
}

class Mines
{
	public Mine[] mines;
	
	public void AddMine(Mine mine) 
	{
		Mine[] newMines = new Mine[mines.length + 1];
		newMines[mines.length] = mine;
		for (int i = 0; i < mines.length; i++)
			newMines[i] = mines[i];
		mines = newMines;
	}
}

class Mine
{
	public String name;
	public Vector3Int[] blocks;
	
	public Mine(String name, Vector3Int[] blocks) 
	{
		this.name = name;
		this.blocks = blocks;
	}
	
	public Mine(String name, ArrayList<Vector3Int> blocksList) 
	{
		this.name = name;
		blocks = new Vector3Int[blocksList.size()];
		for (int i = 0; i < blocks.length; i++) 
			blocks[i] = blocksList.get(i);
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
		Bukkit.getLogger().info("Minemanager 5");
		//worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		//mines = GetMinesFromDisk();
	}
	
	public void HandleCommand(Player player, String[] args) 
	{
		if (args.length > 0) 
		{
			if (args[0] == "create") 
			{
				if (args.length > 0) 
				{
					String name = args[1];
					
					Selection selection = worldEdit.getSelection(player);
					if (selection != null) 
					{
						World world = selection.getWorld();
						Location min = selection.getMinimumPoint();
						Location max = selection.getMaximumPoint();
						
						ArrayList<Vector3Int> blocks = new ArrayList<Vector3Int>();
						for (int x = min.getBlockX(); x < max.getBlockX(); x++) 
						{
							for (int y = min.getBlockY(); y < max.getBlockY(); x++) 
							{
								for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) 
								{
									Block block = world.getBlockAt(x, y, z);
									Material mat = block.getType();
									
									if (mat == Material.GOLD_BLOCK) 
									{
										blocks.add(new Vector3Int(x, y, z));
									}
								}
							}
						}
						
						Mine mine = new Mine(name, blocks);
						mines.AddMine(mine);
						
						boolean res = WriteMinesToDisk(mines);
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
			}
			// define mine with gold blocks and worldedit
			// mine with hard coded values
			// /sell
			// resets on timer or %
			// /mine reset in region or /mine reset a
		}
	}
	
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
}
