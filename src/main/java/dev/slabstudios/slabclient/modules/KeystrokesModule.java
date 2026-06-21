package dev.slabstudios.slabclient.modules;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

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

		// Click tracking for CPS
		boolean lmbDown = Mouse.isButtonDown(0);
		if (lmbDown && !wasLmbDown) {
			lmbClicks.add(System.currentTimeMillis());
		}
		wasLmbDown = lmbDown;

		boolean rmbDown = Mouse.isButtonDown(1);
		if (rmbDown && !wasRmbDown) {
			rmbClicks.add(System.currentTimeMillis());
		}
		wasRmbDown = rmbDown;

		long now = System.currentTimeMillis();
		lmbClicks.removeIf(time -> now - time > 1000);
		rmbClicks.removeIf(time -> now - time > 1000);
	}

	@Override
	public void render(boolean force) {
		if (!visible && !force) return;

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fr = mc.fontRendererObj;

		// Key states
		boolean w = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
		boolean a = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
		boolean s = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
		boolean d = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
		boolean lmb = Mouse.isButtonDown(0);
		boolean rmb = Mouse.isButtonDown(1);

		// Render grid
		drawKey(fr, "W", x + 22, y, 20, 20, w);
		drawKey(fr, "A", x, y + 22, 20, 20, a);
		drawKey(fr, "S", x + 22, y + 22, 20, 20, s);
		drawKey(fr, "D", x + 44, y + 22, 20, 20, d);

		// Mouse keys with CPS
		drawMouseKey(fr, "LMB", lmbClicks.size(), x, y + 44, 31, 20, lmb);
		drawMouseKey(fr, "RMB", rmbClicks.size(), x + 33, y + 44, 31, 20, rmb);
	}

	private void drawKey(FontRenderer fr, String label, int kx, int ky, int width, int height, boolean pressed) {
		int bgColor = pressed ? 0xAAFFFFFF : 0x60000000;
		int textColor = pressed ? 0xFF000000 : 0xFFFFFFFF;

		drawRect(kx, ky, kx + width, ky + height, bgColor);
		drawCenteredString(fr, label, kx + width / 2, ky + (height - 8) / 2, textColor);
	}

	private void drawMouseKey(FontRenderer fr, String label, int cps, int kx, int ky, int width, int height, boolean pressed) {
		int bgColor = pressed ? 0xAAFFFFFF : 0x60000000;
		int textColor = pressed ? 0xFF000000 : 0xFFFFFFFF;
		int cpsColor = pressed ? 0xFF555555 : 0xFFAAAAAA;

		drawRect(kx, ky, kx + width, ky + height, bgColor);
		
		// Draw label
		drawCenteredString(fr, label, kx + width / 2, ky + 2, textColor);
		
		// Draw CPS smaller below
		String cpsText = cps + " CPS";
		net.minecraft.client.renderer.GlStateManager.pushMatrix();
		net.minecraft.client.renderer.GlStateManager.translate(kx + width / 2.0F, ky + 11.0F, 0.0F);
		net.minecraft.client.renderer.GlStateManager.scale(0.7F, 0.7F, 1.0F);
		drawCenteredString(fr, cpsText, 0, 0, cpsColor);
		net.minecraft.client.renderer.GlStateManager.popMatrix();
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
