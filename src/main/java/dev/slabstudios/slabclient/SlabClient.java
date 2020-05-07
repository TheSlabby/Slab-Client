package dev.slabstudios.slabclient;

import java.util.UUID;

import dev.slabstudios.slabclient.commands.SetAutoGGMSG;
import dev.slabstudios.slabclient.utils.CapesAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = SlabClient.MODID, version = SlabClient.VERSION)
public class SlabClient {
	public static final String MODID = "slabclient";
	public static final String VERSION = "1.0";

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// lets register events
		MinecraftForge.EVENT_BUS.register(new RenderGuiHandler());
		ClientCommandHandler.instance.registerCommand(new SetAutoGGMSG());
		
		MinecraftForge.EVENT_BUS.register(new ConnectionHandler());
		
		//capes
//		Minecraft mc = Minecraft.getMinecraft();
//		UUID id;
//		try{
//			id = EntityPlayer.getUUID(mc.thePlayer.getGameProfile());
//		}catch(Exception e) {
//			id = EntityPlayer.getOfflineUUID("TheSlab");
//			e.printStackTrace();
//		}
//		CapesAPI.loadCape(id);
//		System.out.println("has cape: "+CapesAPI.hasCape(id));
	}


}
