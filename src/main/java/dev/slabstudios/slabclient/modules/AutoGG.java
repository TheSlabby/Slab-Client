package dev.slabstudios.slabclient.modules;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import dev.slabstudios.slabclient.ConnectionHandler;
import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoGG extends Module {

	private long cooldownTime = 10000; // 10 second cooldown
	private long cooldown = 0;

	public static String endGameMSG = "gg";
	private ArrayList<String> triggers;

	public AutoGG(int x, int y) {
		super(x, y);
		this.visible = false; // it works in the background lmao
		this.key = "AutoGG";
		this.value = "Enabled";
		
		
		MinecraftForge.EVENT_BUS.register(this);

		// get triggers (copied from sk1er lmao)
		try {
			final String rawTriggers = IOUtils.toString(new URL(
					"https://gist.githubusercontent.com/minemanpi/72c38b0023f5062a5f3eba02a5132603/raw/triggers.txt"));
			triggers = new ArrayList<String>(Arrays.asList(rawTriggers.split("\n")));
		} catch (Exception e) {
			System.out.println("Couldn't get triggers :(");
		}
	}

	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		if (ConnectionHandler.ip.equalsIgnoreCase("mc.hypixel.net") && enabled && (cooldown == 0 || cooldown + cooldownTime < Minecraft.getSystemTime())) {

			String msg = event.message.getUnformattedText().trim();
			System.out.println(msg);
			for (String trigger : triggers) {
				if (msg.startsWith(trigger.trim())) {
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/achat " + endGameMSG);
					cooldown = Minecraft.getSystemTime();
					break;
				}
			}
		}
	}
	
	@Override
	public void render(boolean forced) {
		if(forced) {
			super.render(true);
		}
	}

}
