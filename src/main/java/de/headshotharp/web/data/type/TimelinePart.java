package de.headshotharp.web.data.type;

import de.headshotharp.web.util.DateTime;

public class TimelinePart
{
	public int block_break;
	public int block_place;
	public DateTime timestamp;

	public TimelinePart(int block_break, int block_place, DateTime timestamp)
	{
		this.block_break = block_break;
		this.block_place = block_place;
		this.timestamp = timestamp;
	}
}
