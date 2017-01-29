package de.headshotharp.web.data.type;

import java.util.List;

import de.headshotharp.web.Config;
import de.headshotharp.web.util.ServerOnlineChecker;

public class ServerStatus
{
	public boolean online;
	public List<Player> onlinePlayers;
	public String uptime;

	public ServerStatus(boolean onlineStatus, List<Player> onlinePlayers, String uptime)
	{
		this.online = onlineStatus;
		this.onlinePlayers = onlinePlayers;
		this.uptime = uptime;
	}

	public ServerStatus(List<Player> onlinePlayers, String uptime)
	{
		online = ServerOnlineChecker.isOnline();
		this.onlinePlayers = onlinePlayers;
		this.uptime = uptime;
	}

	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		str.append("<p><b>Server ist " + (online ? "online" : "offline") + "</b></p><br />");
		int count = onlinePlayers.size();
		if (count > 0)
		{
			str.append("<p><b>Spieler online (" + count + "):</b></p>");
			int i = 0;
			for (Player p : onlinePlayers)
			{
				i++;
				str.append("<p class='player' style='background-image: url(\"/skins/" + p.name + "/head.png\");'>" + p.name + "</p>");
				if (i >= Config.MAX_PLAYER_ONLINE_LIST) break;
			}
			if (count > Config.MAX_PLAYER_ONLINE_LIST) str.append("<p>...</p>");
		}
		else
		{
			str.append("<p><b>Es sind keine Spieler online</b></p>");
		}
		return str.toString();
	}

	public String getAjaxEncode()
	{
		String split = Config.VALUE_SPLIT;
		StringBuilder str = new StringBuilder();
		str.append((online ? "1" : "0") + split);
		for (Player player : onlinePlayers)
		{
			str.append(player.id + split);
		}
		str.append(onlinePlayers.size());
		return str.toString();
	}
}
