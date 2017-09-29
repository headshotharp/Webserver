package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.Player;

@Component
public class PlayerExtractor implements ResultSetExtractor<Player> {
	@Override
	public Player extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (rs.next()) {
			return new Player(rs);
		}
		return null;
	}
}
