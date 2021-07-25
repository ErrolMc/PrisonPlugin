package com.errol;

import java.sql.*;

public class MySQL
{
	private String host = "localhost";
	private String port = "3306"; // 3308?
	private String database = "prisonutils";
	private String username = "root";
	private String password = "";
	
	private Connection connection;
	
	public boolean IsConnected() { return connection != null; }
	
	public void Connect() throws ClassNotFoundException, SQLException 
	{
		if (!IsConnected()) 
		{
			String connectionStr = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
			connection = DriverManager.getConnection(connectionStr, username, password);
		}
	}
	
	public void Disconnect() 
	{
		if (IsConnected()) 
		{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Connection GetConnection() 
	{
		return connection;
	}

}
