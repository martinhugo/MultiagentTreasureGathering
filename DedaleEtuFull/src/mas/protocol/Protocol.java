package mas.protocol;

import java.util.List;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.agents.ExplorationAgent;

public abstract class Protocol {
	protected String convID;
	protected ExplorationAgent myAgent;
	protected AID dest;
	
	public Protocol(ExplorationAgent myAgent){
		this.myAgent = myAgent;
	}

	public abstract List<ACLMessage> answer(ACLMessage message);
	public abstract boolean finished();
	public abstract void update();
	
	public String getConversationID() {
		return convID;
	}
	
	public void setConversationID(String id){
		this.convID = id;
	}
	
	public abstractAgent getMyAgent(){
		return myAgent;
	}
	
	public AID getDest(){
		return dest;
	}

	
}
	