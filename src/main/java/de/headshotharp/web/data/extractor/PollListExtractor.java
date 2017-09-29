package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.Poll;

@Component
public class PollListExtractor implements ResultSetExtractor<List<Poll>> {

	@Override
	public List<Poll> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<Poll> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new Poll(rs));
		}
		return list;
	}

}
