package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sfmainframe.Commons;
import sfmainframe.Player;
import sfmainframe.Reusable;
import sfmainframe.gameplay.KillingMode;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class MarinesSection {

    Map<MarinesCompartment, Reusable<Player>> marines;

    private CommanderState[][] commanders = new CommanderState[MarinesCompartment.getShipCompartments().length][Commons.PLAYERS_MAX];


    public MarinesSection() {

        marines = new HashMap<MarinesCompartment, Reusable<Player>>();

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments())
            marines.put(mc, new Reusable<Player>(Player.class, Player.getValues()));

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments())
            for (Player p : Player.getValues())
                clear(p, mc);
    }


    /**
     * Removes all marines and commander of the player from the compartment.
     * 
     * @param player
     *            player whose marines shall be removed
     * @param location
     *            compartment from which marines shall be removed
     */
    public void clear(Player player, MarinesCompartment location) {
        marines.get(location).clear(player);
        commanders[player.ordinal()][location.ordinal()] = CommanderState.NOT_THERE;
    }


    public MarinesSection(MarinesSection ms) {

        marines = new HashMap<MarinesCompartment, Reusable<Player>>();
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                marines.get(mc).setReady(p, ms.marines.get(mc).getReady(p));
                marines.get(mc).setUsed(p, ms.marines.get(mc).getUsed(p));
                commanders[mc.ordinal()][p.ordinal()] = ms.commanders[mc.ordinal()][p.ordinal()];
            }
        }
    }


    public MarinesSection(ShipClass sc, Player shipOwner) {
        marines.get(MarinesCompartment.DECK).setReady(shipOwner, sc.getCrewDeckMax());
        marines.get(MarinesCompartment.BATTERIES).setReady(shipOwner, sc.getCrewMax() - sc.getCrewDeckMax());
    }


    public int getMarinesNumber(Player player, MarinesCompartment location, int state) {
        switch (state) {
        case Commons.READY:
            return marines.get(location).getReady(player);
        case Commons.USED:
            return marines.get(location).getUsed(player);
        case Commons.BOTH:
            return marines.get(location).getTotal(player);
        default:
            throw new IllegalArgumentException();
        }
    }


    public CommanderState getCommanderState(Player player, MarinesCompartment location) {
        return commanders[location.ordinal()][player.ordinal()];
    }


    public void moveMarines(Player player, MarinesCompartment source, MarinesCompartment destination, int amount) {
        if (source != MarinesCompartment.SHIP_X)
            marines.get(source).modifyReady(player, -amount);

        if (destination != MarinesCompartment.SHIP_X)
            marines.get(destination).modifyUsed(player, amount);
    }


    public void setCommander(Player player, MarinesCompartment location, CommanderState state) {
        commanders[player.ordinal()][location.ordinal()] = state;
    }


    public void moveCommander(Player player, MarinesCompartment source, MarinesCompartment destination) {
        if (source != MarinesCompartment.SHIP_X)
            commanders[player.ordinal()][source.ordinal()] = CommanderState.NOT_THERE;
        if (destination != MarinesCompartment.SHIP_X)
            commanders[player.ordinal()][destination.ordinal()] = CommanderState.USED;
    }


    /**
     * 
     * @param player
     * @param location
     * @param amount
     * @param mode
     * @return obj[0] - faktyczna liczba zabitych marynarzy; obj[1] - z
     *         dowódcą/bez dowódcy
     */
    public Object[] killMarines(Player player, MarinesCompartment location, int amount, KillingMode mode) {
        /*
         * #return: faktyczna ilosc zabitych marynarzy (nie wliczajac dowodcy)
         */

        Object[] ret = new Object[2];
        ret[1] = KillingMode.WITHOUT_COMMANDER;

        int currentAmount = amount;

        if (currentAmount <= marines.get(location).getReady(player)) {
            marines.get(location).modifyReady(player, -currentAmount);
            ret[0] = amount;
            return ret;
        }

        currentAmount -= marines.get(location).getReady(player);
        marines.get(location).setReady(player, 0);

        if (currentAmount <= marines.get(location).getUsed(player)) {
            marines.get(location).modifyUsed(player, -currentAmount);
            ret[0] = amount;
            return ret;
        }

        currentAmount -= marines.get(location).getUsed(player);
        marines.get(location).setUsed(player, 0);

        // 5.3.3
        if (commanders[location.ordinal()][player.ordinal()] != CommanderState.NOT_THERE) {
            if (mode == KillingMode.WITH_COMMANDER)
                commanders[location.ordinal()][player.ordinal()] = CommanderState.NOT_THERE;
            ret[0] = amount;
            ret[1] = KillingMode.WITH_COMMANDER;
            return ret;
        }
        // --

        ret[0] = currentAmount;
        return ret;
    }


    public boolean isCommanderOnboard(Player player) {
        if (player == Player.NONE)
            return false;

        for (int location = 0; location < MarinesCompartment.getSize(); location++)
            if (commanders[location][player.ordinal()] != CommanderState.NOT_THERE)
                return true;
        return false;
    }


    public void useMarines(Player player, MarinesCompartment compartment, int number) {
        marines.get(compartment).use(player, number);
    }


    public void prepareForNewTurn() {
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                marines.get(mc).refresh();
                if (commanders[mc.ordinal()][p.ordinal()] == CommanderState.USED)
                    commanders[mc.ordinal()][p.ordinal()] = CommanderState.READY;
            }
        }
    }


    public void writeToStream(DataOutputStream dos) throws IOException {
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                dos.writeInt(marines.get(mc).getReady(p));
                dos.writeInt(marines.get(mc).getUsed(p));
            }
        }

        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                dos.writeInt(commanders[i][j].ordinal());
    }


    public void readFromStream(DataInputStream dis) throws IOException {
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                marines.get(mc).setReady(p, dis.readInt());
                marines.get(mc).setUsed(p, dis.readInt());
            }
        }

        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                commanders[i][j] = CommanderState.valueOf(dis.readInt());
    }
}
