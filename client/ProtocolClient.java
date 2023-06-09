package client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.joml.*;
import tage.networking.client.GameConnectionClient;

public class ProtocolClient extends GameConnectionClient {
	private UUID id;
	private MyGame game;
	private GhostNPC ghostNPC;
	private GhostManager ghostManager;

	public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game)
			throws IOException {
		super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		ghostManager = game.getGhostManager();
		System.out.println("Server address: " + remoteAddr + " | Port: " + remotePort + " | Protocol: " + protocolType);
	}

	// --------------- GHOST NPC SECTION --------------- //
	/*
	 * private void createGhostNPC(Vector3f position) throws IOException {
	 * if (ghostNPC == null) {
	 * ghostNPC = new GhostNPC(0, game.getNPCshape(), game.getNPCtexture(),
	 * position);
	 * System.out.println("adding ghost npc at position --> " + position);
	 * }
	 * }
	 * 
	 * private void updateGhostNPC(Vector3f position, double gsize) {
	 * boolean gs;
	 * if (ghostNPC == null) {
	 * try {
	 * createGhostNPC(position);
	 * } catch (IOException e) {
	 * System.out.println("error creating npc");
	 * }
	 * }
	 * ghostNPC.setPosition(position);
	 * if (gsize == 1.0)
	 * gs = false;
	 * else
	 * gs = true;
	 * ghostNPC.setSize(gs);
	 * }
	 */
	@Override
	protected void processPacket(Object message) {
		String strMessage = (String) message;
		System.out.println("message received --> " + strMessage);
		String[] messageTokens = strMessage.split(",");

		// Game specific protocol to handle the message
		if (messageTokens.length > 0) {
			System.out.println("message token: " + messageTokens[0]);
			// Handle JOIN message
			// Format: (join,success) or (join,failure)
			if (messageTokens[0].compareTo("join") == 0) {
				if (messageTokens[1].compareTo("success") == 0) {
					System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition(), game.getPlayerRotation());
					// sendNeedNPCmsg(id, game.getPlayerPosition());
				}
				if (messageTokens[1].compareTo("failure") == 0) {
					System.out.println("join failure confirmed");
					game.setIsConnected(false);
				}
			}

			// Handle BYE message
			// Format: (bye,remoteId)
			if (messageTokens[0].compareTo("bye") == 0) {
				// remove ghost avatar with id = remoteId
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
				ghostManager.removeGhostNPC(ghostID);
			}

			// Handle CREATE message
			// Format: (create,remoteId,x,y,z)
			// AND
			// Handle DETAILS_FOR message
			// Format: (dsfr,remoteId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0)) {
				// create a new ghost avatar Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);

				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4]));
				Matrix4f ghostRotation = new Matrix4f(
						Float.parseFloat(messageTokens[5]),
						Float.parseFloat(messageTokens[6]),
						Float.parseFloat(messageTokens[7]),
						Float.parseFloat(messageTokens[8]),
						Float.parseFloat(messageTokens[9]),
						Float.parseFloat(messageTokens[10]),
						Float.parseFloat(messageTokens[11]),
						Float.parseFloat(messageTokens[12]),
						Float.parseFloat(messageTokens[13]),
						Float.parseFloat(messageTokens[14]),
						Float.parseFloat(messageTokens[15]),
						Float.parseFloat(messageTokens[16]),
						Float.parseFloat(messageTokens[17]),
						Float.parseFloat(messageTokens[18]),
						Float.parseFloat(messageTokens[19]),
						Float.parseFloat(messageTokens[20]));
				try {
					ghostManager.createGhostAvatar(ghostID, ghostPosition, ghostRotation);
				} catch (IOException e) {
					System.out.println("error creating ghost avatar");
				}
			}

			// Handle WANTS_DETAILS message
			// Format: (wsds,remoteId)
			if (messageTokens[0].compareTo("wsds") == 0) {
				// Send the local client's avatar's information
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition(), game.getPlayerRotation());
			}

			// Handle MOVE message
			// Format: (move,remoteId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0) {
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);

				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4]));
				Matrix4f ghostRotation = new Matrix4f(
						Float.parseFloat(messageTokens[5]),
						Float.parseFloat(messageTokens[6]),
						Float.parseFloat(messageTokens[7]),
						Float.parseFloat(messageTokens[8]),
						Float.parseFloat(messageTokens[9]),
						Float.parseFloat(messageTokens[10]),
						Float.parseFloat(messageTokens[11]),
						Float.parseFloat(messageTokens[12]),
						Float.parseFloat(messageTokens[13]),
						Float.parseFloat(messageTokens[14]),
						Float.parseFloat(messageTokens[15]),
						Float.parseFloat(messageTokens[16]),
						Float.parseFloat(messageTokens[17]),
						Float.parseFloat(messageTokens[18]),
						Float.parseFloat(messageTokens[19]),
						Float.parseFloat(messageTokens[20]));
				String team = messageTokens[21];

				game.setGhostWalking(true);
				ghostManager.updateGhostAvatar(ghostID, ghostPosition, ghostRotation, team);
			}

			// more additions to the network protocol to handle ghosts:
			if (messageTokens[0].compareTo("createNPC") == 0) {
				// create a new ghost NPC
				// Parse out the position
				UUID ghostID = UUID.fromString(messageTokens[1]);
				Vector3f ghostPosition = new Vector3f(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4]));
				// Vector3f ghostPosition = new Vector3f(game.getPlayerPosition());
				try {
					ghostManager.createGhostNPC(ghostID, ghostPosition);
				} catch (IOException e) {
					System.out.println("error creating ghost npc");
				}
			}

			if (messageTokens[0].compareTo("isnear") == 0) {
				// UUID ghostID = UUID.fromString(messageTokens[1]);
				// Parse out the position
				// Vector3f ghostPosition = new Vector3f(
				// Float.parseFloat(messageTokens[1]),
				// Float.parseFloat(messageTokens[2]),
				// Float.parseFloat(messageTokens[3]));
				game.checkNPCNear(ghostNPC);
				System.out.println("sending check for isnear");
				// call method that checks if avatar is near to npc??
			}

			// Handle MOVE NPC message
			// Format: (mnpc,remoteId,x,y,z)
			if (messageTokens[0].compareTo("mnpc") == 0) {
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);

				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
						Float.parseFloat(messageTokens[2]),
						Float.parseFloat(messageTokens[3]),
						Float.parseFloat(messageTokens[4]));
				ghostManager.updateGhostNPC(ghostID, ghostPosition);
			}

			if (messageTokens[0].compareTo("bsUpdate") == 0) {
				String blueScore = messageTokens[1];
				game.setBlueScore(blueScore);
			}

			if (messageTokens[0].compareTo("rsUpdate") == 0) {
				String redScore = messageTokens[1];
				game.setRedScore(redScore);
			}
			if (messageTokens[0].compareTo("lifeUpdate") == 0) {
				game.minusLife();
			}
		}
	}

	// send npc near message
	public void sendNPCNearMessage(UUID ghostID) {
		try {
			sendPacket(new String("isnear," + ghostID.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// send npc location info

	public void sendMoveNPCMessage(Vector3f position) {
		try {
			String message = new String("mnpc," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// send need NPC message
	public void sendNeedNPCmsg(UUID clientID, Vector3f position) {
		try {
			System.out.println("client telling server it needs NPC");
			String message = new String("needNPC," + clientID.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// The initial message from the game client requesting to join the
	// server. localId is a unique identifier for the client. Recommend
	// a random UUID.
	// Message Format: (join,localId)

	public void sendJoinMessage() {
		try {
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server that the client is leaving the server.
	// Message Format: (bye,localId)

	public void sendByeMessage() {
		try {
			sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server of the client's Avatar's position. The server
	// takes this message and forwards it to all other clients registered
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the
	// position

	public void sendCreateMessage(Vector3f position, Matrix4f rotation) {
		try {
			String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + rotation.m00();
			message += "," + rotation.m01();
			message += "," + rotation.m02();
			message += "," + rotation.m03();
			message += "," + rotation.m10();
			message += "," + rotation.m11();
			message += "," + rotation.m12();
			message += "," + rotation.m13();
			message += "," + rotation.m20();
			message += "," + rotation.m21();
			message += "," + rotation.m22();
			message += "," + rotation.m23();
			message += "," + rotation.m30();
			message += "," + rotation.m31();
			message += "," + rotation.m32();
			message += "," + rotation.m33();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server of the local avatar's position. The server then
	// forwards this message to the client with the ID value matching remoteId.
	// This message is generated in response to receiving a WANTS_DETAILS message
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the
	// position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position, Matrix4f rotation) {
		try {
			String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + rotation.m00();
			message += "," + rotation.m01();
			message += "," + rotation.m02();
			message += "," + rotation.m03();
			message += "," + rotation.m10();
			message += "," + rotation.m11();
			message += "," + rotation.m12();
			message += "," + rotation.m13();
			message += "," + rotation.m20();
			message += "," + rotation.m21();
			message += "," + rotation.m22();
			message += "," + rotation.m23();
			message += "," + rotation.m30();
			message += "," + rotation.m31();
			message += "," + rotation.m32();
			message += "," + rotation.m33();

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs the server that the local avatar has changed position.
	// Message Format: (move,localId,x,y,z) where x, y, and z represent the
	// position.

	public void sendMoveMessage(Vector3f position, Matrix4f rotation, String team) {
		try {
			String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			message += "," + rotation.m00();
			message += "," + rotation.m01();
			message += "," + rotation.m02();
			message += "," + rotation.m03();
			message += "," + rotation.m10();
			message += "," + rotation.m11();
			message += "," + rotation.m12();
			message += "," + rotation.m13();
			message += "," + rotation.m20();
			message += "," + rotation.m21();
			message += "," + rotation.m22();
			message += "," + rotation.m23();
			message += "," + rotation.m30();
			message += "," + rotation.m31();
			message += "," + rotation.m32();
			message += "," + rotation.m33();
			message += "," + team;

			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Vector3f getAvLocation() {
		return game.getPlayerPosition();
	}

	public void sendBlueScore(int blueScore) {
		try {
			String message = new String("bsUpdate," + blueScore);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendRedScore(int redScore) {
		try {
			String message = new String("rsUpdate," + redScore);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void updateGhostLife(UUID ghostId) {
		try {
			String message = new String("lifeUpdate," + ghostId.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}