package de.headshotharp.web.util.graphics;

public class Bootstrap {
	public static String button(String title, String link, ButtonType type) {
		return "<a class='btn btn-" + type + "' style='margin-bottom: 0.2em;' href='" + link + "'>" + title + "</a> ";
	}

	public static enum ButtonType {
		DEFAULT("default"), SUCCESS("success"), WARNING("warning"), DANGER("danger");

		private String css;

		ButtonType(String css) {
			this.css = css;
		}

		@Override
		public String toString() {
			return css;
		}
	}
}
