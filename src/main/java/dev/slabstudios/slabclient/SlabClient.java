package dev.slabstudios.slabclient;

import dev.slabstudios.slabclient.modules.AutoGG;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;

@Mod(SlabClient.MODID)
public class SlabClient {
	public static final String MODID = "slabclient";
	public static final String VERSION = "2.0";

	public SlabClient(IEventBus modEventBus) {
		// Register our event handlers
		NeoForge.EVENT_BUS.register(new RenderGuiHandler());
		NeoForge.EVENT_BUS.register(new ConnectionHandler());
		NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			Commands.literal("setgg")
				.executes(context -> {
					context.getSource().sendFailure(Component.literal("§cInvalid Arguments. Usage: /setgg [end game message]"));
					return 0;
				})
				.then(Commands.argument("message", StringArgumentType.greedyString())
					.executes(context -> {
						String msg = StringArgumentType.getString(context, "message");
						AutoGG.endGameMSG = msg;
						context.getSource().sendSuccess(() -> 
							Component.literal("§aSet gg message to: §l" + msg), false);
						return 1;
					}))
		);
	}
}
