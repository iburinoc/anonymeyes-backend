package com.topsradiance.anonymeyes.backend;

public class Location {
	public final double x, y;
	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Location(byte[] buf) {
		long a = (buf[8] << 56) |
				(buf[9] << 48) |
				(buf[10] << 40) |
				(buf[11] << 32) |
				(buf[12] << 24) |
				(buf[13] << 16) |
				(buf[14] << 8) |
				buf[15];
		this.x = Double.longBitsToDouble(a);
		long b = (buf[16] << 56) |
				(buf[17] << 48) |
				(buf[18] << 40) |
				(buf[19] << 32) |
				(buf[20] << 24) |
				(buf[21] << 16) |
				(buf[22] << 8) |
				buf[23];
		this.y = Double.longBitsToDouble(b);
	}
}
