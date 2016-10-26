package com.gmail.azfaloth.telegramcraft;

public class TGPoller extends Thread
{
	Initialize plugin;
	public TGPoller(Initialize plugin)
	{
		this.plugin = plugin;
	}
	public void run()
	{
		if(plugin.utils.isConnected)
		{
			try
			{
			plugin.utils.getUpdate();
			}
			catch(Exception e)
			{
				System.out.println(plugin.messagePrefix + " TGPoller threw Exception");
				e.printStackTrace();
			}
		}
		else
		{
			plugin.utils.connect();
		}
	}

}
