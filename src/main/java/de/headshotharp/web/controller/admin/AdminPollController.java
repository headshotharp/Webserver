package de.headshotharp.web.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.headshotharp.web.Config;
import de.headshotharp.web.auth.PermissionsGroup;
import de.headshotharp.web.controller.type.ControllerData;
import de.headshotharp.web.data.type.Player;
import de.headshotharp.web.data.type.Poll;
import de.headshotharp.web.util.DateTime;
import de.headshotharp.web.util.DateTime.DateTimeFormat;
import de.headshotharp.web.util.graphics.Bootstrap;
import de.headshotharp.web.util.graphics.Bootstrap.ButtonType;
import de.headshotharp.web.controller.DefaultController;

@Controller
@RequestMapping("/admin/poll")
public class AdminPollController extends DefaultController
{
	public static PermissionsGroup permissionsGroup = PermissionsGroup.ADMIN;

	@RequestMapping("")
	String poll(@ModelAttribute("ControllerData") ControllerData cd)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getModel().addAttribute("content", Bootstrap.button("Zurück", "/admin", ButtonType.DEFAULT) + "<br /><hr /><h3>Administration Umfragen</h3>" + Bootstrap.button("Neue Umfrage", "/admin/poll/add", ButtonType.DEFAULT) + "<br /><br /><p><b>Umfrage bearbeiten:</b></p>" + Poll.toAdminListHtml(cd.getDataProvider().getPollBasicList(true)));
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
	String pollAdd(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam Map<String, String> requestParams)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getModel().addAttribute("content", makeForm(requestParams));
			}
			else
			{
				cd.getModel().addAttribute("content", "<p>Es ist dir nicht erlaubt den Blog zu bearbeiten.</p>");
			}
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Blog zu bearbeiten.</p>");
		}
		return "index";
	}

	@PostMapping("/add")
	String pollAddPost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam Map<String, String> requestParams)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				String title = requestParams.get("title");
				if (title.length() == 0)
				{
					cd.getModel().addAttribute("content", "<p class='bg-danger'>Es muss ein Titel eingegeben werden.</p>" + makeForm(requestParams));
				}
				else
				{
					String desc = requestParams.get("desc");
					int duration = 30;
					try
					{
						duration = Integer.parseInt(requestParams.get("duration"));
					}
					catch (NumberFormatException e)
					{

					}
					DateTime end = DateTime.now().addDays(duration);
					List<String> options = new ArrayList<String>();
					for (String key : requestParams.keySet())
					{
						if (key.startsWith("option"))
						{
							String option = requestParams.get(key);
							if (option.length() > 0) options.add(option);
						}
					}
					if (options.size() >= 2)
					{
						int pollid = cd.getDataProvider().addPoll(player.id, title, desc, end.format(DateTimeFormat.FORMAT_SQL_DATESTAMP.getSimpleDateFormat()));
						if (pollid > 0)
						{
							for (String key : requestParams.keySet())
							{
								if (key.startsWith("option"))
								{
									String option = requestParams.get(key);
									if (option.length() > 0) cd.getDataProvider().addPollOption(pollid, option);
								}
							}
							cd.getModel().addAttribute("content", "<p>Umfrage wurde hinzugefügt.</p><br />" + Bootstrap.button("Zurück", "/admin/poll", ButtonType.SUCCESS));
						}
						else
						{
							cd.getModel().addAttribute("content", "<p>Fehler beim Eintragen der Umfrage.</p>");
						}
					}
					else
					{
						cd.getModel().addAttribute("content", "<p class='bg-danger'>Es müssen mindestens 2 Optionen ausgefüllt werden.</p>" + makeForm(requestParams));
					}
				}
			}
			else
			{
				cd.getModel().addAttribute("content", "<p>Es ist dir nicht erlaubt den Blog zu bearbeiten.</p>");
			}
		}
		else
		{
			cd.getModel().addAttribute("content", "<p>Du musst eingeloggt sein, um den Blog zu bearbeiten.</p>");
		}
		return "index";
	}

	@RequestMapping("/delete/{pollId}")
	String pollDelete(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int pollId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				Poll poll = cd.getDataProvider().getPollWithoutOptions(pollId);
				if (poll != null)
				{
					String html = "<p>Umfrage <b>" + poll.title + "</b> wirklich löschen?</p><br /><form style='display: inline-block;' method='post' action='/admin/poll/delete'><input type='hidden' name='pollId' value='" + poll.id + "' /><input class='btn btn-danger' type='submit' name='delete' value='Löschen'/></form> <a style='display: inline-block; position: relative;' class='btn btn-default' href='/admin/poll'>Abbrechen</a><br class='clear'/>";
					cd.getModel().addAttribute("content", html);
				}
				else
				{
					cd.getModel().addAttribute("content", "<p>Die Umfrage existiert nicht.</p>" + Bootstrap.button("Zurück", "/admin/poll", ButtonType.SUCCESS));
				}
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
	String pollDeletePost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("pollId") int pollId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getDataProvider().setPollDeleted(pollId, true);
				cd.getModel().addAttribute("content", "<p>Eintrag wurde gelöscht.</p>" + Bootstrap.button("Zurück", "/admin/poll", ButtonType.SUCCESS));
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

	@RequestMapping("/reactivate/{pollId}")
	String pollReactivate(@ModelAttribute("ControllerData") ControllerData cd, @PathVariable int pollId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				Poll poll = cd.getDataProvider().getPollWithoutOptions(pollId);
				if (poll != null)
				{
					String html = "<p>Umfrage <b>" + poll.title + "</b> wirklich wiederherstellen?</p><br /><form style='display: inline-block;' method='post' action='/admin/poll/delete'><input type='hidden' name='pollId' value='" + poll.id + "' /><input class='btn btn-danger' type='submit' name='delete' value='Löschen'/></form> <a style='display: inline-block; position: relative;' class='btn btn-default' href='/admin/poll'>Abbrechen</a><br class='clear'/>";
					cd.getModel().addAttribute("content", html);
				}
				else
				{
					cd.getModel().addAttribute("content", "<p>Die Umfrage existiert nicht.</p>" + Bootstrap.button("Zurück", "/admin/poll", ButtonType.SUCCESS));
				}
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
	String pollreactivatePost(@ModelAttribute("ControllerData") ControllerData cd, @RequestParam("pollId") int pollId)
	{
		cd.getModel().addAttribute("bg", "");
		if (cd.getAuthentication().isLoggedIn())
		{
			Player player = cd.getAuthentication().getPlayer();
			if (player.hasPermission(permissionsGroup))
			{
				cd.getDataProvider().setPollDeleted(pollId, false);
				cd.getModel().addAttribute("content", "<p>Eintrag wurde wiederhergestellt.</p>" + Bootstrap.button("Zurück", "/admin/poll", ButtonType.SUCCESS));
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

	private String makeForm(@RequestParam Map<String, String> requestParams)
	{
		String title = requestParams.get("title");
		if (title == null) title = "";
		String desc = requestParams.get("desc");
		if (desc == null) desc = "";
		String duration = requestParams.get("duration");
		if (duration == null) duration = "30";
		List<String> options = new ArrayList<String>();
		for (String key : requestParams.keySet())
		{
			if (key.startsWith("option"))
			{
				String option = requestParams.get(key);
				if (option.length() > 0) options.add(option);
			}
		}
		int diff = Config.VALUE_POLL_OPTION_SIZE - options.size();
		for (int i = 0; i < diff; i++)
		{
			options.add("");
		}
		String html = "<h3>Neue Umfrage:</h3><form accept-charset='UTF-8' method='post' action='/admin/poll/add'>";
		html += "<div class='form-group'><label>Titel:</label><input type='text' class='form-control' name='title' value='" + title + "'></div><div class='form-group'><label>Beschreibung:</label><textarea class='form-control' name='desc' rows='5'>" + desc + "</textarea></div><div class='form-group'><label>Dauer: <small>(in Tagen)</small></label><input class='form-control' type='text' name='duration' value='" + duration + "' /></div><div class='form-group'>";
		for (int i = 1; i <= Config.VALUE_POLL_OPTION_SIZE; i++)
			html += "<label>Option " + i + ":</label><input class='form-control' type='text' name='option" + i + "' value='" + options.get(i - 1) + "' />";
		html += "</div><input type='submit' class='btn btn-success' value='Hinzufügen' /> <a class='btn btn-default' href='/admin/poll'>Abbrechen</a></form>";
		return html;
	}
}
