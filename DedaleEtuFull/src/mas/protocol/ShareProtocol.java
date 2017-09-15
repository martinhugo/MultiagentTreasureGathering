package mas.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import mas.agents.ExplorationAgent;
import mas.beliefs.Agent;
import mas.beliefs.World;

public class ShareProtocol extends Protocol{
	private boolean initiator;
	private ShareProtocolState state = ShareProtocolState.SYNC;
	private long timeOut;
	private static final int WAITING_TIME = 500;
	private static final long CLOCK_DELTA = 20;
	private long currentClockSender = 0;


	public ShareProtocol(ExplorationAgent myAgent){
		super(myAgent);
	}

	public void init(ACLMessage msg){
		if(msg.getPerformative() == ACLMessage.PROPOSE){
			this.setConversationID(System.currentTimeMillis() + " " + this.getMyAgent().getAID() + " " + msg.getSender());
			this.initiator = false;
		}
		else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
			this.setConversationID(msg.getConversationId());
			this.initiator = true;
		}

		this.dest = msg.getSender();
	}

	@Override
	public ArrayList<ACLMessage> answer(ACLMessage  message){
		ArrayList<ACLMessage> answers = new ArrayList<ACLMessage>();

		if(state == ShareProtocolState.SYNC){
			ACLMessage msg =getSyncResponse(message);
			if(msg != null){
				answers.add(msg);
			}
		}
		else if(state == ShareProtocolState.INFORM){
			ACLMessage msg = getInformResponse(message);

			if(msg != null){
				answers.add(msg);
			}
		}

		return answers;
	}

	/**
	 * Return the response of the given message during the SYNC phase.
	 * @param message the message to answer
	 * @return the response to the given message
	 */
	public ACLMessage getSyncResponse(ACLMessage message){

		int perf = message.getPerformative();
		ACLMessage answer;
		AID sender = message.getSender();
		World world = ((ExplorationAgent) this.myAgent).getWorld();

		switch(perf){
			case ACLMessage.PROPOSE:
				answer = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				//envoi de mon horloge
				try {
					answer.setContentObject(world.getMySelf().getTick());
				} catch (IOException e) {
					e.printStackTrace();
				}

				this.timeOut = System.currentTimeMillis() + WAITING_TIME;
				state = ShareProtocolState.INFORM;
				break;

			case ACLMessage.ACCEPT_PROPOSAL:
				answer = new ACLMessage(ACLMessage.INFORM);

				try {
					answer.setContentObject(((ExplorationAgent) this.getMyAgent()).getWorld().getSendableWorld(myAgent));
				} catch (IOException e) {
					e.printStackTrace();
				}

				state = ShareProtocolState.INFORM;
				this.timeOut = System.currentTimeMillis() + WAITING_TIME;
				break;

			default:
				System.out.println((message.getPerformative() == ACLMessage.INFORM) + " " + this.getConversationID() + " " + message.getConversationId());
				answer = null;
				break;
		}


		if(answer!=null){
			if(world.getAgents().containsKey(sender)){
				this.currentClockSender = world.getAgents().get(sender).getTick();
			}
			answer.setConversationId(this.getConversationID());
			answer.addReceiver(this.dest);
			answer.setSender(this.getMyAgent().getAID());
			answer.setProtocol(ShareProtocol.class.getName());
		}

		return answer;
	}

	/**
	 * Return the response of the given message during the INFORM phase.
	 * @param message the message to answer
	 * @return the response to the given message
	 */
	public ACLMessage getInformResponse(ACLMessage message){
		ACLMessage msg = null;
		if(message.getPerformative() == ACLMessage.INFORM){
			if(! initiator){
				msg = new ACLMessage(ACLMessage.INFORM);
				try {
					msg.setContentObject(((ExplorationAgent) this.getMyAgent()).getWorld().getSendableWorld(myAgent));
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg = new ACLMessage(ACLMessage.CONFIRM);
			}

			// Update our world representation
			try {
				((ExplorationAgent) this.getMyAgent()).getWorld().sync((World) message.getContentObject());
			}
			catch(UnreadableException e) {
				e.printStackTrace();
			}
		}

		if(msg!=null){
			msg.setConversationId(this.getConversationID());
			msg.addReceiver(this.dest);
			msg.setProtocol(ShareProtocol.class.getName());
			msg.setSender(this.getMyAgent().getAID());
		}

		state = ShareProtocolState.DONE;
		System.out.println("Share protocol is done between " + message.getSender().getLocalName() + " and " + this.myAgent.getLocalName() + "\n");

		return msg;
	}

	public boolean finished(){
		return state == ShareProtocolState.DONE;
	}

	public void update(){
		if (this.timeOut <= System.currentTimeMillis()){
			this.state = ShareProtocolState.DONE;
		}

	}


	/**
	 * Returns true if the agent accept the communication
	 * False otherwise
	 * @param message
	 * @return
	 */
	public boolean accept(ACLMessage message){
		// recuperation de la connaissance de l'agent envoyeur
		HashMap<AID, Agent> listAgents = ((ExplorationAgent) this.getMyAgent()).getWorld().getAgents();
		Agent agentSender = null;
		if(listAgents.containsKey(message.getSender())){
			agentSender = listAgents.get(message.getSender());
		}
		if((message.getPerformative() == ACLMessage.PROPOSE) || (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)){
			try {
				currentClockSender = (long) message.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}

		// si agent jamais rencontre ou si informations suffisamment interressantes
		//System.out.println(currentClockSender + " " + agentSender.getTick());
		return ((agentSender == null) || (currentClockSender - agentSender.getTick() >= CLOCK_DELTA));
	}

	/**
	 * Create the initiation message.
	 * The message is meant to be sent to everyone in the near neighborhood, with a specific broadcast conversation ID.
	 * When an initiation message is received, the two agents creates an other protocol to exchange their information
	 * @return the ACLMessage used to initiate the exchange protocol
	 */
	public ACLMessage getInitiationMessage(){
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		this.setConversationID("BROADCAST SHARING " + this.getMyAgent().getAID() + " - " + System.currentTimeMillis());

		msg.setConversationId(this.getConversationID());
		this.timeOut = System.currentTimeMillis() + WAITING_TIME;
		msg.setSender(this.getMyAgent().getAID());
		try {
			msg.setContentObject(myAgent.getWorld().getMySelf().getTick());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Adding all the agents
		Set<AID> keyAgents = ((ExplorationAgent) this.getMyAgent()).getWorld().getAgents().keySet();

		for(AID id: keyAgents){
			if (!this.getMyAgent().getAID().equals(id)){
				msg.addReceiver(id);
			}
		}

		msg.setProtocol(ShareProtocol.class.getName());
		return msg;
	}
}

enum ShareProtocolState{
	SYNC, INFORM, DONE;
}
