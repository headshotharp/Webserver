package de.headshotharp.web.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.data.DataProvider;
import de.headshotharp.web.data.type.Chat;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.ServerStatus;
import de.headshotharp.web.util.Utils;

@Controller
@RequestMapping("/data")
public class DataController {
	@RequestMapping("/me/{request}")
	@ResponseBody
	String meRaw(HttpSession session, @PathVariable String request) {
		String data = "ERROR";
		DataProvider dp = new DataProvider();
		Authentication auth = new Authentication(session, dp);
		Player player = auth.getPlayer();
		if (player != null) {
			if (request.equals("blockbreakplacetoday")) {
				int block_break = player.getBlockBreakToday(dp);
				int block_place = player.getBlockPlaceToday(dp);
				data = block_break + Config.VALUE_SPLIT + block_place;
			} else {
				data = player.get(request);
			}
		}
		auth.close();
		return data;
	}

	@RequestMapping("/server/status")
	@ResponseBody
	String serverStatus() {
		DataProvider dp = new DataProvider();
		ServerStatus serverStatus = dp.getServerStatus();
		dp.close();
		return serverStatus.getAjaxEncode();
	}

	@RequestMapping("/server/blockbreakplacemonth")
	@ResponseBody
	String serverBlockBreakPlaceMonth() {
		DataProvider dp = new DataProvider();
		List<Player> list = dp.getPlayerListActive();
		int block_break = 0;
		int block_place = 0;
		for (Player p : list) {
			block_break += p.getBlockBreakThisMonth(dp);
			block_place += p.getBlockPlaceThisMonth(dp);
		}
		dp.close();
		return block_break + Config.VALUE_SPLIT + block_place;
	}

	@RequestMapping("/server/blockbreakplacemonth/update")
	@ResponseBody
	String serverBlockBreakPlaceMonthUpdate() {
		DataProvider dp = new DataProvider();
		List<Player> list = dp.getPlayerListActive();
		int block_break = 0;
		int block_place = 0;
		for (Player p : list) {
			block_break += p.getBlockBreakThisMonth(dp);
			block_place += p.getBlockPlaceThisMonth(dp);
		}
		dp.setBlockBreakMonthDb(block_break);
		dp.setBlockPlaceMonthDb(block_place);
		dp.close();
		return block_break + Config.VALUE_SPLIT + block_place;
	}

	@RequestMapping("/chat/init")
	@ResponseBody
	String chatInit(HttpSession session) {
		DataProvider dp = new DataProvider();
		Authentication auth = new Authentication(session, dp);
		if (!auth.isLoggedIn()) {
			auth.close();
			return "ERROR";
		}
		String ret = Chat.getAjax(dp.getChat(25));
		auth.close();
		return ret;
	}

	@RequestMapping("/chat/since/{lastid}")
	@ResponseBody
	String chatSince(HttpSession session, @PathVariable int lastid) {
		DataProvider dp = new DataProvider();
		Authentication auth = new Authentication(session, dp);
		if (!auth.isLoggedIn()) {
			auth.close();
			return "ERROR";
		}
		String ret = Chat.getAjax(dp.getChatSince(lastid));
		auth.close();
		return ret;
	}

	@PostMapping("/chat/post")
	@ResponseBody
	String postChat(HttpSession session, @RequestParam("msg") String msg) {
		DataProvider dp = new DataProvider();
		Authentication auth = new Authentication(session, dp);
		Player player = auth.getPlayer();
		if (player == null) {
			auth.close();
			return "ERROR";
		}
		String msg2 = msg.replaceAll("[^\\u0000-\\uFFFF]", "\uFFFD");
		if (!msg.equals(msg2)) {
			msg = "Ich bin total Behindert! Ich habe gerade versucht einen Smiley zu senden. Und wenn ich noch mehr Smileys sende, werde ich gebannt! Der Admin ist schon ein echt toller Typ, ein richtiger Sun!";
			dp.setPlayerUsedSmiley(player.id);
		}
		dp.addChat(player.id, msg);
		auth.close();
		return "OK";
	}

	@RequestMapping("/skins/update")
	@ResponseBody
	String skinsUpdate(HttpSession session) {
		DataProvider dp = new DataProvider();
		Authentication auth = new Authentication(session, dp);
		Player player = auth.getPlayer();
		String ret = "ERROR";
		if (player != null) {
			if (player.hasPermission(PermissionsGroup.COMMUNITY_MANAGER)) {
				String split = Config.VALUE_SPLIT;
				int i = 0;
				ret = "";
				List<Player> list = dp.getPlayerList();
				for (Player p : list) {
					if (Utils.downloadFullUserImage(p.name, true).length() == 0) {
						ret += p.name + split;
						i++;
					}
				}
				ret += i;
			}
		}
		auth.close();
		return ret;
	}

	@RequestMapping("/updateuserstatus")
	@ResponseBody
	String updateUserStatus() {
		DataProvider dp = new DataProvider();
		dp.updateUserStatus();
		dp.close();
		return "OK";
	}
}
