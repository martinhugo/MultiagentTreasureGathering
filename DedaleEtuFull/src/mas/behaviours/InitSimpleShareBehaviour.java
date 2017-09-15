package mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import mas.agents.ExplorationAgent;
import mas.protocol.ShareProtocol;

public class InitSimpleShareBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = -2237816435251255416L;

	public InitSimpleShareBehaviour(ExplorationAgent myAgent){
		super(myAgent);
	}
	
	@Override
	public void action() {
		// If an update needs to be sent
		ShareProtocol shareProtocol = new ShareProtocol((ExplorationAgent) myAgent);
		((ExplorationAgent) myAgent).sendMessage(shareProtocol.getInitiationMessage());
		((ExplorationAgent) myAgent).getProtocols().put(shareProtocol.getConversationID(), shareProtocol);
	}
	
}