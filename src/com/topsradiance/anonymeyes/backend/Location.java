package com.topsradiance.anonymeyes.backend;

public class Location {
	public final double x, y;
	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Location(byte[] buf) {
		long a = bytesToLong(buf, 8);
		System.out.println("lat long: " + Long.toHexString(a));
		this.y = Double.longBitsToDouble(a);
		long b = bytesToLong(buf, 16);
		this.x = Double.longBitsToDouble(b);
		System.out.println("lon long: " + Long.toHexString(b));
	}
	
	private long bytesToLong(byte[] b, int idx) {
	        long result = 0;
	        for (int i = 0; i < 8; i++) {
	            result <<= 8;
	            result |= (b[i+idx] & 0xFF);
	        }
	        return result;
	    }
}
