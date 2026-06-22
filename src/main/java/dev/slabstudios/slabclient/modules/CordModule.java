package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class CordModule extends Module {

	public CordModule(int x, int y) {
		super(x, y);
		
		this.key = "XYZ";
		this.value = "0, 0, 0";
	}

	@Override
	public void update() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			BlockPos coords = mc.player.blockPosition();
			this.value = coords.getX() + ", " + coords.getY() + ", " + coords.getZ();
		}
	}

}
