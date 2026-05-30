package net.guilhermegomes;

import net.fabricmc.api.ModInitializer;
import net.guilhermegomes.network.SmartStorageNetworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartStorage implements ModInitializer {
	public static final String MOD_ID = "smart-storage";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SmartStorageNetworking.register();
		LOGGER.info("Smart Inventory initialized.");
	}
}
