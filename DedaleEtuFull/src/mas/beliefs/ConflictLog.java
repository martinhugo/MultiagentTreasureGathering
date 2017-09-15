package mas.beliefs;

import java.io.Serializable;

import jade.core.AID;

public class ConflictLog implements Serializable{

	private static final long serialVersionUID = 669124649941326939L;
	private String conflictID;
	private AID agentID;
	public String getConflictID() {
		return conflictID;
	}
	public void setConflictID(String conflictID) {
		this.conflictID = conflictID;
	}
	public AID getAgentID() {
		return agentID;
	}
	public void setAgentID(AID agentID) {
		this.agentID = agentID;
	}
	public ConflictLog(String conflictID, AID agentID) {
		super();
		this.conflictID = conflictID;
		this.agentID = agentID;
	}


}
