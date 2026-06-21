package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ComboModule extends Module {

	private int comboCount = 0;
	private long lastHitTime = 0;

	public ComboModule(int x, int y) {
		super(x, y);
		this.key = "Combo";
		this.value = "0";
		this.visible = true;

		MinecraftForge.EVENT_BUS.register(this);
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
		if (event.entityPlayer == null || !event.entityPlayer.worldObj.isRemote) return;
		if (!enabled) return;

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null) return;

		if (event.entityPlayer == mc.thePlayer && event.target instanceof EntityLivingBase) {
			this.comboCount++;
			this.lastHitTime = System.currentTimeMillis();
		}
	}

	@SubscribeEvent
	public void onHurt(LivingHurtEvent event) {
		if (event.entity == null || !event.entity.worldObj.isRemote) return;
		if (!enabled) return;

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer == null) return;

		if (event.entity == mc.thePlayer) {
			this.comboCount = 0;
		}
	}

}
