package dev.slabstudios.slabclient;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import dev.slabstudios.slabclient.modules.AutoGG;
import dev.slabstudios.slabclient.modules.AutoSprintModule;
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
import dev.slabstudios.slabclient.modules.ServerIPModule;
import dev.slabstudios.slabclient.modules.TimeModule;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class RenderGuiHandler {

	public static List<Module> modules = new ArrayList<Module>();

	public RenderGuiHandler() {
		//on instance
		new FPSModule(5, 5);
		new CordModule(5, 15);
		// new CPSModule(5, 25); // CPS is now rendered inside Keystrokes
		new ServerIPModule(5, 25);
		new PingModule(5, 35);
		new TimeModule(5, 45);
		new AutoSprintModule(5, 55);
		new FullbrightModule(5, 65);
		new DamageIndicatorsModule(5, 75);
		new ReachModule(5, 85);
		new ComboModule(5, 95);
		new BlockOverlayModule(5, 105);
		
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		new KeystrokesModule(sr.getScaledWidth() - 70, 5);
		
		new ArmorStatusModule(5, 115);
		
		//no interface
		//new AutoGG(5, 5);
		
		//big interface
		new PotionStatus(5, 210);
		
		
		// Load module configuration states and positions
		ConfigManager.load();
	}

	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Text event) {
		//render overlays
		for (Module module : modules) {
			module.render(false);
			module.update();
		}
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiMenu());
		}
	}

}
