package com.topsradiance.anonymeyes.backend;

import java.awt.image.BufferedImage;
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

	private BufferedImage img;
	private long starttime;

	private List<byte[]> packets;
	private Map<Integer, Long> recTime;
	private List<byte[]> audio;
	private Map<Integer, Long> audioTime;
	private int width, height;

	private int maxFrame = 0;

	public Handler(Long id, Location loc) {
		this.packets = new ArrayList<byte[]>();
		this.recTime = new HashMap<Integer, Long>();
		this.audio = new ArrayList<byte[]>();
		this.audioTime = new HashMap<Integer, Long>();
		this.id = id;
		lastMessage = System.currentTimeMillis();
		fname = Long.toString(lastMessage / 1000L) + "," + loc.y + "," + loc.x + ".mp4";
		System.out.println("New handler with id " + id + " and fname " + fname);
		Endpoint.startRecording(fname);
		this.loc = loc;
		this.starttime = System.nanoTime();
	}

	public void data(byte[] buf) {
		try {
			lastMessage = System.currentTimeMillis();

			if(buf[24] == 0) {

				packets.add(buf);
				int frame = getFrameNum(buf);
				this.maxFrame = Math.max(this.maxFrame, frame);
				if(!recTime.containsKey(frame)) {
					recTime.put(frame, System.nanoTime() - starttime);
				}
			} else {
				audio.add(buf);
				int idx = getAudioIdx(buf);
				audioTime.put(idx, System.nanoTime() - starttime);
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
	
	private int getAudioIdx(byte[] buf) {
		return ((buf[27] & 0xff) << 24) |
				((buf[28] & 0xff) << 16) |
				((buf[29] & 0xff) <<  8) |
				((buf[30] & 0xff) <<  0);
	}
	
	private int getFrameNum(byte[] buf) {
		return ((buf[31] & 0xff) << 24) |
				((buf[32] & 0xff) << 16) |
				((buf[33] & 0xff) <<  8) |
				((buf[34] & 0xff) <<  0);
	}
	
	private int getRowNum(byte[] buf) {
		return ((buf[29] & 0xff) << 8) |
				((buf[30] & 0xff) << 0);
	}
	
	public void exit() {
		this.width = Server.getWidth(this.packets.get(0));
		this.height = Server.getHeight(this.packets.get(0));
		System.out.println("Closing handler " + id);
		Server.handlerMap.remove(id);
		
		System.out.println("Creating video for " + id);
		
		this.img = new BufferedImage(height, width, BufferedImage.TYPE_3BYTE_BGR);
		
		out = ToolFactory.makeWriter(Server.ROOT_DIR + "/" + fname);
		out.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, height, width);
		out.addAudioStream(1, 0, ICodec.ID.CODEC_ID_AAC, 1, 11025);
		
		List<List<byte[]>> frames = new ArrayList<List<byte[]>>();
		for(int i = 0; i <= maxFrame; i++) {
			frames.add(new ArrayList<byte[]>());
		}
		
		for(int i = 0; i < packets.size(); i++) {
			byte[] buf = packets.get(i);
			frames.get(getFrameNum(buf)).add(buf);
		}
		
		out.setForceInterleave(false);
		long lastVTime = 0, lastATime = 0;
		for(int i = 0, j = 0; i < frames.size() && j < audio.size();) {
			if(recTime.containsKey(i)) {
				lastVTime = recTime.get(i);
			}
			
			if(audioTime.containsKey(j)) {
				lastATime = audioTime.get(j);
			}
			
			if(lastATime < lastVTime) {
				byte[] samples = audio.get(j);
				if(samples != null) {
					short[] sbuf = new short[(((samples[25] & 0xff) << 8) | (samples[26] & 0xff))];
					for(int k = 0; k < sbuf.length; k++) {
						sbuf[k] = (short) (((samples[27 + k * 2] & 0xff) << 8) | (samples[27 + k * 2 + 1] & 0xff));
					}
					out.encodeAudio(1, sbuf);
				}
				j++;
				lastATime++;
			} else {


				for(int l = 0; l < frames.get(i).size(); l++) {
					byte[] buf = frames.get(i).get(l);
					int idx = 35;
					int offset = 0;
					for(int k = 0; k < width; k++) {

						int rgb = 0xff000000;
						rgb |= ((buf[idx] & 0xff) & (0xf << offset)) << (4 - offset);
						offset ^= 4; if(offset == 0) idx++;
						rgb |= (((buf[idx] & 0xff) & (0xf << offset)) << (4 - offset)) << 8;
						offset ^= 4; if(offset == 0) idx++;
						rgb |= (((buf[idx] & 0xff) & (0xf << offset)) << (4 - offset)) << 16;
						offset ^= 4; if(offset == 0) idx++;
						img.setRGB(height - 1 - getRowNum(buf), k, rgb);
					}
				}

				out.encodeVideo(0, img, lastVTime, TimeUnit.NANOSECONDS);
				lastVTime++;
			}
		}
		out.flush();
		out.close();
		
		Endpoint.doneRecording(fname);
	}
}
