package com.gmail.azfaloth.telegramcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Initialize extends JavaPlugin implements Listener
{
	String messagePrefix = "[TelegramCraft] ";
	String botToken;
	String serverName;
	ArrayList<String> blockedFormats = new ArrayList<>();

	ArrayList<Integer> ids = new ArrayList<>();
	ArrayList<Integer> stafflist = new ArrayList<>();
	HashMap<String, String> linkCodes = new HashMap<String, String>();
	HashMap<Integer, String> linkedChats = new HashMap<Integer, String>();

	Plugin plugin;
	Plugin ess;
	TelegramUtils utils;
	boolean debugchatformats;

	public void onEnable()
	{
		saveDefaultConfig();
		loadConfigVariables();
		plugin = this;
		utils = new TelegramUtils((Initialize) plugin);
		Thread tgpoller = new TGPoller(this);


		//load Essentials reference
		try
		{ess = Bukkit.getServer().getPluginManager().getPlugin("Essentials");}
		catch (Exception e)
		{ess = null;
		System.out.println(messagePrefix +"Essentials not found");}

		//registering listeners
		ServerCommandHandler sch = new ServerCommandHandler((Initialize) plugin);
		Bukkit.getPluginCommand("telegram").setExecutor(sch);
		Bukkit.getPluginCommand("staff").setExecutor(sch);
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new TelegramCommandHandler((Initialize) plugin), this);


		if(utils.connect())
		{System.out.println(messagePrefix +"Successfully connected to Telegram Servers!");}

		utils.sendAllClients("Server is starting up.", "true");

		ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);
		pool.scheduleAtFixedRate(tgpoller, 2, 2, TimeUnit.SECONDS);

		/*Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			public void run()
			{
				if(utils.isConnected)
				{
					utils.getUpdate();
				}
			}
		}, 80L, 60L);
		 */

	}

	@SuppressWarnings("unchecked")
	private void loadConfigVariables() 
	{
		debugchatformats = getConfig().getBoolean("enable.chatformatdebug");
		blockedFormats = (ArrayList<String>) getConfig().getStringList("blockedFormats");
		botToken = getConfig().getString("botToken");
		serverName = getConfig().getString("serverName");
		ids = (ArrayList<Integer>) getConfig().getList("IDList");
		stafflist = (ArrayList<Integer>) getConfig().getList("StaffList");

		if(getConfig().getConfigurationSection("linkCodes") != null)
		{for (String key : getConfig().getConfigurationSection("linkCodes").getKeys(false)) 
		{	
			linkCodes.put(key, getConfig().getString("linkCodes." + key));
		}}

		if(getConfig().getConfigurationSection("linkedChats") != null)
		{for (String key : getConfig().getConfigurationSection("linkedChats.").getKeys(false)) 
		{	
			linkedChats.put(Integer.parseInt(key), getConfig().getString("linkedChats." + key));
		}}

	}

	public void onDisable()
	{
		utils.sendAllClients("Server is shutting down.", "true");
		//utils.sendAllClients("Server Shutting Down!", "true");
		plugin.getConfig().set("botToken", botToken);
		//saving ids
		getConfig().set("IDList", ids);

		//saving link codes
		for (String key : linkCodes.keySet()) 
		{
			getConfig().set("linkCodes." + key, linkCodes.get(key));
		}

		//saving linked chats
		for (Integer key : linkedChats.keySet()) 
		{
			getConfig().set("linkedChats." + key, linkedChats.get(key));
		}

		this.saveConfig();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		if(!this.getConfig().getBoolean("enable.joinquitmessages", true)) return;
		if(utils.isConnected)
		{
			utils.sendAllClients("`" + fixMarkupChars(e.getPlayer().getName()) + " joined the game.`", "false");
		}
	}


	@EventHandler
	public void onDeath(PlayerDeathEvent e)
	{
		
		if(!this.getConfig().getBoolean("enable.deathmessages", false)) return;
		if(utils.isConnected)
		{
			try
			{
			utils.sendAllClients("`"+ fixMarkupChars(e.getDeathMessage()) + "`", "true");
			}
			catch(NullPointerException exc)
			{
				System.out.println(messagePrefix + "Late cancelled player death");
			}
			
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		if(!this.getConfig().getBoolean("enable.joinquitmessages", true)) return;
		if(utils.isConnected)
		{
			utils.sendAllClients("`" + fixMarkupChars(e.getPlayer().getName()) + " left the game.`", "false");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e)
	{	
		if(debugchatformats)
		{
			System.out.println("Format is " + e.getFormat());
			System.out.println("Chat is " + e.getMessage());
		}

		if(utils.isConnected)
		{
			for(String form: blockedFormats)
			{
				if(e.getFormat().contains(form))
				{return;}
			}
			String chat = "*" + fixMarkupChars(e.getPlayer().getDisplayName()) +"*" + ": _" + fixMarkupChars(e.getMessage()) + "_";
			utils.sendAllClients(chat, "true");
		}
	}
	public String fixMarkupChars(String s)
	{
		return s.replaceAll("§[0-9,a-z]", "").replace("_", "-").replace("*", "").replace("'", "");
	}

}
