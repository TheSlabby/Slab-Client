package dev.slabstudios.slabclient;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public class Button {

	public int x, y, sx, sy, color;
	public String text;
	public String action = "close"; // close is default action

	public Button(String text, int x, int y, int sx, int sy, int color) {
		this.x = x;
		this.y = y;
		this.sx = sx;
		this.sy = sy;
		this.color = color;
		this.text = text;
	}

	public void render(GuiScreen screen, Minecraft mc, int mx, int my) {
		int finalColor = this.color;
		if (isHovering(mx, my)) {
			finalColor = finalColor + 0xFF000000;
		} else {
			finalColor = finalColor + 0x7F000000;
		}

		Gui.drawRect(x, y, x+sx, y+sy, finalColor);
		
		// render text
		GL11.glPushMatrix();
		float scale = (float) (sy * .04);
		GL11.glScalef(scale, scale, scale);
		screen.drawCenteredString(mc.fontRendererObj, text, (int) ((x + sx / 2) / scale),
				(int) ((y + sy / 2 - (4 * scale)) / scale), 0xFFFFFF);
		GL11.glPopMatrix();

	}

	public boolean isHovering(int x, int y) {
		if (x > this.x && x < this.sx + this.x && y > this.y && y < this.sy + this.y)
			return true;
		return false;
	}

	public void checkClicked(int x, int y) {
		if (isHovering(x, y)) {
			if (action.equals("close")) {
				Minecraft.getMinecraft().displayGuiScreen((GuiScreen) null);
			}
		}
	}

}
