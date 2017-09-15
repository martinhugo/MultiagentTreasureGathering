package mas.beliefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jade.core.AID;
import mas.abstractAgent;
import mas.agents.ExplorationAgent;


public class World implements Serializable{

	private static final long serialVersionUID = -9025510059056917114L;

	private final int NB_TICK = 20;
	private int positionCounter;
	private long lastClockUpdate;
	private Graph graph;
	private HashMap<AID, Agent> agents;
	private Agent myself;
	private boolean finished;
	private List<String> blockedNodes;
	private PlanManager planManager;

	public World(ExplorationAgent myAgent, Graph map, HashMap<AID, Agent> agents, PlanManager plan) {
		super();
		this.graph = map;
		this.agents = agents;
		this.planManager = plan;
		this.finished = false;
		this.positionCounter = 0;
		this.lastClockUpdate = 0;
		this.blockedNodes = new ArrayList<String>();
	}

	public World(ExplorationAgent myAgent){
		this(myAgent, new Graph(), new HashMap<AID, Agent>(), new PlanManager());
		this.graph.setWorld(this);
		this.planManager.setWorld(this);
	}

	public PlanManager getPlanManager() {
		return planManager;
	}

	/**
	 * Inits the agents hashmap. Each agent of the list is added to the hashmap
	 * If the id of the given agent (myself) is found on the list, a shortcut attributes myself is initialized.
	 * @param agents the agent's list used to init the hashmap
	 * @param myAgent the current agent
	 */
	public void initAgents(List<AID> agents, abstractAgent myAgent){
		for(AID id: agents){
			Agent agent =  new Agent(id);
			if(id.equals(myAgent.getAID())){
				this.myself = agent;
				this.myself.setCapacity(myAgent.getBackPackFreeSpace());
			}
			this.agents.put(id, agent);
		}
	}

	public Agent getMySelf(){
		return this.myself;
	}

	public Graph getGraph() {
		return this.graph;
	}

	public void setGraph(Graph graph){
		this.graph = graph;
	}

	public void setPositionCounter(int positionCounter){
		this.positionCounter = positionCounter;
	}

	public int getPositionCounter(){
		return this.positionCounter;
	}

	public HashMap<AID, Agent> getAgents() {
		return this.agents;
	}

	public void setAgents(HashMap<AID, Agent> agents) {
		this.agents = agents;
	}

	public boolean isFinished(){
		if(!finished){
			this.finished = this.allAgentsDone();
		}
		return this.finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public List<String> getBlockedNodes(){
		return this.blockedNodes;
	}

	public void reinitBlockedNodes(){
		this.blockedNodes = new ArrayList<String>();
	}

	public void addBlockedNode(String node){
		this.blockedNodes.add(node);
	}

	public boolean isBlocked(String node){
		return this.blockedNodes.contains(node);
	}

	/**
	 * Synchronize the current world with the given world
	 * @param world
	 */
	public void sync(World world){
		this.graph.sync(world.getGraph());
		this.syncAgents(world.getAgents());
		//this.tree.sync(world.getFloodTree());
		planManager.generateSimplePlan(); // MAJ du plan courant
		// rajouter maj quand découvert de trésor ?? oui
	}


	/**
	 * This method determine if the agent send a message or not
	 * Currently, it is just based on the number of updated nodes
	 * @return a boolean that indicates if the agent can send a message or not
	 */
	public boolean canSendMessage(){
		if(getMySelf().getTick() - lastClockUpdate >= this.NB_TICK){
			lastClockUpdate = getMySelf().getTick();
			return true;
		}
		return false;
	}

	/**
	 * Return the reference of the most interesting treasure for the agent.
	 * This treasure has the lowest difference between 1 and the treasure/capacity ratio
	 * @return the best treasure
	 */
	public Treasure getBestTreasure(){
		float bestUtility = Float.MAX_VALUE;
		Treasure bestTreasure = null;
		Agent myself = this.myself;

		// on récupère le meilleur trésor pour l'agent
		for (String key : this.graph.getTreasures().keySet()){
			Treasure treasure = (this.graph.getTreasures()).get(key);
			float utility = myself.getUtility(treasure);

			if(utility < bestUtility){
				bestUtility = utility;
				bestTreasure = treasure;
			}
		}
		return bestTreasure;
	}

	/**
	 * Returns the best treasure found until now.
	 * It returns the biggest lowest treasure if it exist a lower treasure than the current capacity.
	 * Otherwise the lowest biggest treasure is returned.
	 * @return
	 */
	public Treasure getBestTreasure2(){
		Treasure lowestTreasure = null;
		Treasure biggestTreasure = null;
		int lowestValue = 0;
		int biggestValue = Integer.MAX_VALUE;
		Agent myself = this.myself;

		Treasure treasure;
		// on récupère le meilleur trésor pour l'agent
		for (String key : this.graph.getTreasures().keySet()){
			treasure = this.graph.getTreasures().get(key);
			if(myself.isSameType(treasure)){
				if(treasure.getValue() <= myself.getCapacity() && lowestValue<treasure.getValue()){
					lowestValue = treasure.getValue();
					lowestTreasure = treasure;
				}
				else if ( (treasure.getValue() > myself.getCapacity()) && (biggestValue>treasure.getValue()) ){
					biggestValue = treasure.getValue();
					biggestTreasure = treasure;
				}
			}
		}

		if(lowestTreasure != null){
			return lowestTreasure;
		}
		else{
			return biggestTreasure;
		}
	}

	/**
	 * return true if all the agents are full
	 * @return
	 */
	public boolean allAgentsDone(){
		Set<AID> ids = agents.keySet();

		for(AID id:ids){
			if (!agents.get(id).isDone())
				return false;
		}

		return true;
	}

	/**
	 * synchronizes the given agents list with the world's agent list
	 * The clock is updated for each changed agent
	 */
	public void syncAgents(HashMap<AID, Agent> agents){
		Set<AID> ids = agents.keySet();
		for(AID id:ids){
			if(this.agents.containsKey(id)){
				int nbChanges = this.agents.get(id).sync(agents.get(id));
				this.getMySelf().incClock(nbChanges);

			}
			else{
				this.agents.put(id, agents.get(id));
				this.getMySelf().incClock(3); // + 3 => 1 for the agent, 1 for the capacity, 1 for the type ?
			}
		}
	}


	public World getWorldFromTick(long tick, ExplorationAgent agent){
		World world = new World(agent);
		world.setGraph(this.getGraph().getGraphFromTick(tick));
		world.setAgents(this.getAgentsFromTick(tick));
		return world;
	}

	@SuppressWarnings("unchecked")
	public HashMap<AID, Agent> getAgentsFromTick(long tick){
		HashMap<AID, Agent> agents = (HashMap<AID, Agent>) DeepCopy.copy(this.agents);
		for(AID aid : agents.keySet()){
			Agent agent = this.agents.get(aid);
			if(agent.getLastKnowledge() < tick){
				agents.remove(agents.get(aid));
			}
		}
		return agents;
	}

	public World getSendableWorld(ExplorationAgent agent){

		World world = new World(agent);
		world.setGraph((Graph) DeepCopy.copy(this.getGraph()));
		world.setAgents((HashMap<AID, Agent>) DeepCopy.copy(this.getAgents()));

		return world;
	}
}