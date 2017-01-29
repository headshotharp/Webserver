package de.headshotharp.web.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.auth.RegistrationStatus;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.DataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.ServerStatus;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;

@Controller
public class AuthenticationController extends DefaultController
{
	@GetMapping("/login")
	public String getLogin(@ModelAttribute("ControllerData") ControllerData cd)
	{
		if (cd.getAuthentication().isLoggedIn())
		{
			cd.getModel().addAttribute("bg", "");
			cd.getModel().addAttribute("content", "<p>Du bist bereits eingeloggt</p>");
		}
		else
		{
			cd.getModel().addAttribute("template", "login");
		}
		return "index";
	}

	@PostMapping("/login")
	public String postLogin(@ModelAttribute("ControllerData") ControllerData cd, HttpServletResponse response, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam(value = "stay", required = false, defaultValue = "false") boolean stay)
	{
		cd.getAuthentication().login(username, Authentication.MD5(password), stay, response);
		String text;
		if (cd.getAuthentication().isLoggedIn())
		{
			text = "Erfolgreich eingeloggt.";
			cd.getModel().addAttribute("player", cd.getAuthentication().getPlayer());
			cd.getModel().addAttribute("loggedin", true);
		}
		else
		{
			text = "Falscher Benutzername oder Passwort.";
		}
		cd.getModel().addAttribute("bg", "");
		cd.getModel().addAttribute("content", "<p>" + text + "</p>");
		return "index";
	}

	@GetMapping("/register")
	String getRegister(@ModelAttribute("ControllerData") ControllerData cd)
	{
		return getLogin(cd);
	}

	@PostMapping("/register")
	String postRegister(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("password2") String password2, @RequestParam("code") String code)
	{
		RegistrationStatus status = cd.getAuthentication().register(username, password, password2, code);
		cd.getModel().addAttribute("bg", "");
		cd.getModel().addAttribute("content", "<p>" + status.getMessage() + "</p><br /><a class='btn btn-default' href='/register'>Zurück</a>");
		return "index";
	}

	@RequestMapping("/logout")
	public String logout(Model model, HttpSession session, HttpServletResponse response)
	{
		DataProvider dp = new DataProvider();
		Authentication.logout(dp, session, response);
		ServerStatus serverStatus = dp.getServerStatus();
		model.addAttribute("server", serverStatus);
		model.addAttribute("loggedin", false);
		model.addAttribute("onlinelistscript", Player.getJavascriptArrayForInitialOnlineList(dp.getPlayerList()));
		model.addAttribute("bg", "");
		model.addAttribute("content", "<p>Erfolgreich ausgeloggt</p>");
		dp.close();
		return "index";
	}

	@RequestMapping("/resetpassword")
	String resetPassword(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			cd.getModel().addAttribute("content", "<p>Du bist eingeloggt. Sicher, dass du dein Passwort vergessen hast? Bitte logge dich <a class='btn btn-sm btn-success' href='/logout'>hier</a> zuerst aus.</p>");
		}
		else
		{
			cd.getModel().addAttribute("template", "resetpassword");
		}
		return "index";
	}

	@PostMapping("/resetpassword")
	String resetPasswordPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("password2") String password2, @RequestParam("code") String code)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			cd.getModel().addAttribute("content", "<p>Du bist eingeloggt. Sicher, dass du dein Passwort vergessen hast? Bitte logge dich <a class='btn btn-sm btn-success' href='/logout'>hier</a> zuerst aus.</p>");
		}
		else
		{
			String html = "<p>";
			RegistrationStatus status = cd.getAuthentication().forgotPasswd(username, password, password2, code);
			if (status == RegistrationStatus.SUCCESSFUL_REGISTERED)
			{
				html += "Dein Passwort wurde geändert. Du kannst dich <a class='btn btn-sm btn-success' href='/login'>hier</a> einloggen.";
			}
			else
			{
				html += status.getMessage();
			}
			html += "</p>" + Bootstrap.button("Zurück", "/resetpassword", ButtonType.SUCCESS);
			cd.getModel().addAttribute("content", html);
		}
		return "index";
	}
}
