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
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockOverlayModule extends Module {

	public BlockOverlayModule() {
		super(0, 0);
		// TODO Auto-generated constructor stub
		this.visible = false;

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void drawOutline(BlockPos pos) {
		
		GL11.glPushMatrix();
				
		
		//translate draw origin to the center of the chunk the player is in		
		GlStateManager.disableTexture2D();
		
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		GL11.glTranslated(-player.posX,-player.posY,-player.posZ);
		
		GL11.glBegin(GL11.GL_LINES);
		
		
		// Yellow line in the middle
		GL11.glColor3f(1F, 1F, 0F);
		GL11.glLineWidth(20f);

		GL11.glVertex3d(pos.getX(), pos.getY(), pos.getZ());
		GL11.glVertex3d(pos.getX(), 1+pos.getY(), pos.getZ());
		
		
		// Red lines on corners
		GL11.glColor3f(1F, 0F, 0F); 

		GL11.glVertex3d(-1, 0, -1);
		GL11.glVertex3d(-1, 1, -1);

		GL11.glVertex3d(1, 0, 1);
		GL11.glVertex3d(1, 1, 1);

		GL11.glVertex3d(-1, 0, 1);
		GL11.glVertex3d(-1, 1, 1);

		GL11.glVertex3d(1, 0, -1);
		GL11.glVertex3d(1, 1, -1);
		
		GL11.glEnd();
		
		GlStateManager.enableTexture2D();
	
	GL11.glPopMatrix();

		

	}

	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
		if (objectMouseOver != null) {
			if (objectMouseOver.typeOfHit == MovingObjectType.ENTITY) {
				System.out.println("Mouse is over entity!");
			} else if (objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
				Minecraft mc = Minecraft.getMinecraft();

				BlockPos pos = objectMouseOver.getBlockPos();

				Block block = mc.theWorld.getBlockState(pos).getBlock();

				System.out.println("Looking at " + block.getLocalizedName());

				// render wireframe
				drawOutline(pos);
			}
		}
	}
}
