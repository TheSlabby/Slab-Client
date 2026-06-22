package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.ConnectionHandler;
import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingModule extends Module {

	public PingModule(int x, int y) {
		super(x, y);
		this.key = "Ping";
		this.value = "Disconnected";
	}

	@Override
	public void update() {
		this.visible = ConnectionHandler.remote && this.enabled;
		if (ConnectionHandler.remote) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && mc.getConnection() != null) {
				PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(mc.player.getUUID());
				if (playerInfo != null) {
					this.value = playerInfo.getLatency() + "ms";
				} else {
					this.value = "0ms";
				}
			} else {
				this.value = "0ms";
			}
		} else {
			this.value = "Disconnected";
		}
	}

}
