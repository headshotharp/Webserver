package de.headshotharp.web.util.graphics;

import java.util.ArrayList;
import java.util.List;

public class SvgDonut {
	private String svgId;
	private String dataName;
	private String svgName;
	private List<SvgDonutpart> parts;

	public SvgDonut(String svgId, String svgName, String dataName) {
		parts = new ArrayList<SvgDonutpart>();
		this.svgId = svgId;
		this.svgName = svgName;
		this.dataName = dataName;
	}

	public void addPart(SvgDonutpart part) {
		parts.add(part);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("var " + dataName + "=[");
		for (SvgDonutpart part : parts) {
			str.append(part.toString());
		}
		str.append("];");
		str.append("var " + svgName + " = d3.select('#" + svgId + "');");
		str.append("Donut3D.draw('" + svgId + "'," + dataName + ",150,120,130,100,30,0.4);");
		// id, data, centerX, centerY, radiusX, radiusY, height, innerRadius
		return str.toString();
	}

	public static class SvgDonutpart {
		public String label = "";
		public String color = "#ffffff";
		public int value = 0;

		public SvgDonutpart(String label, String color, int value) {
			this.label = label;
			this.color = color;
			this.value = value;
		}

		@Override
		public String toString() {
			return "{label:'" + label + "', color:'" + color + "', value:" + value + "},";
		}
	}
}
