package com.gmail.azfaloth.telegramcraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TelegramUtils {

	protected boolean isConnected = false;
	private String workingauth = "";
	private int lastUpdate = 0;
	private Initialize plugin;

	public TelegramUtils(Initialize plugin) {
		this.plugin = plugin;
	}

	public boolean connect() 
	{
		try{
			JsonObject obj = sendGet("https://api.telegram.org/bot" + plugin.botToken + "/getMe");
			if(obj.isJsonNull())
			{
				System.out.print(plugin.messagePrefix +  "Connection refused by Telegram Servers. Either the servers are down or the Bot Token is wrong.");
				return isConnected = false;	
			}
			return isConnected = true;
		}
		catch(Exception e)
		{
			System.out.print(plugin.messagePrefix +  "Exception occurred while connecting to Telegram Servers.");
			e.printStackTrace();
			return isConnected = false;
		}	
	}

	public void sendAllClients(String message, String noNotification)
	{
		for(int id:plugin.ids)
		{
			sendOneClient(id, message, noNotification);
		}
	}

	public void sendOneClient(Integer id, String message)
	{
		sendOneClient(id, message, "true");
	}

	public void sendOneClient(Integer id, String message, String noNotification)
	{
		//Json Builder here and send 1)method = "sendMessage" and 2)Json to post method
		HashMap<String, String> chat = new HashMap<String, String>();
		chat.put("disable_notification", noNotification);
		chat.put("parse_mode", "Markdown");
		chat.put("chat_id", id.toString());
		chat.put("text", message);

		JSONObject chatobject = new JSONObject(chat);
		String stringjson = chatobject.toString();
		//TODO
		//System.out.println(stringjson);
		new Thread(new Runnable(){
			public void run(){

				post("sendMessage",stringjson,id);
			}

		}).start();
	}



	//Telegram API Technical Methods
	public JsonObject sendGet(String url) throws IOException //put this in its own thread
	{
		try
		{
			String a = url;
			URL url2 = new URL(a);
			String all = "";
			URLConnection conn = url2.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = br.readLine()) != null) 
			{
				all += inputLine;
			}
			br.close();
			plugin.utils.workingauth = all;
		}
		catch(Exception e)
		{
			System.out.println(plugin.messagePrefix + "Exception occurred in sendGet Method.");	
			e.printStackTrace();
		}
		JsonParser parser = new JsonParser();
		JsonObject j = parser.parse(plugin.utils.workingauth).getAsJsonObject();
		return j;
	}

	public synchronized void post(String method, String jsonObject, Integer id)
	{	
		URL url = null;
		HttpURLConnection connection = null;
		String body = jsonObject;
		try {url= new URL("https://api.telegram.org/bot" + plugin.botToken + "/" + method);
		}
		catch(MalformedURLException e) 
		{
			System.out.println(plugin.messagePrefix +"Malformed URL at Post Method");
			e.printStackTrace();
			return;
		}

		try{connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/json; ; Charset=UTF-8");
		connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
		}
		catch(IOException ex)
		{System.out.println(plugin.messagePrefix +"Exception in HTTP connection in Post Method");
		ex.printStackTrace();
		return;
		}
		try     (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(connection.getOutputStream()), "UTF-8"));
				)
		{
			writer.write(body);
		} 
		catch (IOException e) 
		{
			System.out.println(plugin.messagePrefix + "IOException occurred at buffer write in post method.");
			System.out.println(body);
			//System.out.println(plugin.messagePrefix + e.getLocalizedMessage());
			e.printStackTrace();

			connect();
		}

		try     (
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
				)
		{
			String rec = reader.readLine();
			JsonParser parser = new JsonParser();
			JsonObject j = parser.parse(rec).getAsJsonObject();
			if(j.isJsonNull())
			{
				System.out.println(plugin.messagePrefix + "Received Null Json from Telegram Servers in Post Method. Reconnecting..");
				connect();
				return;
			}
			if(j.has("ok"))
			{
				if(j.get("ok").getAsString().equals("true"))
				{
					//System.out.println(plugin.messagePrefix + "Message Sent Succesfully");
					//We don't need to spam this in console each time it is successful.
				}
			}
		}
		catch (IOException e) 
		{
			if(e.getMessage().contains("HTTP response code:"))
			{
				int a = e.getMessage().indexOf("code:");
				int errorcode = Integer.parseInt(e.getMessage().substring(a+6, a+5+4));
				if (errorcode == 400) 
				{
					System.out.println(plugin.messagePrefix + "HTTP Error 400 received from Telegram. This means that the sent JSON was improperly formatted. Most likely related to unacceptable characters in message.");
					return;
				}
				if (errorcode == 403) 
				{
					System.out.println(plugin.messagePrefix + "HTTP Error 403 received from Telegram. Message recipient with id "+id.toString()+" probably closed the bot window. Removing them from my client list now.");
					plugin.ids.remove(plugin.ids.indexOf(id));
					return;
				}
				else
				{
					System.out.println(plugin.messagePrefix + "HTTPException occurred at buffer read in post method.");
					System.out.println(plugin.messagePrefix + "HTTP Error encountered with unhandled error code" + errorcode );
					return;
				}
			}
			System.out.println(plugin.messagePrefix + "Unhandled IOException occurred at buffer read in post method.");
			e.printStackTrace();

			connect();
		}
	}

	public void getUpdate()
	{

		JsonObject update = new JsonObject();
		try {

			update = sendGet("https://api.telegram.org/bot" + plugin.botToken + "/getUpdates?offset=" + (lastUpdate + 1));

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if(update.isJsonNull())
		{
			System.out.println(plugin.messagePrefix + "Could not get update from Telegram Servers. Telegram Server may be down. Reconnecting..");
			connect();
		}
		if(update.has("result"))
		{
			for (JsonElement ob : update.getAsJsonArray("result")) 
			{
				if (ob.isJsonObject()) 
				{
					JsonObject obj = (JsonObject) ob;
					if(obj.has("update_id"))
					{
						lastUpdate = obj.get("update_id").getAsInt();
					}
					if (obj.has("message")) 
					{
						JsonObject chat = obj.getAsJsonObject("message").getAsJsonObject("chat");
						int id = chat.get("id").getAsInt();
						if(!plugin.ids.contains(id)) 
						{
							plugin.ids.add(id);
							System.out.println(plugin.messagePrefix + "Telegram Client "+id+ " has connected to the bot. Added them to the recipient list.");
						}
						if(chat.get("type").getAsString().equals("private"))
						{
							String name = "";
							if(chat.has("first_name"))
							{
								name = chat.get("first_name").getAsString();
							}
							if(obj.getAsJsonObject("message").has("text"))
							{
								String text = obj.getAsJsonObject("message").get("text").getAsString();
								for(char c : text.toCharArray())
								{
									if((int) c == 55357)
									{
										sendOneClient(id, "*Emoticons are not allowed, sorry!*", "true");
										return;
									}
								}
								if(text.length() == 0) 
									return;

								if(text.startsWith("/"))//detects that it is a command and passes handling to commandhandler
								{
									List<String> wordList = new ArrayList<String>(); 
									//	List<String> wordList = Arrays.asList(text.split("\\s+"));
									Collections.addAll(wordList, text.split("\\s+")); 
									String command = wordList.get(0).replaceAll("/", "");
									wordList.remove(0);
									String args = "";

									if(wordList.size()>0)
									{
										for(int i = 0;i<wordList.size();i++)
										{
											args = args +" " + wordList.get(i);	
										}
										args = args.substring(1);	
									}
									final String finalarguments = args;
									final String finalname = name;


									Future<Boolean> future = Bukkit.getScheduler().callSyncMethod((Plugin)plugin, new Callable<Boolean>() 
									{
										@Override
										public Boolean call() throws Exception
										{
											TelegramCommandEvent e = new TelegramCommandEvent(id, command, finalarguments, finalname);
											plugin.getServer().getPluginManager().callEvent(e);
											return true;
										}
									});

									try {
										if(future.get())
											return;
									} catch (InterruptedException | ExecutionException ex) 
									{
										System.out.println(plugin.messagePrefix + "Async errors in command event firing.");
										future.cancel(true);
										return;
									}


								}

								if(plugin.linkedChats.containsKey(id))
								{
									sendToMC(UUID.fromString(plugin.linkedChats.get(id)), text, id);
									return;
								}
								else
								{
									sendOneClient(id, "*Sorry, we don't know who you are! Please link your in-game account by using /linktelegram ingame to use telegram chat!*", "true");
									return;
								}
							}
						}
					}
				}
			}	
		}
	}
	public void sendToMC(UUID uuid, String msg, int sender)
	{
		OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
		List<Integer> recievers = new ArrayList<Integer>();
		recievers.addAll(plugin.ids);
		recievers.remove((Object) sender);
		String msgF = "";
		try
		{
			msgF = plugin.getConfig().getString("chat-format").replace('&', '§').replace("%player%",op.getName()).replace("%message%", msg);
		}
		catch(NullPointerException e)
		{
			plugin.linkedChats.remove(sender);
			System.out.println("It appears that this player no longer exists. Clearing from linked chats list.");
			sendOneClient(sender, "Error finding this player. Please re-link telegram", "true");
		}
		for(int id : recievers)
		{
			sendOneClient(id, plugin.fixMarkupChars(msgF), "true");
		}
		System.out.println(msgF.replaceAll("§[0-9,a-z]", ""));
		try
		{
			for(Player p: plugin.getServer().getOnlinePlayers())
			{
				p.sendMessage(msgF);
			}
			//Bukkit.broadcastMessage(msgF);
		}
		catch(Exception e)
		{
			System.out.println("Exception at the send message to players part again. Utils, line 298");
		}

	}
}
