package client;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;

public class GhostManager {
	private MyGame game;
	private Vector<GhostAvatar> ghostAvatars = new Vector<GhostAvatar>();
	private Vector<GhostNPC> ghostNPCs = new Vector<GhostNPC>();

	public GhostManager(VariableFrameRateGame vfrg) {
		game = (MyGame) vfrg;
	}

	public void createGhostAvatar(UUID id, Vector3f position) throws IOException {
		System.out.println("adding ghost with ID --> " + id);
		ObjShape s = game.getGhostShape();
		TextureImage t = game.getGhostTexture();
		GhostAvatar newAvatar = new GhostAvatar(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(0.43f);// 0.25
		newAvatar.setLocalScale(initialScale);
		ghostAvatars.add(newAvatar);
	}

	public void createGhostNPC(UUID id, Vector3f pos) throws IOException {
		System.out.println("adding npc ghost with ID --> " + id);
		ObjShape s = game.getNPCshape();
		TextureImage t = game.getGhostTexture();
		GhostNPC newNPC = new GhostNPC(id, s, t, pos);
		newNPC.setLocalScale(new Matrix4f().scaling(.43f));
		ghostNPCs.add(newNPC);
	}

	public void removeGhostAvatar(UUID id) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null) {
			game.getEngine().getSceneGraph().removeGameObject(ghostAvatar);
			ghostAvatars.remove(ghostAvatar);
		} else {
			System.out.println("tried to remove, but unable to find ghost in list");
		}
	}

	private void removeGhostNPC(UUID id) {
		GhostNPC ghostNPC = findNPC(id);
		if(ghostNPC != null) {
			game.getEngine().getSceneGraph().removeGameObject(ghostNPC);
			ghostNPCs.remove(ghostNPC);
		} else {
			System.out.println("tried to remove, unable to find ghost in list");
		}
	}

	private GhostAvatar findAvatar(UUID id) {
		GhostAvatar ghostAvatar;
		Iterator<GhostAvatar> it = ghostAvatars.iterator();
		while (it.hasNext()) {
			ghostAvatar = it.next();
			if (ghostAvatar.getID().compareTo(id) == 0) {
				return ghostAvatar;
			}
		}
		return null;
	}

	private GhostNPC findNPC(UUID id) {
		GhostNPC ghostNPC;
		Iterator<GhostNPC> it = ghostNPCs.iterator();
		while(it.hasNext()) {
			ghostNPC = it.next();
			if(ghostNPC.getNPCid().compareTo(id) == 0) {
				return ghostNPC;
			}
		}
		return null;
	}

	public void updateGhostAvatar(UUID id, Vector3f position) {
		GhostAvatar ghostAvatar = findAvatar(id);
		if (ghostAvatar != null) {
			ghostAvatar.setPosition(position);
		} else {
			System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
		}
	}

	public void updateGhostNPC(UUID id, Vector3f pos) {
		GhostNPC ghostNPC = findNPC(id);
		if(ghostNPC != null) {
			ghostNPC.setPosition(pos);
		} else {
			System.out.println("tried to update ghost avatar position, but unable to find ghost in list");
		}
	}

	public Vector<GhostAvatar> getGhostAvatars() {
		return ghostAvatars;
	}

	public Vector<GhostNPC> getGhostNPCs() {
		return ghostNPCs;
	}
}