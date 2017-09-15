package mas.behaviours;

import jade.core.behaviours.FSMBehaviour;
import mas.agents.ExplorationAgent;

public class FSMExploBehaviour extends FSMBehaviour{

	private static final long serialVersionUID = -8433280335445300037L;

	public FSMExploBehaviour(ExplorationAgent myAgent){
		super(myAgent);

		this.registerFirstState(new ObserveBehaviour(myAgent), "OBSERVE");
		this.registerLastState(new InfiniteMailCheckingBehaviour(myAgent), "LAST_CHECK");
		this.registerState(new MailCheckingBehaviour(myAgent), "MAIL_CHECKING");
		this.registerState(new MoveBehaviour(myAgent), "MOVE");
		this.registerState(new PickBehaviour(myAgent), "PICK");
		this.registerState(new InitSimpleShareBehaviour(myAgent), "SHARE");
		this.registerState(new ConflictBehaviour(myAgent), "CONFLICT");
		this.registerState(new PlanManagementBehaviour(myAgent), "PLAN_MANAGEMENT");
		
		
		this.registerTransition("OBSERVE", "SHARE", 1);
		this.registerDefaultTransition("SHARE", "MAIL_CHECKING");
		this.registerTransition("OBSERVE", "CONFLICT", 2);
		this.registerDefaultTransition("CONFLICT", "MAIL_CHECKING");
		this.registerTransition("OBSERVE", "MAIL_CHECKING", 3);
		
		this.registerDefaultTransition("MAIL_CHECKING", "PLAN_MANAGEMENT");
		
		this.registerTransition("PLAN_MANAGEMENT", "PICK", 1);
		this.registerTransition("PLAN_MANAGEMENT", "MOVE", 0);
		this.registerTransition("PLAN_MANAGEMENT", "LAST_CHECK", 2);
		this.registerTransition("PLAN_MANAGEMENT", "MAIL_CHECKING", 3); // RESTART

		
		this.registerDefaultTransition("PICK", "PLAN_MANAGEMENT"); 
		this.registerDefaultTransition("MOVE", "OBSERVE");		
	}


}
