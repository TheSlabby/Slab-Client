package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

public class AutoSprintModule extends Module {

	public boolean toggled = false;
	
	public AutoSprintModule(int x, int y) {
		super(x, y);
		
		this.key = "Autosprint";
		this.value = "On";
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private boolean keyPressed = false;
	
	@Override
	public void update() {
		
		if (enabled) {
		
			Minecraft mc = Minecraft.getMinecraft();
			KeyBinding keybind = mc.gameSettings.keyBindSprint;
			
			//check if key is pressed
			if (!keyPressed && keybind.isPressed()) {
				keyPressed = true;
				toggled = !toggled;
				System.out.println("toggled sprint: " + toggled);
			} else keyPressed = false;
			
			KeyBinding.setKeyBindState(keybind.getKeyCode(), toggled);
		}
		
		if (toggled) this.value = "On";
		else if (!toggled) this.value = "Off";
		if (!this.enabled) this.value = "Disabled";
		
	}
	

}
