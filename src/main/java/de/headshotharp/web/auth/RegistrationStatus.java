package de.headshotharp.web.auth;

public enum RegistrationStatus {
	UNDEFINED(""), NO_DATA(
			"Du musst in Minecraft zuerst den Befehl <code>/startregister</code> ausführen. Oder hast du dich beim Namen vertippt? Achte auch auf Groß-/Kleinschreibung."), WRONGE_CODE(
					"Der Sicherheitscode ist falsch. Gib in Minecraft <code>/startregister</code> bzw. <code>/resetpassword</code> ein, um einen neuen Code zu erhalten."), UNEQUAL_PASSWD(
							"Du hast nicht zweimal das gleiche Passwort eingegeben, bitte kontrolliere deine Eingaben."), NOT_REGISTERED(
									""), ALREADY_REGISTERED("Du bist bereits registriert."), SUCCESSFUL_REGISTERED(
											"Du hast dich erfolgreich registriert. Vielen Dank und viel Spaß!"), DB_ERROR(
													"Es ist ein Problem bei einer Datenbankabfrage aufgetreten. Bitte Probiere es erneut oder kontaktiere einen Admin."), SHORT_PASSWORD(
															"Das Passwort muss mindestens 8 Zeichen lang sein."), NOT_LOGGED_IN(
																	"");

	private String msg;

	private RegistrationStatus(String msg) {
		this.msg = msg;
	}

	public String getMessage() {
		return msg;
	}
}
