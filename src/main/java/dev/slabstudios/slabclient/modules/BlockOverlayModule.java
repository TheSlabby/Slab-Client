package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;

public class BlockOverlayModule extends Module {

	public BlockOverlayModule(int x, int y) {
		super(x, y);
		this.key = "Block Overlay";
		this.enabled = true;
		this.visible = true;

		NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
		if (!enabled) return;

		net.minecraft.world.phys.Vec3 camPos = event.getCamera().position();
		
		event.addCustomRenderer((state, collector, poseStack, levelRenderState) -> {
			poseStack.pushPose();
			net.minecraft.core.BlockPos pos = state.pos();
			poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
			
			// Submit the outline of the block voxel shape using lines RenderType and Cyan color
			collector.submitShapeOutline(
				poseStack,
				state.shape(),
				RenderTypes.lines(),
				0xFF00FFFF, // Cyan: 0xFF00FFFF
				2.5F,       // Line thickness
				true        // depthTest
			);
			
			poseStack.popPose();
			return true; // Cancel default rendering
		});
	}

}
