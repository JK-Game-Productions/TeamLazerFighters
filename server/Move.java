
import java.util.UUID;

import javax.vecmath.Vector3f;

import tage.ai.behaviortrees.BTCondition;

public class Move extends BTCondition {
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;

    public Move(NPC n) {
        super(false);
        npc = n;
    }

    protected boolean check() {

        return false;
    }
}
