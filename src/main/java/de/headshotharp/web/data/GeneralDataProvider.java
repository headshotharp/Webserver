package de.headshotharp.web.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.ServerStatus;
import de.headshotharp.web.util.ServerOnlineChecker;
import de.headshotharp.web.util.Utils;

@Component
public class GeneralDataProvider {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private UserDataProvider userDataProvider;

	@Autowired
	private ServerOnlineChecker serverOnlineChecker;

	public int getServerTotalBlockBreak() {
		String sql = "SELECT sum(block_break) as block_break FROM users";
		return jdbc.queryForObject(sql, Integer.class);
	}

	public int getServerTotalBlockPlace() {
		String sql = "SELECT sum(block_place) as block_place FROM users";
		return jdbc.queryForObject(sql, Integer.class);
	}

	public int getServerTotalBlockBreakMonth() {
		List<Player> list = userDataProvider.getPlayerListActive();
		int block_break = 0;
		for (Player p : list) {
			block_break += p.getBlockBreakThisMonth(userDataProvider);
		}
		return block_break;
	}

	public int getServerTotalBlockPlaceMonth() {
		List<Player> list = userDataProvider.getPlayerListActive();
		int block_place = 0;
		for (Player p : list) {
			block_place += p.getBlockPlaceThisMonth(userDataProvider);
		}
		return block_place;
	}

	public int getBlockBreakMonthFromDb() {
		try {
			return Integer.parseInt(getServerStatusValue("blockbreakmonth"));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public int getBlockPlaceMonthFromDb() {
		try {
			return Integer.parseInt(getServerStatusValue("blockplacemonth"));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public void setBlockBreakMonthDb(int value) {
		setServerStatusValue("blockbreakmonth", "" + value);
		setServerStatusValue("blockbreakmonthbonus", "" + StaticConfig.getMonthBonus(value));
	}

	public void setBlockPlaceMonthDb(int value) {
		setServerStatusValue("blockplacemonth", "" + value);
		setServerStatusValue("blockplacemonthbonus", "" + StaticConfig.getMonthBonus(value));
	}

	/**
	 * returns a compact server status with server online status and list of online
	 * players
	 *
	 * @return
	 */
	public ServerStatus getServerStatus() {
		return new ServerStatus(serverOnlineChecker.isOnline(), userDataProvider.getOnlinePlayerList(),
				getServerUptime());
	}

	/**
	 * returns the string directly from unix command "uptime", but truncated to
	 * uptime with time interval (e.g. 44 days) <br />
	 * string is germanized by
	 * {@link de.headshotharp.web.util.Utils#germanizeUptimeString(String)
	 * germanizeUptimeString}
	 *
	 * @return
	 */
	public String getServerUptime() {
		return Utils.germanizeUptimeString(getServerStatusValue("uptime"));
	}

	/**
	 * returns value as String for table serverstatus where status = key
	 *
	 * @param key
	 * @return
	 */
	public String getServerStatusValue(String key) {
		String sql = "SELECT value FROM serverstatus WHERE status = ?";
		return jdbc.queryForObject(sql, String.class, key);
	}

	/**
	 * updates serverstatus table
	 *
	 * @param key
	 * @param value
	 */
	public void setServerStatusValue(String key, String value) {
		String sql = "UPDATE serverstatus SET value = ? WHERE status = ?";
		jdbc.update(sql, value, key);
	}
}
