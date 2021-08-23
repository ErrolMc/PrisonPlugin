package com.errol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.entity.Player;

public class SQLGetter
{
	private Main plugin;

	public SQLGetter(Main plugin) 
	{
		this.plugin = plugin;
	}
	
	public void CreateTable() 
	{
		try 
		{
			String query = "CREATE TABLE IF NOT EXISTS PlayerData "
					     + "(NAME VARCHAR(100), UUID VARCHAR(100), MONEY INT(100), TOKENS INT(100), PRIMARY KEY (NAME))";

			PreparedStatement ps = plugin.SQL.GetConnection().prepareStatement(query);
			ps.executeUpdate();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	public void CreatePlayer(Player player) 
	{
		try 
		{
			UUID uuid = player.getUniqueId();

			if (!Exists(uuid)) 
			{
				String query = "INSERT IGNORE INTO playerdata (NAME,UUID) VALUES (?,?)";
				
				PreparedStatement ps = plugin.SQL.GetConnection().prepareStatement(query);
				ps.setString(1, player.getName());
				ps.setString(2, uuid.toString());
				
				ps.executeUpdate();
				
				return;
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public boolean Exists(UUID uuid) 
	{
		try 
		{
			String query = "SELECT * FROM playerdata WHERE UUID=?";
			
			PreparedStatement ps = plugin.SQL.GetConnection().prepareStatement(query);
			ps.setString(1, uuid.toString());
			
			ResultSet results = ps.executeQuery();
			if (results.next())
				return true;
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return false;
	}
}
