package mas.beliefs;

import java.io.Serializable;

import jade.core.AID;

public class Agent implements Serializable{

	private static final long serialVersionUID = -8381970440409791897L;

	private AID id;
	private int capacity;
	private TreasureType type;
	private String position;
	private long tick;
	private long lastKnowledge;
	private boolean done;
	private String conflictID;

	public Agent(AID id, int capacity, String position, String direction) {
		super();
		this.id = id;
		this.capacity = capacity;
		this.position = position;
		this.tick = 0;
		this.type = TreasureType.UNKNOWN;
		this.done = false;
		this.conflictID = "";
	}

	public Agent(AID id){
		this(id, Integer.MAX_VALUE, "","");
	}

	public long getTick(){
		return this.tick;
	}

	public void setTick(long tick){
		this.tick = tick;
	}

	public void incClock(int value){
		this.tick += value;
	}

	public void setLastKnowledge(long l){
		this.lastKnowledge = l;
	}

	public long getLastKnowledge(){
		return this.lastKnowledge;
	}

	/**
	 * Set the conflictID of the agent.
	 * A conflict id is the conflict's protocol ID.
	 * @param ID the new ID
	 */
	public void setConflictID(String ID){
		this.conflictID = ID;
	}

	/**
	 * Return the conflict's protocol ID with the agent.
	 * @param ID
	 * @return
	 */
	public String getConflictID(){
		return conflictID;
	}

	public AID getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setType(TreasureType type){
        this.type = type;
    }

    public TreasureType getTreasureType(){
        return this.type;
    }

    public boolean isSameType(Treasure treasure){
    	if(this.type == TreasureType.UNKNOWN || this.type == treasure.getType()){
    		return true;
    	}
    	else{
    		return false;
    	}
    }

	/**
	 * Returns the ratio between the proposed treasure and my capacity
	 * @param treasure
	 * @return
	 */
	public float getRatio(Treasure treasure){
		if (((type == TreasureType.UNKNOWN) || (treasure.getType() == type)) && ((treasure.getValue() != 0) && this.capacity != 0)){
			return (float)treasure.getValue()/(float)this.capacity;
		}
		else{
			return Float.MAX_VALUE;
		}
	}

	/**
	 * Return the interest of the agent for the treasure
	 * @param treasure the treasure
	 * @return 1 - the treasure's ratio
	 */
	public float getUtility(Treasure treasure){
		return Math.abs(1 - getRatio(treasure));
	}

	/**
	 * Synchronize the current agent with the given one
	 * @param agent the other version of the current agents
	 * @return the number of changes made during the update
	 */
	public int sync(Agent agent){
		int nbChanges = 0;

		if(this.type == TreasureType.UNKNOWN && this.type != agent.getTreasureType()){
			this.type = agent.getTreasureType();
			nbChanges++;
		}

		if(this.capacity > agent.getCapacity()){
			this.capacity = agent.getCapacity();
			nbChanges++;
		}

		if(agent.isDone() && !this.done){
			done = true;
			nbChanges++;
		}
		this.setTick(Math.max(agent.getTick(), this.getTick()));
		return nbChanges;
	}

	public boolean isDone(){
		return done;
	}

	public void setDone(boolean done){
		this.done = done;
	}
}
