package com.errol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

class Mines
{
	private Main plugin;
	
	public ArrayList<Mine> mines;
	public ArrayList<SignTemplate> templateSigns;
	
	public static long seconds = 0;
	private int checkInterval = 5; // seconds
	
	private BukkitScheduler scheduler;

    public Mines(Main plugin, World world)
    {
    	this.plugin = plugin;
    	
    	GetSignDataFromDisk();
    	GetMinesFromDisk(world);

    	// update the mines every second
    	scheduler = Bukkit.getServer().getScheduler();
    	scheduler.runTaskTimer(plugin, new Runnable() 
    	{
			@Override
			public void run()
			{
				seconds += checkInterval;
				Update();
			}
    	}, 0, 20 * checkInterval);
    	
    	ResetAll();
    }
    
    void GetSignDataFromDisk() 
    {
    	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("signs.yml"));
    	
    	templateSigns = new ArrayList<SignTemplate>();
    	if (config.contains("PercentMined"))
    		templateSigns.add(new SignTemplate(config.getConfigurationSection("PercentMined"), MineSign.SignType.PercentMined));
    	if (config.contains("PercentLeft"))
    		templateSigns.add(new SignTemplate(config.getConfigurationSection("PercentLeft"), MineSign.SignType.PercentLeft));
    	if (config.contains("BlocksMined"))
    		templateSigns.add(new SignTemplate(config.getConfigurationSection("BlocksMined"), MineSign.SignType.BlocksMined));
    	if (config.contains("TimeLeft"))
    		templateSigns.add(new SignTemplate(config.getConfigurationSection("TimeLeft"), MineSign.SignType.TimeLeft));
    }
    
    void GetMinesFromDisk(World world) 
    {
    	Bukkit.getLogger().info("[Mines] Getting mines from disk in world " + world.getName());
    	mines = new ArrayList<Mine>();
    	
    	YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("mines.yml"));
    	
    	int i = 0;
    	while (true)
    	{
    		if (config.contains("m"+i)) 
    		{
        		ConfigurationSection section = config.getConfigurationSection("m" + i);
        		
        		String name = section.getString("name");
        		Vector3Int min = new Vector3Int(section.getString("min"));
        		Vector3Int max = new Vector3Int(section.getString("max"));
        		
        		Mine mine = new Mine(name, min, max, world);
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
        		
        		if (section.contains("shop"))
        		{
        			ConfigurationSection shopSection = section.getConfigurationSection("shop");
        			ConfigurationSection blocks = shopSection.getConfigurationSection("blocks");
        			ConfigurationSection prices = shopSection.getConfigurationSection("prices");
        			
            		int j = 0;
            		while (true) 
            		{
            			if (blocks.contains(""+j)) 
            			{
                			String blockName = blocks.getString(""+j);
                			long price = prices.getLong(""+j);
                			
                			MineShopBlock block = new MineShopBlock(Material.valueOf(blockName), price);
                			mine.shop.AddBlock(block);
                			
                			j++;
            			}
            			else
            				break;
            		}
        		}
        		
        		if (section.contains("signs"))
        		{
        			ConfigurationSection signs = section.getConfigurationSection("signs");
        			
        			int j = 0;
        			while (true)
        			{
        				if (signs.contains("sign" + j)) 
        				{
            				ConfigurationSection signSection = signs.getConfigurationSection("sign" + j);
            				
            				Vector3Int position = new Vector3Int(signSection.getString("position"));
            				MineSign.SignType type = MineSign.SignType.valueOf(signSection.getString("type"));
            				
            				Block block = world.getBlockAt(position.x, position.y, position.z);
            				if (StaticUtils.IsSign(block.getType())) 
            				{
            					Sign sign = (Sign)block.getState();
            					mine.AddSign(GetSignTemplate(type), sign);
            				}
            				else
            					Bukkit.getLogger().info("[Mines] Cant find sign " + j + " for mine " +  mine.name);

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
    		
    		if (mine.shop.blocks.size() > 0)
    		{
    			ConfigurationSection shopSection = section.createSection("shop");
    			ConfigurationSection blocks = shopSection.createSection("blocks");
    			ConfigurationSection prices = shopSection.createSection("prices");
    			
    			MineShop shop = mine.shop;
    			for (int j = 0; j < shop.blocks.size(); j++) 
    			{
    				MineShopBlock block = shop.blocks.get(j);
        			blocks.set(""+j, block.material.toString());
        			prices.set(""+j, block.price);
    			}
    		}
    		
    		{
    			ConfigurationSection signs = section.createSection("signs");
    			for (int j = 0; j < mine.signs.size(); j++) 
    			{
    				ConfigurationSection signSection = signs.createSection("sign" + j);
    				
    				MineSign sign = mine.signs.get(j);

    				signSection.set("position", sign.Position().toString());
    				signSection.set("type", sign.Type().toString());
    			}
    		}
    	}
  
    	// mine file
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
	
	public boolean SellMine(String name, Player player) 
	{
		for (Mine mine : mines) 
		{
			if (mine.name.equalsIgnoreCase(name)) 
				return mine.Sell(player);
		}
		return false;
	}
	
	public boolean AddBlock(String mineName, String blockName, String priceStr, Player player) 
	{
		long price = StaticUtils.ParseLong(priceStr);
		if (price > 0) 
		{
			for (Mine mine : mines) 
			{
				if (mine.name.equalsIgnoreCase(mineName)) 
				{
					if (mine.AddBlock(blockName, price)) 
					{
						SaveToDisk();
						return true;
					}
					player.sendMessage("[Mines] " + blockName + " already exists in " + mineName + "!");
					return false;
				}
			}
		}
		player.sendMessage("[Mines] Price is < 0");
		return false;
	}
	
	public boolean RemoveBlock(String mineName, String block) 
	{
		for (Mine mine : mines) 
		{
			if (mine.name.equalsIgnoreCase(mineName)) 
			{
				if (mine.RemoveBlock(block)) 
				{
					SaveToDisk();
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean AddSignToMine(String mineName, MineSign.SignType signType, Sign sign) 
	{
		for (Mine mine : mines) 
		{
			if (mine.name.equalsIgnoreCase(mineName)) 
			{
				mine.AddSign(GetSignTemplate(signType), sign);
				return true;
			}
		}
		return false;
	}
	
	public boolean SignExists(Vector3Int position) 
	{
		for (Mine mine : mines) 
		{
			if (mine.ContainsSign(position)) 
				return true;
		}
		return false;
	}
	
	public boolean DeleteSign(Vector3Int position) 
	{
		for (Mine mine : mines) 
		{
			if (mine.ContainsSign(position)) 
			{
				return mine.DeleteSign(position);	
			}
		}
		return false;
	}
	
	public boolean RightClickSign(Vector3Int position) 
	{
		for (Mine mine : mines) 
		{
			if (mine.ContainsSign(position)) 
			{
				return mine.RightClickSign(position);	
			}
		}
		return false;
	}
	
	public String GetMineFromPlayer(Player player) 
	{
		for (Mine mine : mines) 
		{
			if (mine.ContainsPlayer(player)) 
				return mine.name;
		}
		return "";
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
    	
		for (Mine mine : mines) 
			mine.Cleanup();
    	mines.clear();

    	Bukkit.getLogger().info("[Mines] Performing clean up!");
    }
    
    public SignTemplate GetSignTemplate(MineSign.SignType type) 
    {
    	for (SignTemplate template : templateSigns) 
    	{
    		if (template.type == type)
    			return template;
    	}
    	return new SignTemplate();
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
			mine.Tick();
		}
	}
}

