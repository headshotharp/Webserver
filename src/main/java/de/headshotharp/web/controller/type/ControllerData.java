package de.headshotharp.web.controller.type;

import java.io.Closeable;
import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;
import de.headshotharp.web.auth.Authentication;
import de.headshotharp.web.data.DataProvider;

public class ControllerData implements Closeable
{
	private Model model;
	private HttpSession session;
	private DataProvider dp;
	private Authentication auth;

	public ControllerData(Model model, HttpSession session, DataProvider dp, Authentication auth)
	{
		this.model = model;
		this.session = session;
		this.dp = dp;
		this.auth = auth;
	}

	public Model getModel()
	{
		return model;
	}

	public HttpSession getSession()
	{
		return session;
	}

	public DataProvider getDataProvider()
	{
		return dp;
	}

	public Authentication getAuthentication()
	{
		return auth;
	}

	@Override
	public void close()
	{
		auth.close();
	}
}
