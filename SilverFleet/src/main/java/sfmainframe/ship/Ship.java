package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sfmainframe.Commons;
import sfmainframe.Coordinate;
import sfmainframe.Player;
import sfmainframe.Range;
import sfmainframe.ReusableElement;
import sfmainframe.board.Board;
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

    /**
     * ID of the ship in the game. Used for player convenience only.
     * <p>
     * Numbering starts with 1.
     */
    private final int id;

    /**
     * Type (class) of the ship.
     */
    private final ShipClass shipClass;

    /**
     * Current owner of the ship (or <code>NONE</code> if no owner).
     */
    private Player owner;

    /**
     * Owner at the beginning of a current gameplay.
     */
    private Player initialOwner;

    /**
     * Player who interned this ship.
     */
    private Player internedBy;

    /**
     * Current position on the board.
     */
    private Coordinate position;

    /**
     * Rotation on the board.
     */
    private RotateDirection rotation;

    /**
     * Code of the current ship's movement phase (eg. move, rotate etc.)
     */
    private MovesQueueCode movesQueueCode;

    /**
     * Ship's durability.
     */
    private int durability;

    /**
     * Number of points of happiness.
     */
    private int happiness;

    /**
     * Helm points.
     */
    private ReusableElement helm;

    /**
     * Number of mast points.
     */
    private int mast;

    /**
     * Distance moved (in hexes) so far in the current turn.
     */
    private int distanceMoved;

    /**
     * Cargo hold.
     */
    private final CargoHold cargoHold;

    /**
     * Cannon section.
     */
    private CannonSection cannonSection;

    /**
     * Marines section.
     */
    private MarinesSection marinesSection;

    private BoardingFirstTurn boardingFirstTurn; // XXX: wtf?

    /**
     * Boarding actions used in TODO
     */
    private Map<MarinesCompartment, Map<Player, Integer>> boardingActionUsed;

    /**
     * Descripes state of the ship (eg. is a wreck, are explosives set etc.)
     */
    private final Set<Parameter> parameters;

    /**
     * Number of marines on board when sails were ripped.
     */
    private final Map<Player, Integer> marinesOnBoardWhileSailesRipped; // XXX:
                                                                        // non-final,
                                                                        // immutable?

    /**
     * Ship which is a tug for this ship.
     */
    private Ship towedBy;

    /**
     * Ship which is towed by this ship.
     */
    private Ship towOther;

    /**
     * Escape attempt used during the current stuck.
     */
    private final Set<ShallowAttempt> escapeAttemptsUsed;

    /**
     * Position of the ship in the previous turn.
     */
    private Coordinate previousTurnPosition;

    /**
     * Map of coupled ships and reasons they are coupled.
     */
    private final Map<Ship, CoupleReason> shipsCoupled;

    /**
     * Fulfilled conditions which result in gain in happiness.
     */
    private final Set<Happiness> gainedHappiness;


    public Ship(Player _owner, ShipClass _class, int _ID) {
        id = _ID;
        owner = _owner;
        initialOwner = _owner;
        shipClass = _class;
        internedBy = Player.NONE;

        position = new Coordinate(Coordinate.dummy);
        rotation = RotateDirection.N;
        movesQueueCode = MovesQueueCode.NEW;

        durability = _class.getDurabilityMax();
        happiness = 0;

        helm = new ReusableElement();
        helm.setReady(_class.getHelmMax());
        mast = _class.getMastMax();
        distanceMoved = 0;

        cannonSection = new CannonSection(_class);
        marinesSection = new MarinesSection(_class, owner);

        boardingActionUsed = new HashMap<MarinesCompartment, Map<Player, Integer>>();
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            boardingActionUsed.put(mc, new HashMap<Player, Integer>());
            for (Player p : Player.getValues())
                boardingActionUsed.get(mc).put(p, 0);
        }

        cargoHold = new CargoHold(_class.getLoadMax());

        boardingFirstTurn = BoardingFirstTurn.NOT_APPLICABLE;

        parameters = EnumSet.noneOf(Parameter.class);

        marinesOnBoardWhileSailesRipped = new HashMap<Player, Integer>();
        for (Player p : Player.getValues())
            marinesOnBoardWhileSailesRipped.put(p, 0);

        towedBy = null;
        towOther = null;

        escapeAttemptsUsed = new HashSet<ShallowAttempt>();
        previousTurnPosition = new Coordinate(Commons.NIL, Commons.NIL);

        shipsCoupled = new HashMap<Ship, CoupleReason>();

        gainedHappiness = EnumSet.noneOf(Happiness.class);
    }


    // XXX: konstruktory kopiujące?
    public Ship(Ship ship) {
        id = ship.id;
        owner = ship.owner;
        initialOwner = ship.initialOwner;
        shipClass = ship.shipClass;
        internedBy = ship.internedBy;

        position = new Coordinate(ship.position);
        rotation = ship.rotation;
        movesQueueCode = ship.movesQueueCode;

        durability = ship.durability;
        happiness = ship.happiness;

        helm.copy(ship.helm);
        mast = ship.mast;
        distanceMoved = ship.distanceMoved;

        cannonSection = new CannonSection(ship.cannonSection);
        marinesSection = new MarinesSection(ship.marinesSection);

        cargoHold = new CargoHold(ship.cargoHold);

        boardingFirstTurn = ship.boardingFirstTurn;

        parameters = EnumSet.copyOf(ship.parameters);

        marinesOnBoardWhileSailesRipped = new HashMap<Player, Integer>(ship.marinesOnBoardWhileSailesRipped);

        towedBy = ship.towedBy;
        towOther = ship.towOther;

        escapeAttemptsUsed = new HashSet<ShallowAttempt>(ship.escapeAttemptsUsed);
        previousTurnPosition = ship.previousTurnPosition;

        shipsCoupled = new HashMap<Ship, CoupleReason>(ship.shipsCoupled);

        gainedHappiness = EnumSet.copyOf(ship.gainedHappiness);
    }


    public Ship(DataInputStream data_input) throws IOException {
        int dummy = 0;
        int counter = 0;

        id = data_input.readInt();
        owner = Player.valueOf(data_input.readInt());
        initialOwner = Player.valueOf(data_input.readInt());

        shipClass = ShipClass.valueOf(data_input.readInt());

        internedBy = Player.valueOf(data_input.readInt());

        dummy = data_input.readInt();
        position.set(dummy, data_input.readInt());

        rotation = RotateDirection.valueOf(data_input.readInt());

        movesQueueCode = MovesQueueCode.valueOf(data_input.readInt());
        distanceMoved = data_input.readInt();

        durability = data_input.readInt();
        happiness = data_input.readInt();

        helm = new ReusableElement();
        helm.setReady(data_input.readInt());
        helm.setUsed(data_input.readInt());
        mast = data_input.readInt();

        cannonSection.readFromStream(data_input);
        marinesSection.readFromStream(data_input);
        cargoHold = new CargoHold(shipClass.getLoadMax());
        cargoHold.readFromFile(data_input);

        boardingFirstTurn = BoardingFirstTurn.valueOf(data_input.readInt());

        boardingActionUsed = new HashMap<MarinesCompartment, Map<Player, Integer>>();
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            boardingActionUsed.put(mc, new HashMap<Player, Integer>());
            for (Player p : Player.getValues())
                boardingActionUsed.get(mc).put(p, data_input.readInt());
        }

        // FIXME: odczyt ilości?
        parameters = EnumSet.noneOf(Parameter.class);
        // for (Parameter p : Parameter.values())
        // parameters.add(p, data_input.readInt());

        marinesOnBoardWhileSailesRipped = new HashMap<Player, Integer>();
        for (Player p : Player.getValues())
            marinesOnBoardWhileSailesRipped.put(p, data_input.readInt());

        // FIXME: ładowanie danego statku na podstawie ID -> konieczne w
        // osobnej funkcji, po załadowaniu statków
        // towedBy = data_input.readInt();
        // towOther = data_input.readInt();

        escapeAttemptsUsed = new HashSet<ShallowAttempt>();
        dummy = data_input.readInt();
        for (int i = 0; i < dummy; i++)
            escapeAttemptsUsed.add(ShallowAttempt.valueOf(data_input.readUTF()));

        dummy = data_input.readInt();
        previousTurnPosition.set(dummy, data_input.readInt());

        shipsCoupled = new HashMap<Ship, CoupleReason>();
        counter = data_input.readInt();
        for (int i = 0; i < counter; i++) {
            // FIXME: ładowanie danego statku na podstawie ID -> konieczne w
            // osobnej funkcji, po załadowaniu statków
            Ship ship = null;// .add(data_input.readInt());
            CoupleReason reason = CoupleReason.valueOf(data_input.readUTF());

            shipsCoupled.put(ship, reason);
        }

        gainedHappiness = EnumSet.noneOf(Happiness.class);
        for (Happiness h : Happiness.values())
            if (data_input.readBoolean())
                gainedHappiness.add(h);
    }


    public Integer getID() {
        return id;
    }


    public Player getOwner() {
        return owner;
    }


    public void setOwner(Player owner) {
        this.owner = owner;
    }


    public Player getFirstOwner() {
        return initialOwner;
    }


    public void setFirstOwner(Player player) {
        initialOwner = player;
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


    public int getHelm(int state) {
        switch (state) {
        case Commons.READY:
            return helm.getReady();
        case Commons.USED:
            return helm.getUsed();
        case Commons.BOTH:
            return helm.getTotal();
        default:
            throw new IllegalArgumentException("no such state exception");
        }
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


    public boolean isEscapeAttemptUsed(ShallowAttempt type) {
        return escapeAttemptsUsed.contains(type);
    }


    public void clearEscapeAttemptUsed() {
        escapeAttemptsUsed.clear();
    }


    public void setBoardingActionUsed(Player player, MarinesCompartment location, int value) {
        boardingActionUsed.get(location).put(player, value);
    }


    public int getBoardingActionUsed(Player player, MarinesCompartment location) {
        return boardingActionUsed.get(location).get(player);
    }


    public void setBoardingFirstTurn(BoardingFirstTurn value) {
        boardingFirstTurn = value;
    }


    public BoardingFirstTurn isBoardingFirstTurn() {
        return boardingFirstTurn;
    }


    public boolean isParameter(Parameter type) {
        return parameters.contains(type);
    }


    public void setParameter(Parameter type) {
        parameters.add(type);
    }


    public void clearParameter(Parameter type) {
        parameters.remove(type);
    }


    public boolean isHappinessFlagSet(Happiness h) {
        return gainedHappiness.contains(h);
    }


    public void setHappinessFlag(Happiness h) {
        gainedHappiness.add(h);
    }


    public Map<Ship, CoupleReason> getShipsCoupled() {
        return shipsCoupled;
    }


    public boolean isShipCoupled(Ship ship) {
        return shipsCoupled.keySet().contains(ship);
    }


    public void addShipCoupled(Ship ship, CoupleReason reason) {
        shipsCoupled.put(ship, reason);
    }


    public CoupleReason getCoupleReason(Ship ship) {
        return shipsCoupled.get(ship);
    }


    public void uncouple(Ship ship) {
        shipsCoupled.remove(ship);
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
        mast = Math.max(0, mast - value);
    }


    public void destroyHelm(int value) {
        if (value <= helm.getReady())
            helm.modifyReady(-value);
        else {
            value -= helm.getReady();
            helm.setReady(0);
        }

        helm.setUsed(Math.max(0, helm.getUsed() - value));
    }


    public void modifyHappiness(int value) {
        // par. 18.9
        happiness = Math.max(0, happiness + value);
        // --
    }


    public int getPlayerMarinesOnShip(Player player, boolean withCommanders) {
        int totalNumber = 0;
        for (MarinesCompartment loc : MarinesCompartment.getShipCompartments()) {
            totalNumber += getMarinesNumber(player, loc, Commons.BOTH);
            if (withCommanders)
                if (getCommanderState(player, loc) == CommanderState.READY
                        || getCommanderState(player, loc) == CommanderState.USED)
                    totalNumber += 1;
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


    // FIXME: "return Object[]" brzydko wygląda
    public Object[] killMarines(Player player, MarinesCompartment location, int amount, KillingMode mode) {
        return marinesSection.killMarines(player, location, amount, mode);
    }


    public boolean isCommanderOnboard(Player player) {
        return marinesSection.isCommanderOnboard(player);
    }


    public void ripSailes() {
        setParameter(Parameter.SAILES_RIPPED);
        setParameter(Parameter.IS_WRECK);
        setHappiness(0);
        for (Player p : Player.getValues()) {
            marinesOnBoardWhileSailesRipped.put(p, getMarinesNumber(p, MarinesCompartment.DECK, Commons.BOTH));
        }
    }


    public Map<Player, Integer> getMarinesOnDeckWhileSailedRipped() {
        return marinesOnBoardWhileSailesRipped;
    }


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

        switch (movement) {
        case TRANSFER:
            return (movesQueueCode != MovesQueueCode.MOVE_ROTATE && movesQueueCode != MovesQueueCode.ROTATE_MOVE_ROTATE);
        case ROTATE:
            return true;
        default:
            throw new IllegalArgumentException();
        }
    }


    public void nextMovementCode(MovementType type) {
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
        helm.use(amount);
    }


    public void setSails() {
        clearParameter(Parameter.SAILES_RIPPED);
        clearParameter(Parameter.IS_WRECK);
        for (Player p : Player.getValues())
            marinesOnBoardWhileSailesRipped.put(p, 0);
    }


    public void shoot(Player player, GunCompartment location, Gun _type) {
        cannonSection.useCannon(location, _type);
        marinesSection.useMarines(player, MarinesCompartment.BATTERIES, _type.getCrewSize());
    }


    public int escapeFromBoarding() {
        // shipsCoupled.clear();
        // TODO: wtf?
    	throw new UnsupportedOperationException("not yet implemented");
    }


    public boolean checkEscapeAttempt(ShallowAttempt type) {
        // par. 17.6
        if (isParameter(Parameter.TURN_ESCAPE_ATTEMPT_USED) || escapeAttemptsUsed.contains(type))
            return false;
        // --

        // Nie można przeprowadzać dwóch prób równocześnie.
        if (isParameter(Parameter.PULL_ON_ANCHOR_ATTEMPT_CARRIED)
                || isParameter(Parameter.TOWED_BY_ONE_ATTEMPT_CARRIED))
            return false;

        // par. 17.8.1
        if (type == ShallowAttempt.DROP_SILVER) {
            if (cargoHold.getLoad(CargoType.SILVER) == 0)
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
        setParameter(Parameter.TURN_ESCAPE_ATTEMPT_USED); // par. 17.6

        escapeAttemptsUsed.add(type);

        switch (type) {
        case DROP_SILVER:
            cargoHold.unloadAll(CargoType.SILVER);
            break;
        case DROP_CANNONS:
            cannonSection.clear();
            break;
        case PULL_ANCHOR:
            setParameter(Parameter.ACTIONS_OVER); // par. 17.10.1
            setParameter(Parameter.PULL_ON_ANCHOR_ATTEMPT_CARRIED);
            break;
        case TOW_BY_ONE:
            setParameter(Parameter.TOWED_BY_ONE_ATTEMPT_CARRIED);
            break;
        }
    }


    public Ship getTowedBy() {
        return towedBy;
    }


    public void setTowedBy(Ship ship) {
        towedBy = ship;
    }


    public Ship getTowOther() {
        return towOther;
    }


    public void setTowOther(Ship ship) {
        towOther = ship;
    }


    public int getCannonsNumber(GunCompartment location, Gun _type, int _state) {
        return cannonSection.getCannonsNumber(location, _type, _state);
    }


    public void destroyCannon(GunCompartment location, Gun _type, int _state) throws IllegalArgumentException {
        cannonSection.destroyCannon(location, _type, _state);
    }


    public void loadCargo(CargoType type, int amount) {
        cargoHold.load(type, amount);
    }


    public void unloadCargo(CargoType type, int amount) {
        cargoHold.unload(type, amount);
    }


    public void unloadAll(CargoType type) {
        cargoHold.unloadAll(type);
    }


    public int getLoad(CargoType type) {
        return cargoHold.getLoad(type);
    }


    public boolean isOnGameBoard() {
        if (!position.isValid() || isParameter(Parameter.IS_SUNK))
            return false;

        return true;
    }


    public void prepareForNewTurn() {
        movesQueueCode = MovesQueueCode.NEW;
        clearParameter(Parameter.ACTIONS_OVER);

        helm.refresh();
        distanceMoved = 0;

        cannonSection.prepareForNewTurn();
        // marynarze musza byc "wyzerowani" przed tura abordazowa
        marinesSection.prepareForNewTurn();

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments())
            for (Player p : Player.getValues())
                boardingActionUsed.get(mc).put(p, 0);

        previousTurnPosition = position;
        clearParameter(Parameter.TURN_ESCAPE_ATTEMPT_USED);
    }


    public void repairHull(int points) {
        durability += points;
    }


    public void repairMast(int points) {
        mast += points;
    }


    public void repairHelm(int points) {
        helm.modifyReady(points);
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
        value += (shipClass.getHelmMax() - helm.getTotal()) * RepairType.HELM.getCost();

        for (GunCompartment c : GunCompartment.getValues()) {
            for (Gun t : Gun.getValues()) {
                value += (shipClass.getCannonMax()[c.ordinal()][t.ordinal()] - getCannonsNumber(c, t, Commons.BOTH))
                        * t.getPrice(DealType.BUY);
            }
        }

        return value;
    }


    public void writeToFile(DataOutputStream data_output) throws IOException {
        data_output.writeInt(id);
        data_output.writeInt(owner.ordinal());
        data_output.writeInt(initialOwner.ordinal());
        data_output.writeInt(shipClass.ordinal());
        data_output.writeInt(internedBy.ordinal());

        data_output.writeInt(position.getA());
        data_output.writeInt(position.getB());
        data_output.writeInt(rotation.ordinal());

        data_output.writeInt(movesQueueCode.ordinal());
        data_output.writeInt(distanceMoved);

        data_output.writeInt(durability);
        data_output.writeInt(happiness);
        data_output.writeInt(helm.getReady());
        data_output.writeInt(helm.getUsed());
        data_output.writeInt(mast);

        cannonSection.writeToStream(data_output);
        marinesSection.writeToStream(data_output);
        cargoHold.writeToFile(data_output);

        data_output.writeInt(boardingFirstTurn.ordinal());

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues())
                data_output.writeInt(boardingActionUsed.get(mc).get(p));
        }

        // FIXME: zapis
        // for (Parameter p : Parameter.values())
        // data_output.writeInt(parameters.get(p));

        for (Player p : Player.getValues())
            data_output.writeInt(marinesOnBoardWhileSailesRipped.get(p));

        data_output.writeInt(towedBy.getID());
        data_output.writeInt(towOther.getID());

        data_output.writeInt(escapeAttemptsUsed.size());
        for (ShallowAttempt sa : escapeAttemptsUsed)
            data_output.writeUTF(sa.name());
        data_output.writeInt(previousTurnPosition.getA());
        data_output.writeInt(previousTurnPosition.getB());

        data_output.writeInt(shipsCoupled.size());
        for (Ship s : shipsCoupled.keySet()) {
            data_output.writeInt(s.getID());
            data_output.writeUTF(shipsCoupled.get(s).name());
        }

        for (Happiness h : Happiness.values())
            data_output.writeBoolean(gainedHappiness.contains(h));
    }


    // TODO: usunąc parametry
    public Range checkAngleToRotate(RotateDirection towedByRotation, RotateDirection towOtherRotation) {
        /*
         * Funkcja zwraca maksymalny możliwy zakres obrotu statku.
         */

        // par. 12.3
        if (getShipsCoupled().size() > 0)
            return new Range(0, 0);
        // --

        // par. 17.2
        if (isParameter(Parameter.IS_IMMOBILIZED))
            return new Range(0, 0);
        // --

        // XXX: o co tu chodzi? w tym "!= null"?
//        if (isParameter(Parameter.IS_WRECK) || towedByRotation != null) {
        if (isParameter(Parameter.IS_WRECK)) {
            return new Range(0, 0);
        }

        if (getMovesQueueCode() == MovesQueueCode.END)
            return new Range(0, 0);

        if (getTowOther() != null) {
            // par. 16.8
            Range range = new Range(-Math.min(1, helm.getReady()), Math.min(1, helm.getReady()));
            // --

            // par. 8.5.3
            RotateDirection nextLeft = RotateDirection.rotate(getRotation(), -1);
            if (Board.getNextHexCoords(position, nextLeft).equals(getTowOther().position))
                range.setLowerBound(0);
            
            RotateDirection nextRight = RotateDirection.rotate(getRotation(), 1);
            if (Board.getNextHexCoords(position, nextRight).equals(getTowOther().position))
                range.setUpperBound(0);
            // --

            return range;
        }

        return new Range(-helm.getReady(), helm.getReady());
    }


    public CommanderState getCommanderOnBoardState(Player player) {
        for (MarinesCompartment loc : MarinesCompartment.getShipCompartments()) {
            if (getCommanderState(player, loc) != CommanderState.NOT_THERE)
                return getCommanderState(player, loc);
        }

        return CommanderState.NOT_THERE;
    }


    public void escapeFromShallow() {
        clearParameter(Parameter.IS_IMMOBILIZED);
        clearParameter(Parameter.TURN_ESCAPE_ATTEMPT_USED);
        clearEscapeAttemptUsed();
        clearParameter(Parameter.PULL_ON_ANCHOR_ATTEMPT_CARRIED);
        clearParameter(Parameter.TOWED_BY_ONE_ATTEMPT_CARRIED);
    }


    public void plantExplosives() {
        setParameter(Parameter.IS_EXPLOSIVE);
    }


    public int calculateSellPrice() {
        // par. 20.4.2
        int price = getShipClass().getPrice() / 2;

        price -= (getShipClass().getDurabilityMax() - getDurability()) * RepairType.DURABILITY.getCost();

        price -= (getShipClass().getMastMax() - getMast()) * RepairType.MAST.getCost();

        price -= (getShipClass().getHelmMax() - getHelm(Commons.BOTH)) * RepairType.HELM.getCost();

        for (GunCompartment gc : GunCompartment.getValues()) {
            for (Gun t : Gun.getValues()) {
                price -= (getShipClass().getCannonMax()[gc.ordinal()][t.ordinal()] - getCannonsNumber(gc, t,
                        Commons.BOTH)) * t.getPrice(DealType.BUY);
            }
        }

        int marinesTotal = 0;
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            marinesTotal += getMarinesNumber(owner, mc, Commons.BOTH);
        }

        if (marinesTotal < getShipClass().getCrewMax())
            price -= getShipClass().getCrewMax() - marinesTotal;

        return price;
    }

    public String getCaption() { 
    	return String.format("%d, %s", id, shipClass);
    }
}