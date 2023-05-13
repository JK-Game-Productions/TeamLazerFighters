
import tage.ai.behaviortrees.BTCondition;

public class Move extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;

    public Move(NPC n) {
        super(false);
        npc = n;

        // n.lookAt();
        // n.setLocation(null);
    }

    protected boolean check() {
        server.sendWantsDetailsMessages(null);
        // server.sendNPCinfo();
        return false;
    }
}
