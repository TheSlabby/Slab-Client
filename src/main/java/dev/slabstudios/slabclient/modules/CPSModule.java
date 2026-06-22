package dev.slabstudios.slabclient.modules;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.slabstudios.slabclient.Module;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

public class CPSModule extends Module {

	public static List<Long> clicks = new ArrayList<Long>();
	
	public CPSModule(int x, int y) {
		super(x, y);
		this.key = "CPS";
		this.value = "0";
		
		NeoForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void update() {
		long runTime = System.currentTimeMillis();
		clicks.removeIf(time -> runTime > time + 1000);
		this.value = Integer.toString(clicks.size());
	}
	
	@SubscribeEvent
	public void onMouseEvent(InputEvent.MouseButton.Pre event) {
		if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
			clicks.add(System.currentTimeMillis());
		}
	}

}
