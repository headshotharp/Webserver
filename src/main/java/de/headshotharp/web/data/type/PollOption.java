package de.headshotharp.web.data.type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PollOption {
	public int id = 0;
	public String polloption;
	public int resultAmount = 0;

	public PollOption(int id, String polloption) {
		this.id = id;
		this.polloption = polloption;
	}

	public PollOption(int id, String polloption, int resultAmount) {
		this.id = id;
		this.polloption = polloption;
		this.resultAmount = resultAmount;
	}

	public PollOption(ResultSet rs) throws SQLException {
		this(rs.getInt("id"), rs.getString("polloption"));
	}
}
