package dev.slabstudios.slabclient;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ConnectionHandler {

	public static String ip = "Disconnected";
	public static boolean remote = false;
	
	public ConnectionHandler() {
		System.out.println("Connection handler active!");
	}
	
	@SubscribeEvent
	public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		if(!event.isLocal) {
			ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
			remote = true;
		}else {
			remote = false;
			ip = "Disconnected";
		}
	}
	
}
