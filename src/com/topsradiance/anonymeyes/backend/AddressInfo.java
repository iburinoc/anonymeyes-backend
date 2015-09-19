package com.topsradiance.anonymeyes.backend;

import java.net.InetAddress;

public class AddressInfo {
	public final int port;
	public final InetAddress addr;
	
	public AddressInfo(int port, InetAddress addr) {
		this.port = port;
		this.addr = addr;
	}
}
