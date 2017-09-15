//package mas.protocol;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//import jade.core.AID;
//import jade.lang.acl.ACLMessage;
//import mas.agents.ExplorationAgent;
//import mas.beliefs.FloodTree;
//
//public class FloodSynchroProtocol extends Protocol{
//	private FloodTree tree;
//	private long timeout;
//	private FloodState state;
//	public static final String name = "FloodSynchroProtocol";
//	private static final int WAITING_TIME = 400;
//			
//	public FloodSynchroProtocol(ExplorationAgent myAgent){
//		super(myAgent);
//		this.tree = myAgent.getWorld().getFloodTree();
//		this.tree.reset();
//	}
//	
//	public FloodSynchroProtocol(ExplorationAgent myAgent, boolean initiator){
//		this(myAgent);
//		if(initiator){
//			String id = this.getMyAgent().getLocalName() + System.currentTimeMillis();
//			this.tree.setID(id);
//			this.tree.setRoot(myAgent.getAID());
//			this.setConversationID(id);
//		}
//	}
//	
//	
//	/**** ANSWERS GENERATION ****/
//	
//	/**
//	 * Returns the answers to the given message. 
//	 * The tree is updated if needed and the answers are returned.
//	 * @param msg the received message
//	 * @return the answers to the received message
//	 */
//	public List<ACLMessage> answer(ACLMessage msg){
//		ArrayList<ACLMessage> answers = new ArrayList<ACLMessage>();
//		
//		switch(msg.getPerformative()){
//			case ACLMessage.PROPAGATE:
//				System.out.println("Propagate received " + this.getMyAgent().getLocalName());
//				this.tree.setID(msg.getConversationId());
//				answers.addAll(this.getPropagateAnswers(msg));
//				break;
//				
//			case ACLMessage.CONFIRM:
//				System.out.println("Confirm received " + this.getMyAgent().getLocalName());
//				tree.add(this.getMyAgent().getAID(), msg.getSender());
//				answers.add(getConfirmAnswer(msg));
//				break;
//				
//			case ACLMessage.SUBSCRIBE:
//				System.out.println("SYNCHRONYZED " + this.getMyAgent().getLocalName());
//				tree.add(msg.getSender(), this.getMyAgent().getAID());
//				
//				this.state =  FloodState.SYNCHRONIZED;
//				break;
//				
//			case ACLMessage.CANCEL:
//				answers.add(this.getCancelAnswer(msg));
//				endProtocol();
//				break;
//		}
//		
//		return answers;
//	}
//	
//	/**
//	 * Return all the asnwers to a propagate message
//	 * @param floodMsg the initiation message
//	 */
//	public List<ACLMessage> getPropagateAnswers(ACLMessage floodMsg){
//		ArrayList<ACLMessage> answers = new ArrayList<ACLMessage>();
//		
//		ACLMessage confirmMessage = new ACLMessage(ACLMessage.CONFIRM);
//		confirmMessage.addReceiver(floodMsg.getSender());
//		confirmMessage.setSender(this.getMyAgent().getAID());
//		confirmMessage.setConversationId(floodMsg.getConversationId());
//		confirmMessage.setProtocol(floodMsg.getProtocol());
//		answers.add(confirmMessage);
//		
//		floodMsg.setSender(this.getMyAgent().getAID());
//		floodMsg.removeReceiver(this.getMyAgent().getAID());
//		answers.add(floodMsg);
//		
//		timeout = WAITING_TIME + System.currentTimeMillis();
//		return answers;
//	}
//	
//	/**
//	 * Return the answer to a confirm message. 
//	 * @param confirmMsg the confirm message
//	 */
//	public ACLMessage getConfirmAnswer(ACLMessage confirmMsg){
//		ACLMessage answer = new ACLMessage(ACLMessage.SUBSCRIBE);
//		
//		try {
//			answer.setContentObject(tree);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		answer.setSender(this.getMyAgent().getAID());
//		answer.addReceiver(confirmMsg.getSender());
//		answer.setConversationId(confirmMsg.getConversationId());
//		answer.setProtocol(confirmMsg.getProtocol());
//		return answer;
//	}
//	
//	/**
//	 * Return the answer to a propagate message
//	 * @param cancelMsg the cancel message
//	 */
//	public ACLMessage getCancelAnswer(ACLMessage cancelMsg){
//		ACLMessage answer = new ACLMessage(ACLMessage.CANCEL);
//		answer.setSender(this.getMyAgent().getAID());
//		//for(AID receivers:tree.getReceivers()){
//			//answer.addReceiver(confirmMsg.getSender());
//		//}
//		answer.setConversationId(cancelMsg.getConversationId());
//		answer.setProtocol(cancelMsg.getProtocol());
//		return answer;
//	}
//	
//	/** 
//	 * Return the initiation message of the protocol
//	 * @return the initiation message
//	 */
//	public ACLMessage getInitiationMessage(){
//		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);
//		msg.setConversationId(this.getConversationID());
//		msg.setProtocol(FloodSynchroProtocol.name);		
//		msg.setSender(this.getMyAgent().getAID());
//		
//		// Adding all the agents.
//		
//		Set<AID> keyAgents = ((ExplorationAgent) this.getMyAgent()).getWorld().getAgents().keySet();
//		for(AID id: keyAgents){
//			if (!this.getMyAgent().getAID().equals(id)){
//				msg.addReceiver(id);
//			}
//		}
//		
//		timeout = WAITING_TIME + System.currentTimeMillis();
//		return msg;
//	}
//	
//	/*** STATE MANAGEMENT **/
//	
//	public void endProtocol(){
//		state = FloodState.FINISHED;
//		tree.reset();
//	}
//	/**
//	 * @return true if the protocol if synchronized, false otherwise
//	 */
//	public boolean isSynchronized(){
//		return this.state == FloodState.SYNCHRONIZED;
//	}
//	
//	/**
//	 * Checks for any update of the internal state of the protocol
//	 */
//	public void update(){ 
//		if((state != FloodState.SYNCHRONIZED) && (timeout <= System.currentTimeMillis())){
//			endProtocol();
//		}
//	}
//	
//	
//	/**
//	 * @return true if the protocol is finished, false otherwise
//	 */
//	@Override
//	public boolean finished() {
//		return this.state == FloodState.FINISHED;
//	}
//
//
//}
//
//enum FloodState{
//	INITIATED, SYNCHRONIZED, FINISHED;
//}
