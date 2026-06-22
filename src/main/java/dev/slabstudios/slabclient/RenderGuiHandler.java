package dev.slabstudios.slabclient;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.slabstudios.slabclient.modules.AutoGG;
import dev.slabstudios.slabclient.modules.CPSModule;
import dev.slabstudios.slabclient.modules.CordModule;
import dev.slabstudios.slabclient.modules.FPSModule;
import dev.slabstudios.slabclient.modules.FullbrightModule;
import dev.slabstudios.slabclient.modules.PotionStatus;
import dev.slabstudios.slabclient.modules.DamageIndicatorsModule;
import dev.slabstudios.slabclient.modules.ReachModule;
import dev.slabstudios.slabclient.modules.ComboModule;
import dev.slabstudios.slabclient.modules.KeystrokesModule;
import dev.slabstudios.slabclient.modules.ArmorStatusModule;
import dev.slabstudios.slabclient.modules.BlockOverlayModule;
import dev.slabstudios.slabclient.modules.PingModule;
import dev.slabstudios.slabclient.modules.RadarModule;
import dev.slabstudios.slabclient.modules.ServerIPModule;
import dev.slabstudios.slabclient.modules.TimeModule;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class RenderGuiHandler {

	public static List<Module> modules = new ArrayList<Module>();

	private boolean initialized = false;

	public RenderGuiHandler() {
		// on instance (lazy initialized to prevent NPE during mod bootstrap)
	}

	private synchronized void initModules() {
		if (initialized) return;
		
		new FPSModule(5, 5);
		new CordModule(5, 15);
		new ServerIPModule(5, 25);
		new PingModule(5, 35);
		new TimeModule(5, 45);
		new FullbrightModule(5, 65);
		new DamageIndicatorsModule(5, 75);
		new ReachModule(5, 85);
		new ComboModule(5, 95);
		new BlockOverlayModule(5, 105);
		
		Minecraft mc = Minecraft.getInstance();
		int width = mc.getWindow().getGuiScaledWidth();
		new KeystrokesModule(width - 70, 5);
		new RadarModule(width / 2 - 120, 10);
		
		new ArmorStatusModule(5, 115);
		new PotionStatus(5, 210);
		
		// Load module configuration states and positions
		ConfigManager.load();
		
		initialized = true;
	}

	@SubscribeEvent
	public void onRenderGui(RenderGuiEvent.Post event) {
		if (!initialized) {
			initModules();
		}
		//render overlays
		for (Module module : modules) {
			module.render(event.getGuiGraphics(), false);
			module.update();
		}
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.screen() == null && event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT && event.getAction() == GLFW.GLFW_PRESS) {
			mc.setScreenAndShow(new GuiMenu());
		}
	}

}
