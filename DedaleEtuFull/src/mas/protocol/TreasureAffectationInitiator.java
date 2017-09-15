//package mas.protocol;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Set;
//
//import jade.core.AID;
//import jade.lang.acl.ACLMessage;
//import mas.abstractAgent;
//import mas.agents.ExplorationAgent;
//import mas.beliefs.Treasure;
//
//public class TreasureAffectationInitiator extends Protocol{
//	private HashMap<AID, Float> proposals;
//	private static final int WAITING_TIME = 500;
//	
//	public TreasureAffectationInitiator(abstractAgent myAgent) {
//		super(myAgent);
//		proposals = new HashMap<AID, Float>();
//	}
//
//	public void receiveProposal(ACLMessage message){
//		if(message.getPerformative() == ACLMessage.PROPOSE){
//			this.proposals.put(message.getSender(), Float.parseFloat(message.getContent()));
//		}
//	}
//	
//	/**
//	 * BESOIN D'UN MECANISME POUR DELAYER UNE REPONSE
//	 * @param message
//	 * @return
//	 */
//	@Override
//	public ACLMessage getResponse(ACLMessage message){
//		AID agent = this.getBestProposal();
//		if (agent != null){
//			ACLMessage positiveAnswer = new ACLMessage(ACLMessage.CONFIRM);
//			positiveAnswer.setSender(this.getMyAgent().getAID());
//			positiveAnswer.setConversationId(this.getConversationID());
//			positiveAnswer.addReceiver(agent);
//			
//			ACLMessage negativeAnswer = new ACLMessage(ACLMessage.REFUSE);
//			negativeAnswer.setSender(this.getMyAgent().getAID());
//			negativeAnswer.setConversationId(this.getConversationID());
//			
//			for(AID other:proposals.keySet()){
//				if(!agent.equals(other)){
//					negativeAnswer.addReceiver(other);
//				}
//			}
//		}
//		return null;
//	}
//	
//	public AID getBestProposal(){
//		int ratio = 0;
//		AID best = null;
//		for(AID agent: proposals.keySet()){
//			if (ratio<=proposals.get(agent)){
//				best = agent;
//			}
//		}
//		
//		return best;
//	}
//	
//	
//	@Override
//	public boolean finished() {
//		return false;
//	}
//
//	@Override
//	public boolean checkTimeOut() {
//		return false;
//	}
//	
//	public ACLMessage getInitiationMessage(){
//		
//		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
//		this.setConversationID("TREASURE FOUND - NEED ATTRIBUTION " + this.getMyAgent().getAID() + " - " + System.currentTimeMillis());
//		
//		msg.setConversationId(this.getConversationID());
//		Treasure treasure = ((ExplorationAgent) this.getMyAgent()).getWorld().getGraph().getNode(this.getMyAgent().getCurrentPosition()).getTreasure();
//		try {
//			msg.setContentObject(treasure);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		this.setTimeOut(System.currentTimeMillis() + WAITING_TIME);
//		msg.setSender(this.getMyAgent().getAID());
//		
//		// Adding all the agents
//		Set<AID> keyAgents = ((ExplorationAgent) this.getMyAgent()).getWorld().getAgents().keySet();
//		
//		for(AID id: keyAgents){
//			if (!this.getMyAgent().getAID().equals(id)){
//				msg.addReceiver(id);
//			}
//		}
//		return msg;
//	}
//
//}
