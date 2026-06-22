package dev.slabstudios.slabclient.modules;

import dev.slabstudios.slabclient.Module;
import dev.slabstudios.slabclient.SlabSocketClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.ChatFormatting;

public class RadarModule extends Module {

	private static final int COMPASS_WIDTH = 240;
	private static final int COMPASS_HEIGHT = 14;
	private static final float COMPASS_FOV = 120.0f; // field of view in degrees

	public RadarModule(int x, int y) {
		super(x, y);
		this.key = "Radar";
		this.value = "Enabled";
		this.visible = true;
	}

	@Override
	public void update() {
		if (enabled) {
			this.value = "Enabled";
		} else {
			this.value = "Disabled";
		}
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, boolean force) {
		if (!visible && !force) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;

		// 1. Draw feathered compass background (fades out at left and right 30px edges)
		// Center solid part (0x60000000)
		guiGraphics.fill(x + 30, y, x + COMPASS_WIDTH - 30, y + COMPASS_HEIGHT, 0x60000000);
		// Left fade part
		for (int i = 0; i < 30; i++) {
			int alpha = (int) ((i / 30.0f) * 0x60);
			guiGraphics.fill(x + i, y, x + i + 1, y + COMPASS_HEIGHT, (alpha << 24));
		}
		// Right fade part
		for (int i = 0; i < 30; i++) {
			int alpha = (int) (((30 - i) / 30.0f) * 0x60);
			guiGraphics.fill(x + COMPASS_WIDTH - 30 + i, y, x + COMPASS_WIDTH - 30 + i + 1, y + COMPASS_HEIGHT, (alpha << 24));
		}

		// Draw feathered top and bottom borders (0x30FFFFFF max opacity)
		// Center solid part
		guiGraphics.fill(x + 30, y, x + COMPASS_WIDTH - 30, y + 1, 0x30FFFFFF);
		guiGraphics.fill(x + 30, y + COMPASS_HEIGHT - 1, x + COMPASS_WIDTH - 30, y + COMPASS_HEIGHT, 0x30FFFFFF);
		// Left fade part
		for (int i = 0; i < 30; i++) {
			int alpha = (int) ((i / 30.0f) * 0x30);
			guiGraphics.fill(x + i, y, x + i + 1, y + 1, (alpha << 24) | 0x00FFFFFF);
			guiGraphics.fill(x + i, y + COMPASS_HEIGHT - 1, x + i + 1, y + COMPASS_HEIGHT, (alpha << 24) | 0x00FFFFFF);
		}
		// Right fade part
		for (int i = 0; i < 30; i++) {
			int alpha = (int) (((30 - i) / 30.0f) * 0x30);
			guiGraphics.fill(x + COMPASS_WIDTH - 30 + i, y, x + COMPASS_WIDTH - 30 + i + 1, y + 1, (alpha << 24) | 0x00FFFFFF);
			guiGraphics.fill(x + COMPASS_WIDTH - 30 + i, y + COMPASS_HEIGHT - 1, x + COMPASS_WIDTH - 30 + i + 1, y + COMPASS_HEIGHT, (alpha << 24) | 0x00FFFFFF);
		}

		// Enable scissor clipping slightly inside the background bounds (x+5 to x+235) to prevent edge overflow
		guiGraphics.enableScissor(x + 5, y, x + COMPASS_WIDTH - 5, y + COMPASS_HEIGHT);

		// Get current player yaw (normalized to 0-360)
		float yaw = mc.player.getYRot() % 360;
		if (yaw < 0) yaw += 360;

		// 2. Render Cardinal Directions and Heading Degree Numbers (Fortnite/PUBG Style)
		// Standard compass degrees: North is 0, East is 90, South is 180, West is 270.
		int[] angles = { 0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330 };
		String[] labels = { "N", "30", "60", "E", "120", "150", "S", "210", "240", "W", "300", "330" };
		
		for (int i = 0; i < 12; i++) {
			int alphaDegree = angles[i];
			// Convert standard compass angle to Minecraft yaw (North=180, East=270, South=0, West=90)
			float dirYaw = (alphaDegree + 180.0f) % 360.0f;
			float relAngle = dirYaw - yaw;
			
			// Normalize to [-180, 180]
			relAngle = (relAngle + 180.0f) % 360.0f;
			if (relAngle < 0) relAngle += 360.0f;
			relAngle -= 180.0f;

			// If inside compass FOV, draw it
			if (Math.abs(relAngle) <= COMPASS_FOV / 2.0f) {
				float pct = relAngle / (COMPASS_FOV / 2.0f); // -1.0 to 1.0
				int labelX = x + COMPASS_WIDTH / 2 + (int)(pct * (COMPASS_WIDTH / 2.0f));

				String label = labels[i];
				int color = 0xFFFFFFFF;
				boolean isCardinal = label.equals("N") || label.equals("S") || label.equals("E") || label.equals("W");
				if (label.equals("N")) {
					color = 0xFFFF5555; // Red for North
				} else if (label.equals("S") || label.equals("E") || label.equals("W")) {
					color = 0xFFFFAA00; // Gold for other major cardinals
				} else {
					color = 0xFFAAAAAA; // Grey for degree numbers
				}

				// Fade out slightly before hitting the edge of the scissor box
				float alphaFactor = 1.0f - (Math.abs(relAngle) / (COMPASS_FOV / 2.0f - 10.0f));
				alphaFactor = Math.max(0.0f, Math.min(1.0f, alphaFactor));
				alphaFactor = (float) Math.pow(alphaFactor, 1.5);
				int alpha = (int) (alphaFactor * 255.0f);

				// Skip completely if alpha is near 0 to avoid FontRenderer's 255 alpha default reset!
				if (alpha < 10) {
					continue;
				}

				int colorWithAlpha = (alpha << 24) | (color & 0x00FFFFFF);

				if (isCardinal) {
					// Draw cardinals at normal size
					guiGraphics.centeredText(mc.font, label, labelX, y + 3, colorWithAlpha);
				} else {
					// Scale down secondary degree numbers to 0.75 size
					guiGraphics.pose().pushMatrix();
					guiGraphics.pose().translate(labelX, y + 4.5f);
					guiGraphics.pose().scale(0.75f, 0.75f);
					guiGraphics.centeredText(mc.font, label, 0, 0, colorWithAlpha);
					guiGraphics.pose().popMatrix();
				}
			}
		}

		// 3. Render minor tick marks (every 15 degrees, skipping direction/number positions)
		for (int angleDeg = 0; angleDeg < 360; angleDeg += 15) {
			if (angleDeg % 30 == 0) continue; // Skip angles where we draw direction labels or heading numbers

			// Convert standard compass angle to Minecraft yaw
			float dirYaw = (angleDeg + 180.0f) % 360.0f;
			float relAngle = dirYaw - yaw;
			
			relAngle = (relAngle + 180.0f) % 360.0f;
			if (relAngle < 0) relAngle += 360.0f;
			relAngle -= 180.0f;

			if (Math.abs(relAngle) <= COMPASS_FOV / 2.0f) {
				float pct = relAngle / (COMPASS_FOV / 2.0f);
				int tickX = x + COMPASS_WIDTH / 2 + (int)(pct * (COMPASS_WIDTH / 2.0f));
				
				// Fade out slightly before the edges
				float alphaFactor = 1.0f - (Math.abs(relAngle) / (COMPASS_FOV / 2.0f - 5.0f));
				alphaFactor = Math.max(0.0f, Math.min(1.0f, alphaFactor));
				alphaFactor = (float) Math.pow(alphaFactor, 1.5);
				int alpha = (int) (alphaFactor * 128.0f); // Max tick alpha 128

				if (alpha < 5) {
					continue;
				}

				int tickColor = (alpha << 24) | 0x00FFFFFF;
				guiGraphics.fill(tickX, y + COMPASS_HEIGHT - 4, tickX + 1, y + COMPASS_HEIGHT - 1, tickColor);
			}
		}

		// 4. Draw center indicator line (red vertical marker)
		guiGraphics.fill(x + COMPASS_WIDTH / 2 - 1, y, x + COMPASS_WIDTH / 2 + 1, y + COMPASS_HEIGHT, 0xFFFF5555);

		// 5. Draw other players' indicators on the compass bar
		for (SlabSocketClient.RemotePlayer rp : SlabSocketClient.remotePlayers.values()) {

			// Calculate target yaw/angle relative to local player
			double dx = rp.x - mc.player.getX();
			double dz = rp.z - mc.player.getZ();

			double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
			double relAngle = targetYaw - mc.player.getYRot();

			// Normalize to [-180, 180]
			relAngle = (relAngle + 180) % 360;
			if (relAngle < 0) relAngle += 360;
			relAngle -= 180;

			if (Math.abs(relAngle) <= COMPASS_FOV / 2.0f) {
				float pct = (float)(relAngle / (COMPASS_FOV / 2.0f));
				int markX = x + COMPASS_WIDTH / 2 + (int)(pct * (COMPASS_WIDTH / 2.0f));

				// Calculate fade alpha (fade out slightly before the edges)
				float alphaFactor = 1.0f - (float)(Math.abs(relAngle) / (COMPASS_FOV / 2.0f - 5.0f));
				alphaFactor = Math.max(0.0f, Math.min(1.0f, alphaFactor));
				alphaFactor = (float) Math.pow(alphaFactor, 1.5);
				
				int alphaRect = (int) (alphaFactor * 255.0f);
				if (alphaRect < 10) {
					continue;
				}

				int rectColor = (alphaRect << 24) | 0x0055FFFF; // Cyan

				// Draw player indicator dot/tick on compass bar
				guiGraphics.fill(markX - 2, y + COMPASS_HEIGHT - 6, markX + 2, y + COMPASS_HEIGHT - 2, rectColor);
			}
		}

		// Disable scissor clipping to allow drawing names and lines below the compass bar
		guiGraphics.disableScissor();

		// 6. Draw player connector lines and name tags below the compass bar
		int labelOffset = 0; // to prevent overlapping names below the bar

		for (SlabSocketClient.RemotePlayer rp : SlabSocketClient.remotePlayers.values()) {

			// Calculate target yaw/angle relative to local player
			double dx = rp.x - mc.player.getX();
			double dz = rp.z - mc.player.getZ();
			double distance = Math.sqrt(dx * dx + dz * dz);

			double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
			double relAngle = targetYaw - mc.player.getYRot();

			// Normalize to [-180, 180]
			relAngle = (relAngle + 180) % 360;
			if (relAngle < 0) relAngle += 360;
			relAngle -= 180;

			if (Math.abs(relAngle) <= COMPASS_FOV / 2.0f) {
				float pct = (float)(relAngle / (COMPASS_FOV / 2.0f));
				int markX = x + COMPASS_WIDTH / 2 + (int)(pct * (COMPASS_WIDTH / 2.0f));

				// Calculate fade alpha
				float alphaFactor = 1.0f - (float)(Math.abs(relAngle) / (COMPASS_FOV / 2.0f - 10.0f));
				alphaFactor = Math.max(0.0f, Math.min(1.0f, alphaFactor));
				alphaFactor = (float) Math.pow(alphaFactor, 1.5);
				
				int alphaRect = (int) (alphaFactor * 255.0f);
				if (alphaRect < 10) {
					continue;
				}

				int lineColor = ((int)(alphaFactor * 96.0f) << 24) | 0x0055FFFF;

				// Draw a small connector line pointing down to the name
				guiGraphics.fill(markX, y + COMPASS_HEIGHT, markX + 1, y + COMPASS_HEIGHT + 2, lineColor);

				// Render name + distance scaled down and faded
				guiGraphics.pose().pushMatrix();
				guiGraphics.pose().translate(markX, y + COMPASS_HEIGHT + 3 + (labelOffset % 2) * 8);
				guiGraphics.pose().scale(0.7f, 0.7f);
				
				int textColor = (alphaRect << 24) | 0x00FFFFFF;
				String infoText = ChatFormatting.GREEN + rp.username + ChatFormatting.GRAY + " (" + (int)distance + "m)";
				guiGraphics.centeredText(mc.font, infoText, 0, 0, textColor);
				
				guiGraphics.pose().popMatrix();
				labelOffset++;
			}
		}
	}

	@Override
	public int getWidth() {
		return COMPASS_WIDTH;
	}

	@Override
	public int getHeight() {
		return COMPASS_HEIGHT + 18; // Space for name tags
	}
}
