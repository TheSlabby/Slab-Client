package dev.slabstudios.slabclient.modules;

import java.util.ArrayList;
import java.util.List;

import dev.slabstudios.slabclient.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CPSModule extends Module{

	public static List<Long> clicks = new ArrayList<Long>();
	
	public CPSModule(int x, int y) {
		super(x, y);
		this.key = "CPS";
		this.value = "0";
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void update() {
		long runTime = Minecraft.getSystemTime(); //in ms
		for(int i=0;i<clicks.size();i++) {
			if(runTime > clicks.get(i)+1000) {
				clicks.remove(i);
			}
		}
		this.value = Integer.toString(clicks.size());
	}
	
	
	@SubscribeEvent
	public void onMouseEvent(MouseEvent evt) {
		if (evt.button == 0 && evt.buttonstate == true) {
			clicks.add(Minecraft.getSystemTime());
		}
	}

}
