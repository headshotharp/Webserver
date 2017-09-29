package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.Chat;

@Component
public class ChatListExtractor implements ResultSetExtractor<List<Chat>> {
	@Override
	public List<Chat> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<Chat> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new Chat(rs));
		}
		return list;
	}
}
