package de.headshotharp.web.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.GeneralDataProvider;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.ServerStatus;

@Controller
public class DefaultController {
	@Autowired
	private GeneralDataProvider generalDataProvider;

	@Autowired
	private UserDataProvider userDataProvider;

	@ModelAttribute("ControllerData")
	public ControllerData getControllerData(Model model, HttpSession session,
			@CookieValue(value = StaticConfig.COOKIE_NAME_USERID, defaultValue = "") String cookieuserid,
			@CookieValue(value = StaticConfig.COOKIE_NAME_TOKEN, defaultValue = "") String cookietoken) {
		// serverstatus
		ServerStatus serverStatus = generalDataProvider.getServerStatus();
		model.addAttribute("server", serverStatus);
		// authentication
		Authentication auth = new Authentication(session, userDataProvider);
		boolean loggedin = auth.isLoggedIn();
		if (!loggedin) {
			loggedin = auth.login(cookieuserid, cookietoken);
		}
		model.addAttribute("loggedin", loggedin);
		if (loggedin) {
			Player player = auth.getPlayer();
			model.addAttribute("player", player);
		}
		// default stuff
		model.addAttribute("onlinelistscript",
				Player.getJavascriptArrayForInitialOnlineList(userDataProvider.getPlayerList()));
		// return ControllerData
		return new ControllerData(model, session, auth);
	}
}
