package dev.slabstudios.slabclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.slabstudios.slabclient.modules.AutoGG;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

	private static final File configFile = new File("config/slabclient.json");
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static void save() {
		try {
			if (!configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}
			
			ClientConfig config = new ClientConfig();
			for (Module module : RenderGuiHandler.modules) {
				config.modules.put(module.key, new ModuleConfig(module.enabled, module.x, module.y));
			}
			
			config.endGameMsg = AutoGG.endGameMSG;
			config.serverAddress = SlabSocketClient.serverAddress;

			FileWriter writer = new FileWriter(configFile);
			gson.toJson(config, writer);
			writer.close();
			System.out.println("Slab Client: Config saved successfully!");
		} catch (Exception e) {
			System.out.println("Slab Client: Failed to save config!");
			e.printStackTrace();
		}
	}

	public static void load() {
		try {
			if (!configFile.exists()) {
				return;
			}
			
			FileReader reader = new FileReader(configFile);
			ClientConfig config = gson.fromJson(reader, ClientConfig.class);
			reader.close();

			if (config != null) {
				if (config.modules != null) {
					for (Module module : RenderGuiHandler.modules) {
						ModuleConfig modConfig = config.modules.get(module.key);
						if (modConfig != null) {
							module.enabled = modConfig.enabled;
							module.visible = modConfig.enabled;
							module.x = modConfig.x;
							module.y = modConfig.y;
						}
					}
				}
				
				AutoGG.endGameMSG = config.endGameMsg;
				if (config.serverAddress != null) {
					SlabSocketClient.serverAddress = config.serverAddress;
				}
			}
			System.out.println("Slab Client: Config loaded successfully!");
		} catch (Exception e) {
			System.out.println("Slab Client: Failed to load config!");
			e.printStackTrace();
		}
	}

	private static class ClientConfig {
		Map<String, ModuleConfig> modules = new HashMap<String, ModuleConfig>();
		String endGameMsg = "gg";
		String serverAddress = "127.0.0.1:8080";
	}

	private static class ModuleConfig {
		boolean enabled;
		int x;
		int y;

		ModuleConfig(boolean enabled, int x, int y) {
			this.enabled = enabled;
			this.x = x;
			this.y = y;
		}
	}
}
