package com.topsradiance.anonymeyes.backend;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.xuggle.xuggler.ICodec;

public class Server {
	public static Map<Long, Handler> handlerMap;
	public static Deque<byte[]> bufQueue;

	public static String ROOT_DIR;

	public static void main(String[] args) throws Exception {
		if(args.length >= 1) {
			ROOT_DIR = args[0];
		} else {
			ROOT_DIR = ".";
		}
		System.out.println("Using root dir " + ROOT_DIR);
		DatagramSocket serverSocket = new DatagramSocket(52525);
		
		handlerMap = new HashMap<Long, Handler>();

		new Cleaner().start();

		while(true) {
			try {
				byte[] recBuf = new byte[512];
				DatagramPacket receivePacket = new DatagramPacket(recBuf, recBuf.length);
				serverSocket.receive(receivePacket);
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
		return ((buf[0] & 0xff) << 56) |
				((buf[1] & 0xff) << 48) |
				((buf[2] & 0xff) << 40) |
				((buf[3] & 0xff) << 32) |
				((buf[4] & 0xff) << 24) |
				((buf[5] & 0xff) << 16) |
				((buf[6] & 0xff) << 8) |
				(buf[7] & 0xff);
	}

	public static int getWidth(byte[] buf) {
		return ((buf[25] & 0xff) << 8) | (buf[26] & 0xff);
	}
	public static int getHeight(byte[] buf) {
		return ((buf[27] & 0xff) << 8) | (buf[28] & 0xff);
	}
}
