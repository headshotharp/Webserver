package de.headshotharp.web.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.DefaultController;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;

@Controller
@RequestMapping("/admin/player")
public class AdminPlayerController extends DefaultController {
	@Autowired
	private UserDataProvider userDataProvider;

	@RequestMapping("")
	String player(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(PermissionsGroup.DEVELOPMENT)) {
				userDataProvider.updateUserStatus();
				StringBuilder str = new StringBuilder();
				str.append(Bootstrap.button("Zurück", "/admin", ButtonType.DEFAULT)
						+ "<br /><hr /><h3>Administration Spieler</h3>");
				List<Player> list = userDataProvider.getPlayerListOrderStatus();
				str.append(
						"<table class='table table-bordered'><tr class='active'><th>ID</th><th>Name</th><th>Realname</th><th>Status</th><th>Aktion</th></tr>");
				for (Player p : list) {
					str.append("<tr class='" + p.status.getBootstrapClass() + "'><td>" + p.id + "</td><td>"
							+ p.getPrefixName() + "</td><td>" + p.realname + "</td><td>" + p.status
							+ "</td><td><a href='/admin/player/change/" + p.id
							+ "'><span class='glyphicon glyphicon-pencil'></span></a></td></tr>");
				}
				str.append("</table>");
				cd.getModel().addAttribute("content", str.toString());
			} else {
				cd.getModel().addAttribute("content", StaticConfig.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		} else {
			cd.getModel().addAttribute("content", StaticConfig.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@RequestMapping("/change/{userid}")
	String playerChange(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int userid) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(PermissionsGroup.ADMIN)) {
				Player p = userDataProvider.getPlayerById(userid);
				if (p != null) {
					String html = Bootstrap.button("Zurück", "/admin/player", ButtonType.DEFAULT)
							+ "<br /><hr /><h3>Administration Spieler</h3><p>Spieler bearbeiten: <b>"
							+ p.getPrefixName()
							+ "</b></p><form accept-charset='UTF-8' method='post' action='/admin/player/change'>";
					html += "<div class='form-group'><label>Realname:</label><input type='text' class='form-control' name='realname' value='"
							+ p.realname + "'></div>";
					html += "<input type='hidden' name='userid' value='" + userid + "' class='btn btn-success' />";
					html += "<input type='submit' value='Bearbeiten' class='btn btn-success' /> <a class='btn btn-default' href='/admin/player'>Abbrechen</a>";
					html += "</form>";
					cd.getModel().addAttribute("content", html);
				} else {
					cd.getModel().addAttribute("content", "<p>Es gibt keinen Spieler mit der ID " + userid + ".</p>");
				}
			} else {
				cd.getModel().addAttribute("content", StaticConfig.VALUE_TEXT_ADMINAREA_NOT_PERMITTED_WRITE);
			}
		} else {
			cd.getModel().addAttribute("content", StaticConfig.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@PostMapping("/change")
	String playerChangePost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("userid") int userid,
			@RequestParam("realname") String realname) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(PermissionsGroup.ADMIN)) {
				Player p = userDataProvider.getPlayerById(userid);
				if (p != null) {
					userDataProvider.setPlayerRealname(userid, realname);
					String html = Bootstrap.button("Zurück", "/admin/player", ButtonType.DEFAULT) + "<p>Spieler <b>"
							+ p.getPrefixName() + "</b> erfolgreich bearbeitet.</p>";
					cd.getModel().addAttribute("content", html);
				} else {
					cd.getModel().addAttribute("content", "<p>Es gibt keinen Spieler mit der ID " + userid + ".</p>");
				}
			} else {
				cd.getModel().addAttribute("content", StaticConfig.VALUE_TEXT_ADMINAREA_NOT_PERMITTED_WRITE);
			}
		} else {
			cd.getModel().addAttribute("content", StaticConfig.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}
}
