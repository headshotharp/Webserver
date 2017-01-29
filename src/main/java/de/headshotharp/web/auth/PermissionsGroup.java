package de.headshotharp.web.auth;

import java.awt.Color;

import de.headshotharp.commonutils.CommonUtils;

public enum PermissionsGroup
{
	BEGINNER("[B]", 0, Color.BLACK, ""),
	PLAYER("[P]", 1, new Color(0, 140, 50), "player"),
	TRUSTED_PLAYER("[T]", 2, new Color(0, 140, 50), "trusted"),
	DEVELOPMENT("[DEV]", 3, new Color(255, 140, 26), "development"),
	COMMUNITY_MANAGER("[CM]", 4, new Color(230, 220, 0), "cm"),
	ADMIN("[SA]", 5, new Color(253, 62, 255), "admin");

	private String prefix;
	private int val;
	private Color color;
	private String bukkitname;

	PermissionsGroup(String prefix, int value, Color color, String bukkitname)
	{
		this.prefix = prefix;
		this.val = value;
		this.color = color;
		this.bukkitname = bukkitname;
	}

	public int getValue()
	{
		return val;
	}

	/**
	 * ADMIN, COMMUNITY_MANAGER, DEV are managers<br />
	 * others are players
	 * 
	 * @return
	 */
	public boolean isManager()
	{
		return val >= 3;
	}

	public String getHtmlPrefix()
	{
		return "<b style='color: " + CommonUtils.colorToHtml(color) + "'>" + prefix + "</b>";
	}

	public String getHtmlPrefixVisibleDesktop()
	{
		return "<b class='hidden-xs' style='color: " + CommonUtils.colorToHtml(color) + "'>" + prefix + "</b>";
	}

	public String getPrefix()
	{
		return prefix;
	}

	public boolean hasPermission(PermissionsGroup group)
	{
		return val >= group.val;
	}

	/**
	 * returns found {@link PermissionsGroup} or {@link PermissionsGroup.PLAYER}
	 * if not found
	 * 
	 * @param name
	 * @return
	 */
	public static PermissionsGroup byValue(int val)
	{
		for (PermissionsGroup g : values())
		{
			if (g.val == val) return g;
		}
		return PermissionsGroup.PLAYER;
	}

	/**
	 * returns found {@link PermissionsGroup} or {@link PermissionsGroup.PLAYER}
	 * if not found
	 * 
	 * @param name
	 * @return
	 */
	public static PermissionsGroup byBukkitName(String name)
	{
		for (PermissionsGroup g : values())
		{
			if (g.bukkitname.equalsIgnoreCase(name)) return g;
		}
		return PermissionsGroup.PLAYER;
	}
}
