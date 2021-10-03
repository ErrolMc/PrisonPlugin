package com.errol.mines;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

class SignTemplate
{
	MineSign.SignType type;
	String[] lines;
	
	public SignTemplate() 
	{
		lines = new String[4];
		for (int i = 0; i < 4; i++) 
			lines[i] = "";
	}
	
	public SignTemplate(ConfigurationSection section, MineSign.SignType type) 
	{
		this.type = type;
		
		lines = new String[4];
		for (int i = 0; i < 4; i++) 
		{
			lines[i] = "";
			if (section.contains(""+i))
				lines[i] = section.getString(""+i);
		}
	}
	
	public void UpdateSign(Sign sign, String key, String newText) 
	{
		for (int i = 0; i < 4; i++)
		{
			String line = new String(lines[i]);
			if (line.contains(key)) 
				line = line.replace(key, newText);	
			sign.setLine(i, ChatColor.translateAlternateColorCodes('&', line));			
		}	
		
		sign.update();
	}
}