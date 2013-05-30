package sfmainframe.gameplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import sfmainframe.Commons;
import sfmainframe.Coordinate;
import sfmainframe.Dice;
import sfmainframe.MainBoard;
import sfmainframe.Player;
import sfmainframe.PlayerClass;
import sfmainframe.board.Board;
import sfmainframe.board.Hex;
import sfmainframe.board.RotateDirection;
import sfmainframe.board.Terrain;
import sfmainframe.gameplay.between.Auction;
import sfmainframe.gameplay.between.RepairType;
import sfmainframe.gui.DisplayMode;
import sfmainframe.gui.Tabs;
import sfmainframe.gui.UpdateMode;
import sfmainframe.ship.BoardingFirstTurn;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;
import sfmainframe.ship.Happiness;
import sfmainframe.ship.Parameter;
import sfmainframe.ship.Ship;
import sfmainframe.ship.ShipClass;
import sfmainframe.ship.cargo.CargoType;
import sfmainframe.ship.cargo.TransferCargo;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class Game {

    private static final String[] internedOptions = { "Return", "Sell", "Destroy" };

    private static final int MIN_WIND_SPEED = 0;
    private static final int MAX_WIND_SPEED = 12;

    private int conflictID; // ID rozgrywki
    private int turnID; // ID tury
    private Stage stage; // etap (kierunek i sila wiatru / ruch gracza itd.)
    private Player currentPlayer;

    private int windSpeed;
    private RotateDirection windDirection;

    private Board board;
    private final List<Ship> ships;
    private List<PlayerClass> players;

    // TODO : 3 itd. - sabotaż...
    /**
     * Stores information about how many times a player has been "processed":
     * <ul>
     * <li>0 - not processed</li>
     * <li>1 - normal turn processed</li>
     * <li>2 - boarding turn processed (only if applicable)</li>
     * </ul>
     */
    private Map<Player, Integer> processed = new HashMap<Player, Integer>();

    private Set<Player> agressors = new HashSet<Player>();
    private Set<Player> defenders = new HashSet<Player>();

    private Map<ShipClass, Integer> bank;
    private List<Auction> auctions;
    private int auctionsCounter;


    // ---

    public Game() {
        board = new Board(Board.WIDTH_MAX, Board.HEIGHT_MAX);

        ships = new ArrayList<Ship>();
        players = new ArrayList<PlayerClass>();

        // TODO : obecnie domyślnie 8 graczy
        for (Player p : Player.getValues())
            players.add(new PlayerClass(p));

        stage = Stage.PRE_CONFLICT_OPERATIONS;

        conflictID = 0;
        turnID = 0;
        windSpeed = Commons.NIL;
        windDirection = RotateDirection.N;
        currentPlayer = Player.NONE;

        auctions = new ArrayList<Auction>();
        auctionsCounter = 1;

        bank = new HashMap<ShipClass, Integer>();
        for (ShipClass c : ShipClass.values())
            bank.put(c, 0);
    }


    public void init() {
        // wczytanie mapy
        String path = MainBoard.class.getResource("maps/map.hex").getPath();
        path = path.substring(1);
        path = path.replace("%20", " ");
        // File file = new File(path);

        // File file = new File("maps/map.hex");
        // FileInputStream file_input;

        try {
            // file_input = new FileInputStream(file);

            // DataInputStream data_in = new DataInputStream (file_input);

            InputStream data_in = MainBoard.class.getResourceAsStream("maps/map.hex");
            board.readData(data_in);

            // file_input.close ();
        } catch (IOException e) {
            JOptionPane.showConfirmDialog(null, "No map file.");
            System.out.println("IO exception = " + e);
        }

        stage = Stage.PRE_CONFLICT_OPERATIONS;

        for (PlayerClass p : players)
            p.addAlly(p.getIdentity());

        for (Ship s : ships)
            if (s.getOwner() == Player.HAMPSHIRE)
                s.setHappiness(+1);

    }


    public Board getBoard() {
        return board;
    }


    /**
     * Clear ships from player's marines and recalculate their ownership status.
     * Ships interned by the player are sunk.
     * 
     * @param player
     */
    public void endPlayerGame(Player player) {
        MainBoard.addMessage(player.toString() + " was eliminated.\n");

        for (Ship s : ships) {
            for (MarinesCompartment comp : MarinesCompartment.getShipCompartments()) {
                s.clearPlayerMarines(player, comp);
                s.setCommander(player, comp, CommanderState.NOT_THERE);
            }
            Ships.calculateShipOwner(s);

            if (s.getInternedBy() == player)
                sinkShip(s, DestroyShipMode.SINK);
        }

        getPlayer(player).endGame();

        for (PlayerClass p : players) {
            if (p.isAlly(player))
                p.removeAlly(player);

            if (p.getCommanderInternedBy() == player)
                p.setCommanderInternedBy(Player.NONE);
        }

        if (player == currentPlayer)
            endPlayerTurn();
    }


    public boolean checkIfObstacleOnBulletPath(int sourceShipID, int targetShipID, Gun gunType) {
        Coordinate source = getShip(sourceShipID).getPosition();
        Coordinate target = getShip(targetShipID).getPosition();

        return board.checkIfObstacleOnBulletPath(source, target, gunType);
    }


    public GunCompartment calculateSourceGunCompartment(int sourceID, int targetID) {
        Coordinate source = getShip(sourceID).getPosition();
        Coordinate target = getShip(targetID).getPosition();

        return board.calculateSourceGunCompartment(source.diff(target), getShip(targetID).getRotation().ordinal());
    }


    public Player getCurrentPlayer() {
        return currentPlayer;
    }


    /**
     * 
     * @param times
     *            Number of times a player has already been processed.
     * @return
     */
    private int getProcessedPlayersNumber(int times) {
        int number = 0;
        for (Player p : Player.getValues())
            if (processed.get(p) == times)
                number++;
        return number;
    }


    private NextPlayerState determineNextPlayer(Ship ship) {
        boolean apply = false;

        int playersInGameCounter = 0;
        for (PlayerClass p : players) {
            if (p.isInGame())
                playersInGameCounter++;
        }

        if (playersInGameCounter <= 1)
            return NextPlayerState.NO_PLAYERS_LEFT;

        if (stage == Stage.INTERNED_SHIPS) {
            // FIXME: wtf?
            for (Player p : Player.getValues()) {
                processed.put(p, 1);

                int internedShipsNumber = 0;
                for (Player p2 : Player.getValues()) {
                    if (p != p2)
                        internedShipsNumber += getPlayer(p).getInternedShips(p2).size();
                }

                if (internedShipsNumber > 0) {
                    currentPlayer = p;
                    return NextPlayerState.NEXT_PLAYER;
                }
            }

            return NextPlayerState.LAST_PLAYER;
        }

        // FIXME: co, gdy shipID == null?

        // par. 12.4
        if (stage == Stage.BOARDING_MOVEMENTS || stage == Stage.BOARDING_ACTIONS || stage == Stage.BOARDING_SABOTAGE) {
            // par. 12.2.2.3
            Player owner = ship.getOwner();
            if (ship.isBoardingFirstTurn() == BoardingFirstTurn.YES
                    && (stage == Stage.BOARDING_MOVEMENTS && processed.get(owner) == 0
                            || stage == Stage.BOARDING_ACTIONS && processed.get(owner) == 1 || stage == Stage.BOARDING_SABOTAGE
                            && processed.get(owner) == 2)) {
                currentPlayer = ship.getOwner();
                processed.put(currentPlayer, processed.get(currentPlayer) + 1);
                return NextPlayerState.NEXT_PLAYER;
            }
            // --
            else {
                // par. 12.2.2.2
                for (Player p : agressors) {
                    if (stage == Stage.BOARDING_MOVEMENTS && processed.get(p) == 0 || stage == Stage.BOARDING_ACTIONS
                            && processed.get(p) == 1 || stage == Stage.BOARDING_SABOTAGE && processed.get(p) == 2) {
                        currentPlayer = p;
                        processed.put(p, processed.get(p) + 1);
                        return NextPlayerState.NEXT_PLAYER;
                    }
                }
                // --

                for (Player p : defenders) {
                    if (stage == Stage.BOARDING_MOVEMENTS && processed.get(p) == 0 || stage == Stage.BOARDING_ACTIONS
                            && processed.get(p) == 1 || stage == Stage.BOARDING_SABOTAGE && processed.get(p) == 2) {
                        currentPlayer = p;
                        processed.put(p, processed.get(p) + 1);
                        return NextPlayerState.NEXT_PLAYER;
                    }
                }

                return NextPlayerState.LAST_PLAYER;
            }
        }
        // --

        // XXX: co to jest za kod?
        int last = Commons.NIL;
        /*
         * Poniższy, okomentowany kod ma sens tylko wtedy, kiedy zaczyna inny
         * gracz niz Pasadena, a póki co takiej możliwosci nie ma :)
         * 
         * if (stage == Stage.DEPLOYMENT && currentPlayer != Player.NONE) //
         * tylko jedna kolejka, potem rozgrywka last =
         * Player.NONE.ordinal()-currentPlayer.ordinal(); else last =
         * Player.values().length;
         */
        if (currentPlayer == Player.NONE)
            last = Player.values().length;
        else
            last = Player.NONE.ordinal() - currentPlayer.ordinal();

        for (int inx = 0; inx < last; inx++) {
            if (currentPlayer == Player.NONE)
                currentPlayer = Player.valueOf(0);
            else {
                /*
                 * if (stage == Stage.DEPLOYMENT)
                 */currentPlayer = Player.valueOf((currentPlayer.ordinal() + 1)); // czekamy
                                                                                  // na
                                                                                  // NONE!
                /*
                 * else currentPlayer =
                 * integerToPlayer((currentPlayer.ordinal()+
                 * 1)%Player.NONE.ordinal());
                 */
            }

            if (currentPlayer == Player.NONE)
                return NextPlayerState.LAST_PLAYER;

            if (stage == Stage.BETWEEN_TURNS) {
                if (getPlayer(currentPlayer).isInGame() && processed.get(currentPlayer) == 0)
                    apply = true;
            }

            if (stage == Stage.DEPLOYMENT) {
                if (getPlayer(currentPlayer).isInGame())
                    apply = true;
            }
            if (stage == Stage.PLAYERS_MOVES) {
                if (!getPlayer(currentPlayer).isInGame())
                    continue;

                // choc jeden okret gracza znajduje sie na planszy
                Object[] fleet = getPlayer(currentPlayer).getFleet().toArray();
                for (int i = 0; i < fleet.length; i++)
                    if (Board.isOnMap(getShip((Integer) (fleet[i])).getPosition())) {
                        apply = true;
                        break;
                    }
            }

            if (apply)
                return NextPlayerState.NEXT_PLAYER;
        }

        throw new IllegalStateException();
    }


    /**
     * Operations performed at the very beginning of every conflict.
     */
    private void preConflictOperations() {
        final int SILVER_PRICE_MODIFIER = 15;

        conflictID++;
        turnID = 0;

        // XXX: potencjalnie niebezpieczne (losowanie bez końca)
        // par. 6.5
        do {
            currentPlayer = Player.valueOf((Dice.roll() + Dice.roll()) % Commons.PLAYERS_MAX);
        } while (!getPlayer(currentPlayer).isInGame());

        // par. 7.1
        windSpeed = Dice.roll();
        windDirection = RotateDirection.valueOf(Dice.roll());
        // --

        for (PlayerClass p : players) {
            if (!p.isInGame())
                continue;

            // par. 5.1.4 (niespłacenie pożyczki)
            if (p.getLoan() > 0) {
                p.endGame();
                MainBoard.addMessage(p.getIdentity().toString() + " didn't pay off his debts.\n");
            }
            // --

            // par. 5.3.7
            if (p.getCommanderInternedBy() != Player.NONE) {
                p.endGame();
                MainBoard.addMessage(p.getIdentity().toString() + " didn't pay off the ransom.\n");
            }
            // --

            // par. 5.3.9
            if (p.getFleet().size() == 0) {
                p.endGame();
                MainBoard.addMessage(p.getIdentity().toString() + " doesn't posess any Ships.\n");
            }
            // --
        }

        // par. 20.1.1
        for (PlayerClass p : players)
            p.setCurrentSilverPrice(Dice.roll() + SILVER_PRICE_MODIFIER);
        // --

        // zaladunek srebra na jednostki Sidonii
        for (Ship s : getPlayer(Player.PASADENA).getFleet())
            s.loadCargo(CargoType.SILVER, s.getShipClass().getLoadMax());

        // par. 5.6.4
        for (Ship s : ships) {
            if (s.getInternedBy() != Player.NONE) {
                sinkShip(s, DestroyShipMode.SINK);
                addShipToBank(ShipClass.NONE);
            }
        }
        // --

        for (int s = 0; s < Commons.SHIPS_MAX; s++)
            getShip(s).setRotation(RotateDirection.N);
    }


    private void deployment() {
        if (currentPlayer == Player.NONE) {
            // rozmieszczenie okretow kazdego gracza na jego polu startowym
            for (Player p : Player.getValues()) {
                for (Ship s : getPlayer(p).getFleet()) {
                    positionSearch: for (int a = 0; a < Board.WIDTH_MAX; a++) {
                        for (int b = 0; b < Board.HEIGHT_MAX; b++) {
                            Hex hex = board.getHex(a, b);
                            if (hex.owner == p && hex.terrain == Terrain.WATER && hex.ship == null) {
                                s.setPosition(a, b);
                                break positionSearch;
                            }
                        }
                    }
                }
            }
        }

        switch (determineNextPlayer(null)) {
        case NO_PLAYERS_LEFT:
            // end game
            return;
        case NEXT_PLAYER:
            MainBoard.switchStageDisplayMode(DisplayMode.DEPLOY_MODE);
            MainBoard.makeHeaderLabel();
            return;
        case LAST_PLAYER:
            stage = Stage.DEFINE_WIND;
            break;
        default:
            throw new IllegalArgumentException();
        }

        MainBoard.switchStageDisplayMode(DisplayMode.CONFLICT_MODE);
        MainBoard.makeHeaderLabel();
    }


    /**
     * Roll a dice to change wind strength and direction [7.2].
     */
    private void defineWind() {
        int roll = Dice.roll();
        if (roll == 1)
            windDirection = RotateDirection.rotate(windDirection, -1);
        if (roll == 6)
            windDirection = RotateDirection.rotate(windDirection, +1);

        roll = Dice.roll();
        if (roll <= 2)
            windSpeed = Math.max(MIN_WIND_SPEED, windSpeed - 1);
        if (roll >= 5)
            windSpeed = Math.min(MAX_WIND_SPEED, windSpeed + 1);
    }


    private void moveWrecks() {
        final int DEADLY_WIND_STRENGTH = 10;
        final int MIN_MARINES_TO_SET_SAILES = 4;

        for (Ship s : ships) {
            if (s.isParameter(Parameter.IS_WRECK)) {
                // par. 15.2
                s.setRotation(windDirection);
                Ships.forcedShipMovement(s, ShipMovementMode.MOVE_WRECK_NORMAL);
                // --
            }

            // par. 15.3
            if (windSpeed > DEADLY_WIND_STRENGTH && Dice.roll() == 6)
                sinkShip(s, DestroyShipMode.SINK);
            // --
        }

        // par. 9.6.1
        for (Ship s : ships) {
            if (!s.isParameter(Parameter.SAILES_RIPPED))
                continue;

            // par. 9.6.1.3
            if (windSpeed < DEADLY_WIND_STRENGTH)
                s.setSails();
            // --

            Player playerInControl = Player.NONE;
            Map<Player, Integer> previousMarinesNumber = s.getMarinesOnDeckWhileSailedRipped();
            int groupSum = 0;

            for (Player plr : Player.getValues()) {
                if (Ships.checkIfPlayerControlsLocation(s, plr, MarinesCompartment.DECK, true)) {
                    playerInControl = plr;
                    break;
                }
            }

            if (playerInControl != Player.NONE) {
                for (Player plr : Player.getValues()) {
                    if (getPlayer(playerInControl).isAlly(plr))
                        groupSum += s.getMarinesNumber(plr, MarinesCompartment.DECK, Commons.BOTH)
                                - previousMarinesNumber.get(plr);
                }

                if (groupSum > MIN_MARINES_TO_SET_SAILES)
                    s.setSails();
            }
        }
        // --
    }


    private void stormAndTow() {
        Ship tugID = null;

        for (Ship s : ships) {
            if (!Board.isOnMap(s.getPosition()))
                continue;

            s.prepareForNewTurn();

            // par. 14.9
            if (s.isParameter(Parameter.IS_EXPLOSIVE))
                sinkShip(s, DestroyShipMode.BLOWUP);
            // --

            if (s.isParameter(Parameter.PULL_ON_ANCHOR_ATTEMPT_CARRIED)) {
                s.clearParameter(Parameter.SAILES_RIPPED);
                // par. 17.10.2, 17.10.3
                if (Ships.rollDice(s, s.getOwner()) + Ships.rollDice(s, s.getOwner()) < s.getMast()) {
                    MainBoard.addMessage("Ship #" + s.getID() + " escaped from treachous waters!\n");
                    s.escapeFromShallow();
                } else {
                    MainBoard.addMessage("Ship #" + s.getID() + " failed to escape from treachous waters!\n");
                    s.setParameter(Parameter.ACTIONS_OVER);
                }
                // --
            }

            if (s.isParameter(Parameter.TOWED_BY_ONE_ATTEMPT_CARRIED)) {
                s.clearParameter(Parameter.TOWED_BY_ONE_ATTEMPT_CARRIED);

                tugID = s.getTowedBy();
                if (s.getTowedBy() != null) {
                    // par. 17.11.4
                    int sizeModifier = 0;
                    if (s.getShipClass().getDurabilityMax() < tugID.getShipClass().getDurabilityMax())
                        sizeModifier = 1;
                    // --

                    // par. 17.11.3
                    if (Ships.rollDice(s, s.getOwner()) + sizeModifier < s.getHelm(Commons.BOTH)
                            + s.getTowedBy().getHelm(Commons.BOTH)) {
                        MainBoard.addMessage("Ship #" + s.getID() + " escaped from treachous waters!\n");
                        s.escapeFromShallow();
                    }
                    // --
                }
                // par. 17.11.5
                else {
                    MainBoard.addMessage("Ship #" + s.getID() + " failed to escape from treachous waters!\n");
                    Ships.throwTow(s);
                }
                // --
            }

            // par. 16.11
            if (windSpeed > 8) {
                if (s.getTowOther() != null || s.getTowedBy() != null)
                    Ships.throwTow(s);
            }
            // --

            // par. 9.1
            if (windSpeed >= 8) {
                if (Ships.forcedShipMovement(s, ShipMovementMode.MOVE_STORM) != MovementEvent.NONE)
                    continue;
            }
            // --

            // par. 9.2, par. 17.13
            if (windSpeed > 10) {
                if (Ships.forcedShipMovement(s, ShipMovementMode.MOVE_STORM) != MovementEvent.NONE)
                    continue;
                Ships.storm(s); // par. 9.4
            }
            // --

            if (s.isParameter(Parameter.IS_IMMOBILIZED)) {
                Ships.damageHull(s, 1); // par. 17.4
                // par. 17.5
                if (windSpeed > 8)
                    Ships.damageHull(s, 1);
                // --
            }
        }

        // przygotowanie marynarzy uzytych w czasie abordazu
        for (Ship s : ships)
            s.prepareForNewTurn();
    }


    private void playersMoves() {
        switch (determineNextPlayer(null)) {
        case NO_PLAYERS_LEFT:
            stage = Stage.INTERNED_SHIPS;
            break;
        case NEXT_PLAYER:
            return;
        case LAST_PLAYER:
            stage = Stage.BOARDING_MOVEMENTS;
            break;
        default:
            MainBoard.addMessage("\n***ERROR***\nUnhandled exception in: determineNextPlayer()\n\n");
        }

        for (Player p : Player.getValues())
            processed.put(p, 0);
        MainBoard.switchStageDisplayMode(DisplayMode.BOARDING_MODE);
    }


    // FIXME: o co tutaj chodzi?!
    private void boardingPhase() {

        for (Ship currentShipBoarding : ships) {
            // FIXME: skomplikowany warunek, magiczne liczby
            if (getProcessedPlayersNumber(3) == Commons.PLAYERS_MAX || !currentShipBoarding.isOnGameBoard()
                    || !Ships.checkIfShipBoarded(currentShipBoarding, currentShipBoarding.getOwner()))
                continue;

            if (getProcessedPlayersNumber(0) == Commons.PLAYERS_MAX) {
                for (Player p : Player.getValues())
                    processed.put(p, 0);
                agressors.clear();
                defenders.clear();

                currentShipBoarding.prepareForNewTurn();

                for (Player p : Player.getValues()) {
                    if (currentShipBoarding.getPlayerMarinesOnShip(p, true) == 0) {
                        // brak marynarzy na pokladzie - nie ma co
                        // przetwarzac
                        processed.put(p, 3);
                    } else {
                        if (getPlayer(currentShipBoarding.getOwner()).isAlly(p))
                            defenders.add(p);
                        else
                            agressors.add(p);
                    }
                }
            }

            switch (determineNextPlayer(currentShipBoarding)) {
            case NO_PLAYERS_LEFT:
                stage = Stage.INTERNED_SHIPS;
                break;
            case NEXT_PLAYER:
                MainBoard.setSelectedShip(currentShipBoarding, Tabs.MARINES);
                return;
            case LAST_PLAYER:
                currentPlayer = Player.NONE;
                if (stage == Stage.BOARDING_MOVEMENTS) {
                    stage = Stage.BOARDING_ACTIONS;
                    MainBoard.makeHeaderLabel();
                    endPlayerTurn();
                    // czy to dobrze ?
                    return;
                } else if (stage == Stage.BOARDING_ACTIONS) {
                    stage = Stage.BOARDING_SABOTAGE;
                    MainBoard.makeHeaderLabel();
                    endPlayerTurn();
                    // czy to dobrze ?
                    return;
                } else
                    continue;
            default:
                throw new IllegalArgumentException();
            }
        }

        for (Player p : Player.getValues())
            processed.put(p, 0);

        if (stage != Stage.INTERNED_SHIPS) {
            stage = Stage.DEFINE_WIND;
            MainBoard.switchStageDisplayMode(DisplayMode.CONFLICT_MODE);
            endPlayerTurn();
            return;
        }
    }


    // FIXME: mieszanie GUI i logiki
    private void internedShips() {
        // par. 5.6.3
        while (determineNextPlayer(null) != NextPlayerState.LAST_PLAYER) {
            for (Player p : Player.getValues()) {
                if (p == currentPlayer)
                    continue;

                for (Ship s : getPlayer(currentPlayer).getInternedShips(p)) {
                    int rv = JOptionPane.showOptionDialog(null, "What to do with this interned ship?",
                            "Ship " + s.getID(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                            internedOptions, internedOptions[2]);

                    if (rv == JOptionPane.YES_OPTION) {
                        getPlayer(p).addShipToFleet(s);
                        continue;
                    } else if (rv == JOptionPane.NO_OPTION) {
                        JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
                        Object[] message = { "Starting price: ", priceSpinner };

                        JOptionPane optionPane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE,
                                JOptionPane.DEFAULT_OPTION);
                        JDialog dialog = optionPane.createDialog(null);
                        dialog.setVisible(true);

                        auctions.add(new Auction(auctionsCounter, 0, s, s.getShipClass(), (Integer) (priceSpinner
                                .getValue())));
                        auctionsCounter++;
                    } else {
                    	// CANCEL_OPTION
                        sinkShip(s, DestroyShipMode.SINK);
                        addShipToBank(ShipClass.NONE); // par. 5.6.6
                    }
                }
            }
        }
        // --

        for (PlayerClass p : players)
            p.clearInternedShips();

        /***/
        stage = Stage.BETWEEN_TURNS;
        endConflict();
        turnID = 0;

        for (Player p : Player.getValues()) {
            if (getPlayer(p).isInGame())
                processed.put(p, 0);
            else
                processed.put(p, 1);
        }
    }


    private void betweenTurns() {
        NextPlayerState st = determineNextPlayer(null);

        if (st == NextPlayerState.NEXT_PLAYER) {
        	// FIXME: uncomment
//            MainBoard.betweenTurnsDialog.update(UpdateMode.DEFAULT);
            MainBoard.addComponentsToPane();
            return;
        } else if (st == NextPlayerState.LAST_PLAYER) {
            turnID++;

            for (Auction a : auctions) {
                if (turnID < a.getStartTurnID() + 2)
                    continue;

                Player winner = Player.NONE;
                int highestBid = a.getStartingPrice();
                for (Player p : Player.getValues()) {
                    if (a.getOffer(p) > highestBid) {
                        highestBid = a.getOffer(p);
                        winner = p;
                    }
                }

                auctions.remove(a);
                if (winner != Player.NONE) {
                    if (a.getOfferedShipID() == null) {
                        launchShip(winner, getFreeID(), a.getOfferedShipClass());
                    } else {
                        // FIXME
                        // getPlayer(winner).addShipToFleet(a.getOfferedShipID());

                        getShip(a.getOfferedShipID()).setInternedBy(Player.NONE);
                        getPlayer(getShip(a.getOfferedShipID()).getOwner()).addGold(highestBid);
                        getShip(a.getOfferedShipID()).setOwner(winner);
                    }

                    getPlayer(winner).removeGold(highestBid);

                    MainBoard.addMessage(String.format("Auction #%d won by %s!", a.getAuctionID(), winner.toString()));
                } else {
                    // par. 5.6.4
                    if (a.getOfferedShipID() != null && getShip(a.getOfferedShipID()).getInternedBy() != Player.NONE) {
                        // FIXME
                        // sinkShip(a.getOfferedShipID(),
                        // DestroyShipMode.SINK);
                    }
                    // --
                }
            }

            currentPlayer = Player.NONE;

            if (getProcessedPlayersNumber(1) == Commons.PLAYERS_MAX) {
                stage = Stage.PRE_CONFLICT_OPERATIONS;
                MainBoard.addComponentsToPane();
                endPlayerTurn();
                return;
            }

            endPlayerTurn();
            return;
        }
    }


    // OK
    public void endPlayerTurn() {
        MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
        MainBoard.getBoardPanel().resetClipBoardShipID();

        if (stage == Stage.PRE_CONFLICT_OPERATIONS) {
            preConflictOperations();
            stage = Stage.DEPLOYMENT;
        }

        // if (stage != Stage.BOARDING_ACTIONS)
        // currentShipBoarding = ships.get(0);

        if (stage == Stage.DEPLOYMENT) {
            deployment();
        }

        if (stage == Stage.DEFINE_WIND) {
            turnID++;
            defineWind();

            for (int i = 0; i < Commons.SHIPS_MAX; i++)
                getShip(i).setBoardingFirstTurn(BoardingFirstTurn.NO);

            stage = Stage.MOVE_WRECKS;
        }

        if (stage == Stage.MOVE_WRECKS) {
            moveWrecks();
            stage = Stage.STORM_AND_TOW;
        }

        if (stage == Stage.STORM_AND_TOW) {
            stormAndTow();
            stage = Stage.PLAYERS_MOVES;
        }

        if (stage == Stage.PLAYERS_MOVES) {
            playersMoves();
        }

        // par. 6.6
        if (stage == Stage.BOARDING_MOVEMENTS || stage == Stage.BOARDING_ACTIONS || stage == Stage.BOARDING_SABOTAGE) {
            boardingPhase();
        }
        // --

        if (stage == Stage.INTERNED_SHIPS) {
            internedShips();
        }

        if (stage == Stage.BETWEEN_TURNS) {
            betweenTurns();
        }
    }


    public int getConflictID() {
        return conflictID;
    }


    public int getTurnID() {
        return turnID;
    }


    public Stage getStage() {
        return stage;
    }


    public int getWindSpeed() {
        return windSpeed;
    }


    public RotateDirection getWindDirection() {
        return windDirection;
    }


    public PlayerClass getPlayer(Player player) {
        for (PlayerClass p : players)
            if (p.getIdentity() == player)
                return p;
        throw new IllegalArgumentException();
    }


    public Ship getShip(int id) {
        for (Ship s : ships)
            if (s.getID() == id)
                return s;

        throw new NoSuchElementException();
    }
    
    public Collection<Ship> getShips() {
    	return ships;
    }


    public Ship getShipOnHex(Coordinate coord) {
        for (Ship s : ships)
            if (s.getPosition().equals(coord))
                return s;
        return null;
    }


    public int getPlayerFleetSize(Player player) {
        int size = 0;
        for (Ship s : ships)
            if (s.getOwner() == player)
                size++;
        return size;
    }


    // OK
    public void endConflict() {
        Map<Player, Integer> silver = new HashMap<Player, Integer>();
        for (Player p : Player.getValues())
            silver.put(p, 0);

        // par. 3.x ("wyplaty" za zdobycze w grze)
        for (Ship s : ships) {
            if (s.getOwner() != Player.NONE) {
                silver.put(s.getOwner(), silver.get(s.getOwner()) + s.getLoad(CargoType.SILVER));
                s.unloadAll(CargoType.SILVER);
            }
        }

        int spanishSilver = silver.get(Player.PASADENA) + silver.get(Player.SIDONIA);

        // 200d / 100t
        getPlayer(Player.PASADENA).addGold((spanishSilver / 100) * 200);
        // 1d / 1t
        getPlayer(Player.ELMETH).addGold(silver.get(Player.ELMETH) / 2);
        // 1/2 srebrana własność
        getPlayer(Player.ELMETH).addSilver(silver.get(Player.ELMETH) / 2);
        // 50d / 100t
        getPlayer(Player.SIDONIA).addGold((spanishSilver / 100) * 50);
        // 1/2 srebrana własność
        getPlayer(Player.PLEENSY).addSilver(silver.get(Player.PLEENSY) / 2);

        getPlayer(Player.DELACROIX).addSilver(silver.get(Player.DELACROIX));
        getPlayer(Player.DISCASTER).addSilver(silver.get(Player.DISCASTER));
        getPlayer(Player.HAMPSHIRE).addSilver(silver.get(Player.HAMPSHIRE));
        getPlayer(Player.LEPPO).addSilver(silver.get(Player.LEPPO));

        getPlayer(Player.ELMETH).addGold(getPlayer(Player.ELMETH).getDestroyedShipsNumber() * 150);

        produceShips();

        // par. 15.8 (wraki na planszy moga byc kupione jako dowolny nowy okret)
        for (Ship s : ships) {
            if (s.getID() != null && s.getOwner() == Player.NONE && s.isOnGameBoard()) {
                // mozliwosc zakupu jako dowolny nowy okret
                addShipToBank(ShipClass.NONE);
                ships.remove(s);
                continue;
            }

            // szczescie
            s.modifyHappiness(+1); // par. 18.1
            if (s.isHappinessFlagSet(Happiness.CAPTURE))
                s.modifyHappiness(+2);
            else if (s.isHappinessFlagSet(Happiness.SUNK) || s.isHappinessFlagSet(Happiness.BOARDING))
                s.modifyHappiness(+1);
            // TODO: premia trzech punktow

            /*
             * oproznienie statku: - dziala z ladowni przeniesione do magazynu -
             * wlasni marynarze i dowodca przeniesieni na lad - marynarze i
             * dowodcy sojusznika zwroceni - marynarze i dowodcy wroga
             * internowani
             */

            Player shipOwner = s.getOwner();
            s.prepareForNewTurn();

            getPlayer(shipOwner).addCannons(Gun.LIGHT, s.getLoad(CargoType.CANNONS_LIGHT));
            s.unloadAll(CargoType.CANNONS_LIGHT);
            getPlayer(shipOwner).addCannons(Gun.MEDIUM, s.getLoad(CargoType.CANNONS_MEDIUM));
            s.unloadAll(CargoType.CANNONS_MEDIUM);

            for (MarinesCompartment c : MarinesCompartment.getShipCompartments()) {
                for (Player p : Player.getValues()) {
                    if (getPlayer(shipOwner).isAlly(p)) {
                        getPlayer(p).addMarines(s.getMarinesNumber(p, c, Commons.BOTH));
                        if (s.getCommanderState(p, c) != CommanderState.NOT_THERE)
                            getPlayer(p).setCommanderInternedBy(Player.NONE);
                    } else {
                        getPlayer(shipOwner).addMarinesInterned(p, s.getMarinesNumber(p, c, Commons.BOTH));
                        if (s.getCommanderState(p, c) != CommanderState.NOT_THERE) {
                            getPlayer(shipOwner).addCommandersInterned(p);
                            getPlayer(p).setCommanderInternedBy(s.getOwner());
                        }
                    }

                    s.moveMarines(p, c, MarinesCompartment.SHIP_X, s.getMarinesNumber(p, c, Commons.BOTH));
                    s.setCommander(p, c, CommanderState.NOT_THERE);
                }
            }
        }

        // par. 5.1.3 (dopisanie odsetek do pozyczek)
        for (PlayerClass p : players) {
            p.setLoan((int) (p.getLoan() * 1.3));
        }
        // --

        // Produkowanie dział przez odlewnie i statków przez stocznie
        for (Player p : Player.getValues()) {
            getPlayer(p).addCannons(Gun.LIGHT, 8 * getPlayer(p).getFoundries());
            getPlayer(p).addCannons(Gun.MEDIUM, 4 * getPlayer(p).getFoundries());
            getPlayer(p).addCannons(Gun.HEAVY, 2 * getPlayer(p).getFoundries());

            getPlayer(p).addShipToYard(getPlayer(p).getShipyards());
        }
        // --
    }


    public boolean checkIfAlly(Player player, Player ally) {
        return getPlayer(player).isAlly(ally);
    }


    public void pass() {
        processed.put(currentPlayer, 1);
        endPlayerTurn();
    }


    public void writeToFile(DataOutputStream data_output) {
        Object[] set;
        try {
            data_output.writeInt(conflictID);
            data_output.writeInt(turnID);
            data_output.writeInt(stage.ordinal());

            data_output.writeInt(windSpeed);
            data_output.writeInt(windDirection.ordinal());

            board.writeData(data_output);

            for (PlayerClass p : players)
                p.writeToFile(data_output);

            for (int i = 0; i < Commons.SHIPS_MAX; i++)
                getShip(i).writeToFile(data_output);

            data_output.writeInt(currentPlayer.ordinal());

            for (ShipClass c : ShipClass.values())
                data_output.writeInt(bank.get(c));

            data_output.writeInt(auctions.size());
            Object[] aset = new Object[auctions.size()];
            aset = auctions.toArray();
            for (int i = 0; i < auctions.size(); i++)
                ((Auction) (aset[i])).writeToFile(data_output);

            data_output.writeInt(auctionsCounter);

            // FIXME
            // data_output.writeInt(currentShipBoardingID);

            for (Player p : Player.getValues())
                data_output.writeInt(processed.get(p));

            data_output.writeInt(agressors.size());
            set = new Object[agressors.size()];
            set = agressors.toArray();
            for (int i = 0; i < agressors.size(); i++)
                data_output.writeInt((Integer) set[i]);

            data_output.writeInt(defenders.size());
            set = new Object[defenders.size()];
            set = defenders.toArray();
            for (int i = 0; i < defenders.size(); i++)
                data_output.writeInt((Integer) set[i]);

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    public void readFromFile(DataInputStream data_input) {
        int counter = 0;

        try {
            conflictID = data_input.readInt();
            turnID = data_input.readInt();
            stage = Stage.valueOf(data_input.readInt());
            windSpeed = data_input.readInt();
            windDirection = RotateDirection.valueOf(data_input.readInt());
            board = new Board(data_input.readInt(), data_input.readInt());
            board.readData(data_input);

            for (Player p : Player.getValues())
                getPlayer(p).readFromFile(data_input);

            // FIXME
            // for (int i = 0; i < Commons.SHIPS_MAX; i++)
            // getShip(i).readFromFile(data_input);

            currentPlayer = Player.valueOf(data_input.readInt());

            for (ShipClass c : ShipClass.values())
                bank.put(c, data_input.readInt());

            auctionsCounter = data_input.readInt();
            auctions.clear();
            for (int i = 0; i < auctionsCounter; i++)
                auctions.add(Auction.readFromFile(data_input));

            // FIXME
            // currentShipBoardingID = data_input.readInt();

            for (Player p : Player.getValues())
                processed.put(p, data_input.readInt());

            agressors.clear();
            counter = data_input.readInt();
            for (int i = 0; i < counter; i++)
                agressors.add(Player.valueOf(data_input.readInt()));

            defenders.clear();
            counter = data_input.readInt();
            for (int i = 0; i < counter; i++)
                defenders.add(Player.valueOf(data_input.readInt()));

        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    public void submitAuctionOffer(int auctionID, Player player, int offer) {
        for (Auction a : auctions) {
            if (a.getAuctionID() == auctionID)
                a.setOffer(player, offer);
        }
    }


    public int getAuctionsCount() {
        return auctions.size();
    }


    public Auction getAuction(int auctionID) {
        for (Auction a : auctions)
            if (a.getAuctionID() == auctionID)
                return a;
        throw new IllegalArgumentException();
    }


    public int getShipsInBank(ShipClass shipClass) {
        return bank.get(shipClass);
    }


    public void addShipToBank(ShipClass sc) {
        bank.put(sc, bank.get(sc) + 1);
    }


    public void produceShips() {
        // par. 20.4.1 (produkcja statkow dla banku)
        for (ShipClass c : ShipClass.values()) {
            if (c != ShipClass.NONE && bank.get(c) == 0)
                bank.put(c, 1);
        }
    }


    public void transfer(PlayerClass player, Ship from, Ship to, TransferCargo cargo, MarinesCompartment marinesFrom,
            MarinesCompartment marinesTo, GunCompartment cannonsFrom, GunCompartment cannonsTo, Gun gunType, int number) {
        if (cargo == TransferCargo.MARINES) {
            if (from == null)
                player.removeMarines(number);
            else
                from.moveMarines(player.getIdentity(), marinesFrom, MarinesCompartment.SHIP_X, number);

            if (to == null)
                player.addMarines(number);
            else
                to.moveMarines(player.getIdentity(), MarinesCompartment.SHIP_X, marinesTo, number);
        } else {
            if (from == null)
                player.removeCannons(gunType, number);
            else
                from.modifyCannonsNumber(cannonsFrom, gunType, -number);

            if (to == null)
                player.addCannons(gunType, number);
            else
                to.modifyCannonsNumber(cannonsTo, gunType, number);
        }
    }


    public void acceptContractOffer(PlayerClass _clientA, Ship[] _shipsA, int _cannonsLightA, int _cannonsMediumA,
            int _cannonsHeavyA, int _marinesA, int _goldA, int _silverA, boolean _freeCommanderA,

            PlayerClass _clientB, Ship[] _shipsB, int _cannonsLightB, int _cannonsMediumB, int _cannonsHeavyB,
            int _marinesB, int _goldB, int _silverB, boolean _freeCommanderB) {

        int maxThisMarines = Integer.MAX_VALUE;
        // TODO : najpierw wymiana marynarzy-jencow, dopowro potem oddawanie

        for (Ship ship : _shipsB) {
            ship.setOwner(_clientA.getIdentity());
            ship.setInternedBy(Player.NONE);
        }

        _clientA.addCannons(Gun.LIGHT, _cannonsLightB);
        _clientA.addCannons(Gun.MEDIUM, _cannonsLightB);
        _clientA.addCannons(Gun.HEAVY, _cannonsLightB);

        _clientA.removeCannons(Gun.LIGHT, _cannonsLightA);
        _clientA.removeCannons(Gun.MEDIUM, _cannonsLightA);
        _clientA.removeCannons(Gun.HEAVY, _cannonsLightA);

        maxThisMarines = Math.min(_clientA.getMarinesInterned(_clientB.getIdentity()), _marinesA);
        _clientA.removeMarinesInterned(_clientB.getIdentity(), maxThisMarines);
        if (maxThisMarines < _marinesA)
            _clientA.removeMarines(_marinesA - maxThisMarines);
        _clientA.addMarines(_marinesB);

        _clientA.addGold(_goldB);
        _clientA.removeGold(_goldA);
        _clientA.addSilver(_silverB);
        _clientA.removeSilver(_silverA);

        if (_freeCommanderB)
            _clientA.setCommanderInternedBy(Player.NONE);

        for (Ship ship : _shipsA) {
            ship.setOwner(_clientB.getIdentity());
            ship.setInternedBy(Player.NONE);
        }

        _clientB.addCannons(Gun.LIGHT, _cannonsLightA);
        _clientB.addCannons(Gun.MEDIUM, _cannonsLightA);
        _clientB.addCannons(Gun.HEAVY, _cannonsLightA);

        _clientB.removeCannons(Gun.LIGHT, _cannonsLightB);
        _clientB.removeCannons(Gun.MEDIUM, _cannonsLightB);
        _clientB.removeCannons(Gun.HEAVY, _cannonsLightB);

        maxThisMarines = Math.min(_clientB.getMarinesInterned(_clientA.getIdentity()), _marinesB);
        _clientB.removeMarinesInterned(_clientA.getIdentity(), maxThisMarines);
        if (maxThisMarines < _marinesA)
            _clientB.removeMarines(_marinesB - maxThisMarines);
        _clientB.addMarines(_marinesA);

        _clientB.addGold(_goldA);
        _clientB.removeGold(_goldB);
        _clientB.addSilver(_silverA);
        _clientB.removeSilver(_silverB);

        if (_freeCommanderA)
            _clientB.setCommanderInternedBy(Player.NONE);
    }


    public void repairShip(Player player, int shipID, RepairType repairType, int points) {
        switch (repairType) {
        case DURABILITY:
            getShip(shipID).repairHull(points);
            break;
        case MAST:
            getShip(shipID).repairMast(points);
            break;
        case HELM:
            getShip(shipID).repairHelm(points);
            break;
        }

        getPlayer(player).removeGold(points * repairType.getCost());
    }


    public int getFreeID() {
        List<Integer> ids = new LinkedList<Integer>();

        // XXX: jakoś optymalniej?
        for (int i = 1; i < Commons.SHIPS_MAX; i++)
            ids.add(i);

        for (Ship s : ships) {
            ids.remove(s.getID());
        }

        return ids.get(0);
    }


    public void buildShip(Player player, ShipClass shipClass) {
        // TODO: ograniczenie na wielkość floty, np.:
        // ships.size() < SHIPS_MAX

        launchShip(player, getFreeID(), shipClass);
        getPlayer(player).removeShipFromYard();
        getPlayer(player).removeGold(shipClass.getPrice() / 2); // par.
                                                                // 20.3.1
    }


    private void sinkShip(Ship ship, DestroyShipMode mode) {
        Ships.sinkShip(ship, mode);
        ships.remove(ship);
    }


    public void sellShip(Player player, Ship ship) {
        sinkShip(ship, DestroyShipMode.SINK);
        getPlayer(player).addGold(ship.calculateSellPrice());
    }


    public void launchShip(Player player, int shipID, ShipClass shipClass) {
        Ship s = new Ship(player, shipClass, shipID);
        ships.add(s);
        getPlayer(player).addShipToFleet(s);
    }


    public void submitContractOffer() {
        // TODO: monit?
    }
}