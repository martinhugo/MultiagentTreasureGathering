package mas.beliefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import env.Attribute;

public class Node implements Serializable {

	private static final long serialVersionUID = -6588050890129952191L;

	private String id;
	private HashSet<String> neighbors;
	private Treasure treasure = null;
	private boolean visited;
	private ConflictLog conflictLog = null;
	private long tick;


	// Constructor
	public Node(String id, HashSet<String> neighbors, List<Attribute> content, boolean visited, int timestamp) {
		this.id = id;
		this.neighbors = neighbors;
		this.setContent(content);
		this.visited = visited;
	}

	public Node(String id){
		this(id, new HashSet<String>(), new ArrayList<Attribute>(), false, 0);
	}

	// Getter & Setter
	public String getId() {
		return id;
	}

	public long getTick() {
		return tick;
	}

	public void setTick(long clock) {
		this.tick = clock;
	}

	public ConflictLog getConflictLog() {
		return conflictLog;
	}

	public void setConflictLog(ConflictLog conflictLog) {
		this.conflictLog = conflictLog;
	}


	public HashSet<String> getNeighbors() {
		return neighbors;
	}


	public void setNeighbors(HashSet<String> neighbors) {
		this.neighbors = neighbors;
	}

	public void setContent(List<Attribute> content) {
		boolean treasure = false;

		for(Attribute attr:content){
			if(Treasure.isTreasure(attr.getName())){
				this.setTreasure(attr);
				treasure = true;
			}
		}

		// cas ou le trésor a disparu
		if(this.getTreasure() != null && treasure == false){
			this.getTreasure().setValue(0);
		}
	}

	/**
	 * Return true if the node has been visited
	 * @return
	 */
	public boolean isVisited(){
		return visited;
	}

	public void setVisited(boolean visited){
		this.visited = visited;
	}


	public Treasure getTreasure(){
		return treasure;
	}

	/**
	 * Sets the treasure attribute from the treasure param
	 * @param treasure the treasure used to set the treasure attributes
	 * @return true if the treasure has been updated or set
	 */
	public boolean setTreasure(Treasure treasure){
		boolean changed = false;
		if(this.treasure == null){
			this.treasure = new Treasure(treasure);
			changed = true;
		}
		else{
			changed = this.treasure.update(treasure);
		}

		return changed;
	}

	/**
	 * Sets the treasure attribute from the treasure param
	 * @param treasure the treasure used to set the treasure attributes
	 */
	public void setTreasure(Attribute attr){
		if(treasure == null){
			treasure = new Treasure(attr, id);
		}
		else{
			treasure.update(attr);
		}
	}

	// Neighbors manipulation
	/**
	 * Adds a list node to neighborhood
	 * @param node a list of Node
	*/
	public void addNeighbors(List<Node> nodes){
		for(Node node:nodes)
			this.neighbors.add(node.getId());
	}

	/**
	 * Add a neighbor to the neighbor list
	 * Return true if the neighbor wasn't in the neighbor list
	 * @param node the added node
	*/
	public boolean addNeighbor(Node node){
		return this.neighbors.add(node.getId());
	}

	/**
	 * Remove a neighbor from the neighbor list
	 * @param node the node to remove
	 */
	public void removeNeighbor(Node node){
		this.neighbors.remove(node.getId());
	}


	/**
	 * Updates the node from an other agent's belief about the node
	 * Returns the number of changes made during the synchronization
	 * @param node the agent's beliefs about the node
	 */
	public int sync(Node node) {
		int nbChanges = 0;

		if(node.isVisited() && !visited){
			visited = true;
			nbChanges++;
		}

		// Treasure update
		if(node.getTreasure() != null){
			if(this.setTreasure(node.getTreasure())){
				nbChanges++;
			}
		}

		for(String neighbor : node.getNeighbors()){
			if(neighbors.add(neighbor)){
				nbChanges++;
			}
		}

		return nbChanges;
	}
}