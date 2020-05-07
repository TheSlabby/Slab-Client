package dev.slabstudios.slabclient;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import dev.slabstudios.slabclient.modules.AutoGG;
import dev.slabstudios.slabclient.modules.AutoSprintModule;
import dev.slabstudios.slabclient.modules.CPSModule;
import dev.slabstudios.slabclient.modules.CordModule;
import dev.slabstudios.slabclient.modules.FPSModule;
import dev.slabstudios.slabclient.modules.PotionStatus;
import dev.slabstudios.slabclient.modules.ServerIPModule;
import dev.slabstudios.slabclient.modules.TimeModule;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class RenderGuiHandler {

	public static List<Module> modules = new ArrayList<Module>();

	public RenderGuiHandler() {
		//on instance
		new FPSModule(5,5 + (modules.size()) * 10);
		new CordModule(5,5 + (modules.size()) * 10);
		new CPSModule(5,5 + (modules.size()) * 10); //set init positions lmao
		new ServerIPModule(5,5 + (modules.size()) * 10);
		new TimeModule(5,5 + (modules.size()) * 10);
		new AutoSprintModule(5,5 + (modules.size()) * 10);
		
		//no interface
		new AutoGG(5,5 + (modules.size()) * 10);
		
		//big interface
		new PotionStatus(5,5 + (modules.size()) * 10);
		
		
		//new BlockOverlayModule();
		
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
