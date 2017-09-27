package de.headshotharp.web.util.graphics;

import de.headshotharp.web.data.type.Timeline;
import de.headshotharp.web.data.type.TimelinePart;

public class Svg {
	public static String lineGraph(Timeline timeline) {
		// settings
		int fontSize = 10;
		int sideoffset = 90;
		int legend_height_part = 40;
		int barwidth = 15;
		int barspace = 10;
		int[] legend = makeLegendArray(0, timeline.getMax(), 8);
		int newMax = legend[legend.length - 1];
		int height = legend_height_part * legend.length;
		int legend_right_offset = sideoffset + 31 * (barwidth * 2 + barspace + 1) - barspace;
		int width = legend_right_offset + sideoffset;
		// go
		StringBuilder str = new StringBuilder();
		str.append(svgFull(width, height));
		str.append(rect(0, 0, width, height, "black", "white"));
		// legend left
		int yoffsetlegend = 20;
		str.append(group(0, 0));
		str.append(legendHighlight(sideoffset, legend_right_offset - sideoffset, legend_height_part, yoffsetlegend,
				legend.length, "#eee"));
		str.append(Svg.legendLeft(sideoffset, legend_height_part, 10, yoffsetlegend, legend));
		str.append(Svg.legendRight(legend_right_offset, legend_height_part, 10, yoffsetlegend, legend));
		// bar chart
		int baseline = yoffsetlegend + legend_height_part * (legend.length - 1);
		int real_height = legend_height_part * (legend.length - 1);
		int i = 0;
		for (TimelinePart tlp : timeline.getParts()) {
			int x = sideoffset + i * (barwidth * 2 + barspace);
			// block_break
			int valHeight = Math.max(tlp.block_break * real_height / newMax, 1);
			str.append(rect(x, baseline - valHeight, barwidth, valHeight, "same", "rgb(52, 173, 78)"));
			// block_place
			valHeight = Math.max(tlp.block_place * real_height / newMax, 1);
			str.append(rect(x + barwidth + 1, baseline - valHeight, barwidth, valHeight, "same", "#00cfff"));
			// text
			str.append(part("text", true, "x", x + barwidth + 1, "y", baseline + fontSize * 3 / 2, "font-size",
					fontSize + "px", "text-anchor", "middle", tlp.timestamp.day + "."));
			i++;
		}
		str.append("</g>");
		str.append(endSvg());
		return str.toString();
	}

	public static String legendLeft(int width, int height_part, int line, int yoffset, int... values) {
		int fontSize = 10;
		StringBuilder str = new StringBuilder();
		int x1 = width - line;
		int y;
		for (int i = 0; i < values.length; i++) {
			y = height_part * i + yoffset;
			str.append(part("line", true, "x1", x1, "y1", y, "x2", width, "y2", y, "stroke", "black", "stroke-width",
					"1"));
			str.append(part("text", true, "x", x1 - 5, "y", y + fontSize / 2, "font-size", fontSize + "px",
					"text-anchor", "end", values[values.length - 1 - i]));
		}
		str.append(part("line", true, "x1", width, "y1", yoffset, "x2", width, "y2",
				height_part * (values.length - 1) + yoffset, "stroke", "black", "stroke-width", "1"));
		return str.toString();
	}

	public static String legendRight(int xoffset, int height_part, int line, int yoffset, int... values) {
		int fontSize = 10;
		StringBuilder str = new StringBuilder();
		int y;
		for (int i = 0; i < values.length; i++) {
			y = height_part * i + yoffset;
			str.append(part("line", true, "x1", xoffset, "y1", y, "x2", xoffset + line, "y2", y, "stroke", "black",
					"stroke-width", "1"));
			str.append(part("text", true, "x", xoffset + line + 5, "y", y + fontSize / 2, "font-size", fontSize + "px",
					"text-anchor", "start", values[values.length - 1 - i]));
		}
		str.append(part("line", true, "x1", xoffset, "y1", yoffset, "x2", xoffset, "y2",
				height_part * (values.length - 1) + yoffset, "stroke", "black", "stroke-width", "1"));
		return str.toString();
	}

	public static String legendHighlight(int xoffset, int width, int height_part, int yoffset, int amount,
			String color) {
		StringBuilder str = new StringBuilder();
		int y;
		for (int i = 0; i < amount; i += 2) {
			y = height_part * i + yoffset;
			str.append(rect(xoffset, y, width, height_part, color, color));
		}
		return str.toString();
	}

	public static String part(String type, boolean close, Object... attr) {
		StringBuilder str = new StringBuilder();
		str.append("<" + type);
		for (int i = 0; i < attr.length / 2; i++) {
			str.append(" " + attr[i * 2] + "='" + attr[i * 2 + 1] + "'");
		}
		if (attr.length % 2 == 0) {
			if (close)
				str.append(" />");
			else
				str.append(">");
		} else {
			str.append(">" + attr[attr.length - 1]);
			if (close)
				str.append("</" + type + ">");
		}
		return str.toString();
	}

	public static String svg(int width, int height) {
		return "<svg width='" + width + "px' height='" + height + "px' viewBox='0 0 " + width + " " + height + "'>";
	}

	public static String svgFull(int width, int height) {
		return "<svg width='100%' viewBox='0 0 " + width + " " + height + "'>";
	}

	public static String endSvg() {
		return "</svg>";
	}

	public static String rect(int x, int y, int width, int height, String stroke, String fill) {
		if (stroke.equalsIgnoreCase("same"))
			stroke = fill;
		if (fill.equalsIgnoreCase("same"))
			fill = stroke;
		return part("rect", true, "x", x, "y", y, "width", width, "height", height, "stroke", stroke, "fill", fill,
				"stroke-width", 1);
	}

	public static String group(int x, int y) {
		return part("g", false, "transform", "translate(" + x + "," + y + ")");
	}

	public static String group(int x, int y, String inner) {
		return part("g", true, "transform", "translate(" + x + "," + y + ")", inner);
	}

	public static int[] makeLegendArray(int min, int max, int fields) {
		int diff = (max - min) / (fields - 1);
		diff = round(diff);
		int[] arr = new int[fields];
		for (int i = 0; i < fields; i++) {
			arr[i] = i * diff;
		}
		return arr;
	}

	public static int round(int i) {
		if (i <= 10)
			return 10;
		if (i <= 20)
			return 20;
		if (i <= 30)
			return 30;
		if (i <= 50)
			return 50;
		if (i <= 100)
			return 100;
		int digits = String.valueOf(i).length();
		int c = 1;
		for (int x = 0; x < digits - 2; x++) {
			c *= 10;
		}
		return i / c * c + c;
	}
}
