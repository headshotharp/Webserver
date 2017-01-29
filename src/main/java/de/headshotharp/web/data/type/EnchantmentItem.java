package de.headshotharp.web.data.type;

import de.headshotharp.enchantment.helper.EnchantmentHelper;

public class EnchantmentItem
{
	private int id;
	private EnchantmentHelper ench;

	public EnchantmentItem(int id, String name)
	{
		this.id = id;
		ench = EnchantmentHelper.byName(name);
	}

	public int getId()
	{
		return id;
	}

	public EnchantmentHelper getEnch()
	{
		return ench;
	}

	@Override
	public String toString()
	{
		return id + ": " + getEnch().getNormalName();
	}
}
