package dev.slabstudios.slabclient.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lwjgl.opengl.GL11;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

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
		
		MinecraftForge.EVENT_BUS.register(this);
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
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (!enabled) return;
		if (event.phase != TickEvent.Phase.START) return;

		synchronized (indicators) {
			Iterator<DamageIndicator> iterator = indicators.iterator();
			while (iterator.hasNext()) {
				DamageIndicator ind = iterator.next();
				ind.age++;
				if (ind.age >= 10) {
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
	public void onEntityUpdate(LivingUpdateEvent event) {
		if (event.entityLiving == null || !event.entityLiving.worldObj.isRemote) return;
		if (!enabled) return;

		EntityLivingBase entity = event.entityLiving;
		if (entity == Minecraft.getMinecraft().thePlayer) return;

		int id = entity.getEntityId();
		float health = entity.getHealth();

		if (entityHealthMap.containsKey(id)) {
			float oldHealth = entityHealthMap.get(id);
			if (health < oldHealth) {
				float damage = oldHealth - health;
				
				Minecraft mc = Minecraft.getMinecraft();
				double dx = 0;
				double dz = 0;
				double spawnX = entity.posX;
				double spawnY = entity.posY + entity.height + 0.4;
				double spawnZ = entity.posZ;

				if (mc.thePlayer != null) {
					dx = entity.posX - mc.thePlayer.posX;
					dz = entity.posZ - mc.thePlayer.posZ;
					double len = Math.sqrt(dx * dx + dz * dz);
					if (len > 0.0) {
						dx /= len;
						dz /= len;
						// Offset past the entity's bounding box radius to place clearly on the far side
						double offset = entity.width * 1.6;
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

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!enabled) return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		if (player == null) return;

		double renderX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
		double renderY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
		double renderZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;

		synchronized (indicators) {
			for (DamageIndicator ind : indicators) {
				// Interpolate positions to eliminate camera jitter/flicker
				double x = (ind.prevX + (ind.x - ind.prevX) * event.partialTicks) - renderX;
				double y = (ind.prevY + (ind.y - ind.prevY) * event.partialTicks) - renderY;
				double z = (ind.prevZ + (ind.z - ind.prevZ) * event.partialTicks) - renderZ;

				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, z); 
				GL11.glNormal3f(0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
				
				float ageInterpolated = ind.age + event.partialTicks;
				
				float scaleMultiplier = 1.0F;
				float alpha = 1.0F;

				if (ageInterpolated < 2.0F) {
					// Pop up with overshoot (0.10s)
					float progress = ageInterpolated / 2.0F;
					float ease = 1.0F - (float) Math.pow(1.0F - progress, 3);
					scaleMultiplier = ease * 1.5F;
				} else if (ageInterpolated < 4.0F) {
					// Smooth settle to 1.0 (0.10s)
					float progress = (ageInterpolated - 2.0F) / 2.0F;
					float ease = (float) (Math.cos(progress * Math.PI) + 1.0) / 2.0F;
					scaleMultiplier = 1.0F + ease * 0.5F;
				} else {
					// Snappy shrink and fade over the last 6 ticks (from tick 4 to 10) (0.30s)
					float progress = (ageInterpolated - 4.0F) / 6.0F;
					if (progress > 1.0F) progress = 1.0F;
					scaleMultiplier = 1.0F - progress;
					alpha = 1.0F - progress;
				}

				// Apply roll rotation (Z rotation in screen-space, upright by tick 4)
				float currentRoll = ind.roll * (1.0F - Math.min(1.0F, ageInterpolated / 4.0F));
				GlStateManager.rotate(currentRoll, 0.0F, 0.0F, 1.0F);

				float scaleFactor = -0.03F * scaleMultiplier; 
				GlStateManager.scale(scaleFactor, scaleFactor, Math.abs(scaleFactor));
				
				GlStateManager.disableLighting();
				GlStateManager.depthMask(false);
				GlStateManager.disableDepth();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

				int alphaBits = (int) (alpha * 255) & 0xFF;
				int color = (alphaBits << 24) | 0xFFCC00;
				int heartColor = (alphaBits << 24) | 0xFF5555;

				String numberText = String.format("-%.1f", ind.damage);
				String heartText = " ❤";
				int numWidth = mc.fontRendererObj.getStringWidth(numberText);
				int heartWidth = mc.fontRendererObj.getStringWidth(heartText);
				int totalWidth = numWidth + heartWidth;

				mc.fontRendererObj.drawStringWithShadow(numberText, -totalWidth / 2, 0, color);
				mc.fontRendererObj.drawStringWithShadow(heartText, -totalWidth / 2 + numWidth, 0, heartColor);

				GlStateManager.enableDepth();
				GlStateManager.depthMask(true);
				GlStateManager.enableLighting();
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}

	@SubscribeEvent
	public void onDisconnect(ClientDisconnectionFromServerEvent event) {
		synchronized (indicators) {
			indicators.clear();
		}
		entityHealthMap.clear();
	}

	private static class DamageIndicator {
		double x, y, z;
		double prevX, prevY, prevZ;
		double vx, vy, vz;
		float damage;
		int age;
		float roll;

		DamageIndicator(double x, double y, double z, double dx, double dz, float damage) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.prevX = x;
			this.prevY = y;
			this.prevZ = z;
			this.damage = damage;
			this.age = 0;

			// Random roll angle between -15 and +15 degrees
			this.roll = (float) ((Math.random() - 0.5) * 30.0);

			// Trajectory carrying hit momentum away from the player
			this.vx = dx * 0.08 + (Math.random() - 0.5) * 0.04;
			this.vy = 0.15 + Math.random() * 0.06; // Jump upwards
			this.vz = dz * 0.08 + (Math.random() - 0.5) * 0.04;
		}
	}

}
