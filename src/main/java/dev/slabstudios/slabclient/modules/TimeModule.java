package dev.slabstudios.slabclient.modules;

import java.text.SimpleDateFormat;
import java.util.Date;
import dev.slabstudios.slabclient.Module;

public class TimeModule extends Module {

	private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");

	public TimeModule(int x, int y) {
		super(x, y);
		this.key = "Time";
	}

	@Override
	public void update() {
		this.value = formatter.format(new Date());
	}

}
