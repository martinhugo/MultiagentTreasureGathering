package mas.beliefs;

import java.io.Serializable;

import env.Attribute;

public class Treasure implements Serializable {
	private static final long serialVersionUID = 1242870919686012172L;
	private int value;
	private TreasureType type;
	private String position;
	
	/**
	 * Creates a new treasure from an observed attribute
	 * @param attr the observed attribute
	 */
	public Treasure(Attribute attr, String pos){
		this.value = (int) attr.getValue();
		setType(attr.getName());
		this.position = pos;
	}
	
	/**
	 * Creates a new treasure from an other treasure
	 * @param treasure the other treasure
	 */
	public Treasure(Treasure treasure){
		this.type = treasure.getType();
		this.value = treasure.getValue();
		this.position = treasure.getPosition();
	}
	
	/**
	 * Updates the current treasure from an other treasure
	 * @param treasure the treasure used for the update
	 */
	public boolean update(Treasure treasure){
		if (value > treasure.getValue()){
			value = treasure.getValue();
			return true;
		}
		
		return false;
	}
	
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	/**
	 * Updates the current treasure from a new observation
	 * @param treasure the treasure used for the update
	 */
	public boolean update(Attribute attr){
		if (value >= (int) attr.getValue()){
			value = (int) attr.getValue();
			return true;
		}
		return false;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public void setType(String type) {
		if(type == TreasureType.DIAMONDS.getName()){
			this.type = TreasureType.DIAMONDS;
		}else{
			this.type = TreasureType.TREASURE;
		}
	}
	
	public TreasureType getType(){
		return type;
	}
	
	/**
	 * Return true if the String type references a treasure
	 * @param type the string which we want to know its type
	 * @return true if type references a Treasure type
	 */
	public static boolean isTreasure(String type){
		return (type == TreasureType.DIAMONDS.getName()) || (type == TreasureType.TREASURE.getName());
	}
	
}