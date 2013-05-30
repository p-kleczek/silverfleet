package sfmainframe.ship.marines;

public enum CommanderState {
    NOT_THERE, READY, USED, IMPRISONED;

    public static CommanderState valueOf(int playerId) {
        for (CommanderState cs : CommanderState.values())
            if (cs.ordinal() == playerId)
                return cs;

        throw new IllegalArgumentException();
    }

}
