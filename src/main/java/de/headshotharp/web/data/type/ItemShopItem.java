package de.headshotharp.web.data.type;

import java.util.ArrayList;
import java.util.List;

import de.headshotharp.commonutils.CommonUtils;

public class ItemShopItem {
	private int id;
	private String name;
	private String mc_item;
	private List<EnchantmentItem> enchantmentItems = new ArrayList<EnchantmentItem>();

	public ItemShopItem(int id, String name, String mc_item) {
		this.id = id;
		this.name = name;
		this.mc_item = mc_item;
	}

	public List<EnchantmentItem> getEnchantmentItems() {
		return enchantmentItems;
	}

	public void setEnchantmentItems(List<EnchantmentItem> enchantmentItems) {
		this.enchantmentItems = enchantmentItems;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMcItem() {
		return mc_item;
	}

	public String getImage() {
		return getImageByType(mc_item);
	}

	public static String getImageByType(String type) {
		if (type.equalsIgnoreCase("DIAMOND_PICKAXE"))
			return "mc_pickaxe.png";
		if (type.equalsIgnoreCase("DIAMOND_SPADE"))
			return "mc_shovel.png";
		if (type.equalsIgnoreCase("DIAMOND_AXE"))
			return "mc_axe.png";
		if (type.equalsIgnoreCase("DIAMOND_SWORD"))
			return "mc_sword.png";
		return "mc_items.png";
	}

	public String getDescription() {
		StringBuilder str = new StringBuilder();
		str.append("Enthält folgende Enchantments:<ul>");
		for (EnchantmentItem item : enchantmentItems) {
			str.append(
					"<li>" + item.getEnch().getTrivialName() + " " + CommonUtils.IntToRoman(item.getLevel()) + "</li>");
		}
		str.append("</ul>");
		return str.toString();
	}
}
