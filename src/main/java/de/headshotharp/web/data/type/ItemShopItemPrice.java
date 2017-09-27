package de.headshotharp.web.data.type;

import java.util.List;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;

public class ItemShopItemPrice extends ItemShopItem {
	private int price;
	private int discount;

	public ItemShopItemPrice(int id, String name, String mc_item, int price) {
		super(id, name, mc_item);
		this.price = price;
		this.discount = 0;
	}

	public ItemShopItemPrice(int id, String name, String mc_item, int price, int discount) {
		this(id, name, mc_item, price);
		this.discount = discount;
	}

	public int getDiscount() {
		return discount;
	}

	public int getPrice() {
		return (int) (price * (1 - (discount / 100.0f)));
	}

	public int getRawPrice() {
		return price;
	}

	public String toHTML() {
		String style = "";
		String buy;
		String disc = "";
		if (discount > 0) {
			disc = "<p class='shop-discount'>-" + discount + "%</p>";
			buy = "Für <small><s>" + CommonUtils.decimalDots(getRawPrice()) + " " + Config.VALUE_CURRENCY_HTML
					+ "</s></small> " + CommonUtils.decimalDots(getPrice()) + " " + Config.VALUE_CURRENCY_HTML
					+ " kaufen.";
		} else {
			buy = "Für " + CommonUtils.decimalDots(getPrice()) + " " + Config.VALUE_CURRENCY_HTML + " kaufen";
		}
		return "<div class='shop'><h1>" + getName() + disc + "</h1><img " + style + "src='/img/" + getImage() + "'><p>"
				+ getDescription() + "</p><a href='/shop/items/buy/" + getId() + "'><h2>" + buy
				+ "</h2></a><a class='how-to-use' href='#fineprint'>*</a></div>";
	}

	public static String toHtml(List<ItemShopItemPrice> list) {
		StringBuilder str = new StringBuilder();
		for (ItemShopItemPrice e : list) {
			str.append(e.toHTML());
		}
		str.append(
				"<div class='shop-info' id='fineprint'><p class='star'>*</p><p><b>Gekaufte Artikel benutzen:</b><br />Nach dem Kauf in Minecraft den Befehl <code>/itemshop</code> eingeben. Es erscheint eine Liste mit gekauften Artikeln.<br />Bsp.: <code>37: Pickaxe Andromeda</code> Die Zahl vor dem Doppelpunkt (hier: <i>37</i>) ist eine eindeutige ID.<br />Um das Anfordern des Items einzuleiten musst du den Befehl <code>/itemshop 37</code> ausführen.</p><br /><p><b>Hinweis:</b><br />Dieser Vorgang kann <i><b>nicht</b></i> rückgängig gemacht werden.<i><b>Kein Geld zurück!</b></i></p><br class='clear' /></div>");
		return str.toString();
	}
}
