import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import tage.networking.server.GameConnectionServer;

public class GameAIServerUDP extends GameConnectionServer<UUID> {
    NPCcontroller npcCtrl;

    public GameAIServerUDP(int localPort, NPCcontroller npc) throws IOException {
        super(localPort, ProtocolType.UDP);
        npcCtrl = npc;
    }

    // --- additional protocol for NPCs ----
    public void sendCheckForAvatarNear() {
        try {
            String message = new String("isnr");
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

    public void sendNPCinfo() {
    }

    public void sendNPCstart(UUID clientID) {
    }

    @Override
    public void processPacket(Object o, InetAddress senderIP, int port) {
        String message = (String) o;
        String[] messageTokens = message.split(",");

        if (messageTokens.length > 0) {
            // Case where server receives request for NPCs
            // Received Message Format: (needNPC,id)
            if (messageTokens[0].compareTo("needNPC") == 0) {
                System.out.println("server got a needNPC message");
                UUID clientID = UUID.fromString(messageTokens[1]);
                sendNPCstart(clientID);
            }
            // Case where server receives notice that an av is close to the npc
            // Received Message Format: (isnear,id)
            if (messageTokens[0].compareTo("isnear") == 0) {
                UUID clientID = UUID.fromString(messageTokens[1]);
                handleNearTiming(clientID);
            }
        }
    }

    public void handleNearTiming(UUID clientID) {
        npcCtrl.setNearFlag(true);
    }

    // ------------ SENDING NPC MESSAGES -----------------
    // Informs clients of the whereabouts of the NPCs.
    public void sendCreateNPCmsg(UUID clientID, String[] position) {
        try {
            System.out.println("server telling clients about an NPC");
            String message = new String("createNPC," + clientID.toString());
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
