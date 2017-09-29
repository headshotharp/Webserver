package de.headshotharp.web.auth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;

public class Authentication {
	private static SecureRandom random = new SecureRandom();
	private HttpSession session;
	private UserDataProvider dp;
	private String userid = "";
	private boolean loginChecked = false;
	private boolean loggedin = false;
	private Player player = null;

	public Authentication(HttpSession session, UserDataProvider dp) {
		this.session = session;
		this.dp = dp;
	}

	public Player getPlayer() {
		isLoggedIn();
		return player;
	}

	public boolean isLoggedIn() {
		if (loginChecked) {
			return loggedin;
		}
		String userid = (String) session.getAttribute(StaticConfig.SESSION_VAR_USERID);
		if (userid == null) {
			return false;
		}
		String username = (String) session.getAttribute(StaticConfig.SESSION_VAR_USERNAME);
		if (username == null) {
			return false;
		}
		String password = (String) session.getAttribute(StaticConfig.SESSION_VAR_PASSWORD);
		if (password == null) {
			return false;
		}
		AuthenticationStatus status = dp.checkCredentials(userid, username, password);
		if (status == AuthenticationStatus.OK) {
			this.userid = userid;
			loginChecked = true;
			player = dp.getPlayerById(userid);
			loggedin = true;
			return true;
		} else {
			loginChecked = true;
			return false;
		}
	}

	public boolean login(String cookieuserid, String cookietoken) {
		if (cookieuserid.length() == 0) {
			return false;
		}
		if (cookietoken.length() == 0) {
			return false;
		}
		int c_userid = -1;
		try {
			c_userid = Integer.parseInt(cookieuserid);
		} catch (NumberFormatException e) {
			return false;
		}
		if (dp.checkToken(c_userid, cookietoken)) {
			AuthPlayer player = dp.getAuthPlayer(c_userid);
			loggedin = login(player.getUsername(), player.getPassword(), false, null);
			return loggedin;
		}
		return false;
	}

	/**
	 * <i>password must be already MD5 hashed</i>
	 *
	 * @param username
	 * @param password
	 * @param stayLoggedin
	 * @return
	 */
	public boolean login(String username, String password, boolean stayLoggedin, HttpServletResponse response) {
		if (username.length() == 0) {
			return false;
		}
		if (password.length() == 0) {
			return false;
		}
		player = dp.getPlayerByName(username);
		if (player == null) {
			return false;
		}
		String id = "" + player.id;
		session.setAttribute(StaticConfig.SESSION_VAR_USERID, id);
		session.setAttribute(StaticConfig.SESSION_VAR_USERNAME, username);
		session.setAttribute(StaticConfig.SESSION_VAR_PASSWORD, password);
		session.setMaxInactiveInterval(3600);
		AuthenticationStatus status = dp.checkCredentials(id, username, password);
		if (status == AuthenticationStatus.OK) {
			this.userid = id;
			loginChecked = true;
			if (stayLoggedin) {
				String token = dp.createToken(player.id);
				setCookieToken(response, userid, token);
			}
			try {
				int i_id = Integer.parseInt(userid);
				dp.setPlayerWebLoginNow(i_id);
			} catch (Exception e) {
				System.out.println("Could't parse LOGIN EVENT userid (lastweblogin not set)");
			}
			loggedin = true;
			return true;
		} else {
			player = null;
			loginChecked = true;
			loggedin = false;
			return false;
		}
	}

	public static void logout(UserDataProvider dp, HttpSession session, HttpServletResponse response) {
		String userid = (String) session.getAttribute(StaticConfig.SESSION_VAR_USERID);
		int i_userid = -1;
		try {
			i_userid = Integer.parseInt(userid);
		} catch (NumberFormatException e) {

		}
		session.invalidate();
		Cookie cookieToken = new Cookie(StaticConfig.COOKIE_NAME_TOKEN, "");
		cookieToken.setSecure(true);
		cookieToken.setMaxAge(-1);
		response.addCookie(cookieToken);
		Cookie cookieUserid = new Cookie(StaticConfig.COOKIE_NAME_USERID, "");
		cookieUserid.setSecure(true);
		cookieUserid.setMaxAge(-1);
		response.addCookie(cookieUserid);
		dp.deleteToken(i_userid);
	}

	public static void setCookieToken(HttpServletResponse response, String userid, String token) {
		Cookie cookieToken = new Cookie(StaticConfig.COOKIE_NAME_TOKEN, token);
		// cookieToken.setSecure(true);
		cookieToken.setMaxAge(604800);
		response.addCookie(cookieToken);
		Cookie cookieUserid = new Cookie(StaticConfig.COOKIE_NAME_USERID, userid);
		// cookieUserid.setSecure(true);
		cookieUserid.setMaxAge(604800);
		response.addCookie(cookieUserid);
	}

	public static String MD5(String input) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
		final BigInteger bigInt = new BigInteger(1, md.digest());
		return String.format("%032x", bigInt);
	}

	public static String nextSessionToken() {
		return new BigInteger(130, random).toString(32);
	}

	public RegistrationStatus register(String username, String passwd, String passwd2, String code) {
		Player p = dp.getPlayerByName(username);
		if (p == null) {
			return RegistrationStatus.NO_DATA;
		}
		RegistrationStatus status = dp.getPlayerRegistrationStatus(p.id);
		if (status == RegistrationStatus.ALREADY_REGISTERED) {
			return RegistrationStatus.ALREADY_REGISTERED;
		}
		if (status == RegistrationStatus.UNDEFINED) {
			return RegistrationStatus.DB_ERROR;
		}
		return setPassword(dp, p, passwd, passwd2, code);
	}

	/**
	 * password must be clear text
	 *
	 * @param username
	 * @param passwd
	 * @param passwd2
	 * @param code
	 * @return
	 */
	public RegistrationStatus forgotPasswd(String username, String passwd, String passwd2, String code) {
		Player p = dp.getPlayerByName(username);
		if (p == null) {
			return RegistrationStatus.NO_DATA;
		}
		return setPassword(dp, p, passwd, passwd2, code);
	}

	/**
	 * password must be clear text
	 *
	 * @param dp
	 * @param p
	 * @param passwd
	 * @param passwd2
	 * @param code
	 * @return
	 */
	public static RegistrationStatus setPassword(UserDataProvider dp, Player p, String passwd, String passwd2,
			String code) {
		String dbCode = dp.getPlayerSecurityCode(p.id);
		if (dbCode == null) {
			return RegistrationStatus.DB_ERROR;
		}
		if (!checkSecurityCode(dbCode, code)) {
			return RegistrationStatus.WRONGE_CODE;
		}
		if (!passwd.equals(passwd2)) {
			return RegistrationStatus.UNEQUAL_PASSWD;
		}
		if (passwd.length() < 8) {
			return RegistrationStatus.SHORT_PASSWORD;
		}
		dp.setPlayerPassword(p.id, MD5(passwd));
		dp.resetPlayerSecurityCode(p.id);
		return RegistrationStatus.SUCCESSFUL_REGISTERED;
	}

	/**
	 * password must be clear text <br />
	 * returns {@link de.headshotharp.web.auth.RegistrationStatus#WRONGE_CODE
	 * WRONGE_CODE} if oldpassword is wrong
	 *
	 * @param dp
	 * @param p
	 * @param passwd
	 * @param passwd2
	 * @param oldpw
	 * @return
	 */
	public RegistrationStatus changePasswordWithOldPassword(String passwd, String passwd2, String oldpw) {
		if (player == null) {
			return RegistrationStatus.NOT_LOGGED_IN;
		}
		if (!passwd.equals(passwd2)) {
			return RegistrationStatus.UNEQUAL_PASSWD;
		}
		if (passwd.length() < 8) {
			return RegistrationStatus.SHORT_PASSWORD;
		}
		if (!dp.getAuthPlayer(player.id).getPassword().equals(MD5(oldpw))) {
			return RegistrationStatus.WRONGE_CODE;
		}
		String md5pass = MD5(passwd);
		dp.setPlayerPassword(player.id, md5pass);
		dp.resetPlayerSecurityCode(player.id);
		session.setAttribute(StaticConfig.SESSION_VAR_PASSWORD, md5pass);
		return RegistrationStatus.SUCCESSFUL_REGISTERED;
	}

	public static boolean checkSecurityCode(String code1, String code2) {
		if (code1.equals(code2) && !code1.equals("NONE")) {
			return true;
		}
		return false;
	}
}
