package de.headshotharp.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.DefaultController;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.Utils;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;

@Controller
@RequestMapping("/admin/skins")
public class AdminSkinController extends DefaultController {
	@Autowired
	private Config config;

	@Autowired
	private UserDataProvider userDataProvider;

	@GetMapping("")
	String skins(@ModelAttribute("ControllerData") ControllerData cd) {
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(PermissionsGroup.DEVELOPMENT)) {
				cd.getModel().addAttribute("srcscripts", new String[] { "/js/skinchange.js" });
				StringBuilder str = new StringBuilder();
				str.append(Bootstrap.button("Zurück", "/admin", ButtonType.DEFAULT)
						+ "<br /><hr /><h3>Administration Skins</h3><br /><div id='updateallskins'><a class='btn btn-default' href='javascript:void(0);' onClick='updateSkins();'>Alle aktualisieren</a></div><br /><table class='table-sm table-bordered'>");
				str.append("<tr><th>Name</th><th></th></tr>");
				for (Player p : userDataProvider.getPlayerListOrderedLastLogin()) {
					str.append("<tr><td>" + p.name
							+ "</th><th><form action='/admin/skins' method='post'><input type='hidden' name='userId' value='"
							+ p.id
							+ "' /><input class='btn btn-sm btn-success' type='submit' value='Aktualisieren' /></form></th></tr>");
				}
				str.append("</table>");
				cd.getModel().addAttribute("bg", "");
				cd.getModel().addAttribute("content", str.toString());
			} else {
				cd.getModel().addAttribute("bg", "");
				cd.getModel().addAttribute("content", "<p>Es ist dir nicht erlaubt Skins zu bearbeiten.</p>");
			}
		} else {
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Adminbereich zu sehen.</p>");
		}
		return "index";
	}

	@PostMapping("")
	String skinsPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("userId") int userId) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(PermissionsGroup.DEVELOPMENT)) {
				Player p = userDataProvider.getPlayerById(userId);
				if(p != null) {
					String base64 = Utils.downloadFullUserImage(p.name, true, config.getPath().getSkins(), userDataProvider, userId);
					String back = "<br /><a class='btn btn-success' href='/admin/skins'>Zurück</a>";
					if (base64.length() > 0) {
						cd.getModel().addAttribute("content",
								"<p>Skin erfolgreich heruntergeladen:</p><br /><img src='" + base64 + "' /><br />" + back);
					} else {
						cd.getModel().addAttribute("content",
								"<p>Fehler beim herunterladen des Skins, bitte versuche es später erneut.</p>" + back);
					}
				} else {
					cd.getModel().addAttribute("content", "<p>Der User mit der ID " + userId + " existiert nicht.</p>");
				}
			} else {
				cd.getModel().addAttribute("content", "<p>Es ist dir nicht erlaubt Skins zu bearbeiten.</p>");
			}
		} else {
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Adminbereich zu sehen.</p>");
		}
		return "index";
	}
}
