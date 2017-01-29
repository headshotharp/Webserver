package de.headshotharp.web.util.graphics;

import java.awt.Color;

public class ColorUtils
{
	public static int mixValue(int a, int b, float mix)
	{
		if (b > a) mix = 1 - mix;
		return (int) ((Math.max(a, b) - Math.min(a, b)) * mix + Math.min(a, b));
	}

	public static class ColorMixer
	{
		Color a, b;

		public ColorMixer(Color a, Color b)
		{
			this.a = a;
			this.b = b;
		}

		public Color mix(float mix)
		{
			return new Color(mixValue(a.getRed(), b.getRed(), mix), mixValue(a.getGreen(), b.getGreen(), mix), mixValue(a.getBlue(), b.getBlue(), mix));
		}
	}
}
