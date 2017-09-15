//package mas.protocol;
//
//import jade.lang.acl.ACLMessage;
//import mas.abstractAgent;
//
//public class TreasureAffectationResponder extends Protocol{
//	public static final String NAME = "TreasureAffectation";
//	private static final int WAITING_TIME = 500;
//	private boolean finished;
//	
//	public TreasureAffectationResponder(abstractAgent myAgent) {
//		super(myAgent);
//		this.finished = false;
//	}
//	
//	/**
//	 * Return a proposition if the agent if interested with the found Treasure.
//	 * Then the protocol waits for an answer. If it's positive, the current plan changes.
//	 * @param message
//	 * @return
//	 */
//	@Override
//	public ACLMessage getResponse(ACLMessage message){
//		ACLMessage answer = null;
//		
//		switch(message.getPerformative()){
//			case ACLMessage.QUERY_IF:
//				// if interesting proposal ? or propose no matter what ?
//				answer = new ACLMessage(ACLMessage.PROPOSE);
//				//answer.setContent(ratio);
//				answer.setSender(this.getMyAgent().getAID());
//				answer.addReceiver(message.getSender());
//				answer.setProtocol(TreasureAffectationResponder.NAME);
//				answer.setConversationId(message.getConversationId());
//				this.setTimeOut(TreasureAffectationResponder.WAITING_TIME);
//				break;
//			case ACLMessage.CONFIRM:
//				// this.getMyAgent().setPlan()
//				this.finished = true;
//				break;
//			case ACLMessage.REFUSE:
//				this.finished = true;
//				break;
//		}
//		
//
//		return answer;
//	}
//	
//	@Override
//	public boolean finished() {
//		return finished;
//	}
//
//	@Override
//	public boolean checkTimeOut() {
//		if (this.getTimeOut() <= System.currentTimeMillis()){
//			this.finished = true;
//		}
//		
//		return !finished;
//	}
//
//}
