package de.headshotharp.web.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.controller.DefaultController;

@Controller
public class CustomErrorController extends DefaultController implements ErrorController
{
	private static final String PATH = "/error";

	@RequestMapping(value = PATH)
	public String error(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		cd.getModel().addAttribute("content", "<p>Fehler - Seite nicht gefunden.</p>");
		return "index";
	}

	@Override
	public String getErrorPath()
	{
		return PATH;
	}
}
