package sfmainframe.ship;


public enum BoardingFirstTurn {
	NOT_APPLICABLE, NO, YES;

	public static BoardingFirstTurn valueOf(int n) {
		for (BoardingFirstTurn bft : BoardingFirstTurn.values())
			if (bft.ordinal() == n)
				return bft;

		throw new IllegalArgumentException();
	}
}
