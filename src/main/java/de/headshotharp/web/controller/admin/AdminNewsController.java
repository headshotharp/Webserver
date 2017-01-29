package de.headshotharp.web.controller.admin;

import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.News;
import de.headshotharp.web.data.type.News.NewsType;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.util.Utils;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController extends DefaultController
{
	public static PermissionsGroup permissionsGroup = PermissionsGroup.DEVELOPMENT;

	@RequestMapping("")
	String news(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				String html = Bootstrap.button("Zurück", "/admin", ButtonType.DEFAULT) + "<br /><hr /><h3>Administration</h3>" + Bootstrap.button("Neuer Eintrag", "/admin/news/add", ButtonType.DEFAULT) + "<br /><br /><p><b>Eintrag bearbeiten:</b></p>" + News.toAdminListHtml(cd.getDataProvider().getNews(true));
				cd.getModel().addAttribute("content", html);
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@GetMapping("/add")
	String newsAdd(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				String html = "<h3>Neuer Eintrag:</h3><form accept-charset='UTF-8' method='post' action='/admin/news/add'><div class='form-group'><label>Titel:</label><input id='txt_title' type='text' class='form-control' name='title' value=''></div><div class='form-group'><label>Nachricht:</label><textarea id='txt_msg' class='form-control' name='msg' rows='15'></textarea></div>";
				html += "<div class='form-group'><label>Typ:</label><fieldset><input type='radio' name='type' value='0' checked='checked' /> <label>Standard</label><br /><input type='radio' name='type' value='2' /> <label>Positiv</label><br /><input type='radio' name='type' value='1' /> <label>Negativ</label><br /></fieldset></div>";
				html += "<input type='submit' class='btn btn-success' value='Hinzufügen' /> <a onclick='preview(); return true;' href='#preview' class='btn btn-default'>Vorschau</a> <a class='btn btn-default' href='/admin/news'>Abbrechen</a></form> <div id='preview'></div>";
				cd.getModel().addAttribute("content", html);
				cd.getModel().addAttribute("specialstyle", "news.css");
				cd.getModel().addAttribute("srcscripts", new String[]
				{ "/js/previewnews.js" });
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@PostMapping("/add")
	String newsAddPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("title") String title, @RequestParam("msg") String msg, @RequestParam("type") int type)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				int newsId = cd.getDataProvider().addNews(player.id, Utils.cleanHtml(title, Whitelist.none()), Utils.cleanNewsMsg(msg), NewsType.byValue(type));
				cd.getModel().addAttribute("content", "<p>Eintrag wurde erstellt.</p>" + Bootstrap.button("Zurück", "/admin/news", ButtonType.SUCCESS) + "<br class='clear' />" + cd.getDataProvider().getNews(newsId).toHtml(false));
				cd.getModel().addAttribute("specialstyle", "news.css");
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@RequestMapping("/change/{newsId}")
	String newsChange(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int newsId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				News news = cd.getDataProvider().getNews(newsId);
				int type = news.type.getValue();
				String html = "<h3>Eintrag bearbeiten:</h3><form accept-charset='UTF-8' method='post' action='/admin/news/change'><input type='hidden' name='newsId' value='" + news.id + "' /><div class='form-group'><label>Titel:</label><input type='text' class='form-control' name='title' id='txt_title' value='" + news.title + "'></div><div class='form-group'><label>Nachricht:</label><textarea class='form-control' id='txt_msg' name='msg' rows='15'>" + CommonUtils.br2nl(news.msg) + "</textarea></div>";
				html += "<div class='form-group'><label>Typ:</label><fieldset><input type='radio' name='type' value='0'" + (type == 0 ? " checked='checked'" : "") + " /> <label>Standard</label><br /><input type='radio' name='type' value='2'" + (type == 2 ? " checked='checked'" : "") + " /> <label>Positiv</label><br /><input type='radio' name='type' value='1'" + (type == 1 ? " checked='checked'" : "") + " /> <label>Negativ</label><br /></fieldset></div>";
				html += "<input type='submit' class='btn btn-success' value='Bearbeiten' /> <a onclick='preview(); return true;' href='#preview' class='btn btn-default'>Vorschau</a> <a class='btn btn-default' href='/admin/news'>Abbrechen</a></form><div id='preview'></div>";
				cd.getModel().addAttribute("content", html);
				cd.getModel().addAttribute("specialstyle", "news.css");
				cd.getModel().addAttribute("srcscripts", new String[]
				{ "/js/previewnews.js" });
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@GetMapping("/change")
	String newsChangeGet(@ModelAttribute("ControllerData") ControllerData cd)
	{
		return news(cd);
	}

	@PostMapping("/change")
	String newsChangePost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("newsId") int newsId, @RequestParam("title") String title, @RequestParam("msg") String msg, @RequestParam("type") int type)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getDataProvider().updateNews(newsId, Utils.cleanHtml(title, Whitelist.none()), Utils.cleanNewsMsg(msg), NewsType.byValue(type));
				cd.getModel().addAttribute("content", "<p>Eintrag wurde bearbeitet.</p>" + Bootstrap.button("Zurück", "/admin/news", ButtonType.SUCCESS) + "<br class='clear' />" + cd.getDataProvider().getNews(newsId).toHtml(false));
				cd.getModel().addAttribute("specialstyle", "news.css");
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@RequestMapping("/delete/{newsId}")
	String newsDelete(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int newsId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				News news = cd.getDataProvider().getNews(newsId);
				String form = "<p>Eintrag wirklich löschen?</p><br /><form style='display: inline-block;' method='post' action='/admin/news/delete'><input type='hidden' name='newsId' value='" + news.id + "' /><input class='btn btn-danger' type='submit' name='delete' value='Löschen'/></form> <a style='display: inline-block; position: relative;' class='btn btn-default' href='/admin/news'>Abbrechen</a><br class='clear'/>";
				cd.getModel().addAttribute("content", form + news.toHtml(false));
				cd.getModel().addAttribute("specialstyle", "news.css");
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@PostMapping("/delete")
	String newsDeletePost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("newsId") int newsId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getDataProvider().setNewsDeleted(newsId, true);
				cd.getModel().addAttribute("content", "<p>Eintrag wurde gelöscht.</p>" + Bootstrap.button("Zurück", "/admin/news", ButtonType.SUCCESS));
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@RequestMapping("/reactivate/{newsId}")
	String newsReactivate(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int newsId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				News news = cd.getDataProvider().getNews(newsId);
				String form = "<p>Eintrag wirklich wiederherstellen?</p><br /><form style='display: inline-block;' method='post' action='/admin/news/reactivate'><input type='hidden' name='newsId' value='" + news.id + "' /><input class='btn btn-warning' type='submit' name='delete' value='Wiederherstellen'/></form> <a style='display: inline-block; position: relative;' class='btn btn-default' href='/admin/news'>Abbrechen</a><br class='clear'/>";
				cd.getModel().addAttribute("content", form + news.toHtml(false));
				cd.getModel().addAttribute("specialstyle", "news.css");
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}

	@PostMapping("/reactivate")
	String newsReactivatePost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("newsId") int newsId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getDataProvider().setNewsDeleted(newsId, false);
				cd.getModel().addAttribute("content", "<p>Eintrag wurde wiederhergestellt.</p>" + Bootstrap.button("Zurück", "/admin/news", ButtonType.SUCCESS));
			}
			else
			{
				cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_PERMITTED);
			}
		}
		else
		{
			cd.getModel().addAttribute("content", Config.VALUE_TEXT_ADMINAREA_NOT_LOGGEDIN);
		}
		return "index";
	}
}
