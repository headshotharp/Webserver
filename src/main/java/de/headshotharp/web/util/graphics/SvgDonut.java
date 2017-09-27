package de.headshotharp.web.util.graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.headshotharp.commonutils.CommonUtils;
import de.headshotharp.web.util.graphics.ColorUtils.ColorMixer;

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

	public void autoColor(Color a, Color b) {
		ColorMixer mixer = new ColorMixer(a, b);
		int amount = 0;
		int c = 0;
		for (SvgDonutpart part : parts) {
			amount += part.value;
			if (part.color == null)
				c++;
		}
		if (c == 0)
			return;
		for (SvgDonutpart part : parts) {
			if (part.color == null)
				part.color = mixer.mix(part.value / (float) amount);
		}
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
		public Color color = null;
		public int value = 0;

		public SvgDonutpart(String label, Color c, int value) {
			this.label = label;
			this.color = c;
			this.value = value;
		}

		public SvgDonutpart(String label, int value) {
			this.label = label;
			this.value = value;
		}

		@Override
		public String toString() {
			String c;
			if (color != null)
				c = CommonUtils.colorToHtml(color);
			else
				c = "#ffffff";
			return "{label:'" + label + "', color:'" + c + "', value:" + value + "},";
		}
	}
}
