package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ReachModule extends Module {

	private double lastReach = 0.0;
	private long lastHitTime = 0;

	public ReachModule(int x, int y) {
		super(x, y);
		this.key = "Reach";
		this.value = "0.00m";
		this.visible = true;

		MinecraftForge.EVENT_BUS.register(this);
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
		if (event.entityPlayer == null || !event.entityPlayer.worldObj.isRemote) return;
		if (!enabled) return;

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null) return;

		if (event.entityPlayer == mc.thePlayer && event.target instanceof EntityLivingBase) {
			EntityLivingBase target = (EntityLivingBase) event.target;
			
			// Compute eye-to-eye reach distance
			Vec3 playerEyes = mc.thePlayer.getPositionEyes(1.0F);
			Vec3 targetEyes = new Vec3(target.posX, target.posY + target.getEyeHeight(), target.posZ);
			
			this.lastReach = playerEyes.distanceTo(targetEyes);
			this.lastHitTime = System.currentTimeMillis();
		}
	}

}
