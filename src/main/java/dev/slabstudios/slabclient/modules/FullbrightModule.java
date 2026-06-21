package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;

public class FullbrightModule extends Module {

	private float oldGamma = 1.0F;

	public FullbrightModule(int x, int y) {
		super(x, y);
		this.key = "Fullbright";
		this.value = "Off";
		this.enabled = false;
		this.visible = true;
	}

	@Override
	public void update() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc != null && mc.gameSettings != null) {
			if (enabled) {
				if (mc.gameSettings.gammaSetting < 10.0F) {
					oldGamma = mc.gameSettings.gammaSetting;
					mc.gameSettings.gammaSetting = 100.0F;
				}
				this.value = "On";
			} else {
				if (mc.gameSettings.gammaSetting > 10.0F) {
					mc.gameSettings.gammaSetting = oldGamma;
				}
				this.value = "Off";
			}
		}
	}

}
