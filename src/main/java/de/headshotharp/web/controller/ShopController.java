package de.headshotharp.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.EnchantmentItemPrice;
import de.headshotharp.web.data.type.ItemShopItemPrice;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.controller.DefaultController;

@Controller
public class ShopController extends DefaultController
{
	@RequestMapping("/shop")
	String shop(@ModelAttribute("ControllerData") ControllerData cd)
	{
		String html = "<div class='shop'><h1>Enchantments</h1><img src='/img/mc_enchantement_book.png'><p>Hier kannst du alle in Minecraft verfügbaren Enchantments (Verzauberungen) kaufen.</p><a href='/shop/enchantments/'><h2>zum Shop</h2></a></div>";
		html += "<div class='shop'><h1>Items</h1><img src='/img/mc_items.png'><p>Hier kannst du Items und Pakete kaufen.</p><a href='/shop/items/'><h2>zum Shop</h2></a></div>";
		cd.getModel().addAttribute("content", html);
		cd.getModel().addAttribute("specialstyle", "shop.css");
		return "index";
	}

	/*
	 * Item Shop
	 */

	@RequestMapping("/shop/items")
	String shopItem(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("content", ItemShopItemPrice.toHtml(cd.getDataProvider().getItemShopItems()));
		cd.getModel().addAttribute("specialstyle", "shop.css");
		return "index";
	}

	@GetMapping("/shop/items/buy/{itemid}")
	String shopItemBuy(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int itemid)
	{
		if (cd.getAuthentication().isLoggedIn())
		{
			ItemShopItemPrice item = cd.getDataProvider().getItemShopItem(itemid);
			if (cd.getAuthentication().getPlayer().money >= item.getPrice())
			{
				String html = "<div class='bg' style='text-align: left;'><p>Möchtest du <b>" + item.getName() + "</b> wirklich für <b>" + CommonUtils.decimalDots(item.getPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</b> kaufen?</p>";
				html += "<form class='inline' method='post' action='/shop/items/buy'><input type='hidden' name='itemid' value='" + itemid + "' /><input type='submit' class='btn btn-success' value='Kaufen' /></form> <a href='/shop/items' class='btn btn-default'>Abbrechen</a>";
				html += "</div>" + item.toHTML();
				cd.getModel().addAttribute("content", html);
				cd.getModel().addAttribute("specialstyle", "shop.css");
			}
			else
			{
				String html = "<div class='bg' style='text-align: left;'><p>Du kannst dir <b>" + item.getName() + "</b> für <b>" + CommonUtils.decimalDots(item.getPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</b> nicht leisten.</p>";
				html += "<a href='/shop/items' class='btn btn-default'>Zurück</a>";
				html += "</div>";
				cd.getModel().addAttribute("content", html);
			}
		}
		else
		{
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um etwas im Shop zu kaufen.</p>");
		}
		return "index";
	}

	@PostMapping("/shop/items/buy")
	String shopItemBuyPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("itemid") int itemid)
	{
		if (cd.getAuthentication().isLoggedIn())
		{
			ItemShopItemPrice item = cd.getDataProvider().getItemShopItem(itemid);
			Player player = cd.getAuthentication().getPlayer();
			if (player.money >= item.getPrice())
			{
				player.addMoney(cd.getDataProvider(), -item.getPrice());
				cd.getDataProvider().buyItemShopItem(player.id, itemid);
				String html = "<p>Vielen Dank für den Kauf von <b>" + item.getName() + "</b>.</p>";
				html += "<a href='/shop/items' class='btn btn-default'>Zurück</a>";
				cd.getModel().addAttribute("bg", "");
				cd.getModel().addAttribute("content", html);
			}
			else
			{
				String html = "<div class='bg' style='text-align: left;'><p>Du kannst dir <b>" + item.getName() + "</b> für <b>" + CommonUtils.decimalDots(item.getPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</b> nicht leisten.</p>";
				html += "<a href='/shop/items' class='btn btn-default'>Zurück</a>";
				html += "</div>";
				cd.getModel().addAttribute("content", html);
			}
		}
		else
		{
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um etwas im Shop zu kaufen.</p>");
		}
		return "index";
	}

	/*
	 * Enchantment Shop
	 */
	@RequestMapping("/shop/enchantments")
	String shopEnchantment(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("content", EnchantmentItemPrice.toHtml(cd.getDataProvider().getShopEnchantmentItems()));
		cd.getModel().addAttribute("specialstyle", "shop.css");
		return "index";
	}

	@GetMapping("/shop/enchantments/buy/{itemid}")
	String shopBuy(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int itemid)
	{
		if (cd.getAuthentication().isLoggedIn())
		{
			EnchantmentItemPrice ench = cd.getDataProvider().getShopEnchantmentItem(itemid);
			if (cd.getAuthentication().getPlayer().money >= ench.getPrice())
			{
				String html = "<div class='bg' style='text-align: left;'><p>Möchtest du <b>" + ench.getEnch().getNormalName() + "</b> wirklich für <b>" + CommonUtils.decimalDots(ench.getPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</b> kaufen?</p>";
				html += "<form class='inline' method='post' action='/shop/enchantments/buy'><input type='hidden' name='itemid' value='" + itemid + "' /><input type='submit' class='btn btn-success' value='Kaufen' /></form> <a href='/shop/enchantments' class='btn btn-default'>Abbrechen</a>";
				html += "</div>" + ench.toHTML();
				cd.getModel().addAttribute("content", html);
				cd.getModel().addAttribute("specialstyle", "shop.css");
			}
			else
			{
				String html = "<div class='bg' style='text-align: left;'><p>Du kannst dir <b>" + ench.getEnch().getNormalName() + "</b> für <b>" + CommonUtils.decimalDots(ench.getPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</b> nicht leisten.</p>";
				html += "<a href='/shop/enchantments' class='btn btn-default'>Zurück</a>";
				html += "</div>";
				cd.getModel().addAttribute("content", html);
			}
		}
		else
		{
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um etwas im Shop zu kaufen.</p>");
		}
		return "index";
	}

	@PostMapping("/shop/enchantments/buy")
	String shopBuyPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("itemid") int itemid)
	{
		if (cd.getAuthentication().isLoggedIn())
		{
			EnchantmentItemPrice ench = cd.getDataProvider().getShopEnchantmentItem(itemid);
			Player player = cd.getAuthentication().getPlayer();
			if (player.money >= ench.getPrice())
			{
				player.addMoney(cd.getDataProvider(), -ench.getPrice());
				cd.getDataProvider().buyEnchantment(player.id, itemid);
				String html = "<p>Vielen Dank für den Kauf von <b>" + ench.getEnch().getNormalName() + "</b>.</p>";
				html += "<a href='/shop/enchantments' class='btn btn-default'>Zurück</a>";
				cd.getModel().addAttribute("bg", "");
				cd.getModel().addAttribute("content", html);
			}
			else
			{
				String html = "<div class='bg' style='text-align: left;'><p>Du kannst dir <b>" + ench.getEnch().getNormalName() + "</b> für <b>" + CommonUtils.decimalDots(ench.getPrice()) + " " + Config.VALUE_CURRENCY_HTML + "</b> nicht leisten.</p>";
				html += "<a href='/shop/enchantments' class='btn btn-default'>Zurück</a>";
				html += "</div>";
				cd.getModel().addAttribute("content", html);
			}
		}
		else
		{
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um etwas im Shop zu kaufen.</p>");
		}
		return "index";
	}
}
