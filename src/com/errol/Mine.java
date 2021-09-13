package com.errol;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

class Mine
{	
	// publics
	public String name;
	public Vector3Int min;
	public Vector3Int max;
	public long timeBetweenResets;
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
		this.timeBetweenResets = 600;
		this.lastResetTime = Mines.seconds;
		
		this.blockChances = new ArrayList<BlockChance>();
	}
	
	public Mine(String name, Vector3Int min, Vector3Int max) 
	{
		this.name = name;
		this.min = min;
		this.max = max;
		
		this.timeBetweenResets = 600;
		this.lastResetTime = Mines.seconds;
		
		this.blockChances = new ArrayList<BlockChance>();
	}
	
	public boolean CanReset() 
	{
		if (resetting)
			return false;
		return Mines.seconds > lastResetTime + timeBetweenResets;
	}
	
	public void SetDefaultChances() 
	{
		if (blockChances != null)
			blockChances.clear();
		else
			blockChances = new ArrayList<BlockChance>();
		
		blockChances.add(new BlockChance(Material.STONE, 70));
		blockChances.add(new BlockChance(Material.COBBLESTONE, 20));
		blockChances.add(new BlockChance(Material.COAL_ORE, 10));
	}
	
	public void Reset(boolean log) 
	{
		if (blockChances.size() == 0)
		{
			Bukkit.getLogger().info("[Mines] Cant reset " + name + " as there arent any block chances");
			return;
		}
			
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
		
		if (log)
			Bukkit.getServer().broadcastMessage("[Mines] Mine " + name + " has been reset");
	}
	
	public int NumBlocks() 
	{
		int x = max.x - min.x + 1;
		int y = max.y - min.y + 1;
		int z = max.z - min.z + 1;
		
		return Math.abs(x) * Math.abs(y) * Math.abs(z);
	}
	
	public void Clear() 
	{
		World world = Bukkit.getServer().getWorlds().get(0);
		for (int x = min.x; x <= max.x; x++) 
		{
			for (int y = min.y; y <= max.y; y++) 
			{
				for (int z = min.z; z <= max.z; z++) 
				{
					Block block = world.getBlockAt(x, y, z);
					block.setType(Material.AIR);
				}
			}
		}
	}
}
