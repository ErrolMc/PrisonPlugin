package com.errol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

class Mine
{	
	// publics
	public String name;
	public Vector3Int min;
	public Vector3Int max;
	public long timeBetweenResets;
	public ArrayList<BlockChance> blockChances;
	public ArrayList<MineSign> signs;
	public MineShop shop;
	
	// privates
	private World world;
	private boolean resetting;
	private long lastResetTime;
	private Map<String, MineSign> signPositions;
	
	private static int timeBetweenSignResets = 60 * 1; // seconds

	public Mine(String name, World world) 
	{
		this.name = name;
		this.world = world;
		this.min = Vector3Int.Zero();
		this.max = Vector3Int.Zero();
		
		this.resetting = false;
		this.timeBetweenResets = 600;
		this.lastResetTime = Mines.seconds;
		
		this.signs = new ArrayList<MineSign>();
		this.blockChances = new ArrayList<BlockChance>();
		this.signPositions = new HashMap<String, MineSign>();
		this.shop = new MineShop(name);
	}
	
	public Mine(String name, Vector3Int min, Vector3Int max, World world) 
	{
		this.name = name;
		this.world = world;
		this.min = min;
		this.max = max;
		
		this.resetting = false;
		this.timeBetweenResets = 600;
		this.lastResetTime = Mines.seconds;
		
		this.signs = new ArrayList<MineSign>();
		this.blockChances = new ArrayList<BlockChance>();
		this.signPositions = new HashMap<String, MineSign>();
		this.shop = new MineShop(name);
	}
	
	public boolean CanReset() 
	{
		if (resetting)
			return false;
		return Mines.seconds >= lastResetTime + timeBetweenResets;
	}
	
	public boolean CanResetFromSign() 
	{
		if (resetting)
			return false;
		if (Mines.seconds < lastResetTime + timeBetweenSignResets) 
			return false;
		if (BlocksRemoved() == 0)
			return false;
		return true;
	}
	
	public long TimeUntilReset() 
	{
		return (lastResetTime + timeBetweenResets) - Mines.seconds;
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
		
		if (world == null) 
		{
			Bukkit.getLogger().info("[Mines] World isnt linked to mine " + name);
			return;
		}
			
		resetting = true;

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
		
		TickSigns();
	}
	
	public int NumBlocks() 
	{
		int x = max.x - min.x + 1;
		int y = max.y - min.y + 1;
		int z = max.z - min.z + 1;
		
		return Math.abs(x) * Math.abs(y) * Math.abs(z);
	}
	
	public boolean ClickSign(Vector3Int position, Player player, boolean right) 
	{
		String key = position.toString();
		if (signPositions.containsKey(key))
		{
			MineSign mineSign = signPositions.get(key); 
			MineSign.SignType type = mineSign.Type();

			if (right) 
			{
				if (type == MineSign.SignType.Sell) 
				{
					Sell(player);
				}
				else 
				{
					if (CanResetFromSign()) 
						Reset(true);	
					else
						player.sendMessage("[Mines] Cant reset " + name + " right now!");
				}	
			}
			else 
			{
				if (type == MineSign.SignType.Sell) 
				{
					player.sendMessage("[Mines] Showing shop for " + name + "!");
					shop.ShowItemsToPlayer(player);
				}
			}

			return true;
		}
		
		return false;
	}
	
	public int BlocksRemoved() 
	{
		int blocksRemoved = 0;
		for (int x = min.x; x <= max.x; x++) 
		{
			for (int y = min.y; y <= max.y; y++) 
			{
				for (int z = min.z; z <= max.z; z++) 
				{
					Block block = world.getBlockAt(x, y, z);
					if (block.getType() == Material.AIR)
						blocksRemoved++;
				}
			}
		}
		return blocksRemoved;
	}
	
	public double PercentMined(boolean left) 
	{
		int totalBlocks = NumBlocks();
		
		double percentage = (double)(totalBlocks - BlocksRemoved()) / (double)totalBlocks;
		percentage *= 100;
		percentage = (double)Math.round(percentage * 100d) / 100d;
		
		if (left)
			return percentage;
		return 100 - percentage;
	}
	
	public void AddSign(SignTemplate template, Sign sign)
	{
		MineSign mineSign = new MineSign(sign, template);
		signs.add(mineSign);
		signPositions.put(mineSign.Position().toString(), mineSign);
		
		if (mineSign.Type() == MineSign.SignType.Sell)
			mineSign.UpdateName(name);

		TickSigns();
	}
	
	public boolean ContainsPlayer(Vector3Int location) 
	{
		if (location.x >= min.x && location.x <= max.x && location.z >= min.z && location.z <= max.z)
			return true;
		return false;
	}
	
	public boolean ContainsSign(Vector3Int position) 
	{
		return signPositions.containsKey(position.toString());
	}
	
	public boolean DeleteSign(Vector3Int position) 
	{
		String key = position.toString();
		if (signPositions.containsKey(key))
		{
			MineSign mineSign = signPositions.get(key); 
			if (signs.contains(mineSign)) 
				signs.remove(mineSign);	
			signPositions.remove(key);
			
			return true;
		}
		return false;
	}
	
	public boolean Sell(Player player) 
	{
		return shop.Sell(player);
	}
	
	public boolean AddBlock(String blockName, long price) 
	{
		return shop.AddBlock(new MineShopBlock(Material.valueOf(blockName), price));
	}	
	
	public boolean RemoveBlock(String blockName) 
	{
		return shop.RemoveBlock(blockName);
	}	
	
	public void Clear() 
	{
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
	
	public void Tick() 
	{
		if (CanReset())
			Reset(true);
		
		TickSigns();
	}
	
	void TickSigns() 
	{
		for (MineSign sign : signs) 
		{
			MineSign.SignType type = sign.Type();
			if (type == MineSign.SignType.TimeLeft)
				sign.UpdateTime(TimeUntilReset());
			else if (type == MineSign.SignType.PercentLeft) 
				sign.UpdatePercentage(PercentMined(true));
		}
	}
	
	public void Cleanup() 
	{
		signs.clear();
		blockChances.clear();
	}
}
