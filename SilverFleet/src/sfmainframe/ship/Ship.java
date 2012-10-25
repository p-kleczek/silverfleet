package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import sfmainframe.Commons;
import sfmainframe.Coordinate;
import sfmainframe.MainBoard;
import sfmainframe.Player;
import sfmainframe.board.RotateDirection;
import sfmainframe.gameplay.KillingMode;
import sfmainframe.gameplay.MovementType;
import sfmainframe.gameplay.MovesQueueCode;
import sfmainframe.gameplay.between.DealType;
import sfmainframe.gameplay.between.RepairType;
import sfmainframe.ship.cargo.CargoType;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class Ship {

    public static final int PREVIOUS_OWNER = -1;

    private Integer shipID; // ID w grze
    private Player owner; // ID wlasciciela
    private Player firstOwner; // ID wlasciciela na poczatku rozgrywki
    private ShipClass shipClass; // typ okretu
    private Player internedBy; // ID gracza internujacego okret

    private Coordinate position;
    private RotateDirection rotation;

    /** Code of the current ship's movement phase (eg. move, rotate etc.) */
    private MovesQueueCode movesQueueCode;
    private int actionsOver;

    private int durability;
    private int happiness;

    private int[] helm = new int[2];
    private int mast;

    private int distanceMoved;

    private Map<CargoType, Integer> load;

    private CannonSection cannonSection;
    private MarinesSection marinesSection;

    private BoardingFirstTurn boardingFirstTurn;
    private int[][] boardingActionUsed = new int[MarinesCompartment.getShipCompartments().length][Commons.PLAYERS_MAX];

    private Map<Parameter, Integer> parameters;

    private boolean sailesRipped;
    private Map<Player, Integer> marinesOnBoardWhileSailesRipped;

    private Integer towedByID; // ID okretu holujacego
    private Integer towOtherID; // ID okretu holowanego

    private boolean turnEscapeAttemptUsed; // czy w danej turze zostala juz
                                           // podjeta proba zejscia z mielizny
    Set<ShallowAttempt> escapeAttemptsUsed;
    // zejscia
    // podjete
    // podczas
    // danego
    // ugrzezniecia
    private boolean pullOnAnchorAttemptCarried; // flaga informujaca, czy w
                                                // poprzedniej turze podjeto
                                                // probe podciagniecia sie na
                                                // kotwicy
    private boolean towByOneAttemptCarried; // flaga informujaca, czy w
                                            // poprzedniej turze zalozono hol na
                                            // unieruchomiony okret
    private Coordinate previousTurnPosition; // polozenie koncowe statku w
                                             // poprzedniej turze

    Vector<Integer> shipsCoupled;// ID okretow sczepionych (przeladunek/abordaz)
    Vector<CoupleReason> coupledReasons;

    private boolean happinessSunk; // okret podczas rozgrywki zatopil okret
                                   // przeciwnika
    private boolean happinessBoarding; // zaloga okretu podczas rozgrywki
                                       // zdobyla okret przeciwnika
    private boolean happinessCapture; // okret przyspozyl flocie nowy okret
                                      // (tylko przez internowanie)


    public Ship() {
        shipID = null;
        owner = Player.NONE;
        firstOwner = Player.NONE;
        shipClass = ShipClass.NONE;
        internedBy = Player.NONE;

        position = Coordinate.dummy;
        rotation = RotateDirection.N;
        movesQueueCode = MovesQueueCode.NEW;
        actionsOver = Commons.OFF;

        durability = 0;
        happiness = 0;

        helm[Commons.READY] = 0;
        helm[Commons.USED] = 0;
        mast = 0;
        distanceMoved = 0;

        cannonSection = new CannonSection();
        marinesSection = new MarinesSection();

        load = new HashMap<CargoType, Integer>();
        for (CargoType t : CargoType.values())
            load.put(t, 0);

        boardingFirstTurn = BoardingFirstTurn.NOT_APPLICABLE;

        parameters = new HashMap<Parameter, Integer>();
        for (Parameter parameter : Parameter.values())
            parameters.put(parameter, Commons.OFF);
        parameters.put(Parameter.IS_SUNK, Commons.ON);

        sailesRipped = false;
        marinesOnBoardWhileSailesRipped = new HashMap<Player, Integer>();
        for (Player p : Player.getValues())
            marinesOnBoardWhileSailesRipped.put(p, 0);

        towedByID = null;
        towOtherID = null;

        turnEscapeAttemptUsed = false;
        escapeAttemptsUsed = new HashSet<ShallowAttempt>();
        pullOnAnchorAttemptCarried = false;
        towByOneAttemptCarried = false;
        previousTurnPosition = new Coordinate(Commons.NIL, Commons.NIL);

        shipsCoupled = new Vector<Integer>();
        coupledReasons = new Vector<CoupleReason>();

        happinessSunk = false;
        happinessBoarding = false;
        happinessCapture = false;
    }


    public Ship(Ship ship) {
        shipID = ship.shipID;
        owner = ship.owner;
        firstOwner = ship.firstOwner;
        shipClass = ship.shipClass;
        internedBy = ship.internedBy;

        position = ship.position;
        rotation = ship.rotation;
        movesQueueCode = ship.movesQueueCode;
        actionsOver = ship.actionsOver;

        durability = ship.durability;
        happiness = ship.happiness;

        helm[Commons.READY] = ship.helm[Commons.READY];
        helm[Commons.USED] = ship.helm[Commons.USED];
        mast = ship.mast;
        distanceMoved = ship.distanceMoved;

        cannonSection = new CannonSection(ship.cannonSection);
        marinesSection = new MarinesSection(ship.marinesSection);

        load = new HashMap<CargoType, Integer>(ship.load);

        boardingFirstTurn = ship.boardingFirstTurn;

        parameters = new HashMap<Parameter, Integer>(ship.parameters);

        sailesRipped = ship.sailesRipped;
        marinesOnBoardWhileSailesRipped = new HashMap<Player, Integer>(ship.marinesOnBoardWhileSailesRipped);

        towedByID = ship.towedByID;
        towOtherID = ship.towOtherID;

        turnEscapeAttemptUsed = ship.turnEscapeAttemptUsed;
        escapeAttemptsUsed = new HashSet<ShallowAttempt>(ship.escapeAttemptsUsed);
        pullOnAnchorAttemptCarried = ship.pullOnAnchorAttemptCarried;
        towByOneAttemptCarried = ship.towByOneAttemptCarried;
        previousTurnPosition = ship.previousTurnPosition;

        shipsCoupled = ship.shipsCoupled;
        coupledReasons = ship.coupledReasons;

        happinessSunk = ship.happinessSunk;
        happinessBoarding = ship.happinessBoarding;
        happinessCapture = ship.happinessCapture;
    }


    public void setShip(Player _owner, ShipClass _class, int _ID) {
        shipID = _ID;
        owner = _owner;
        firstOwner = _owner;
        shipClass = _class;

        durability = _class.getDurabilityMax();

        helm[Commons.READY] = _class.getHelmMax();
        mast = _class.getMastMax();

        parameters.put(Parameter.IS_SUNK, Commons.OFF);

        cannonSection = new CannonSection(_class);
        marinesSection = new MarinesSection(_class, owner);

        load.put(CargoType.FREE_SPACE, _class.getLoadMax());
    }


    public Integer getID() {
        return shipID;
    }


    public Player getOwner() {
        return owner;
    }


    public void setOwner(Player owner) {
        this.owner = owner;
    }


    public Player getFirstOwner() {
        return firstOwner;
    }


    public void setFirstOwner(Player player) {
        firstOwner = player;
    }


    public ShipClass getShipClass() {
        return shipClass.copy();
    }


    public Player getInternedBy() {
        return internedBy;
    }


    public void setInternedBy(Player player) {
        internedBy = player;
    }


    public int isActionsOver() {
        return actionsOver;
    }


    public void setActionsOver(int state) {
        actionsOver = state;
    }


    public int getDurability() {
        return durability;
    }


    public void setDurability(int value) {
        durability = value;
    }


    public int getHappiness() {
        return happiness;
    }


    public void setHappiness(int value) {
        happiness = value;
    }


    public int getHelm(int type) {
        if (type == Commons.BOTH)
            return helm[Commons.READY] + helm[Commons.USED];
        else
            return helm[type];
    }


    public int getMast() {
        return mast;
    }


    public int getDistanceMoved() {
        return distanceMoved;
    }


    public void setDistanceMoved(int value) {
        distanceMoved = value;
    }


    public boolean isTurnEscapeAttemptUsed() {
        return turnEscapeAttemptUsed;
    }


    public void setTurnEscapeAttemptUsed(boolean value) {
        turnEscapeAttemptUsed = value;
    }


    public boolean isEscapeAttemptUsed(ShallowAttempt type) {
        return escapeAttemptsUsed.contains(type);
    }


    public void clearEscapeAttemptUsed() {
        escapeAttemptsUsed.clear();
    }


    public void setBoardingActionUsed(Player player, MarinesCompartment location, int value) {
        boardingActionUsed[location.ordinal()][player.ordinal()] = value;
    }


    public int getBoardingActionUsed(Player player, MarinesCompartment location) {
        return boardingActionUsed[location.ordinal()][player.ordinal()];
    }


    public void setBoardingFirstTurn(BoardingFirstTurn value) {
        boardingFirstTurn = value;
    }


    public BoardingFirstTurn isBoardingFirstTurn() {
        return boardingFirstTurn;
    }


    public int getParameter(Parameter type) {
        return parameters.get(type);
    }


    public void setParameter(Parameter type, int value) {
        parameters.put(type, value);
    }


    public boolean getHappinessSunk() {
        return happinessSunk;
    }


    public void setHappinessSunk(boolean value) {
        happinessSunk = value;
    }


    public boolean getHappinessBoarding() {
        return happinessBoarding;
    }


    public void setHappinessBoarding(boolean value) {
        happinessBoarding = value;
    }


    public boolean getHappinessCapture() {
        return happinessCapture;
    }


    public void setHappinessCapture(boolean value) {
        happinessCapture = value;
    }


    public Vector<Integer> getShipsCoupled() {
        return shipsCoupled;
    }


    public boolean isShipCoupled(int id) {
        return shipsCoupled.contains(Integer.valueOf(id));
    }


    public void addShipCoupled(int id) {
        shipsCoupled.add(Integer.valueOf(id));
    }


    public CoupleReason getCoupleReason(int shipID) {
        return coupledReasons.get(shipsCoupled.indexOf(shipID));
    }


    public void couple(int shipID, CoupleReason reason) {
        shipsCoupled.add(shipID);
        coupledReasons.add(reason);
    }


    public void uncouple(int shipID) {
        int inx = shipsCoupled.indexOf(shipID);
        shipsCoupled.remove(inx);
        coupledReasons.remove(inx);
    }


    /**
     * 
     * @param value
     * @return <code>true</code> if a ship sunk
     */
    public boolean destroyHull(int value) {
        if (value >= durability) {
            durability = 0;
            return true;
        }

        durability -= value;
        return false;
    }


    public void destroyMast(int value) {
        MainBoard.addMessage("Ship #" + shipID + ": " + Math.min(5, mast) + " speed points lost.\n");
        mast = Math.max(0, mast - value);
    }


    public void destroyHelm(int value) {
        if (value <= helm[Commons.READY])
            helm[Commons.READY] -= value;
        else {
            value -= helm[Commons.READY];
            helm[Commons.READY] = 0;
        }
        helm[Commons.USED] = Math.max(0, helm[Commons.USED] - value);
    }


    public void modifyHappiness(int value) {
        // par. 18.9
        happiness = Math.max(0, happiness + value);
        // --
    }


    // MARYNARZE
    public int getPlayerMarinesOnShip(Player player, boolean withCommanders) {
        int totalNumber = 0;
        for (MarinesCompartment loc : MarinesCompartment.getShipCompartments()) {
            totalNumber += getMarinesNumber(player, loc, Commons.BOTH);
            if (withCommanders)
                if (getCommanderState(player, loc) == CommanderState.READY
                        || getCommanderState(player, loc) == CommanderState.USED)
                    totalNumber++;
        }
        return totalNumber;
    }


    public int getMarinesNumber(Player player, MarinesCompartment location, int state) {
        return marinesSection.getMarinesNumber(player, location, state);
    }


    public CommanderState getCommanderState(Player player, MarinesCompartment location) {
        return marinesSection.getCommanderState(player, location);
    }


    public void moveMarines(Player player, MarinesCompartment source, MarinesCompartment destination, int amount) {
        marinesSection.moveMarines(player, source, destination, amount);
    }


    public void setCommander(Player player, MarinesCompartment location, CommanderState state) {
        marinesSection.setCommander(player, location, state);
    }


    public void moveCommander(Player player, MarinesCompartment source, MarinesCompartment destination) {
        marinesSection.moveCommander(player, source, destination);
    }


    public Object[] killMarines(Player player, MarinesCompartment location, int amount, KillingMode mode) {
        return marinesSection.killMarines(player, location, amount, mode);
    }


    public boolean isCommanderOnboard(Player player) {
        return marinesSection.isCommanderOnboard(player);
    }


    public boolean isSailesRipped() {
        return sailesRipped;
    }


    public void ripSailes() {
        sailesRipped = true;
        parameters.put(Parameter.IS_WRECK, Commons.ON);
        setHappiness(0);
        for (Player p : Player.getValues()) {
            marinesOnBoardWhileSailesRipped.put(p, getMarinesNumber(p, MarinesCompartment.DECK, Commons.BOTH));
        }
    }


    public Map<Player, Integer> getMarinesOnDeckWhileSailedRipped() {
        return marinesOnBoardWhileSailesRipped;
    }


    // RUCH OKRETU
    public Coordinate getPosition() {
        return position;
    }


    public void setPosition(Coordinate coord) {
        setPosition(coord.getA(), coord.getB());
    }


    public void setPosition(int a, int b) {
        position.set(a, b);
    }


    public RotateDirection getRotation() {
        return rotation;
    }


    public void setRotation(RotateDirection value) {
        rotation = value;
    }


    public MovesQueueCode getMovesQueueCode() {
        return movesQueueCode;
    }


    public boolean isMovementPossible(MovementType movement) {
        if (movesQueueCode == MovesQueueCode.END)
            return false;

        if (movement == MovementType.TRANSFER) {
            if (movesQueueCode == MovesQueueCode.MOVE_ROTATE || movesQueueCode == MovesQueueCode.ROTATE_MOVE_ROTATE)
                return false;
            return true;
        } else if (movement == MovementType.ROTATE)
            return true;
        else
            return false; // nieoczekiwana wartość
    }


    public void addMovementCode(MovementType type) {
        if (type == MovementType.TRANSFER) {
            switch (movesQueueCode) {
            case NEW:
                movesQueueCode = MovesQueueCode.MOVE;
                break;
            case ROTATE:
                movesQueueCode = MovesQueueCode.ROTATE_MOVE;
                break;
            }
        }

        if (type == MovementType.ROTATE) {
            switch (movesQueueCode) {
            case NEW:
                movesQueueCode = MovesQueueCode.ROTATE;
                break;
            case MOVE:
                movesQueueCode = MovesQueueCode.MOVE_ROTATE;
                break;
            case ROTATE_MOVE:
                movesQueueCode = MovesQueueCode.ROTATE_MOVE_ROTATE;
                break;
            }
        }

        if (type == MovementType.NEW_MOVE)
            movesQueueCode = MovesQueueCode.NEW;
        if (type == MovementType.END_MOVE)
            movesQueueCode = MovesQueueCode.END;
    }


    public void useHelm(int amount) {
        helm[Commons.READY] -= amount;
        helm[Commons.USED] += amount;
    }


    // SZTORM
    public void setSails() {
        sailesRipped = false;
        setParameter(Parameter.IS_WRECK, Commons.OFF);
        for (Player p : Player.getValues())
            marinesOnBoardWhileSailesRipped.put(p, 0);
    }


    // OSTRZAL
    public void shoot(Player player, GunCompartment location, Gun _type) {
        cannonSection.useCannon(location, _type);
        marinesSection.useMarines(player, MarinesCompartment.BATTERIES, _type.getCrewSize());
    }


    // ABORDAZ
    public int escapeFromBoarding() {
        shipsCoupled.clear();
        // TODO
        return Commons.NIL;
    }


    // MIELIZNY
    public boolean checkEscapeAttempt(ShallowAttempt type) {
        // par. 17.6
        if (turnEscapeAttemptUsed || escapeAttemptsUsed.contains(type))
            return false;
        // --

        // Nie można przeprowadzać dwóch prób równocześnie.
        if (pullOnAnchorAttemptCarried || towByOneAttemptCarried)
            return false;

        // par. 17.8.1
        if (type == ShallowAttempt.DROP_SILVER) {
            if (load.get(CargoType.SILVER) == 0)
                return false;
        }
        // --

        // par. 17.9.1
        if (type == ShallowAttempt.DROP_CANNONS) {
            if (cannonSection.getTotalCannonNumber() == 0)
                return false;
        }
        // --

        return true;
    }


    public Coordinate getPreviousTurnPosition() {
        return previousTurnPosition;
    }


    public void makeEscapeAttempt(ShallowAttempt type) {
        turnEscapeAttemptUsed = true; // par. 17.6

        escapeAttemptsUsed.add(type);

        switch (type) {
        case DROP_SILVER:
            unloadCargo(CargoType.SILVER, Commons.INF);
            break;
        case DROP_CANNONS:
            cannonSection.clear();
            break;
        case PULL_ANCHOR:
            actionsOver = Commons.ON; // par. 17.10.1
            pullOnAnchorAttemptCarried = true;
            break;
        case TOW_BY_ONE:
            towByOneAttemptCarried = true;
            break;
        }
    }


    // HOLOWANIE
    public Integer getTowedBy() {
        return towedByID;
    }


    public void setTowedBy(Integer ship) {
        towedByID = ship;
    }


    public void clearTowedBy() {
        towedByID = null;
    }


    public Integer getTowOther() {
        return towOtherID;
    }


    public void setTowOther(Integer ship) {
        towOtherID = ship;
    }


    public void clearTow() {
        towOtherID = null;
    }


    public int getCannonsNumber(GunCompartment location, Gun _type, int _state) {
        return cannonSection.getCannonsNumber(location, _type, _state);
    }


    public int destroyCannon(GunCompartment location, Gun _type, int _state) throws IllegalArgumentException {
        return cannonSection.destroyCannon(location, _type, _state);
    }


    public void loadCargo(CargoType type, int amount) {
        load.put(type, load.get(type) + amount);

        if (type == CargoType.CANNONS_MEDIUM)
            load.put(CargoType.FREE_SPACE, load.get(CargoType.FREE_SPACE) - amount * 2);
        else
            load.put(CargoType.FREE_SPACE, load.get(CargoType.FREE_SPACE) - amount);
    }


    public void unloadCargo(CargoType type, int amount) {
        if (amount == Commons.INF)
            load.put(type, 0);
        else
            load.put(type, load.get(type) - amount);

        if (type == CargoType.CANNONS_MEDIUM)
            load.put(CargoType.FREE_SPACE, load.get(CargoType.FREE_SPACE) + amount * 2);
        else
            load.put(CargoType.FREE_SPACE, load.get(CargoType.FREE_SPACE) + amount);
    }


    public int getLoad(CargoType type) {
        return load.get(type);
    }


    public void setPullOnAnchorAttemptCarried(boolean state) {
        pullOnAnchorAttemptCarried = state;
    }


    public boolean isPullOnAnchorAttemptCarried() {
        return pullOnAnchorAttemptCarried;
    }


    public void setTowByOneAttemptCarried(boolean state) {
        towByOneAttemptCarried = state;
    }


    public boolean isTowByOneAttemptCarried() {
        return towByOneAttemptCarried;
    }


    public boolean isOnGameBoard() {
        if (position.getA() == Commons.NIL || position.getB() == Commons.NIL)
            return false;
        if (parameters.get(Parameter.IS_OUTSIDE_MAP) == Commons.ON || parameters.get(Parameter.IS_SUNK) == Commons.ON)
            return false;

        return true;
    }


    public void prepareForNewTurn() {
        movesQueueCode = MovesQueueCode.NEW;
        actionsOver = Commons.OFF;

        helm[Commons.READY] += helm[Commons.USED];
        helm[Commons.USED] = 0;
        distanceMoved = 0;

        cannonSection.prepareForNewTurn();
        // marynarze musza byc "wyzerowani" przed tura abordazowa
        marinesSection.prepareForNewTurn();

        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                boardingActionUsed[i][j] = 0;

        previousTurnPosition = position;
        turnEscapeAttemptUsed = false;
    }


    public void repairHull(int points) {
        durability += points;
    }


    public void repairMast(int points) {
        mast += points;
    }


    public void repairHelm(int points) {
        helm[Commons.READY] += points;
    }


    public void modifyCannonsNumber(GunCompartment location, Gun type, int number) {
        cannonSection.modifyCannonsNumber(location, type, number);
    }


    public void clearPlayerMarines(Player player, MarinesCompartment location) {
        marinesSection.clear(player, location);
    }


    public int calculateRepairCosts() {
        int value = 0;

        value += (shipClass.getDurabilityMax() - durability) * RepairType.DURABILITY.getCost();
        value += (shipClass.getMastMax() - mast) * RepairType.MAST.getCost();
        value += (shipClass.getHelmMax() - (helm[Commons.READY] + helm[Commons.USED])) * RepairType.HELM.getCost();

        for (GunCompartment c : GunCompartment.values()) {
            if (c == GunCompartment.NONE)
                continue;
            for (Gun t : Gun.values()) {
                if (t == Gun.NONE)
                    continue;

                value += (shipClass.getCannonMax()[c.ordinal()][t.ordinal()] - getCannonsNumber(c, t, Commons.BOTH))
                        * t.getPrice(DealType.BUY);
            }
        }

        return value;
    }


    public void writeToFile(DataOutputStream data_output) {
        try {
            data_output.writeInt(shipID);
            data_output.writeInt(owner.ordinal());
            data_output.writeInt(firstOwner.ordinal());
            data_output.writeInt(shipClass.ordinal());
            data_output.writeInt(internedBy.ordinal());

            data_output.writeInt(position.getA());
            data_output.writeInt(position.getB());
            data_output.writeInt(rotation.ordinal());

            data_output.writeInt(movesQueueCode.ordinal());
            data_output.writeInt(actionsOver);
            data_output.writeInt(distanceMoved);

            data_output.writeInt(durability);
            data_output.writeInt(happiness);
            data_output.writeInt(helm[Commons.READY]);
            data_output.writeInt(helm[Commons.USED]);
            data_output.writeInt(mast);

            cannonSection.writeToStream(data_output);
            marinesSection.writeToStream(data_output);

            for (CargoType t : CargoType.getValues())
                data_output.writeInt(load.get(t));

            data_output.writeInt(boardingFirstTurn.ordinal());

            for (int i = 0; i < MarinesCompartment.getSize(); i++)
                for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                    data_output.writeInt(boardingActionUsed[i][j]);

            for (Parameter p : Parameter.values())
                data_output.writeInt(parameters.get(p));

            data_output.writeBoolean(sailesRipped);

            for (Player p : Player.getValues())
                data_output.writeInt(marinesOnBoardWhileSailesRipped.get(p));

            data_output.writeInt(towedByID);
            data_output.writeInt(towOtherID);

            data_output.writeBoolean(turnEscapeAttemptUsed);
            data_output.writeInt(escapeAttemptsUsed.size());
            for (ShallowAttempt sa : escapeAttemptsUsed)
                data_output.writeUTF(sa.name());
            data_output.writeBoolean(pullOnAnchorAttemptCarried);
            data_output.writeBoolean(towByOneAttemptCarried);
            data_output.writeInt(previousTurnPosition.getA());
            data_output.writeInt(previousTurnPosition.getB());

            data_output.writeInt(shipsCoupled.size());
            for (int i = 0; i < shipsCoupled.size(); i++) {
                data_output.writeInt(shipsCoupled.elementAt(i));
                data_output.writeInt(coupledReasons.elementAt(i).ordinal());
            }

            data_output.writeBoolean(happinessSunk);
            data_output.writeBoolean(happinessBoarding);
            data_output.writeBoolean(happinessCapture);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    public void readFromFile(DataInputStream data_input) {
        int dummy = 0;
        int counter = 0;

        try {
            shipID = data_input.readInt();
            owner = Player.valueOf(data_input.readInt());
            firstOwner = Player.valueOf(data_input.readInt());

            shipClass = ShipClass.valueOf(data_input.readInt());

            internedBy = Player.valueOf(data_input.readInt());

            dummy = data_input.readInt();
            position.set(dummy, data_input.readInt());

            rotation = RotateDirection.valueOf(data_input.readInt());

            movesQueueCode = MovesQueueCode.valueOf(data_input.readInt());
            actionsOver = data_input.readInt();
            distanceMoved = data_input.readInt();

            durability = data_input.readInt();
            happiness = data_input.readInt();
            helm[Commons.READY] = data_input.readInt();
            helm[Commons.USED] = data_input.readInt();
            mast = data_input.readInt();

            cannonSection.readFromStream(data_input);
            marinesSection.readFromStream(data_input);

            for (CargoType t : CargoType.getValues())
                load.put(t, data_input.readInt());

            boardingFirstTurn = BoardingFirstTurn.valueOf(data_input.readInt());

            for (int i = 0; i < MarinesCompartment.getSize(); i++)
                for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                    boardingActionUsed[i][j] = data_input.readInt();

            for (Parameter p : Parameter.values())
                parameters.put(p, data_input.readInt());

            sailesRipped = data_input.readBoolean();

            for (Player p : Player.getValues())
                marinesOnBoardWhileSailesRipped.put(p, data_input.readInt());

            towedByID = data_input.readInt();
            towOtherID = data_input.readInt();

            turnEscapeAttemptUsed = data_input.readBoolean();
            dummy = data_input.readInt();
            for (int i = 0; i < dummy; i++)
                escapeAttemptsUsed.add(ShallowAttempt.valueOf(data_input.readUTF()));
            pullOnAnchorAttemptCarried = data_input.readBoolean();
            towByOneAttemptCarried = data_input.readBoolean();

            dummy = data_input.readInt();
            previousTurnPosition.set(dummy, data_input.readInt());

            shipsCoupled.clear();
            coupledReasons.clear();
            counter = data_input.readInt();
            for (int i = 0; i < counter; i++) {
                shipsCoupled.add(data_input.readInt());

                dummy = data_input.readInt();
                switch (dummy) {
                case 0:
                    coupledReasons.add(CoupleReason.BOARDING);
                    break;
                case 1:
                    coupledReasons.add(CoupleReason.HANDLING);
                    break;
                default:
                    System.err.print("Unknown couple reason value\n");
                }
            }

            happinessSunk = data_input.readBoolean();
            happinessBoarding = data_input.readBoolean();
            happinessCapture = data_input.readBoolean();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    public Coordinate checkAngleToRotate(RotateDirection towedByRotation, RotateDirection towOtherRotation) {
        /*
         * Funkcja zwraca maksymalny możliwy zakres obrotu statku.
         */

        // par. 12.3
        if (getShipsCoupled().size() > 0)
            return new Coordinate(0, 0);
        // --

        // par. 17.2
        if (getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON)
            return new Coordinate(0, 0);
        // --

        if (getParameter(Parameter.IS_WRECK) == Commons.ON || towedByRotation != null) {
            return new Coordinate(0, 0);
        }

        if (getMovesQueueCode() == MovesQueueCode.END)
            return new Coordinate(0, 0);

        if (getTowOther() != null) {
            // par. 16.8
            Coordinate crd = new Coordinate(-Math.min(1, getHelm(Commons.READY)), Math.min(1, getHelm(Commons.READY)));
            // --

            // par. 8.5.3
            if (RotateDirection.rotate(getRotation(), -4) == towOtherRotation)
                crd.setA(0);

            if (RotateDirection.rotate(getRotation(), +4) == towOtherRotation)
                crd.setB(0);
            // --

            return crd;
        }

        return new Coordinate(-getHelm(Commons.READY), getHelm(Commons.READY));
    }


    public CommanderState getCommanderOnBoardState(Player player) {
        for (MarinesCompartment loc : MarinesCompartment.values()) {
            if (loc == MarinesCompartment.NONE)
                continue;
            if (getCommanderState(player, loc) != CommanderState.NOT_THERE)
                return getCommanderState(player, loc);
        }

        return CommanderState.NOT_THERE;
    }


    public void escapeFromShallow() {
        setParameter(Parameter.IS_IMMOBILIZED, Commons.OFF);
        setTurnEscapeAttemptUsed(false);
        clearEscapeAttemptUsed();
        setPullOnAnchorAttemptCarried(false);
        setTowByOneAttemptCarried(false);
    }


    public void plantExplosives() {
        setParameter(Parameter.IS_EXPLOSIVE, Commons.ON);
    }


    public int calculateSellPrice() {
        // par. 20.4.2
        int price = getShipClass().getPrice() / 2;

        price -= (getShipClass().getDurabilityMax() - getDurability()) * RepairType.DURABILITY.getCost();

        price -= (getShipClass().getMastMax() - getMast()) * RepairType.MAST.getCost();

        price -= (getShipClass().getHelmMax() - getHelm(Commons.BOTH)) * RepairType.HELM.getCost();

        for (GunCompartment gc : GunCompartment.values()) {
            if (gc == GunCompartment.NONE)
                continue;
            for (Gun t : Gun.values()) {
                if (t == Gun.NONE)
                    continue;
                price -= (getShipClass().getCannonMax()[gc.ordinal()][t.ordinal()] - getCannonsNumber(gc, t,
                        Commons.BOTH)) * t.getPrice(DealType.BUY);
            }
        }

        int marinesTotal = 0;
        for (MarinesCompartment mc : MarinesCompartment.values()) {
            if (mc == MarinesCompartment.NONE || mc == MarinesCompartment.SHIP_X)
                continue;
            // FIXME
            // marinesTotal += getMarinesNumber(currentPlayer, mc,
            // Commons.BOTH);
        }

        if (marinesTotal < getShipClass().getCrewMax())
            price -= getShipClass().getCrewMax() - marinesTotal;

        if (price < 0)
            return Commons.NIL;
        else
            return price;
    }

}