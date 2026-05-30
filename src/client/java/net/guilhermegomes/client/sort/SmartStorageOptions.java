package net.guilhermegomes.client.sort;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.guilhermegomes.SmartStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Opcoes do cliente, persistidas em config/smart-storage.json (carregadas no primeiro acesso). */
public final class SmartStorageOptions {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("smart-storage.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String PRESERVE_HOTBAR = "preserveHotbar";

	private static boolean preserveHotbar = load();

	private SmartStorageOptions() {
	}

	public static boolean preserveHotbar() {
		return preserveHotbar;
	}

	public static void toggleHotbar() {
		preserveHotbar = !preserveHotbar;
		save();
	}

	private static boolean load() {
		try {
			if (Files.exists(FILE)) {
				JsonObject json = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
				if (json.has(PRESERVE_HOTBAR)) {
					return json.get(PRESERVE_HOTBAR).getAsBoolean();
				}
			}
		} catch (Exception e) {
			SmartStorage.LOGGER.warn("Could not read config, using defaults", e);
		}
		return true;
	}

	private static void save() {
		JsonObject json = new JsonObject();
		json.addProperty(PRESERVE_HOTBAR, preserveHotbar);
		try {
			Files.writeString(FILE, GSON.toJson(json));
		} catch (IOException e) {
			SmartStorage.LOGGER.warn("Could not write config", e);
		}
	}
}
