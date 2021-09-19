package com.errol;

import org.bukkit.Material;

public class MineShopBlock
{
	public Material material;
	public long price;
	
	public MineShopBlock(Material material, long price) 
	{
		this.material = material;
		this.price = price;
	}
	
	public boolean Equals(String blockName) 
	{
		return material.toString().equalsIgnoreCase(blockName);
	}
}