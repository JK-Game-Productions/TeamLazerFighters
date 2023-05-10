import java.io.IOException;
import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer {
	private GameAIServerUDP UDPServer;
	private NPCcontroller npcCtrl;

	public NetworkingServer(int serverPort, String protocol) {
		npcCtrl = new NPCcontroller();

		try {
			if (protocol.toUpperCase().compareTo("TCP") == 0) {
				GameServerTCP thisTCPServer = new GameServerTCP(serverPort);
			} else {
				// GameServerUDP thisUDPServer = new GameServerUDP(serverPort);
				UDPServer = new GameAIServerUDP(serverPort, npcCtrl);
			}
		} catch (IOException e) {
			System.out.println("server didn't start");
			e.printStackTrace();
		}
		System.out.println("\n*** Starting " + protocol + " server on port " + serverPort + " ***");
		npcCtrl.start(UDPServer);

	}

	public static void main(String[] args) {
		if (args.length > 1) {
			NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
			System.out.println("Starting " + app);
			System.out.println("\n*** " + args[1] + " Server running on port " + args[0] + " ***");
		}
	}

}
