package mas.beliefs;

public enum TreasureType {
	UNKNOWN("Unknown"), DIAMONDS("Diamonds"), TREASURE("Treasure");

	private String name;

	private TreasureType(String str){
		name = str;
	}

	public String getName(){
		return name;
	}
}