package de.headshotharp.web.data.type;

import java.util.List;

import de.headshotharp.web.Config;
import de.headshotharp.web.util.DateTime;
import de.headshotharp.web.util.DateTime.DateTimeFormat;

public class News
{
	public int id;
	public NewsType type;
	public DateTime timestamp;
	public String title, msg;
	public String poster;
	public boolean deleted;

	public News(int id, NewsType type, DateTime timestamp, String title, String msg, String poster, boolean deleted)
	{
		this.id = id;
		this.type = type;
		this.timestamp = timestamp;
		this.title = title;
		this.msg = msg;
		this.poster = poster;
		this.deleted = deleted;
	}

	@Override
	public String toString()
	{
		return toHtml(false);
	}

	public String toHtml(boolean right)
	{
		return "<div class=\"news " + type + (right ? " news-right" : "") + "\"><p class=\"timeplate\">" + timestamp.format(DateTimeFormat.FORMAT_HUMAN_READABLE_DATE.getSimpleDateFormat()) + "</p><h1 style=\"background-image: url('" + Player.getHeadUrl(poster) + "');\">" + title + "</h1><br />" + msg.replace(Config.VALUE_CURRENCY_TEXT, Config.VALUE_CURRENCY_HTML) + "</div>";
	}

	public static String toHtml(List<News> list)
	{
		StringBuilder str = new StringBuilder();
		boolean right = false;
		for (News n : list)
		{
			if (!n.deleted)
			{
				str.append(n.toHtml(right));
				right = !right;
			}
		}
		return str.toString();
	}

	public String toAdminListHtml()
	{
		String title;
		String operation;
		if (deleted)
		{
			operation = "<a class='btn-sm btn-warning' href='/admin/news/reactivate/" + id + "'>Wiederherstellen</a>";
			title = "<i><small>" + this.title + "</small></i>";
		}
		else
		{
			operation = "<a class='btn-sm btn-danger' href='/admin/news/delete/" + id + "'>Löschen</a>";
			title = this.title;
		}
		return "<tr><td>" + id + "</td><td>" + title + "</td><td><a class='btn-sm btn-success' href='/admin/news/change/" + id + "'>Bearbeiten</a> " + operation + "</td></tr>";
	}

	public static String toAdminListHtml(List<News> list)
	{
		StringBuilder str = new StringBuilder();
		str.append("<table class='table-sm table-bordered'><tr><th>ID</th><th>Titel</th><th>Aktion</th></tr>");
		for (News n : list)
		{
			str.append(n.toAdminListHtml());
		}
		str.append("</table>");
		return str.toString();
	}

	public static enum NewsType
	{
		DEFAULT(0, ""), WARNING(1, "news-warning"), SUCCESS(2, "news-fixed");

		private int val;
		private String html;

		NewsType(int val, String html)
		{
			this.val = val;
			this.html = html;
		}

		public static NewsType byValue(int value)
		{
			for (NewsType type : values())
			{
				if (type.val == value) return type;
			}
			return NewsType.DEFAULT;
		}

		public int getValue()
		{
			return val;
		}

		@Override
		public String toString()
		{
			return html;
		}
	}
}
