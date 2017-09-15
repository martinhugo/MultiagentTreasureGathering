package mas.protocol;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import mas.agents.ExplorationAgent;
import mas.beliefs.ConflictLog;
import mas.beliefs.Graph;
import mas.beliefs.PlanInstruction;
import mas.beliefs.World;

public class ConflictProtocol extends Protocol{
	private String nodeConflict;
	private String senderPosition;
	private String senderDestination;
	
	private ArrayList<String> planToSend;
	private boolean waiting;
	private long timeout;
	private ProtocolState state = ProtocolState.SYNC;
	private static final int WAITING_TIME = 100;

	public ConflictProtocol(ExplorationAgent myAgent){
		super(myAgent);
		waiting = false;
	}

	@Override
	public List<ACLMessage> answer(ACLMessage message){
		ArrayList<ACLMessage> answers = new ArrayList<ACLMessage>();
		ACLMessage msg = null;
		if(this.state == ProtocolState.SYNC){
			msg = getSyncResponse(message);
		}
		else if (this.state == ProtocolState.INFORM){
			msg = getInformResponse(message);
		}
		if(msg != null){
			answers.add(msg);
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
		ACLMessage answer = null;

		switch(perf){
			// reception d'un message de conflit
			case ACLMessage.REQUEST:
				// reception noeud bloquant pour l'envoyeur + position courante de l'envoyeur
				String[] positions = message.getContent().split("-");
				String pos = ((ExplorationAgent)this.getMyAgent()).getCurrentPosition();
				senderPosition = positions[1];
				senderDestination = positions[0];
				nodeConflict = this.myAgent.getWorld().getPlanManager().getMainPlan().get(0);
				this.waiting = false;
				this.dest = message.getSender();
				
				// if i'm in conflict with the agent
				if((pos.equals(senderDestination)) && (this.myAgent.acceptNewProtocol(message))){
					answer = new ACLMessage(ACLMessage.INFORM);
					try {
						// envoi du message contenant la distance au noeud le plus interessant
						ArrayList<Serializable> content = new ArrayList<Serializable>();
						String infoToSend = this.myAgent.getWorld().getPlanManager().getMainPlan().get(0) + "-" + this.getMyAgent().getCurrentPosition();
						content.add(infoToSend);
						content.add(myAgent.getWorld());
						answer.setContentObject(content);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{
					this.state = ProtocolState.DONE;
					answer = null;
				}

				break;
				
			case ACLMessage.CANCEL:

				if(message.getConversationId().equals(this.getConversationID())){
					if(waiting){
						myAgent.removeWaitingConflicts();
						waiting = false;
						myAgent.getWorld().getGraph().getNode(this.senderPosition).setConflictLog(null);
						ArrayList<String> plan = myAgent.getWorld().getGraph().getConflictPath(myAgent.getCurrentPosition());
						this.setUpPlan(plan, false);
					}
				}
		
				this.state = ProtocolState.DONE;
				answer = null;
				break;


			default:
				answer = null;
				break;
		}

		if(answer!=null){
			this.state = ProtocolState.INFORM;
			this.setTimeOut(System.currentTimeMillis() + WAITING_TIME);
			answer.setConversationId(this.getConversationID());
			answer.setProtocol(ConflictProtocol.class.getName());
			answer.addReceiver(message.getSender());
			answer.setSender(this.getMyAgent().getAID());
		}

		return answer;
	}

	private void setTimeOut(long l) {
		this.timeout = l;
	}


	/**
	 * Return the response of the given message during the INFORM phase.
	 * @param message the message to answer
	 * @return the response to the given message
	 */
	public ACLMessage getInformResponse(ACLMessage message){
		int perf = message.getPerformative();
		ACLMessage answer = null;

		switch (perf){
			case ACLMessage.INFORM:
				// creation d'un message confirm
				if(this.myAgent.acceptNewProtocol(message)){
					answer = new ACLMessage(ACLMessage.PROPOSE);
					
					// Trouver un moyen plus propre d'informer nos voisins qu'on bouge ?
					// Il faut rajouter le "CONFLICT" dans tous les plans qui écrasent le plan courant sinon peut generer un bug.
					try {
						ArrayList<Serializable> content = (ArrayList<Serializable>) message.getContentObject();
						String[] positions = (String[]) ((String) content.get(0)).split("-");
						
						// setting the protocol parameters
						senderPosition = positions[1];
						senderDestination = positions[0];
						nodeConflict = myAgent.getWorld().getPlanManager().getMainPlan().get(0);
						dest = message.getSender();
						
						// memorize the log conflict
						ConflictLog log = new ConflictLog(this.getConversationID(), message.getSender());
						myAgent.getWorld().getGraph().getNode(this.senderPosition).setConflictLog(log);
	
						// Synchronize world and taking a decision
						World other = (World) content.get(1);
						boolean move = this.moveDecision(other);
	
						// Création du message suivant
						content = new ArrayList<Serializable>();
						content.add(move);
						content.add(myAgent.getWorld());
						content.add(this.planToSend);
						answer.setContentObject(content);
	
	
						// if I can move and my path is shortest than the other agent
						if(move){
							// Reconstruction information
							this.myAgent.removeWaitingConflicts();	
							// mise en forme de l'information de mise à jour
							this.state = ProtocolState.DONE;
							this.waiting = false;
							
						}
						else{
							this.waiting = true;
							this.state = ProtocolState.SYNC;
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					((ExplorationAgent) this.getMyAgent()).getWorld().setPositionCounter(0);
				}
				else{
					this.state = ProtocolState.DONE;
					answer = null;
				}
				
				break;

			case ACLMessage.PROPOSE:
				try {
					// memorize the log conflict
					ConflictLog log = new ConflictLog(this.getConversationID(), message.getSender());
					myAgent.getWorld().getGraph().getNode(this.senderPosition).setConflictLog(log);
					
					ArrayList<Serializable> content = (ArrayList<Serializable>) message.getContentObject();
					World other = (World) content.get(1);
					this.myAgent.getWorld().sync(other);
					boolean otherIsMoving = (boolean) content.get(0);

	 				// if I can move and the other agent ask me to move
					if(otherIsMoving){
						// si il me gène j'attend, recalculer plan ??
						if(this.senderPosition.equals(myAgent.getWorld().getPlanManager().getMainPlan().get(0))){
							ArrayList<String> path = this.myAgent.getWorld().getGraph().getConflictPath(this.myAgent.getCurrentPosition());
							if(path.get(0).equals(senderPosition)){
								this.waiting = true;
								this.state = ProtocolState.SYNC;
							}
							else{
								this.setUpPlan(path, false);
								this.myAgent.removeWaitingConflicts();
								this.state = ProtocolState.DONE;
								this.waiting = false;
							}
						}
						else{
							this.state = ProtocolState.DONE;
						}
					}
					else{
						//List<String> path = this.myAgent.getWorld().getGraph().getPath(myAgent.getCurrentPosition(), null, TypePath.INTERSECTION_NODE, false);
						ArrayList<String> path = (ArrayList<String>) content.get(2);
						if(path == null){
							path = this.myAgent.getWorld().getGraph().getConflictPath(this.myAgent.getCurrentPosition());
						}
						this.setUpPlan(path, false);
						this.myAgent.removeWaitingConflicts();
						this.state = ProtocolState.DONE;
						this.waiting = false;

					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}

				((ExplorationAgent) this.getMyAgent()).getWorld().setPositionCounter(0);
				break;
//
//			case ACLMessage.REQUEST:
//				//creation d'un message inform
//				answer = new ACLMessage(ACLMessage.INFORM);
//				try {
//					answer.setContentObject(myAgent.getWorld());
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				break;


			default:
				answer = null;
				break;

		}

		if(answer!=null){
			answer.setConversationId(this.getConversationID());
			answer.setProtocol(ConflictProtocol.class.getName());
			answer.addReceiver(message.getSender());
			answer.setSender(this.getMyAgent().getAID());
			this.setTimeOut(System.currentTimeMillis() + WAITING_TIME);
		}
		return answer;
	}




	/** Decides if the agent move or not
	 * 	Returns true if the agent move, false otherwise.
	 * 	If the agent move, the corresponding moving plan is saved as an attribute or set as a conflict plan ??
	 * @param other the other agent's graph
	 * @return the move decision
	 */
	public boolean moveDecision(World other){
		boolean move = false;
		boolean pick = false;
		// Maj du plan actuel on le compare avec le plan précédent.
		ArrayList<String> plan = this.myAgent.getWorld().getPlanManager().getMainPlan() ;
		this.myAgent.getWorld().sync(other);

		// Si mon plan et ma direction ont changé
		if(!plan.equals(this.myAgent.getWorld().getPlanManager().getMainPlan())){
			// Si j'ai envie de pick sur ma position courante, informer mon plan de conflit ?
			if(this.myAgent.getWorld().getPlanManager().getMainPlan().get(0).equals(PlanInstruction.PICK.name())){
				pick = true;
			}
			// Si je ne gène plus l'autre
			else if(!this.myAgent.getWorld().getPlanManager().getMainPlan().get(0).equals(this.senderPosition)){
				move = true;
				plan = this.myAgent.getWorld().getPlanManager().getMainPlan();
			}
		}

		// Si je n'ai pas déja trouvé de plan de conflit
		if(!move){
			// Si il gène il bouge
			if(!this.myAgent.getCurrentPosition().equals(senderDestination) && !pick){
				move = false;
				//plan = this.myAgent.getWorld().getGraph().getConflictPath(this.myAgent.getCurrentPosition());
				plan = null;
			}
			// Si nous nous génons mutuellement, on décide sur place qui bouge.
			else{
				plan = this.myAgent.getWorld().getGraph().getConflictPath(this.myAgent.getCurrentPosition());
				if(plan.isEmpty()){
					plan = null;
					move = false;
				}
				else{
					move = !plan.get(0).equals(this.senderPosition);
				}
			}
		}

		// Mise en forme du plan
		if(move){
			this.setUpPlan(plan, pick);
		}
		else{
			this.planToSend = plan;
		}
		return move;
	}

	public void setUpPlan(ArrayList<String> plan, boolean pick){
		//plan = myAgent.getWorld().getPlanManager().reconstruct(plan);
		if(plan.get(0).equals(this.myAgent.getCurrentPosition())){
			plan.remove(0);
		}
		
		if(pick){
			plan.add(0, PlanInstruction.PICK.getName());
			plan.add(1, PlanInstruction.CONFLICT.getName());
		}
		else{
			plan.add(1, PlanInstruction.CONFLICT.getName());
		}
		this.myAgent.getWorld().getPlanManager().setConflictPlan(plan);
	}


	public boolean checkTimeOut(){
		if (this.getTimeOut() <= System.currentTimeMillis()){
			this.state = ProtocolState.DONE;
			((ExplorationAgent) this.getMyAgent()).getWorld().setPositionCounter(0);
			return false;
		}
		return true;
	}



	/**
	 * Create the initiation message for an existing protocol (with the given conversation ID).
	 * The message is meant to be sent to everyone in the near neighborhood, with a specific broadcast conversation ID.
	 * When an initiation message is received, the two agents creates an other protocol to exchange their information
	 * @return the ACLMessage used to initiate the exchange protocol
	 */
	public ACLMessage getInitiationMessage(String conversationID, AID dest){

		String conflictNode = this.myAgent.getWorld().getPlanManager().getMainPlan().get(0);

		//ajout du noeud bloquant � la liste des noeuds bloqu�s
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setProtocol(ConflictProtocol.class.getName());
		
		if(conversationID.equals("")){
			this.setConversationID("BROADCAST CONFLICT " + this.getMyAgent().getAID() + " - " + System.currentTimeMillis());
			
		}
		else{
			this.setConversationID(conversationID);
		}
		
		msg.setConversationId(this.getConversationID());
		//envoi noeud qui bloque + position courante
		String infoToSend = conflictNode + "-" + this.myAgent.getCurrentPosition();
		msg.setContent(infoToSend);
		this.setTimeOut(System.currentTimeMillis() + WAITING_TIME);
		msg.setSender(this.getMyAgent().getAID());

		// Adding all the receivers
		if(dest == null){
			Set<AID> keyAgents = ((ExplorationAgent) this.getMyAgent()).getWorld().getAgents().keySet();
		
			for(AID id: keyAgents){
				if (!this.getMyAgent().getAID().equals(id)){
					msg.addReceiver(id);
				}
			}
		}
		// adding the wanted receiver
		else{
			msg.addReceiver(dest);
		}
		
		this.setState(ProtocolState.INFORM);
		return msg;
	}
	
	/**
	 * Create the initiation message for a brand new protocol.
	 * The message is meant to be sent to everyone in the near neighborhood, with a specific broadcast conversation ID.
	 * When an initiation message is received, the two agents creates an other protocol to exchange their information
	 * @return the ACLMessage used to initiate the exchange protocol
	 */
	public ACLMessage getInitiationMessage(){
		return this.getInitiationMessage("", null);
	}

	@Override
	public void update() {
		if( timeout <= System.currentTimeMillis() && !waiting){
			this.state = ProtocolState.DONE;
			((ExplorationAgent) this.getMyAgent()).getWorld().setPositionCounter(0);
		}
	}

	public boolean finished(){
		return this.state == ProtocolState.DONE;
	}

	private long getTimeOut() {
		return this.timeout;
	}

	public String getNodeConflict() {
		return nodeConflict;
	}

	public void setNodeConflict(String nodeConflict) {
		this.nodeConflict = nodeConflict;
	}

	public String getSenderPosition() {
		return senderPosition;
	}

	public void setSenderPosition(String senderPosition) {
		this.senderPosition = senderPosition;
	}

	public void setState(ProtocolState state) {
		this.state = state;
	}

	public ProtocolState getState(){
		return state;
	}





	public void setWaiting(boolean done) {
		this.waiting = done;
	}

	public boolean getWaiting() {
		return waiting;
	}

}

