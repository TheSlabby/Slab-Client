package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

public class ReachModule extends Module {

	private double lastReach = 0.0;
	private long lastHitTime = 0;

	public ReachModule(int x, int y) {
		super(x, y);
		this.key = "Reach";
		this.value = "0.00m";
		this.visible = true;
		
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void update() {
		if (!enabled) {
			this.value = "Disabled";
			return;
		}

		// Reset reach display to 0.00m after 2 seconds of inactivity
		if (lastReach > 0.0 && (System.currentTimeMillis() - lastHitTime > 2000)) {
			lastReach = 0.0;
		}

		this.value = String.format("%.2fm", lastReach);
	}

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		if (event.getEntity() == null || !event.getEntity().level().isClientSide()) return;
		if (!enabled) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		if (event.getEntity() == mc.player && event.getTarget() instanceof LivingEntity) {
			LivingEntity target = (LivingEntity) event.getTarget();
			
			// Compute eye-to-eye reach distance
			Vec3 playerEyes = mc.player.getEyePosition(1.0F);
			Vec3 targetEyes = target.getEyePosition(1.0F);
			
			this.lastReach = playerEyes.distanceTo(targetEyes);
			this.lastHitTime = System.currentTimeMillis();
		}
	}

}
