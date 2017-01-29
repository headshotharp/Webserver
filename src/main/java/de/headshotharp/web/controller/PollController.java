package de.headshotharp.web.controller;

import java.awt.Color;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.Poll;
import de.headshotharp.web.data.type.Poll.PollOption;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.util.graphics.SvgDonut;
import de.headshotharp.web.util.graphics.SvgDonut.SvgDonutpart;
import de.headshotharp.web.controller.DefaultController;

@Controller
public class PollController extends DefaultController
{
	@GetMapping("/poll")
	String poll(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		List<Poll> list = cd.getDataProvider().getPollBasicActiveList();
		String html = "<h3>Aktive Umfragen:</h3><ul>";
		for (Poll poll : list)
		{
			html += "<li><a href='/poll/" + poll.id + "'>" + poll.title + "</a></li>";
		}
		html += "</ul>";
		cd.getModel().addAttribute("content", html);
		return "index";
	}

	@GetMapping("/poll/{pollId}")
	String pollGet(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int pollId)
	{
		cd.getModel().addAttribute("bg", "");
		Poll poll = cd.getDataProvider().getPollWithOptions(pollId);
		if (poll != null)
		{
			if (cd.getAuthentication().isLoggedIn())
			{
				Player player = cd.getAuthentication().getPlayer();
				int result = cd.getDataProvider().getPollResult(poll.id, player.id);
				if (result > 0)
				{
					String pollresult = "<h3>Umfrage:</h3><p><b>" + poll.title + "</b></p><p>" + poll.description + "</p><ul>";
					for (PollOption option : poll.options)
					{
						if (option.id == result) pollresult += "<li class='check'><b>" + option.polloption + "</b></li>";
						else pollresult += "<li>" + option.polloption + "</li>";
					}
					pollresult += "</ul>";
					// ###
					List<PollOption> pollResults = cd.getDataProvider().getPollResults(poll.id);
					SvgDonut svgDonutResult = new SvgDonut("donut_result", "donut_result", "data_donut_result");
					StringBuilder tableResult = new StringBuilder();
					tableResult.append("<table class='table-sm table-bordered'><tr><th>Nr.</th><th>Option</th><th>Stimmen</th></tr>");
					int index = 0;
					int totalVotes = 0;
					for (PollOption po : pollResults)
					{
						Color c = Config.COLORS_BASE_DEFAULT[index++ % Config.COLORS_BASE_DEFAULT.length];
						svgDonutResult.addPart(new SvgDonutpart(po.polloption, c, po.resultAmount));
						tableResult.append("<tr><td style='background-color: " + CommonUtils.colorToHtml(c) + "'>" + index + "</td><td><p>" + po.polloption + "</p></td><td class='text-right'>" + po.resultAmount + "</td></tr>");
						totalVotes += po.resultAmount;
					}
					tableResult.append("<tr><td></td><td><p>Gesamt</p></td><td class='text-right'>" + totalVotes + "</td></tr>");
					tableResult.append("</table>");
					cd.getModel().addAttribute("pollresult", pollresult);
					cd.getModel().addAttribute("template", "poll");
					cd.getModel().addAttribute("table", tableResult.toString());
					cd.getModel().addAttribute("scripts", new String[]
					{ svgDonutResult.toString() });
				}
				else
				{
					String html = "<h3>Umfrage:</h3><p><b>" + poll.title + "</b></p><p>" + poll.description + "</p>";
					for (PollOption option : poll.options)
					{
						html += "<p><form class='inline-block' method='post' action='/poll'><input type='hidden' name='pollId' value='" + poll.id + "' /><input type='hidden' name='optionId' value='" + option.id + "' /><input class='btn-sm btn-primary' type='submit' value='Auswählen' /></form> " + option.polloption + "</p>";
					}
					cd.getModel().addAttribute("content", html);
				}
			}
			else
			{
				String html = "<h3>Umfrage:</h3><p><b>" + poll.title + "</b></p><p>" + poll.description + "</p><ul>";
				for (PollOption option : poll.options)
				{
					html += "<li>" + option.polloption + "</li>";
				}
				html += "</ul><br /><p>Bitte logge dich <a class='btn-sm btn-success' href='/login'>hier</a> ein, um an der Umfrage teilzunehmen.</p>";
				cd.getModel().addAttribute("content", html);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Diese Umfrage existiert nicht.</p>");
		}
		return "index";
	}

	@PostMapping("/poll")
	String pollPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("pollId") int pollId, @RequestParam("optionId") int optionId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			cd.getDataProvider().addPollResult(pollId, player.id, optionId);
			cd.getModel().addAttribute("content", "<p>Danke, dass du an der Umfrage teilgenommen hast.</p>" + Bootstrap.button("Ergebnis", "/poll/" + pollId, ButtonType.SUCCESS));
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um an der Umfrage teilzunehmen.</p>");
		}
		return "index";
	}
}
