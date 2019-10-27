package de.headshotharp.web.util;

import java.awt.Graphics;
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

import org.apache.commons.text.StringEscapeUtils;
//import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import de.headshotharp.colors.ColorUtils;
import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.StaticConfig;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.data.UserDataProvider;
import de.headshotharp.web.data.type.Player;

public class Utils {
	public static String cleanHtml(String html, Whitelist whitelist) {
		return Jsoup.clean(html, whitelist).replace("\n", "");
	}

	public static String escapeHtml(String in) {
		return StringEscapeUtils.escapeHtml4(in);
	}

	public static String cleanNewsMsg(String msg) {
		return cleanHtml(CommonUtils.nl2br(msg), StaticConfig.WHITELIST_BLOG_DEFAULT);
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
	public static String copyPermissionsIntoDatabase(UserDataProvider userDataProvider, String file) {
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
					if (ok) {
						break;
					}
				}
				if (ok) {
					if (line.equals("    group:")) {
						continue;
					}
					if (line.startsWith("    -")) {
						group = line.substring(6, line.length());
						PermissionsGroup pg = PermissionsGroup.byBukkitName(group);
						Player player = userDataProvider.getPlayerByName(username);
						if (player != null) {
							userDataProvider.setPlayerPermissionsGroup(player.id, pg);
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
	public static String downloadFullUserImage(String username, boolean clean, String path,
			UserDataProvider userDataProvider, int userid) {
		try {
			// setup
			String imageurl = "https://minecraft.tools/download-skin/" + username;
			String bodyPath = path + File.separator + username + File.separator + "body.png";
			String headPath = path + File.separator + username + File.separator + "head.png";
			if (clean) {
				new File(bodyPath).delete();
				new File(headPath).delete();
			}
			new File(path + File.separator + username).mkdirs();
			if (new File(headPath).exists() && new File(bodyPath).exists()) {
				try {
					return img2Base64(ImageIO.read(new File(bodyPath)));
				} catch (IOException e) {
					return "";
				}
			}
			BufferedImage baseImg = saveImage(imageurl);
			BufferedImage fullBodyImg = null;
			BufferedImage headImg = baseImg.getSubimage(8, 8, 8, 8);
			if (!new File(headPath).exists()) {
				ImageIO.write(headImg, "png", new File(headPath));
				userDataProvider.setColor(userid, ColorUtils.toHtmlColor(ColorUtils.averageColorFromImage(headImg)));
			}
			if (!new File(bodyPath).exists()) {
				// get parts from downloaded image
				BufferedImage bodyImg = baseImg.getSubimage(20, 20, 8, 12);
				BufferedImage leftArmImg = baseImg.getSubimage(44, 20, 4, 12);
				BufferedImage leftLegImg = baseImg.getSubimage(4, 20, 4, 12);
				BufferedImage rightArmImg = null;
				BufferedImage rightLegImg = null;
				if (baseImg.getHeight() == 32) {
					rightArmImg = mirrorLeftRight(leftArmImg);
					rightLegImg = mirrorLeftRight(leftLegImg);
				} else {
					rightArmImg = baseImg.getSubimage(36, 52, 4, 12);
					rightLegImg = baseImg.getSubimage(20, 52, 4, 12);
				}
				// put body image together
				fullBodyImg = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = fullBodyImg.getGraphics();
				graphics.drawImage(headImg, 4, 0, null);
				graphics.drawImage(leftArmImg, 0, 8, null);
				graphics.drawImage(bodyImg, 4, 8, null);
				graphics.drawImage(rightArmImg, 12, 8, null);
				graphics.drawImage(leftLegImg, 4, 20, null);
				graphics.drawImage(rightLegImg, 8, 20, null);
				ImageIO.write(fullBodyImg, "png", new File(bodyPath));
			}
			if (fullBodyImg == null) {
				return "";
			}
			return img2Base64(fullBodyImg);
		} catch (Exception e) {
			System.out.println("Error while creating skin for " + username);
			e.printStackTrace();
		}
		return "";
	}

	public static BufferedImage mirrorUpDown(BufferedImage image) {
		BufferedImage mirror = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight() / 2; j++) {
				mirror.setRGB(i, j, image.getRGB(i, image.getHeight() - j - 1));
				mirror.setRGB(i, image.getHeight() - j - 1, image.getRGB(i, j));
			}
		}
		return mirror;
	}

	public static BufferedImage mirrorLeftRight(BufferedImage image) {
		BufferedImage mirror = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < image.getWidth() / 2; i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				mirror.setRGB(i, j, image.getRGB(image.getWidth() - i - 1, j));
				mirror.setRGB(image.getWidth() - i - 1, j, image.getRGB(i, j));
			}
		}
		return mirror;
	}

	public static BufferedImage saveImage(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			BufferedImage image = ImageIO.read(
					// connection.getInputStream()
					url);// NOSONAR
			return image;
		} catch (Exception e) {
			e.printStackTrace();
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
