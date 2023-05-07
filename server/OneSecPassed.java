import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameAIServerUDP server;

    public OneSecPassed(NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        npcc = c;
        npc = n;
    }

    protected boolean check() {
        return false;
    }
}