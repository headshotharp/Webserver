package de.headshotharp.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;

@Controller
@RequestMapping("/admin")
public class AdminController extends DefaultController {
	@RequestMapping("")
	String admin(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(PermissionsGroup.DEVELOPMENT)) {
				String html = "<h3>Administration</h3>";
				html += Bootstrap.button("News", "/admin/news", ButtonType.DEFAULT);
				html += Bootstrap.button("Umfrage", "/admin/poll", ButtonType.DEFAULT);
				html += Bootstrap.button("Spieler", "/admin/player", ButtonType.DEFAULT);
				html += Bootstrap.button("Skins", "/admin/skins", ButtonType.DEFAULT);
				html += Bootstrap.button("Permissions", "/admin/permissions", ButtonType.DEFAULT);
				html += Bootstrap.button("Shop", "/admin/shop", ButtonType.DEFAULT);
				cd.getModel().addAttribute("content", html);
			} else {
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		} else {
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}
}
