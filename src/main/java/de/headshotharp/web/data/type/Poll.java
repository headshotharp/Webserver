package de.headshotharp.web.data.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import de.headshotharp.web.util.DateTime;
import de.headshotharp.web.util.DateTime.DateTimeFormat;

public class Poll {
	public int id;
	public String username;
	public String title;
	public String description;
	public DateTime start;
	public DateTime end;
	boolean deleted;
	public List<PollOption> options = null;

	public Poll(int id, String username, String title, String description, DateTime start, DateTime end,
			boolean deleted) {
		this.id = id;
		this.username = username;
		this.title = title;
		this.description = description;
		this.start = start;
		this.end = end;
		this.deleted = deleted;
	}

	public Poll(ResultSet rs) throws SQLException {
		this(rs.getInt("id"), rs.getString("name"), rs.getString("title"), rs.getString("description"),
				DateTime.parse(rs.getString("start")), DateTime.parse(rs.getString("end")),
				(rs.getInt("deleted") != 0));
	}

	public boolean isActive() {
		return DateTime.now().isBetween(start, end);
	}

	public String toAdminListHtml() {
		String title;
		String operation="";
		operation += "<a class='btn-sm btn-default' href='/poll/" + id + "'>Ansehen</a>";
		if (deleted) {
			operation += "<a class='btn-sm btn-warning' href='/admin/poll/reactivate/" + id + "'>Wiederherstellen</a>";
			title = "<i><small>" + this.title + "</small></i>";
		} else {
			operation += "<a class='btn-sm btn-danger' href='/admin/poll/delete/" + id + "'>Löschen</a>";
			title = this.title;
		}
		return "<tr><td>" + id + "</td><td>" + title + "</td><td>"
				+ end.format(DateTimeFormat.FORMAT_HUMAN_READABLE_DATE.getSimpleDateFormat()) + "</td><td>" + operation
				+ "</td></tr>";
	}

	public static String toAdminListHtml(List<Poll> list) {
		StringBuilder str = new StringBuilder();
		str.append(
				"<table class='table-sm table-bordered'><tr><th>ID</th><th>Titel</th><th>Ende</th><th>Aktion</th></tr>");
		for (Poll p : list) {
			str.append(p.toAdminListHtml());
		}
		str.append("</table>");
		return str.toString();
	}
}
