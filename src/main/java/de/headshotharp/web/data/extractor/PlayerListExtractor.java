package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.Player;

@Component
public class PlayerListExtractor implements ResultSetExtractor<List<Player>> {
	@Override
	public List<Player> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<Player> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new Player(rs));
		}
		return list;
	}
}
