import tage.ai.behaviortrees.BTCondition;

public class GetBig extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameAIServerUDP server;

    public GetBig(NPC n) {
        super(false);
        npc = n;
    }

    protected boolean check() {
        return false;
    }
}
