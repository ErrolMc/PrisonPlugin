package com.errol;

import org.bukkit.Material;

public class StaticUtils
{
	public static int ParseInt(String str) 
	{
		try 
		{
			int num = Integer.parseInt(str);
			return num;
		}
		catch (NumberFormatException ex) 
		{ 
			// not a number
			return -1;
		}
	}
	
	public static int ParseInt(String str, int _default) 
	{
		int num = ParseInt(str);
		if (num == -1)
			return _default;
		return num;
	}
	
	public static long ParseLong(String str) 
	{
		try 
		{
			long num = Long.parseLong(str);
			return num;
		}
		catch (NumberFormatException ex) 
		{ 
			// not a number
		}
		return -1;
	}
	
	public static Boolean IsSign(Material material) 
	{
		return material == Material.SIGN || material == Material.SIGN_POST || material == Material.WALL_SIGN;
	}
}
