package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;

public class FPSModule extends Module{

	public FPSModule(int x, int y) {
		super(x, y);
		
		this.key = "FPS";
		this.value = "0";
	}

	
	@Override
	public void update() {
		int fps = Integer.valueOf(Minecraft.getDebugFPS());
		this.value = Integer.toString(fps);
	}

}
