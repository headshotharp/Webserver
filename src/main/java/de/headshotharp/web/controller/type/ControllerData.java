package de.headshotharp.web.controller.type;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;

import de.headshotharp.web.auth.Authentication;

public class ControllerData {
	private Model model;
	private HttpSession session;
	private Authentication auth;

	public ControllerData(Model model, HttpSession session, Authentication auth) {
		this.model = model;
		this.session = session;
		this.auth = auth;
	}

	public Model getModel() {
		return model;
	}

	public HttpSession getSession() {
		return session;
	}

	public Authentication getAuthentication() {
		return auth;
	}
}
