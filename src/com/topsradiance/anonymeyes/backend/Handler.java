package com.topsradiance.anonymeyes.backend;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class Handler {
	public String fname;
	public Location loc;
	public long lastMessage;
	
	private static final int FRAME_RATE = 20;
	private IMediaWriter out;
	
	public Handler(Long id, Location loc) {
		lastMessage = System.currentTimeMillis();
		this.loc = loc;
		
		String fname = "./" + Long.toString(lastMessage / 1000L) + "," + loc.y + "," + loc.x + ".mp4";
		
		out = ToolFactory.makeWriter(fname);
		out.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 360, 480);
	}
	
	public void data(byte[] buf) {
		lastMessage = System.currentTimeMillis();
		
		
	}
}
