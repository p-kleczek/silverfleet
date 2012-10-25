package sfmainframe.ship;


public enum BoardingFirstTurn {
	NOT_APPLICABLE, NO, YES;

	public static BoardingFirstTurn valueOf(int playerId) {
		for (BoardingFirstTurn plr : BoardingFirstTurn.values())
			if (plr.ordinal() == playerId)
				return plr;

		throw new IllegalArgumentException();
	}
}
