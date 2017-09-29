package de.headshotharp.web.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.GeneralDataProvider;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.graphics.SvgDonut;
import de.headshotharp.web.util.graphics.SvgDonut.SvgDonutpart;

@Controller
public class ServerController extends DefaultController {
	@Autowired
	private UserDataProvider userDataProvider;

	@Autowired
	private GeneralDataProvider generalDataProvider;

	@RequestMapping("/server")
	String server(@ModelAttribute("ControllerData") ControllerData cd,
			@RequestParam(value = "month", required = false, defaultValue = "false") boolean month) {
		// player, table, donut
		int maxPlayers = 7;
		int colorNumber = 0;
		// block break
		List<Player> topList = month ? userDataProvider.getPlayerListTopBlockBreakMonth()
				: userDataProvider.getPlayerListTopBlockBreak();
		SvgDonut svgDonutBreak;
		StringBuilder tableBreak = new StringBuilder();
		{
			int totalBlockBreak = month ? generalDataProvider.getServerTotalBlockBreakMonth()
					: generalDataProvider.getServerTotalBlockBreak();
			int restBreak = totalBlockBreak;
			svgDonutBreak = new SvgDonut("donut_block_break", "donut_block_break", "data_block_break");
			int i = 0;
			tableBreak.append(
					"<table class='table-sm table-bordered'><tr><th>Nr.</th><th>Spieler</th><th>Abgebaute Blöcke</th></tr>");
			for (Player p : topList) {
				if (p.color == null) {
					p.color = StaticConfig.COLORS_BASE_DEFAULT[colorNumber % StaticConfig.COLORS_BASE_DEFAULT.length];
					colorNumber++;
				}
				svgDonutBreak.addPart(new SvgDonutpart(p.name, p.color, p.block_break));
				restBreak -= p.block_break;
				i++;
				tableBreak.append("<tr><td style='background-color: " + CommonUtils.colorToHtml(p.color) + "'>" + i
						+ "</td><td><p class='nowrap'>" + p.getShortnameHtml() + "</p></td><td class='text-right'>"
						+ p.getBlockBreakFormat() + "</td></tr>");
				if (i >= maxPlayers) {
					break;
				}
			}
			tableBreak
					.append("<tr><td style='background-color: " + CommonUtils.colorToHtml(StaticConfig.COLOR_REST_GRAY)
							+ "'></td><td><p>Rest</p></td><td class='text-right'>" + CommonUtils.decimalDots(restBreak)
							+ "</td></tr>");
			svgDonutBreak.addPart(new SvgDonutpart("Rest", StaticConfig.COLOR_REST_GRAY, restBreak));
			tableBreak.append("<tr><td></td><td><p>Gesamt</p></td><td class='text-right'>"
					+ CommonUtils.decimalDots(totalBlockBreak) + "</td></tr>");
			tableBreak.append("</table>");
		}
		// block place
		SvgDonut svgDonutPlace;
		StringBuilder tablePlace = new StringBuilder();
		{
			Collections.sort(topList, Player.COMPARATOR_BLOCK_PLACE);
			int totalBlockPlace = month ? generalDataProvider.getServerTotalBlockPlaceMonth()
					: generalDataProvider.getServerTotalBlockPlace();
			int restPlace = totalBlockPlace;
			svgDonutPlace = new SvgDonut("donut_block_place", "donut_block_place", "data_block_place");
			int i = 0;
			tablePlace.append(
					"<table class='table-sm table-bordered'><tr><th>Nr.</th><th>Spieler</th><th>Platzierte Blöcke</th></tr>");
			for (Player p : topList) {
				if (p.color == null) {
					p.color = StaticConfig.COLORS_BASE_DEFAULT[colorNumber % StaticConfig.COLORS_BASE_DEFAULT.length];
					colorNumber++;
				}
				svgDonutPlace.addPart(new SvgDonutpart(p.name, p.color, p.block_place));
				restPlace -= p.block_place;
				i++;
				tablePlace.append("<tr><td style='background-color: " + CommonUtils.colorToHtml(p.color) + "'>" + i
						+ "</td><td><p class='text-nowrap'>" + p.getShortnameHtml() + "</p></td><td class='text-right'>"
						+ p.getBlockPlaceFormat() + "</td></tr>");
				if (i >= maxPlayers) {
					break;
				}
			}
			tablePlace
					.append("<tr><td style='background-color: " + CommonUtils.colorToHtml(StaticConfig.COLOR_REST_GRAY)
							+ "'></td><td><p>Rest</p></td><td class='text-right'>" + CommonUtils.decimalDots(restPlace)
							+ "</td></tr>");
			svgDonutPlace.addPart(new SvgDonutpart("Rest", StaticConfig.COLOR_REST_GRAY, restPlace));
			tablePlace.append("<tr><td></td><td><p>Gesamt</p></td><td class='text-right'>"
					+ CommonUtils.decimalDots(totalBlockPlace) + "</td></tr>");
			tablePlace.append("</table>");
		}
		cd.getModel().addAttribute("thismonth", month);
		cd.getModel().addAttribute("template", "server");
		cd.getModel().addAttribute("tableBreak", tableBreak.toString());
		cd.getModel().addAttribute("tablePlace", tablePlace.toString());
		cd.getModel().addAttribute("srcscripts", new String[] { "/js/progressbar.js" });
		cd.getModel().addAttribute("scripts", new String[] { svgDonutBreak.toString() + svgDonutPlace.toString(),
				"$(document).ready(function(){setTimeout(updateServerProgressbars(), 200);});" });
		return "index";
	}
}
