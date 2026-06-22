package dev.slabstudios.slabclient.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class DamageIndicatorsModule extends Module {

	public static DamageIndicatorsModule instance;

	private final List<DamageIndicator> indicators = new ArrayList<DamageIndicator>();
	private final Map<Integer, Float> entityHealthMap = new HashMap<Integer, Float>();

	public DamageIndicatorsModule(int x, int y) {
		super(x, y);
		instance = this;
		this.key = "Damage Indicators";
		this.value = "Enabled";
		this.visible = true;
		
		NeoForge.EVENT_BUS.register(this);
	}

	@Override
	public void update() {
		if (enabled) {
			this.value = "Enabled";
		} else {
			this.value = "Disabled";
		}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent.Post event) {
		if (!enabled) return;

		synchronized (indicators) {
			Iterator<DamageIndicator> iterator = indicators.iterator();
			while (iterator.hasNext()) {
				DamageIndicator ind = iterator.next();
				ind.age++;
				if (ind.age >= 20) {
					iterator.remove();
					continue;
				}

				ind.prevX = ind.x;
				ind.prevY = ind.y;
				ind.prevZ = ind.z;

				// Apply physics (gravity pull and air resistance)
				ind.x += ind.vx;
				ind.y += ind.vy;
				ind.z += ind.vz;
				
				ind.vy -= 0.010; // Slightly lower gravity so it stays in the air longer
				ind.vx *= 0.94;  // Air resistance drag
				ind.vz *= 0.94;
			}
		}
	}

	@SubscribeEvent
	public void onEntityTick(EntityTickEvent.Post event) {
		if (!enabled) return;

		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity living)) return;
		if (!living.level().isClientSide()) return;
		
		Minecraft mc = Minecraft.getInstance();
		if (living == mc.player) return;

		int id = living.getId();
		float health = living.getHealth();

		if (entityHealthMap.containsKey(id)) {
			float oldHealth = entityHealthMap.get(id);
			if (health < oldHealth) {
				float damage = oldHealth - health;
				
				double dx = 0;
				double dz = 0;
				double spawnX = living.getX();
				double spawnY = living.getY() + living.getBbHeight() + 0.4;
				double spawnZ = living.getZ();

				if (mc.player != null) {
					dx = living.getX() - mc.player.getX();
					dz = living.getZ() - mc.player.getZ();
					double len = Math.sqrt(dx * dx + dz * dz);
					if (len > 0.0) {
						dx /= len;
						dz /= len;
						double offset = living.getBbWidth() * 1.6;
						spawnX += dx * offset;
						spawnZ += dz * offset;
					}
				}

				synchronized (indicators) {
					indicators.add(new DamageIndicator(
						spawnX, 
						spawnY, 
						spawnZ, 
						dx,
						dz,
						damage
					));
				}
			}
			entityHealthMap.put(id, health);
		} else {
			entityHealthMap.put(id, health);
		}
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, boolean force) {
		if (!visible && !force) return;

		if (force) {
			super.render(guiGraphics, true);
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		net.minecraft.client.Camera camera = mc.gameRenderer.mainCamera();
		net.minecraft.world.phys.Vec3 camPos = camera.position();
		
		org.joml.Vector3fc forward = camera.forwardVector();
		org.joml.Vector3fc up = camera.upVector();
		org.joml.Vector3fc left = camera.leftVector();
		
		double fov = mc.options.fov().get();
		double halfWidth = mc.getWindow().getGuiScaledWidth() / 2.0;
		double halfHeight = mc.getWindow().getGuiScaledHeight() / 2.0;
		double fovFactor = halfHeight / Math.tan(Math.toRadians(fov) / 2.0);

		synchronized (indicators) {
			for (DamageIndicator ind : indicators) {
				// Interpolate positions to eliminate camera jitter/flicker
				double x = ind.prevX + (ind.x - ind.prevX) * partialTicks;
				double y = ind.prevY + (ind.y - ind.prevY) * partialTicks;
				double z = ind.prevZ + (ind.z - ind.prevZ) * partialTicks;

				// Relative to camera position
				double dx = x - camPos.x;
				double dy = y - camPos.y;
				double dz = z - camPos.z;

				// Project onto camera local axes
				double localForward = dx * forward.x() + dy * forward.y() + dz * forward.z();
				if (localForward <= 0) continue; // Behind camera

				double localUp = dx * up.x() + dy * up.y() + dz * up.z();
				double localRight = -(dx * left.x() + dy * left.y() + dz * left.z());

				// Project to screen space
				float screenX = (float) (halfWidth + (localRight / localForward) * fovFactor);
				float screenY = (float) (halfHeight - (localUp / localForward) * fovFactor);

				float ageInterpolated = ind.age + partialTicks;
				float scaleMultiplier = 1.0F;
				float alpha = 1.0F;

				if (ageInterpolated < 3.0F) {
					float progress = ageInterpolated / 3.0F;
					float ease = 1.0F - (float) Math.pow(1.0F - progress, 3);
					scaleMultiplier = ease * 1.5F;
				} else if (ageInterpolated < 6.0F) {
					float progress = (ageInterpolated - 3.0F) / 3.0F;
					float ease = (float) (Math.cos(progress * Math.PI) + 1.0) / 2.0F;
					scaleMultiplier = 1.0F + ease * 0.5F;
				} else {
					float progress = (ageInterpolated - 6.0F) / 14.0F;
					if (progress > 1.0F) progress = 1.0F;
					scaleMultiplier = 1.0F - progress;
					alpha = 1.0F - progress;
				}

				int alphaBits = (int) (alpha * 255) & 0xFF;
				int color = (alphaBits << 24) | 0xFFCC00;
				int heartColor = (alphaBits << 24) | 0xFF5555;

				String numberText = String.format("-%.1f", ind.damage);
				String heartText = " ❤";
				int numWidth = mc.font.width(numberText);
				int heartWidth = mc.font.width(heartText);
				int totalWidth = numWidth + heartWidth;

				guiGraphics.pose().pushMatrix();
				guiGraphics.pose().translate((float) screenX, (float) screenY);
				guiGraphics.pose().scale(scaleMultiplier, scaleMultiplier);

				// Draw projected nameplate text at screen position
				guiGraphics.text(mc.font, numberText, -totalWidth / 2, -4, color, true);
				guiGraphics.text(mc.font, heartText, -totalWidth / 2 + numWidth, -4, heartColor, true);

				guiGraphics.pose().popMatrix();
			}
		}
	}

	@SubscribeEvent
	public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		synchronized (indicators) {
			indicators.clear();
		}
		entityHealthMap.clear();
	}
}
