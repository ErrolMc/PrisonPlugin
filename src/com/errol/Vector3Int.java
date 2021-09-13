package com.errol;

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