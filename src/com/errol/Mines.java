package com.errol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

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
    	
    	int i = 0;
    	while (true)
    	{
    		if (config.contains("m"+i)) 
    		{
        		ConfigurationSection section = config.getConfigurationSection("m" + i);
        		
        		String name = section.getString("name");
        		Vector3Int min = new Vector3Int(section.getString("min"));
        		Vector3Int max = new Vector3Int(section.getString("max"));
        		
        		Mine mine = new Mine(name, min, max);
        		mine.timeBetweenResets = section.getLong("timebetweenresets");
        		
        		{
            		ConfigurationSection blocks = section.getConfigurationSection("blocks");
            		ConfigurationSection keys = blocks.getConfigurationSection("keys");
            		ConfigurationSection values = blocks.getConfigurationSection("values");
            		
            		int j = 0;
            		while (true) 
            		{
            			if (keys.contains(""+j)) 
            			{
                			String blockName = keys.getString(""+j);
                			double chance = values.getDouble(""+j);
                			
                			BlockChance blockChance = new BlockChance(Material.valueOf(blockName), chance);
                			mine.blockChances.add(blockChance);
                			
                			j++;
            			}
            			else
            				break;
            		}
        		}	
        		
            	mines.add(mine);
            	i++;
    		}
    		else
    			break;
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
    	
    	ResetAll();
    }
    
    public boolean SaveToDisk()
    {
    	YamlConfiguration config = new YamlConfiguration();
    	
    	for (int i = 0; i < mines.size(); i++) 
    	{
    		Mine mine = mines.get(i);
    		
    		ConfigurationSection section = config.createSection("m" + i);
    		section.set("name", mine.name);
    		section.set("min", mine.min.toString());
    		section.set("max", mine.max.toString());
    		section.set("timebetweenresets", mine.timeBetweenResets);
    		
    		{
        		ConfigurationSection blocks = section.createSection("blocks");
        		ConfigurationSection keys = blocks.createSection("keys");
        		ConfigurationSection values = blocks.createSection("values");
        		
        		for (int j = 0; j < mine.blockChances.size(); j++) 
        		{
        			BlockChance blockChance = mine.blockChances.get(j);
        			keys.set(""+j, blockChance.block.toString());
        			values.set(""+j, blockChance.chance);
        		}
    		}
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

		mines.add(mine);
		return true;
	}
	
	public boolean ResetMine(String name) 
	{
		for (Mine mine : mines) 
		{
			if (mine.name.equalsIgnoreCase(name)) 
			{
				mine.Reset(true);
				return true;
			}
		}
		return false;
	}
	
	public void ResetAll() 
	{
		for (Mine mine : mines) 
		{
			mine.Reset(false);	
		}
	}
	
    public void Cleanup() 
    {
    	scheduler.cancelAllTasks();
    	mines.clear();
    	
    	Bukkit.getLogger().info("[Mines] Performing clean up!");
    }
    
    public boolean DeleteMine(String name) 
    {
		for (int i = 0; i < mines.size(); i++) 
		{
			Mine mine = mines.get(i);
			if (mine.name.equalsIgnoreCase(name)) 
			{
				mine.Clear();
				mines.remove(i);
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
				mine.Reset(true);
			}
		}
	}
}
