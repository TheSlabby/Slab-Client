package dev.slabstudios.slabclient.commands;

import dev.slabstudios.slabclient.modules.AutoGG;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class SetAutoGGMSG extends CommandBase{

	
	
	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return "setgg";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}
	
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring)
	{
		if(astring.length == 0)
		{
			icommandsender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Invalid Arguments. Usage: " + this.getCommandUsage(icommandsender)));
			return;
		}
		String msg = "";
		for (String s : astring) {
			msg = msg + s + " ";
		}
		AutoGG.endGameMSG = msg;
		icommandsender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Set gg message to: " + EnumChatFormatting.BOLD + msg));
	}


	@Override
	public String getCommandUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/setgg [end game message]";
	}

}
