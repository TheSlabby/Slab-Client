package dev.slabstudios.slabclient.modules;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import dev.slabstudios.slabclient.ConnectionHandler;
import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

public class AutoGG extends Module {

	private long cooldownTime = 10000; // 10 second cooldown
	private long cooldown = 0;

	public static String endGameMSG = "gg";
	private ArrayList<String> triggers;

	public AutoGG(int x, int y) {
		super(x, y);
		this.visible = false; // it works in the background
		this.key = "AutoGG";
		this.value = "Enabled";
		
		NeoForge.EVENT_BUS.register(this);

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
							"https://gist.githubusercontent.com/minemanpi/72c38b0023f5062a5f3eba02a5132603/raw/triggers.txt"), "UTF-8");
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
		// Only want system/server messages to prevent players from spoofing endgame messages in chat.
		if (event.isSystem() && ConnectionHandler.ip.equalsIgnoreCase("mc.hypixel.net") && enabled) {
			long now = System.currentTimeMillis();
			if (cooldown == 0 || cooldown + cooldownTime < now) {
				String msg = event.getMessage().getString().trim();
				System.out.println(msg);
				for (String trigger : triggers) {
					if (msg.startsWith(trigger.trim())) {
						Minecraft mc = Minecraft.getInstance();
						if (mc.player != null && mc.player.connection != null) {
							mc.player.connection.sendChat("/achat " + endGameMSG);
							cooldown = now;
						}
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void render(GuiGraphicsExtractor guiGraphics, boolean forced) {
		if (forced) {
			super.render(guiGraphics, true);
		}
	}

}
