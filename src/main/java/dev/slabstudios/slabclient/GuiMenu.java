package dev.slabstudios.slabclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import scala.Int;

public class GuiMenu extends GuiScreen {

	private Module dragging;
	private int dragInitX;
	private int dragInitY;
	private int mouseInitX;
	private int mouseInitY;
	public static boolean active;

	private List<Button> buttons = new ArrayList<Button>();

	public static int getWidth() {
		Minecraft mc = Minecraft.getMinecraft();
		return new ScaledResolution(mc).getScaledWidth();
	}

	public static int getHeight() {
		Minecraft mc = Minecraft.getMinecraft();
		return new ScaledResolution(mc).getScaledHeight();
	}

	@Override
	public void initGui() {
		buttons.add(new Button("Close", (int) (getWidth() - getWidth() * .15), 0, (int) (getWidth() * .15),
				(int) (getHeight() * .1), 0xFF0000));
	}

	private HashMap<Long, Module> clicks = new HashMap<Long, Module>(); // time, module clicked

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// check if any modules have been double clicked (probably the shittiest code
		// ive ever written lol)
		HashMap<Module, Integer> clickMap = new HashMap<Module, Integer>();

		for (Entry<Long, Module> entry : clicks.entrySet()) {
			if (entry.getKey() + 500 < Minecraft.getSystemTime()) {
				clicks.remove(entry.getKey());
			}
			if (clickMap.get(entry.getValue()) == null) {
				clickMap.put(entry.getValue(), 1);
			} else {
				clickMap.put(entry.getValue(), clickMap.get(entry.getValue()) + 1);
				if (clickMap.get(entry.getValue()) >= 2) {
					System.out.println("Double click!");
					clicks.clear();
					entry.getValue().toggleEnabled();
					break;
				}
			}
		}

		// render slab client logo
		Gui.drawRect(0, 0, getWidth(), getHeight(), 0x7F000000);

		GL11.glPushMatrix();
		float scale = 4;
		GL11.glScalef(scale, scale, scale);
		this.drawCenteredString(mc.fontRendererObj,
				EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "Slab Client", (int) (getWidth() / 2 / scale),
				(int) ((getHeight() / 2 - (4 * scale)) / scale), 0xFFFFFF);
		GL11.glPopMatrix();

		for (Button button : buttons) {
			button.render(this, mc, mouseX, mouseY);
		}

		// show boxes around modules
		for (Module module : RenderGuiHandler.modules) {
			int width = module.getWidth();
			int height = module.getHeight();

			int color = 0x7FFFFFFF;
			if (module.enabled == false)
				color = 0x7FFF7F7F;

			Gui.drawRect(module.x - 1, module.y - 1, module.x + width + 1, module.y + height + 1, color);
			module.render(true); // force render
		}

		if (dragging != null) {
			dragging.x = dragInitX + (mouseX - mouseInitX);
			dragging.y = dragInitY + (mouseY - mouseInitY);
		}
	}

	@Override
	public void mouseClicked(int x, int y, int buttonID) {
		System.out.println("Player clicked (" + x + "," + y + ")");

		
		boolean moduleClicked = false;
		
		for (Module module : RenderGuiHandler.modules) {
			if (x > module.x && x < module.getWidth() + module.x && y > module.y && y < module.getHeight() + module.y) {
				mouseInitX = x;
				mouseInitY = y;
				dragInitX = module.x;
				dragInitY = module.y;
				dragging = module;
				clicks.put(Minecraft.getSystemTime(), module); // so we can check for double click
				System.out.println("Now dragging!");
				moduleClicked = true;
				break;
			}
		}
		
		if(!moduleClicked) {
			for (Button button : buttons) {
				button.checkClicked(x, y);
			}
		}

	}

	@Override
	public void mouseReleased(int x, int y, int state) {
		if (dragging != null) {
			// snap dragged item back to grid
		}
		dragging = null;
	}

}
