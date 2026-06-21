package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.ConnectionHandler;
import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

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
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.thePlayer != null && mc.getNetHandler() != null) {
				NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
				if (playerInfo != null) {
					this.value = playerInfo.getResponseTime() + "ms";
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
