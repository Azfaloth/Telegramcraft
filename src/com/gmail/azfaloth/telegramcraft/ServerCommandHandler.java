package com.gmail.azfaloth.telegramcraft;

import java.util.Random;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ServerCommandHandler implements CommandExecutor 
{
Initialize plugin;
	public ServerCommandHandler(Initialize plugin) 
	{
	this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(commandLabel.equalsIgnoreCase("telegram"))
		{
			if (args.length == 1)
			{
				if(args[0].equalsIgnoreCase("link"))
				{
					if(sender instanceof ConsoleCommandSender)
					{
						System.out.println(plugin.messagePrefix+ "Console cannot be linked!");
						return true;
					}
					else
						if(sender instanceof Player)
						{
							Player player = (Player) sender;
							if((player.hasPermission("telegramcraft.link")))
							{
								String token = generateLinkToken();
								plugin.linkCodes.put(token, player.getUniqueId().toString());
								plugin.saveConfig();
								player.sendMessage("Use /link "+token+" in the telegram bot to link telegram with your in-game character.");
								return true;
							}
							else
							{
								player.sendMessage("You do not have permissions for this command");
								return true;
							}
						}
				}
				
				if(args[0].equalsIgnoreCase("reload"))
				{
					if(sender instanceof ConsoleCommandSender)
					{
						plugin.reloadConfig();
						System.out.println(plugin.messagePrefix+ "Config has been reloaded from disk.");
						return true;
					}
					else
						if(sender instanceof Player)
						{
							Player player = (Player) sender;
							if((player.hasPermission("telegramcraft.reload")))
							{
								plugin.reloadConfig();
								player.sendMessage(plugin.messagePrefix+ "Config has been reloaded from disk.");
								return true;
							}
							else
							{
								player.sendMessage("You do not have permissions for this command");
								return true;
							}
						}
				}

				if(args[0].equalsIgnoreCase("settoken"))
				{
					if(sender instanceof ConsoleCommandSender)
					{
						System.out.println(plugin.messagePrefix+ "Usage is /telegram settoken <token>");
						return true;
					}
					else
						if(sender instanceof Player)
						{
							Player player = (Player) sender;
							if((player.hasPermission("telegramcraft.settoken")))
							{
								player.sendMessage("Usage is /telegram settoken <token>");
								return true;
							}
							else
							{
								player.sendMessage("You do not have permissions for this command");
								return true;
							}
						}
				}

			}

			if (args.length == 2)
			{
				if(args[0].equalsIgnoreCase("settoken"))
				{
					if(sender instanceof ConsoleCommandSender)
					{
						plugin.botToken = args[1];
						plugin.getConfig().set("botToken", plugin.botToken);
						plugin.saveConfig();
						System.out.println(plugin.messagePrefix+ "Bot Token has been set.");
						return true;
					}
					else
						if(sender instanceof Player)
						{
							Player player = (Player) sender;
							if((player.hasPermission("telegramcraft.settoken")))
							{
								plugin.botToken = args[1];
								player.sendMessage("The bot token has been set.");
								return true;
							}
							else
							{
								player.sendMessage("You do not have permissions for this command");
								return true;
							}
						}
					return false;
				} return false;

			}return false;



		}
		if(commandLabel.equalsIgnoreCase("staff"))
		{
				if(sender instanceof ConsoleCommandSender)
				{
					System.out.println(plugin.messagePrefix + "This command can only be run by a player.");
					return true;
				}
				if(sender instanceof Player)
				{
					Player p = (Player) sender;
					String argstring = "";
					for(int i=0; i<args.length; i++)
					{
						argstring = argstring + " " + args[i];
					}
					
					String message = "*"+ p.getDisplayName().replaceAll("§[0-9,a-z]", "").replace("_", "-") + "*" + ":" + argstring.toString().replace("_", "-");
					for(int id:plugin.stafflist)
					{
					plugin.utils.sendOneClient(id, "`Attention:`", "false");
					plugin.utils.sendOneClient(id, "`<STAFF MESSAGE>` " + message + " `<STAFF MESSAGE>`", "false");
					}
					p.sendMessage("Message sent!");
					
				 return true;
				}
				else return false;
		}
		else return false;
	}


	public static String generateLinkToken()
	{
		Random rnd = new Random();
		int i = rnd.nextInt(9999999);
		String s = i + "";
		String finals = "";
		for(char m : s.toCharArray()){
			int m2 = Integer.parseInt(m + "");
			int rndi = rnd.nextInt(2);
			if(rndi == 0){
				m2+=97;
				char c = (char) m2;
				finals = finals + c;
			}else{
				finals = finals + m;
			}
		}
		return finals;
	}
}
