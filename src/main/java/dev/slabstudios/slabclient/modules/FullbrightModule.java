package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;

public class FullbrightModule extends Module {

	private double oldGamma = 1.0;

	public FullbrightModule(int x, int y) {
		super(x, y);
		this.key = "Fullbright";
		this.value = "Off";
		this.enabled = false;
		this.visible = true;
	}

	@Override
	public void update() {
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.player != null) {
			if (enabled) {
				if (!mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)) {
					mc.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
						net.minecraft.world.effect.MobEffects.NIGHT_VISION, 100000, 0, false, false
					));
				}
				this.value = "On";
			} else {
				if (mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)) {
					net.minecraft.world.effect.MobEffectInstance effect = mc.player.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
					if (effect != null && effect.getDuration() > 1000) {
						mc.player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
					}
				}
				this.value = "Off";
			}
		}
	}

}
