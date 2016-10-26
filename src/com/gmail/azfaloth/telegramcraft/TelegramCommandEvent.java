package com.gmail.azfaloth.telegramcraft;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TelegramCommandEvent extends Event
{
String command;
String arguments;
int uuid;
int id;
String name;
private static final HandlerList handlers = new HandlerList();

public TelegramCommandEvent(int id, String command, String arguments, String name)
{
	this.command = command;
	this.arguments = arguments;
	this.id = id;
	this.name = name;
	System.out.println("Telegram Client "+id + " has run a Telegram command - "+command);
}

public String getArgs()
{
	return this.arguments;
}

public String getCommand()
{
	return this.command;
}

public String getName()
{
	return this.name;
}

public int getSenderID()
{
	return this.id;
}

public int getSenderUUID()
{
	return this.uuid;
}
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}


}
