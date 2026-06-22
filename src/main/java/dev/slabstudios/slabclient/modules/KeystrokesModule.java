package dev.slabstudios.slabclient.modules;

import java.util.ArrayList;
import java.util.List;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class KeystrokesModule extends Module {

	private final List<Long> lmbClicks = new ArrayList<Long>();
	private final List<Long> rmbClicks = new ArrayList<Long>();
	private boolean wasLmbDown = false;
	private boolean wasRmbDown = false;

	public KeystrokesModule(int x, int y) {
		super(x, y);
		this.key = "Keystrokes";
		this.value = "Enabled";
		this.visible = true;
	}

	@Override
	public void update() {
		if (enabled) {
			this.value = "Enabled";
		} else {
			this.value = "Disabled";
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		
		// Click tracking for CPS
		boolean lmbDown = mc.mouseHandler.isLeftPressed();
		if (lmbDown && !wasLmbDown) {
			lmbClicks.add(System.currentTimeMillis());
		}
		wasLmbDown = lmbDown;

		boolean rmbDown = mc.mouseHandler.isRightPressed();
		if (rmbDown && !wasRmbDown) {
			rmbClicks.add(System.currentTimeMillis());
		}
		wasRmbDown = rmbDown;

		long now = System.currentTimeMillis();
		lmbClicks.removeIf(time -> now - time > 1000);
		rmbClicks.removeIf(time -> now - time > 1000);
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, boolean force) {
		if (!visible && !force) return;

		Minecraft mc = Minecraft.getInstance();
		Font fr = mc.font;

		// Key states
		boolean w = mc.options.keyUp.isDown();
		boolean a = mc.options.keyLeft.isDown();
		boolean s = mc.options.keyDown.isDown();
		boolean d = mc.options.keyRight.isDown();
		boolean lmb = mc.mouseHandler.isLeftPressed();
		boolean rmb = mc.mouseHandler.isRightPressed();

		// Render grid
		drawKey(guiGraphics, fr, "W", x + 22, y, 20, 20, w);
		drawKey(guiGraphics, fr, "A", x, y + 22, 20, 20, a);
		drawKey(guiGraphics, fr, "S", x + 22, y + 22, 20, 20, s);
		drawKey(guiGraphics, fr, "D", x + 44, y + 22, 20, 20, d);

		// Mouse keys with CPS
		drawMouseKey(guiGraphics, fr, "LMB", lmbClicks.size(), x, y + 44, 31, 20, lmb);
		drawMouseKey(guiGraphics, fr, "RMB", rmbClicks.size(), x + 33, y + 44, 31, 20, rmb);
	}

	private void drawKey(GuiGraphicsExtractor guiGraphics, Font fr, String label, int kx, int ky, int width, int height, boolean pressed) {
		int bgColor = pressed ? 0xAAFFFFFF : 0x60000000;
		int textColor = pressed ? 0xFF000000 : 0xFFFFFFFF;

		guiGraphics.fill(kx, ky, kx + width, ky + height, bgColor);
		guiGraphics.centeredText(fr, label, kx + width / 2, ky + (height - 8) / 2, textColor);
	}

	private void drawMouseKey(GuiGraphicsExtractor guiGraphics, Font fr, String label, int cps, int kx, int ky, int width, int height, boolean pressed) {
		int bgColor = pressed ? 0xAAFFFFFF : 0x60000000;
		int textColor = pressed ? 0xFF000000 : 0xFFFFFFFF;
		int cpsColor = pressed ? 0xFF555555 : 0xFFAAAAAA;

		guiGraphics.fill(kx, ky, kx + width, ky + height, bgColor);
		
		// Draw label
		guiGraphics.centeredText(fr, label, kx + width / 2, ky + 2, textColor);
		
		// Draw CPS smaller below
		String cpsText = cps + " CPS";
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(kx + width / 2.0F, ky + 11.0F);
		guiGraphics.pose().scale(0.7F, 0.7F);
		guiGraphics.centeredText(fr, cpsText, 0, 0, cpsColor);
		guiGraphics.pose().popMatrix();
	}

	@Override
	public int getWidth() {
		return 64;
	}

	@Override
	public int getHeight() {
		return 64;
	}

}
