package com.topsradiance.anonymeyes.backend;

public class Location {
	public final double x, y;
	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Location(byte[] buf) {
		long a = ((buf[8] & 0xff) << 56) |
				((buf[9] & 0xff) << 48) |
				((buf[10] & 0xff) << 40) |
				((buf[11] & 0xff) << 32) |
				((buf[12] & 0xff) << 24) |
				((buf[13] & 0xff) << 16) |
				((buf[14] & 0xff) << 8) |
				(buf[15] & 0xff);
		this.y = Double.longBitsToDouble(a);
		long b = ((buf[16] & 0xff) << 56) |
				((buf[17] & 0xff) << 48) |
				((buf[18] & 0xff) << 40) |
				((buf[19] & 0xff) << 32) |
				((buf[20] & 0xff) << 24) |
				((buf[21] & 0xff) << 16) |
				((buf[22] & 0xff) << 8) |
				(buf[23] & 0xff);
		this.x = Double.longBitsToDouble(b);
	}
}
