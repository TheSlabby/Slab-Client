package dev.slabstudios.slabclient.modules;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class PotionStatus extends Module {

	public PotionStatus(int x, int y) {
		super(x, y);
		this.key = "Potion Status";
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, boolean force) {
		if (force || this.visible) {
			if (force) {
				super.render(guiGraphics, true);
			}
			
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			Collection<MobEffectInstance> effects = mc.player.getActiveEffects();

			int i = 0;
			for (MobEffectInstance effect : effects) {
				i++;

				// offset the active effect display downwards
				int yOffset = this.y + i * 20;

				MobEffect potion = effect.getEffect().value();
				String potionDisplayText = potion.getDisplayName().getString() + " "
						+ (effect.getAmplifier() + 1);
				
				int duration = effect.getDuration() / 20;
				long minutes = TimeUnit.SECONDS.toMinutes(duration);
				duration -= TimeUnit.MINUTES.toSeconds(minutes);
				String seconds = String.format("%02d", duration);
				String durationText = minutes + ":" + seconds;
				
				// Draw effect name with its color and the duration in gray
				int color = 0xFF000000 | potion.getColor();
				guiGraphics.text(mc.font, potionDisplayText, x, yOffset, color, true);
				guiGraphics.text(mc.font, durationText, x, yOffset + 9, 0xFFAAAAAA, true);
			}
		}
	}

}
