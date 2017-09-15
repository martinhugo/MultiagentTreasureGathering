package mas.beliefs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import env.Attribute;
import env.Couple;
import jade.lang.acl.ACLMessage;
import mas.protocol.ConflictProtocol;


public class Graph implements Serializable{

	private static final long serialVersionUID = 5813447266237753038L;
	private boolean fullyExplored;
	private HashMap<String, Node> knownNodes;
	private HashMap<String, Treasure> treasures;
	private World world;

	public Graph(World world, HashMap<String, Node> knownNode, HashMap<String, Treasure>  treasures) {
		this.knownNodes = knownNode;
		this.treasures = treasures;
		this.world = world;
	}

	public Graph(World world) {
		this(world, new HashMap<String, Node>(), new HashMap<String, Treasure>());
	}

	public Graph() {
		this(null, new HashMap<String, Node>(), new HashMap<String, Treasure>());
	}

	public void setWorld(World world){
		this.world = world;
	}

	public void setFullyExplored(boolean complete){
		this.fullyExplored = complete;
	}

	/**
	 * @returns true if the graph is fully explored
	 */
	public boolean isFullyExplored(){
		if(fullyExplored){
			return fullyExplored;
		}
		else{
			Set<String> ids = knownNodes.keySet();
			for(String id:ids){
				if(!knownNodes.get(id).isVisited()){
					return false;
				}
			}

			fullyExplored = true;
			return fullyExplored;
		}
	}

	/**
	 * @returns true if the treasures are fully collected
	 */
	public boolean isFullyCollected(){
		boolean unknown = (world.getMySelf().getTreasureType() == TreasureType.UNKNOWN);
		for(String id:treasures.keySet()){
			if((treasures.get(id).getValue() != 0) && (unknown || world.getMySelf().getTreasureType() == treasures.get(id).getType())){
				return false;
			}
		}

		return true;
	}

	public Node getNode(String id){
		return this.knownNodes.get(id);
	}

	public boolean contains(String id){
		return this.knownNodes.containsKey(id);
	}

	public void addNode(Node node){
		this.knownNodes.put(node.getId(), node);
	}

	public void removeNode(Node node){
        for (String id : node.getNeighbors()){
            Node neighbor = this.getNode(id);
            neighbor.removeNeighbor(node);
        }
        this.knownNodes.remove(node.getId());
    }

	public Set<String> getIds(){
		return this.knownNodes.keySet();
	}

	public boolean isNeighbor(String nodeNeighborId, String nodeId){
		Node node = this.getNode(nodeId);
		if(node.getNeighbors().contains(nodeNeighborId)){
			return true;
		}
		return false;
	}

	/**
	 * Update the graph with the given sensor informations
	 * The current position is fully updated with the detected attributes and neighbors nodes.
	 * Each neighbors node has its neighborhood updated with the current node.
	 * @param sensorInformation
	 */
	public void updateGraph(List<Couple<String,List<Attribute>>> sensorInformation){

		String currentId = sensorInformation.get(0).getLeft();
		List<Attribute> currentContent= sensorInformation.get(0).getRight();
		HashSet<String> currentNeighbors;
		Node currentNode;

		// Création/Maj du noeud courant
		// We keep the reference on the neighbors nodes' list, to update it with the found neighbors.
		if(this.contains(currentId)){
			currentNode = this.getNode(currentId);
			currentNeighbors = currentNode.getNeighbors();
			currentNode.setContent(currentContent);

			// => maj horloge
			if(!currentNode.isVisited()){
				currentNode.setVisited(true);
				world.getMySelf().incClock(1);
			}
			else if (this.fullyExplored){
				this.world.getMySelf().incClock(1);
			}

			Treasure treasure = currentNode.getTreasure();
			if(treasure != null){
				this.updateTreasures(currentId, treasure);
			}



		}
		// pas nécessaire le noeud courant est toujours dans le graphe
		else{
			currentNeighbors = new HashSet<String>();
			currentNode = new Node(currentId, currentNeighbors, currentContent, true, 0);
			if(currentNode.getTreasure() != null){
				world.getMySelf().incClock(3);
			}
			else{
				world.getMySelf().incClock(1);
			}
			this.addNode(currentNode);

		}

		//update the clock of the node to the current clock of the agent
		currentNode.setTick(world.getMySelf().getTick());

		// For all neighbor nodes
		for(int i=1; i<sensorInformation.size(); i++){
			Node neighborNode;
			String neighborId = sensorInformation.get(i).getLeft();

			if(this.contains(neighborId)){
				neighborNode = this.getNode(neighborId);
				if(neighborNode.addNeighbor(currentNode)){
					world.getMySelf().incClock(1);
				}
			}

			else{
				HashSet<String> neighbors = new HashSet<String>();
				neighbors.add(currentNode.getId());
				neighborNode = new Node(neighborId, neighbors, new ArrayList<Attribute>(), false, 0);
				this.addNode(neighborNode);
				world.getMySelf().incClock(1);
			}

			// Update of the neighbors nodes' list of the current node
			if(currentNode.addNeighbor(neighborNode)){
				world.getMySelf().incClock(1);
			}
		}

		//Update the agent knowledge about it ending
		world.getMySelf().setDone((this.isFullyExplored() && this.isFullyCollected()) || (world.getMySelf().getCapacity() == 0));
	}


	/**
	 * Synchronize the graph with the given graph
	 * @param graph the given graph
	 */
	public void sync(Graph graph){
		for(String id:graph.getIds()){
			if(this.contains(id)){
				int nbChanges = this.getNode(id).sync(graph.getNode(id));
				world.getMySelf().incClock(nbChanges);
			}
			else{
				this.addNode(graph.getNode(id));
				world.getMySelf().incClock(1 + graph.getNode(id).getNeighbors().size());
			}

			Treasure treasure = this.getNode(id).getTreasure();
			if(treasure != null){
				this.updateTreasures(id, treasure);
			}
		}

		//Update the agent knowledge about it ending
		world.getMySelf().setDone((this.isFullyExplored() && this.isFullyCollected()) || (world.getMySelf().getCapacity() == 0));
	}


	/**
	 * Updates the treasures placed at the id node
	 * @param id the node's id, which refers it positions in the graph
	 * @param treasure the treasure to update
	 */
	public void updateTreasures(String id, Treasure treasure){
		if(this.treasures.containsKey(id)){
			if(this.treasures.get(id).update(treasure)){
				world.getMySelf().incClock(35);
			}
		}

		else{
			this.treasures.put(id, treasure);
			world.getMySelf().incClock(35);
		}
	}

	/**
	 * Return the treasures representation
	 * @return the treasures representation
	 */
	public HashMap<String, Treasure> getTreasures(){
		return treasures;
	}

	/**
	 *
	 * @param start le noeud ou commencer le parcours en largeur
	 * @param end le noeud ou terminer le parcours en largeur (peut être null)
	 * @param typePath le type de parcours que l'on souhaite (RANDOM, INTERSECTION_NODE, TO_NODE, UNVISITED_NODE)
	 * @return le chemin le plus court vers le noeud but
	 */
	public ArrayList<String> getPath(String start, String end, TypePath typePath, boolean blockedNodes){

		if(typePath == TypePath.RANDOM){ // cas génération d'un chemin aléatoire
			end = this.getRandomEnd();
		}

		boolean found = false;
		List<String> fifo = new ArrayList<String>();
		HashMap<String, String> listNodesVisited = new HashMap<String, String>(); // dictionnaire de la forme -> (fils, pere)
		listNodesVisited.put(start, start);
		fifo.add(start);
		String currentNode = start;

		while(!found && !fifo.isEmpty()){ // tant qu'on a pas trouvé de noeud but ou que il reste des noeuds à parcourir

			currentNode = fifo.get(0);
			fifo.remove(0);

			HashSet<String> allFils = this.getNode(currentNode).getNeighbors(); // récupération de la liste des fils
			for(String fils : allFils){
				if(!listNodesVisited.containsKey(fils) && (!blockedNodes || this.getNode(fils).getConflictLog() == null)){ // si fils jamais visité dans le parcours
					fifo.add(fils); // ajout du fils à la liste des noeuds à visiter
					listNodesVisited.put(fils, currentNode); // ajout du fils dans le dictionnaire
				}
			}
			found = isDone(typePath, currentNode, end); // vérification si noeud courant est un noeud but
		}
		if(found && typePath == TypePath.INTERSECTION_NODE){ // on prend un fils non visité dans le cas d'une intersection
			currentNode = neighborNotVisited(currentNode, listNodesVisited, true); // blockedNode avant CARE CARE BUG ?
 		}
		if(found && currentNode != null){ // si on a trouvé un noeud but, retourne le chemin entre start et ce noeud
			return reconstructPath(start, currentNode, listNodesVisited);
		}
		else{ // si on a trouvé aucun noeud but, retourne une liste vide
			return new ArrayList<String>();
		}
	}

	public ArrayList<String> reconstructPath(String start, String end, HashMap<String, String> dico){
        ArrayList<String> path = new ArrayList<String>();
        String currentNode = end;
        path.add(end);
        while(!currentNode.equals(start)){
            currentNode = dico.get(currentNode);
            if(!currentNode.equals(start)){
                path.add(0, currentNode);
            }
        }
        return path;
    }

	public String neighborNotVisited(String currentNode, HashMap<String, String> dico, boolean blockedNodes){
		HashSet<String> allFils = this.getNode(currentNode).getNeighbors();
		String neighbor = null;
		for(String fils : allFils){
			if(neighbor == null && !fils.equals(dico.get(currentNode)) && (!blockedNodes || this.getNode(fils).getConflictLog() == null)){
				neighbor = fils;
			}
		}
		return neighbor;
	}

	public String getRandomEnd(){
		List<String> listOfNodes = new ArrayList<String>(this.getIds());

		listOfNodes.remove(this.world.getMySelf().getPosition());
		for(String neighbor:this.getNode(this.world.getMySelf().getPosition()).getNeighbors()){
			listOfNodes.remove(neighbor);
		}

		Random rand = new Random();
		int randomIndex = rand.nextInt(listOfNodes.size());
		return listOfNodes.get(randomIndex);
	}

	public boolean isDone(TypePath typePath, String node, String end){
		boolean done;
		Node n = this.getNode(node);
		switch(typePath){
		case UNVISITED_NODE:
			done = !n.isVisited();
			break;
		case INTERSECTION_NODE:
			done = (n.getNeighbors().size() > 2) && this.existsFreeNeighbor(n);
			break;
		case RANDOM:
		case TO_NODE:
			done = node.equals(end);
			break;
		default:
			done = false;
			break;
		}
		return done;
	}

	/** Returns true if the given node has a free neighbor (free of conflict)
	 * @param node the studied node
	 * @return true if it has a free neighbor
	 */
	public boolean existsFreeNeighbor(Node node){
		for(String neighbor:node.getNeighbors()){
			if(this.getNode(neighbor).getConflictLog() == null){
				return true;
			}
		}
		return false;
	}

	/**
	 * Méthode permettant de fixer tous les conflicts log du graph à null et d'informer les voisins correspondant.
	 * Retourne la réponse permettant d'informer les conflits en attente.
	 **/
	public List<ACLMessage> cleanConflictLogs(){
		ArrayList<ACLMessage> answers = new ArrayList<ACLMessage>();
		for(String node : this.knownNodes.keySet()){
			ConflictLog log = this.getNode(node).getConflictLog();
			if(log!= null){
				String convID = log.getConflictID();
				ACLMessage cancelMessage = new ACLMessage(ACLMessage.CANCEL);
				cancelMessage.setSender(world.getMySelf().getId());
				cancelMessage.addReceiver(log.getAgentID());
				cancelMessage.setConversationId(convID);
				cancelMessage.setProtocol(ConflictProtocol.class.getName());

				answers.add(cancelMessage);
				this.getNode(node).setConflictLog(null);
			}
		}

		return answers;
	}

	/** Returns the conflict path for the given starting point **/
	public ArrayList<String> getConflictPath(String start){
		// Chemin non bloqué
		ArrayList<String> path = this.getPath(start, null, TypePath.INTERSECTION_NODE, true);
		if(path.isEmpty()){
			path = this.getPath(start, null, TypePath.UNVISITED_NODE, true);
		}

		if(path.isEmpty()){
			// bloquage
			path = this.getPath(start, null, TypePath.UNVISITED_NODE, false);
			if(path.size() == 0){
				//path = this.getPath(start, null, TypePath.INTERSECTION_NODE, false);
				path = this.getPath(start, null, TypePath.RANDOM, false);
			}
		}

		return path;
	}

	public Graph getGraphFromTick(long tick){

		Graph graph = (Graph) DeepCopy.copy(this);

		for(String node : this.knownNodes.keySet()){
			if(graph.getNode(node).getTick() < tick && graph.getNode(node).getTick() != 0){
				graph.removeNode(graph.getNode(node));
			}
		}
		return graph;
	}

}
