package de.headshotharp.web.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime
{
	/**
	 * static usage of {@link java.text.SimpleDateFormat SimpleDateFormat} may
	 * cause problems<br />
	 * please use {@link de.headshotharp.web.util.DateTime.DateTimeFormat
	 * DateTimeFormat}
	 * 
	 * @see java.text.SimpleDateFormat SimpleDateFormat
	 * @see de.headshotharp.web.util.DateTime.DateTimeFormat DateTimeFormat
	 */
	@Deprecated
	public static final SimpleDateFormat FORMAT_SQL_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * static usage of {@link java.text.SimpleDateFormat SimpleDateFormat} may
	 * cause problems<br />
	 * please use {@link de.headshotharp.web.util.DateTime.DateTimeFormat
	 * DateTimeFormat}
	 * 
	 * @see java.text.SimpleDateFormat SimpleDateFormat
	 * @see de.headshotharp.web.util.DateTime.DateTimeFormat DateTimeFormat
	 */
	@Deprecated
	public static final SimpleDateFormat FORMAT_SQL_DATESTAMP = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * static usage of {@link java.text.SimpleDateFormat SimpleDateFormat} may
	 * cause problems<br />
	 * please use {@link de.headshotharp.web.util.DateTime.DateTimeFormat
	 * DateTimeFormat}
	 * 
	 * @see java.text.SimpleDateFormat SimpleDateFormat
	 * @see de.headshotharp.web.util.DateTime.DateTimeFormat DateTimeFormat
	 */
	@Deprecated
	public static final SimpleDateFormat FORMAT_HUMAN_READABLE = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	/**
	 * static usage of {@link java.text.SimpleDateFormat SimpleDateFormat} may
	 * cause problems<br />
	 * please use {@link de.headshotharp.web.util.DateTime.DateTimeFormat
	 * DateTimeFormat}
	 * 
	 * @see java.text.SimpleDateFormat SimpleDateFormat
	 * @see de.headshotharp.web.util.DateTime.DateTimeFormat DateTimeFormat
	 */
	@Deprecated
	public static final SimpleDateFormat FORMAT_HUMAN_READABLE_DATE = new SimpleDateFormat("dd.MM.yyyy");

	public static final String[] MONTH = new String[]
	{ "", "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };

	public int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;

	public DateTime()
	{

	}

	public DateTime(int year, int month, int day, int hour, int minute, int second)
	{
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	public DateTime(int year, int month, int day)
	{
		this.year = year;
		this.month = month;
		this.day = day;
	}

	/**
	 * returns a new date with added amount of days
	 * 
	 * @param amount
	 * @return
	 */
	public DateTime addDays(int amount)
	{
		DateTime dt = new DateTime();
		Calendar c = new GregorianCalendar();
		c.setTime(toDate());
		c.add(Calendar.DAY_OF_MONTH, amount);
		dt.year = c.get(Calendar.YEAR);
		dt.month = c.get(Calendar.MONTH) + 1;
		dt.day = c.get(Calendar.DAY_OF_MONTH);
		dt.hour = c.get(Calendar.HOUR_OF_DAY);
		dt.minute = c.get(Calendar.MINUTE);
		dt.second = c.get(Calendar.SECOND);
		return dt;
	}

	public DateTime nextMonth()
	{
		DateTime dt = copy();
		dt.month++;
		if (dt.month > 12)
		{
			dt.month = 1;
			dt.year++;
		}
		return dt;
	}

	public DateTime prevMonth()
	{
		DateTime dt = copy();
		dt.month--;
		if (dt.month < 1)
		{
			dt.month = 12;
			dt.year--;
		}
		return dt;
	}

	public boolean isBetween(DateTime start, DateTime end)
	{
		long me = toDate().getTime();
		if (start.toDate().getTime() <= me && me <= end.toDate().getTime()) return true;
		return false;
	}

	public int diff(DateTime date)
	{
		long diff = toDate().getTime() - date.toDate().getTime();
		return (int) (diff / (1000 * 60 * 60 * 24));
	}

	public boolean isSame(DateTime dt)
	{
		return (year == dt.year && month == dt.month && day == dt.day && hour == dt.hour && minute == dt.minute && second == dt.second);
	}

	public boolean isSameDay(DateTime dt)
	{
		return (year == dt.year && month == dt.month && day == dt.day);
	}

	public boolean isSameMonth(DateTime dt)
	{
		return (year == dt.year && month == dt.month);
	}

	public Date toDate()
	{
		Calendar c = new GregorianCalendar();
		c.set(year, month - 1, day, hour, minute, second);
		return c.getTime();
	}

	public String format(String format)
	{
		return new SimpleDateFormat(format).format(toDate());
	}

	public String format(SimpleDateFormat sdf)
	{
		return sdf.format(toDate());
	}

	public DateTime copy()
	{
		DateTime dt = new DateTime();
		dt.year = year;
		dt.month = month;
		dt.day = day;
		dt.hour = hour;
		dt.minute = minute;
		dt.second = second;
		return dt;
	}

	public static DateTime now()
	{
		return byDate(new Date());
	}

	public static DateTime byDate(Date d)
	{
		DateTime dt = new DateTime();
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		dt.year = c.get(Calendar.YEAR);
		dt.month = c.get(Calendar.MONTH) + 1;
		dt.day = c.get(Calendar.DAY_OF_MONTH);
		dt.hour = c.get(Calendar.HOUR_OF_DAY);
		dt.minute = c.get(Calendar.MINUTE);
		dt.second = c.get(Calendar.SECOND);
		return dt;
	}

	public static DateTime parse(String sqlTimestamp)
	{
		if (sqlTimestamp == null) return new DateTime();
		if (sqlTimestamp.equals("")) return new DateTime();
		if (sqlTimestamp.equals("0000-00-00 00:00:00")) return new DateTime();
		try
		{
			Date d = DateTimeFormat.FORMAT_SQL_TIMESTAMP.getSimpleDateFormat().parse(sqlTimestamp);
			return byDate(d);
		}
		catch (ParseException e)
		{
			return null;
		}
	}

	public String toMonthAndYear()
	{
		return MONTH[month] + " " + year;
	}

	public static int getDaysInMonth(int year, int month)
	{
		return YearMonth.of(year, month).lengthOfMonth();
	}

	@Override
	public String toString()
	{
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(toDate());
	}

	/**
	 * trying bugfix: multiple simultanious connection may cause inconsistancy
	 * of SimpleDateFormat (suspicion: static usage) <br />
	 * this class provides static usage of newly created objects
	 */
	public static enum DateTimeFormat
	{
		FORMAT_SQL_TIMESTAMP("yyyy-MM-dd HH:mm:ss"), FORMAT_SQL_DATESTAMP("yyyy-MM-dd"), FORMAT_HUMAN_READABLE("dd.MM.yyyy HH:mm:ss"), FORMAT_HUMAN_READABLE_DATE("dd.MM.yyyy");

		private String pattern;

		private DateTimeFormat(String format)
		{
			this.pattern = format;
		}

		public String getPattern()
		{
			return pattern;
		}

		public SimpleDateFormat getSimpleDateFormat()
		{
			return new SimpleDateFormat(pattern);
		}
	}
}
