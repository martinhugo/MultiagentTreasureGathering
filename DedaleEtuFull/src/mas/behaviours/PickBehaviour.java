package mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import mas.agents.ExplorationAgent;
import mas.beliefs.Node;
import mas.beliefs.TreasureType;

public class PickBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = -8316904702620041901L;
	ExplorationAgent myAgent;
	
	public PickBehaviour(ExplorationAgent myAgent){
		super(myAgent);
		this.myAgent = myAgent;
		
	}

	@Override
	public void action(){
		
		String position = ((mas.abstractAgent)this.myAgent).getCurrentPosition();
		int freeSpace = ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace();
		Node currentNode = ((ExplorationAgent)this.myAgent).getWorld().getGraph().getNode(position);
		
		
		// on tente de récupérer
		if(myAgent.getWorld().getPlanManager().pickDecision()){
			TreasureType type = currentNode.getTreasure().getType();
			System.out.println(this.myAgent.getLocalName() + " Trésor avant: " + currentNode.getTreasure().getValue() + " " + type);
			System.out.println(this.myAgent.getLocalName() + " Free space: " + freeSpace);
			((mas.abstractAgent)this.myAgent).pick();
			((ExplorationAgent)this.myAgent).getWorld().getMySelf().setCapacity(((mas.abstractAgent)this.myAgent).getBackPackFreeSpace());

			// si quelque chose a bien été récupéré
			if(freeSpace != ((mas.abstractAgent)this.myAgent).getBackPackFreeSpace()){
				// mise à jour de mes connaissances
				
				((ExplorationAgent) this.myAgent).getWorld().getMySelf().setType(type);
			}
			
			// mise à jour sur la connaissance de ce que l'on peut ramasser
			else{
				if(type != TreasureType.DIAMONDS){
					((ExplorationAgent) this.myAgent).getWorld().getMySelf().setType(TreasureType.DIAMONDS);
				}
				else{
					((ExplorationAgent) this.myAgent).getWorld().getMySelf().setType(TreasureType.TREASURE);
				}
			}

			// mise à jour dans tous les cas (si on a récupéré, ou si les infos ne sont plus à jour)
			((ExplorationAgent) this.myAgent).getWorld().getGraph().updateGraph(((mas.abstractAgent)this.myAgent).observe());
			System.out.println(this.myAgent.getLocalName() +  " Trésor après: " + currentNode.getTreasure().getValue() + " " + currentNode.getTreasure().getType());
			System.out.println();
		}
	}
}
