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

import de.headshotharp.web.data.extractor.PollExtractor;
import de.headshotharp.web.data.extractor.PollListExtractor;
import de.headshotharp.web.data.extractor.PollOptionListExtractor;
import de.headshotharp.web.data.type.Poll;
import de.headshotharp.web.data.type.PollOption;

@Component
public class PollDataProvider {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private PollListExtractor pollListExtractor;

	@Autowired
	private PollExtractor pollExtractor;

	@Autowired
	private PollOptionListExtractor pollOptionListExtractor;

	/**
	 * returns all polls (if deleted = true, it will show deleted polls as well)
	 * <br />
	 * Polls dont have their poll options yet
	 *
	 * @param deleted
	 * @return
	 */
	public List<Poll> getPollBasicList(boolean deleted) {
		String sql = "SELECT p.id, u.name, p.title, p.description, p.start, p.end, p.deleted FROM poll AS p JOIN users AS u ON u.id = p.userid"
				+ (deleted ? "" : " WHERE p.deleted = 0") + " ORDER BY id DESC";
		return jdbc.query(sql, pollListExtractor);
	}

	public List<Poll> getPollBasicActiveList() {
		String sql = "SELECT p.id, u.name, p.title, p.description, p.start, p.end, p.deleted FROM poll AS p JOIN users AS u ON u.id = p.userid WHERE p.deleted = 0 AND p.start <= NOW() AND p.end >= NOW() ORDER BY id DESC";
		return jdbc.query(sql, pollListExtractor);
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
		String sql = "INSERT INTO poll (userid, title, description, end) VALUES (?,?,?,?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, userid);
				stmt.setString(2, title);
				stmt.setString(3, desc);
				stmt.setString(4, end);
				return stmt;
			}
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	public void addPollOption(int pollid, String text) {
		String sql = "INSERT INTO polloptions (pollid, polloption) VALUES (?,?)";
		jdbc.update(sql, pollid, text);
	}

	public void setPollDeleted(int id, boolean deleted) {
		String sql = "UPDATE poll SET deleted = ? where id = ?";
		jdbc.update(sql, deleted ? 1 : 0);
	}

	public Poll getPollWithoutOptions(int pollid) {
		String sql = "SELECT p.id, u.name, p.title, p.description, p.start, p.end, p.deleted FROM poll AS p JOIN users AS u ON u.id = p.userid WHERE p.id = ?";
		return jdbc.query(sql, pollExtractor, pollid);
	}

	public List<PollOption> getPollOptionsFor(int pollid) {
		String sql = "SELECT id, polloption, 0 as count FROM polloptions WHERE pollid = ?";
		return jdbc.query(sql, pollOptionListExtractor, pollid);
	}

	public Poll getPollWithOptions(int pollid) {
		Poll poll = getPollWithoutOptions(pollid);
		if (poll != null) {
			poll.options = getPollOptionsFor(pollid);
		}
		return poll;
	}

	/**
	 * returns optionId if already voted, -1 otherwise
	 *
	 * @param pollId
	 * @param userId
	 * @return
	 */
	public Integer getPollResult(int pollId, int userId) {
		String sql = "SELECT optionid FROM pollresults WHERE pollid = ? AND userid = ?";
		return jdbc.queryForObject(sql, Integer.class, pollId, userId);
	}

	public List<PollOption> getPollResults(int pollid) {
		String sql = "SELECT optionid as id, polloption, count FROM (SELECT optionid, polloption, COUNT(optionid) AS count FROM pollresults AS pr JOIN polloptions AS po ON po.id = pr.optionid WHERE pr.pollid = ? GROUP BY pr.optionid) a ORDER BY count DESC";
		return jdbc.query(sql, pollOptionListExtractor, pollid);
	}

	public void addPollResult(int pollId, int userId, int pollOptionId) {
		String sql = "INSERT INTO pollresults (pollid, userid, optionid) VALUES (?,?,?)";
		jdbc.update(sql, pollId, userId, pollOptionId);
	}
}
