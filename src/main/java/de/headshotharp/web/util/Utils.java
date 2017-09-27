package de.headshotharp.web.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.data.DataProvider;
import de.headshotharp.web.data.type.Player;

public class Utils {
	public static String cleanHtml(String html, Whitelist whitelist) {
		return Jsoup.clean(html, whitelist).replace("\n", "");
	}

	public static String escapeHtml(String in) {
		return StringEscapeUtils.escapeHtml(in);
	}

	public static String cleanNewsMsg(String msg) {
		return cleanHtml(CommonUtils.nl2br(msg), Config.WHITELIST_BLOG_DEFAULT);
	}

	public static String germanizeUptimeString(String in) {
		return in.replace("days", "Tagen").replace("day", "Tag");
	}

	/**
	 * copies permissions from permissions.yml file into DB <br />
	 * <br />
	 * returns log as HTML for output
	 * 
	 * @param dp
	 * @param file
	 * @return
	 */
	public static String copyPermissionsIntoDatabase(DataProvider dp, String file) {
		StringBuilder str = new StringBuilder();
		str.append("<ul>");
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			String username = "";
			String group = "";
			boolean ok = false;
			while ((line = br.readLine()) != null) {
				if (line.equals("users:")) {
					ok = true;
					continue;
				} else if (line.equals("groups:")) {
					if (ok)
						break;
				}
				if (ok) {
					if (line.equals("    group:"))
						continue;
					if (line.startsWith("    -")) {
						group = line.substring(6, line.length());
						PermissionsGroup pg = PermissionsGroup.byBukkitName(group);
						Player player = dp.getPlayerByName(username);
						if (player != null) {
							dp.setPlayerPermissionsGroup(player.id, pg);
							str.append("<li>Set '" + player.name + " (" + player.id + ") GROUP '" + pg.toString()
									+ "'</li>");
						} else {
							str.append("<li>Player '" + username + "' not found</li>");
						}
					} else {
						username = line.substring(2, line.length() - 1);
					}
				}
			}
		} catch (Exception e) {
			str.append("ERROR: " + e.getMessage());
		}
		str.append("</ul>");
		return str.toString();
	}

	/**
	 * downloads user image (head and body) for username.<br />
	 * deletes files first if 'clean' <br />
	 * <br />
	 * return empty string on error, base64 body image for html on success
	 * 
	 * @param username
	 * @param clean
	 * @return
	 */
	public static String downloadFullUserImage(String username, boolean clean) {
		boolean error = false;
		String imageurl = "https://www.minecraftskinstealer.com/skin.php?s=700&u=" + username;
		String path = Config.PATH_SKINS;
		String basePath = path + "/" + username + "/base.png";
		String bodyPath = path + "/" + username + "/body.png";
		String headPath = path + "/" + username + "/head.png";
		if (clean) {
			new File(basePath).delete();
			new File(bodyPath).delete();
			new File(headPath).delete();
		}
		new File(path + "/" + username).mkdirs();
		BufferedImage baseImg = null;
		BufferedImage headImg = null;
		BufferedImage bodyImg = null;
		if (!new File(basePath).exists()) {
			baseImg = saveImage(imageurl);
			try {
				ImageIO.write(baseImg, "png", new File(basePath));
			} catch (IOException e) {
				e.printStackTrace();
				error = true;
			}
		}
		if (!new File(headPath).exists()) {
			try {
				if (baseImg == null)
					baseImg = ImageIO.read(new File(basePath));
				headImg = baseImg.getSubimage(90, 50, 80, 80);
				ImageIO.write(headImg, "png", new File(headPath));
			} catch (IOException e) {
				e.printStackTrace();
				error = true;
			}
		}
		if (!new File(bodyPath).exists()) {
			try {
				if (baseImg == null)
					baseImg = ImageIO.read(new File(basePath));
				bodyImg = baseImg.getSubimage(50, 50, 160, 320);
				ImageIO.write(bodyImg, "png", new File(bodyPath));
			} catch (IOException e) {
				e.printStackTrace();
				error = true;
			}
		}
		if (error)
			return "";
		if (bodyImg == null)
			return "";
		return img2Base64(bodyImg);
	}

	public static BufferedImage saveImage(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			BufferedImage image = ImageIO.read(connection.getInputStream());
			return image;
		} catch (Exception e) {
			return null;
		}
	}

	public static String img2Base64(BufferedImage img) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", Base64.getEncoder().wrap(os));
			String base64 = os.toString(StandardCharsets.UTF_8.name());
			os.close();
			return "data:image/png;base64," + base64;
		} catch (final Exception oe) {
			return null;
		}
	}
}
