package de.headshotharp.web.data.type;

import java.util.List;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;

public class EnchantmentItemPrice extends EnchantmentItem
{
	private int price;
	private int category;
	private int discount;

	private EnchantmentItemPrice(int id, String name, int price, int category)
	{
		super(id, name);
		this.price = price;
		this.category = category;
		this.discount = 0;
	}

	public EnchantmentItemPrice(int id, String name, int price, int category, int discount)
	{
		this(id, name, price, category);
		this.discount = discount;
	}

	public int getDiscount()
	{
		return discount;
	}

	public int getPrice()
	{
		return (int) (price * (1 - (discount / 100.0f)));
	}

	public int getRawPrice()
	{
		return price;
	}

	public String toHTML()
	{
		String style = "";
		String treasure = "";
		if (category == 6)
		{
			style = "style='filter: drop-shadow(0 10px 10px #4d3463);' ";
			treasure = "<br /><small><i>Treasure Enchantment</i></small>";
		}
		String buy;
		String disc = "";
		if (discount > 0)
		{
			disc = "<p class='shop-discount'>-" + discount + "%</p>";
			buy = "Für <small><s>" + CommonUtils.decimalDots(getRawPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</s></small> " + CommonUtils.decimalDots(getPrice()) + " " + Config.VALUE_CURRENCY_HTML + " kaufen.";
		}
		else
		{
			buy = "Für " + CommonUtils.decimalDots(getPrice()) + " " + Config.VALUE_CURRENCY_HTML + " kaufen";
		}
		return "<div class='shop'><h1>" + getEnch().getNormalName() + disc + "</h1><img " + style + "src='/img/mc_enchantement_book.png'><p>" + getEnch().getGermanDescription() + treasure + "</p><a href='/shop/enchantments/buy/" + getId() + "'><h2>" + buy + "</h2></a><a class='how-to-use' href='#fineprint'>*</a></div>";
	}

	public static String toHtml(List<EnchantmentItemPrice> list)
	{
		StringBuilder str = new StringBuilder();
		for (EnchantmentItemPrice e : list)
		{
			str.append(e.toHTML());
		}
		str.append("<div class='shop-info' id='fineprint'><p class='star'>*</p><p><b>Gekaufte Artikel benutzen:</b><br />Nach dem Kauf in Minecraft den Befehl <code>/shop</code> eingeben. Es erscheint eine Liste mit gekauften Artikeln.<br />Bsp.: <code>37: Unbreaking III</code> Die Zahl vor dem Doppelpunkt (hier: <i>37</i>) ist eine eindeutige ID.<br />Um den Artikel zu Nutzen, muss sich das Item, auf das der Artikel angewendet werden soll, in der Hand befinden.<br />Um das Benutzen zu bestätigen den Befehl <code>/shop 37</code> ausführen.</p><br /><p><b>Hinweis:</b><br />Dieser Vorgang kann <i><b>nicht</b></i> rückgängig gemacht werden. Es ist auch möglich, Enchantments auf einen Dirt-Block anzuwenden. <i><b>Kein Geld zurück!</b></i></p><br class='clear' /></div>");
		return str.toString();
	}
}
