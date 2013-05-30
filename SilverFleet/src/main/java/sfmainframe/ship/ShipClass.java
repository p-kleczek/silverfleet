package sfmainframe.ship;


public enum ShipClass {
	CARRACK(100, 6, 1, new int[][] { { 0, 0, 0 }, { 2, 0, 0 }, { 2, 0, 0 }, { 3, 0, 0 } }, 74, 60, 200, 1000), GALEON(
			80, 5, 1, new int[][] { { 2, 0, 0 }, { 4, 4, 3 }, { 4, 4, 3 }, { 8, 3, 0 } }, 173, 80, 40, 1100), FRIGATE(
			65, 15, 2, new int[][] { { 2, 0, 0 }, { 4, 3, 2 }, { 4, 3, 2 }, { 2, 0, 0 } }, 148, 90, 50, 750), BRIGANTINE(
			40, 10, 2, new int[][] { { 2, 0, 0 }, { 1, 2, 1 }, { 1, 2, 1 }, { 2, 0, 0 } }, 92, 60, 60, 350), CARAVEL(
			24, 13, 2, new int[][] { { 2, 0, 0 }, { 2, 1, 0 }, { 2, 1, 0 }, { 2, 1, 0 } }, 105, 80, 50, 300), SLOOP(16,
			16, 3, new int[][] { { 2, 0, 0 }, { 1, 1, 0 }, { 1, 1, 0 }, { 0, 0, 0 } }, 64, 50, 20, 250), PINSE(8, 18,
			3, new int[][] { { 1, 0, 0 }, { 2, 0, 0 }, { 2, 0, 0 }, { 0, 0, 0 } }, 50, 40, 10, 180), NONE(0, 0, 0,
			new int[0][0], 0, 0, 0, 0);

	private int durabilityMax;
	private int mastMax;
	private int helmMax;
	private int cannonMax[][];
	private final int crewMax;
	private final int crewDeckMax;
	private final int loadMax;
	private final int price;


	private ShipClass(int durabilityMax, int mastMax, int helmMax, int[][] cannonMax, int crewMax, int crewDeckMax,
			int loadMax, int price) {
		this.durabilityMax = durabilityMax;
		this.mastMax = mastMax;
		this.helmMax = helmMax;
		this.cannonMax = cannonMax;
		this.crewMax = crewMax;
		this.crewDeckMax = crewDeckMax;
		this.loadMax = loadMax;
		this.price = price;
	}


	public static ShipClass valueOf(int classId) {
		for (ShipClass c : ShipClass.values())
			if (c.ordinal() == classId)
				return c;
		throw new IllegalArgumentException();
	}


	public int getDurabilityMax() {
		return durabilityMax;
	}


	public int getMastMax() {
		return mastMax;
	}


	public int getHelmMax() {
		return helmMax;
	}


	public int[][] getCannonMax() {
		return cannonMax;
	}


	public int getCrewMax() {
		return crewMax;
	}


	public int getCrewDeckMax() {
		return crewDeckMax;
	}


	public int getLoadMax() {
		return loadMax;
	}


	public int getPrice() {
		return price;
	}


	public ShipClass copy() {
		return ShipClass.valueOf(this.name());
	}
}
