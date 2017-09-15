package mas.beliefs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;

public class FloodTree implements Serializable{
	
	private static final long serialVersionUID = 6139425419685222265L;
	private HashMap<AID, HashSet<AID>> receivers;
	private HashMap<AID, AID> sources;
	private AID root;
	private String id = "";
	
	public FloodTree(){
		this.receivers = new HashMap<AID, HashSet<AID>>();
		this.sources = new HashMap<AID,AID>();
	}
	
	public void add(AID source, AID receiver){
		if(!receivers.containsKey(source)){
			receivers.put(source, new HashSet<AID>());
		}
		receivers.get(source).add(receiver);
		sources.put(receiver, source);
	}
	
	public AID getSourceByID(AID id){
		return sources.get(id);
	}
	
	public HashSet<AID> getReceiversByID(AID id){
		return receivers.get(id);
	}
	
	public HashMap<AID,AID> getSources(){
		return sources;
	}
	
	public HashMap<AID, HashSet<AID>> getReceivers(){
		return receivers;
	}
	
	/** 
	 * @return true if the given AID is the root of the flood tree 
	**/
	public boolean isRoot(AID myself){
		return root.equals(myself);
	}
	
	/**
	 * @return true if the flood tree is empty
	 */
	public boolean isEmpty(){
		return (sources.isEmpty() && receivers.isEmpty());
	}
	
	/**
	 * Synchronize the tree from an other tree
	 * @param tree the tree used to synchronize the current tree
	 */
	public void sync(FloodTree tree){
		for(AID receiver:tree.getSources().keySet()){
			this.sources.put(receiver, tree.getSourceByID(receiver));
		}
		
		for(AID source:tree.getReceivers().keySet()){
			this.receivers.put(source, tree.getReceiversByID(source));
		}
	}
	
	/**
	 * Resets the tree
	 */
	public void reset(){
		this.receivers = new HashMap<AID, HashSet<AID>>();
		this.sources = new HashMap<AID,AID>();
		this.id = "";
		
	}
	
	/**
	 * sets the tree's root
	 * @param root
	 */
	public void setRoot(AID root){
		this.root = root;
	}
	
	public void setID(String ID){
		this.id = ID;
	}
	
	public String getID(){
		return id;
	}

}
