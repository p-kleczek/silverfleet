package sfmainframe.ship.marines;

public enum MarinesCompartment {
	DECK, INMOVE, BATTERIES, SHIP_X, NONE;

	public static MarinesCompartment[] getShipCompartments() {
		return new MarinesCompartment[] { DECK, INMOVE, BATTERIES };
	}


	public static int getSize() {
		return MarinesCompartment.values().length - 1;
	}
}
