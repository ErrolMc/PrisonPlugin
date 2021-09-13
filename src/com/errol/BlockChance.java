package com.errol;

import org.bukkit.Material;

class BlockChance implements Comparable<BlockChance>
{
	public Material block;
	public double chance;
	
	public BlockChance(Material block, double chance) 
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
