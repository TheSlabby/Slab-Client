package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.HashMap;

public class ComboModule extends Module {

	private int comboCount = 0;
	private long lastHitTime = 0;

	private int lastAttackedEntityId = -1;
	private long lastAttackTime = 0;

	private float lastPlayerHealth = -1f;
	private final HashMap<Integer, Float> entityHealthMap = new HashMap<>();

	public ComboModule(int x, int y) {
		super(x, y);
		this.key = "Combo";
		this.value = "0";
		this.visible = true;
		
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void update() {
		if (!enabled) {
			this.value = "Disabled";
			return;
		}

		// Reset combo to 0 after 2 seconds of inactivity
		if (comboCount > 0 && (System.currentTimeMillis() - lastHitTime > 2000)) {
			comboCount = 0;
		}

		this.value = String.valueOf(comboCount);
	}

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		if (event.getEntity() == null || !event.getEntity().level().isClientSide()) return;
		if (!enabled) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		if (event.getEntity() == mc.player && event.getTarget() instanceof LivingEntity target) {
			this.lastAttackedEntityId = target.getId();
			this.lastAttackTime = System.currentTimeMillis();
		}
	}

	@SubscribeEvent
	public void onEntityTick(EntityTickEvent.Post event) {
		if (!enabled) return;

		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity living)) return;
		if (!living.level().isClientSide()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		if (living == mc.player) {
			float health = mc.player.getHealth();
			if (lastPlayerHealth != -1f && health < lastPlayerHealth) {
				// Reset combo if the player takes damage
				comboCount = 0;
			}
			lastPlayerHealth = health;
			return;
		}

		int id = living.getId();
		float health = living.getHealth();

		if (entityHealthMap.containsKey(id)) {
			float oldHealth = entityHealthMap.get(id);
			if (health < oldHealth) {
				if (id == lastAttackedEntityId && (System.currentTimeMillis() - lastAttackTime < 500)) {
					comboCount++;
					lastHitTime = System.currentTimeMillis();
					lastAttackedEntityId = -1; // Reset to avoid double counting
				}
			}
		}
		entityHealthMap.put(id, health);
	}

	@SubscribeEvent
	public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		comboCount = 0;
		lastPlayerHealth = -1f;
		entityHealthMap.clear();
		lastAttackedEntityId = -1;
	}

}
