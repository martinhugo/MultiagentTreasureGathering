package mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import mas.agents.ExplorationAgent;
import mas.protocol.ConflictProtocol;

public class ConflictBehaviour extends OneShotBehaviour{
	private ExplorationAgent myAgent;
	private static final long serialVersionUID = -6907960017150620565L;

	public ConflictBehaviour(ExplorationAgent myAgent){
		super(myAgent);
		this.myAgent = myAgent;
	}

	@Override
	public void action(){
		ConflictProtocol conflictProtocol = new ConflictProtocol(myAgent);
		myAgent.sendMessage(conflictProtocol.getInitiationMessage());
		myAgent.getProtocols().put(conflictProtocol.getConversationID(), conflictProtocol);
		myAgent.getWorld().setPositionCounter(0);
	}
}