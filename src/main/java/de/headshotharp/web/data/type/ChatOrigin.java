package de.headshotharp.web.data.type;

public enum ChatOrigin {
	SERVER(0), WEB(1), MINECRAFT(2);
	private int number;

	ChatOrigin(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public static ChatOrigin byNumber(int number) {
		for (ChatOrigin co : values()) {
			if (co.number == number)
				return co;
		}
		return null;
	}
}
