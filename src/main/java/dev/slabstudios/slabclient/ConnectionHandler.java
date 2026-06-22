package dev.slabstudios.slabclient;
 
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import com.google.gson.JsonObject;
 
public class ConnectionHandler {
 
	public static String ip = "Disconnected";
	public static boolean remote = false;
	private int ticks = 0;
	
	public ConnectionHandler() {
		System.out.println("Connection handler active!");
	}
	
	@SubscribeEvent
	public void onConnect(ClientPlayerNetworkEvent.LoggingIn event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getCurrentServer() != null) {
			ip = mc.getCurrentServer().ip;
			remote = true;
		} else {
			remote = false;
			ip = "Disconnected";
		}
		
		// Notify C++ backend of server change
		sendServerChange();
	}

	@SubscribeEvent
	public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		ip = "Disconnected";
		remote = false;
		
		// Notify C++ backend of server change
		sendServerChange();
	}

	@SubscribeEvent
	public void onTick(ClientTickEvent.Post event) {
		ticks++;
		if (ticks >= 20) {
			ticks = 0;
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && mc.level != null && SlabSocketClient.status.equals("Connected")) {
				JsonObject pos = new JsonObject();
				pos.addProperty("type", "position");
				pos.addProperty("x", mc.player.getX());
				pos.addProperty("y", mc.player.getY());
				pos.addProperty("z", mc.player.getZ());
				pos.addProperty("yaw", mc.player.getYRot());
				pos.addProperty("pitch", mc.player.getXRot());
				pos.addProperty("dimension", mc.level.dimension().identifier().toString());
				String worldName = (mc.getSingleplayerServer() != null) ? mc.getSingleplayerServer().getWorldData().getLevelName() : "MpServer";
				pos.addProperty("world_name", ip + "///" + worldName);
				
				SlabSocketClient.send(pos.toString());
			}
		}
	}
	
	private void sendServerChange() {
		if (SlabSocketClient.status.equals("Connected")) {
			JsonObject change = new JsonObject();
			change.addProperty("type", "server_change");
			change.addProperty("server_ip", ip);
			SlabSocketClient.send(change.toString());
		}
	}
}
