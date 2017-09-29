package de.headshotharp.web.data.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.type.ItemShopItemPrice;

@Component
public class ItemShopItemPriceExtractor implements ResultSetExtractor<ItemShopItemPrice> {
	@Override
	public ItemShopItemPrice extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (rs.next()) {
			return new ItemShopItemPrice(rs);
		}
		return null;
	}
}
