package de.headshotharp.web.data.type;

import java.util.List;

import de.headshotharp.web.Config;
import de.headshotharp.web.util.Utils;

public class Chat {
	public int id;
	public String username;
	public String timestamp;
	public String msg;
	public ChatOrigin origin;

	public Chat(int id, String username, String timestamp, String msg, ChatOrigin origin) {
		this.id = id;
		this.username = username;
		this.timestamp = timestamp;
		this.msg = msg;
		this.origin = origin;
	}

	public String getHtml() {
		return "<div class=\"chat-item\"><table class=\"chat-item-info\"><tr><td><img src=\""
				+ Player.getHeadUrl(username) + "\" /></td><td><div class=\"nameplate\"><p>" + username
				+ "</p></div></td><td><div class=\"timeplate\"><p>" + timestamp
				+ "</p></div></td></tr></table><table class=\"chat-item-text\"><tr><td><p>" + msg
				+ "</p></td></tr></table></div>";
	}

	public static String getHtml(List<Chat> list) {
		StringBuilder sb = new StringBuilder();
		for (Chat c : list) {
			sb.append(c.getHtml());
		}
		return sb.toString();
	}

	public String getAjaxEncode() {
		String split = Config.VALUE_SPLIT;
		return id + split + username + split + Utils.escapeHtml(msg) + split + Player.getHeadUrl(username) + split
				+ origin.getNumber() + split + timestamp + split;
	}

	public static String getAjax(List<Chat> list) {
		StringBuilder str = new StringBuilder();
		for (Chat chat : list) {
			str.append(chat.getAjaxEncode());
		}
		str.append("" + list.size());
		return str.toString();
	}
}
