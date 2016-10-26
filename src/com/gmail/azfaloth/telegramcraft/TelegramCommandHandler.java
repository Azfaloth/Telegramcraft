package com.gmail.azfaloth.telegramcraft;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.UUID;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.earth2me.essentials.User;


public class TelegramCommandHandler implements Listener
{
	Initialize plugin;
	private static Object minecraftServer;
	private static Field recentTps;
	public TelegramCommandHandler(Initialize plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void CommandEvent(TelegramCommandEvent e)
	{
		String text = e.getCommand();
		int id = e.getSenderID();
		//TODO
		//System.out.println(text);
		//System.out.println(e.getArgs());
		if(text.equals("help"))
		{
			plugin.utils.sendOneClient
			(id, 
					"Use /start to connect to the server." + '\n' +'\n' +
					"Use /stop to disconnect from the server." + '\n' +'\n' +
					"Use /list or /online to see the online players." + '\n' +'\n' +
					"Use /link <token> to link telegram with your in-game character. This <token> is obtained by using \"/telegram link\" from in-game." + '\n' +'\n' +
					"Use /lag to see the server TPS."
					);
			return;
		}

		if(text.equals("start"))
		{
			plugin.utils.sendOneClient(id, 
					"*Welcome to the " + plugin.serverName + " Server Telegram bot "+ e.getName()+ "!"  + '\n' +'\n' +
					"You are connected to the " + plugin.serverName + " server. You can now see the server chat on this bot. Obviously non public chats are not visible here."  + '\n' +'\n' +
					"A bot of this kind is naturally quite spammy. For this reason, normal chats will be silent. You will only get notifications for events."  + '\n' +'\n' +
					"If you havent already, type \"/telegram link\" ingame to link your character to your telegram account. Then you can chat as your character directly from telegram!*" + '\n' +'\n' +
					"Type \"/help\" for a list of commands that you can use here.");
			return;
		}

		if(text.equals("stop"))
		{
			plugin.utils.sendOneClient(id, "*You are now disconnected! Type /start if you want to reconnect!*");
			plugin.ids.remove(plugin.ids.indexOf(id));
			return;
		}


		if(text.equals("lag"))
		{
			DecimalFormat df = new DecimalFormat("#.##");
			double tps = getRecentTps()[0];
			if(tps>20)
			{tps = 20;}
			int percentlag = (int) Math.round((1.0D - tps / 20.0D) * 100.0D);
			if(percentlag<0)
			{percentlag = 0;}
			plugin.utils.sendOneClient(id, "*Server TPS is " + df.format(tps)+ "*"+"\n"+"*Server Lag Percent is "+percentlag+"%*" );
			return;
		}

		if(text.equals("link"))
		{
			String token = e.getArgs();
			if(plugin.linkCodes.containsKey(token))
			{
				String uuid = plugin.linkCodes.get(token);
				plugin.linkedChats.put(id, uuid);
				OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
				plugin.linkCodes.remove(token);
				plugin.utils.sendOneClient(id, "*You are now linked with "+p.getName().toString()+". Whatever you type will now be sent to the server!*");
				return;
			}
			else
				plugin.utils.sendOneClient(id, "*Code is incorrect! Please retry.*");
			return;
		}

		if(text.equals("list") || text.equals("online"))
		{
			Collection<? extends Player> online = plugin.getServer().getOnlinePlayers();
			User user;
			String afk;
			String list = "";
			for(Player p: online)
			{
				afk = "";
				if(plugin.ess!=null)
				{user = ((com.earth2me.essentials.IEssentials) plugin.ess).getUser(p);
				if(user.isAfk())
				{
					afk = "(AFK)";
				}}
				list = list+(p.getDisplayName().replaceAll("§[0-9,a-z]", "").replaceAll("_", "-")) + afk + '\n';

			}
			plugin.utils.sendOneClient(id, "There are " + online.size() + " Players online right now." + '\n' + list);
			return;
		}

		if(text.equals("linktelegram"))
		{
			plugin.utils.sendOneClient(id, "*This command must be used-in game to generate a token. Then type in that token here to verify and connect accounts!*");
			return;
		}

		else
		{
			plugin.utils.sendOneClient(id, "*You have entered an invalid command! Type /help to see a list of available commands!*");
		}
	}



	public static double[] getRecentTps() {
		try {
			if (minecraftServer == null) {
				Server server = Bukkit.getServer();
				Field consoleField = server.getClass().getDeclaredField("console");
				consoleField.setAccessible(true);
				minecraftServer = consoleField.get(server);
			}
			if (recentTps == null) {
				recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
				recentTps.setAccessible(true);
			}
			return (double[]) recentTps.get(minecraftServer);
		} catch (IllegalAccessException | NoSuchFieldException ignored) {
		}
		return new double[] {0, 0, 0};
	}
}

