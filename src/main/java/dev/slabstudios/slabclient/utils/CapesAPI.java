package dev.slabstudios.slabclient.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.Identifier;

public class CapesAPI {

	/**
	 * Base URL to fetch the capes from
	 */
	private static final String BASE_URL = "http://URL.com/getCape/%s";

	/**
	 * Holds a list of UUIDs whose cape is currently being fetched
	 */
	private static final ArrayList<UUID> pendingRequests = new ArrayList<UUID>();

	private static final Map<UUID, Identifier> capes = new HashMap<UUID, Identifier>();

	/**
	 * Fetches a cape for the given player and stores it's Identifier in the
	 * capes map
	 *
	 * @param uuid UUID of the player to load the cape for
	 */
	public static void loadCape(final UUID uuid) {
		// Stubbed out for Minecraft 26.2 compatibility as CapesAPI is unused
	}

	/**
	 * Set the cape of a player
	 *
	 * @param uuid             UUID of the Player to store the cape for
	 * @param resourceLocation Identifier of the cape
	 */
	public static void setCape(UUID uuid, Identifier resourceLocation) {
		CapesAPI.capes.put(uuid, resourceLocation);
	}

	/**
	 * Remove the cape of the user from the cape hashmap
	 */
	public static void deleteCape(UUID uuid) {
		CapesAPI.capes.remove(uuid);
	}

	/**
	 * Get the cape of the user from the cape hashmap
	 *
	 * @return Identifier of the cape or null if none was found
	 */
	public static Identifier getCape(UUID uuid) {
		return capes.getOrDefault(uuid, null);
	}

	/**
	 * Determines whether a player has a cape. If capes were reset recently, this
	 * check also fetches the capes of previously seen players.
	 *
	 * @param uuid UUID of the player to check for
	 * @return true if the player has a cape, otherwise false
	 */
	public static boolean hasCape(UUID uuid) {
		boolean hasCape = CapesAPI.capes.containsKey(uuid);
		Identifier resourceLocation = CapesAPI.capes.get(uuid);

		if (hasCape && resourceLocation == null && !CapesAPI.hasPendingRequests(uuid)) {
			CapesAPI.loadCape(uuid);
			return false;
		}

		return hasCape;
	}

	/**
	 * Resets the capes map and downloads capes of players previously seen once you
	 * they are in range
	 */
	public static void resetCapes() {
		for (UUID userId : CapesAPI.capes.keySet()) {
			CapesAPI.capes.put(userId, null);
		}
	}

	/**
	 * Determines wether a player's cape is currently being fetched
	 *
	 * @param uuid UUID of the player to check for
	 * @return true if the player's cape is currently being fetched, false otherwise
	 */
	private static boolean hasPendingRequests(UUID uuid) {
		return CapesAPI.pendingRequests.contains(uuid);
	}

}