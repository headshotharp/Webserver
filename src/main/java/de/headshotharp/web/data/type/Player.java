package de.headshotharp.web.data.type;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.data.DataProvider;
import de.headshotharp.web.util.DateTime;
import de.headshotharp.web.util.DateTime.DateTimeFormat;

public class Player implements Comparable<Player>
{
	public int id;
	public String name, realname;
	public DateTime creation_date, lastGift;
	public int money, block_break, block_place;
	public boolean online, giftReady;
	public PermissionsGroup group;
	public Color color = null;
	public PlayerStatus status;

	public Player(int id, String name, String realname, DateTime creation_date, DateTime lastGift, int money, int block_break, int block_place, boolean online, PermissionsGroup group, PlayerStatus status)
	{
		this.id = id;
		this.name = name;
		this.realname = realname;
		this.creation_date = creation_date;
		this.lastGift = lastGift;
		this.money = money;
		this.block_break = block_break;
		this.block_place = block_place;
		this.online = online;
		this.group = group;
		this.status = status;
		giftReady = Config.isGiftReady(lastGift);
	}

	public int getGift(DataProvider dp)
	{
		giftReady = false;
		int giftAmount = Config.getGiftAmount();
		dp.updateGiftNow(id);
		money += giftAmount;
		dp.setMoney(id, money);
		return giftAmount;
	}

	public void addMoney(DataProvider dp, int amount)
	{
		setMoney(dp, money + amount);
	}

	public void setMoney(DataProvider dp, int amount)
	{
		money = amount;
		dp.setMoney(id, money);
	}

	public int getBlockBreakToday(DataProvider dp)
	{
		int i = dp.getPlayerBlockBreakUntilYesterday(id);
		return block_break - i;
	}

	public int getBlockPlaceToday(DataProvider dp)
	{
		int i = dp.getPlayerBlockPlaceUntilYesterday(id);
		return block_place - i;
	}

	public int getBlockBreakThisMonth(DataProvider dp)
	{
		int i = dp.getPlayerBlockBreakUntilMonthstart(id);
		return block_break - i;
	}

	public int getBlockPlaceThisMonth(DataProvider dp)
	{
		int i = dp.getPlayerBlockPlaceUntilMonthstart(id);
		return block_place - i;
	}

	/**
	 * returns fullname for managers, ingame name only for players in
	 * <b>HTML</b>
	 * 
	 * @return
	 */
	public String getShortnameHtml()
	{
		if (group.isManager()) return getPrefixName();
		return name;
	}

	/**
	 * returns name with HTML PermissionsGroup prefix
	 * 
	 * @return
	 */
	public String getPrefixName()
	{
		return group.getHtmlPrefix() + " " + name;
	}

	/**
	 * returns realname of player<br />
	 * might by "" if unknown
	 * 
	 * @return
	 */
	public String getRealname()
	{
		return realname;
	}

	/**
	 * returns money with currency in <b>HTML</b> format
	 * 
	 * @return
	 */
	public String getMoneyHtml()
	{
		return getMoneyFormat() + Config.VALUE_CURRENCY_HTML;
	}

	public boolean hasPermission(PermissionsGroup pg)
	{
		return this.group.hasPermission(pg);
	}

	public boolean isManager()
	{
		return this.group.isManager();
	}

	public String getHeadUrl()
	{
		return getHeadUrl(name);
	}

	public String getBodyUrl()
	{
		return getBodyUrl(name);
	}

	public String getBaseUrl()
	{
		return getBaseUrl(name);
	}

	public static String getBodyUrl(String username)
	{
		return "/skins/" + username + "/body.png";
	}

	public static String getHeadUrl(String username)
	{
		return "/skins/" + username + "/head.png";
	}

	public static String getBaseUrl(String username)
	{
		return "/skins/" + username + "/";
	}

	public String getMoneyFormat()
	{
		return CommonUtils.decimalDots(money);
	}

	public String getBlockBreakFormat()
	{
		return CommonUtils.decimalDots(block_break);
	}

	public String getBlockPlaceFormat()
	{
		return CommonUtils.decimalDots(block_place);
	}

	public String getPlayerForListHtml()
	{
		return "<div class='player'><div class='name'><p>" + getShortnameHtml() + "</p>" + (online ? "<img src='/img/serveronline.png' />" : "") + "</div><div class='skin'><img src='" + getBodyUrl() + "'></div></div>";
	}

	public static String getPlayerListHtml(List<Player> list)
	{
		StringBuilder str = new StringBuilder();
		str.append("<div class='playerlist'>");
		for (Player p : list)
		{
			str.append(p.getPlayerForListHtml());
		}
		str.append("</div>");
		return str.toString();
	}

	public static String getJavascriptData(List<Player> list)
	{
		StringBuilder str = new StringBuilder();
		str.append("var data = [");
		for (Player p : list)
		{
			str.append("{id:" + p.id + ",name:\"" + p.name + "\",prefix:\"" + p.group.getHtmlPrefixVisibleDesktop() + "\",block_break:" + p.block_break + ",block_place:" + p.block_place + ",date:\"" + p.creation_date.format(DateTimeFormat.FORMAT_SQL_TIMESTAMP.getSimpleDateFormat()) + "\"},");
		}
		str.append("];");
		return str.toString();
	}

	public String get(String request)
	{
		if (request.equalsIgnoreCase("id"))
		{
			return "" + id;
		}
		else if (request.equalsIgnoreCase("name"))
		{
			return name;
		}
		else if (request.equalsIgnoreCase("realname"))
		{
			return realname;
		}
		else if (request.equalsIgnoreCase("creation_date"))
		{
			return creation_date.toString();
		}
		else if (request.equalsIgnoreCase("lastgift"))
		{
			return lastGift.toString();
		}
		else if (request.equalsIgnoreCase("giftready"))
		{
			return giftReady ? "1" : "0";
		}
		else if (request.equalsIgnoreCase("money"))
		{
			return "" + money;
		}
		else if (request.equalsIgnoreCase("block_break"))
		{
			return "" + block_break;
		}
		else if (request.equalsIgnoreCase("block_place"))
		{
			return "" + block_place;
		}
		else if (request.equalsIgnoreCase("online"))
		{
			return online ? "1" : "0";
		}
		else if (request.equalsIgnoreCase("head_url"))
		{
			return getHeadUrl();
		}
		else
		{
			return "ERROR";
		}
	}

	public String getAjaxEncode()
	{
		String split = Config.VALUE_SPLIT;
		return getShortnameHtml() + split + getBaseUrl() + split + (online ? "1" : "0") + split;
	}

	/**
	 * creates javascript array of players for server online list
	 * 
	 * @param list
	 * @return
	 */
	public static String getJavascriptArrayForInitialOnlineList(List<Player> list)
	{
		StringBuilder str = new StringBuilder();
		str.append("var playerListOnline = {");
		for (Player p : list)
		{
			str.append(p.id + ": {name:'" + p.name + "'},");
		}
		str.append("};");
		return str.toString();
	}

	@Override
	public int compareTo(Player o)
	{
		if (id > o.id) return 1;
		if (id < o.id) return -1;
		return 0;
	}

	public static final Comparator<Player> COMPARATOR_BLOCK_PLACE = new Comparator<Player>()
	{
		@Override
		public int compare(Player o1, Player o2)
		{
			if (o1.block_place < o2.block_place) return 1;
			if (o1.block_place > o2.block_place) return -1;
			return 0;
		}
	};
}
