package com.topsradiance.anonymeyes.backend;

public class Cleaner extends Thread {
	@Override
	public void run() {
		while(true) {
			System.out.println("Cleaning");
			long cTime = System.currentTimeMillis();
			for(Long id : Server.handlerMap.keySet()) {
				Handler h = Server.handlerMap.get(id);
				if(cTime - h.lastMessage >= 5000) {
					h.exit();
				}
			}

			try {
				Thread.sleep(2500);
			} catch(InterruptedException e) {}
		}
	}
}
