package sfmainframe.gameplay.between;

public enum RepairType {
	DURABILITY(5), MAST(6), HELM(30);

	private final int cost;


	private RepairType(int cost) {
		this.cost = cost;
	}


	public int getCost() {
		return cost;
	}

}
