import tage.ai.behaviortrees.BTCondition;

public class GetSmall extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;

    public GetSmall(NPC n) {
        super(false);
        npc = n;
        n.getSmall();// ??
    }

    protected boolean check() {
        // server.sendNPCinfo();
        return false;
    }
}
