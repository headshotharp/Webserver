package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.ItemShopItemPrice;

@Component
public class ItemShopItemPriceListExtractor implements ResultSetExtractor<List<ItemShopItemPrice>> {
	@Override
	public List<ItemShopItemPrice> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<ItemShopItemPrice> list = new ArrayList<>();
		while (rs.next()) {
			list.add(new ItemShopItemPrice(rs));
		}
		return list;
	}
}
