import tage.ai.behaviortrees.BTCondition;

public class GetSmall extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameAIServerUDP server;

    public GetSmall(NPC n) {
        super(false);
        npc = n;
    }

    protected boolean check() {
        return false;
    }
}
