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

class Prestige
{
	public String tag;
	public long cost;
	public float multiplier;
	public boolean max;
	
	public Prestige(String tag, long cost, float multiplier) 
	{
		this.tag = tag;
		this.cost = cost;
		this.multiplier = multiplier;
		this.max = false;
	}
	
	public Prestige(String tag, long cost, float multiplier, boolean max) 
	{
		this.tag = tag;
		this.cost = cost;
		this.multiplier = multiplier;
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
	
	public static Prestige[] prestiges = 
	{
		new Prestige("p0", 0, 1),
		new Prestige("p1", 10000, 1),
		new Prestige("p2", 20000, 1.1f),
		new Prestige("p3", 30000, 1.2f),
		new Prestige("p4", 40000, 1.3f),
		new Prestige("p5", 50000, 1.4f),
		new Prestige("p6", 60000, 1.5f),
		new Prestige("p7", 70000, 1.6f),
		new Prestige("p8", 80000, 1.7f),
		new Prestige("p9", 90000, 1.8f),
		new Prestige("p10", 100000, 1.9f, true),
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
    
    public void TryPrestige(Player player) 
    {
    	Rank rank = GetRank(player);
    	Prestige prestige = GetPrestige(player);
    	Prestige next = NextPrestige(prestige);
    	long money = plugin.moneyManager.GetMoney(player.getUniqueId());

    	if (rank.max) 
    	{
        	if (prestige.max) 
        	{
        		player.sendMessage("You cannot prestige because you are the max prestige!");
        	}
        	else if (money >= next.cost)
        	{
        		plugin.moneyManager.TakeMoney(player.getUniqueId(), next.cost);
        		
        		if (perms.playerInGroup(player, prestige.tag))
        			perms.playerRemoveGroup(player, prestige.tag); // remove cur prestige
        		if (perms.playerInGroup(player, rank.tag))
        			perms.playerRemoveGroup(player, rank.tag); // remove rank z
        		
        		perms.playerAddGroup(player, next.tag); // next prestige
        		perms.playerAddGroup(player, ranks[0].tag); // a
        		
        		player.sendMessage("Prestiged up from " + prestige.tag + " to " + next.tag);
        	} 
        	else 
        	{
        		long diff = next.cost - money;
        		player.sendMessage("You need " + diff + " to prestige from " + prestige.tag + " to " + next.tag);
        	}
        }
    	else 
    	{
    		player.sendMessage("You cannot prestige because arent the max rank!");
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
    
    private Prestige GetPrestige(Player player) 
    {
    	for (int i = prestiges.length - 1; i > 0; i--)
    	{
    		if (perms.playerInGroup(player, prestiges[i].tag))
    			return prestiges[i];
    	}
    	return prestiges[0];
    }
    
    private Prestige NextPrestige(Prestige prestige) 
    {
    	for (int i = prestiges.length - 1; i >= 0; i--)
    	{
    		if (prestiges[i].tag == prestige.tag) 
    		{
    			if (i + 1 < prestiges.length)
    				return prestiges[i + 1];	
    		}
    	}
    	return prestiges[0];
    }
}
