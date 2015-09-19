package com.topsradiance.anonymeyes.backend;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class Handler {
	public long id;
	public String fname;
	public Location loc;
	public long lastMessage;
	
	private static final int FRAME_RATE = 20;
	private IMediaWriter out;
	
	private WritableRaster imgRaster;
	private BufferedImage img;
	private int[] colorBuf;
	private long starttime;
	
	public Handler(Long id, Location loc, int width, int height) {
		this.id = id;
		lastMessage = System.currentTimeMillis();
		String fname = "./" + Long.toString(lastMessage / 1000L) + "," + loc.y + "," + loc.x + ".mp4";
		System.out.println("New handler with id " + id + " and fname " + fname + ", dimensions " + width + "," + height);
		this.img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		this.imgRaster = this.img.getRaster();
		this.colorBuf = new int[width * height * 3];
		this.loc = loc;
		
		out = ToolFactory.makeWriter(fname);
		out.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, width, height);
		this.starttime = System.nanoTime();
	}
	
	public void data(byte[] buf) {
		try {
			lastMessage = System.currentTimeMillis();

			int idx = 28;
			int offset = 0;
			for(int i = 0; i < colorBuf.length; i++) {
				colorBuf[i] = (buf[idx] & (0xf << offset)) << (4 - offset);
				offset ^= 4;
				if(offset == 0) idx++;
			}

			imgRaster.setPixels(0, 0, img.getWidth(), img.getHeight(), colorBuf);
			out.encodeVideo(0, img, System.nanoTime() - starttime, TimeUnit.NANOSECONDS);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exit() {
		System.out.println("Closing handler " + id);
		out.close();
		Server.handlerMap.remove(id);
		
		Endpoint.request(fname);
	}
}
