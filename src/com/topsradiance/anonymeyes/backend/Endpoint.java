package com.topsradiance.anonymeyes.backend;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Endpoint {
	public static void request(String fname) {
		try {
			String addr = "http://localhost/newstream";
			URL url = new URL(addr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(fname);
			wr.flush();
			wr.close();
			
			con.getResponseCode();
			
			System.out.println("Request made to " + addr + " with content " + fname);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
