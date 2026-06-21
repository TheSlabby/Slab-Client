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

		// Initialize with default fallback Hypixel triggers to prevent crashes and handle 404s
		this.triggers = new ArrayList<String>(Arrays.asList(
			"1st Killer - ",
			"1st Place - ",
			"Winner: ",
			"WINNER!",
			"Won the game!",
			"Top Players:",
			"Round Over!",
			"Winner - ",
			"Game over!",
			"Winner "
		));

		// Get triggers asynchronously so it doesn't freeze the client on startup
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final String rawTriggers = IOUtils.toString(new URL(
							"https://gist.githubusercontent.com/minemanpi/72c38b0023f5062a5f3eba02a5132603/raw/triggers.txt"));
					ArrayList<String> loadedTriggers = new ArrayList<String>(Arrays.asList(rawTriggers.split("\n")));
					if (!loadedTriggers.isEmpty()) {
						triggers = loadedTriggers;
					}
				} catch (Exception e) {
					System.out.println("Couldn't get triggers from URL (using fallback triggers): " + e.getMessage());
				}
			}
		}).start();
	}

	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		// event.type: 0 is chat, 1 is system message, 2 is action bar. 
		// We only want system messages (type != 0) to prevent players from spoofing endgame messages in chat.
		if (event.type != 0 && ConnectionHandler.ip.equalsIgnoreCase("mc.hypixel.net") && enabled && (cooldown == 0 || cooldown + cooldownTime < Minecraft.getSystemTime())) {

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
