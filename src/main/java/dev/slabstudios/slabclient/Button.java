package dev.slabstudios.slabclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Button {

	public int x, y, sx, sy, color;
	public String text;
	public String action = "close"; // close is default action
	public Runnable onClick;

	public Button(String text, int x, int y, int sx, int sy, int color) {
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
		this.color = color;
		this.text = text;
	}

	public void render(GuiGraphicsExtractor guiGraphics, Minecraft mc, int mx, int my) {
		int finalColor = this.color;
		if (isHovering(mx, my)) {
			finalColor = finalColor + 0xFF000000;
		} else {
			finalColor = finalColor + 0x7F000000;
		}

		guiGraphics.fill(x, y, x + sx, y + sy, finalColor);
		
		// render text
		float scale = (float) (sy * .04);
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.centeredText(mc.font, text, (int) ((x + sx / 2) / scale),
				(int) ((y + sy / 2 - (4 * scale)) / scale), 0xFFFFFFFF);
		guiGraphics.pose().popMatrix();
	}

	public boolean isHovering(int x, int y) {
		if (x > this.x && x < this.sx + this.x && y > this.y && y < this.sy + this.y)
			return true;
		return false;
	}

	public void checkClicked(int x, int y) {
		if (isHovering(x, y)) {
			if (onClick != null) {
				onClick.run();
			} else if (action.equals("close")) {
				Minecraft.getInstance().setScreenAndShow((net.minecraft.client.gui.screens.Screen) null);
			}
		}
	}

}
