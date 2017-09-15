package mas.beliefs;

public enum PlanInstruction {
	PICK("Pick"), CONFLICT("Conflict"), EXPLORE("Explo");
	
	private String name;

	private PlanInstruction(String str){
		name = str;
	}

	public String getName(){
		return name;
	}
}
