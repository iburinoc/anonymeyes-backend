package com.topsradiance.anonymeyes.backend;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Server {
	public static Map<Long, Handler> handlerMap;
	public static Deque<byte[]> bufQueue;

	public static void main(String[] args) throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(52525);
		bufQueue = new ArrayDeque<byte[]>();
		for(int i = 0; i < 1000; i++) {
			bufQueue.addLast(new byte[65000]);

		}
		handlerMap = new HashMap<Long, Handler>();
		
		new Cleaner().start();

		while(true) {
			try {
				byte[] recBuf = bufQueue.remove();
				DatagramPacket receivePacket = new DatagramPacket(recBuf, recBuf.length);
				serverSocket.receive(receivePacket);
				System.out.println("RECEIVED: " + receivePacket.getAddress().toString());
				long id = getId(recBuf);

				if(handlerMap.get(id) == null) {
					Location l = new Location(recBuf);
					handlerMap.put(id, new Handler(id, l, getWidth(recBuf), getHeight(recBuf)));
				}

				handlerMap.get(id).data(recBuf);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static long getId(byte[] buf) {
		return (buf[0] << 56) |
				(buf[1] << 48) |
				(buf[2] << 40) |
				(buf[3] << 32) |
				(buf[4] << 24) |
				(buf[5] << 16) |
				(buf[6] << 8) |
				buf[7];
	}

	public static int getWidth(byte[] buf) {
		return (buf[24] << 8) | buf[25];
	}
	public static int getHeight(byte[] buf) {
		return (buf[26] << 8) | buf[27];
	}
}
