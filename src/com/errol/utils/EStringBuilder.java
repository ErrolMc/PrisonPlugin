package com.errol.utils;

public class EStringBuilder
{
	String text;

	public EStringBuilder() 
	{
		text = "";
	}
	
	public void AddLine(String line) 
	{
		if (text == null)
			text = "";
		text += line + "\n";
	}
	
	public void Add(String text) 
	{
		if (this.text == null)
			this.text = "";
		this.text += text;
	}
	
	public String Text() { return text; }
}
