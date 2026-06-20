package dev.slabstudios.slabclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
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
			
			Map<String, ModuleConfig> configMap = new HashMap<String, ModuleConfig>();
			for (Module module : RenderGuiHandler.modules) {
				configMap.put(module.key, new ModuleConfig(module.enabled, module.x, module.y));
			}

			FileWriter writer = new FileWriter(configFile);
			gson.toJson(configMap, writer);
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
			
			Type type = new TypeToken<Map<String, ModuleConfig>>(){}.getType();
			FileReader reader = new FileReader(configFile);
			Map<String, ModuleConfig> configMap = gson.fromJson(reader, type);
			reader.close();

			if (configMap != null) {
				for (Module module : RenderGuiHandler.modules) {
					ModuleConfig config = configMap.get(module.key);
					if (config != null) {
						module.enabled = config.enabled;
						module.visible = config.enabled;
						module.x = config.x;
						module.y = config.y;
					}
				}
			}
			System.out.println("Slab Client: Config loaded successfully!");
		} catch (Exception e) {
			System.out.println("Slab Client: Failed to load config!");
			e.printStackTrace();
		}
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
