package sfmainframe.board;

public enum RotateDirection {
	N, NE, SE, S, SW, NW;

	public static RotateDirection rotate(RotateDirection direction, int angle) {
		return RotateDirection.valueOf((direction.ordinal() + angle + 6) % 6);
	}

	public static RotateDirection valueOf(int n) {
		for (RotateDirection rot : RotateDirection.values())
			if (n == rot.ordinal())
				return rot;
		throw new IllegalArgumentException();
	}
}
