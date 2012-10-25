package sfmainframe.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import sfmainframe.Commons;
import sfmainframe.Coordinate;
import sfmainframe.Dice;
import sfmainframe.MainBoard;
import sfmainframe.Player;
import sfmainframe.PlayerClass;
import sfmainframe.board.Board;
import sfmainframe.board.ObstacleReport;
import sfmainframe.board.RotateDirection;
import sfmainframe.board.Terrain;
import sfmainframe.gui.DataExtractors;
import sfmainframe.gui.MsgMode;
import sfmainframe.gui.Tabs;
import sfmainframe.ship.AimPart;
import sfmainframe.ship.BoardingFirstTurn;
import sfmainframe.ship.CoupleReason;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;
import sfmainframe.ship.Parameter;
import sfmainframe.ship.ShallowAttempt;
import sfmainframe.ship.Ship;
import sfmainframe.ship.cargo.CargoDestination;
import sfmainframe.ship.cargo.CargoType;
import sfmainframe.ship.cargo.HandlingPartner;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.CompartmentAllies;
import sfmainframe.ship.marines.MarinesCompartment;

public class Ships {

    public static final int COMMANDER_TO_BE_KILLED = 0xAAAA;
    private final double WIND_FACTOR[] = { 3.5, 5.0, 7.0 }; // par. 8.3

    private Game game;
    private Ship[] ships;


    public Ships() {
        ships = new Ship[Settings.SHIPS_MAX];
        for (int i = 0; i < Settings.SHIPS_MAX; i++)
            ships[i] = new Ship();

        game = MainBoard.game;
    }


    public void initTurn() {
        for (int i = 0; i < Settings.SHIPS_MAX; i++)
            if (ships[i].getOwner() == Player.HAMPSHIRE)
                ships[i].setHappiness(+1);
    }


    public void coupleShips(int shipOneID, int shipTwoID, CoupleReason reason) {
        MainBoard.addMessage("Ship #" + shipOneID + " and ship #" + shipTwoID + " coupled (" + reason.toString()
                + ").\n");

        ships[shipOneID].couple(shipTwoID, reason);
        ships[shipTwoID].couple(shipOneID, reason);
    }


    public void throwTow(int shipID) {
        Integer irv = ships[shipID].getTowedBy();
        if (irv != null) {
            MainBoard.addMessage("Ship #" + shipID + ": threw tow\n");
            MainBoard.addMessage("Ship #" + irv + ": threw tow\n");
            ships[irv].clearTow();
            ships[shipID].clearTowedBy();
            return;
        }

        irv = ships[shipID].getTowOther();
        if (irv != null) {
            MainBoard.addMessage("Ship #" + shipID + ": threw tow\n");
            MainBoard.addMessage("Ship #" + irv + ": threw tow\n");
            ships[irv].clearTowedBy();
            ships[shipID].clearTow();
            return;
        }

    }


    /**
     * 
     * @param shipID
     * @param mode
     * @return players who end game
     */
    public void sinkShip(int shipID, DestroyShipMode mode) {

        MainBoard.addMessage("Ship #" + shipID + " sank!\n");

        ships[shipID].setPosition(Coordinate.dummy);
        ships[shipID].setParameter(Parameter.IS_SUNK, Commons.ON);

        if (mode == DestroyShipMode.BLOWUP)
            blowUpShip(shipID);

        if (ships[shipID].getShipsCoupled().size() > 0) {
            Integer[] coupled = (Integer[]) (ships[shipID].getShipsCoupled().toArray());
            for (int i = 0; i < coupled.length; i++) {
                ships[coupled[i]].uncouple(shipID);
            }
        }

        throwTow(shipID);

        // par. 5.3.5
        for (MarinesCompartment location : MarinesCompartment.values()) {
            if (location == MarinesCompartment.SHIP_X || location == MarinesCompartment.NONE)
                continue;

            for (Player player : Player.getValues()) {
                if (ships[shipID].getCommanderState(player, location) != CommanderState.NOT_THERE)
                    game.endPlayerGame(player);
            }
        }
        // --

        if (ships[shipID].getInternedBy() != Player.NONE)
            game.getPlayer(ships[shipID].getInternedBy()).removeInternedShip(ships[shipID].getOwner(), shipID);

        game.getPlayer(ships[shipID].getOwner()).removeShipFromFleet(shipID);
        if (game.getPlayer(ships[shipID].getOwner()).getFleet().size() == 0)
            game.endPlayerGame(ships[shipID].getOwner());

        ships[shipID] = new Ship();

        MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
    }


    public void blowUpShip(int shipID) {
        Vector<Integer> coupledShips = ships[shipID].getShipsCoupled();

        MainBoard.addMessage("Ship #" + shipID + ": blown up\n");

        for (int i = 0; i < coupledShips.size(); i++) {
            int coupledShipID = coupledShips.get(i);
            Player shipOwner = ships[coupledShipID].getOwner();

            // par. 12.2.3.5.2
            int dA = ships[coupledShipID].getPosition().getA() - ships[shipID].getPosition().getA();
            int dB = ships[coupledShipID].getPosition().getB() - ships[shipID].getPosition().getB();
            boolean coupledWithSides = false;

            int angleModifier = Commons.NIL;
            RotateDirection rotations[] = { RotateDirection.NE, RotateDirection.SE, RotateDirection.SW,
                    RotateDirection.NW };

            if (dA == 0 && dB == 1 && ships[shipID].getRotation() != RotateDirection.N
                    && ships[shipID].getRotation() != RotateDirection.S)
                angleModifier = 0;

            if (dA == 1 && dB == 1 && ships[shipID].getRotation() != RotateDirection.NE
                    && ships[shipID].getRotation() != RotateDirection.SW)
                angleModifier = 1;

            if (dA == 1 && dB == 0 && ships[shipID].getRotation() != RotateDirection.SE
                    && ships[shipID].getRotation() != RotateDirection.NW)
                angleModifier = 2;

            if (dA == 0 && dB == -1 && ships[shipID].getRotation() != RotateDirection.N
                    && ships[shipID].getRotation() != RotateDirection.S)
                angleModifier = 3;

            if (dA == -1 && dB == -1 && ships[shipID].getRotation() != RotateDirection.NE
                    && ships[shipID].getRotation() != RotateDirection.SW)
                angleModifier = 4;

            if (dA == -1 && dB == 0 && ships[shipID].getRotation() != RotateDirection.NW
                    && ships[shipID].getRotation() != RotateDirection.SE)
                angleModifier = 5;

            if (angleModifier != Commons.NIL) {
                for (int dR = 0; dR < 6; dR++) {
                    if (rotations[dR] == RotateDirection.rotate(ships[coupledShipID].getRotation(), angleModifier)) {
                        coupledWithSides = true;
                        break;
                    }
                }
            }

            if (coupledWithSides) {
                if (!damageHull(shipID, rollDice(coupledShipID, shipOwner)))
                    destroyMast(coupledShipID, 1);
            }
            // --

            // par. 12.2.3.5.3
            for (Player plr : Player.getValues()) {
                killMarines(coupledShipID, plr, MarinesCompartment.DECK, rollDice(coupledShipID, shipOwner),
                        KillingMode.WITH_COMMANDER);
            }
            calculateShipOwner(coupledShipID);
            // --
        }
    }


    public int rollDice(int shipID, Player player) {
        int roll = Dice.roll();

        if (player != ships[shipID].getOwner())
            return roll;

        if (ships[shipID].getHappiness() == 0)
            return roll;

        if (!MainBoard.rollAgainDialog(roll, player))
            return roll;

        ships[shipID].setHappiness(ships[shipID].getHappiness() - 1);
        return Dice.roll();

        // TODO: kryterium gracza (timer, aby nie czekać wciąż)

        /*
         * timer = new Timer(); timer.schedule(new checkRollAnswer(), 0);
         * 
         * while (MainBoard.getHappinessAction() == HappinessAction.NONE) { try
         * { this.wait(100); // Thread.sleep(10); } catch (InterruptedException
         * ex) {} } /
         */

        /*
         * MainBoard.setRemainingTimeLabelText("Remaining time: n/a");
         * 
         * MainBoard.addTestMsg("Action: "+
         * MainBoard.getHappinessAction().toString()+"\n");
         * 
         * if (MainBoard.getHappinessAction() == HappinessAction.ACCEPT) {
         * MainBoard.setHappinessAction(HappinessAction.NONE); return roll; } if
         * (MainBoard.getHappinessAction() == HappinessAction.AGAIN) {
         * MainBoard.setHappinessAction(HappinessAction.NONE);
         * ships[shipID].SetHappiness(ships[shipID].ShowHappiness()-1); return
         * RollDice(); }
         */
    }


    public boolean destroyMast(int shipID, int points) {
        boolean damage = false;
        MainBoard.addMessage(String.format("Ship #%d: speed fell by %d knots\n", shipID,
                Math.min(ships[shipID].getMast(), points)));
        if (ships[shipID].getMast() > 0)
            damage = true;
        ships[shipID].destroyMast(points);

        Integer towed = ships[shipID].getTowOther();
        if (towed != null && !checkIfStillTowable(shipID, towed))
            throwTow(shipID);

        return damage;
    }


    // TODO: String.format()
    public boolean destroyHelm(int shipID, int points) {
        boolean damage = false;
        MainBoard.addMessage("Ship #" + shipID + ": manoeuvrability fell by "
                + Math.min(ships[shipID].getHelm(Commons.BOTH), points) + " points\n");
        if (ships[shipID].getHelm(Commons.BOTH) > 0)
            damage = true;
        ships[shipID].destroyHelm(points);

        Integer towed = ships[shipID].getTowOther();
        if (towed != null && !checkIfStillTowable(shipID, towed))
            throwTow(shipID);

        return damage;
    }


    public boolean destroyHeaviestCannon(int shipID, GunCompartment location) {
        if (!destroyCannonIS(shipID, location, Gun.HEAVY, MsgMode.ON)) {
            if (!destroyCannonIS(shipID, location, Gun.MEDIUM, MsgMode.ON)) {
                if (!destroyCannonIS(shipID, location, Gun.LIGHT, MsgMode.ON)) {
                    MainBoard.addMessage("No loss taken.\n");
                    return false;
                }
            }
        }
        return true;
    }


    // TODO: String.format()
    public boolean damageHull(int shipID, int points) {
        boolean isSunk = ships[shipID].destroyHull(points);

        if (isSunk) {
            sinkShip(shipID, DestroyShipMode.SINK);
            return true;
        }

        MainBoard.addMessage("Ship #" + shipID + ": hull damage (" + points + " pts)\n");

        Integer towed = ships[shipID].getTowOther();
        if (towed != null && !checkIfStillTowable(shipID, towed))
            throwTow(shipID);

        return false;
    }


    public boolean checkIfStillTowable(int tugID, int towedID) {
        /*
         * sprawdzanie spelnienia warunkow holowania w sytuacji, gdy statek jest
         * juz sczepiony holem
         */

        if (ships[tugID].getDurability() * 2 < ships[towedID].getDurability())
            return false; // par. 16.1
        if (ships[tugID].getHelm(Commons.BOTH) == 0 || ships[tugID].getMast() == 0)
            return false; // par. 16.2
        if (!checkIfPlayerControlsLocation(tugID, game.getCurrentPlayer(), MarinesCompartment.DECK, true)
                || !checkIfPlayerControlsLocation(towedID, game.getCurrentPlayer(), MarinesCompartment.DECK, true))
            return false; // par. 12.10, 16.5

        return true;
    }


    public boolean checkIfTowable(int tugID, int towedID) {
        Coordinate tugPos = ships[tugID].getPosition();
        Coordinate towedPos = ships[towedID].getPosition();

        if (!checkIfStillTowable(tugID, towedID))
            return false;
        if (ships[tugID].getTowedBy() != null || ships[tugID].getTowOther() != null
                || ships[towedID].getTowedBy() != null || ships[towedID].getTowOther() != null)
            return false; // par. 16.3
        if (Math.abs(tugPos.getA() - towedPos.getA()) > 1 || Math.abs(tugPos.getB() - towedPos.getB()) > 1
                || getAlliedMarinesNumber(tugID, game.getCurrentPlayer(), MarinesCompartment.DECK) < 2
                || getAlliedMarinesNumber(towedID, game.getCurrentPlayer(), MarinesCompartment.DECK) < 2)
            return false; // par. 16.4
        if (game.getWindSpeed() > 8)
            return false; // par. 16.10
        if (ships[tugID].isActionsOver() == Commons.ON)
            return false; // par. 16.6

        return true;
    }


    public boolean checkIfPlayerControlsLocation(int shipID, Player player, MarinesCompartment location,
            boolean withAllies) {
        /*
         * Funkcja sprawdza, czy gracz kontroluje przedział marynarzy. Gracz
         * kontroluje przedział, gdy posiada w nim co najmniej jednego marynarza
         * oraz nie znajdują się w nim marynarze wrogów.
         * 
         * withAllies - zaznaczona sprawia, że przy obliczaniu ilości własnych
         * marynarzy w przedziale brani są też pod uwagę marynarze sojuszniczy
         */

        for (Player plr : Player.getValues()) {
            if (!game.getPlayer(player).isAlly(plr) && ships[shipID].getMarinesNumber(plr, location, Commons.BOTH) > 0)
                return false;
        }

        if (withAllies && getAlliedMarinesNumber(shipID, player, location) == 0)
            return false;
        else if (!withAllies && ships[shipID].getMarinesNumber(player, location, Commons.BOTH) == 0)
            return false;
        else
            return true;
    }


    public int getAlliedMarinesNumber(int shipID, Player player, MarinesCompartment location) {
        /*
         * Funkcja zwraca łączną ilość sojuszniczych marynarzy w przedziale.
         */

        int number = 0;
        for (Player plr : Player.getValues()) {
            if (game.getPlayer(player).isAlly(plr))
                number += ships[shipID].getMarinesNumber(plr, location, Commons.BOTH);
        }

        return number;
    }


    public boolean destroyCannonIS(int shipID, GunCompartment location, Gun type, MsgMode mode) {
        try {
            ships[shipID].destroyCannon(location, type, Commons.BOTH);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (mode == MsgMode.ON)
            MainBoard.addMessage("Ship #" + shipID + ": " + type.toString() + " cannon from " + location.toString()
                    + " compartment lost.\n");

        return true;
    }


    public Ship getShip(int shipID) {
        return ships[shipID];
    }


    /**
     * 
     * @param shipID
     * @return list of the following elements: winner, players who end game
     */
    public List<Object> calculateShipOwner(int shipID) {
        List<Object> result = new ArrayList<Object>();
        Integer winnerId = new Integer(0);
        Vector<Player> playersWhoEndGame = new Vector<Player>();
        result.add(winnerId);
        result.add(playersWhoEndGame);

        if (ships[shipID].getOwner() != Player.NONE
                && ships[shipID].getPlayerMarinesOnShip(ships[shipID].getOwner(), false) > 0) {
            winnerId = Ship.PREVIOUS_OWNER;
            return result;
        }

        // brak dotychczasowego wlasciciela, walka o wladze
        int[] marines = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        Player winner = Player.NONE;
        int playersOnBoard = 0;

        // sumowanie marynarzy danego gracza
        for (Player plr : Player.values()) {
            if (plr == Player.NONE)
                continue;
            marines[plr.ordinal()] = ships[shipID].getPlayerMarinesOnShip(plr, true);
            if (marines[plr.ordinal()] > 0)
                playersOnBoard++;
        }

        if (playersOnBoard == 0) {
            if (ships[shipID].getOwner() != Player.NONE) {
                game.getPlayer(ships[shipID].getOwner()).removeShipFromFleet(shipID);
                if (game.getPlayer(ships[shipID].getOwner()).getFleet().size() == 0)
                    playersWhoEndGame.add(ships[shipID].getOwner());
            }

            ships[shipID].setOwner(Player.NONE);
            ships[shipID].setParameter(Parameter.IS_WRECK, Commons.ON); // par.
                                                                        // 15.1
            ships[shipID].setHappiness(0); // par. 18.8
            MainBoard.addMessage("Ship #" + shipID + ": is now a wreck\n");

            winnerId = Player.NONE.ordinal();
            return result;
        }

        for (Player plr : Player.values()) {
            if (ships[shipID].getPlayerMarinesOnShip(plr, false) > ships[shipID].getPlayerMarinesOnShip(winner, false)
                    || ships[shipID].getPlayerMarinesOnShip(plr, false) == ships[shipID].getPlayerMarinesOnShip(winner,
                            false) && Dice.roll() > 3)
                winner = plr; // par. 5.5.1.2
        }

        if (playersOnBoard == 1 && ships[shipID].getPlayerMarinesOnShip(ships[shipID].getOwner(), true) == 1
                || winner == ships[shipID].getOwner()) {
            winnerId = Ship.PREVIOUS_OWNER;
            return result;
        }

        // par. 5.5.1
        int first = 0;
        int i = 0;
        for (i = 0; i < Commons.PLAYERS_MAX; i++) {
            if (marines[i] > 0) {
                first = i;
                break;
            }
        }
        for (i = first + 1; i < Commons.PLAYERS_MAX; i++) {
            if (!game.getPlayer(Player.valueOf(first)).isAlly(Player.valueOf(i)))
                break;
        }

        if (i == Commons.PLAYERS_MAX) {
            // Na okrecie pozostala tylko jedna strona.

            // par. 5.3.6, 5.3.10 (uwięzienie / uwolnienie dowódców)

            for (MarinesCompartment loc : MarinesCompartment.values()) {
                if (loc == MarinesCompartment.NONE || loc == MarinesCompartment.SHIP_X)
                    continue;
                for (Player plr : Player.values()) {
                    if (plr == Player.NONE)
                        continue;

                    if (game.getPlayer(winner).isAlly(plr)) {
                        if (ships[shipID].getCommanderState(plr, loc) == CommanderState.IMPRISONED)
                            ships[shipID].setCommander(plr, loc, CommanderState.READY);
                    } else {
                        if (ships[shipID].getCommanderState(plr, loc) != CommanderState.NOT_THERE) {
                            ships[shipID].setCommander(plr, loc, CommanderState.NOT_THERE);
                            ships[shipID].setCommander(plr, MarinesCompartment.INMOVE, CommanderState.IMPRISONED);
                        }
                    }
                }
            }
            // --
        }
        // --

        // -- par. 18.8
        if (!game.getPlayer(ships[shipID].getOwner()).isAlly(winner))
            ships[shipID].setHappiness(0);
        // --

        game.getPlayer(ships[shipID].getOwner()).removeShipFromFleet(shipID);
        if (game.getPlayer(ships[shipID].getOwner()).getFleet().size() == 0)
            playersWhoEndGame.add(ships[shipID].getOwner());

        ships[shipID].setOwner(winner);
        ships[shipID].setParameter(Parameter.IS_WRECK, Commons.OFF);

        MainBoard.addMessage("Ship #" + shipID + ": new owner is " + winner.toString() + "\n");

        winnerId = winner.ordinal();
        return result;
    }


    public int killMarines(int shipID, Player player, MarinesCompartment location, int number, KillingMode mode) {
        /*
         * player = NONE: straty dla kazdego gracza, proporcjonalnie do udzialu
         * jego marynarzy w przedziale, zaokraglane w gore
         */
        int killed = 0;
        boolean commander = false;

        if (number == 0)
            return 0;

        if (player != Player.NONE) {
            Object[] ret = ships[shipID].killMarines(player, location, number, mode);
            killed = (Integer) ret[0];
            if (ret[1] == KillingMode.WITH_COMMANDER) {
                if (mode == KillingMode.WITH_COMMANDER)
                    game.endPlayerGame(player);
                commander = true;
            }

            MainBoard.addMessage("Ship #" + shipID + ": " + killed + " marines ");
            if (commander && mode == KillingMode.WITH_COMMANDER)
                MainBoard.addMessage("and commander ");
            MainBoard.addMessage("lost (" + player.toString() + ")\n");

            calculateShipOwner(shipID);
        } else {
            Vector<Player> plrs = new Vector<Player>();
            int totalNumber = 0;

            for (Player plr : Player.values()) {
                if (plr == Player.NONE)
                    continue;
                if (ships[shipID].getMarinesNumber(plr, location, Commons.BOTH) > 0
                        || ships[shipID].getCommanderState(plr, location) != CommanderState.NOT_THERE) {
                    plrs.add(plr);

                    totalNumber += ships[shipID].getMarinesNumber(plr, location, Commons.BOTH);
                    if (ships[shipID].getCommanderState(plr, location) != CommanderState.NOT_THERE)
                        totalNumber += 1;
                }
            }

            for (int i = 0; i < plrs.size(); i++) {
                int thisNumber = ships[shipID].getMarinesNumber(plrs.get(i), location, Commons.BOTH);
                if (ships[shipID].getCommanderState(plrs.get(i), location) != CommanderState.NOT_THERE)
                    thisNumber += 1;

                Object[] ret = ships[shipID].killMarines(player, location, (int) (Math.ceil(thisNumber / totalNumber)),
                        mode);
                killed = (Integer) ret[0];
                if (ret[1] == KillingMode.WITH_COMMANDER) {
                    if (mode == KillingMode.WITH_COMMANDER)
                        game.getPlayer(Player.valueOf(i)).endGame();
                    commander = true;
                }

                MainBoard.addMessage("Ship #" + shipID + ": " + killed + " marines ");
                if (commander && mode == KillingMode.WITH_COMMANDER)
                    MainBoard.addMessage("and commander ");
                MainBoard.addMessage("lost (" + plrs.get(i).toString() + ")\n");
            }

            calculateShipOwner(shipID);
        }

        if (commander)
            return COMMANDER_TO_BE_KILLED;
        else
            return killed;
    }


    public Coordinate checkAngleToRotate(int shipId) {
        RotateDirection rotTowedBy = null;
        RotateDirection rotTowOther = null;

        if (ships[shipId].getTowedBy() != null)
            rotTowedBy = ships[ships[shipId].getTowedBy()].getRotation();
        if (ships[shipId].getTowOther() != null)
            rotTowedBy = ships[ships[shipId].getTowOther()].getRotation();

        return ships[shipId].checkAngleToRotate(rotTowedBy, rotTowOther);
    }


    private boolean runDown(int aggressorID, int victimID) {
        /*
         * #return: true, gdy okręt taranujący zatonął
         */
        MainBoard.addMessage("Ship #" + aggressorID + ": rammed ship #" + victimID + "\n");

        // par. 11.1
        if (!damageHull(victimID, ships[aggressorID].getDurability())) {
            destroyMast(victimID, 4);
            destroyHelm(victimID, 1);
            ships[victimID].addMovementCode(MovementType.END_MOVE); // par. 11.3
        }
        // --
        else
            game.getPlayer(ships[aggressorID].getOwner()).addDestroyedShip(); // par.
                                                                              // 3.2

        // par. 11.2
        if (!damageHull(aggressorID, ships[victimID].getShipClass().getDurabilityMax() / 2)) {
            if (rollDice(aggressorID, game.getCurrentPlayer()) == 6)
                destroyHelm(aggressorID, 1);

            ships[aggressorID].addMovementCode(MovementType.END_MOVE); // par.
                                                                       // 11.3
            return false;
        }
        // --
        else
            return true;
    }


    public void handle(int sourceID, int targetID, Player player, CargoDestination from, CargoDestination to,
            CargoType cargoType, GunCompartment sourceCompartment, GunCompartment targetCompartment, int quantity) {
        Gun gunType = Gun.NONE;

        // par. 14.5
        if (!ships[sourceID].isShipCoupled(targetID))
            coupleShips(sourceID, targetID, CoupleReason.HANDLING);
        // --

        if (cargoType == CargoType.SILVER) {
            ships[sourceID].unloadCargo(CargoType.SILVER, quantity);
            ships[targetID].loadCargo(CargoType.SILVER, quantity);
            ships[sourceID].moveMarines(player, MarinesCompartment.INMOVE, MarinesCompartment.INMOVE, quantity);
            return;
        }

        if (cargoType == CargoType.CANNONS_LIGHT)
            gunType = Gun.LIGHT;
        if (cargoType == CargoType.CANNONS_MEDIUM)
            gunType = Gun.MEDIUM;

        if (from == CargoDestination.CARGO)
            ships[sourceID].unloadCargo(cargoType, quantity);
        if (from == CargoDestination.BATTERIES) {
            for (int i = 0; i < quantity; i++)
                destroyCannonIS(sourceID, sourceCompartment, gunType, MsgMode.OFF);
        }

        if (cargoType == CargoType.CANNONS_LIGHT) {
            ships[sourceID].moveMarines(player, MarinesCompartment.INMOVE, MarinesCompartment.INMOVE, quantity * 10);
            if (to == CargoDestination.CARGO)
                ships[targetID].loadCargo(CargoType.CANNONS_LIGHT, quantity);
            else
                ships[targetID].modifyCannonsNumber(targetCompartment, Gun.LIGHT, quantity);
        }
        if (cargoType == CargoType.CANNONS_MEDIUM) {
            ships[sourceID].moveMarines(player, MarinesCompartment.INMOVE, MarinesCompartment.INMOVE, quantity * 20);
            if (to == CargoDestination.CARGO)
                ships[targetID].loadCargo(CargoType.CANNONS_MEDIUM, quantity);
            else
                ships[targetID].modifyCannonsNumber(targetCompartment, Gun.MEDIUM, quantity);
        }
    }


    public boolean checkIfSetExplosivesPossible(int shipID, Player player) {
        if (ships[shipID].isActionsOver() == Commons.ON)
            return false;

        if (ships[shipID].getParameter(Parameter.IS_EXPLOSIVE) == Commons.ON)
            return false;

        if (game.getCurrentPlayer() != ships[shipID].getOwner())
            return false;

        // par. 14.9 (okręt musi być pusty)
        if (getCompartmentAllies(shipID, player, MarinesCompartment.DECK) != CompartmentAllies.NONE
                || getCompartmentAllies(shipID, player, MarinesCompartment.INMOVE) != CompartmentAllies.NONE
                || getCompartmentAllies(shipID, player, MarinesCompartment.BATTERIES) != CompartmentAllies.NONE)
            return false;
        // --

        return true;
    }


    public void endHandling(int shipOneID, int shipTwoID) {
        ships[shipOneID].uncouple(shipTwoID);
        ships[shipTwoID].uncouple(shipOneID);

        // par. 14.6
        ships[shipOneID].addMovementCode(MovementType.END_MOVE);
        ships[shipTwoID].addMovementCode(MovementType.END_MOVE);
        // --
    }


    public void tow(int tugID, int towedID) {
        MainBoard.addMessage("Ship #" + tugID + ": now towing ship #" + towedID + "\n");

        ships[tugID].addMovementCode(MovementType.END_MOVE);
        ships[towedID].addMovementCode(MovementType.END_MOVE);

        ships[tugID].setTowOther(towedID);
        ships[towedID].setTowedBy(tugID);

        ships[tugID].setActionsOver(Commons.ON); // 16.6
        ships[towedID].setActionsOver(Commons.ON); // 16.6
    }


    public void surrenderMarines(int shipID, MarinesCompartment location, int number) {
        // par. 12.5
        killMarines(shipID, game.getCurrentPlayer(), location, number, KillingMode.WITHOUT_COMMANDER);
        calculateShipOwner(shipID);
        // --
    }


    public void moveMarinesShip(int sourceShipID, int targetShipID, Player player, int amount) {
        Boolean boardingFirstTurn = Boolean.FALSE;
        boolean shipAlreadyBoarded = false;

        // par. 12.1
        if (ships[targetShipID].getOwner() != Player.NONE
                && !game.getPlayer(game.getCurrentPlayer()).isAlly(ships[targetShipID].getOwner())) {
            boardingFirstTurn = Boolean.TRUE;

            if (!ships[sourceShipID].isShipCoupled(targetShipID))
                coupleShips(sourceShipID, targetShipID, CoupleReason.BOARDING);

            for (Player p : Player.getValues()) {
                if (!game.getPlayer(ships[targetShipID].getOwner()).isAlly(p)
                        && ships[targetShipID].getPlayerMarinesOnShip(player, false) > 0)
                    shipAlreadyBoarded = true;
            }

            if (shipAlreadyBoarded)
                boardingFirstTurn = Boolean.FALSE;
        }
        // --
        else {
            if (!ships[sourceShipID].isShipCoupled(targetShipID))
                coupleShips(sourceShipID, targetShipID, CoupleReason.HANDLING);
        }

        ships[sourceShipID].moveMarines(game.getCurrentPlayer(), MarinesCompartment.DECK, MarinesCompartment.SHIP_X,
                amount);
        ships[targetShipID].moveMarines(game.getCurrentPlayer(), MarinesCompartment.SHIP_X, MarinesCompartment.DECK,
                amount);
        MainBoard.addMessage("Ship #" + targetShipID + ": " + amount + " marines moved from ship #" + sourceShipID
                + "\n");

        calculateShipOwner(sourceShipID);
        calculateShipOwner(targetShipID);

        if (boardingFirstTurn == Boolean.TRUE) {
            MainBoard.addMessage("Ship #" + targetShipID + " boarded.\n");
            ships[targetShipID].setBoardingFirstTurn(BoardingFirstTurn.YES);
        }
    }


    public void moveCommanderShip(int sourceShipID, int targetShipID) {
        if (ships[targetShipID].getOwner() != Player.NONE
                && !game.getPlayer(game.getCurrentPlayer()).isAlly(ships[targetShipID].getOwner())) {

            if (!ships[sourceShipID].isShipCoupled(targetShipID))
                coupleShips(sourceShipID, targetShipID, CoupleReason.BOARDING);
        } else {
            if (!ships[sourceShipID].isShipCoupled(targetShipID))
                coupleShips(sourceShipID, targetShipID, CoupleReason.HANDLING);
        }

        ships[sourceShipID].moveCommander(game.getCurrentPlayer(), MarinesCompartment.DECK, MarinesCompartment.SHIP_X);
        ships[targetShipID].moveCommander(game.getCurrentPlayer(), MarinesCompartment.SHIP_X, MarinesCompartment.DECK);
        MainBoard.addMessage("Ship #" + targetShipID + ": commander moved from ship #" + sourceShipID + "\n");

        calculateShipOwner(sourceShipID);
        calculateShipOwner(targetShipID);
    }


    public void boardingEscape(int shipID) {
        MainBoard.addMessage("Ship #" + shipID + ": did boarding escape\n");

        Vector<Integer> shipsCoupled = ships[shipID].getShipsCoupled();
        for (int i = 0; i < shipsCoupled.size(); i++) {
            ships[shipID].uncouple(shipsCoupled.get(i));
            ships[shipsCoupled.get(i)].uncouple(shipID);
        }

        moveShip(shipID, 1); // par. 12.8.3
    }


    public void closeCombat(int shipID, Player player, String enemiesStr, MarinesCompartment location) {
        int marinesLoss = rollDice(shipID, player) * rollDice(shipID, player);

        // par. 12.2.2.3
        if (game.getCurrentPlayer() == ships[shipID].getOwner()
                && ships[shipID].isBoardingFirstTurn() == BoardingFirstTurn.YES)
            marinesLoss *= 2;
        // --

        String[] enemiesStrs = enemiesStr.split(" ");

        // par. 12.2.2.4
        for (int i = 0; i < enemiesStrs.length; i++) {
            for (Player plr : Player.values()) {
                if (plr.toString().equals(enemiesStrs[i]))
                    killMarines(shipID, plr, location, marinesLoss / enemiesStrs.length, KillingMode.WITHOUT_COMMANDER);
                break;
            }
        }
        // --

        ships[shipID].setBoardingActionUsed(game.getCurrentPlayer(), location, 1);
        calculateShipOwner(shipID);
    }


    public void makeShallowEscapeAttempt(int shipID, ShallowAttempt type, Integer towedID) {
        boolean attemptSuccessful = false;
        Player currentPlayer = game.getCurrentPlayer();

        if (ships[shipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON) {
            ships[shipID].makeEscapeAttempt(type); // par. 17.7

            if (type == ShallowAttempt.PULL_ANCHOR || type == ShallowAttempt.TOW_BY_ONE)
                return;

            if (type == ShallowAttempt.DROP_SILVER && rollDice(shipID, currentPlayer) > 2)
                attemptSuccessful = true; // par. 17.8.2
            if (type == ShallowAttempt.DROP_CANNONS && rollDice(shipID, currentPlayer) > 1)
                attemptSuccessful = true; // par. 17.9.2
            if (type == ShallowAttempt.TOW_BY_BOATS
                    && rollDice(shipID, currentPlayer) + rollDice(shipID, currentPlayer) > 10)
                attemptSuccessful = true; // 17.12.1

            if (attemptSuccessful) {
                MainBoard.addMessage("Ship #" + shipID + ": escaped from treachous waters\n");
                getShip(shipID).escapeFromShallow();
                return;
            } else
                MainBoard.addMessage("Ship #" + shipID + ": failed to escape from treachous waters\n");
        } else {
            ships[towedID].makeEscapeAttempt(type);
        }
    }


    public boolean checkIfEscapeAttemptPossible(int shipID, ShallowAttempt type, Integer towedID) {
        if (ships[shipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.OFF) {
            // statek taki moze byc tylko holownikiem
            if (type == ShallowAttempt.TOW_BY_ONE) {
                if (ships[towedID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.OFF
                        || !checkIfTowable(shipID, towedID)
                        || !(ships[shipID].getMovesQueueCode() == MovesQueueCode.NEW || ships[shipID]
                                .getMovesQueueCode() == MovesQueueCode.ROTATE))
                    // par. 17.11.1 (ostatni warunek)
                    return false;
                return true;
            } else
                return false;
        } else {
            // okret, ktory faktycznie ugrzazl na mieliznie
            if (type == ShallowAttempt.TOW_BY_ONE)
                return false;
            else
                return ships[shipID].checkEscapeAttempt(type);
        }
    }


    public int checkMaxQuantityToHandle(int sourceID, int targetID, CargoDestination from, CargoDestination to,
            CargoType cargoType, GunCompartment sourceCompartment, GunCompartment targetCompartment) {
        int max = 0;
        Gun gunType = Gun.NONE;
        Player currentPlayer = game.getCurrentPlayer();

        if (cargoType == CargoType.CANNONS_LIGHT)
            gunType = Gun.LIGHT;
        if (cargoType == CargoType.CANNONS_MEDIUM)
            gunType = Gun.MEDIUM;

        if (cargoType == CargoType.SILVER) {
            max = ships[sourceID].getLoad(CargoType.SILVER);
            max = Math.min(max,
                    ships[sourceID].getMarinesNumber(currentPlayer, MarinesCompartment.INMOVE, Commons.READY));
            max = Math.min(max, ships[targetID].getLoad(CargoType.FREE_SPACE));
            max = max - (max % 10); // 10 marynarzy - 10t srebra (mozna
                                    // przenoscic tylko pelne 10t)
            return max;
        }

        if (from == CargoDestination.CARGO)
            max = ships[sourceID].getLoad(cargoType);
        else
            max = ships[sourceID].getCannonsNumber(sourceCompartment, gunType, Commons.BOTH);

        if (cargoType == CargoType.CANNONS_LIGHT) {
            max = Math.min(max,
                    ships[sourceID].getMarinesNumber(currentPlayer, MarinesCompartment.INMOVE, Commons.READY) / 10);
            if (to == CargoDestination.CARGO)
                max = Math.min(max, ships[targetID].getLoad(CargoType.FREE_SPACE)); // dzialo
                                                                                    // lekkie
                                                                                    // wazy
                                                                                    // 1t
            else {
                // par. 14.3
                int can = ships[targetID].getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.LIGHT
                        .ordinal()]
                        + ships[targetID].getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.MEDIUM
                                .ordinal()]
                        + ships[targetID].getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.HEAVY
                                .ordinal()]
                        - ships[targetID].getCannonsNumber(targetCompartment, Gun.LIGHT, Commons.BOTH)
                        - ships[targetID].getCannonsNumber(targetCompartment, Gun.MEDIUM, Commons.BOTH)
                        - ships[targetID].getCannonsNumber(targetCompartment, Gun.HEAVY, Commons.BOTH);
                max = Math.min(max, can);
                // --
            }
        }
        if (cargoType == CargoType.CANNONS_MEDIUM) {
            max = Math.min(max,
                    ships[sourceID].getMarinesNumber(currentPlayer, MarinesCompartment.INMOVE, Commons.READY) / 20);
            if (to == CargoDestination.CARGO)
                max = Math.min(max, ships[targetID].getLoad(CargoType.FREE_SPACE) / 2); // dzialo
                                                                                        // srednie
                                                                                        // wazy
                                                                                        // 2t
            else {
                // par. 14.3
                int can = ships[targetID].getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.MEDIUM
                        .ordinal()]
                        + ships[targetID].getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.HEAVY
                                .ordinal()]
                        - ships[targetID].getCannonsNumber(targetCompartment, Gun.MEDIUM, Commons.BOTH)
                        - ships[targetID].getCannonsNumber(targetCompartment, Gun.HEAVY, Commons.BOTH);
                max = Math.min(max, can);
                // --
            }
        }

        return max;
    }


    public boolean checkIfHandleUncouplePossible(int oneID, int twoID) {
        if (!ships[oneID].isShipCoupled(twoID))
            return false;

        if (ships[oneID].getCoupleReason(twoID) != CoupleReason.HANDLING)
            return false;

        if (ships[oneID].isActionsOver() == Commons.ON)
            return false;

        return true;
    }


    public boolean checkIfSabotageSuccessful(int shipID, Player player, MarinesCompartment location) {
        int enemyMarinesNumber = 0;
        for (Player plr : Player.values()) {
            if (plr == Player.NONE)
                continue;
            if (!game.getPlayer(player).isAlly(plr))
                enemyMarinesNumber += ships[shipID].getMarinesNumber(plr, location, Commons.BOTH);
        }

        // par. 12.2.3.1
        if (enemyMarinesNumber == 0
                && rollDice(shipID, player) + rollDice(shipID, player) < ships[shipID].getMarinesNumber(player,
                        location, Commons.BOTH))
            return true;
        // --
        // par. 12.2.3.2
        if (enemyMarinesNumber > 0
                && rollDice(shipID, player) + rollDice(shipID, player) + rollDice(shipID, player) < ships[shipID]
                        .getMarinesNumber(player, location, Commons.BOTH) - enemyMarinesNumber)
            return true;
        // --

        return false;
    }


    private CompartmentAllies getCompartmentAllies(int shipID, Player player, MarinesCompartment location) {
        /*
         * #rv: ENEMY_PRESENCE - gdy w przedziale choc jeden wrogi marynaz
         * ALLIES_ONLY - gdy brak wrogow i choc jeden marynaz sojuszniczy NONE -
         * gdy grak wrogow i brak sojusznikow
         */

        boolean ally = false;
        for (Player plr : Player.values()) {
            if (plr == Player.NONE)
                continue;
            if (plr == player)
                continue;
            if (!game.getPlayer(player).isAlly(plr) && ships[shipID].getMarinesNumber(plr, location, Commons.BOTH) > 0)
                return CompartmentAllies.ENEMY_PRESENCE;
            if (game.getPlayer(player).isAlly(plr) && ships[shipID].getMarinesNumber(plr, location, Commons.BOTH) > 0)
                ally = true;
        }

        if (ally)
            return CompartmentAllies.ALLIES_ONLY;
        else
            return CompartmentAllies.NONE;
    }


    public void moveCommander(int shipID, MarinesCompartment source, MarinesCompartment destination) {
        getShip(shipID).moveCommander(game.getCurrentPlayer(), source, destination);
        MainBoard.addMessage("Ship #" + shipID + ": commander moved from " + source.toString() + " to "
                + destination.toString() + "\n");
    }


    void storm(int shipID) {
        if (!getShip(shipID).isOnGameBoard())
            return;

        if (rollDice(shipID, getShip(shipID).getOwner()) < 5)
            return; // par. 9.5

        int eventType = rollDice(shipID, getShip(shipID).getOwner()) + rollDice(shipID, getShip(shipID).getOwner())
                + rollDice(shipID, getShip(shipID).getOwner());
        Player shipOwner = getShip(shipID).getOwner();

        MainBoard.addMessage("Ship #" + shipID + ": storm event #" + eventType + "happen\n");

        if (eventType == 3) {
            if (game.getWindSpeed() >= 10)
                getShip(shipID).ripSailes();
            return;
        }

        if (eventType == 4) {
            if (destroyHeaviestCannon(shipID, GunCompartment.SIDE_R))
                damageHull(shipID, 2);
            return;
        }

        if (eventType == 5) {
            if (destroyHeaviestCannon(shipID, GunCompartment.SIDE_L)) {
                killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES, 4, KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(shipID);
            }
            return;
        }

        if (eventType == 6) {
            while (destroyCannonIS(shipID, GunCompartment.BOW, Gun.LIGHT, MsgMode.ON)) {
                killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES, Gun.LIGHT.getCrewSize(),
                        KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(shipID);
            }

            while (destroyCannonIS(shipID, GunCompartment.BOW, Gun.MEDIUM, MsgMode.ON)) {
                killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES, Gun.MEDIUM.getCrewSize(),
                        KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(shipID);
            }
            return;
        }

        if (eventType == 7) {
            destroyMast(shipID, 5);
            return;
        }

        if (eventType == 8) {
            int cannons = getShip(shipID).getCannonsNumber(GunCompartment.STERN, Gun.LIGHT, Commons.BOTH);
            killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES,
                    Gun.LIGHT.getCrewSize() * Math.min(cannons, 1), KillingMode.WITHOUT_COMMANDER);
            calculateShipOwner(shipID);

            while (destroyCannonIS(shipID, GunCompartment.BOW, Gun.LIGHT, MsgMode.ON))
                continue;

            return;
        }

        if (eventType == 9) {
            damageHull(shipID, rollDice(shipID, getShip(shipID).getOwner()));
            return;
        }

        if (eventType == 10) {
            getShip(shipID).modifyHappiness(1);
            return;
        }

        if (eventType == 11) {
            killMarines(shipID, Player.NONE, MarinesCompartment.DECK, 1, KillingMode.WITH_COMMANDER);
            calculateShipOwner(shipID);

            return;
        }

        if (eventType == 12) {
            if (destroyHelm(shipID, 1))
                getShip(shipID).modifyHappiness(-1);
            return;
        }

        if (eventType == 13) {
            if (destroyHelm(shipID, getShip(shipID).getShipClass().getHelmMax())) {
                getShip(shipID).modifyHappiness(-getShip(shipID).getHappiness());
            }
            return;
        }

        if (eventType == 14) {
            if (destroyHeaviestCannon(shipID, GunCompartment.SIDE_L)) {
                killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES, 4, KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(shipID);
            }
            return;

        }

        if (eventType == 15) {
            if (destroyHeaviestCannon(shipID, GunCompartment.SIDE_R))
                damageHull(shipID, 2);
            return;
        }

        if (eventType == 16) {
            killMarines(shipID, Player.NONE, MarinesCompartment.DECK, rollDice(shipID, shipOwner),
                    KillingMode.WITH_COMMANDER);
            calculateShipOwner(shipID);
            return;
        }

        if (eventType == 17) {
            killMarines(shipID, Player.NONE, MarinesCompartment.DECK, 10, KillingMode.WITH_COMMANDER);
            calculateShipOwner(shipID);
            return;
        }

        if (eventType == 18) {
            sinkShip(shipID, DestroyShipMode.SINK);
            return;
        }
    }


    public boolean checkIfShipBoarded(int shipID, Player player) {
        for (MarinesCompartment comp : MarinesCompartment.values()) {
            if (comp == MarinesCompartment.SHIP_X || comp == MarinesCompartment.NONE)
                continue;

            if ((getShip(shipID).getMarinesNumber(player, comp, Commons.BOTH) > 0
                    || getShip(shipID).getCommanderState(player, comp) == CommanderState.READY || getShip(shipID)
                    .getCommanderState(player, comp) == CommanderState.USED)
                    && DataExtractors.getEnemyGroups(getShip(shipID), game.getPlayer(player), comp).length() > 0)
                return true;
        }

        return false;
    }


    public int findFreeShipSlot() {
        int slot = Commons.NIL;
        for (int i = 1; i < Commons.SHIPS_MAX; i++) {
            if (ships[i].getID() == null)
                break;
        }

        return slot;
    }


    public Ship getShipOnHex(Coordinate coord) {
        for (Ship s : ships)
            if (s.getPosition().equals(coord))
                return s;
        return null;
    }


    void moveShip(int shipID, int distance) {
        Player currentPlayer = game.getCurrentPlayer();
        Coordinate shift = new Coordinate(0, 0); // przesuniecie [A,B] (w
                                                 // przypadku udanego ruchu)

        Coordinate position = ships[shipID].getPosition();
        RotateDirection rotation = ships[shipID].getRotation();

        Coordinate towedNewCrd = Coordinate.dummy;

        boolean tugChangedOwner = false;
        boolean towedChangedOwner = false;
        boolean rammed = false;
        boolean crashed = false;
        boolean ranAground = false;

        Integer shipTowedBy = ships[shipID].getTowOther();

        if (distance == 0)
            return;

        switch (rotation) {
        case N:
            shift = new Coordinate(0, distance);
            break;
        case NE:
            shift = new Coordinate(distance, distance);
            break;
        case SE:
            shift = new Coordinate(distance, 0);
            break;
        case S:
            shift = new Coordinate(0, -distance);
            break;
        case SW:
            shift = new Coordinate(-distance, -distance);
            break;
        case NW:
            shift = new Coordinate(-distance, 0);
            break;
        }

        if (!Board.isOnMap(position.sum(shift))) {
            MainBoard.addMessage("Ship #" + shipID + " escaped.\n");

            // par. 5.4.1, 5.4.2
            ships[shipID].setParameter(Parameter.IS_OUTSIDE_MAP, Commons.ON);
            getShip(shipID).setPosition(Coordinate.dummy);
            // --

            if (shipTowedBy != null) {
                /*
                 * Sytuacja, gdy holownik stoi na własnym polu, tuż przy
                 * krawędzi planszy, a okręt holowany nie znajduje się na polu
                 * tego gracza (wtedy funkcja checkDistanceToMove() nie wychwyci
                 * tej sytuacji).
                 */
                if (ships[shipTowedBy].getOwner() != ships[shipID].getOwner())
                    captureShip(shipTowedBy, game.getPlayer(ships[shipID].getOwner()));
                else {
                    // Zakładam, że okręt holowany wypływa poza mapę wraz z
                    // holownikiem.
                    MainBoard.addMessage("Ship #" + shipTowedBy + " escaped.\n");
                    ships[shipTowedBy].setParameter(Parameter.IS_OUTSIDE_MAP, Commons.ON);
                    getShip(shipTowedBy).setPosition(Coordinate.dummy);
                    throwTow(shipID);
                }
            }

            MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
            return;
        }

        Player owner = ships[shipID].getOwner();
        ObstacleReport report = game.getBoard().isObstacleOnPath(position, rotation, distance, owner,
                game.getPlayer(owner).getAllies());

        if (!game.getPlayer(ships[shipID].getOwner()).isAlly(report.hexOwner)) {
            // par. 5.5.2
            if (checkIfPlayerControlsLocation(shipID, report.hexOwner, MarinesCompartment.DECK, true)) {
                captureShip(shipID, game.getPlayer(report.hexOwner));
                getShip(shipID).setPosition(position.sum(shift));
            }
            // --
            // par. 5.6.1
            else
                internShip(shipID, report.hexOwner);
            // --

            tugChangedOwner = true;
        }

        /**
         * funkcja isObstacle.. sprawdza przeszkody na trasie holownik->dystans
         * powinno byc sprawdzanie najpierw dla okretu holowanego, potem dla
         */

        if (report.hexTerrainType == Terrain.SHALLOW) {
            // par. 17.1
            if (ships[shipID].getLoad(CargoType.SILVER) == 0 && rollDice(shipID, currentPlayer) > 4
                    || ships[shipID].getLoad(CargoType.SILVER) != 0 && rollDice(shipID, currentPlayer) > 2) {

                MainBoard.addMessage("Ship #" + shipID + " ran aground.\n");
                ships[shipID].setParameter(Parameter.IS_IMMOBILIZED, Commons.ON);
                ships[shipID].addMovementCode(MovementType.END_MOVE);
                ships[shipID].modifyHappiness(-1); // par. 17.3

                ranAground = true;
            }
            // --
        }

        if (report.hexTerrainType == Terrain.ISLAND) {
            crashed = true;
            sinkShip(shipID, DestroyShipMode.SINK);
        }

        if (report.hexShipID != null) {
            /*
             * Modyfikacja wynikajaca z natury zdarzenia (zaznaczamy, ze chcemy
             * staranowac przeciwnika poprzez -umowny- ruch na pole zajmowane
             * przez wrogi okręt, a faktycznie zatrzymujemy się pole przed nim.
             */

            switch (rotation) {
            case N:
                getShip(shipID).setPosition(position.getA(), position.getB() + distance - 1);
                break;
            case NE:
                getShip(shipID).setPosition(position.getA() + distance - 1, position.getB() + distance - 1);
                break;
            case SE:
                getShip(shipID).setPosition(position.getA() + distance - 1, position.getB());
                break;
            case S:
                getShip(shipID).setPosition(position.getA(), position.getB() - distance + 1);
                break;
            case SW:
                getShip(shipID).setPosition(position.getA() - distance + 1, position.getB() - distance + 1);
                break;
            case NW:
                getShip(shipID).setPosition(position.getA() - distance + 1, position.getB());
                break;
            }

            runDown(shipID, report.hexShipID);
            rammed = true;
        }

        if (shipTowedBy != null) {
            // par. 16.9 (nowe współrzędne okrętu holowanego - heks tuż za
            // holownikiem)

            if (shift.getA() != 0)
                towedNewCrd.setA(position.getA() + (Math.abs(shift.getA()) - 1)
                        * (shift.getA() / Math.abs(shift.getA())));
            if (shift.getB() != 0)
                towedNewCrd.setB(position.getB() + (Math.abs(shift.getB()) - 1)
                        * (shift.getB() / Math.abs(shift.getB())));
            // --

            Player hexOwner = game.getBoard().getHex(towedNewCrd).owner;
            if (hexOwner != Player.NONE && hexOwner != ships[shipTowedBy].getOwner()) {
                // par. 5.5.2
                if (checkIfPlayerControlsLocation(shipTowedBy, hexOwner, MarinesCompartment.DECK, true))
                    captureShip(shipTowedBy, game.getPlayer(hexOwner));
                // --
                // par. 5.6.1
                else
                    internShip(shipTowedBy, hexOwner);
                // --
                towedChangedOwner = true;
            }

            if (game.getBoard().getHex(towedNewCrd).terrain == Terrain.SHALLOW) {
                // par. 17.1
                if (ships[shipTowedBy].getLoad(CargoType.SILVER) == 0 && rollDice(shipID, currentPlayer) > 4
                        || ships[shipTowedBy].getLoad(CargoType.SILVER) != 0 && rollDice(shipID, currentPlayer) > 2) {
                    MainBoard.addMessage("Ship #" + shipTowedBy + " ran aground.\n");
                    ships[shipTowedBy].setParameter(Parameter.IS_IMMOBILIZED, Commons.ON);
                    ships[shipTowedBy].modifyHappiness(-1); // par. 17.3
                    ranAground = true;
                }
                // --
            }
        }

        if (!tugChangedOwner && !crashed && !rammed) {
            // Z holownikiem nic się nie stało.
            getShip(shipID).setPosition(position.sum(shift));
            ships[shipID].addMovementCode(MovementType.TRANSFER);
            ships[shipID].setDistanceMoved(ships[shipID].getDistanceMoved() + distance);
        }

        if (shipTowedBy != null && !towedChangedOwner) {
            // par. 16.9
            getShip(shipTowedBy).setPosition(towedNewCrd);
            ships[shipTowedBy].setRotation(ships[shipID].getRotation());
            // --
            ships[shipTowedBy].addMovementCode(MovementType.TRANSFER);

        }

        if (!tugChangedOwner && !crashed && !towedChangedOwner && !ranAground && !rammed)
            MainBoard.addMessage("Ship #" + shipID + ": succesfully moved.\n");
        else
            MainBoard.addMessage("Ship #" + shipID + ": movement problem occured (new position is ["
                    + (ships[shipID].getPosition().getA() + 1) + "," + (ships[shipID].getPosition().getB() + 1)
                    + "])\n");
    }


    private void captureShip(int shipID, PlayerClass newOwner) {
        MainBoard.addMessage("Ship #" + shipID + ": captured by " + newOwner.toString() + "\n");

        ships[shipID].setOwner(newOwner.getIdentity());

        Integer towedBy = ships[shipID].getTowedBy();
        // par. 18.3 (zmienna pomocnicza)
        if (towedBy != null)
            ships[towedBy].setHappinessCapture(true);
        // --

        for (Player plr : Player.values()) {
            if (plr == Player.NONE || plr == newOwner.getIdentity())
                continue;

            for (MarinesCompartment location : MarinesCompartment.values()) {
                if (location == MarinesCompartment.SHIP_X)
                    continue;

                newOwner.addMarinesInterned(plr, ships[shipID].getMarinesNumber(plr, location, Commons.BOTH));
                killMarines(shipID, plr, location, ships[shipID].getMarinesNumber(plr, location, Commons.BOTH),
                        KillingMode.WITHOUT_COMMANDER);

                if (ships[shipID].getCommanderState(plr, location) != CommanderState.NOT_THERE) {
                    if (newOwner.isAlly(plr)
                            && ships[shipID].getCommanderState(plr, location) == CommanderState.IMPRISONED) {
                        ships[shipID].setCommander(plr, location, CommanderState.READY);
                    }
                    if (!newOwner.isAlly(plr)) {
                        Coordinate pos = ships[shipID].getPosition();
                        if (game.getBoard().getHex(pos).owner == newOwner.getIdentity()) {
                            newOwner.addCommandersInterned(plr);
                            ships[shipID].setCommander(plr, location, CommanderState.NOT_THERE);
                        } else {
                            ships[shipID].setCommander(plr, location, CommanderState.NOT_THERE);
                            ships[shipID].setCommander(plr, MarinesCompartment.INMOVE, CommanderState.IMPRISONED);
                        }
                    }
                }
            }
        }
    }


    private void internShip(int shipID, Player newOwner) {
        MainBoard.addMessage("Ship #" + shipID + ": interneed by " + newOwner.toString() + "\n");

        getShip(shipID).setPosition(Coordinate.dummy);
        throwTow(shipID);

        /*
         * Póki co nie ma możliwości zachowania neutralności. Są tylko
         * sojusznicy i wrogowie. // par. 5.6.4 Player shipOwner =
         * ships[shipID].getOwner(); if (!((shipOwner == Player.PASADENA ||
         * shipOwner == Player.SIDONIA) && (newOwner == Player.PASADENA ||
         * newOwner == Player.SIDONIA)) ... // --
         */

        game.getPlayer(getShip(shipID).getOwner()).removeShipFromFleet(shipID);
        ships[shipID].setInternedBy(newOwner);
        game.getPlayer(newOwner).addInternedShip(ships[shipID].getOwner(), shipID);
        game.getPlayer(getShip(shipID).getOwner()).addInternedShip(ships[shipID].getOwner(), shipID);
    }


    public MovementEvent forcedShipMovement(int shipID, ShipMovementMode mode)
    /*
     * Funkcja obsługuje wymuszyny ruch okrętu (-zawsze- o jedno pole z
     * wiatrem).
     */
    {
        Coordinate shift = new Coordinate(0, 0); // przesuniecie (w przypadku
                                                 // udanego ruchu)
        Coordinate position = ships[shipID].getPosition();
        RotateDirection rotation = ships[shipID].getRotation();

        switch (rotation) {
        case N:
            shift = new Coordinate(0, 1);
            break;
        case NE:
            shift = new Coordinate(1, 1);
            break;
        case SE:
            shift = new Coordinate(1, 0);
            break;
        case S:
            shift = new Coordinate(0, -1);
            break;
        case SW:
            shift = new Coordinate(-1, -1);
            break;
        case NW:
            shift = new Coordinate(-1, 0);
            break;
        }

        // par. 15.6 (jedynie huragan może zepchnąć wrak z mielizny)
        if (mode == ShipMovementMode.MOVE_WRECK_NORMAL && ships[shipID].getParameter(Parameter.IS_WRECK) == Commons.ON
                && ships[shipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON)
            return MovementEvent.NONE;
        // --

        if (!Board.isOnMap(position.sum(shift))) {
            getShip(shipID).setPosition(Coordinate.dummy);
            MainBoard.setSelectedShip(null, Tabs.MOVEMENT);

            // par. 15.4
            if (ships[shipID].getParameter(Parameter.IS_WRECK) == Commons.ON) {
                sinkShip(shipID, DestroyShipMode.SINK);
                return MovementEvent.SUNK;
            }
            // --
            else {
                MainBoard.addMessage("Ship #" + shipID + ": escaped.\n");
                return MovementEvent.ESCAPED;
            }
        }

        Player owner = ships[shipID].getOwner();
        ObstacleReport report = game.getBoard().isObstacleOnPath(position, rotation, 1, owner,
                game.getPlayer(owner).getAllies());

        if (report.hexOwner != Player.NONE) {
            if (ships[shipID].getMarinesNumber(report.hexOwner, MarinesCompartment.DECK, Commons.BOTH) > 0
                    || ships[shipID].getOwner() == Player.NONE) {
                captureShip(shipID, game.getPlayer(report.hexOwner));
                return MovementEvent.CAPTURED;
            } else {
                internShip(shipID, report.hexOwner);
                return MovementEvent.INTERNED;
            }
        }

        if (report.hexTerrainType == Terrain.SHALLOW) {
            // par. 15.5, 17.1
            if (ships[shipID].getLoad(CargoType.SILVER) == 0 && rollDice(shipID, ships[shipID].getOwner()) > 4
                    || ships[shipID].getLoad(CargoType.SILVER) != 0 && rollDice(shipID, ships[shipID].getOwner()) > 2
                    || ships[shipID].getParameter(Parameter.IS_WRECK) == Commons.ON) {
                MainBoard.addMessage("Ship #" + shipID + ": ran aground.\n");
                ships[shipID].setParameter(Parameter.IS_IMMOBILIZED, Commons.ON);
                ships[shipID].modifyHappiness(-1); // par. 17.3
            }
        } else if (report.hexTerrainType == Terrain.ISLAND) {
            sinkShip(shipID, DestroyShipMode.SINK);
            return MovementEvent.SUNK;
        } else {
            // par. 17.14
            if (ships[shipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON)
                getShip(shipID).escapeFromShallow();
            // --
        }

        if (report.hexShipID != null) {
            MainBoard.addMessage("Ship #" + report.hexShipID + ": rammed by ship" + shipID + ".\n");

            if (runDown(shipID, report.hexShipID))
                return MovementEvent.SUNK;
            else
                return MovementEvent.NONE;
        }

        getShip(shipID).setPosition(position.sum(shift));
        return MovementEvent.NONE;
    }


    public void rotateShip(int shipID, int _angle) {
        ships[shipID].setRotation(RotateDirection.valueOf((ships[shipID].getRotation().ordinal() + _angle + 6) % 6));

        if (game.getStage() != Stage.DEPLOYMENT)
            ships[shipID].useHelm(_angle);

        if (ships[shipID].getTowOther() != null)
            ships[shipID].useHelm(ships[shipID].getHelm(Commons.READY)); // 16.8

        ships[shipID].addMovementCode(MovementType.ROTATE);
    }


    public void shoot(int shipID, int targetShipID, int distance, GunCompartment compartment, Gun ownGunType,
            AimPart aimedPart, Gun aimedGunType) {
        int accuracyBonus = 0; // bonus +1 do celnosci, gdy abordaz pod pokladem
                               // lub sczepienie

        Player currentPlayer = game.getCurrentPlayer();

        if (ships[targetShipID].getShipsCoupled().size() > 0)
            accuracyBonus = 1;

        // par. 12.7
        int groupNumber = DataExtractors.getCompartmentGroups(getShip(shipID), MarinesCompartment.BATTERIES).split("#").length;
        if (groupNumber > 0)
            accuracyBonus = 1;
        // --

        ships[shipID].shoot(currentPlayer, compartment, ownGunType);

        // par. 10.4
        if (rollDice(shipID, currentPlayer) + accuracyBonus <= distance + 2) {
            MainBoard.addMessage("Ship #" + shipID + ": shot missed the ship.\n");
            return;
        }
        // --
        else
            MainBoard.addMessage("Ship #" + shipID + ": shot hit the ship.\n");

        // par. 3.2 (operacja pomocnicza)
        if (damageHull(targetShipID, ownGunType.getShotDamage())) {
            game.getPlayer(currentPlayer).addDestroyedShip();
            return;
        }
        // --

        if (aimedPart == AimPart.RIGGING) {
            if (rollDice(shipID, currentPlayer) >= 6 - ownGunType.ordinal()) {
                MainBoard.addMessage("Ship #" + shipID + ": shot hit the part.\n");
                destroyMast(targetShipID, 1);
            } else
                MainBoard.addMessage("Ship #" + shipID + ": shot missed the part.\n");

            return;
        }

        /*
         * W poniższych wywołaniach funkcji killMarines() przekazujemy player =
         * Player.NONE, gdyż zgodnie z par. 12.7 straty są dzielone między
         * wszystkich graczy.
         */

        if (aimedPart == AimPart.CANNON) {
            if (rollDice(shipID, currentPlayer) >= 5) {
                MainBoard.addMessage("Ship #" + shipID + ": shot hit the part.\n");
                if (destroyCannonIS(targetShipID, calculateCompartmentToAim(shipID, targetShipID), aimedGunType,
                        MsgMode.ON)) {
                    killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES, aimedGunType.getCrewSize(),
                            KillingMode.WITHOUT_COMMANDER);
                    calculateShipOwner(shipID);
                }
            } else
                MainBoard.addMessage("Shot missed the target.\n");

            return;
        }

        if (aimedPart == AimPart.HULL) {
            int roll = rollDice(shipID, currentPlayer);
            GunCompartment comp = calculateCompartmentToAim(shipID, targetShipID);
            int marinesToKill = 0;

            MainBoard.addMessage("Hull damage code: " + roll + "\n");

            switch (roll) {
            case 1:
                marinesToKill = Math.min(1, ships[targetShipID].getCannonsNumber(comp, Gun.HEAVY, Commons.BOTH))
                        * Gun.HEAVY.getCrewSize();
                break;
            case 2:
                marinesToKill = Math.min(2, ships[targetShipID].getCannonsNumber(comp, Gun.LIGHT, Commons.BOTH))
                        * Gun.LIGHT.getCrewSize();
                break;
            case 3:
                marinesToKill = Math.min(1, ships[targetShipID].getCannonsNumber(comp, Gun.MEDIUM, Commons.BOTH))
                        * Gun.MEDIUM.getCrewSize();
                break;
            case 4:
                marinesToKill = Math.min(1, ships[targetShipID].getCannonsNumber(comp, Gun.LIGHT, Commons.BOTH))
                        * Gun.LIGHT.getCrewSize();
                break;
            case 5:
                marinesToKill = Math.min(2, ships[targetShipID].getCannonsNumber(comp, Gun.MEDIUM, Commons.BOTH))
                        * Gun.MEDIUM.getCrewSize();
                break;
            case 6:
                marinesToKill = 0;
                break;
            }

            killMarines(shipID, Player.NONE, MarinesCompartment.BATTERIES, marinesToKill, KillingMode.WITHOUT_COMMANDER);
            calculateShipOwner(shipID);
        }
    }


    public int checkDistanceToMove(int shipID) {
        /*
         * Funkcja zwraca ilość pól możliwych do przebycia przez jednostkę z
         * uwzględnieniem przeszkód terenowych, pól graczy itp.
         */

        Player currentPlayer = game.getCurrentPlayer();

        int maxDistance = 0;

        ObstacleReport report = new ObstacleReport();
        Player previousHexOwner = Player.NONE; // właściciel poprzednio
                                               // sprawdzanego pola
        Terrain previousHexTerrain = Terrain.WATER; // typ terenu poprzednio
        // sprawdzonego pola

        int signA = 0; // Ak-Ap (zakładajc ruch o jedno pole)
        int signB = 0; // Bk-Bp (zakładajc ruch o jedno pole)

        int shiftA = 0; // signA*distance (0 < distance <= maxDistance)
        int shiftB = 0; // shiftB*distance (0 < distance <= maxDistance)

        Coordinate position = ships[shipID].getPosition();
        RotateDirection rotation = ships[shipID].getRotation();
        int crdA = position.getA();
        int crdB = position.getB();

        // par. 8.5
        if (!ships[shipID].isMovementPossible(MovementType.TRANSFER))
            return 0;
        // --

        // par. 17.2
        if (ships[shipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON)
            return 0;
        // --

        if (ships[shipID].getParameter(Parameter.IS_WRECK) == Commons.ON || ships[shipID].getTowedBy() != null) {
            return 0;
        }

        // par. 8.4
        if (Math.abs(game.getWindDirection().ordinal() - rotation.ordinal()) == 3)
            return 0;
        // --

        // par. 12.3
        if (ships[shipID].getShipsCoupled().size() > 0)
            return 0;
        // --

        // par. 2.5, 8.1
        int maxDist_ = Math.min(ships[shipID].getMast(),
                getAlliedMarinesNumber(shipID, currentPlayer, MarinesCompartment.DECK) / 2)
                + game.getWindSpeed();

        // par. 16.7
        if (ships[shipID].getTowOther() != null)
            maxDist_ -= 1;
        // --

        // par. 8.2, 8.3
        int delta = Math.abs(game.getWindDirection().ordinal() - rotation.ordinal());
        if (delta < 3)
            maxDist_ /= WIND_FACTOR[delta];
        if (delta > 3)
            maxDist_ /= WIND_FACTOR[6 - delta];
        // -- --

        if (maxDist_ == 0)
            return 0;

        Integer towedShipID = ships[shipID].getTowOther();
        if (towedShipID != null) {
            // par. 16.12
            if (game.getWindDirection().ordinal() - rotation.ordinal() == 0)
                return 0;
            // --

            if (ships[towedShipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON)
                return 0;

            // par. 16.5
            if (!checkIfPlayerControlsLocation(towedShipID, currentPlayer, MarinesCompartment.DECK, false))
                return 0;
            // --

            if (!this.checkIfStillTowable(shipID, towedShipID))
                return 0;
        }

        previousHexOwner = game.getBoard().getHex(position).owner;
        previousHexTerrain = game.getBoard().getHex(position).terrain;

        switch (rotation) {
        case N:
            signA = 0;
            signB = 1;
            break;
        case NE:
            signA = 1;
            signB = 1;
            break;
        case SE:
            signA = 1;
            signB = 0;
            break;
        case S:
            signA = 0;
            signB = -1;
            break;
        case SW:
            signA = -1;
            signB = -1;
            break;
        case NW:
            signA = -1;
            signB = 0;
            break;
        }

        for (maxDistance = 1; maxDistance <= maxDist_; maxDistance++) {
            shiftA = signA * maxDistance;
            shiftB = signB * maxDistance;

            // par. 5.4.1
            if (!Board.isOnMap(new Coordinate(crdA + shiftA, crdB + shiftB))) {
                if (previousHexOwner != currentPlayer)
                    maxDistance--;
                break;
            }
            // --

            /*
             * Poniższy fragment wynika z konieczność: - rzucenia holu z powodu
             * internowania bądź zdobycia okrętu holowanego - sprawdzenia, czy
             * okręt holowany nie utknął na mieliźnie
             */
            if (towedShipID != null) {
                if (!game.getPlayer(previousHexOwner).isAlly(ships[towedShipID].getOwner()))
                    break;
                if (previousHexTerrain == Terrain.SHALLOW)
                    break;
            }
            // --

            Player owner = ships[shipID].getOwner();
            report = game.getBoard().isObstacleOnPath(position, rotation, maxDistance, owner,
                    game.getPlayer(owner).getAllies());

            /*
             * Fragment dotyczący pola-mielizny jest konieczny, gdyż w każdej
             * chwili okręt może utknąć na mieliźnie.
             */
            if (report.hexOwner != Player.NONE || report.hexTerrainType == Terrain.SHALLOW
                    || report.hexTerrainType == Terrain.ISLAND || report.hexShipID != null)
                break;

            previousHexOwner = game.getBoard().getHex(crdA + shiftA, crdB + shiftB).owner;
            previousHexTerrain = game.getBoard().getHex(crdA + shiftA, crdB + shiftB).terrain;
        }

        // modyfikacja "techniczna" (gdy brak przeszkod)
        if (maxDistance > maxDist_)
            maxDistance--;
        // --

        // par. 5.3.2
        if (ships[shipID].isCommanderOnboard(currentPlayer) && getPlayerFleetSize(currentPlayer) > 1) {
            if (report.hexTerrainType == Terrain.ISLAND || !Board.isOnMap(new Coordinate(crdA + shiftA, crdB + shiftB)))
                return Math.min(0, maxDistance - 1);

            // Brak możliwości taranowania, gdy spowodowałoby to zatopienie
            // okrętu flagowego
            if (report.hexShipID != null
                    && ships[shipID].getDurability() <= ships[report.hexShipID].getShipClass().getDurabilityMax() / 2)
                return Math.min(0, maxDistance - 1);
        }
        // --

        return maxDistance;
    }


    public boolean checkIfBoardable(int sourceID, Player player, int targetID) {
        /*
         * Uwaga: abordaż okrętu sojuszniczego to nic innego jak przeładunek.
         */

        Coordinate sourcePos = ships[sourceID].getPosition();
        Coordinate targetPos = ships[targetID].getPosition();

        if (Math.abs(sourcePos.getA() - targetPos.getA()) > 1 || Math.abs(sourcePos.getB() - targetPos.getB()) > 1)
            return false; // 12.1

        if ((ships[sourceID].getTowOther() != null || ships[sourceID].getTowedBy() != null)
                && !game.getPlayer(ships[sourceID].getOwner()).isAlly(ships[targetID].getOwner()))
            return false; // par. 16.14

        return true;
    }


    public HandlingPartner checkIfHandleable(int sourceID, Player player, int targetID) {
        Coordinate sourcePos = ships[sourceID].getPosition();
        Coordinate targetPos = ships[targetID].getPosition();

        // par. 14.1
        if (!game.getPlayer(player).isAlly(ships[targetID].getOwner()))
            return HandlingPartner.NONE;
        // --

        // par. 14.2
        if (Math.abs(sourcePos.getA() - targetPos.getA()) > 1 || Math.abs(sourcePos.getB() - targetPos.getB()) > 1)
            return HandlingPartner.NONE;
        // --

        if (ships[sourceID].getOwner() == ships[targetID].getOwner() || ships[targetID].getOwner() == Player.NONE)
            return HandlingPartner.OWN;
        else
            return HandlingPartner.ALLY;
    }


    public boolean checkIfBoardingEscapePossible(int shipID, Player player) {
        Player currentPlayer = game.getCurrentPlayer();

        if (game.getStage() != Stage.BOARDING_ACTIONS)
            return false;

        if (ships[shipID].getBoardingActionUsed(player, MarinesCompartment.DECK) > 0)
            return false;

        // par. 12.8.1
        if (!checkIfPlayerControlsLocation(shipID, currentPlayer, MarinesCompartment.DECK, false))
            return false;
        // --

        // par. 12.8.2
        if (Math.abs(game.getWindDirection().ordinal() - ships[shipID].getRotation().ordinal()) == 3)
            return false;

        if (ships[shipID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON
                || ships[shipID].getParameter(Parameter.IS_WRECK) == Commons.ON || ships[shipID].getMast() == 0
                || ships[shipID].getTowedBy() != null)
            return false;

        Integer towedID = ships[shipID].getTowOther();
        if (towedID != null) {
            if (game.getWindDirection().ordinal() - ships[shipID].getRotation().ordinal() == 0)
                return false; // par. 16.12

            if (ships[towedID].getParameter(Parameter.IS_IMMOBILIZED) == Commons.ON)
                return false;

            if (!checkIfPlayerControlsLocation(towedID, currentPlayer, MarinesCompartment.DECK, true))
                return false; // par. 16.5
        }

        Coordinate shift = new Coordinate(0, 0);

        Coordinate position = ships[shipID].getPosition();
        RotateDirection rotation = ships[shipID].getRotation();

        switch (rotation) {
        case N:
            shift = new Coordinate(0, 1);
            break;
        case NE:
            shift = new Coordinate(1, 1);
            break;
        case SE:
            shift = new Coordinate(1, 0);
            break;
        case S:
            shift = new Coordinate(0, -1);
            break;
        case SW:
            shift = new Coordinate(-1, -1);
            break;
        case NW:
            shift = new Coordinate(-1, 0);
            break;
        }

        if (!Board.isOnMap(position.sum(shift))) {
            if (game.getBoard().getHex(position).owner != currentPlayer
                    || ships[shipID].isCommanderOnboard(currentPlayer)
                    && game.getPlayer(currentPlayer).getFleet().size() > 1)
                return false; // par. 5.4.1
        }

        Player owner = ships[shipID].getOwner();
        ObstacleReport report = game.getBoard().isObstacleOnPath(position, rotation, 1, owner,
                game.getPlayer(owner).getAllies());

        if (report.hexTerrainType == Terrain.ISLAND)
            return false;
        if (report.hexShipID != null)
            return false;
        // --

        /*
         * Aby móc wyjść z abordażu, musimy mieć faktycznie do czynienia ze
         * sczepieniem w wyniku abordażu. Dodatkowo, okręt chcący uciec nie może
         * być sczepiony z jakimkolwiek innym okrętem na drodze przeładunku.
         */

        Vector<Integer> coupled = ships[shipID].getShipsCoupled();
        for (int i = 0; i < coupled.size(); i++) {
            if (ships[shipID].getCoupleReason(coupled.get(i)) == CoupleReason.HANDLING)
                return false;
        }
        if (coupled.size() == 0)
            return false;

        return true;
    }


    public int getShipDistanceToMove(int shipID) {
        return Math.max(0, checkDistanceToMove(shipID) - ships[shipID].getDistanceMoved());
    }


    public void deployCommander(int shipID) {
        Player currentPlayer = game.getCurrentPlayer();

        Object[] fleet = game.getPlayer(currentPlayer).getFleet().toArray();

        for (int i = 0; i < fleet.length; i++) {
            if (ships[(Integer) (fleet[i])].getCommanderState(currentPlayer, MarinesCompartment.DECK) == CommanderState.READY) {
                ships[(Integer) (fleet[i])].setCommander(currentPlayer, MarinesCompartment.DECK,
                        CommanderState.NOT_THERE);
                break;
            }
        }

        ships[shipID].setCommander(currentPlayer, MarinesCompartment.DECK, CommanderState.READY);
    }


    public GunCompartment calculateCompartmentToAim(int sourceID, int targetID) {
        /** okret target strzela do source (?) */

        /*
         * Na podstawie kata miedzy wektorem B+, a wektorem celu (oraz obrotu
         * okretu-celu) oblicza, w jaki przedzial dzialowy trafiamy
         */
        Coordinate source = getShip(sourceID).getPosition();
        Coordinate target = getShip(targetID).getPosition();
        int dA = source.getA() - target.getA();
        int dB = source.getB() - target.getB();

        return game.getBoard().calculateCompartmentToAim(dA, dB, getShip(targetID).getRotation().ordinal());
    }


    public int getPlayerFleetSize(Player player) {
        int size = 0;
        for (int i = 0; i < Commons.SHIPS_MAX; i++)
            if (getShip(i).getOwner() == player)
                size++;
        return size;
    }


    void clearSlot(int shipID) {
        ships[shipID] = new Ship();
    }
}
