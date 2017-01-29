package de.headshotharp.web.controller.admin;

import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.DataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.Utils;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;

@Controller
@RequestMapping("/admin/permissions")
public class AdminPermissionsController extends DefaultController
{
	public static PermissionsGroup permissionsGroup = PermissionsGroup.ADMIN;

	@GetMapping("")
	String permissions(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			cd.getModel().addAttribute("player", player);
			if (player.hasPermission(permissionsGroup))
			{
				String permissionsFile = DataProvider.getProperties().getProperty("path.permissions");
				String html = Bootstrap.button("Zurück", "/admin", ButtonType.DEFAULT) + "<br /><hr /><h3>Administration Permissions</h3><p>Aktualisiere Datenbank aus Permissions Datei</p><form method='post' action='/admin/permissions'><input class='form-control' type='text' name='filename' value='" + permissionsFile + "' /><br /><input class='btn btn-success' type='submit' value='Aktualisieren' /></form>";
				cd.getModel().addAttribute("content", html);
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@PostMapping("")
	String permissionsPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("filename") String permissionsFile)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				if (new File(permissionsFile).exists())
				{
					String result = Utils.copyPermissionsIntoDatabase(cd.getDataProvider(), permissionsFile);
					cd.getModel().addAttribute("content", "<h3>Administration Permissions</h3><p>Log:</p>" + result + "<br />" + Bootstrap.button("Zurück", "/admin/permissions", ButtonType.SUCCESS));
				}
				else
				{
					cd.getModel().addAttribute("content", "<h3>Administration Permissions</h3><p>Fehler: Die Datei existiert nicht!</p>" + Bootstrap.button("Zurück", "/admin/permissions", ButtonType.SUCCESS));
				}
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}
}
