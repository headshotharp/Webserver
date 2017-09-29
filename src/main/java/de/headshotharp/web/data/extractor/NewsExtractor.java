package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.News;

@Component
public class NewsExtractor implements ResultSetExtractor<News> {
	@Override
	public News extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (rs.next()) {
			return new News(rs);
		}
		return null;
	}
}
