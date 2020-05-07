package dev.slabstudios.slabclient.modules;

import java.text.SimpleDateFormat;
import java.util.Date;
import dev.slabstudios.slabclient.Module;

public class TimeModule extends Module {

	public TimeModule(int x, int y) {
		super(x, y);

		this.key = "Time";
	}

	@Override
	public void update() {
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
		Date date = new Date();
		String time = formatter.format(date);
		this.value = time;
		
	}

}
