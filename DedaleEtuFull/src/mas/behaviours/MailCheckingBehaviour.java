package mas.behaviours;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.abstractAgent;
import mas.agents.ExplorationAgent;
import mas.protocol.ConflictProtocol;
import mas.protocol.EndProtocol;
import mas.protocol.Protocol;
import mas.protocol.ShareProtocol;

public class MailCheckingBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -5759810713575585403L;
	private ExplorationAgent myAgent;
	private HashMap<String, Protocol> protocols; // conversationID -> protocol
	private boolean finished;

	public MailCheckingBehaviour(ExplorationAgent myAgent) {
		super(myAgent);
		finished = false;
		protocols = myAgent.getProtocols();
		this.myAgent = myAgent;
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive();
		finished = ((msg == null) && (protocols.isEmpty()));
		
		// If a message has been received
		if(msg != null){
			Protocol currentProtocol = null;

			// Existing protocol
			if(protocols.containsKey(msg.getConversationId())){
				currentProtocol = protocols.get(msg.getConversationId());
			}

			// New protocol
			else if(this.myAgent.acceptNewProtocol(msg)){
				// Conflict
				if(msg.getProtocol().equals(ConflictProtocol.class.getName())){
					((ExplorationAgent) this.myAgent).getWorld().setPositionCounter(0);
					currentProtocol = new ConflictProtocol((ExplorationAgent)this.myAgent);
					currentProtocol.setConversationID(msg.getConversationId());
					protocols.put(currentProtocol.getConversationID(), currentProtocol);
				}

				// Share
				else if(msg.getProtocol().equals(ShareProtocol.class.getName())){
					currentProtocol = new ShareProtocol((ExplorationAgent)this.myAgent);
					if(((ShareProtocol) currentProtocol).accept(msg)){
						((ShareProtocol) currentProtocol).init(msg);
						protocols.put(currentProtocol.getConversationID(), currentProtocol);
					}
				}

				// End
				else if(msg.getProtocol().equals(EndProtocol.class.getName())){
						finished = true;
						myAgent.getWorld().setFinished(true);
				}


			}

			if(currentProtocol != null){
				// Send the differents answers
				List<ACLMessage> answers = currentProtocol.answer(msg);
				if(!answers.isEmpty()){
					for(ACLMessage answer:answers)
						((abstractAgent)myAgent).sendMessage(answer);
				}

				// Check if protocol is done
				if (currentProtocol.finished()){
					protocols.remove(msg.getConversationId());
				}
			}
		}

		// update list of protocols depending of their timeout
		this.checkProtocols();

	}



	/**
	 * this method verify that all of the timeout are not outdated,
	 * else, the protocol is removed
	 */
	private void checkProtocols(){
		Set<String> ids = new HashSet<String>(protocols.keySet());

		for(String id : ids){
			protocols.get(id).update();
			if(protocols.get(id).finished()){
				protocols.remove(id);
			}
		}
	}

//	private void checkProtocolsState(){
//		Set<String> ids = new HashSet<String>(protocols.keySet());
//
//		for(String id : ids){
//			protocols.get(id).checkTimeOut();
//
//			switch(protocols.get(id).getName()){
//			case TreasureAffectationInitiator.NAME: envoi de rÃ©ponses
//			default: suppression du protocole
//			}
//
//		}
//
//	}

	@Override
	public boolean done() {
		return finished;
	}

}


