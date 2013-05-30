package sfmainframe.gameplay;


public enum Stage {
    DEFINE_WIND("defining wind"), MOVE_WRECKS("moving wrecks"), PLAYERS_MOVES("player's moves"), BOARDING_MOVEMENTS(
            "boarding movements"), BOARDING_ACTIONS("boarding actions"), BOARDING_SABOTAGE("boarding sabotage"), DEPLOYMENT(
            "deployment"), PRE_CONFLICT_OPERATIONS("pre-conflict operations"), STORM_AND_TOW("storm-and-tow"), BETWEEN_TURNS(
            "between turns"), INTERNED_SHIPS("try interned ships");

    private final String description;


    private Stage(String description) {
        this.description = description;
    }


    public String getDescription() {
        return description;
    }


    public static Stage valueOf(int n) {
        for (Stage s : Stage.values())
            if (n == s.ordinal())
                return s;
        throw new IllegalArgumentException();
    }
}
