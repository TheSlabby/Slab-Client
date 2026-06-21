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
	private Button resetButton;
	private long lastResetClickTime = 0;

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
		buttons.clear();
		buttons.add(new Button("Close", getWidth() / 2 - 100, getHeight() - 30, 95, 20, 0xFF0000));

		resetButton = new Button("Reset Layout", getWidth() / 2 + 5, getHeight() - 30, 95, 20, 0x0000FF);
		resetButton.onClick = () -> {
			long now = Minecraft.getSystemTime();
			if (now - lastResetClickTime < 3000) {
				// Reset all layout positions to default
				for (Module module : RenderGuiHandler.modules) {
					if (module.key.equals("Keystrokes")) {
						module.x = getWidth() - 70;
						module.y = 5;
					} else {
						module.x = 5;
						if (module.key.equals("FPS")) module.y = 5;
						else if (module.key.equals("Coordinates")) module.y = 15;
						else if (module.key.equals("Server IP")) module.y = 25;
						else if (module.key.equals("Ping")) module.y = 35;
						else if (module.key.equals("Time")) module.y = 45;
						else if (module.key.equals("Autosprint")) module.y = 55;
						else if (module.key.equals("Fullbright")) module.y = 65;
						else if (module.key.equals("Damage Indicators")) module.y = 75;
						else if (module.key.equals("Reach")) module.y = 85;
						else if (module.key.equals("Combo")) module.y = 95;
						else if (module.key.equals("Block Overlay")) module.y = 105;
						else if (module.key.equals("Armor Status")) module.y = 115;
						else if (module.key.equals("Potion Status")) module.y = 210;
					}
				}
				ConfigManager.save();
				resetButton.text = "Layout Reset!";
				lastResetClickTime = 0;
			} else {
				resetButton.text = "Confirm?";
				lastResetClickTime = now;
			}
		};
		buttons.add(resetButton);
	}

	private HashMap<Long, Module> clicks = new HashMap<Long, Module>(); // time, module clicked

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Reset layout button confirmation timeout
		if (lastResetClickTime > 0 && Minecraft.getSystemTime() - lastResetClickTime > 3000) {
			resetButton.text = "Reset Layout";
			lastResetClickTime = 0;
		}

		// check if any modules have been double clicked
		clicks.keySet().removeIf(time -> time + 500 < Minecraft.getSystemTime());

		HashMap<Module, Integer> clickMap = new HashMap<Module, Integer>();
		for (Entry<Long, Module> entry : clicks.entrySet()) {
			int count = clickMap.getOrDefault(entry.getValue(), 0) + 1;
			clickMap.put(entry.getValue(), count);
			if (count >= 2) {
				System.out.println("Double click!");
				clicks.clear();
				entry.getValue().toggleEnabled();
				break;
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

		// Draw instructions for the player
		this.drawCenteredString(mc.fontRendererObj,
				EnumChatFormatting.GRAY + "Double-click a module to toggle it. Drag to rearrange.",
				getWidth() / 2, getHeight() / 2 + 25, 0xFFFFFF);

		this.drawCenteredString(mc.fontRendererObj,
				EnumChatFormatting.YELLOW + "Custom Commands: " + EnumChatFormatting.WHITE + "/setgg <message>",
				getWidth() / 2, getHeight() / 2 + 40, 0xFFFFFF);

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
			ConfigManager.save();
		}
		dragging = null;
	}

}
