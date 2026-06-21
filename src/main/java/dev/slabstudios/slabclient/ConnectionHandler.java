package dev.slabstudios.slabclient;
 
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.google.gson.JsonObject;
 
public class ConnectionHandler {
 
	public static String ip = "Disconnected";
	public static boolean remote = false;
	private int ticks = 0;
	
	public ConnectionHandler() {
		System.out.println("Connection handler active!");
	}
	
	@SubscribeEvent
	public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		if(!event.isLocal && Minecraft.getMinecraft().getCurrentServerData() != null) {
			ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
			remote = true;
		}else {
			remote = false;
			ip = "Disconnected";
		}
		
		// Notify C++ backend of server change
		sendServerChange();
	}

	@SubscribeEvent
	public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		ip = "Disconnected";
		remote = false;
		
		// Notify C++ backend of server change
		sendServerChange();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			ticks++;
			if (ticks % 20 == 0) { // Every 20 ticks (1 second)
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.thePlayer != null && mc.theWorld != null && SlabSocketClient.status.equals("Connected")) {
					JsonObject pos = new JsonObject();
					pos.addProperty("type", "position");
					pos.addProperty("x", mc.thePlayer.posX);
					pos.addProperty("y", mc.thePlayer.posY);
					pos.addProperty("z", mc.thePlayer.posZ);
					pos.addProperty("yaw", mc.thePlayer.rotationYaw);
					pos.addProperty("pitch", mc.thePlayer.rotationPitch);
					pos.addProperty("dimension", mc.thePlayer.dimension);
					pos.addProperty("world_name", mc.theWorld.getWorldInfo().getWorldName());
					
					SlabSocketClient.send(pos.toString());
				}
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
