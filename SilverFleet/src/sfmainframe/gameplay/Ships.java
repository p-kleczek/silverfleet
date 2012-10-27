package sfmainframe.gameplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import sfmainframe.Commons;
import sfmainframe.Coordinate;
import sfmainframe.Dice;
import sfmainframe.MainBoard;
import sfmainframe.Player;
import sfmainframe.PlayerClass;
import sfmainframe.Range;
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
import sfmainframe.ship.Happiness;
import sfmainframe.ship.Parameter;
import sfmainframe.ship.ShallowAttempt;
import sfmainframe.ship.Ship;
import sfmainframe.ship.cargo.CargoDestination;
import sfmainframe.ship.cargo.CargoType;
import sfmainframe.ship.cargo.HandlingPartner;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.CompartmentAllies;
import sfmainframe.ship.marines.MarinesCompartment;

public final class Ships {

    public static final int PREVIOUS_OWNER = -1;
    public static final int COMMANDER_TO_BE_KILLED = 0xAAAA;

    /**
     * Wind factor modifies the maximal distance a ship can cover in one turn
     * [8.3].
     */
    private static final List<Double> WIND_FACTOR = Collections.unmodifiableList(Arrays.asList(new Double[] { 3.5, 5.0,
            7.0 }));

    private static Game game;


    static void coupleShips(Ship shipOne, Ship shipTwo, CoupleReason reason) {
        MainBoard.addMessage("Ship #" + shipOne.getID() + " and ship #" + shipTwo.getID() + " coupled ("
                + reason.toString() + ").\n");

        shipOne.addShipCoupled(shipTwo, reason);
        shipTwo.addShipCoupled(shipOne, reason);
    }


    static void throwTow(Ship ship) {
        Ship s = ship.getTowedBy();
        if (s != null) {
            MainBoard.addMessage("Ship #" + ship.getID() + ": threw tow\n");
            MainBoard.addMessage("Ship #" + s.getID() + ": threw tow\n");
            s.setTowOther(null);
            ship.setTowedBy(null);
            return;
        }

        s = ship.getTowOther();
        if (s != null) {
            MainBoard.addMessage("Ship #" + ship.getID() + ": threw tow\n");
            MainBoard.addMessage("Ship #" + s.getID() + ": threw tow\n");
            s.setTowedBy(null);
            ship.setTowOther(null);
            return;
        }
    }


    /**
     * 
     * @param shipID
     * @param mode
     * @return players who end game
     */
    static void sinkShip(Ship ship, DestroyShipMode mode) {

        MainBoard.addMessage("Ship #" + ship.getID() + " sank!\n");

        ship.setPosition(Coordinate.dummy);
        ship.setParameter(Parameter.IS_SUNK);

        if (mode == DestroyShipMode.BLOWUP)
            blowUpShip(ship);

        for (Ship s : ship.getShipsCoupled().keySet())
            s.uncouple(ship);

        throwTow(ship);

        // par. 5.3.5
        for (MarinesCompartment location : MarinesCompartment.getShipCompartments()) {
            for (Player player : Player.getValues()) {
                if (ship.getCommanderState(player, location) != CommanderState.NOT_THERE)
                    game.endPlayerGame(player);
            }
        }
        // --

        if (ship.getInternedBy() != Player.NONE)
            game.getPlayer(ship.getInternedBy()).removeInternedShip(ship.getOwner(), ship);

        game.getPlayer(ship.getOwner()).removeShipFromFleet(ship);
        if (game.getPlayer(ship.getOwner()).getFleet().size() == 0)
            game.endPlayerGame(ship.getOwner());

        MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
    }


    public static void blowUpShip(Ship ship) {
        MainBoard.addMessage("Ship #" + ship.getID() + ": blown up\n");

        for (Ship s : ship.getShipsCoupled().keySet()) {
            Player shipOwner = s.getOwner();

            // par. 12.2.3.5.2
            Coordinate crd = s.getPosition().diff(ship.getPosition());
            int dA = crd.getA();
            int dB = crd.getB();
            boolean coupledWithSides = false;

            Integer angleModifier = null;
            RotateDirection rotations[] = { RotateDirection.NE, RotateDirection.SE, RotateDirection.SW,
                    RotateDirection.NW };

            if (dA == 0 && dB == 1 && ship.getRotation() != RotateDirection.N
                    && ship.getRotation() != RotateDirection.S)
                angleModifier = 0;

            if (dA == 1 && dB == 1 && ship.getRotation() != RotateDirection.NE
                    && ship.getRotation() != RotateDirection.SW)
                angleModifier = 1;

            if (dA == 1 && dB == 0 && ship.getRotation() != RotateDirection.SE
                    && ship.getRotation() != RotateDirection.NW)
                angleModifier = 2;

            if (dA == 0 && dB == -1 && ship.getRotation() != RotateDirection.N
                    && ship.getRotation() != RotateDirection.S)
                angleModifier = 3;

            if (dA == -1 && dB == -1 && ship.getRotation() != RotateDirection.NE
                    && ship.getRotation() != RotateDirection.SW)
                angleModifier = 4;

            if (dA == -1 && dB == 0 && ship.getRotation() != RotateDirection.NW
                    && ship.getRotation() != RotateDirection.SE)
                angleModifier = 5;

            if (angleModifier != null) {
                for (int dR = 0; dR < 6; dR++) {
                    if (rotations[dR] == RotateDirection.rotate(s.getRotation(), angleModifier)) {
                        coupledWithSides = true;
                        break;
                    }
                }
            }

            if (coupledWithSides) {
                if (!damageHull(ship, rollDice(s, shipOwner)))
                    destroyMast(s, 1);
            }
            // --

            // par. 12.2.3.5.3
            for (Player plr : Player.getValues()) {
                killMarines(s, plr, MarinesCompartment.DECK, rollDice(s, shipOwner), KillingMode.WITH_COMMANDER);
            }
            calculateShipOwner(s);
            // --
        }
    }


    public static int rollDice(Ship ship, Player player) {
        int roll = Dice.roll();

        if (player != ship.getOwner())
            return roll;

        if (ship.getHappiness() == 0)
            return roll;

        if (!MainBoard.rollAgainDialog(roll, player))
            return roll;

        ship.setHappiness(ship.getHappiness() - 1);
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
         * ship.SetHappiness(ship.ShowHappiness()-1); return RollDice(); }
         */
    }


    public static boolean destroyMast(Ship ship, int points) {
        boolean damage = false;
        MainBoard.addMessage(String.format("Ship #%d: speed fell by %d knots\n", ship.getID(),
                Math.min(ship.getMast(), points)));
        if (ship.getMast() > 0)
            damage = true;
        ship.destroyMast(points);

        Ship towed = ship.getTowOther();
        if (towed != null && !checkIfStillTowable(ship, towed))
            throwTow(ship);

        return damage;
    }


    public static boolean destroyHelm(Ship ship, int points) {
        boolean damage = false;
        MainBoard.addMessage("Ship #" + ship.getID() + ": manoeuvrability fell by "
                + Math.min(ship.getHelm(Commons.BOTH), points) + " points\n");
        if (ship.getHelm(Commons.BOTH) > 0)
            damage = true;
        ship.destroyHelm(points);

        Ship towed = ship.getTowOther();
        if (towed != null && !checkIfStillTowable(ship, towed))
            throwTow(ship);

        return damage;
    }


    public static boolean destroyHeaviestCannon(Ship ship, GunCompartment location) {
        if (!destroyCannonIS(ship, location, Gun.HEAVY, MsgMode.ON)) {
            if (!destroyCannonIS(ship, location, Gun.MEDIUM, MsgMode.ON)) {
                if (!destroyCannonIS(ship, location, Gun.LIGHT, MsgMode.ON)) {
                    MainBoard.addMessage("No loss taken.\n");
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean damageHull(Ship ship, int points) {
        boolean isSunk = ship.destroyHull(points);

        if (isSunk) {
            sinkShip(ship, DestroyShipMode.SINK);
            return true;
        }

        MainBoard.addMessage("Ship #" + ship.getID() + ": hull damage (" + points + " pts)\n");

        Ship towed = ship.getTowOther();
        if (towed != null && !checkIfStillTowable(ship, towed))
            throwTow(ship);

        return false;
    }


    public static boolean checkIfStillTowable(Ship tug, Ship towed) {
        /*
         * sprawdzanie spelnienia warunkow holowania w sytuacji, gdy statek jest
         * juz sczepiony holem
         */

        if (tug.getDurability() * 2 < towed.getDurability())
            return false; // par. 16.1
        if (tug.getHelm(Commons.BOTH) == 0 || tug.getMast() == 0)
            return false; // par. 16.2
        if (!checkIfPlayerControlsLocation(tug, game.getCurrentPlayer(), MarinesCompartment.DECK, true)
                || !checkIfPlayerControlsLocation(towed, game.getCurrentPlayer(), MarinesCompartment.DECK, true))
            return false; // par. 12.10, 16.5

        return true;
    }


    public static boolean checkIfTowable(Ship tug, Ship towed) {
        Coordinate tugPos = tug.getPosition();
        Coordinate towedPos = towed.getPosition();

        if (!checkIfStillTowable(tug, towed))
            return false;
        if (tug.getTowedBy() != null || tug.getTowOther() != null || towed.getTowedBy() != null
                || towed.getTowOther() != null)
            return false; // par. 16.3
        if (Math.abs(tugPos.getA() - towedPos.getA()) > 1 || Math.abs(tugPos.getB() - towedPos.getB()) > 1
                || getAlliedMarinesNumber(tug, game.getCurrentPlayer(), MarinesCompartment.DECK) < 2
                || getAlliedMarinesNumber(towed, game.getCurrentPlayer(), MarinesCompartment.DECK) < 2)
            return false; // par. 16.4
        if (game.getWindSpeed() > 8)
            return false; // par. 16.10
        if (tug.isParameter(Parameter.ACTIONS_OVER))
            return false; // par. 16.6

        return true;
    }


    public static boolean checkIfPlayerControlsLocation(Ship ship, Player player, MarinesCompartment location,
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
            if (!game.getPlayer(player).isAlly(plr) && ship.getMarinesNumber(plr, location, Commons.BOTH) > 0)
                return false;
        }

        if (withAllies && getAlliedMarinesNumber(ship, player, location) == 0)
            return false;
        else if (!withAllies && ship.getMarinesNumber(player, location, Commons.BOTH) == 0)
            return false;
        else
            return true;
    }


    public static int getAlliedMarinesNumber(Ship ship, Player player, MarinesCompartment location) {
        /*
         * Funkcja zwraca łączną ilość sojuszniczych marynarzy w przedziale.
         */

        int number = 0;
        for (Player plr : Player.getValues()) {
            if (game.getPlayer(player).isAlly(plr))
                number += ship.getMarinesNumber(plr, location, Commons.BOTH);
        }

        return number;
    }


    public static boolean destroyCannonIS(Ship ship, GunCompartment location, Gun type, MsgMode mode) {
        ship.destroyCannon(location, type, Commons.BOTH);

        if (mode == MsgMode.ON)
            MainBoard.addMessage("Ship #" + ship + ": " + type.toString() + " cannon from " + location.toString()
                    + " compartment lost.\n");

        return true;
    }


    /**
     * 
     * @param shipID
     * @return list of the following elements: winner, players who end game
     */
    public static List<Object> calculateShipOwner(Ship ship) {
        List<Object> result = new ArrayList<Object>();
        Integer winnerId = new Integer(0);
        Vector<Player> playersWhoEndGame = new Vector<Player>();
        result.add(winnerId);
        result.add(playersWhoEndGame);

        if (ship.getOwner() != Player.NONE && ship.getPlayerMarinesOnShip(ship.getOwner(), false) > 0) {
            winnerId = PREVIOUS_OWNER;
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
            marines[plr.ordinal()] = ship.getPlayerMarinesOnShip(plr, true);
            if (marines[plr.ordinal()] > 0)
                playersOnBoard++;
        }

        if (playersOnBoard == 0) {
            if (ship.getOwner() != Player.NONE) {
                game.getPlayer(ship.getOwner()).removeShipFromFleet(ship);
                if (game.getPlayer(ship.getOwner()).getFleet().size() == 0)
                    playersWhoEndGame.add(ship.getOwner());
            }

            ship.setOwner(Player.NONE);
            ship.setParameter(Parameter.IS_WRECK); // par.
                                                   // 15.1
            ship.setHappiness(0); // par. 18.8
            MainBoard.addMessage("Ship #" + ship.getID() + ": is now a wreck\n");

            winnerId = Player.NONE.ordinal();
            return result;
        }

        for (Player plr : Player.values()) {
            if (ship.getPlayerMarinesOnShip(plr, false) > ship.getPlayerMarinesOnShip(winner, false)
                    || ship.getPlayerMarinesOnShip(plr, false) == ship.getPlayerMarinesOnShip(winner, false)
                    && Dice.roll() > 3)
                winner = plr; // par. 5.5.1.2
        }

        if (playersOnBoard == 1 && ship.getPlayerMarinesOnShip(ship.getOwner(), true) == 1 || winner == ship.getOwner()) {
            winnerId = PREVIOUS_OWNER;
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
                        if (ship.getCommanderState(plr, loc) == CommanderState.IMPRISONED)
                            ship.setCommander(plr, loc, CommanderState.READY);
                    } else {
                        if (ship.getCommanderState(plr, loc) != CommanderState.NOT_THERE) {
                            ship.setCommander(plr, loc, CommanderState.NOT_THERE);
                            ship.setCommander(plr, MarinesCompartment.INMOVE, CommanderState.IMPRISONED);
                        }
                    }
                }
            }
            // --
        }
        // --

        // -- par. 18.8
        if (!game.getPlayer(ship.getOwner()).isAlly(winner))
            ship.setHappiness(0);
        // --

        game.getPlayer(ship.getOwner()).removeShipFromFleet(ship);
        if (game.getPlayer(ship.getOwner()).getFleet().size() == 0)
            playersWhoEndGame.add(ship.getOwner());

        ship.setOwner(winner);
        ship.clearParameter(Parameter.IS_WRECK);

        MainBoard.addMessage("Ship #" + ship.getID() + ": new owner is " + winner.toString() + "\n");

        winnerId = winner.ordinal();
        return result;
    }


    public static int killMarines(Ship ship, Player player, MarinesCompartment location, int number, KillingMode mode) {
        /*
         * player = NONE: straty dla kazdego gracza, proporcjonalnie do udzialu
         * jego marynarzy w przedziale, zaokraglane w gore
         */
        int killed = 0;
        boolean commander = false;

        if (number == 0)
            return 0;

        if (player != Player.NONE) {
            Object[] ret = ship.killMarines(player, location, number, mode);
            killed = (Integer) ret[0];
            if (ret[1] == KillingMode.WITH_COMMANDER) {
                if (mode == KillingMode.WITH_COMMANDER)
                    game.endPlayerGame(player);
                commander = true;
            }

            MainBoard.addMessage("Ship #" + ship.getID() + ": " + killed + " marines ");
            if (commander && mode == KillingMode.WITH_COMMANDER)
                MainBoard.addMessage("and commander ");
            MainBoard.addMessage("lost (" + player.toString() + ")\n");

            calculateShipOwner(ship);
        } else {
            Vector<Player> plrs = new Vector<Player>();
            int totalNumber = 0;

            for (Player plr : Player.values()) {
                if (plr == Player.NONE)
                    continue;
                if (ship.getMarinesNumber(plr, location, Commons.BOTH) > 0
                        || ship.getCommanderState(plr, location) != CommanderState.NOT_THERE) {
                    plrs.add(plr);

                    totalNumber += ship.getMarinesNumber(plr, location, Commons.BOTH);
                    if (ship.getCommanderState(plr, location) != CommanderState.NOT_THERE)
                        totalNumber += 1;
                }
            }

            for (int i = 0; i < plrs.size(); i++) {
                int thisNumber = ship.getMarinesNumber(plrs.get(i), location, Commons.BOTH);
                if (ship.getCommanderState(plrs.get(i), location) != CommanderState.NOT_THERE)
                    thisNumber += 1;

                Object[] ret = ship.killMarines(player, location, (int) (Math.ceil(thisNumber / totalNumber)), mode);
                killed = (Integer) ret[0];
                if (ret[1] == KillingMode.WITH_COMMANDER) {
                    if (mode == KillingMode.WITH_COMMANDER)
                        game.getPlayer(Player.valueOf(i)).endGame();
                    commander = true;
                }

                MainBoard.addMessage("Ship #" + ship.getID() + ": " + killed + " marines ");
                if (commander && mode == KillingMode.WITH_COMMANDER)
                    MainBoard.addMessage("and commander ");
                MainBoard.addMessage("lost (" + plrs.get(i).toString() + ")\n");
            }

            calculateShipOwner(ship);
        }

        if (commander)
            return COMMANDER_TO_BE_KILLED;
        else
            return killed;
    }


    public static Range checkAngleToRotate(Ship ship) {
        RotateDirection rotTowedBy = null;
        RotateDirection rotTowOther = null;

        if (ship.getTowedBy() != null)
            rotTowedBy = ship.getTowedBy().getRotation();
        if (ship.getTowOther() != null)
            rotTowedBy = ship.getTowOther().getRotation();

        return ship.checkAngleToRotate(rotTowedBy, rotTowOther);
    }


    private static boolean runDown(Ship aggressor, Ship victim) {
        /*
         * #return: true, gdy okręt taranujący zatonął
         */
        MainBoard.addMessage("Ship #" + aggressor.getID() + ": rammed ship #" + victim.getID() + "\n");

        // par. 11.1
        if (!damageHull(victim, aggressor.getDurability())) {
            destroyMast(victim, 4);
            destroyHelm(victim, 1);
            victim.nextMovementCode(MovementType.END_MOVE); // par. 11.3
        }
        // --
        else
            game.getPlayer(aggressor.getOwner()).addDestroyedShip(); // par.
                                                                     // 3.2

        // par. 11.2
        if (!damageHull(aggressor, victim.getShipClass().getDurabilityMax() / 2)) {
            if (rollDice(aggressor, game.getCurrentPlayer()) == 6)
                destroyHelm(aggressor, 1);

            aggressor.nextMovementCode(MovementType.END_MOVE); // par.
                                                               // 11.3
            return false;
        }
        // --
        else
            return true;
    }


    public static void handle(Ship source, Ship target, Player player, CargoDestination from, CargoDestination to,
            CargoType cargoType, GunCompartment sourceCompartment, GunCompartment targetCompartment, int quantity) {
        Gun gunType = Gun.NONE;

        // par. 14.5
        if (!source.isShipCoupled(target))
            coupleShips(source, target, CoupleReason.HANDLING);
        // --

        if (cargoType == CargoType.SILVER) {
            source.unloadCargo(CargoType.SILVER, quantity);
            target.loadCargo(CargoType.SILVER, quantity);
            source.moveMarines(player, MarinesCompartment.INMOVE, MarinesCompartment.INMOVE, quantity);
            return;
        }

        if (cargoType == CargoType.CANNONS_LIGHT)
            gunType = Gun.LIGHT;
        if (cargoType == CargoType.CANNONS_MEDIUM)
            gunType = Gun.MEDIUM;

        if (from == CargoDestination.CARGO)
            source.unloadCargo(cargoType, quantity);
        if (from == CargoDestination.BATTERIES) {
            for (int i = 0; i < quantity; i++)
                destroyCannonIS(source, sourceCompartment, gunType, MsgMode.OFF);
        }

        if (cargoType == CargoType.CANNONS_LIGHT) {
            source.moveMarines(player, MarinesCompartment.INMOVE, MarinesCompartment.INMOVE, quantity * 10);
            if (to == CargoDestination.CARGO)
                target.loadCargo(CargoType.CANNONS_LIGHT, quantity);
            else
                target.modifyCannonsNumber(targetCompartment, Gun.LIGHT, quantity);
        }
        if (cargoType == CargoType.CANNONS_MEDIUM) {
            source.moveMarines(player, MarinesCompartment.INMOVE, MarinesCompartment.INMOVE, quantity * 20);
            if (to == CargoDestination.CARGO)
                target.loadCargo(CargoType.CANNONS_MEDIUM, quantity);
            else
                target.modifyCannonsNumber(targetCompartment, Gun.MEDIUM, quantity);
        }
    }


    public static boolean checkIfSetExplosivesPossible(Ship ship, Player player) {
        if (ship.isParameter(Parameter.ACTIONS_OVER))
            return false;

        if (ship.isParameter(Parameter.IS_EXPLOSIVE))
            return false;

        if (game.getCurrentPlayer() != ship.getOwner())
            return false;

        // par. 14.9 (okręt musi być pusty)
        if (getCompartmentAllies(ship, player, MarinesCompartment.DECK) != CompartmentAllies.NONE
                || getCompartmentAllies(ship, player, MarinesCompartment.INMOVE) != CompartmentAllies.NONE
                || getCompartmentAllies(ship, player, MarinesCompartment.BATTERIES) != CompartmentAllies.NONE)
            return false;
        // --

        return true;
    }


    public static void endHandling(Ship shipOne, Ship shipTwo) {
        shipOne.uncouple(shipTwo);
        shipTwo.uncouple(shipOne);

        // par. 14.6
        shipOne.nextMovementCode(MovementType.END_MOVE);
        shipTwo.nextMovementCode(MovementType.END_MOVE);
        // --
    }


    public static void tow(Ship tug, Ship towed) {
        MainBoard.addMessage("Ship #" + tug.getID() + ": now towing ship #" + towed.getID() + "\n");

        tug.nextMovementCode(MovementType.END_MOVE);
        towed.nextMovementCode(MovementType.END_MOVE);

        tug.setTowOther(towed);
        towed.setTowedBy(tug);

        tug.setParameter(Parameter.ACTIONS_OVER); // 16.6
        towed.setParameter(Parameter.ACTIONS_OVER); // 16.6
    }


    public static void surrenderMarines(Ship ship, MarinesCompartment location, int number) {
        // par. 12.5
        killMarines(ship, game.getCurrentPlayer(), location, number, KillingMode.WITHOUT_COMMANDER);
        calculateShipOwner(ship);
        // --
    }


    public static void moveMarinesShip(Ship sourceShip, Ship targetShip, Player player, int amount) {
        Boolean boardingFirstTurn = Boolean.FALSE;
        boolean shipAlreadyBoarded = false;

        // par. 12.1
        if (targetShip.getOwner() != Player.NONE
                && !game.getPlayer(game.getCurrentPlayer()).isAlly(targetShip.getOwner())) {
            boardingFirstTurn = Boolean.TRUE;

            if (!sourceShip.isShipCoupled(targetShip))
                coupleShips(sourceShip, targetShip, CoupleReason.BOARDING);

            for (Player p : Player.getValues()) {
                if (!game.getPlayer(targetShip.getOwner()).isAlly(p)
                        && targetShip.getPlayerMarinesOnShip(player, false) > 0)
                    shipAlreadyBoarded = true;
            }

            if (shipAlreadyBoarded)
                boardingFirstTurn = Boolean.FALSE;
        }
        // --
        else {
            if (!sourceShip.isShipCoupled(targetShip))
                coupleShips(sourceShip, targetShip, CoupleReason.HANDLING);
        }

        sourceShip.moveMarines(game.getCurrentPlayer(), MarinesCompartment.DECK, MarinesCompartment.SHIP_X, amount);
        targetShip.moveMarines(game.getCurrentPlayer(), MarinesCompartment.SHIP_X, MarinesCompartment.DECK, amount);
        MainBoard.addMessage("Ship #" + targetShip.getID() + ": " + amount + " marines moved from ship #"
                + sourceShip.getID() + "\n");

        calculateShipOwner(sourceShip);
        calculateShipOwner(targetShip);

        if (boardingFirstTurn == Boolean.TRUE) {
            MainBoard.addMessage("Ship #" + targetShip.getID() + " boarded.\n");
            targetShip.setBoardingFirstTurn(BoardingFirstTurn.YES);
        }
    }


    public static void moveCommanderShip(Ship sourceShip, Ship targetShip) {
        if (targetShip.getOwner() != Player.NONE
                && !game.getPlayer(game.getCurrentPlayer()).isAlly(targetShip.getOwner())) {

            if (!sourceShip.isShipCoupled(targetShip))
                coupleShips(sourceShip, targetShip, CoupleReason.BOARDING);
        } else {
            if (!sourceShip.isShipCoupled(targetShip))
                coupleShips(sourceShip, targetShip, CoupleReason.HANDLING);
        }

        sourceShip.moveCommander(game.getCurrentPlayer(), MarinesCompartment.DECK, MarinesCompartment.SHIP_X);
        targetShip.moveCommander(game.getCurrentPlayer(), MarinesCompartment.SHIP_X, MarinesCompartment.DECK);
        MainBoard.addMessage("Ship #" + targetShip.getID() + ": commander moved from ship #" + sourceShip.getID()
                + "\n");

        calculateShipOwner(sourceShip);
        calculateShipOwner(targetShip);
    }


    public static void boardingEscape(Ship ship) {
        MainBoard.addMessage("Ship #" + ship.getID() + ": did boarding escape\n");

        for (Ship s : ship.getShipsCoupled().keySet()) {
            ship.uncouple(s);
            s.uncouple(ship);
        }

        moveShip(ship, 1); // par. 12.8.3
    }


    public static void closeCombat(Ship ship, Player player, String enemiesStr, MarinesCompartment location) {
        int marinesLoss = rollDice(ship, player) * rollDice(ship, player);

        // par. 12.2.2.3
        if (game.getCurrentPlayer() == ship.getOwner() && ship.isBoardingFirstTurn() == BoardingFirstTurn.YES)
            marinesLoss *= 2;
        // --

        String[] enemiesStrs = enemiesStr.split(" ");

        // par. 12.2.2.4
        for (int i = 0; i < enemiesStrs.length; i++) {
            for (Player plr : Player.values()) {
                if (plr.toString().equals(enemiesStrs[i]))
                    killMarines(ship, plr, location, marinesLoss / enemiesStrs.length, KillingMode.WITHOUT_COMMANDER);
                break;
            }
        }
        // --

        ship.setBoardingActionUsed(game.getCurrentPlayer(), location, 1);
        calculateShipOwner(ship);
    }


    public static void makeShallowEscapeAttempt(Ship ship, ShallowAttempt type, Ship towed) {
        boolean attemptSuccessful = false;
        Player currentPlayer = game.getCurrentPlayer();

        if (ship.isParameter(Parameter.IS_IMMOBILIZED)) {
            ship.makeEscapeAttempt(type); // par. 17.7

            if (type == ShallowAttempt.PULL_ANCHOR || type == ShallowAttempt.TOW_BY_ONE)
                return;

            if (type == ShallowAttempt.DROP_SILVER && rollDice(ship, currentPlayer) > 2)
                attemptSuccessful = true; // par. 17.8.2
            if (type == ShallowAttempt.DROP_CANNONS && rollDice(ship, currentPlayer) > 1)
                attemptSuccessful = true; // par. 17.9.2
            if (type == ShallowAttempt.TOW_BY_BOATS
                    && rollDice(ship, currentPlayer) + rollDice(ship, currentPlayer) > 10)
                attemptSuccessful = true; // 17.12.1

            if (attemptSuccessful) {
                MainBoard.addMessage("Ship #" + ship.getID() + ": escaped from treachous waters\n");
                ship.escapeFromShallow();
                return;
            } else
                MainBoard.addMessage("Ship #" + ship.getID() + ": failed to escape from treachous waters\n");
        } else {
            towed.makeEscapeAttempt(type);
        }
    }


    public static boolean checkIfEscapeAttemptPossible(Ship ship, ShallowAttempt type, Ship towed) {
        if (!ship.isParameter(Parameter.IS_IMMOBILIZED)) {
            // statek taki moze byc tylko holownikiem
            if (type == ShallowAttempt.TOW_BY_ONE) {
                if (!towed.isParameter(Parameter.IS_IMMOBILIZED)
                        || !checkIfTowable(ship, towed)
                        || !(ship.getMovesQueueCode() == MovesQueueCode.NEW || ship.getMovesQueueCode() == MovesQueueCode.ROTATE))
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
                return ship.checkEscapeAttempt(type);
        }
    }


    public static int checkMaxQuantityToHandle(Ship source, Ship target, CargoDestination from, CargoDestination to,
            CargoType cargoType, GunCompartment sourceCompartment, GunCompartment targetCompartment) {
        int max = 0;
        Gun gunType = Gun.NONE;
        Player currentPlayer = game.getCurrentPlayer();

        if (cargoType == CargoType.CANNONS_LIGHT)
            gunType = Gun.LIGHT;
        if (cargoType == CargoType.CANNONS_MEDIUM)
            gunType = Gun.MEDIUM;

        if (cargoType == CargoType.SILVER) {
            max = source.getLoad(CargoType.SILVER);
            max = Math.min(max, source.getMarinesNumber(currentPlayer, MarinesCompartment.INMOVE, Commons.READY));
            max = Math.min(max, target.getLoad(CargoType.FREE_SPACE));
            max = max - (max % 10); // 10 marynarzy - 10t srebra (mozna
                                    // przenoscic tylko pelne 10t)
            return max;
        }

        if (from == CargoDestination.CARGO)
            max = source.getLoad(cargoType);
        else
            max = source.getCannonsNumber(sourceCompartment, gunType, Commons.BOTH);

        if (cargoType == CargoType.CANNONS_LIGHT) {
            max = Math.min(max, source.getMarinesNumber(currentPlayer, MarinesCompartment.INMOVE, Commons.READY) / 10);
            if (to == CargoDestination.CARGO)
                max = Math.min(max, target.getLoad(CargoType.FREE_SPACE)); // dzialo
                                                                           // lekkie
                                                                           // wazy
                                                                           // 1t
            else {
                // par. 14.3
                int can = target.getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.LIGHT.ordinal()]
                        + target.getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.MEDIUM.ordinal()]
                        + target.getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.HEAVY.ordinal()]
                        - target.getCannonsNumber(targetCompartment, Gun.LIGHT, Commons.BOTH)
                        - target.getCannonsNumber(targetCompartment, Gun.MEDIUM, Commons.BOTH)
                        - target.getCannonsNumber(targetCompartment, Gun.HEAVY, Commons.BOTH);
                max = Math.min(max, can);
                // --
            }
        }
        if (cargoType == CargoType.CANNONS_MEDIUM) {
            max = Math.min(max, source.getMarinesNumber(currentPlayer, MarinesCompartment.INMOVE, Commons.READY) / 20);
            if (to == CargoDestination.CARGO)
                max = Math.min(max, target.getLoad(CargoType.FREE_SPACE) / 2); // dzialo
                                                                               // srednie
                                                                               // wazy
                                                                               // 2t
            else {
                // par. 14.3
                int can = target.getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.MEDIUM.ordinal()]
                        + target.getShipClass().getCannonMax()[targetCompartment.ordinal()][Gun.HEAVY.ordinal()]
                        - target.getCannonsNumber(targetCompartment, Gun.MEDIUM, Commons.BOTH)
                        - target.getCannonsNumber(targetCompartment, Gun.HEAVY, Commons.BOTH);
                max = Math.min(max, can);
                // --
            }
        }

        return max;
    }


    public static boolean checkIfHandleUncouplePossible(Ship one, Ship two) {
        if (!one.isShipCoupled(two))
            return false;

        if (one.getCoupleReason(two) != CoupleReason.HANDLING)
            return false;

        if (one.isParameter(Parameter.ACTIONS_OVER))
            return false;

        return true;
    }


    public static boolean checkIfSabotageSuccessful(Ship ship, Player player, MarinesCompartment location) {
        int enemyMarinesNumber = 0;
        for (Player plr : Player.values()) {
            if (plr == Player.NONE)
                continue;
            if (!game.getPlayer(player).isAlly(plr))
                enemyMarinesNumber += ship.getMarinesNumber(plr, location, Commons.BOTH);
        }

        // par. 12.2.3.1
        if (enemyMarinesNumber == 0
                && rollDice(ship, player) + rollDice(ship, player) < ship.getMarinesNumber(player, location,
                        Commons.BOTH))
            return true;
        // --
        // par. 12.2.3.2
        if (enemyMarinesNumber > 0
                && rollDice(ship, player) + rollDice(ship, player) + rollDice(ship, player) < ship.getMarinesNumber(
                        player, location, Commons.BOTH) - enemyMarinesNumber)
            return true;
        // --

        return false;
    }


    private static CompartmentAllies getCompartmentAllies(Ship ship, Player player, MarinesCompartment location) {
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
            if (!game.getPlayer(player).isAlly(plr) && ship.getMarinesNumber(plr, location, Commons.BOTH) > 0)
                return CompartmentAllies.ENEMY_PRESENCE;
            if (game.getPlayer(player).isAlly(plr) && ship.getMarinesNumber(plr, location, Commons.BOTH) > 0)
                ally = true;
        }

        if (ally)
            return CompartmentAllies.ALLIES_ONLY;
        else
            return CompartmentAllies.NONE;
    }


    public static void moveCommander(Ship ship, MarinesCompartment source, MarinesCompartment destination) {
        ship.moveCommander(game.getCurrentPlayer(), source, destination);
        MainBoard.addMessage("Ship #" + ship.getID() + ": commander moved from " + source.toString() + " to "
                + destination.toString() + "\n");
    }


    static void storm(Ship ship) {
        if (!ship.isOnGameBoard())
            return;

        // Storm has an effect on a ship only if 5 or 6 was rolled [9.5]
        if (rollDice(ship, ship.getOwner()) < 5)
            return;

        int eventType = rollDice(ship, ship.getOwner()) + rollDice(ship, ship.getOwner())
                + rollDice(ship, ship.getOwner());
        Player shipOwner = ship.getOwner();

        MainBoard.addMessage("Ship #" + ship + ": storm event #" + eventType + "happen\n");

        if (eventType == 3) {
            if (game.getWindSpeed() >= 10)
                ship.ripSailes();
            return;
        }

        if (eventType == 4) {
            if (destroyHeaviestCannon(ship, GunCompartment.SIDE_R))
                damageHull(ship, 2);
            return;
        }

        if (eventType == 5) {
            if (destroyHeaviestCannon(ship, GunCompartment.SIDE_L)) {
                killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES, 4, KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(ship);
            }
            return;
        }

        if (eventType == 6) {
            while (destroyCannonIS(ship, GunCompartment.BOW, Gun.LIGHT, MsgMode.ON)) {
                killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES, Gun.LIGHT.getCrewSize(),
                        KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(ship);
            }

            while (destroyCannonIS(ship, GunCompartment.BOW, Gun.MEDIUM, MsgMode.ON)) {
                killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES, Gun.MEDIUM.getCrewSize(),
                        KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(ship);
            }
            return;
        }

        if (eventType == 7) {
            destroyMast(ship, 5);
            return;
        }

        if (eventType == 8) {
            int cannons = ship.getCannonsNumber(GunCompartment.STERN, Gun.LIGHT, Commons.BOTH);
            killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES,
                    Gun.LIGHT.getCrewSize() * Math.min(cannons, 1), KillingMode.WITHOUT_COMMANDER);
            calculateShipOwner(ship);

            while (destroyCannonIS(ship, GunCompartment.BOW, Gun.LIGHT, MsgMode.ON))
                continue;

            return;
        }

        if (eventType == 9) {
            damageHull(ship, rollDice(ship, ship.getOwner()));
            return;
        }

        if (eventType == 10) {
            ship.modifyHappiness(1);
            return;
        }

        if (eventType == 11) {
            killMarines(ship, Player.NONE, MarinesCompartment.DECK, 1, KillingMode.WITH_COMMANDER);
            calculateShipOwner(ship);

            return;
        }

        if (eventType == 12) {
            if (destroyHelm(ship, 1))
                ship.modifyHappiness(-1);
            return;
        }

        if (eventType == 13) {
            if (destroyHelm(ship, ship.getShipClass().getHelmMax())) {
                ship.modifyHappiness(-ship.getHappiness());
            }
            return;
        }

        if (eventType == 14) {
            if (destroyHeaviestCannon(ship, GunCompartment.SIDE_L)) {
                killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES, 4, KillingMode.WITHOUT_COMMANDER);
                calculateShipOwner(ship);
            }
            return;

        }

        if (eventType == 15) {
            if (destroyHeaviestCannon(ship, GunCompartment.SIDE_R))
                damageHull(ship, 2);
            return;
        }

        if (eventType == 16) {
            killMarines(ship, Player.NONE, MarinesCompartment.DECK, rollDice(ship, shipOwner),
                    KillingMode.WITH_COMMANDER);
            calculateShipOwner(ship);
            return;
        }

        if (eventType == 17) {
            killMarines(ship, Player.NONE, MarinesCompartment.DECK, 10, KillingMode.WITH_COMMANDER);
            calculateShipOwner(ship);
            return;
        }

        if (eventType == 18) {
            sinkShip(ship, DestroyShipMode.SINK);
            return;
        }
    }


    public static boolean checkIfShipBoarded(Ship ship, Player player) {
        for (MarinesCompartment comp : MarinesCompartment.values()) {
            if (comp == MarinesCompartment.SHIP_X || comp == MarinesCompartment.NONE)
                continue;

            if ((ship.getMarinesNumber(player, comp, Commons.BOTH) > 0
                    || ship.getCommanderState(player, comp) == CommanderState.READY || ship.getCommanderState(player,
                    comp) == CommanderState.USED)
                    && DataExtractors.getEnemyGroups(ship, game.getPlayer(player), comp).length() > 0)
                return true;
        }

        return false;
    }


    static void moveShip(Ship ship, int distance) {
        Player currentPlayer = game.getCurrentPlayer();
        Coordinate shift = new Coordinate(0, 0); // przesuniecie [A,B] (w
                                                 // przypadku udanego ruchu)

        Coordinate position = ship.getPosition();
        RotateDirection rotation = ship.getRotation();

        Coordinate towedNewCrd = Coordinate.dummy;

        boolean tugChangedOwner = false;
        boolean towedChangedOwner = false;
        boolean rammed = false;
        boolean crashed = false;
        boolean ranAground = false;

        Ship shipTowedBy = ship.getTowOther();

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
            MainBoard.addMessage("Ship #" + ship.getID() + " escaped.\n");

            // par. 5.4.1, 5.4.2
            ship.setPosition(Coordinate.dummy);
            // --

            if (shipTowedBy != null) {
                /*
                 * Sytuacja, gdy holownik stoi na własnym polu, tuż przy
                 * krawędzi planszy, a okręt holowany nie znajduje się na polu
                 * tego gracza (wtedy funkcja checkDistanceToMove() nie wychwyci
                 * tej sytuacji).
                 */
                if (shipTowedBy.getOwner() != ship.getOwner())
                    captureShip(shipTowedBy, game.getPlayer(ship.getOwner()));
                else {
                    // Zakładam, że okręt holowany wypływa poza mapę wraz z
                    // holownikiem.
                    MainBoard.addMessage("Ship #" + shipTowedBy.getID() + " escaped.\n");
                    shipTowedBy.setPosition(Coordinate.dummy);
                    throwTow(ship);
                }
            }

            MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
            return;
        }

        Player owner = ship.getOwner();
        ObstacleReport report = game.getBoard().isObstacleOnPath(position, rotation, distance, owner,
                game.getPlayer(owner).getAllies());

        if (!game.getPlayer(ship.getOwner()).isAlly(report.hexOwner)) {
            // par. 5.5.2
            if (checkIfPlayerControlsLocation(ship, report.hexOwner, MarinesCompartment.DECK, true)) {
                captureShip(ship, game.getPlayer(report.hexOwner));
                ship.setPosition(position.sum(shift));
            }
            // --
            // par. 5.6.1
            else
                internShip(ship, report.hexOwner);
            // --

            tugChangedOwner = true;
        }

        /**
         * funkcja isObstacle.. sprawdza przeszkody na trasie holownik->dystans
         * powinno byc sprawdzanie najpierw dla okretu holowanego, potem dla
         */

        if (report.hexTerrainType == Terrain.SHALLOW) {
            // par. 17.1
            if (ship.getLoad(CargoType.SILVER) == 0 && rollDice(ship, currentPlayer) > 4
                    || ship.getLoad(CargoType.SILVER) != 0 && rollDice(ship, currentPlayer) > 2) {

                MainBoard.addMessage("Ship #" + ship.getID() + " ran aground.\n");
                ship.setParameter(Parameter.IS_IMMOBILIZED);
                ship.nextMovementCode(MovementType.END_MOVE);
                ship.modifyHappiness(-1); // par. 17.3

                ranAground = true;
            }
            // --
        }

        if (report.hexTerrainType == Terrain.ISLAND) {
            crashed = true;
            sinkShip(ship, DestroyShipMode.SINK);
        }

        if (report.hexShip != null) {
            /*
             * Modyfikacja wynikajaca z natury zdarzenia (zaznaczamy, ze chcemy
             * staranowac przeciwnika poprzez -umowny- ruch na pole zajmowane
             * przez wrogi okręt, a faktycznie zatrzymujemy się pole przed nim.
             */

            switch (rotation) {
            case N:
                ship.setPosition(position.getA(), position.getB() + distance - 1);
                break;
            case NE:
                ship.setPosition(position.getA() + distance - 1, position.getB() + distance - 1);
                break;
            case SE:
                ship.setPosition(position.getA() + distance - 1, position.getB());
                break;
            case S:
                ship.setPosition(position.getA(), position.getB() - distance + 1);
                break;
            case SW:
                ship.setPosition(position.getA() - distance + 1, position.getB() - distance + 1);
                break;
            case NW:
                ship.setPosition(position.getA() - distance + 1, position.getB());
                break;
            }

            runDown(ship, report.hexShip);
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
            if (hexOwner != Player.NONE && hexOwner != shipTowedBy.getOwner()) {
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
                if (shipTowedBy.getLoad(CargoType.SILVER) == 0 && rollDice(ship, currentPlayer) > 4
                        || shipTowedBy.getLoad(CargoType.SILVER) != 0 && rollDice(ship, currentPlayer) > 2) {
                    MainBoard.addMessage("Ship #" + shipTowedBy.getID() + " ran aground.\n");
                    shipTowedBy.setParameter(Parameter.IS_IMMOBILIZED);
                    shipTowedBy.modifyHappiness(-1); // par. 17.3
                    ranAground = true;
                }
                // --
            }
        }

        if (!tugChangedOwner && !crashed && !rammed) {
            // Z holownikiem nic się nie stało.
            ship.setPosition(position.sum(shift));
            ship.nextMovementCode(MovementType.TRANSFER);
            ship.setDistanceMoved(ship.getDistanceMoved() + distance);
        }

        if (shipTowedBy != null && !towedChangedOwner) {
            // par. 16.9
            shipTowedBy.setPosition(towedNewCrd);
            shipTowedBy.setRotation(ship.getRotation());
            // --
            shipTowedBy.nextMovementCode(MovementType.TRANSFER);

        }

        if (!tugChangedOwner && !crashed && !towedChangedOwner && !ranAground && !rammed)
            MainBoard.addMessage("Ship #" + ship.getID() + ": succesfully moved.\n");
        else
            MainBoard.addMessage("Ship #" + ship.getID() + ": movement problem occured (new position is ["
                    + (ship.getPosition().getA() + 1) + "," + (ship.getPosition().getB() + 1) + "])\n");
    }


    private static void captureShip(Ship ship, PlayerClass newOwner) {
        MainBoard.addMessage("Ship #" + ship.getID() + ": captured by " + newOwner.toString() + "\n");

        ship.setOwner(newOwner.getIdentity());

        Ship towedBy = ship.getTowedBy();
        // par. 18.3 (zmienna pomocnicza)
        if (towedBy != null)
            towedBy.setHappinessFlag(Happiness.CAPTURE);
        // --

        for (Player plr : Player.values()) {
            if (plr == Player.NONE || plr == newOwner.getIdentity())
                continue;

            for (MarinesCompartment location : MarinesCompartment.values()) {
                if (location == MarinesCompartment.SHIP_X)
                    continue;

                newOwner.addMarinesInterned(plr, ship.getMarinesNumber(plr, location, Commons.BOTH));
                killMarines(ship, plr, location, ship.getMarinesNumber(plr, location, Commons.BOTH),
                        KillingMode.WITHOUT_COMMANDER);

                if (ship.getCommanderState(plr, location) != CommanderState.NOT_THERE) {
                    if (newOwner.isAlly(plr) && ship.getCommanderState(plr, location) == CommanderState.IMPRISONED) {
                        ship.setCommander(plr, location, CommanderState.READY);
                    }
                    if (!newOwner.isAlly(plr)) {
                        Coordinate pos = ship.getPosition();
                        if (game.getBoard().getHex(pos).owner == newOwner.getIdentity()) {
                            newOwner.addCommandersInterned(plr);
                            ship.setCommander(plr, location, CommanderState.NOT_THERE);
                        } else {
                            ship.setCommander(plr, location, CommanderState.NOT_THERE);
                            ship.setCommander(plr, MarinesCompartment.INMOVE, CommanderState.IMPRISONED);
                        }
                    }
                }
            }
        }
    }


    private static void internShip(Ship ship, Player newOwner) {
        MainBoard.addMessage("Ship #" + ship.getID() + ": interneed by " + newOwner.toString() + "\n");

        ship.setPosition(Coordinate.dummy);
        throwTow(ship);

        /*
         * Póki co nie ma możliwości zachowania neutralności. Są tylko
         * sojusznicy i wrogowie. // par. 5.6.4 Player shipOwner =
         * ship.getOwner(); if (!((shipOwner == Player.PASADENA || shipOwner ==
         * Player.SIDONIA) && (newOwner == Player.PASADENA || newOwner ==
         * Player.SIDONIA)) ... // --
         */

        game.getPlayer(ship.getOwner()).removeShipFromFleet(ship);
        ship.setInternedBy(newOwner);
        game.getPlayer(newOwner).addInternedShip(ship.getOwner(), ship);
        game.getPlayer(ship.getOwner()).addInternedShip(ship.getOwner(), ship);
    }


    public static MovementEvent forcedShipMovement(Ship ship, ShipMovementMode mode)
    /*
     * Funkcja obsługuje wymuszyny ruch okrętu (-zawsze- o jedno pole z
     * wiatrem).
     */
    {
        Coordinate shift = new Coordinate(0, 0); // przesuniecie (w przypadku
                                                 // udanego ruchu)
        Coordinate position = ship.getPosition();
        RotateDirection rotation = ship.getRotation();

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
        if (mode == ShipMovementMode.MOVE_WRECK_NORMAL && ship.isParameter(Parameter.IS_WRECK)
                && ship.isParameter(Parameter.IS_IMMOBILIZED))
            return MovementEvent.NONE;
        // --

        if (!Board.isOnMap(position.sum(shift))) {
            ship.setPosition(Coordinate.dummy);
            MainBoard.setSelectedShip(null, Tabs.MOVEMENT);

            // par. 15.4
            if (ship.isParameter(Parameter.IS_WRECK)) {
                sinkShip(ship, DestroyShipMode.SINK);
                return MovementEvent.SUNK;
            }
            // --
            else {
                MainBoard.addMessage("Ship #" + ship.getID() + ": escaped.\n");
                return MovementEvent.ESCAPED;
            }
        }

        Player owner = ship.getOwner();
        ObstacleReport report = game.getBoard().isObstacleOnPath(position, rotation, 1, owner,
                game.getPlayer(owner).getAllies());

        if (report.hexOwner != Player.NONE) {
            if (ship.getMarinesNumber(report.hexOwner, MarinesCompartment.DECK, Commons.BOTH) > 0
                    || ship.getOwner() == Player.NONE) {
                captureShip(ship, game.getPlayer(report.hexOwner));
                return MovementEvent.CAPTURED;
            } else {
                internShip(ship, report.hexOwner);
                return MovementEvent.INTERNED;
            }
        }

        if (report.hexTerrainType == Terrain.SHALLOW) {
            // par. 15.5, 17.1
            if (ship.getLoad(CargoType.SILVER) == 0 && rollDice(ship, ship.getOwner()) > 4
                    || ship.getLoad(CargoType.SILVER) != 0 && rollDice(ship, ship.getOwner()) > 2
                    || ship.isParameter(Parameter.IS_WRECK)) {
                MainBoard.addMessage("Ship #" + ship.getID() + ": ran aground.\n");
                ship.setParameter(Parameter.IS_IMMOBILIZED);
                ship.modifyHappiness(-1); // par. 17.3
            }
        } else if (report.hexTerrainType == Terrain.ISLAND) {
            sinkShip(ship, DestroyShipMode.SINK);
            return MovementEvent.SUNK;
        } else {
            // par. 17.14
            if (ship.isParameter(Parameter.IS_IMMOBILIZED))
                ship.escapeFromShallow();
            // --
        }

        if (report.hexShip != null) {
            MainBoard.addMessage("Ship #" + report.hexShip.getID() + ": rammed by ship" + ship.getID() + ".\n");

            if (runDown(ship, report.hexShip))
                return MovementEvent.SUNK;
            else
                return MovementEvent.NONE;
        }

        ship.setPosition(position.sum(shift));
        return MovementEvent.NONE;
    }


    public static void rotateShip(Ship ship, int _angle) {
        ship.setRotation(RotateDirection.valueOf((ship.getRotation().ordinal() + _angle + 6) % 6));

        if (game.getStage() != Stage.DEPLOYMENT)
            ship.useHelm(_angle);

        if (ship.getTowOther() != null)
            ship.useHelm(ship.getHelm(Commons.READY)); // 16.8

        ship.nextMovementCode(MovementType.ROTATE);
    }


    public static void shoot(Ship ship, Ship targetShip, int distance, GunCompartment compartment, Gun ownGunType,
            AimPart aimedPart, Gun aimedGunType) {
        int accuracyBonus = 0; // bonus +1 do celnosci, gdy abordaz pod pokladem
                               // lub sczepienie

        Player currentPlayer = game.getCurrentPlayer();

        if (targetShip.getShipsCoupled().size() > 0)
            accuracyBonus = 1;

        // par. 12.7
        int groupNumber = DataExtractors.getCompartmentGroups(ship, MarinesCompartment.BATTERIES).split("#").length;
        if (groupNumber > 0)
            accuracyBonus = 1;
        // --

        ship.shoot(currentPlayer, compartment, ownGunType);

        // par. 10.4
        if (rollDice(ship, currentPlayer) + accuracyBonus <= distance + 2) {
            MainBoard.addMessage("Ship #" + ship.getID() + ": shot missed the ship.\n");
            return;
        }
        // --
        else
            MainBoard.addMessage("Ship #" + ship.getID() + ": shot hit the ship.\n");

        // par. 3.2 (operacja pomocnicza)
        if (damageHull(targetShip, ownGunType.getShotDamage())) {
            game.getPlayer(currentPlayer).addDestroyedShip();
            return;
        }
        // --

        if (aimedPart == AimPart.RIGGING) {
            if (rollDice(ship, currentPlayer) >= 6 - ownGunType.ordinal()) {
                MainBoard.addMessage("Ship #" + ship.getID() + ": shot hit the part.\n");
                destroyMast(targetShip, 1);
            } else
                MainBoard.addMessage("Ship #" + ship.getID() + ": shot missed the part.\n");

            return;
        }

        /*
         * W poniższych wywołaniach funkcji killMarines() przekazujemy player =
         * Player.NONE, gdyż zgodnie z par. 12.7 straty są dzielone między
         * wszystkich graczy.
         */

        if (aimedPart == AimPart.CANNON) {
            if (rollDice(ship, currentPlayer) >= 5) {
                MainBoard.addMessage("Ship #" + ship.getID() + ": shot hit the part.\n");
                if (destroyCannonIS(targetShip, calculateCompartmentToAim(ship, targetShip), aimedGunType, MsgMode.ON)) {
                    killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES, aimedGunType.getCrewSize(),
                            KillingMode.WITHOUT_COMMANDER);
                    calculateShipOwner(ship);
                }
            } else
                MainBoard.addMessage("Shot missed the target.\n");

            return;
        }

        if (aimedPart == AimPart.HULL) {
            int roll = rollDice(ship, currentPlayer);
            GunCompartment comp = calculateCompartmentToAim(ship, targetShip);
            int marinesToKill = 0;

            MainBoard.addMessage("Hull damage code: " + roll + "\n");

            switch (roll) {
            case 1:
                marinesToKill = Math.min(1, targetShip.getCannonsNumber(comp, Gun.HEAVY, Commons.BOTH))
                        * Gun.HEAVY.getCrewSize();
                break;
            case 2:
                marinesToKill = Math.min(2, targetShip.getCannonsNumber(comp, Gun.LIGHT, Commons.BOTH))
                        * Gun.LIGHT.getCrewSize();
                break;
            case 3:
                marinesToKill = Math.min(1, targetShip.getCannonsNumber(comp, Gun.MEDIUM, Commons.BOTH))
                        * Gun.MEDIUM.getCrewSize();
                break;
            case 4:
                marinesToKill = Math.min(1, targetShip.getCannonsNumber(comp, Gun.LIGHT, Commons.BOTH))
                        * Gun.LIGHT.getCrewSize();
                break;
            case 5:
                marinesToKill = Math.min(2, targetShip.getCannonsNumber(comp, Gun.MEDIUM, Commons.BOTH))
                        * Gun.MEDIUM.getCrewSize();
                break;
            case 6:
                marinesToKill = 0;
                break;
            }

            killMarines(ship, Player.NONE, MarinesCompartment.BATTERIES, marinesToKill, KillingMode.WITHOUT_COMMANDER);
            calculateShipOwner(ship);
        }
    }


    /**
     * <p>
     * The maximal distance is calculated in such a way that it is not possible
     * to eg. hit a rock within one "move" operation. An additional
     * "confirmation" is required.
     * 
     * 
     * @param ship
     * @return number of hexes a ship can conver (taking various obstacles into
     *         account)
     */
    public static int getDistanceToMove(Ship ship) {
        Player currentPlayer = game.getCurrentPlayer();

        int maxDistance = 0;

        ObstacleReport report = new ObstacleReport();

        // Owner of the previously checked hex.
        Player previousHexOwner = Player.NONE;
        // Terrain type of the previously checked hex.
        Terrain previousHexTerrain = Terrain.WATER;

        Coordinate unitShift;
        Coordinate newPosition = Coordinate.dummy;
        Coordinate position = ship.getPosition();
        RotateDirection rotation = ship.getRotation();

        // It is not possible to sail against the wind. [8.4]
        if (Math.abs(game.getWindDirection().ordinal() - rotation.ordinal()) == 3)
            return 0;

        // It is not possible to interleave movement phase with rotating a ship
        // [8.5]
        if (!ship.isMovementPossible(MovementType.TRANSFER))
            return 0;

        // A ship which ran aground cannot make any movement [17.2].
        if (ship.isParameter(Parameter.IS_IMMOBILIZED))
            return 0;

        if (ship.isParameter(Parameter.IS_WRECK) || ship.getTowedBy() != null)
            return 0;

        // Coupled ships cannot make any move [12.3].
        if (ship.getShipsCoupled().size() > 0)
            return 0;

        // Each "knot" requires two marines as a crew [2.5].
        int currentSpeed = Math.min(ship.getMast(),
                getAlliedMarinesNumber(ship, currentPlayer, MarinesCompartment.DECK) / 2);

        // Strength of the wind is summed with the current ship's speed [8.1].
        int maxDist_ = currentSpeed + game.getWindSpeed();

        // Result of the distance equation is rounded down [8.2].
        int delta = Math.abs(game.getWindDirection().ordinal() - rotation.ordinal());
        if (delta < 3)
            maxDist_ /= WIND_FACTOR.get(delta);
        if (delta > 3)
            maxDist_ /= WIND_FACTOR.get(6 - delta);

        // During towing the speed of a unit amounts always one hex less than
        // the speed of a tug [16.7].
        if (ship.getTowOther() != null)
            maxDist_ -= 1;

        if (maxDist_ <= 0)
            return 0;

        Ship towedShip = ship.getTowOther();
        if (towedShip != null) {
            // It is impossible to tow with/against the wind [16.12].
            if (game.getWindDirection().ordinal() - rotation.ordinal() == 0)
                return 0;

            if (towedShip.isParameter(Parameter.IS_IMMOBILIZED))
                return 0;

            // During towing both decks must be controlled by allied players
            // [16.5].
            if (!checkIfPlayerControlsLocation(towedShip, currentPlayer, MarinesCompartment.DECK, true))
                return 0;

            // XXX: o co chodzi?
            if (!checkIfStillTowable(ship, towedShip))
                return 0;
        }

        previousHexOwner = game.getBoard().getHex(position).owner;
        previousHexTerrain = game.getBoard().getHex(position).terrain;

        switch (rotation) {
        case N:
            unitShift = new Coordinate(0, 1);
            break;
        case NE:
            unitShift = new Coordinate(1, 1);
            break;
        case SE:
            unitShift = new Coordinate(1, 0);
            break;
        case S:
            unitShift = new Coordinate(0, -1);
            break;
        case SW:
            unitShift = new Coordinate(-1, -1);
            break;
        case NW:
            unitShift = new Coordinate(-1, 0);
            break;
        default:
            throw new IllegalArgumentException();
        }

        for (maxDistance = 1; maxDistance <= maxDist_; maxDistance++) {
            newPosition = position.sum(unitShift.mul(maxDistance));

            // It is possible to escape from map only within a basin controlled
            // by the player [5.4.1].
            if (!Board.isOnMap(newPosition)) {
                if (previousHexOwner != currentPlayer)
                    maxDistance--;
                break;
            }

            // If a ship is a tug it is necessary to throw tow if a tug is
            // captured or interned and to check if a towed ship did not run
            // aground (if a previous hex is shallow water).
            if (towedShip != null) {
                if (!game.getPlayer(previousHexOwner).isAlly(towedShip.getOwner()))
                    break;
                if (previousHexTerrain == Terrain.SHALLOW)
                    break;
            }

            Player owner = ship.getOwner();
            report = game.getBoard().isObstacleOnPath(position, rotation, maxDistance, owner,
                    game.getPlayer(owner).getAllies());

            // Require confirmation for ramming, shallow water access etc.
            if (report.hexOwner != Player.NONE || report.hexTerrainType == Terrain.SHALLOW
                    || report.hexTerrainType == Terrain.ISLAND || report.hexShip != null)
                break;

            previousHexOwner = game.getBoard().getHex(newPosition).owner;
            previousHexTerrain = game.getBoard().getHex(newPosition).terrain;
        }

        maxDistance = Math.min(maxDistance, maxDist_);

        // A flag ship cannot escape the board unless it is the last ship on the
        // board [5.3.2]. It is also not possible to intentionally hit rocks or
        // ram another ship (if it would result in the flag ship sink).
        if (ship.isCommanderOnboard(currentPlayer) && game.getPlayerFleetSize(currentPlayer) > 1) {
            if (report.hexTerrainType == Terrain.ISLAND || !Board.isOnMap(newPosition))
                return Math.min(0, maxDistance - 1);

            if (report.hexShip != null && ship.getDurability() <= report.hexShip.getShipClass().getDurabilityMax() / 2)
                return Math.min(0, maxDistance - 1);
        }

        return maxDistance;
    }


    public static boolean checkIfBoardable(Ship source, Player player, Ship target) {
        /*
         * Uwaga: abordaż okrętu sojuszniczego to nic innego jak przeładunek.
         */

        Coordinate sourcePos = source.getPosition();
        Coordinate targetPos = target.getPosition();

        if (Math.abs(sourcePos.getA() - targetPos.getA()) > 1 || Math.abs(sourcePos.getB() - targetPos.getB()) > 1)
            return false; // 12.1

        if ((source.getTowOther() != null || source.getTowedBy() != null)
                && !game.getPlayer(source.getOwner()).isAlly(target.getOwner()))
            return false; // par. 16.14

        return true;
    }


    /**
     * <p>
     * Handling is possible only between allied ships [14.1]. Both ships must
     * stand on adjacent hexes [14.2].
     * 
     * @param source
     * @param player
     * @param target
     * @return
     */
    public static HandlingPartner checkIfHandleable(Ship source, Player player, Ship target) {
        // par. 14.1
        if (!game.getPlayer(player).isAlly(target.getOwner()))
            return HandlingPartner.NONE;

        // par. 14.2
        if (source.getPosition().dist(target.getPosition()) > 1)
            return HandlingPartner.NONE;

        if (source.getOwner() == target.getOwner() || target.getOwner() == Player.NONE)
            return HandlingPartner.OWN;
        else
            return HandlingPartner.ALLY;
    }


    public static boolean checkIfBoardingEscapePossible(Ship ship, Player player) {
        Player currentPlayer = game.getCurrentPlayer();

        if (game.getStage() != Stage.BOARDING_ACTIONS)
            return false;

        if (ship.getBoardingActionUsed(player, MarinesCompartment.DECK) > 0)
            return false;

        // par. 12.8.1
        if (!checkIfPlayerControlsLocation(ship, currentPlayer, MarinesCompartment.DECK, false))
            return false;
        // --

        // par. 12.8.2
        if (Math.abs(game.getWindDirection().ordinal() - ship.getRotation().ordinal()) == 3)
            return false;

        if (ship.isParameter(Parameter.IS_IMMOBILIZED) || ship.isParameter(Parameter.IS_WRECK) || ship.getMast() == 0
                || ship.getTowedBy() != null)
            return false;

        Ship towed = ship.getTowOther();
        if (towed != null) {
            if (game.getWindDirection().ordinal() - ship.getRotation().ordinal() == 0)
                return false; // par. 16.12

            if (towed.isParameter(Parameter.IS_IMMOBILIZED))
                return false;

            if (!checkIfPlayerControlsLocation(towed, currentPlayer, MarinesCompartment.DECK, true))
                return false; // par. 16.5
        }

        Coordinate shift = new Coordinate(0, 0);

        Coordinate position = ship.getPosition();
        RotateDirection rotation = ship.getRotation();

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
            if (game.getBoard().getHex(position).owner != currentPlayer || ship.isCommanderOnboard(currentPlayer)
                    && game.getPlayer(currentPlayer).getFleet().size() > 1)
                return false; // par. 5.4.1
        }

        Player owner = ship.getOwner();
        ObstacleReport report = game.getBoard().isObstacleOnPath(position, rotation, 1, owner,
                game.getPlayer(owner).getAllies());

        if (report.hexTerrainType == Terrain.ISLAND)
            return false;
        if (report.hexShip != null)
            return false;
        // --

        /*
         * Aby móc wyjść z abordażu, musimy mieć faktycznie do czynienia ze
         * sczepieniem w wyniku abordażu. Dodatkowo, okręt chcący uciec nie może
         * być sczepiony z jakimkolwiek innym okrętem na drodze przeładunku.
         */
        if (ship.getShipsCoupled().values().contains(CoupleReason.HANDLING))
            return false;

        if (ship.getShipsCoupled().isEmpty())
            return false;

        return true;
    }


    public static int getShipDistanceToMove(Ship ship) {
        return Math.max(0, getDistanceToMove(ship) - ship.getDistanceMoved());
    }


    public static void deployCommander(Ship ship) {
        Player currentPlayer = game.getCurrentPlayer();

        for (Ship s : game.getPlayer(currentPlayer).getFleet()) {
            if (s.getCommanderState(currentPlayer, MarinesCompartment.DECK) == CommanderState.READY) {
                s.setCommander(currentPlayer, MarinesCompartment.DECK, CommanderState.NOT_THERE);
                break;
            }
        }

        ship.setCommander(currentPlayer, MarinesCompartment.DECK, CommanderState.READY);
    }


    public static GunCompartment calculateCompartmentToAim(Ship source, Ship target) {
        /** okret target strzela do source (?) */

        /*
         * Na podstawie kata miedzy wektorem B+, a wektorem celu (oraz obrotu
         * okretu-celu) oblicza, w jaki przedzial dzialowy trafiamy
         */
        Coordinate crd = source.getPosition().diff(target.getPosition());
        return game.getBoard().calculateCompartmentToAim(crd.getA(), crd.getB(), target.getRotation().ordinal());
    }

}
