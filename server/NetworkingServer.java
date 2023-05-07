import java.io.IOException;

public class NetworkingServer {
	private GameAIServerUDP UDPServer;
	private NPCcontroller npcCtrl;

	public NetworkingServer(int serverPort, String protocol) {
		npcCtrl = new NPCcontroller();

		try {
			UDPServer = new GameAIServerUDP(serverPort, npcCtrl);

			if (protocol.toUpperCase().compareTo("TCP") == 0) {
				new GameServerTCP(serverPort);
			} else {
				new GameServerUDP(serverPort);
			}
		} catch (IOException e) {
			System.out.println("server didn't start");
			e.printStackTrace();
		}
		npcCtrl.start(UDPServer);
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			// NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]),
			// args[1]);
			System.out.println("\n*** " + args[1] + " Server running on port " + args[0] + " ***");
		}
	}

}
