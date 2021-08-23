package com.errol;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

class Rank
{
	public String tag;
	public long cost;
	public boolean max;
	
	public Rank(String tag, long cost) 
	{
		this.tag = tag;
		this.cost = cost;
		this.max = false;
	}
	
	public Rank(String tag, long cost, boolean max) 
	{
		this.tag = tag;
		this.cost = cost;
		this.max = max;
	}
}

public class RankManager
{
	private Main plugin;
	private Permission perms;
	
	public static Rank[] ranks = 
	{
		new Rank("a", 0),
		new Rank("b", 20),
		new Rank("c", 30),
		new Rank("d", 40),
		new Rank("e", 50),
		new Rank("f", 60),
		new Rank("g", 70),
		new Rank("h", 80),
		new Rank("i", 90),
		new Rank("j", 100),
		new Rank("k", 110),
		new Rank("l", 120),
		new Rank("m", 130),
		new Rank("n", 140),
		new Rank("o", 150),
		new Rank("p", 160),
		new Rank("q", 170),
		new Rank("r", 180),
		new Rank("s", 190),
		new Rank("t", 200),
		new Rank("u", 210),
		new Rank("v", 220),
		new Rank("w", 230),
		new Rank("x", 240),
		new Rank("y", 250),
		new Rank("z", 260, true),
	};
	
	public RankManager(Main plugin) 
	{
		this.plugin = plugin;
		SetupPermissions();
	}
	
    private boolean SetupPermissions() 
    {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public void TryRankup(Player player) 
    {
    	Rank rank = GetRank(player);
    	Rank next = NextRank(rank);
    	long money = plugin.moneyManager.GetMoney(player.getUniqueId());

    	if (rank.max)
    	{
    		player.sendMessage("You cannot rankup because you are the max rank!");
    	}
    	else if (money >= next.cost)
    	{
    		plugin.moneyManager.TakeMoney(player.getUniqueId(), next.cost);
    		if (perms.playerInGroup(player, rank.tag))
    			perms.playerRemoveGroup(player, rank.tag);
    		perms.playerAddGroup(player, next.tag);
    		
    		player.sendMessage("Ranked up from " + rank.tag + " to " + next.tag);
    	} 
    	else 
    	{
    		long diff = next.cost - money;
    		player.sendMessage("You need " + diff + " to rank up from " + rank.tag + " to " + next.tag);
    	}
    }
    
    public void LogRank(Player player) 
    {
    	Rank rank = GetRank(player);
    	Rank next = NextRank(rank);
    	long money = plugin.moneyManager.GetMoney(player.getUniqueId());
    	long diff = next.cost - money;

    	if (rank.max)
    		player.sendMessage("You are the max rank! (" + rank.tag + ")");
    	else
    		player.sendMessage("Your rank is " + rank.tag + ", you need " + diff + " more to rank up to " + next.tag);
    }
    
    private Rank GetRank(Player player) 
    {
    	for (int i = ranks.length - 1; i > 0; i--)
    	{
    		if (perms.playerInGroup(player, ranks[i].tag))
    			return ranks[i];
    	}
    	return ranks[0];
    }
    
    private Rank NextRank(Rank rank) 
    {
    	for (int i = ranks.length - 1; i >= 0; i--)
    	{
    		if (ranks[i].tag == rank.tag) 
    		{
    			if (i + 1 < ranks.length)
    				return ranks[i + 1];	
    		}
    	}
    	return ranks[0];
    }
}
