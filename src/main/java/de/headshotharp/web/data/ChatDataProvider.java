package de.headshotharp.web.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.extractor.ChatListExtractor;
import de.headshotharp.web.data.type.Chat;
import de.headshotharp.web.data.type.ChatOrigin;

@Component
public class ChatDataProvider {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private ChatListExtractor chatListExtractor;

	/**
	 * inserts a msg into chat
	 *
	 * @param userid
	 * @param msg
	 */
	public void addChat(int userid, String msg) {
		String sql = "INSERT INTO chat (userid, msg, origin) VALUES (?,?,?)";
		jdbc.update(sql, userid, msg, ChatOrigin.WEB.getNumber());
	}

	/**
	 * returns the last chat entries limit to param
	 *
	 * @param limit
	 * @return
	 */
	public List<Chat> getChat(int limit) {
		String sql = "SELECT * FROM (SELECT c.id, u.name, c.msg, c.timestamp, c.origin FROM chat AS c JOIN users AS u ON u.id = c.userid order by id desc LIMIT "
				+ limit + ") AS i ORDER BY id";
		return jdbc.query(sql, chatListExtractor);
	}

	/**
	 * returns all chat entries since param lastId
	 *
	 * @param lastId
	 * @return
	 */
	public List<Chat> getChatSince(int lastId) {
		String sql = "SELECT c.id, u.name, c.msg, c.timestamp, c.origin FROM chat AS c JOIN users AS u ON u.id = c.userid where c.id > ? order by c.id";
		return jdbc.query(sql, chatListExtractor, lastId);
	}
}
