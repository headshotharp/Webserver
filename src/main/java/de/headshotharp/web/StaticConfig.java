package de.headshotharp.web;

import java.awt.Color;
import java.util.Random;

import org.jsoup.safety.Whitelist;

import de.headshotharp.web.util.DateTime;

public class StaticConfig {
	// SESSION
	public static String SESSION_VAR_USERID = "userid";
	public static String SESSION_VAR_USERNAME = "username";
	public static String SESSION_VAR_PASSWORD = "password";
	public static final String COOKIE_NAME_TOKEN = "staytoken";
	public static final String COOKIE_NAME_USERID = "stayid";

	// VALUES
	public static String VALUE_CURRENCY_HTML = "&real;$";
	public static String VALUE_CURRENCY_TEXT = "R$";
	public static String VALUE_SPLIT = "" + (char) 31;
	public static int VALUE_POLL_OPTION_SIZE = 7;

	// TEXT
	public static String VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN = "<p>Du musst eingeloggt sein, um den Adminbereich zu sehen.</p>";
	public static String VALUE_TEXT_ADMINAREA_NOT_PERMITTED = "<p>Du darfst nicht auf den Adminbereich zugreifen.</p>";
	public static String VALUE_TEXT_ADMINAREA_NOT_PERMITTED_WRITE = "<p>Du darfst diesen Bereich nicht bearbeiten.</p>";

	// GIFT
	public static boolean isGiftReady(DateTime lastGift) {
		return !DateTime.now().isSameDay(lastGift);
	}

	public static int getGiftAmount() {
		return 500 * (new Random().nextInt(5) + 3);
	}

	// month block bonus
	public static int getMonthBonus(int blockmonth) {
		if (blockmonth < 100000) {
			return 0;
		} else if (blockmonth < 250000) {
			return 20;
		} else if (blockmonth < 500000) {
			return 40;
		} else if (blockmonth < 1000000) {
			return 60;
		}
		return 80;
	}

	// WEB
	public static int MAX_PLAYER_ONLINE_LIST = 8;

	// WHITELISTS
	public static Whitelist WHITELIST_BLOG_DEFAULT = new Whitelist()
			.addTags("br", "img", "table", "tr", "th", "td", "a", "i", "b", "ul", "li", "code")
			.addAttributes("img", "src", "width").addAttributes("a", "href", "class");

	// COLORS
	public static Color[] COLORS_BASE_DEFAULT = new Color[] { /* LIGHT */new Color(244, 170, 66)/* orange */,
			new Color(66, 134, 244)/* blue */, new Color(244, 220, 66)/* yellow */, new Color(244, 89, 66)/* red */,
			new Color(51, 206, 64)/* green */, new Color(168, 117, 235)/* lila */, /* DARK */
			new Color(194, 123, 23)/* orange */, new Color(37, 97, 194)/* blue */, new Color(204, 180, 24)/* yellow */,
			new Color(207, 45, 21)/* red */, new Color(17, 158, 29)/* green */, new Color(105, 42, 189)/* lila */ };
	public static Color COLOR_REST_GRAY = new Color(153, 153, 153);
}
