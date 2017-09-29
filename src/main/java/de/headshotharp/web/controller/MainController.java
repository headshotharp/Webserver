package de.headshotharp.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.NewsDataProvider;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.News;
import de.headshotharp.web.data.type.Player;

@Controller
public class MainController extends DefaultController {
	@Autowired
	private UserDataProvider userDataProvider;

	@Autowired
	private NewsDataProvider newsDataProvider;

	@RequestMapping("/")
	String index(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("content", News.toHtml(newsDataProvider.getNews(false)));
		cd.getModel().addAttribute("specialstyle", "news.css");
		return "index";
	}

	@RequestMapping("/playerlist")
	String playerlist(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("content", Player.getPlayerListHtml(userDataProvider.getPlayerListActive()));
		cd.getModel().addAttribute("specialstyle", "playerlist.css");
		return "index";
	}

	@RequestMapping("/bestlist")
	String bestlist(@ModelAttribute("ControllerData") ControllerData cd) {
		String jsUserId = "var loggedinUserid = ";
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			jsUserId += player.id + ";";
		} else {
			jsUserId += "-1;";
		}
		cd.getModel().addAttribute("content", "<div class='table-sidescroll'><table id='bestlist'></table></div>");
		cd.getModel().addAttribute("srcscripts", new String[] { "/js/bestlist.js" });
		cd.getModel().addAttribute("scripts",
				new String[] { jsUserId + Player.getJavascriptData(userDataProvider.getPlayerListActive()) });
		cd.getModel().addAttribute("specialstyle", "bestlist.css");
		return "index";
	}

	@RequestMapping("/gift")
	String gift(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.giftReady) {
				int giftAmount = player.getGift(userDataProvider);
				cd.getModel().addAttribute("content",
						"<p>Du hast " + giftAmount + " " + StaticConfig.VALUE_CURRENCY_HTML + " erhalten.</p>");
			} else {
				cd.getModel().addAttribute("content",
						"<p>Du hast dein Geschenk bereits abgeholt. Bitte komme morgen wieder.</p>");
			}
		} else {
			cd.getModel().addAttribute("content",
					"<p>Du musst eingeloggt sein, um dein tägliches Geschenk zu erhalten.</p>");
		}
		return "index";
	}

	@RequestMapping("/gallery")
	String gallery(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("bg", "");
		cd.getModel().addAttribute("content", "<p>Die Galerie befindet sich im Aufbau.</p>");
		return "index";
	}

	@RequestMapping("/maps")
	String maps(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("template", "maps");
		return "index";
	}

	@RequestMapping("/chat")
	String chat(@ModelAttribute("ControllerData") ControllerData cd) {
		if (cd.getAuthentication().isLoggedIn()) {
			cd.getModel().addAttribute("nochat", "");
			cd.getModel().addAttribute("template", "chat");
		} else {
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content",
					"<p>Du musst dich <a href='/login'>einloggen</a> um den Chat zu sehen.</p>");
		}
		return "index";
	}
}
