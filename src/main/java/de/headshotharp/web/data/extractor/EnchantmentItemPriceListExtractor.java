package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.EnchantmentItemPrice;

@Component
public class EnchantmentItemPriceListExtractor implements ResultSetExtractor<List<EnchantmentItemPrice>> {
	@Override
	public List<EnchantmentItemPrice> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<EnchantmentItemPrice> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new EnchantmentItemPrice(rs));
		}
		return list;
	}
}
