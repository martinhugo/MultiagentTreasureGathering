package mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.agents.ExplorationAgent;
import mas.protocol.EndProtocol;
import mas.protocol.Protocol;

public class InfiniteMailCheckingBehaviour extends CyclicBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4182651536059885288L;
	private ExplorationAgent myAgent;
	public InfiniteMailCheckingBehaviour(ExplorationAgent myAgent) {
		super(myAgent);
		this.myAgent = myAgent;
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive();
		// If a message has been received
		if((msg != null) && (msg.getProtocol() != EndProtocol.class.getName())){
			Protocol endProtocol = new EndProtocol(myAgent);
			((abstractAgent)myAgent).sendMessage(endProtocol.answer(msg).get(0));
		}
			
	}
}


