package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.EnchantmentItem;

@Component
public class EnchantmentItemListExtractor implements ResultSetExtractor<List<EnchantmentItem>> {
	@Override
	public List<EnchantmentItem> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<EnchantmentItem> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new EnchantmentItem(rs));
		}
		return list;
	}
}
