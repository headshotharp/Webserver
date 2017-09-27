package de.headshotharp.web.data.type;

public enum PlayerStatus {
	OK(1, "success"), INACTIVE(2, "active"), NO_ACCOUNT(3, "danger"), NO_LOGIN(0, "warning"), NOTHING(-1, "");

	private int val;
	private String css;

	PlayerStatus(int val, String css) {
		this.val = val;
		this.css = css;
	}

	public int getValue() {
		return val;
	}

	public static PlayerStatus byValue(int val) {
		for (PlayerStatus status : values())
			if (status.getValue() == val)
				return status;
		return PlayerStatus.NOTHING;
	}

	public String getBootstrapClass() {
		return css;
	}
}
