import java.io.IOException;
import tage.networking.IGameConnection.ProtocolType;

public class NetworkingServer {
	private GameServerUDP UDPServer;
	private GameServerTCP TCPServer;
	private NPCcontroller npcCtrl;

	public NetworkingServer(int serverPort, String protocol) {
		npcCtrl = new NPCcontroller();

		try {
			if (protocol.toUpperCase().compareTo("TCP") == 0) {
				TCPServer = new GameServerTCP(serverPort);
			} else {
				// GameServerUDP thisUDPServer = new GameServerUDP(serverPort);
				System.out.println("\n*** Starting " + protocol + " server on port " + serverPort + " ***");
				UDPServer = new GameServerUDP(serverPort, npcCtrl);
				npcCtrl.start(UDPServer);
			}
		} catch (IOException e) {
			System.out.println("server didn't start");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
	}

}
