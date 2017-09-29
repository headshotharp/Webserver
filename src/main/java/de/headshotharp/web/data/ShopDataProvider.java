package de.headshotharp.web.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.extractor.EnchantmentItemListExtractor;
import de.headshotharp.web.data.extractor.EnchantmentItemPriceExtractor;
import de.headshotharp.web.data.extractor.EnchantmentItemPriceListExtractor;
import de.headshotharp.web.data.extractor.ItemShopItemPriceExtractor;
import de.headshotharp.web.data.extractor.ItemShopItemPriceListExtractor;
import de.headshotharp.web.data.type.EnchantmentItem;
import de.headshotharp.web.data.type.EnchantmentItemPrice;
import de.headshotharp.web.data.type.ItemShopItemPrice;

@Component
public class ShopDataProvider {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private EnchantmentItemPriceExtractor enchantmentItemPriceExtractor;

	@Autowired
	private EnchantmentItemPriceListExtractor enchantmentItemPriceListExtractor;

	@Autowired
	private EnchantmentItemListExtractor enchantmentItemListExtractor;

	@Autowired
	private ItemShopItemPriceExtractor itemShopItemPriceExtractor;

	@Autowired
	private ItemShopItemPriceListExtractor itemShopItemPriceListExtractor;

	public void setEnchantmentDiscount(int discount) {
		String sql = "UPDATE serverstatus SET value = ? WHERE status = ?";
		jdbc.update(sql, "" + discount, "enchantdiscount");
	}

	/**
	 * returns enchantment discount from table, but capped between 0 and 30 (in %)
	 *
	 * @return
	 */
	public int getEnchantmentDiscount() {
		String sql = "SELECT value from serverstatus WHERE status = ?";
		String discountStr = jdbc.queryForObject(sql, String.class, "enchantdiscount");
		int discount = Integer.parseInt(discountStr);
		if (discount > 30) {
			discount = 30;
		}
		if (discount < 0) {
			discount = 0;
		}
		return discount;
	}

	public EnchantmentItemPrice getShopEnchantmentItem(int id) {
		int discount = getEnchantmentDiscount();
		String sql = "SELECT id, bukkitname, price, category FROM enchantments WHERE id = ?";
		EnchantmentItemPrice ench = jdbc.query(sql, enchantmentItemPriceExtractor, id);
		ench.setDiscount(discount);
		return ench;
	}

	public List<EnchantmentItem> getEnchantmentItemsForOrder(int id) {
		String sql = "select e.id, e.bukkitname, si.level from shopitemenchantments as si join enchantments as e on e.id = si.enchantmentid where si.shopitemid = ?";
		return jdbc.query(sql, enchantmentItemListExtractor, id);
	}

	public ItemShopItemPrice getItemShopItem(int id) {
		int discount = getEnchantmentDiscount();
		String sql = "SELECT id, name, price, mc_item FROM shopitems WHERE id = ?";
		ItemShopItemPrice item = jdbc.query(sql, itemShopItemPriceExtractor, id);
		item.setDiscount(discount);
		return item;
	}

	public List<ItemShopItemPrice> getBoughtItemShopItems(int userid) {
		int discount = getEnchantmentDiscount();
		String sql = "SELECT si.id, si.name, si.price, si.mc_item FROM shopitemshop AS sis JOIN shopitems AS si ON si.id = sis.itemid WHERE used = 0 AND sis.userid = ?";
		List<ItemShopItemPrice> list = jdbc.query(sql, itemShopItemPriceListExtractor, userid);
		for (ItemShopItemPrice item : list) {
			item.setDiscount(discount);
		}
		return list;
	}

	public List<ItemShopItemPrice> getItemShopItems() {
		int discount = getEnchantmentDiscount();
		String sql = "SELECT id, name, price, mc_item FROM shopitems";
		List<ItemShopItemPrice> list = jdbc.query(sql, itemShopItemPriceListExtractor);
		for (ItemShopItemPrice item : list) {
			item.setDiscount(discount);
		}
		return list;
	}

	public void buyItemShopItem(int userid, int itemid) {
		String sql = "INSERT INTO shopitemshop (userid, itemid) VALUES (?, ?);";
		jdbc.update(sql, userid, itemid);
	}

	public List<EnchantmentItemPrice> getBoughtShopEnchantmentItems(int userid) {
		int discount = getEnchantmentDiscount();
		String sql = "SELECT e.id, e.bukkitname, e.price, e.category FROM enchantmentshop AS es JOIN enchantments AS e ON e.id = es.itemid WHERE es.used = 0 AND es.userid = ?";
		List<EnchantmentItemPrice> list = jdbc.query(sql, enchantmentItemPriceListExtractor, userid);
		for (EnchantmentItemPrice item : list) {
			item.setDiscount(discount);
		}
		return list;
	}

	public List<EnchantmentItemPrice> getShopEnchantmentItems() {
		int discount = getEnchantmentDiscount();
		String sql = "SELECT id, bukkitname, price, category FROM enchantments";
		List<EnchantmentItemPrice> list = jdbc.query(sql, enchantmentItemPriceListExtractor);
		for (EnchantmentItemPrice item : list) {
			item.setDiscount(discount);
		}
		return list;
	}

	public void buyEnchantment(int userid, int itemid) {
		String sql = "INSERT INTO enchantmentshop (userid, itemid) VALUES (?, ?);";
		jdbc.update(sql, userid, itemid);
	}
}
