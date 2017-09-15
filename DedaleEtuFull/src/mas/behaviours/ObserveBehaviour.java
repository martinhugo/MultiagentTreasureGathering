package mas.behaviours;

import java.util.List;

import env.Attribute;
import env.Couple;
import jade.core.behaviours.OneShotBehaviour;
import mas.agents.ExplorationAgent;
import mas.beliefs.Graph;

public class ObserveBehaviour extends OneShotBehaviour {
	private ExplorationAgent myAgent;
	private static final long serialVersionUID = 7740607361546416592L;

	public ObserveBehaviour(ExplorationAgent myAgent){
		super(myAgent);
		this.myAgent = myAgent;
	}

	@Override
	public void action() {
		String myPosition= this.myAgent.getCurrentPosition();

		if (myPosition!=""){
			// I observe the current node
			List<Couple<String,List<Attribute>>> lobs = this.myAgent.observe();//myPosition

			// Then, I update my beliefs if i'm not blocked
			if(myPosition ==  myAgent.getWorld().getMySelf().getPosition()){
                int positionCounter = ((ExplorationAgent) myAgent).getWorld().getPositionCounter();
                ((ExplorationAgent) myAgent).getWorld().setPositionCounter(positionCounter + 1);
			}

			else{
				// r�initialisation de la liste des noeuds bloqu�s
				((ExplorationAgent) myAgent).getWorld().reinitBlockedNodes();

				Graph graph = ((ExplorationAgent) myAgent).getWorld().getGraph();
				graph.updateGraph(lobs);
				myAgent.getWorld().getMySelf().setPosition(myAgent.getCurrentPosition());
				((ExplorationAgent) myAgent).getWorld().setPositionCounter(0);

			}

		}
	}
	
	@Override
	public int onEnd(){
		int result;


		if (myAgent.getWorld().canSendMessage()){
			result =  1;
		}

		else if(myAgent.getWorld().getPositionCounter()>5){
			result = 2;
		}

		else{
			result = 3;
		}

		return result;
	}


}
