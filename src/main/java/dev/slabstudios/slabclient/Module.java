package dev.slabstudios.slabclient;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Module {

	public String key = "KEY";
	public String value = "VALUE";
	public boolean visible = true;
	public boolean enabled = true;
	public int x, y;

	public Module(int x, int y) {
		RenderGuiHandler.modules.add(this);
		this.x = x;
		this.y = y;
	}

	public void update() {
		if (this.enabled) {
			this.value = "Enabled";
		} else this.value = "Disabled";
	}

	public String getRenderText() {
		return ChatFormatting.GRAY + "[" + this.key + "] " + ChatFormatting.AQUA + this.value + ChatFormatting.RESET;
	}
	
	//default toggle enable function thing 
	public void toggleEnabled() {
		this.enabled = !this.enabled;
		this.visible = this.enabled;
		ConfigManager.save();
	}
	
	public void render(GuiGraphicsExtractor guiGraphics, boolean force) {
		if (visible || force) {
			guiGraphics.text(Minecraft.getInstance().font,
					getRenderText(), x, y, 0xFFFFFFFF, true);
		}
	}
	
	public int getWidth() {
		return Minecraft.getInstance().font.width(getRenderText());
	}
	public int getHeight() {
		return 8; //default is 8 unless this bad boy is scaled
	}

}