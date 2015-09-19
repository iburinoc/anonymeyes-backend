package com.topsradiance.anonymeyes.backend;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Endpoint {
	public static void request(String fname) {
		try {
			String addr = "http://localhost/new_video";
			URL url = new URL(addr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(50);
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("filename=" + fname);
			wr.flush();
			wr.close();
			
			con.getResponseCode();
			
			System.out.println("Request made to " + addr + " with content " + fname);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
