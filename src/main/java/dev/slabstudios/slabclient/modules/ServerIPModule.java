package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.ConnectionHandler;
import dev.slabstudios.slabclient.Module;

public class ServerIPModule extends Module {

	public ServerIPModule(int x, int y) {
		super(x, y);
		
		this.key = "IP";
		this.value = "Disconnected";
	}
	
	@Override
	public void update() {
		this.visible = ConnectionHandler.remote && this.enabled;
		if(ConnectionHandler.remote) {
			this.value = ConnectionHandler.ip;
		}else {
			this.value = "Disconnected";
		}
	}
	
}
