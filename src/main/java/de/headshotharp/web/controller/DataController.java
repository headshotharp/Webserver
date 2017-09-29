package de.headshotharp.web.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.headshotharp.web.Config;
import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.data.ChatDataProvider;
import de.headshotharp.web.data.GeneralDataProvider;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Chat;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.ServerStatus;
import de.headshotharp.web.util.Utils;

@RestController
@RequestMapping("/data")
public class DataController {
	@Autowired
	private UserDataProvider userDataProvider;

	@Autowired
	private Config config;

	@Autowired
	private GeneralDataProvider generalDataProvider;

	@Autowired
	private ChatDataProvider chatDataProvider;

	@RequestMapping("/me/{request}")
	String meRaw(HttpSession session, @PathVariable String request) {
		String data = "ERROR";
		Authentication auth = new Authentication(session, userDataProvider);
		Player player = auth.getPlayer();
		if (player != null) {
			if (request.equals("blockbreakplacetoday")) {
				int block_break = player.getBlockBreakToday(userDataProvider);
				int block_place = player.getBlockPlaceToday(userDataProvider);
				data = block_break + StaticConfig.VALUE_SPLIT + block_place;
			} else {
				data = player.get(request);
			}
		}
		return data;
	}

	@RequestMapping("/server/status")
	String serverStatus() {
		ServerStatus serverStatus = generalDataProvider.getServerStatus();
		return serverStatus.getAjaxEncode();
	}

	@RequestMapping("/server/blockbreakplacemonth")
	String serverBlockBreakPlaceMonth() {
		List<Player> list = userDataProvider.getPlayerListActive();
		int block_break = 0;
		int block_place = 0;
		for (Player p : list) {
			block_break += p.getBlockBreakThisMonth(userDataProvider);
			block_place += p.getBlockPlaceThisMonth(userDataProvider);
		}
		return block_break + StaticConfig.VALUE_SPLIT + block_place;
	}

	@RequestMapping("/server/blockbreakplacemonth/update")
	String serverBlockBreakPlaceMonthUpdate() {
		List<Player> list = userDataProvider.getPlayerListActive();
		int block_break = 0;
		int block_place = 0;
		for (Player p : list) {
			block_break += p.getBlockBreakThisMonth(userDataProvider);
			block_place += p.getBlockPlaceThisMonth(userDataProvider);
		}
		generalDataProvider.setBlockBreakMonthDb(block_break);
		generalDataProvider.setBlockPlaceMonthDb(block_place);
		return block_break + StaticConfig.VALUE_SPLIT + block_place;
	}

	@RequestMapping("/chat/init")
	String chatInit(HttpSession session) {
		Authentication auth = new Authentication(session, userDataProvider);
		if (!auth.isLoggedIn()) {
			return "ERROR";
		}
		String ret = Chat.getAjax(chatDataProvider.getChat(25));
		return ret;
	}

	@RequestMapping("/chat/since/{lastid}")
	String chatSince(HttpSession session, @PathVariable int lastid) {
		Authentication auth = new Authentication(session, userDataProvider);
		if (!auth.isLoggedIn()) {
			return "ERROR";
		}
		String ret = Chat.getAjax(chatDataProvider.getChatSince(lastid));
		return ret;
	}

	@PostMapping("/chat/post")
	String postChat(HttpSession session, @RequestParam("msg") String msg) {
		Authentication auth = new Authentication(session, userDataProvider);
		Player player = auth.getPlayer();
		if (player == null) {
			return "ERROR";
		}
		String msg2 = msg.replaceAll("[^\\u0000-\\uFFFF]", "\uFFFD");
		if (!msg.equals(msg2)) {
			msg = "Ich bin total Behindert! Ich habe gerade versucht einen Smiley zu senden. Und wenn ich noch mehr Smileys sende, werde ich gebannt! Der Admin ist schon ein echt toller Typ, ein richtiger Sun!";
			userDataProvider.setPlayerUsedSmiley(player.id);
		}
		chatDataProvider.addChat(player.id, msg);
		return "OK";
	}

	@RequestMapping("/skins/update")
	String skinsUpdate(HttpSession session) {
		Authentication auth = new Authentication(session, userDataProvider);
		Player player = auth.getPlayer();
		String ret = "ERROR";
		if (player != null) {
			if (player.hasPermission(PermissionsGroup.COMMUNITY_MANAGER)) {
				String split = StaticConfig.VALUE_SPLIT;
				int i = 0;
				ret = "";
				List<Player> list = userDataProvider.getPlayerList();
				for (Player p : list) {
					if (Utils.downloadFullUserImage(p.name, true, config.getPath().getSkins()).length() == 0) {
						ret += p.name + split;
						i++;
					}
				}
				ret += i;
			}
		}
		return ret;
	}

	@RequestMapping("/updateuserstatus")
	String updateUserStatus() {
		userDataProvider.updateUserStatus();
		return "OK";
	}
}
