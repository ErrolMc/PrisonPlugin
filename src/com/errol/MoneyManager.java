package com.errol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MoneyManager
{
	private Main plugin;
	
	public MoneyManager(Main plugin) 
	{
		this.plugin = plugin;
	}
	
	public long AddMoney(UUID uuid, long money) 
	{
		return SetMoney(uuid, GetMoney(uuid) + money);
	}
	
	public long TakeMoney(UUID uuid, long money) 
	{
		return SetMoney(uuid, GetMoney(uuid) - money);
	}
	
	public long SetMoney(UUID uuid, long money) 
	{
		try 
		{
			if (money < 0)
				money = 0;
				
			String query = "UPDATE playerdata SET MONEY=? WHERE UUID=?";
			
			PreparedStatement ps = plugin.SQL.GetConnection().prepareStatement(query);
			ps.setLong(1, money);
			ps.setString(2, uuid.toString());

			ps.executeUpdate();
			
			return money;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public long GetMoney(UUID uuid) 
	{
		try 
		{
			String query = "SELECT MONEY FROM playerdata WHERE UUID=?";
			
			PreparedStatement ps = plugin.SQL.GetConnection().prepareStatement(query);
			ps.setString(1, uuid.toString());
			
			ResultSet results = ps.executeQuery();
			if (results.next())
				return results.getInt("MONEY");
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return 0;
	}
}
