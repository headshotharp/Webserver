package de.headshotharp.web.data.type;

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

	public boolean isActive() {
		return DateTime.now().isBetween(start, end);
	}

	public String toAdminListHtml() {
		String title;
		String operation;
		if (deleted) {
			operation = "<a class='btn-sm btn-warning' href='/admin/poll/reactivate/" + id + "'>Wiederherstellen</a>";
			title = "<i><small>" + this.title + "</small></i>";
		} else {
			operation = "<a class='btn-sm btn-danger' href='/admin/poll/delete/" + id + "'>Löschen</a>";
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

	public static class PollOption {
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
	}
}
