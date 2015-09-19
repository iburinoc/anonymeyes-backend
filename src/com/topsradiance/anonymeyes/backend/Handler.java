package com.topsradiance.anonymeyes.backend;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class Handler {
	public long id;
	public String fname;
	public Location loc;
	public long lastMessage;
	
	private IMediaWriter out;
	
	private WritableRaster imgRaster;
	private BufferedImage img;
	private int[] colorBuf;
	private long starttime;
	
	private List<byte[]> packets;
	private Map<Integer, Long> recTime;
	private int width, height;
	
	private int maxFrame = 0;
	
	public Handler(Long id, Location loc, int width, int height) {
		this.packets = new ArrayList<byte[]>();
		this.recTime = new HashMap<Integer, Long>();
		this.id = id;
		this.width = width;
		this.height = height;
		lastMessage = System.currentTimeMillis();
		fname = "./" + Long.toString(lastMessage / 1000L) + "," + loc.y + "," + loc.x + ".mp4";
		System.out.println("New handler with id " + id + " and fname " + fname + ", dimensions " + width + "," + height);
		this.loc = loc;
		this.starttime = System.nanoTime();
	}
	
	public void data(byte[] buf) {
		try {
			lastMessage = System.currentTimeMillis();

			packets.add(buf);
			int frame = getFrameNum(buf);
			this.maxFrame = Math.max(this.maxFrame, frame);
			if(!recTime.containsKey(frame)) {
				recTime.put(frame, System.nanoTime() - starttime);
			}
			/*
			int idx = 28;
			int offset = 0;
			for(int i = 0; i < colorBuf.length; i++) {
				colorBuf[i] = (buf[idx] & (0xf << offset)) << (4 - offset);
				offset ^= 4;
				if(offset == 0) idx++;
			}

			imgRaster.setPixels(0, 0, img.getWidth(), img.getHeight(), colorBuf);
			out.encodeVideo(0, img, System.nanoTime() - starttime, TimeUnit.NANOSECONDS);*/
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private int getFrameNum(byte[] buf) {
		return (buf[30] << 24) |
				(buf[31] << 16) |
				(buf[32] <<  8) |
				(buf[33] <<  0);
	}
	
	private int getRowNum(byte[] buf) {
		return (buf[28] << 8) |
				(buf[29] << 0);
	}
	
	public void exit() {
		System.out.println("Closing handler " + id);
		Server.handlerMap.remove(id);
		
		System.out.println("Creating video for " + id);
		
		this.img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		this.imgRaster = this.img.getRaster();
		this.colorBuf = new int[width * 3];
		
		out = ToolFactory.makeWriter(fname);
		out.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, width, height);
		
		List<List<byte[]>> frames = new ArrayList<List<byte[]>>();
		for(int i = 0; i < maxFrame; i++) {
			frames.add(new ArrayList<byte[]>());
		}
		
		for(int i = 0; i < packets.size(); i++) {
			byte[] buf = packets.get(i);
			frames.get(getFrameNum(buf)).add(buf);
		}
		
		long lastTime = 0;
		for(int i = 0; i < frames.size(); i++) {
			if(recTime.containsKey(i)) {
				lastTime = recTime.get(i);
			} else {
				lastTime++;
			}
			
			for(int j = 0; j < frames.get(i).size(); j++) {
				byte[] buf = frames.get(i).get(j);
				int idx = 34;
				int offset = 0;
				for(int k = 0; k < width; k++) {
					colorBuf[k] = (buf[idx] & (0xf << offset)) << (4 - offset);
					offset ^= 4; if(offset == 0) idx++;
				}
				this.imgRaster.setPixels(0, getRowNum(buf), width, 1, colorBuf);
			}
			
			out.encodeVideo(0, img, lastTime, TimeUnit.NANOSECONDS);
		}
		out.close();
		
		Endpoint.request(fname);
	}
}
