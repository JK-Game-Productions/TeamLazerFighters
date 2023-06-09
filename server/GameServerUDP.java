import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServerUDP extends GameConnectionServer<UUID> {
	NPCcontroller npcCtrl;

	public GameServerUDP(int localPort, NPCcontroller npc) throws IOException {
		super(localPort, ProtocolType.UDP);
		npcCtrl = npc;
	}

	// ------------- Additional protocols for NPCs ------------- //
	public void sendCheckForAvatarNear() {
		try {
			String message = new String("isnear");
			message += "," + (npcCtrl.getNPC()).getX();
			message += "," + (npcCtrl.getNPC()).getY();
			message += "," + (npcCtrl.getNPC()).getZ();
			message += "," + (npcCtrl.getCriteria());
			sendPacketToAll(message);
		} catch (IOException e) {
			System.out.println("couldn't send msg");
			e.printStackTrace();
		}
	}

	public void sendNPCinfo(UUID clientID) {
		try {
			String message = new String("mnpc," + clientID.toString());
			message += "," + (npcCtrl.getNPC()).getX();
			message += "," + (npcCtrl.getNPC()).getY();
			message += "," + (npcCtrl.getNPC()).getZ();
			// message += "," + (npcCtrl.getCriteria());
			sendPacketToAll(message);
		} catch (IOException e) {
			System.out.println("couldn't send msg");
			e.printStackTrace();
		}
	}

	public void sendNPCstart(UUID clientID) {
		try {
			String message = new String("createNPC," + clientID.toString());
			message += "," + (npcCtrl.getNPC()).getX();
			message += "," + (npcCtrl.getNPC()).getY();
			message += "," + (npcCtrl.getNPC()).getZ();
			// message += "," + (npcCtrl.getCriteria());
			sendPacketToAll(message);
		} catch (IOException e) {
			System.out.println("couldn't send msg");
			e.printStackTrace();
		}
	}

	public void handleNearTiming(UUID clientID) {
		npcCtrl.setNearFlag(true);
	}

	@Override
	public void processPacket(Object o, InetAddress senderIP, int senderPort) {
		String message = (String) o;
		String[] messageTokens = message.split(",");

		if (messageTokens.length > 0) {
			// JOIN -- Case where client just joined the server
			// Received Message Format: (join,localId)
			if (messageTokens[0].compareTo("join") == 0) {
				try {
					IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					sendJoinedMessage(clientID, true);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// BYE -- Case where clients leaves the server
			// Received Message Format: (bye,localId)
			if (messageTokens[0].compareTo("bye") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
			}

			// CREATE -- Case where server receives a create message (to specify avatar
			// location)
			// Received Message Format: (create,localId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String[] posRot = { messageTokens[2], messageTokens[3], messageTokens[4],
						messageTokens[5], messageTokens[6], messageTokens[7], messageTokens[8],
						messageTokens[9], messageTokens[10], messageTokens[11], messageTokens[12],
						messageTokens[13], messageTokens[14], messageTokens[15], messageTokens[16],
						messageTokens[17], messageTokens[18], messageTokens[19], messageTokens[20] };
				sendCreateMessages(clientID, posRot);
				sendWantsDetailsMessages(clientID);

				sendNPCstart(clientID);// need this in the create call
			}

			// DETAILS-FOR --- Case where server receives a details for message
			// Received Message Format: (dsfr,remoteId,localId,x,y,z)
			if (messageTokens[0].compareTo("dsfr") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				UUID remoteID = UUID.fromString(messageTokens[2]);
				String[] posRot = { messageTokens[3], messageTokens[4], messageTokens[5],
						messageTokens[6], messageTokens[7], messageTokens[8], messageTokens[9],
						messageTokens[10], messageTokens[11], messageTokens[12], messageTokens[13],
						messageTokens[14], messageTokens[15], messageTokens[16], messageTokens[17],
						messageTokens[18], messageTokens[19], messageTokens[20], messageTokens[21] };
				sendDetailsForMessage(clientID, remoteID, posRot);
			}

			// MOVE --- Case where server receives a move message
			// Received Message Format: (move,localId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String[] posRot = { messageTokens[2], messageTokens[3], messageTokens[4],
						messageTokens[5], messageTokens[6], messageTokens[7], messageTokens[8],
						messageTokens[9], messageTokens[10], messageTokens[11], messageTokens[12],
						messageTokens[13], messageTokens[14], messageTokens[15], messageTokens[16],
						messageTokens[17], messageTokens[18], messageTokens[19], messageTokens[20], messageTokens[21] };
				sendMoveMessages(clientID, posRot);
			}

			// --------------- NPC SECTION --------------- //

			// Case where server receives request for NPCs
			// Received Message Format: (needNPC,id)//and x,y,z
			if (messageTokens[0].compareTo("needNPC") == 0) {
				System.out.println("server got a needNPC message");
				UUID clientID = UUID.fromString(messageTokens[1]);
				sendNPCstart(clientID);
			}

			// Case where server receives notice that an av is close to the npc
			// Received Message Format: (isnear,id)
			if (messageTokens[0].compareTo("isnear") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				System.out.println("isnear = true");
				handleNearTiming(clientID);
			}

			// MOVE NPC --- Case where server receives a move NPC message
			// Received Message Format: (mnpc,localId,x,y,z)
			if (messageTokens[0].compareTo("mnpc") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
				sendMoveNPCMessages(clientID, pos);
			}

			if (messageTokens[0].compareTo("bsUpdate") == 0) {
				String blueScore = messageTokens[1];
				sendBlueScore(blueScore);
			}
			if (messageTokens[0].compareTo("rsUpdate") == 0) {
				String redScore = messageTokens[1];
				sendRedScore(redScore);
			}
			if (messageTokens[0].compareTo("lifeUpdate") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				sendLifeUpdate(clientID);
			}
		}
	}

	// ----------------- SENDING NPC MESSAGES ----------------- //

	// send client npc location
	public void sendMoveNPCMessages(UUID clientID, String[] position) {
		try {
			String message = new String("mnpc," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs clients of the whereabouts of the NPCs.
	public void sendCreateNPCmsg(UUID clientID, String[] position) {
		try {
			System.out.println("server telling clients about an NPC");
			String message = new String("createNPC," + clientID.toString());
			// message += "," + position[0];
			// message += "," + position[1];
			// message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * UUID clientID = UUID.fromString(messageTokens[1]);
	 * String[] pos = { messageTokens[2], messageTokens[3], messageTokens[4] };
	 * sendCreateMessages(clientID, pos);
	 * sendWantsDetailsMessages(clientID);
	 * 
	 * sendNPCstart(clientID);
	 */

	// Informs the client who just requested to join the server if their if their
	// request was able to be granted.
	// Message Format: (join,success) or (join,failure)

	public void sendJoinedMessage(UUID clientID, boolean success) {
		try {
			System.out.println("trying to confirm join");
			String message = new String("join,");
			if (success)
				message += "success";
			else
				message += "failure";
			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client that the avatar with the identifier remoteId has left the
	// server.
	// This message is meant to be sent to all client currently connected to the
	// server
	// when a client leaves the server.
	// Message Format: (bye,remoteId)

	public void sendByeMessages(UUID clientID) {
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client that a new avatar has joined the server with the unique
	// identifier
	// remoteId. This message is intended to be send to all clients currently
	// connected to
	// the server when a new client has joined the server and sent a create message
	// to the
	// server. This message also triggers WANTS_DETAILS messages to be sent to all
	// client
	// connected to the server.
	// Message Format: (create,remoteId,x,y,z) where x, y, and z represent the
	// position

	public void sendCreateMessages(UUID clientID, String[] position) {
		try {
			String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + position[3];
			message += "," + position[4];
			message += "," + position[5];
			message += "," + position[6];
			message += "," + position[7];
			message += "," + position[8];
			message += "," + position[9];
			message += "," + position[10];
			message += "," + position[11];
			message += "," + position[12];
			message += "," + position[13];
			message += "," + position[14];
			message += "," + position[15];
			message += "," + position[16];
			message += "," + position[17];
			message += "," + position[18];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client of the details for a remote client�s avatar. This message is
	// in response
	// to the server receiving a DETAILS_FOR message from a remote client. That
	// remote client�s
	// message�s localId becomes the remoteId for this message, and the remote
	// client�s message�s
	// remoteId is used to send this message to the proper client.
	// Message Format: (dsfr,remoteId,x,y,z) where x, y, and z represent the
	// position.

	public void sendDetailsForMessage(UUID clientID, UUID remoteId, String[] position) {
		try {
			String message = new String("dsfr," + remoteId.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + position[3];
			message += "," + position[4];
			message += "," + position[5];
			message += "," + position[6];
			message += "," + position[7];
			message += "," + position[8];
			message += "," + position[9];
			message += "," + position[10];
			message += "," + position[11];
			message += "," + position[12];
			message += "," + position[13];
			message += "," + position[14];
			message += "," + position[15];
			message += "," + position[16];
			message += "," + position[17];
			message += "," + position[18];
			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a local client that a remote client wants the local client�s avatar�s
	// information.
	// This message is meant to be sent to all clients connected to the server when
	// a new client
	// joins the server.
	// Message Format: (wsds,remoteId)

	public void sendWantsDetailsMessages(UUID clientID) {
		try {
			String message = new String("wsds," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Informs a client that a remote client�s avatar has changed position. x, y,
	// and z represent
	// the new position of the remote avatar. This message is meant to be forwarded
	// to all clients
	// connected to the server when it receives a MOVE message from the remote
	// client.
	// Message Format: (move,remoteId,x,y,z) where x, y, and z represent the
	// position.

	public void sendMoveMessages(UUID clientID, String[] position) {
		try {
			String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + position[3];
			message += "," + position[4];
			message += "," + position[5];
			message += "," + position[6];
			message += "," + position[7];
			message += "," + position[8];
			message += "," + position[9];
			message += "," + position[10];
			message += "," + position[11];
			message += "," + position[12];
			message += "," + position[13];
			message += "," + position[14];
			message += "," + position[15];
			message += "," + position[16];
			message += "," + position[17];
			message += "," + position[18];
			message += "," + position[19];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendBlueScore(String bs) {
		try {
			String message = new String("bsUpdate," + bs);
			sendPacketToAll(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendRedScore(String rs) {
		try {
			String message = new String("rsUpdate," + rs);
			sendPacketToAll(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void sendLifeUpdate(UUID clientID) {
		try {
			String message = new String("lifeUpdate," + clientID.toString());
			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}