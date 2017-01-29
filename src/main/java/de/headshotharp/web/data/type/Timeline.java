package de.headshotharp.web.data.type;

import java.util.List;

public class Timeline
{
	private List<TimelinePart> parts;
	private int max_block_break = 0, max_block_place = 0;
	private int max;

	public Timeline(List<TimelinePart> parts)
	{
		this.parts = parts;
		for (TimelinePart tlp : parts)
		{
			if (max_block_break < tlp.block_break) max_block_break = tlp.block_break;
			if (max_block_place < tlp.block_place) max_block_place = tlp.block_place;
		}
		max = Math.max(max_block_break, max_block_place);
	}

	public List<TimelinePart> getParts()
	{
		return parts;
	}

	public int getMaxBlockBreak()
	{
		return max_block_break;
	}

	public int getMaxBlockPlace()
	{
		return max_block_place;
	}

	public int getMax()
	{
		return max;
	}
}
