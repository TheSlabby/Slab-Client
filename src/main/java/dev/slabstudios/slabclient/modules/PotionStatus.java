package dev.slabstudios.slabclient.modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionStatus extends Module {

	HashMap<String, String> potionNames = new HashMap<String, String>();

	public PotionStatus(int x, int y) {
		super(x, y);
		
		this.key = "Potion Status";

		Potion[] apotion = Potion.potionTypes;
		for (int j = 0; j < apotion.length; ++j) {
			Potion potion = apotion[j];
			if (potion != null) {
				System.out.println(potion.getName());
			}
		}

		// define potion names
		potionNames.put("potion.moveSpeed", "Speed");
		potionNames.put("potion.moveSlowdown", "Slowness");
		potionNames.put("potion.digSpeed", "Haste");
		potionNames.put("potion.digSlowDown", "Mining Fatigue");
		potionNames.put("potion.damageBoost", "Strength");
		potionNames.put("potion.heal", "Healing");
		potionNames.put("potion.harm", "Harm");
		potionNames.put("potion.jump", "Jump");
		potionNames.put("potion.confusion", "Confusion");
		potionNames.put("potion.regeneration", "Regeneration");
		potionNames.put("potion.resistance", "Resistance");
		potionNames.put("potion.fireResistance", "Fire Resistance");
		potionNames.put("potion.waterBreathing", "Water Breathing");
		potionNames.put("potion.invisibility", "Invisibility");
		potionNames.put("potion.blindness", "Blindness");
		potionNames.put("potion.nightVision", "Night Vision");
		potionNames.put("potion.hunger", "Hunger");
		potionNames.put("potion.weakness", "Weakness");
		potionNames.put("potion.poison", "Poison");
		potionNames.put("potion.wither", "Wither");
		potionNames.put("potion.healthBoost", "Health Boost");
		potionNames.put("potion.absorption", "Absorption");
		potionNames.put("potion.saturation", "Saturation");
	}

	@Override
	public void render(boolean forced) {
		if (forced || this.visible) {

			if(forced) {
				super.render(true);
			}
			
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP plr = mc.thePlayer;
			Collection<PotionEffect> effects = plr.getActivePotionEffects();

			int i = 0;
			for (PotionEffect effect : effects) {
				i++; //start at index 1

				int y = this.y + i * 20;

				Potion[] apotion = Potion.potionTypes;

				for (int j = 0; j < apotion.length; ++j) {
					Potion potion = apotion[j];

					if (potion != null && potion.getName().equals(effect.getEffectName())) {

						if (potion.hasStatusIcon()) {

							// render potion here
							int height = 30;

							String potionDisplayText = potionNames.get(potion.getName()) + " "
									+ (effect.getAmplifier() + 1);
							
							
							
							
							int duration = (int) effect.getDuration() / 20;
							long minutes = TimeUnit.SECONDS.toMinutes(duration);
							duration -= TimeUnit.MINUTES.toSeconds(minutes);
							String seconds = String.format("%02d", duration);
							
							//draw strings
							this.drawString(mc.fontRendererObj, potionDisplayText, x + 20, y - 12 + (height / 2),
									potion.getLiquidColor());
							this.drawString(mc.fontRendererObj, minutes + ":" + seconds, x + 20, y - 4 + (height / 2),
									potion.getLiquidColor());

							int potionStatusIcon = potion.getStatusIconIndex();
							mc.renderEngine.bindTexture(
									new ResourceLocation("minecraft", "textures/gui/container/inventory.png"));
							mc.ingameGUI.drawTexturedModalRect(x, y, potionStatusIcon % 8 * 18,
									166 + 32 + potionStatusIcon / 8 * 18, 18, 18);

						}
					}
				}

			}
		}
	}

}
