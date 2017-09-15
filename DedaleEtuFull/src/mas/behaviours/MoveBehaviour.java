package mas.behaviours;

import java.util.List;

import jade.core.behaviours.OneShotBehaviour;
import mas.agents.ExplorationAgent;
import mas.beliefs.Graph;
import mas.beliefs.Node;

/**************************************
 *
 *
 * 				BEHAVIOUR
 *
 *
 **************************************/


public class MoveBehaviour extends OneShotBehaviour{


	private static final long serialVersionUID = 6580945240045407696L;
	/**
	 * When an agent choose to move
	 *
	 */
	public MoveBehaviour (final mas.abstractAgent myagent) {
		super(myagent);

		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();
		if (myPosition!=""){
			Graph graph = ((ExplorationAgent) myAgent).getWorld().getGraph();
			graph.addNode(new Node(myPosition));
		}
	}

	@Override
	public void action() {

		String myPosition=((mas.abstractAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=""){
			
			List<String> plan = (List<String>)((ExplorationAgent) this.myAgent).getWorld().getPlanManager().getMainPlan();
			String nextId = plan.get(0);
			((mas.abstractAgent)this.myAgent).moveTo(nextId);
			//((ExplorationAgent) myAgent).getWorld().setPosition(nextId);
			//myAgent.doWait(100);
		}
	}
}