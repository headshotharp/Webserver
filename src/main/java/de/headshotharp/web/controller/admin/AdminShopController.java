package de.headshotharp.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;

@Controller
@RequestMapping("/admin/shop")
public class AdminShopController extends DefaultController {
	public static PermissionsGroup permissionsGroup = PermissionsGroup.ADMIN;

	@GetMapping("")
	String shop(@ModelAttribute("ControllerData") ControllerData cd) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup)) {
				int discount = cd.getDataProvider().getEnchantmentDiscount();
				cd.getModel().addAttribute("content", Bootstrap.button("Zurück", "/admin", ButtonType.DEFAULT)
						+ "<br /><hr /><h3>Administration Shop</h3><form method='post' action='/admin/shop'><label>Rabatt (in %, 0 bis 30):</label><input type='text' class='form-control' name='discount' value='"
						+ discount
						+ "' /><br /><input class='btn btn-success' type='submit' value='Bestätigen' /></from>");
			} else {
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		} else {
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@PostMapping("")
	String pollAdd(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("discount") String s_discount) {
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn()) {
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup)) {
				String html = "";
				int discount = 0;
				try {
					discount = Integer.parseInt(s_discount);
					if (discount >= 0 && discount <= 30) {
						cd.getDataProvider().setEnchantmentDiscount(discount);
						int db_discount = cd.getDataProvider().getEnchantmentDiscount();
						html = "<p>Rabatt für Enchantments auf <b>" + db_discount + "%</b> gesetzt.</p>"
								+ Bootstrap.button("Zurück", "/admin/shop", ButtonType.SUCCESS);
					} else {
						html = "<p>Deine Eingabe war ungültig.</p>"
								+ Bootstrap.button("Zurück", "/admin/shop", ButtonType.SUCCESS);
					}
				} catch (NumberFormatException e) {
					html = "<p>Deine Eingabe war ungültig.</p>"
							+ Bootstrap.button("Zurück", "/admin/shop", ButtonType.SUCCESS);
				}
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
