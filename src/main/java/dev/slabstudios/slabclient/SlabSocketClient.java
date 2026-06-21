package dev.slabstudios.slabclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import net.minecraft.client.Minecraft;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SlabSocketClient {

	public static class RemotePlayer {
		public String username;
		public String uuid;
		public double x, y, z;
		public float yaw, pitch;
		public int dimension;
		public String worldName;
		public String serverIp;
	}

	public static volatile java.util.Map<String, RemotePlayer> remotePlayers = new java.util.HashMap<>();

	public static String serverAddress = "127.0.0.1:8080";
	public static String status = "Disconnected";
	
	private static Socket socket;
	private static PrintWriter out;
	private static BufferedReader in;
	private static Thread connectionThread;
	private static Thread readThread;

	public static void connect(final String address) {
		disconnect();
		
		status = "Connecting...";
		connectionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String host = "127.0.0.1";
					int port = 8080;
					
					if (address.contains(":")) {
						String[] parts = address.split(":");
						host = parts[0];
						try {
							port = Integer.parseInt(parts[1]);
						} catch (NumberFormatException e) {
							// fallback default
						}
					} else {
						host = address;
					}
					
					socket = new Socket(host, port);
					out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
					status = "Connected";
					
					// Send a handshake packet in JSON with player details
					Minecraft mc = Minecraft.getMinecraft();
					JsonObject handshake = new JsonObject();
					handshake.addProperty("type", "handshake");
					handshake.addProperty("version", SlabClient.VERSION);
					if (mc != null && mc.thePlayer != null) {
						handshake.addProperty("uuid", mc.thePlayer.getUniqueID().toString());
						handshake.addProperty("username", mc.thePlayer.getName());
					} else {
						handshake.addProperty("uuid", "offline-uuid-test");
						handshake.addProperty("username", "Player");
					}
					handshake.addProperty("server_ip", ConnectionHandler.ip);
					send(handshake.toString());
					
					// Start async read thread
					readThread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String line;
								while ((line = in.readLine()) != null) {
									handlePacket(line);
								}
							} catch (Exception e) {
								// Socket closed
							} finally {
								cleanup();
							}
						}
					});
					readThread.setName("SlabSocketReader");
					readThread.start();
					
				} catch (Exception e) {
					status = "Failed";
					cleanup();
				}
			}
		});
		connectionThread.setName("SlabSocketConnect");
		connectionThread.start();
	}

	public static void disconnect() {
		status = "Disconnected";
		cleanup();
	}

	public static void send(String json) {
		if (out != null && socket != null && socket.isConnected()) {
			new Thread(() -> {
				try {
					out.println(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
	}

	private static void handlePacket(String line) {
		try {
			System.out.println("[SlabSocket] Received packet: " + line);
			// Parse JSON dynamically to ensure it is valid
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(line).getAsJsonObject();
			
			// Auto-respond to server ping to show connection activity
			if (json.has("type") && json.get("type").getAsString().equals("ping")) {
				JsonObject pong = new JsonObject();
				pong.addProperty("type", "pong");
				send(pong.toString());
			}
			
			// Handle handshake response
			if (json.has("type") && json.get("type").getAsString().equals("handshake_response")) {
				String statusMsg = json.has("status") ? json.get("status").getAsString() : "unknown";
				String message = json.has("message") ? json.get("message").getAsString() : "";
				System.out.println("[SlabSocket] Handshake result: " + statusMsg + " - " + message);
			}

			// Handle clients list broadcast from server
			if (json.has("clients")) {
				JsonObject clientsObj = json.getAsJsonObject("clients");
				java.util.Map<String, RemotePlayer> newPlayers = new java.util.HashMap<>();
				Minecraft mc = Minecraft.getMinecraft();
				String localUuid = (mc != null && mc.thePlayer != null) ? mc.thePlayer.getUniqueID().toString() : "";
				
				for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : clientsObj.entrySet()) {
					String uuid = entry.getKey();
					if (uuid.equals(localUuid)) {
						continue; // Skip ourselves
					}
					try {
						JsonObject clientData = entry.getValue().getAsJsonObject();
						String username = clientData.get("username").getAsString();
						JsonObject posData = clientData.getAsJsonObject("position");
						
						double x = posData.get("x").getAsDouble();
						double y = posData.get("y").getAsDouble();
						double z = posData.get("z").getAsDouble();
						float yaw = posData.get("yaw").getAsFloat();
						float pitch = posData.get("pitch").getAsFloat();
						int dimension = posData.get("dimension").getAsInt();
						String rawWorldName = posData.get("world_name").getAsString();
						
						String serverIp = "";
						String worldName = rawWorldName;
						if (rawWorldName.contains("///")) {
							String[] parts = rawWorldName.split("///", 2);
							serverIp = parts[0];
							worldName = parts[1];
						}
						
						RemotePlayer rp = new RemotePlayer();
						rp.username = username;
						rp.uuid = uuid;
						rp.x = x;
						rp.y = y;
						rp.z = z;
						rp.yaw = yaw;
						rp.pitch = pitch;
						rp.dimension = dimension;
						rp.worldName = worldName;
						rp.serverIp = serverIp;
						
						newPlayers.put(uuid, rp);
					} catch (Exception e) {
						// Skip malformed/incomplete entry
					}
				}
				remotePlayers = newPlayers;
			}
		} catch (Exception e) {
			System.out.println("[SlabSocket] Failed to parse received JSON packet: " + line);
		}
	}

	private static synchronized void cleanup() {
		remotePlayers = new java.util.HashMap<>();
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (Exception e) {}
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		} catch (Exception e) {}
		try {
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (Exception e) {}
	}
}
