package dev.slabstudios.slabclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumChatFormatting;

/*
 * This is the module class for Slab Client! This is where the magic happens.
 */

public class Module extends Gui {

	public String key = "KEY";
	public String value = "VALUE";
	public boolean visible = true;
	public boolean enabled = true;
	public int x, y;
	String text = "";

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
		return EnumChatFormatting.GRAY + "[" + this.key + "] " + EnumChatFormatting.AQUA + this.value + EnumChatFormatting.RESET;
	}
	
	//default toggle enable function thing 
	public void toggleEnabled() {
		this.enabled = !this.enabled;
		this.visible = this.enabled;
	}
	
	public void render(boolean force) {
		if(visible || force)
			this.drawString(Minecraft.getMinecraft().fontRendererObj,
					getRenderText(), x, y, 0xFFFFFF);
	}
	
	public int getWidth() {
		return Minecraft.getMinecraft().fontRendererObj.getStringWidth(getRenderText());
	}
	public int getHeight() {
		return 8; //default is 8 unless this bad boy is scaled
	}

}