package mas.protocol;

import java.util.ArrayList;
import java.util.List;

import jade.lang.acl.ACLMessage;
import mas.agents.ExplorationAgent;

public class EndProtocol extends Protocol{
	
	public EndProtocol(ExplorationAgent myAgent) {
		super(myAgent);
	}

	@Override
	public List<ACLMessage> answer(ACLMessage message) {
		ArrayList<ACLMessage> answers = new ArrayList<ACLMessage>();
		ACLMessage answerMessage = new ACLMessage(ACLMessage.INFORM);
		answerMessage.setProtocol(EndProtocol.class.getName());
		answerMessage.setSender(myAgent.getAID());
		answerMessage.addReceiver(message.getSender());
		answers.add(answerMessage);
		return answers;
	}

	@Override
	public boolean finished() {
		return true;
	}

	@Override
	public void update() {
	}

}
