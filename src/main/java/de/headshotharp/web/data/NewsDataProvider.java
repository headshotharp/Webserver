package de.headshotharp.web.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import de.headshotharp.web.data.extractor.NewsExtractor;
import de.headshotharp.web.data.extractor.NewsListExtractor;
import de.headshotharp.web.data.type.News;
import de.headshotharp.web.data.type.News.NewsType;

@Component
public class NewsDataProvider {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private NewsListExtractor newsListExtractor;

	@Autowired
	private NewsExtractor newsExtractor;

	/**
	 * returns a list of news ordered by date desc (latest = first)
	 *
	 * @return
	 */
	public List<News> getNews(boolean deleted) {
		String sql = "SELECT n.id, u.name, n.title, n.msg, n.created, n.type, n.deleted FROM news AS n JOIN users AS u ON u.id = n.userid "
				+ (deleted ? "" : "WHERE deleted = 0 ") + "ORDER BY n.created desc";
		return jdbc.query(sql, newsListExtractor);
	}

	/**
	 * returns the news with id
	 *
	 * @param id
	 * @return
	 */
	public News getNews(int id) {
		String sql = "select n.id, u.name, n.title, n.msg, n.created, n.type, n.deleted from news as n join users as u on u.id = n.userid where n.id = ?";
		return jdbc.query(sql, newsExtractor, id);
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
		String sql = "INSERT INTO news (userid, title, msg, type) VALUES (?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, userid);
				stmt.setString(2, title);
				stmt.setString(3, msg);
				stmt.setInt(4, type.getValue());
				return stmt;
			}
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	/**
	 * update news with id set title and msg
	 *
	 * @param id
	 * @param title
	 * @param msg
	 */
	public void updateNews(int id, String title, String msg, NewsType type) {
		String sql = "UPDATE news set title = ?, msg = ?, type = ? where id = ?";
		jdbc.update(sql, title, msg, type.getValue(), id);
	}

	/**
	 * set deleted flag for news
	 *
	 * @param id
	 */
	public void setNewsDeleted(int id, boolean deleted) {
		String sql = "UPDATE news SET deleted = " + (deleted ? "1" : "0") + " where id = ?";
		jdbc.update(sql, id);
	}
}
