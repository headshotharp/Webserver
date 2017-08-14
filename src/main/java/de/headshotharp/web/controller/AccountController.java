package de.headshotharp.web.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.auth.RegistrationStatus;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.EnchantmentItemPrice;
import de.headshotharp.web.data.type.ItemShopItemPrice;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.Timeline;
import de.headshotharp.web.util.DateTime;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;
import de.headshotharp.web.util.graphics.Svg;

@Controller
public class AccountController extends DefaultController
{

	@RequestMapping("/account")
	String account(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam(value = "year", required = false, defaultValue = "0") int year, @RequestParam(value = "month", required = false, defaultValue = "0") int month)
	{
		if (cd.getAuthentication().isLoggedIn())
		{
			DateTime now = DateTime.now();
			if (year < 1) year = now.year;
			if (month < 1) month = now.month;
			DateTime dt = new DateTime();
			dt.year = year;
			dt.month = month;
			DateTime prev = dt.prevMonth();
			int userid = cd.getAuthentication().getPlayer().id;
			Timeline timeline = cd.getDataProvider().getPlayerTimelineMonth(userid, dt.year, dt.month);
			String svg = "<div class='row'><div class='col-xs-4'>";
			if (!cd.getDataProvider().getPlayerTimelineStart(userid).isSameMonth(dt))
			{
				svg += "<a href='/account?year=" + prev.year + "&month=" + prev.month + "#stats' class='btn btn-success'><span class='glyphicon glyphicon-arrow-left'></span> <span class='desktop-only'>Vorheriger Monat</span></a>";
			}
			svg += "</div><div class='col-xs-4 text-center'><b>" + dt.toMonthAndYear() + "</b></div><div class='col-xs-4 text-center'>";
			if (year != now.year || month != now.month)
			{
				DateTime next = dt.nextMonth();
				svg += " <a href='/account?year=" + next.year + "&month=" + next.month + "#stats' class='btn btn-success pull-right'><span class='desktop-only'>Nächster Monat</span> <span class='glyphicon glyphicon-arrow-right'></span></a>";
			}
			svg += "</div></div><br />";
			svg += Svg.lineGraph(timeline);
			cd.getModel().addAttribute("svg", svg);
			cd.getModel().addAttribute("srcscripts", new String[]
			{ "/js/progressbar.js" });
			cd.getModel().addAttribute("scripts", new String[]
			{ "$(document).ready(function(){updateUserProgressbars();});" });
			cd.getModel().addAttribute("template", "account");
		}
		else
		{
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Accountbereich zu betreten.</p>");
		}
		return "index";
	}

	@GetMapping("/inventory")
	public String inventory(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			StringBuilder html = new StringBuilder();
			html.append("");
			List<ItemShopItemPrice> itemShopItems = cd.getDataProvider().getBoughtItemShopItems(cd.getAuthentication().getPlayer().id);
			List<EnchantmentItemPrice> enchantments = cd.getDataProvider().getBoughtShopEnchantmentItems(cd.getAuthentication().getPlayer().id);
			if (itemShopItems.size() == 0 && enchantments.size() == 0)
			{
				html.append("<p>Du hast derzeit keine Items oder Enchantments in deinem Inventar.</p>");
			}
			else
			{
				if (itemShopItems.size() > 0)
				{
					html.append("<h3>Gekaufte Items:</h3><ul>");
					for (ItemShopItemPrice item : itemShopItems)
					{
						html.append("<li>" + item.getName() + "</li>");
					}
					html.append("</ul>");
				}
				if (enchantments.size() > 0)
				{
					html.append("<h3>Gekaufte Enchantments:</h3><ul>");
					for (EnchantmentItemPrice item : enchantments)
					{
						html.append("<li>" + item.getEnch().getNormalName() + "</li>");
					}
					html.append("</ul>");
				}
			}
			cd.getModel().addAttribute("content", html.toString());
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Accountbereich zu betreten.</p>");
		}
		return "index";
	}

	@GetMapping("/settings")
	public String accountsettings(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			String html = "<h3>Einstellungen</h3><h4>Account: " + player.name + (player.realname.length() > 0 ? " <small>(" + player.realname + ")</small>" : "") + "</h4>";
			html += "<hr /><div class='row'><div class='col-md-6'><h3>Passwort ändern:</h3><form method='post' action='/changepw'><label>Altes Passwort:</label><input class='form-control' type='password' name='oldpw' /><label>Neues Passwort:</label><input class='form-control' type='password' name='newpw' /><label>Passwort wiederholen:</label><input class='form-control' type='password' name='newpw2' /><br /><input class='btn btn-success' type='submit' value='Passwort ändern' /></form></div></div>";
			cd.getModel().addAttribute("content", html);
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Accountbereich zu betreten.</p>");
		}
		return "index";
	}

	@PostMapping("/changepw")
	String changepw(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("oldpw") String oldpw, @RequestParam("newpw") String newpw, @RequestParam("newpw2") String newpw2)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			RegistrationStatus status = cd.getAuthentication().changePasswordWithOldPassword(newpw, newpw2, oldpw);
			String html = "<p>";
			if (status == RegistrationStatus.WRONGE_CODE)
			{
				html += "Das eingegebene (alte) Passwort ist falsch.";
			}
			else if (status == RegistrationStatus.SUCCESSFUL_REGISTERED)
			{
				html += "Dein Passwort wurde geändert.";
			}
			else
			{
				html += status.getMessage();
			}
			html += "</p><br />" + Bootstrap.button("Zurück", "/settings", ButtonType.SUCCESS);
			cd.getModel().addAttribute("content", html);
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Accountbereich zu betreten.</p>");
		}
		return "index";
	}
}
