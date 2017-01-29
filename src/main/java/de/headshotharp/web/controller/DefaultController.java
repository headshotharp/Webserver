package de.headshotharp.web.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.DataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.ServerStatus;

@Controller
public class DefaultController
{
	@ModelAttribute("ControllerData")
	public ControllerData getControllerData(Model model, HttpSession session, @CookieValue(value = Config.COOKIE_NAME_USERID, defaultValue = "") String cookieuserid, @CookieValue(value = Config.COOKIE_NAME_TOKEN, defaultValue = "") String cookietoken)
	{
		// create DataProvider
		DataProvider dp = new DataProvider();
		// serverstatus
		ServerStatus serverStatus = dp.getServerStatus();
		model.addAttribute("server", serverStatus);
		// authentication
		Authentication auth = new Authentication(session, dp);
		boolean loggedin = auth.isLoggedIn();
		if (!loggedin) loggedin = auth.login(cookieuserid, cookietoken);
		model.addAttribute("loggedin", loggedin);
		if (loggedin)
		{
			Player player = auth.getPlayer();
			model.addAttribute("player", player);
		}
		// default stuff
		model.addAttribute("onlinelistscript", Player.getJavascriptArrayForInitialOnlineList(dp.getPlayerList()));
		// return ControllerData
		return new ControllerData(model, session, dp, auth);
	}
}
