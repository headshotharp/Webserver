package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.PollOption;

@Component
public class PollOptionListExtractor implements ResultSetExtractor<List<PollOption>> {
	@Override
	public List<PollOption> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<PollOption> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new PollOption(rs));
		}
		return list;
	}
}
