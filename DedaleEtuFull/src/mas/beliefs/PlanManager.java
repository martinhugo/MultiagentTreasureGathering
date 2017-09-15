package mas.beliefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlanManager implements Serializable{

	private static final long serialVersionUID = 2970681589638131200L;
	private ArrayList<String> conflictPlan;
	private ArrayList<String> treasuresPlan;
	private ArrayList<String> exploPlan;
	private World world;
	public static final double THRESHOLD_INTERESTING_TREASURE = 0.1;

	public PlanManager(){
		this.conflictPlan = new ArrayList<String>();
		this.treasuresPlan = new ArrayList<String>();
		this.exploPlan = new ArrayList<String>();
	}

	public void setWorld(World world){
		this.world = world;
	}

	public void setConflictPlan(ArrayList<String> plan){
		this.conflictPlan = plan;
	}

	public void setTreasuresPlan(ArrayList<String> plan){
		this.treasuresPlan = plan;
	}

	public void setExploPlan(ArrayList<String> plan){
		this.exploPlan = plan;
	}

	public List<String> getConflictPlan() {
		return conflictPlan;
	}

	public List<String> getTreasuresPlan() {
		return treasuresPlan;
	}

	public List<String> getExploPlan() {
		return exploPlan;
	}

	/**
	 * Returns the main plan of the protocol.
	 * Conflict > Treasures > Explo
	 * @return the main plan
	 */
	public ArrayList<String> getMainPlan(){
		ArrayList<String> plan;

		if (!this.conflictPlan.isEmpty()){
			plan = this.conflictPlan;
		}
		else if(!this.treasuresPlan.isEmpty()){
			if(!this.treasuresPlan.get(0).equals(PlanInstruction.PICK.getName())){
				this.treasuresPlan = this.reconstruct(this.treasuresPlan);
			}
			plan = this.treasuresPlan;
		}
		else if(!this.exploPlan.isEmpty()){
			this.exploPlan = this.reconstruct(this.exploPlan);
			plan = this.exploPlan;
		}

		else{
			generateSimplePlan();
			plan = getMainPlan();
		}

		// If the plan position is the current position the first element is removed
		if(plan.get(0).equals(this.world.getMySelf().getPosition())){
			plan.remove(0);
			if(plan.isEmpty()){
				plan = getMainPlan();
			}
		}

		return plan;
	}

	/**
	 * Generate a new plan. This method is used in the simple version of the project.
	 * This version relies on local decision and pair communication.
	 */
	public void generateSimplePlan(){


		if(!world.getGraph().isFullyExplored()){
			Treasure treasure = world.getBestTreasure2();
			if((treasure != null) && (world.getMySelf().getUtility(treasure) <= THRESHOLD_INTERESTING_TREASURE)){
				this.treasuresPlan = world.getGraph().getPath(world.getMySelf().getPosition(),treasure.getPosition(), TypePath.TO_NODE, false);
				this.treasuresPlan.add(PlanInstruction.PICK.getName());
				this.exploPlan = new ArrayList<String>();
			}
			else{
				this.exploPlan = world.getGraph().getPath(world.getMySelf().getPosition(), null, TypePath.UNVISITED_NODE, false);
				this.treasuresPlan = new ArrayList<String>();
			}

		}

		else if (world.getMySelf().getCapacity() != 0){
			Treasure treasure = world.getBestTreasure2();

			if(treasure == null){
				this.exploPlan = world.getGraph().getPath(world.getMySelf().getPosition(), null, TypePath.RANDOM, false);
				this.treasuresPlan = new ArrayList<String>();
				this.conflictPlan = new ArrayList<String>();
			}
			else{
				this.treasuresPlan = world.getGraph().getPath(world.getMySelf().getPosition(),treasure.getPosition(), TypePath.TO_NODE, false);
				this.treasuresPlan.add(PlanInstruction.PICK.getName());
				this.exploPlan = new ArrayList<String>();
		
			}
		}

		else{
			this.exploPlan = world.getGraph().getPath(world.getMySelf().getPosition(), null, TypePath.RANDOM, false);
			this.treasuresPlan = new ArrayList<String>();
			this.conflictPlan = new ArrayList<String>();
		}
	}

	/**
	 * Return true if the agent should pick the treasure at the current position
	 * @return
	 */
	public boolean pickDecision(){
		Treasure treasure = world.getGraph().getNode(world.getMySelf().getPosition()).getTreasure();

		if ((treasure == null) || (world.getMySelf().getCapacity() == 0) || (treasure != world.getBestTreasure2())){
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * Return the reconstructed plan if the agent has moved since the last time the plan was used
	 * @param plan
	 * @return the reconstructed plan
	 */
	public ArrayList<String> reconstruct(ArrayList<String> plan){
		Graph graph = world.getGraph();

		if ((!plan.get(0).equals(PlanInstruction.PICK.getName()))
			&& (!plan.get(0).equals(world.getMySelf().getPosition())
			&& !graph.isNeighbor(plan.get(0), world.getMySelf().getPosition()))){

			if(plan.get(plan.size()-1).equals(PlanInstruction.PICK.getName())){
				plan = graph.getPath(world.getMySelf().getPosition(), plan.get(plan.size()-2), TypePath.TO_NODE, false);
				plan.add(PlanInstruction.PICK.getName());
			}

			else{
				plan = graph.getPath(world.getMySelf().getPosition(), plan.get(plan.size()-1), TypePath.TO_NODE, false);
			}
		}

		return plan;

	}
}