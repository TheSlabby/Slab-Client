package dev.slabstudios.slabclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;

public class GuiMenu extends Screen {

	private final Minecraft mc = Minecraft.getInstance();
	private Module dragging;
	private int dragInitX;
	private int dragInitY;
	private int mouseInitX;
	private int mouseInitY;

	private List<Button> buttons = new ArrayList<Button>();
	private Button resetButton;
	private long lastResetClickTime = 0;
	private EditBox serverAddressField;
	private int scrollOffset = 0;
	private int lastMouseX = 0;
	private int lastMouseY = 0;

	public GuiMenu() {
		super(Component.literal("Slab Client Menu"));
	}

	@Override
	protected void init() {
		buttons.clear();
		buttons.add(new Button("Close", width / 2 - 100, height - 30, 95, 20, 0xFF0000));

		resetButton = new Button("Reset Layout", width / 2 + 5, height - 30, 95, 20, 0x0000FF);
		resetButton.onClick = () -> {
			long now = System.currentTimeMillis();
			if (now - lastResetClickTime < 3000) {
				// Reset all layout positions to default
				for (Module module : RenderGuiHandler.modules) {
					if (module.key.equals("Keystrokes")) {
						module.x = width - 70;
						module.y = 5;
					} else if (module.key.equals("Radar")) {
						module.x = width / 2 - 120;
						module.y = 10;
					} else {
						module.x = 5;
						if (module.key.equals("FPS")) module.y = 5;
						else if (module.key.equals("Coordinates")) module.y = 15;
						else if (module.key.equals("Server IP")) module.y = 25;
						else if (module.key.equals("Ping")) module.y = 35;
						else if (module.key.equals("Time")) module.y = 45;
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

		// Initialize socket server input field
		serverAddressField = new EditBox(mc.font, width / 2 - 120, height - 60, 150, 20, Component.literal("Server Address"));
		serverAddressField.setMaxLength(128);
		serverAddressField.setValue(SlabSocketClient.serverAddress);
		serverAddressField.setResponder(text -> SlabSocketClient.serverAddress = text);
		serverAddressField.setTextColor(0xFFFFFFFF);
		serverAddressField.setTextColorUneditable(0xFFE0E0E0);
		this.addRenderableWidget(serverAddressField);

		// Initialize connection button
		String btnText = SlabSocketClient.status.equals("Connected") ? "Disconnect" : "Connect";
		int btnColor = SlabSocketClient.status.equals("Connected") ? 0xFF0000 : 0x00FF00;
		Button connectButton = new Button(btnText, width / 2 + 40, height - 60, 80, 20, btnColor);
		connectButton.onClick = () -> {
			if (SlabSocketClient.status.equals("Connected")) {
				SlabSocketClient.disconnect();
			} else {
				SlabSocketClient.serverAddress = serverAddressField.getValue();
				ConfigManager.save();
				SlabSocketClient.connect(SlabSocketClient.serverAddress);
			}
			init();
		};
		buttons.add(connectButton);
	}

	private HashMap<Long, Module> clicks = new HashMap<Long, Module>(); // time, module clicked

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {

		// Reset layout button confirmation timeout
		long now = System.currentTimeMillis();
		if (lastResetClickTime > 0 && now - lastResetClickTime > 3000) {
			resetButton.text = "Reset Layout";
			lastResetClickTime = 0;
		}

		// check if any modules have been double clicked
		clicks.keySet().removeIf(time -> time + 500 < now);

		HashMap<Module, Integer> clickMap = new HashMap<Module, Integer>();
		for (Entry<Long, Module> entry : clicks.entrySet()) {
			int count = clickMap.getOrDefault(entry.getValue(), 0) + 1;
			clickMap.put(entry.getValue(), count);
			if (count >= 2) {
				clicks.clear();
				entry.getValue().toggleEnabled();
				break;
			}
		}

		// render slab client logo
		guiGraphics.fill(0, 0, width, height, 0x7F000000);

		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;

		int panelWidth = 140;
		int panelHeight = 120;
		int panelX = width - panelWidth - 10;
		int panelY = height - panelHeight - 10;

		// Build connected players list
		List<String> players = new ArrayList<String>();
		if (SlabSocketClient.status.equals("Connected")) {
			if (mc.player != null) {
				players.add(mc.getUser().getName() + " (You)");
			}
			for (SlabSocketClient.RemotePlayer rp : SlabSocketClient.remotePlayers.values()) {
				players.add(rp.username);
			}
		} else {
			players.add(ChatFormatting.RED + "Offline");
		}

		// Draw panel background (glassmorphism/sleek translucent black)
		guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);
		// Draw simple borders
		guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0x40FFFFFF);
		guiGraphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0x40FFFFFF);
		guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, 0x40FFFFFF);
		guiGraphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, 0x40FFFFFF);

		// Header text
		String headerText = ChatFormatting.AQUA + "Connected Players";
		guiGraphics.centeredText(mc.font, headerText, panelX + panelWidth / 2, panelY + 6, 0xFFFFFFFF);
		guiGraphics.fill(panelX + 4, panelY + 18, panelX + panelWidth - 4, panelY + 19, 0x30FFFFFF);

		int listY = panelY + 22;
		int listHeight = panelHeight - 26;
		int rowHeight = 14;
		int totalListHeight = players.size() * rowHeight;
		
		int maxScroll = Math.max(0, totalListHeight - listHeight);
		if (scrollOffset > maxScroll) {
			scrollOffset = maxScroll;
		}

		// Enable scissor clipping
		guiGraphics.enableScissor(panelX + 2, listY, panelX + panelWidth - 2, listY + listHeight);

		for (int i = 0; i < players.size(); i++) {
			String name = players.get(i);
			int itemY = listY + (i * rowHeight) - scrollOffset;
			
			int color = 0xFFFFFFFF;
			if (name.endsWith(" (You)")) {
				color = 0xFF55FF55; // Light green for local player
			}
			
			guiGraphics.text(mc.font, name, panelX + 8, itemY + 3, color, true);
		}

		// Disable scissor clipping
		guiGraphics.disableScissor();

		float scale = 4.0F;
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.centeredText(mc.font,
				ChatFormatting.AQUA + "" + ChatFormatting.BOLD + "Slab Client", (int) (width / 2 / scale),
				(int) ((height / 2 - (4 * scale)) / scale), 0xFFFFFFFF);
		guiGraphics.pose().popMatrix();

		// Draw instructions for the player
		guiGraphics.centeredText(mc.font,
				ChatFormatting.GRAY + "Double-click a module to toggle it. Drag to rearrange.",
				width / 2, height / 2 + 25, 0xFFFFFFFF);

		guiGraphics.centeredText(mc.font,
				ChatFormatting.YELLOW + "Custom Commands: " + ChatFormatting.WHITE + "/setgg <message>",
				width / 2, height / 2 + 40, 0xFFFFFFFF);

		// Draw label for the text box
		guiGraphics.text(mc.font, "Slab Server Address:", width / 2 - 120, height - 72, 0xFFFFFFFF, true);
		
		// Draw status text
		String statusText = "Status: " + SlabSocketClient.status;
		int statusColor = 0xFFAAAAAA; // Gray
		if (SlabSocketClient.status.equals("Connected")) {
			statusColor = 0xFF55FF55; // Green
			String totalClientsText = "Connected Clients: " + (SlabSocketClient.remotePlayers.size() + 1);
			guiGraphics.text(mc.font, totalClientsText, width / 2 + 40, height - 84, 0xFF55FFFF, true);
		} else if (SlabSocketClient.status.equals("Connecting...")) {
			statusColor = 0xFFFFFF55; // Yellow
		} else if (SlabSocketClient.status.equals("Failed")) {
			statusColor = 0xFFFF5555; // Red
		}
		guiGraphics.text(mc.font, statusText, width / 2 + 40, height - 72, statusColor, true);

		for (Button button : buttons) {
			if (button.text.equals("Connect") || button.text.equals("Disconnect") || button.text.equals("Connecting...") || button.text.equals("Connection Failed")) {
				if (SlabSocketClient.status.equals("Connected")) {
					button.text = "Disconnect";
					button.color = 0xFF0000;
				} else if (SlabSocketClient.status.equals("Connecting...")) {
					button.text = "Connecting...";
					button.color = 0xFFFF00;
				} else {
					button.text = "Connect";
					button.color = 0x00FF00;
				}
			}
			button.render(guiGraphics, mc, mouseX, mouseY);
		}

		// show boxes around modules
		for (Module module : RenderGuiHandler.modules) {
			int width = module.getWidth();
			int height = module.getHeight();

			int color = 0x7FFFFFFF;
			if (module.enabled == false)
				color = 0x7FFF7F7F;

			guiGraphics.fill(module.x - 1, module.y - 1, module.x + width + 1, module.y + height + 1, color);
			module.render(guiGraphics, true); // force render
		}

		if (dragging != null) {
			dragging.x = dragInitX + (mouseX - mouseInitX);
			dragging.y = dragInitY + (mouseY - mouseInitY);
		}

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double x = event.x();
		double y = event.y();
		int buttonID = event.button();
		boolean moduleClicked = false;
		
		for (Module module : RenderGuiHandler.modules) {
			if (x > module.x && x < module.getWidth() + module.x && y > module.y && y < module.getHeight() + module.y) {
				mouseInitX = (int) x;
				mouseInitY = (int) y;
				dragInitX = module.x;
				dragInitY = module.y;
				dragging = module;
				clicks.put(System.currentTimeMillis(), module); // so we can check for double click
				moduleClicked = true;
				break;
			}
		}
		
		if (!moduleClicked) {
			for (Button button : buttons) {
				button.checkClicked((int) x, (int) y);
			}
		}

		// Explicitly handle focus for serverAddressField when clicked
		boolean insideAddressField = x >= width / 2 - 120 && x <= width / 2 - 120 + 150 && y >= height - 60 && y <= height - 60 + 20;
		if (insideAddressField) {
			serverAddressField.setFocused(true);
			this.setFocused(serverAddressField);
		} else {
			serverAddressField.setFocused(false);
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (serverAddressField.isFocused() && serverAddressField.keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (serverAddressField.isFocused() && serverAddressField.charTyped(event)) {
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			ConfigManager.save();
		}
		dragging = null;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		int panelWidth = 140;
		int panelHeight = 120;
		int panelX = width - panelWidth - 10;
		int panelY = height - panelHeight - 10;
		
		if (mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight) {
			if (scrollY > 0) {
				scrollOffset -= 14;
			} else {
				scrollOffset += 14;
			}
			if (scrollOffset < 0) {
				scrollOffset = 0;
			}
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
}
