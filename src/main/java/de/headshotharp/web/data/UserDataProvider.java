package de.headshotharp.web.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import de.headshotharp.web.auth.AuthPlayer;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.auth.AuthenticationStatus;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.auth.RegistrationStatus;
import de.headshotharp.web.data.extractor.AuthPlayerExtractor;
import de.headshotharp.web.data.extractor.PlayerExtractor;
import de.headshotharp.web.data.extractor.PlayerListExtractor;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.PlayerStatus;
import de.headshotharp.web.data.type.Timeline;
import de.headshotharp.web.data.type.TimelinePart;
import de.headshotharp.web.util.DateTime;

@Component
public class UserDataProvider {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private PlayerExtractor playerExtractor;

	@Autowired
	private PlayerListExtractor playerListExtractor;

	@Autowired
	private AuthPlayerExtractor authPlayerExtractor;

	/**
	 * sets the lastgift value to NOW() of player userid
	 *
	 * @param userid
	 */
	public void updateGiftNow(int userid) {
		String sql = "UPDATE users SET lastgift = NOW() WHERE id = ?";
		jdbc.update(sql, userid);
	}

	/**
	 * sets money for user userid
	 *
	 * @param userid
	 * @param amount
	 */
	public void setMoney(int userid, int amount) {
		String sql = "UPDATE users SET money = ? WHERE id = ?";
		jdbc.update(sql, amount, userid);
	}

	/**
	 * updates last web login to NOW
	 *
	 * @param userid
	 */
	public void setPlayerWebLoginNow(int userid) {
		String sql = "UPDATE users SET lastweblogin = NOW() WHERE id = ?";
		jdbc.update(sql, userid);
	}

	/**
	 * updates timestamp of used smiley only if not already set<br />
	 * keep first usage of smiley in db
	 *
	 * @param userid
	 */
	public void setPlayerUsedSmiley(int userid) {
		String sql = "UPDATE users SET usedsmiley = IF(usedsmiley = '0000-00-00 00:00:00', NOW(), usedsmiley) where id = ?";
		jdbc.update(sql, userid);
	}

	/**
	 * updates password for player<br />
	 * password must be a MD5 hash already
	 *
	 * @param userid
	 * @param password
	 * @return
	 */
	public void setPlayerPassword(int userid, String password) {
		String sql = "UPDATE users SET password = ? WHERE id = ?";
		jdbc.update(sql, password, userid);
	}

	public void setPlayerPermissionsGroup(int userid, PermissionsGroup group) {
		String sql = "UPDATE users SET permissionsgroup = ? where id = ?";
		jdbc.update(sql, group.getValue(), userid);
	}

	public void setPlayerRealname(int userid, String realname) {
		String sql = "UPDATE users SET realname = ? where id = ?";
		jdbc.update(sql, realname, userid);
	}

	public Integer getPlayerBlockBreakUntilYesterday(int userid) {
		String sql = "SELECT block_break FROM usertimeline WHERE userid = ? ORDER BY id DESC LIMIT 1";
		return jdbc.queryForObject(sql, Integer.class, userid);
	}

	public Integer getPlayerBlockPlaceUntilYesterday(int userid) {
		String sql = "SELECT block_place FROM usertimeline WHERE userid = ? ORDER BY id DESC LIMIT 1";
		return jdbc.queryForObject(sql, Integer.class, userid);
	}

	public Timeline getPlayerTimelineMonth(int userid, int year, int month) {
		String ts_clause = year + "-" + String.format("%02d", month) + "%";
		List<TimelinePart> parts = new ArrayList<>();
		SqlRowSet rs = null;
		SqlRowSet rs2 = null;
		String sql = "SELECT block_break, block_place, timestamp FROM usertimeline WHERE userid = ? AND timestamp LIKE ? ORDER BY timestamp";
		rs = jdbc.queryForRowSet(sql, userid, ts_clause);
		DateTime last = null;
		int lastBreak = 0;
		int lastPlace = 0;
		DateTime cur;
		int curBreak;
		int curPlace;
		int daysOfMonth = DateTime.getDaysInMonth(year, month);
		boolean hasData = false;
		while (rs.next()) {
			hasData = true;
			if (last == null) {
				last = DateTime.parse(rs.getString("timestamp"));
				lastBreak = rs.getInt("block_break");
				lastPlace = rs.getInt("block_place");
				if (last.day != 1) {
					int day = 1;
					while (day < last.day) {
						parts.add(new TimelinePart(0, 0, new DateTime(year, month, day)));
						day++;
					}
				}
			} else {
				cur = DateTime.parse(rs.getString("timestamp"));
				curBreak = rs.getInt("block_break");
				curPlace = rs.getInt("block_place");
				while (last.day + 1 < cur.day) {
					parts.add(new TimelinePart(0, 0, new DateTime(year, month, last.day)));
					last.day++;
				}
				parts.add(new TimelinePart(curBreak - lastBreak, curPlace - lastPlace, last));
				last = cur;
				lastBreak = curBreak;
				lastPlace = curPlace;
			}
		}
		if (hasData) {
			DateTime now = DateTime.now();
			if (year == now.year && month == now.month) {
				Player p = getPlayerById(userid);
				if (p != null) {
					curBreak = p.block_break;
					curPlace = p.block_place;
					parts.add(new TimelinePart(curBreak - lastBreak, curPlace - lastPlace, last));
				}
			} else {
				while (last.day < daysOfMonth) {
					parts.add(new TimelinePart(0, 0, new DateTime(year, month, last.day)));
					last.day++;
				}
				month++;
				if (month > 12) {
					year++;
					month = 1;
				}
				ts_clause = year + "-" + String.format("%02d", month) + "%";
				sql = "SELECT block_break, block_place FROM usertimeline WHERE userid = ? AND timestamp LIKE ? LIMIT 1";
				rs2 = jdbc.queryForRowSet(sql, userid, ts_clause);
				if (rs2.next()) {
					curBreak = rs2.getInt("block_break");
					curPlace = rs2.getInt("block_place");
					parts.add(new TimelinePart(curBreak - lastBreak, curPlace - lastPlace, last));
				}
			}
		}
		return new Timeline(parts);
	}

	public DateTime getPlayerTimelineStart(int userid) {
		String sql = "SELECT timestamp FROM usertimeline WHERE userid = ? LIMIT 1";
		return DateTime.parse(jdbc.queryForObject(sql, String.class, userid));
	}

	public Integer getPlayerBlockBreakUntilMonthstart(int userid) {
		String sql = "SELECT block_break FROM usertimeline WHERE userid = ? AND timestamp >= LAST_DAY(CURDATE()) + INTERVAL 1 DAY - INTERVAL 1 MONTH LIMIT 1";
		return jdbc.queryForObject(sql, Integer.class, userid);
	}

	public Integer getPlayerBlockPlaceUntilMonthstart(int userid) {
		String sql = "SELECT block_place FROM usertimeline WHERE userid = ? AND timestamp >= LAST_DAY(CURDATE()) + INTERVAL 1 DAY - INTERVAL 1 MONTH LIMIT 1";
		return jdbc.queryForObject(sql, Integer.class, userid);
	}

	public void resetPlayerSecurityCode(int userid) {
		String sql = "UPDATE users SET code = ? WHERE id = ?";
		jdbc.update(sql, "NONE", userid);
	}

	/**
	 * returns the security code of player with given ID, null if no player found
	 *
	 * @param id
	 * @return
	 */
	public String getPlayerSecurityCode(int id) {
		String sql = "SELECT code FROM users WHERE id = ?";
		return jdbc.queryForObject(sql, String.class, id);
	}

	/**
	 * returns registration status for player with given ID
	 * <table>
	 * <tr>
	 * <td>{@link de.headshotharp.web.auth.RegistrationStatus#UNDEFINED
	 * UNDEFINED}</td>
	 * <td>Player not found</td>
	 * </tr>
	 * <tr>
	 * <td>{@link de.headshotharp.web.auth.RegistrationStatus#NOT_REGISTERED
	 * NOT_REGISTERED}</td>
	 * <td>Player not registered</td>
	 * </tr>
	 * <tr>
	 * <td>{@link de.headshotharp.web.auth.RegistrationStatus#ALREADY_REGISTERED
	 * ALREADY_REGISTERED}</td>
	 * <td>Player is registered</td>
	 * </tr>
	 * </table>
	 *
	 * @param id
	 * @return
	 */
	public RegistrationStatus getPlayerRegistrationStatus(int id) {
		String sql = "SELECT password FROM users WHERE id = ?";
		String password = jdbc.queryForObject(sql, String.class, id);
		if (password == null) {
			return RegistrationStatus.UNDEFINED;
		}
		if (password.equals("NONE")) {
			return RegistrationStatus.NOT_REGISTERED;
		}
		return RegistrationStatus.ALREADY_REGISTERED;
	}

	/**
	 * returns Player by Username
	 *
	 * @param name
	 * @return player or null if not found
	 */
	public Player getPlayerByName(String name) {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE BINARY name = ?";
		return jdbc.query(sql, playerExtractor, name);
	}

	/**
	 * returns Player by id
	 *
	 * @param name
	 * @return player or null if not found
	 */
	public Player getPlayerById(String sid) {
		try {
			int id = Integer.parseInt(sid);
			return getPlayerById(id);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * returns Player by id
	 *
	 * @param name
	 * @return player or null if not found
	 */
	public Player getPlayerById(int id) {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE id = ?";
		return jdbc.query(sql, playerExtractor, id);
	}

	public List<Player> getPlayerListTopBlockBreak() {
		return getPlayerListTop("block_break");
	}

	public List<Player> getPlayerListTopBlockPlace() {
		return getPlayerListTop("block_place");
	}

	private List<Player> getPlayerListTop(String order) {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE status = 1 ORDER BY "
				+ order + " DESC";
		return jdbc.query(sql, playerListExtractor);
	}

	public List<Player> getPlayerListTopBlockBreakMonth() {
		return getPlayerListTopMonth("block_break");
	}

	public List<Player> getPlayerListTopBlockPlaceMonth() {
		return getPlayerListTopMonth("block_place");
	}

	private List<Player> getPlayerListTopMonth(String order) {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,(block_break - (select block_break from usertimeline where userid = users.id and timestamp >= LAST_DAY(CURDATE()) + INTERVAL 1 DAY - INTERVAL 1 MONTH limit 1)) as block_break,(block_place - (select block_place from usertimeline where userid = users.id and timestamp >= LAST_DAY(CURDATE()) + INTERVAL 1 DAY - INTERVAL 1 MONTH limit 1)) as block_place,online,permissionsgroup,status FROM users WHERE status = 1 ORDER BY "
				+ order + " DESC";
		return jdbc.query(sql, playerListExtractor);
	}

	/**
	 * returns a List of (ALL) Players same order as in DB
	 *
	 * @return
	 */
	public List<Player> getPlayerList() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users";
		return jdbc.query(sql, playerListExtractor);
	}

	/**
	 * returns a List of (ALL) Players same order as in DB
	 *
	 * @return
	 */
	public List<Player> getPlayerListOrderStatus() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users ORDER BY FIELD(status,1,2,0,3)";
		return jdbc.query(sql, playerListExtractor);
	}

	/**
	 * returns a List of Players <br />
	 * ordered by last login
	 *
	 * @return
	 */
	public List<Player> getPlayerListActive() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE status = 1 ORDER BY online DESC, lastlogin DESC";
		return jdbc.query(sql, playerListExtractor);
	}

	/**
	 * returns ALL players ordered by last login <br />
	 * active players first
	 *
	 * @return
	 */
	public List<Player> getPlayerListOrderedLastLogin() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users ORDER BY lastlogin DESC";
		return jdbc.query(sql, playerListExtractor);
	}

	/**
	 * returns a List of online Players ordered by login (first online = top of the
	 * list)
	 *
	 * @return
	 */
	public List<Player> getOnlinePlayerList() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE online != 0 ORDER BY lastlogin";
		return jdbc.query(sql, playerListExtractor);
	}

	public void updateUserStatus(int userid, PlayerStatus status) {
		String sql = "UPDATE users SET status = ? WHERE id = ?";
		jdbc.update(sql, status.getValue(), userid);
	}

	public void updateUserStatus() {
		String sql = "SELECT id, password, lastlogin, lastweblogin FROM users";
		SqlRowSet rs = jdbc.queryForRowSet(sql);
		int id;
		String password;
		PlayerStatus status;
		DateTime lastlogin;
		DateTime lastweblogin;
		DateTime now = DateTime.now();
		int days_ing;
		int days_web;
		int days;
		while (rs.next()) {
			id = rs.getInt("id");
			password = rs.getString("password");
			lastlogin = DateTime.parse(rs.getString("lastlogin"));
			lastweblogin = DateTime.parse(rs.getString("lastweblogin"));
			status = PlayerStatus.OK;
			if (password.equals("NONE")) {
				status = PlayerStatus.NO_ACCOUNT;
			} else if (lastlogin.isSame(new DateTime()) && lastweblogin.isSame(new DateTime())) {
				status = PlayerStatus.NO_LOGIN;
			} else {
				days_ing = Math.abs(now.diff(lastlogin));
				days_web = Math.abs(now.diff(lastweblogin));
				days = Math.min(days_ing, days_web);
				if (days > 90) {
					status = PlayerStatus.INACTIVE;
				} else {
					status = PlayerStatus.OK;
				}
			}
			updateUserStatus(id, status);
		}
	}

	public AuthenticationStatus checkCredentials(String userid, String username, String password) {
		AuthenticationStatus status = AuthenticationStatus.WRONG;
		int i_userid = -1;
		try {
			i_userid = Integer.parseInt(userid);
		} catch (NumberFormatException e) {
			return AuthenticationStatus.ERROR;
		}
		String sql = "SELECT name,password FROM users WHERE id = ?";
		SqlRowSet rs = jdbc.queryForRowSet(sql, i_userid);
		if (rs.next()) {
			if (username.equals(rs.getString("name")) && password.equals(rs.getString("password"))) {
				status = AuthenticationStatus.OK;
			}
		}
		return status;
	}

	public AuthPlayer getAuthPlayer(int userid) {
		String sql = "SELECT name, password FROM users WHERE id = ?";
		return jdbc.query(sql, authPlayerExtractor, userid);
	}

	public String createToken(int userid) {
		String token = Authentication.nextSessionToken();
		String sql = "INSERT INTO usertokens (userid, token) VALUES (?,?)";
		jdbc.update(sql, userid, token);
		return token;
	}

	public void deleteToken(int userid) {
		String sql = "DELETE FROM usertokens WHERE userid = ? AND UNIX_TIMESTAMP(created) > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) ORDER BY id DESC LIMIT 1";
		jdbc.update(sql, userid);
	}

	public boolean checkToken(int userid, String token) {
		String sql = "SELECT token FROM usertokens WHERE userid = ? AND token = ? AND UNIX_TIMESTAMP(created) > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) ORDER BY id DESC LIMIT 1";
		String ret = jdbc.queryForObject(sql, String.class, userid, token);
		return ret != null;
	}
}
