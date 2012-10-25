package sfmainframe.gameplay;

public enum MovesQueueCode {
	NEW, ROTATE, MOVE, ROTATE_MOVE, MOVE_ROTATE, ROTATE_MOVE_ROTATE, END;

	public static MovesQueueCode valueOf(int classId) {
		for (MovesQueueCode c : MovesQueueCode.values())
			if (c.ordinal() == classId)
				return c;
		throw new IllegalArgumentException();
	}
}
