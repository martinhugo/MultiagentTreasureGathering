package mas.behaviours;

import java.util.List;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import mas.agents.ExplorationAgent;
import mas.beliefs.ConflictLog;
import mas.beliefs.PlanInstruction;
import mas.protocol.ConflictProtocol;

public class PlanManagementBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 6066555191133237265L;
	ExplorationAgent myAgent;
	boolean finished;
	List<String> plan;
	int result;

	public PlanManagementBehaviour(ExplorationAgent myAgent){
		super(myAgent);
		this.myAgent = myAgent;
		this.finished = false;
	}

	@Override
	public void action(){

		result = 0;
		if(myAgent.getWorld().isFinished()){
			System.out.println(myAgent.getLocalName().toUpperCase() + " IS DONE");
			result = 2;
			finished = true;
		}
		else{

			plan = myAgent.getWorld().getPlanManager().getMainPlan();

			if(myAgent.getCurrentPosition().equals(plan.get(0))){
				this.plan.remove(0);
				finished = false;
			}
			else{
				finished = true;
				if(plan.get(0).equals(PlanInstruction.PICK.getName())){
					this.plan.remove(0);
					result = 1;
					finished = true;
				}
				else if(plan.get(0).equals(PlanInstruction.CONFLICT.getName())){
					this.plan.remove(0);
					List<ACLMessage> answers = this.myAgent.getWorld().getGraph().cleanConflictLogs();
					for(ACLMessage answer:answers){
						this.myAgent.sendMessage(answer);
					}

					this.myAgent.doWait(500);
					finished = true;
				}
				else if(this.myAgent.getWorld().getGraph().getNode(plan.get(0)).getConflictLog() != null){

					// Restarting the conflict protocol
					ConflictProtocol protocol = new ConflictProtocol(myAgent);
					ConflictLog log = this.myAgent.getWorld().getGraph().getNode(plan.get(0)).getConflictLog();
					ACLMessage answer = protocol.getInitiationMessage(log.getConflictID(), log.getAgentID());
					myAgent.getProtocols().put(protocol.getConversationID(), protocol);

					// On oublie le log du conflit, permet de bouger si le conflit a disparu.
					this.myAgent.getWorld().getGraph().getNode(plan.get(0)).setConflictLog(null);


					this.myAgent.sendMessage(answer);
					result = 3; // restart the protocol
				}
			}
		}
	}

	@Override
	public int onEnd(){
		return result;
	}

	@Override
	public boolean done() {
		return finished;
	}

}
