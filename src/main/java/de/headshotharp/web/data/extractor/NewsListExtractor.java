package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.News;

@Component
public class NewsListExtractor implements ResultSetExtractor<List<News>> {
	@Override
	public List<News> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<News> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new News(rs));
		}
		return list;
	}
}
