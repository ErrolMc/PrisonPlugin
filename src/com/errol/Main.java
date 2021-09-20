package com.errol;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener
{
	public static Main instance;
	
	public MySQL SQL;
	public SQLGetter data;
	public MoneyManager moneyManager;
	public RankManager rankManager;
	public MineManager mineManager;
	
	public void onEnable() 
	{
		instance = this;
		this.getServer().getPluginManager().registerEvents(this, this);
		
		SetupDatabase();
	}
	
	void SetupDatabase() 
	{
		SQL = new MySQL();
		data = new SQLGetter(this);
		moneyManager = new MoneyManager(this);
		rankManager = new RankManager(this);
		mineManager = new MineManager(this);
		
		try {
			SQL.Connect();
		} catch (Exception e) {
			getLogger().info("Database not connected");
		}
		
		if (SQL.IsConnected()) 
		{
			getLogger().info("Database is connected");
			data.CreateTable();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		String commandName = command.getName().toLowerCase();

		if (commandName.equals("prison")) 
		{
			if (args != null && args.length > 0) 
			{
				String arg1 = args[0].toLowerCase();
				if (arg1.equals("connected")) 
				{
					if (SQL != null)
					{
						boolean res = SQL.IsConnected();
						sender.sendMessage("Prison > Connected: " + res);
					}
				}
				return true;
			}
			
			sender.sendMessage("Prison > Please enter command arguments");
		}
		
		if (commandName.equals("pay")) 
		{
			if (args != null && args.length > 1) 
			{
				Player other = Bukkit.getPlayer(args[0].toLowerCase());
				if (other != null && other.isOnline()) 
				{
					long amount = StaticUtils.ParseLong(args[1]); 
					if (amount > 0) 
					{
						Player player = Bukkit.getPlayer(sender.getName());
						UUID uuid = player.getUniqueId();
						long playerMoney = moneyManager.GetMoney(uuid);
						
						if (playerMoney >= amount) 
						{
							UUID otherUUID = other.getUniqueId();
							moneyManager.AddMoney(otherUUID, amount);
							moneyManager.TakeMoney(uuid, amount);
							
							player.sendMessage("Server > Payed $" + amount + " to " + other.getName());
							other.sendMessage("Server > Recieved $" + amount + " from " + player.getName());
							
							return true;
						}
					}
				}
			}
			
			sender.sendMessage("Server > usage: /pay [username] [amount]");
		}
		
		if (commandName.equals("setmoney")) 
		{
			Player player = Bukkit.getPlayer(sender.getName());
			if (player.isOp()) 
			{
				if (args != null && args.length > 1) 
				{
					Player other = Bukkit.getPlayer(args[0].toLowerCase());
					if (other != null && other.isOnline()) 
					{
						long amount = StaticUtils.ParseLong(args[1]); 
						if (amount != -1) 
						{
							moneyManager.SetMoney(other.getUniqueId(), amount);
							
							player.sendMessage("Server > Set the balance of " + other.getName() + " to $" + amount);
							other.sendMessage("Server > Your balance has been set to $" + amount);
							
							return true;
						}
					}
				}
			}
			
			sender.sendMessage("Server > usage: /setmoney [username] [amount]");
		}
		
		if (commandName.equals("addmoney")) 
		{
			Player player = Bukkit.getPlayer(sender.getName());
			if (player.isOp()) 
			{
				if (args != null && args.length > 1) 
				{
					Player other = Bukkit.getPlayer(args[0].toLowerCase());
					if (other != null && other.isOnline()) 
					{
						int amount = StaticUtils.ParseInt(args[1]); 
						if (amount != -1) 
						{
							moneyManager.AddMoney(other.getUniqueId(), amount);
							
							long curMoney = moneyManager.GetMoney(other.getUniqueId());
							player.sendMessage("Server > Added $" + amount + " to " + other.getName() + "'s balance making it $" + curMoney);
							other.sendMessage("Server > Added $" + amount + " to your balance making it $" + curMoney);
							
							return true;
						}
					}
				}
			}
			
			sender.sendMessage("Server > usage: /addmoney [username] [amount]");
		}
		
		if (commandName.equals("takemoney")) 
		{
			Player player = Bukkit.getPlayer(sender.getName());
			if (player.isOp()) 
			{
				if (args != null && args.length > 1) 
				{
					Player other = Bukkit.getPlayer(args[0].toLowerCase());
					if (other != null && other.isOnline()) 
					{
						int amount = StaticUtils.ParseInt(args[1]); 
						if (amount != -1) 
						{
							moneyManager.TakeMoney(other.getUniqueId(), amount);
							
							long curMoney = moneyManager.GetMoney(other.getUniqueId());
							
							player.sendMessage("Server > Taken $" + amount + " from " + other.getName() + "'s balance making it $" + curMoney);
							other.sendMessage("Server > Taken $" + amount + " from your balance making it $" + curMoney);
							
							return true;
						}
					}
				}
			}
			
			sender.sendMessage("Server > usage: /addmoney [username] [amount]");
		}
		
		if (commandName.equals("bal")) 
		{
			String targetName = sender.getName();
			if (args != null && args.length > 0)
				targetName = args[0];
			
			Player targetPlayer = Bukkit.getPlayer(targetName.toLowerCase());
			if (targetPlayer != null) 
			{
				Player player = Bukkit.getPlayer(sender.getName());
				
				long money = moneyManager.GetMoney(targetPlayer.getUniqueId());
				player.sendMessage("Server > The balance of " + targetName + " is $" + money);
				
				return true;
			}

			sender.sendMessage("Server > usage: /bal [username]");
		}
		
		if (commandName.equals("rankup")) 
		{
			rankManager.TryRankup(Bukkit.getPlayer(sender.getName()));
			return true;
		}
		
		if (commandName.equals("rank")) 
		{
			rankManager.LogRank(Bukkit.getPlayer(sender.getName()));
			return true;
		}

		if (commandName.equals("prestige")) 
		{
			rankManager.TryPrestige(Bukkit.getPlayer(sender.getName()));
			return true;
		}
		
		if (commandName.equals("mine")) 
		{
			mineManager.HandleCommand(Bukkit.getPlayer(sender.getName()), args);
			return true;
		}
		
		if (commandName.equals("sell") || commandName.equals("sellall")) 
		{
			mineManager.HandleSell(Bukkit.getPlayer(sender.getName()), commandName, args);
			return true;
		}
		
		return true;
	}
	
    @EventHandler
    public void onPlayerClickSign(PlayerInteractEvent event) 
    {
    	Block block = event.getClickedBlock();
    	Action action = event.getAction();
    	if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) 
    	{
    		if (StaticUtils.IsSign(block.getType())) 
    		{
    			mineManager.ClickSign(new Vector3Int(block.getLocation()), event.getPlayer(), action == Action.RIGHT_CLICK_BLOCK);
    		}
    	}
    }
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) 
    {
		Block block = event.getBlock();
		if (StaticUtils.IsSign(block.getType())) 
		{
			if (mineManager.DeleteSign(new Vector3Int(block.getLocation()))) 
			{
				event.getPlayer().sendMessage("[Mines] Deleted mine sign");	
			}
		}
    }
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) 
	{
		Player player = event.getPlayer();
		data.CreatePlayer(player);
	}
}
