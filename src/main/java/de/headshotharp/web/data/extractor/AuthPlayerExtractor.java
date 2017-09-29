package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.auth.AuthPlayer;

@Component
public class AuthPlayerExtractor implements ResultSetExtractor<AuthPlayer> {
	@Override
	public AuthPlayer extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (rs.next()) {
			return new AuthPlayer(rs);
		}
		return null;
	}
}
