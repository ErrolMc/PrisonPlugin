package com.errol.mines;

import org.bukkit.block.Sign;
import com.errol.utils.Vector3Int;

public class MineSign
{	
	public enum SignType
	{
		PercentMined,
		PercentLeft,
		BlocksMined,
		TimeLeft,
		Sell
	}
	
	private Sign sign;
	private SignTemplate template;
	
	public MineSign(Sign sign, SignTemplate template) 
	{
		this.sign = sign;
		this.template = template;
	}
	
	public void UpdateName(String name) 
	{
		template.UpdateSign(sign, "NAME", name);
	}
	
	public void UpdatePercentage(double percentage) 
	{
		template.UpdateSign(sign, "PERCENT", percentage + "");
	}
	
	public void UpdateTime(long secondsLeft) 
	{
		long hours = secondsLeft / 3600;
		secondsLeft -= hours * 3600;
		long minutes = secondsLeft / 60;
		secondsLeft -= minutes * 60;
		
		String text = "";
		if (hours != 0)
			text += hours + "h";
		if (minutes != 0)
			text += minutes + "m";
		text += secondsLeft + "s";
		
		template.UpdateSign(sign, "TIME", text + "");
	}
	
	public SignType Type() 
	{
		return template.type;
	}
	
	public Vector3Int Position() 
	{
		return new Vector3Int(sign.getBlock().getLocation());
	}
}