package dev.slabstudios.slabclient.modules;

import org.lwjgl.opengl.GL11;

import dev.slabstudios.slabclient.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockOverlayModule extends Module {

	public BlockOverlayModule(int x, int y) {
		super(x, y);
		this.key = "Block Overlay";
		this.enabled = true;
		this.visible = true;

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void drawOutline(BlockPos pos, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		Block block = mc.theWorld.getBlockState(pos).getBlock();
		AxisAlignedBB abb = block.getSelectedBoundingBox(mc.theWorld, pos);
		if (abb == null) return;
		
		// Expand slightly to prevent depth fighting/z-fighting
		double minX = abb.minX - 0.002D;
		double minY = abb.minY - 0.002D;
		double minZ = abb.minZ - 0.002D;
		double maxX = abb.maxX + 0.002D;
		double maxY = abb.maxY + 0.002D;
		double maxZ = abb.maxZ + 0.002D;
		
		GL11.glPushMatrix();
		
		// Translate draw origin to the camera position for smooth rendering
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		
		EntityPlayerSP player = mc.thePlayer;
		double renderX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
		double renderY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
		double renderZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;
		GL11.glTranslated(-renderX, -renderY, -renderZ);
		
		// Enable blending for transparency
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		
		// Render filled transparent block (cyan, 15% opacity)
		GlStateManager.depthMask(false);
		GL11.glColor4f(0.0F, 0.8F, 1.0F, 0.15F);
		GL11.glBegin(GL11.GL_QUADS);
		// Bottom face
		GL11.glVertex3d(minX, minY, minZ);
		GL11.glVertex3d(maxX, minY, minZ);
		GL11.glVertex3d(maxX, minY, maxZ);
		GL11.glVertex3d(minX, minY, maxZ);
		// Top face
		GL11.glVertex3d(minX, maxY, minZ);
		GL11.glVertex3d(minX, maxY, maxZ);
		GL11.glVertex3d(maxX, maxY, maxZ);
		GL11.glVertex3d(maxX, maxY, minZ);
		// North face
		GL11.glVertex3d(minX, minY, minZ);
		GL11.glVertex3d(minX, maxY, minZ);
		GL11.glVertex3d(maxX, maxY, minZ);
		GL11.glVertex3d(maxX, minY, minZ);
		// South face
		GL11.glVertex3d(minX, minY, maxZ);
		GL11.glVertex3d(maxX, minY, maxZ);
		GL11.glVertex3d(maxX, maxY, maxZ);
		GL11.glVertex3d(minX, maxY, maxZ);
		// West face
		GL11.glVertex3d(minX, minY, minZ);
		GL11.glVertex3d(minX, minY, maxZ);
		GL11.glVertex3d(minX, maxY, maxZ);
		GL11.glVertex3d(minX, maxY, minZ);
		// East face
		GL11.glVertex3d(maxX, minY, minZ);
		GL11.glVertex3d(maxX, maxY, minZ);
		GL11.glVertex3d(maxX, maxY, maxZ);
		GL11.glVertex3d(maxX, minY, maxZ);
		GL11.glEnd();
		
		GlStateManager.depthMask(true);
		
		// Render thick outline (cyan, 80% opacity)
		GL11.glLineWidth(2.5F);
		GL11.glColor4f(0.0F, 0.8F, 1.0F, 0.8F);
		GL11.glBegin(GL11.GL_LINES);
		
		// Bottom face
		GL11.glVertex3d(minX, minY, minZ); GL11.glVertex3d(maxX, minY, minZ);
		GL11.glVertex3d(maxX, minY, minZ); GL11.glVertex3d(maxX, minY, maxZ);
		GL11.glVertex3d(maxX, minY, maxZ); GL11.glVertex3d(minX, minY, maxZ);
		GL11.glVertex3d(minX, minY, maxZ); GL11.glVertex3d(minX, minY, minZ);
		
		// Top face
		GL11.glVertex3d(minX, maxY, minZ); GL11.glVertex3d(maxX, maxY, minZ);
		GL11.glVertex3d(maxX, maxY, minZ); GL11.glVertex3d(maxX, maxY, maxZ);
		GL11.glVertex3d(maxX, maxY, maxZ); GL11.glVertex3d(minX, maxY, maxZ);
		GL11.glVertex3d(minX, maxY, maxZ); GL11.glVertex3d(minX, maxY, minZ);
		
		// Vertical edges
		GL11.glVertex3d(minX, minY, minZ); GL11.glVertex3d(minX, maxY, minZ);
		GL11.glVertex3d(maxX, minY, minZ); GL11.glVertex3d(maxX, maxY, minZ);
		GL11.glVertex3d(maxX, minY, maxZ); GL11.glVertex3d(maxX, maxY, maxZ);
		GL11.glVertex3d(minX, minY, maxZ); GL11.glVertex3d(minX, maxY, maxZ);
		
		GL11.glEnd();
		
		// Reset GL states
		GL11.glLineWidth(1.0F);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		
		GL11.glPopMatrix();
	}

	@SubscribeEvent
	public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		if (!enabled) return;
		
		if (event.target != null && event.target.typeOfHit == MovingObjectType.BLOCK) {
			drawOutline(event.target.getBlockPos(), event.partialTicks);
			event.setCanceled(true);
		}
	}
}
