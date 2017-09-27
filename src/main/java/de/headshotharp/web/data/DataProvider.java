package de.headshotharp.web.data;

import java.io.Closeable;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mysql.jdbc.Statement;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.AuthPlayer;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.auth.AuthenticationStatus;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.auth.RegistrationStatus;
import de.headshotharp.web.data.type.Chat;
import de.headshotharp.web.data.type.ChatOrigin;
import de.headshotharp.web.data.type.EnchantmentItem;
import de.headshotharp.web.data.type.EnchantmentItemPrice;
import de.headshotharp.web.data.type.ItemShopItemPrice;
import de.headshotharp.web.data.type.News;
import de.headshotharp.web.data.type.News.NewsType;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.PlayerStatus;
import de.headshotharp.web.data.type.Poll;
import de.headshotharp.web.data.type.Poll.PollOption;
import de.headshotharp.web.data.type.ServerStatus;
import de.headshotharp.web.data.type.Timeline;
import de.headshotharp.web.data.type.TimelinePart;
import de.headshotharp.web.util.DateTime;
import de.headshotharp.web.util.Utils;

public class DataProvider implements Closeable {
	public static Connection getStaticConnection() {
		if (connection == null) {
			requestNewConnection();
		}
		try {
			if (connection.isClosed()) {
				requestNewConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	public static void requestNewConnection() {
		if (useStaticConnection)
			connection = MysqlConnector.getConnection();
	}

	private static boolean useStaticConnection = true;
	private static Connection connection = null;

	private Connection conn;

	/**
	 * Creates a DataProvider and opens a mysql connection <br />
	 * <b>Make sure to close connection via {@link #close() close()} after usage</b>
	 */
	public DataProvider() {
		if (useStaticConnection) {
			conn = getStaticConnection();
		} else {
			conn = MysqlConnector.getConnection();
		}
		try {
			conn.prepareStatement("set character set utf8").executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * sets money for user userid
	 * 
	 * @param userid
	 * @param amount
	 */
	public void setMoney(int userid, int amount) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET money = ? WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, amount);
			stmt.setInt(2, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * sets the lastgift value to NOW() of player userid
	 * 
	 * @param userid
	 */
	public void updateGiftNow(int userid) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET lastgift = NOW() WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * returns a list of news ordered by date desc (latest = first)
	 * 
	 * @return
	 */
	public List<News> getNews(boolean deleted) {
		List<News> list = new ArrayList<News>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT n.id, u.name, n.title, n.msg, n.created, n.type, n.deleted FROM news AS n JOIN users AS u ON u.id = n.userid "
					+ (deleted ? "" : "WHERE deleted = 0 ") + "ORDER BY n.created desc";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new News(rs.getInt("id"), NewsType.byValue(rs.getInt("type")),
						DateTime.parse(rs.getString("created")), rs.getString("title"), rs.getString("msg"),
						rs.getString("name"), (rs.getInt("deleted") == 0 ? false : true)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	/**
	 * returns the news with id
	 * 
	 * @param id
	 * @return
	 */
	public News getNews(int id) {
		News news = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "select n.id, u.name, n.title, n.msg, n.created, n.type, n.deleted from news as n join users as u on u.id = n.userid where n.id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				news = new News(rs.getInt("id"), NewsType.byValue(rs.getInt("type")),
						DateTime.parse(rs.getString("created")), rs.getString("title"), rs.getString("msg"),
						rs.getString("name"), (rs.getInt("deleted") == 0 ? false : true));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			news = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return news;
	}

	/**
	 * inserts a new blog entry into DB
	 * 
	 * @param userid
	 * @param title
	 * @param msg
	 * @return id of inserted news
	 */
	public int addNews(int userid, String title, String msg, NewsType type) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int id = -1;
		try {
			String sql = "INSERT INTO news (userid, title, msg, type) VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, userid);
			stmt.setString(2, title);
			stmt.setString(3, msg);
			stmt.setInt(4, type.getValue());
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
			id = -1;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return id;
	}

	/**
	 * update news with id set title and msg
	 * 
	 * @param id
	 * @param title
	 * @param msg
	 */
	public void updateNews(int id, String title, String msg, NewsType type) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE news set title = ?, msg = ?, type = ? where id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, title);
			stmt.setString(2, msg);
			stmt.setInt(3, type.getValue());
			stmt.setInt(4, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * set deleted flag for news
	 * 
	 * @param id
	 */
	public void setNewsDeleted(int id, boolean deleted) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE news SET deleted = " + (deleted ? "1" : "0") + " where id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * inserts a msg into chat
	 * 
	 * @param userid
	 * @param msg
	 */
	public void addChat(int userid, String msg) {
		PreparedStatement stmt = null;
		try {
			String sql = "INSERT INTO chat (userid, msg, origin) VALUES (?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.setString(2, msg);
			stmt.setInt(3, ChatOrigin.WEB.getNumber());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Smiley send?");
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * returns the last chat entries limit to param
	 * 
	 * @param limit
	 * @return
	 */
	public List<Chat> getChat(int limit) {
		List<Chat> list = new ArrayList<Chat>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM (SELECT c.id, u.name, c.msg, c.timestamp, c.origin FROM chat AS c JOIN users AS u ON u.id = c.userid order by id desc LIMIT "
					+ limit + ") AS i ORDER BY id";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new Chat(rs.getInt("id"), rs.getString("name"),
						DateTime.parse(rs.getString("timestamp")).toString(), rs.getString("msg"),
						ChatOrigin.byNumber(rs.getInt("origin"))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	/**
	 * returns all chat entries since param lastId
	 * 
	 * @param lastId
	 * @return
	 */
	public List<Chat> getChatSince(int lastId) {
		List<Chat> list = new ArrayList<Chat>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT c.id, u.name, c.msg, c.timestamp, c.origin FROM chat AS c JOIN users AS u ON u.id = c.userid where c.id > "
					+ lastId + " order by c.id";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new Chat(rs.getInt("id"), rs.getString("name"),
						DateTime.parse(rs.getString("timestamp")).toString(), rs.getString("msg"),
						ChatOrigin.byNumber(rs.getInt("origin"))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	/**
	 * returns all polls (if deleted = true, it will show deleted polls as well)
	 * <br />
	 * Polls dont have their poll options yet
	 * 
	 * @param deleted
	 * @return
	 */
	public List<Poll> getPollBasicList(boolean deleted) {
		List<Poll> list = new ArrayList<Poll>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT p.id, u.name, p.title, p.description, p.start, p.end, p.deleted FROM poll AS p JOIN users AS u ON u.id = p.userid"
					+ (deleted ? "" : " WHERE p.deleted = 0") + " ORDER BY id DESC";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new Poll(rs.getInt("id"), rs.getString("name"), rs.getString("title"),
						rs.getString("description"), DateTime.parse(rs.getString("start")),
						DateTime.parse(rs.getString("end")), (rs.getInt("deleted") != 0)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public List<Poll> getPollBasicActiveList() {
		List<Poll> list = new ArrayList<Poll>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT p.id, u.name, p.title, p.description, p.start, p.end, p.deleted FROM poll AS p JOIN users AS u ON u.id = p.userid WHERE p.deleted = 0 AND p.start <= NOW() AND p.end >= NOW() ORDER BY id DESC";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new Poll(rs.getInt("id"), rs.getString("name"), rs.getString("title"),
						rs.getString("description"), DateTime.parse(rs.getString("start")),
						DateTime.parse(rs.getString("end")), (rs.getInt("deleted") != 0)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	/**
	 * adds a poll to db and returns it generated id
	 * 
	 * @param userid
	 * @param title
	 * @param desc
	 * @param end
	 * @return
	 */
	public int addPoll(int userid, String title, String desc, String end) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int id = -1;
		try {
			String sql = "INSERT INTO poll (userid, title, description, end) VALUES (?,?,?,?)";
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, userid);
			stmt.setString(2, title);
			stmt.setString(3, desc);
			stmt.setString(4, end);
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
			id = -1;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return id;
	}

	public void addPollOption(int pollid, String text) {
		PreparedStatement stmt = null;
		try {
			String sql = "INSERT INTO polloptions (pollid, polloption) VALUES (?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pollid);
			stmt.setString(2, text);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public void setPollDeleted(int id, boolean deleted) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE poll SET deleted = " + (deleted ? "1" : "0") + " where id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public Poll getPollWithoutOptions(int pollid) {
		Poll poll = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT p.id, u.name, p.title, p.description, p.start, p.end, p.deleted FROM poll AS p JOIN users AS u ON u.id = p.userid WHERE p.id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pollid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				poll = new Poll(rs.getInt("id"), rs.getString("name"), rs.getString("title"),
						rs.getString("description"), DateTime.parse(rs.getString("start")),
						DateTime.parse(rs.getString("end")), (rs.getInt("deleted") != 0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			poll = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return poll;
	}

	public Poll getPollWithOptions(int pollid) {
		Poll poll = getPollWithoutOptions(pollid);
		if (poll != null) {
			poll.options = getPollOptionsFor(pollid);
		}
		return poll;
	}

	public List<PollOption> getPollOptionsFor(int pollid) {
		List<PollOption> list = new ArrayList<PollOption>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id, polloption FROM polloptions WHERE pollid = " + pollid;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new PollOption(rs.getInt("id"), rs.getString("polloption")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	/**
	 * returns optionId if already voted, -1 otherwise
	 * 
	 * @param pollId
	 * @param userId
	 * @return
	 */
	public int getPollResult(int pollId, int userId) {
		int result = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT optionid FROM pollresults WHERE pollid = ? AND userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pollId);
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt("optionid");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = -1;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return result;
	}

	public List<PollOption> getPollResults(int pollid) {
		List<PollOption> list = new ArrayList<PollOption>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT * FROM (SELECT optionid, polloption, COUNT(optionid) AS count FROM pollresults AS pr JOIN polloptions AS po ON po.id = pr.optionid WHERE pr.pollid = ? GROUP BY pr.optionid) a ORDER BY count DESC";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pollid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new PollOption(rs.getInt("optionid"), rs.getString("polloption"), rs.getInt("count")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public void addPollResult(int pollId, int userId, int pollOptionId) {
		PreparedStatement stmt = null;
		try {
			String sql = "INSERT INTO pollresults (pollid, userid, optionid) VALUES (?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, pollId);
			stmt.setInt(2, userId);
			stmt.setInt(3, pollOptionId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	private int getServerTotal(String s) {
		int value = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT sum(" + s + ") as " + s + " FROM users";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				value = rs.getInt(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return value;
	}

	public int getServerTotalBlockBreak() {
		return getServerTotal("block_break");
	}

	public int getServerTotalBlockPlace() {
		return getServerTotal("block_place");
	}

	public int getServerTotalBlockBreakMonth() {
		List<Player> list = getPlayerListActive();
		int block_break = 0;
		for (Player p : list) {
			block_break += p.getBlockBreakThisMonth(this);
		}
		return block_break;
	}

	public int getServerTotalBlockPlaceMonth() {
		List<Player> list = getPlayerListActive();
		int block_place = 0;
		for (Player p : list) {
			block_place += p.getBlockPlaceThisMonth(this);
		}
		return block_place;
	}

	public void setEnchantmentDiscount(int discount) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE serverstatus SET value = ? WHERE status = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "" + discount);
			stmt.setString(2, "enchantdiscount");
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * returns enchantment discount from table, but capped between 0 and 30 (in %)
	 * 
	 * @return
	 */
	public int getEnchantmentDiscount() {
		int discount = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT value from serverstatus WHERE status = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "enchantdiscount");
			rs = stmt.executeQuery();
			if (rs.next()) {
				discount = Integer.parseInt(rs.getString("value"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			discount = 0;
			requestNewConnection();
		} catch (NumberFormatException e2) {
			discount = 0;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		if (discount > 30)
			discount = 30;
		if (discount < 0)
			discount = 0;
		return discount;
	}

	public EnchantmentItemPrice getShopEnchantmentItem(int id) {
		int discount = getEnchantmentDiscount();
		EnchantmentItemPrice ench = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id, bukkitname, price, category FROM enchantments WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				ench = new EnchantmentItemPrice(rs.getInt("id"), rs.getString("bukkitname"), rs.getInt("price"),
						rs.getInt("category"), discount);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ench = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return ench;
	}

	public List<EnchantmentItem> getEnchantmentItemsForOrder(int id) {
		List<EnchantmentItem> list = new ArrayList<EnchantmentItem>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "select e.id, e.bukkitname, si.level from shopitemenchantments as si join enchantments as e on e.id = si.enchantmentid where si.shopitemid ="
					+ id;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new EnchantmentItem(rs.getInt("id"), rs.getString("bukkitname"), rs.getInt("level")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public ItemShopItemPrice getItemShopItem(int id) {
		int discount = getEnchantmentDiscount();
		ItemShopItemPrice item = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id, name, price, mc_item FROM shopitems WHERE id = " + id;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				item = new ItemShopItemPrice(rs.getInt("id"), rs.getString("name"), rs.getString("mc_item"),
						rs.getInt("price"), discount);
				item.setEnchantmentItems(getEnchantmentItemsForOrder(item.getId()));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			item = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return item;
	}

	public List<ItemShopItemPrice> getBoughtItemShopItems(int userid) {
		int discount = getEnchantmentDiscount();
		List<ItemShopItemPrice> list = new ArrayList<ItemShopItemPrice>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ItemShopItemPrice tmp;
		try {
			String sql = "SELECT si.id, si.name, si.price, si.mc_item FROM shopitemshop AS sis JOIN shopitems AS si ON si.id = sis.itemid WHERE used = 0 AND sis.userid = "
					+ userid;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				tmp = new ItemShopItemPrice(rs.getInt("id"), rs.getString("name"), rs.getString("mc_item"),
						rs.getInt("price"), discount);
				tmp.setEnchantmentItems(getEnchantmentItemsForOrder(tmp.getId()));
				list.add(tmp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public List<ItemShopItemPrice> getItemShopItems() {
		int discount = getEnchantmentDiscount();
		List<ItemShopItemPrice> list = new ArrayList<ItemShopItemPrice>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ItemShopItemPrice tmp;
		try {
			String sql = "SELECT id, name, price, mc_item FROM shopitems";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				tmp = new ItemShopItemPrice(rs.getInt("id"), rs.getString("name"), rs.getString("mc_item"),
						rs.getInt("price"), discount);
				tmp.setEnchantmentItems(getEnchantmentItemsForOrder(tmp.getId()));
				list.add(tmp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public void buyItemShopItem(int userid, int itemid) {
		PreparedStatement stmt = null;
		try {
			String sql = "INSERT INTO shopitemshop (userid, itemid) VALUES (?, ?);";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.setInt(2, itemid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public List<EnchantmentItemPrice> getBoughtShopEnchantmentItems(int userid) {
		int discount = getEnchantmentDiscount();
		List<EnchantmentItemPrice> list = new ArrayList<EnchantmentItemPrice>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT e.id, e.bukkitname, e.price, e.category FROM enchantmentshop AS es JOIN enchantments AS e ON e.id = es.itemid WHERE es.used = 0 AND es.userid = "
					+ userid;
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new EnchantmentItemPrice(rs.getInt("id"), rs.getString("bukkitname"), rs.getInt("price"),
						rs.getInt("category"), discount));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public List<EnchantmentItemPrice> getShopEnchantmentItems() {
		int discount = getEnchantmentDiscount();
		List<EnchantmentItemPrice> list = new ArrayList<EnchantmentItemPrice>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id, bukkitname, price, category FROM enchantments";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new EnchantmentItemPrice(rs.getInt("id"), rs.getString("bukkitname"), rs.getInt("price"),
						rs.getInt("category"), discount));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	public void buyEnchantment(int userid, int itemid) {
		PreparedStatement stmt = null;
		try {
			String sql = "INSERT INTO enchantmentshop (userid, itemid) VALUES (?, ?);";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.setInt(2, itemid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * updates last web login to NOW
	 * 
	 * @param userid
	 */
	public void setPlayerWebLoginNow(int userid) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET lastweblogin = NOW() WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * updates last web login to NOW
	 * 
	 * @param userid
	 */
	public void setPlayerUsedSmiley(int userid) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET usedsmiley = IF(usedsmiley = '0000-00-00 00:00:00', NOW(), usedsmiley) where id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * updates password for player<br />
	 * password must be a MD5 hash already<br />
	 * returns true on success
	 * 
	 * @param userid
	 * @param password
	 * @return
	 */
	public boolean setPlayerPassword(int userid, String password) {
		boolean status = false;
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET password = ? WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, password);
			stmt.setInt(2, userid);
			stmt.executeUpdate();
			status = true;
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return status;
	}

	public void setPlayerPermissionsGroup(int userid, PermissionsGroup group) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET permissionsgroup = ? where id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, group.getValue());
			stmt.setInt(2, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public void setPlayerRealname(int userid, String realname) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET realname = ? where id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, realname);
			stmt.setInt(2, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public int getPlayerBlockBreakUntilYesterday(int userid) {
		int block_break = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT block_break FROM usertimeline WHERE userid = ? ORDER BY id DESC LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				block_break = rs.getInt("block_break");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return block_break;
	}

	public int getPlayerBlockPlaceUntilYesterday(int userid) {
		int block_place = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT block_place FROM usertimeline WHERE userid = ? ORDER BY id DESC LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				block_place = rs.getInt("block_place");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return block_place;
	}

	public Timeline getPlayerTimelineMonth(int userid, int year, int month) {
		String ts_clause = year + "-" + String.format("%02d", month) + "%";
		List<TimelinePart> parts = new ArrayList<TimelinePart>();
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try {
			String sql = "SELECT block_break, block_place, timestamp FROM usertimeline WHERE userid = ? AND timestamp LIKE ? ORDER BY timestamp";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.setString(2, ts_clause);
			rs = stmt.executeQuery();
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
					stmt2 = conn.prepareStatement(sql);
					stmt2.setInt(1, userid);
					stmt2.setString(2, ts_clause);
					rs2 = stmt2.executeQuery();
					if (rs2.next()) {
						curBreak = rs2.getInt("block_break");
						curPlace = rs2.getInt("block_place");
						parts.add(new TimelinePart(curBreak - lastBreak, curPlace - lastPlace, last));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
			parts = null;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
			if (rs != null)
				try {
					rs2.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt2.close();
				} catch (Exception e) {
				}
		}
		if (parts != null)
			return new Timeline(parts);
		return null;
	}

	public DateTime getPlayerTimelineStart(int userid) {
		DateTime dt = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT timestamp FROM usertimeline WHERE userid = ? LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				dt = DateTime.parse(rs.getString("timestamp"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return dt;
	}

	public int getPlayerBlockBreakUntilMonthstart(int userid) {
		int block_break = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT block_break FROM usertimeline WHERE userid = ? AND timestamp >= LAST_DAY(CURDATE()) + INTERVAL 1 DAY - INTERVAL 1 MONTH LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				block_break = rs.getInt("block_break");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return block_break;
	}

	public int getPlayerBlockPlaceUntilMonthstart(int userid) {
		int block_place = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT block_place FROM usertimeline WHERE userid = ? AND timestamp >= LAST_DAY(CURDATE()) + INTERVAL 1 DAY - INTERVAL 1 MONTH LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				block_place = rs.getInt("block_place");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return block_place;
	}

	public void resetPlayerSecurityCode(int userid) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET code = ? WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "NONE");
			stmt.setInt(2, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * returns the security code of player with given ID, null if no player found
	 * 
	 * @param id
	 * @return
	 */
	public String getPlayerSecurityCode(int id) {
		String code = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT code FROM users WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				code = rs.getString("code");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			code = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return code;
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
		RegistrationStatus status = RegistrationStatus.UNDEFINED;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT password FROM users WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("password").equals("NONE"))
					status = RegistrationStatus.NOT_REGISTERED;
				else
					status = RegistrationStatus.ALREADY_REGISTERED;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			status = RegistrationStatus.UNDEFINED;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return status;
	}

	/**
	 * returns Player by Username
	 * 
	 * @param name
	 * @return player or null if not found
	 */
	public Player getPlayerByName(String name) {
		Player player = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE BINARY name = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			rs = stmt.executeQuery();
			if (rs.next()) {
				player = new Player(rs.getInt("id"), rs.getString("name"), rs.getString("realname"),
						DateTime.parse(rs.getString("creation_date")), DateTime.parse(rs.getString("lastgift")),
						rs.getInt("money"), rs.getInt("block_break"), rs.getInt("block_place"),
						(rs.getInt("online") == 1 ? true : false),
						PermissionsGroup.byValue(rs.getInt("permissionsgroup")),
						PlayerStatus.byValue(rs.getInt("status")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			player = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return player;
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
		Player player = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE id = '"
					+ id + "'";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.next()) {
				player = new Player(rs.getInt("id"), rs.getString("name"), rs.getString("realname"),
						DateTime.parse(rs.getString("creation_date")), DateTime.parse(rs.getString("lastgift")),
						rs.getInt("money"), rs.getInt("block_break"), rs.getInt("block_place"),
						(rs.getInt("online") == 1 ? true : false),
						PermissionsGroup.byValue(rs.getInt("permissionsgroup")),
						PlayerStatus.byValue(rs.getInt("status")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			player = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return player;
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
		return getPlayerListFromSQL(sql);
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
		return getPlayerListFromSQL(sql);
	}

	private List<Player> getPlayerListFromSQL(String sql) {
		List<Player> list = new ArrayList<Player>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				list.add(new Player(rs.getInt("id"), rs.getString("name"), rs.getString("realname"),
						DateTime.parse(rs.getString("creation_date")), DateTime.parse(rs.getString("lastgift")),
						rs.getInt("money"), rs.getInt("block_break"), rs.getInt("block_place"),
						(rs.getInt("online") == 1 ? true : false),
						PermissionsGroup.byValue(rs.getInt("permissionsgroup")),
						PlayerStatus.byValue(rs.getInt("status"))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			list = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return list;
	}

	/**
	 * returns a List of (ALL) Players same order as in DB
	 * 
	 * @return
	 */
	public List<Player> getPlayerList() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users";
		return getPlayerListFromSQL(sql);
	}

	/**
	 * returns a List of (ALL) Players same order as in DB
	 * 
	 * @return
	 */
	public List<Player> getPlayerListOrderStatus() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users ORDER BY FIELD(status,1,2,0,3)";
		return getPlayerListFromSQL(sql);
	}

	/**
	 * returns a List of Players <br />
	 * ordered by last login
	 * 
	 * @return
	 */
	public List<Player> getPlayerListActive() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE status = 1 ORDER BY online DESC, lastlogin DESC";
		return getPlayerListFromSQL(sql);
	}

	/**
	 * returns ALL players ordered by last login <br />
	 * active players first
	 * 
	 * @return
	 */
	public List<Player> getPlayerListOrderedLastLogin() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users ORDER BY lastlogin DESC";
		return getPlayerListFromSQL(sql);
	}

	/**
	 * returns a List of online Players ordered by login (first online = top of the
	 * list)
	 * 
	 * @return
	 */

	public List<Player> getOnlinePlayerList() {
		String sql = "SELECT id,name,realname,creation_date,lastgift,money,block_break,block_place,online,permissionsgroup,status FROM users WHERE online != 0 ORDER BY lastlogin";
		return getPlayerListFromSQL(sql);
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
		setServerStatusValue("blockbreakmonthbonus", "" + Config.getMonthBonus(value));
	}

	public void setBlockPlaceMonthDb(int value) {
		setServerStatusValue("blockplacemonth", "" + value);
		setServerStatusValue("blockplacemonthbonus", "" + Config.getMonthBonus(value));
	}

	public void updateUserStatus(int userid, PlayerStatus status) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE users SET status = ? WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, status.getValue());
			stmt.setInt(2, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public void updateUserStatus() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT id, password, lastlogin, lastweblogin FROM users";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
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
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	/**
	 * returns a compact server status with server online status and list of online
	 * players
	 * 
	 * @return
	 */
	public ServerStatus getServerStatus() {
		return new ServerStatus(getOnlinePlayerList(), getServerUptime());
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
		String value = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT value FROM serverstatus WHERE status = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, key);
			rs = stmt.executeQuery();
			if (rs.next()) {
				value = rs.getString("value");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return value;
	}

	/**
	 * updates serverstatus table
	 * 
	 * @param key
	 * @param value
	 */
	public void setServerStatusValue(String key, String value) {
		PreparedStatement stmt = null;
		try {
			String sql = "UPDATE serverstatus SET value = ? WHERE status = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, value);
			stmt.setString(2, key);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
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
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT name,password FROM users WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, i_userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				if (username.equals(rs.getString("name")) && password.equals(rs.getString("password")))
					status = AuthenticationStatus.OK;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
			status = AuthenticationStatus.ERROR;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return status;
	}

	public AuthPlayer getAuthPlayer(int userid) {
		AuthPlayer player = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT name, password FROM users WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				player = new AuthPlayer(rs.getString("name"), rs.getString("password"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			player = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return player;
	}

	public String createToken(int userid) {
		PreparedStatement stmt = null;
		String token = null;
		try {
			token = Authentication.nextSessionToken();
			String sql = "INSERT INTO usertokens (userid, token) VALUES (?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.setString(2, token);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			token = null;
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return token;
	}

	public void deleteToken(int userid) {
		PreparedStatement stmt = null;
		try {
			String sql = "DELETE FROM usertokens WHERE userid = ? AND UNIX_TIMESTAMP(created) > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) ORDER BY id DESC LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			requestNewConnection();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
	}

	public boolean checkToken(int userid, String token) {
		String ret = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT token FROM usertokens WHERE userid = ? AND token = ? AND UNIX_TIMESTAMP(created) > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) ORDER BY id DESC LIMIT 1";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userid);
			stmt.setString(2, token);
			rs = stmt.executeQuery();
			if (rs.next()) {
				ret = rs.getString("token");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ret = null;
			requestNewConnection();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
				}
		}
		return ret != null;
	}

	@Override
	public void close() {
		if (useStaticConnection)
			return;
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Properties getProperties() {
		try {
			Properties prop = new Properties();
			String propFileName = "application.properties";
			InputStream inputStream = DataProvider.class.getClassLoader().getResourceAsStream(propFileName);
			if (inputStream == null)
				return null;
			prop.load(inputStream);
			inputStream.close();
			return prop;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
