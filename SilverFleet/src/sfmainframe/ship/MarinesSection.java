package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sfmainframe.Commons;
import sfmainframe.Player;
import sfmainframe.ReusableMap;
import sfmainframe.gameplay.KillingMode;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class MarinesSection {

    private final Map<MarinesCompartment, ReusableMap<Player>> marines;
    private final Map<MarinesCompartment, Map<Player, CommanderState>> commanders;


    private MarinesSection() {
        marines = new HashMap<MarinesCompartment, ReusableMap<Player>>();
        commanders = new HashMap<MarinesCompartment, Map<Player, CommanderState>>();

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            marines.put(mc, new ReusableMap<Player>(Player.class, Player.getValues()));
            commanders.put(mc, new HashMap<Player, CommanderState>());
        }

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
        marines.get(location).getElement(player).clear();
        commanders.get(location).put(player, CommanderState.NOT_THERE);
    }


    public MarinesSection(MarinesSection ms) {
        this();

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                marines.get(mc).getElement(p).copy(ms.marines.get(mc).getElement(p));
                commanders.get(mc).put(p, ms.commanders.get(mc).get(p));
            }
        }
    }


    public MarinesSection(ShipClass sc, Player shipOwner) {
        this();

        marines.get(MarinesCompartment.DECK).getElement(shipOwner).setReady(sc.getCrewDeckMax());
        marines.get(MarinesCompartment.BATTERIES).getElement(shipOwner).setReady(sc.getCrewMax() - sc.getCrewDeckMax());
    }


    public int getMarinesNumber(Player player, MarinesCompartment location, int state) {
        switch (state) {
        case Commons.READY:
            return marines.get(location).getElement(player).getReady();
        case Commons.USED:
            return marines.get(location).getElement(player).getUsed();
        case Commons.BOTH:
            return marines.get(location).getElement(player).getTotal();
        default:
            throw new IllegalArgumentException();
        }
    }


    public CommanderState getCommanderState(Player player, MarinesCompartment location) {
        return commanders.get(location).get(player);
    }


    public void moveMarines(Player player, MarinesCompartment source, MarinesCompartment destination, int amount) {
        if (source != MarinesCompartment.SHIP_X)
            marines.get(source).getElement(player).modifyReady(-amount);

        if (destination != MarinesCompartment.SHIP_X)
            marines.get(destination).getElement(player).modifyUsed(amount);
    }


    public void setCommander(Player player, MarinesCompartment location, CommanderState state) {
        commanders.get(location).put(player, state);
    }


    public void moveCommander(Player player, MarinesCompartment source, MarinesCompartment destination) {
        if (source != MarinesCompartment.SHIP_X)
            commanders.get(source).put(player, CommanderState.NOT_THERE);
        if (destination != MarinesCompartment.SHIP_X)
            commanders.get(destination).put(player, CommanderState.USED);
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
        Object[] ret = new Object[2];
        ret[1] = KillingMode.WITHOUT_COMMANDER;

        int currentAmount = amount;

        if (currentAmount <= marines.get(location).getElement(player).getReady()) {
            marines.get(location).getElement(player).modifyReady(-currentAmount);
            ret[0] = amount;
            return ret;
        }

        currentAmount -= marines.get(location).getElement(player).getReady();
        marines.get(location).getElement(player).setReady(0);

        if (currentAmount <= marines.get(location).getElement(player).getUsed()) {
            marines.get(location).getElement(player).modifyUsed(-currentAmount);
            ret[0] = amount;
            return ret;
        }

        currentAmount -= marines.get(location).getElement(player).getUsed();
        marines.get(location).getElement(player).setUsed(0);

        // 5.3.3
        if (commanders.get(location).get(player) != CommanderState.NOT_THERE) {
            if (mode == KillingMode.WITH_COMMANDER)
                commanders.get(location).put(player, CommanderState.NOT_THERE);
            ret[0] = amount;
            ret[1] = KillingMode.WITH_COMMANDER;
            return ret;
        }
        // --

        ret[0] = currentAmount;
        return ret;
    }


    public boolean isCommanderOnboard(Player player) {
        assert (player != Player.NONE);

        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments())
            if (commanders.get(mc).get(player) != CommanderState.NOT_THERE)
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
                if (commanders.get(mc).get(p) == CommanderState.USED)
                    commanders.get(mc).put(p, CommanderState.READY);
            }
        }
    }


    public void writeToStream(DataOutputStream dos) throws IOException {
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                dos.writeInt(marines.get(mc).getElement(p).getReady());
                dos.writeInt(marines.get(mc).getElement(p).getUsed());
            }
        }

        // FIXME
        // for (int i = 0; i < MarinesCompartment.getSize(); i++)
        // for (int j = 0; j < Commons.PLAYERS_MAX; j++)
        // dos.writeInt(commanders[i][j].ordinal());
    }


    public void readFromStream(DataInputStream dis) throws IOException {
        for (MarinesCompartment mc : MarinesCompartment.getShipCompartments()) {
            for (Player p : Player.getValues()) {
                marines.get(mc).getElement(p).setReady(dis.readInt());
                marines.get(mc).getElement(p).setUsed(dis.readInt());
            }
        }

        // FIXME
        // for (int i = 0; i < MarinesCompartment.getSize(); i++)
        // for (int j = 0; j < Commons.PLAYERS_MAX; j++)
        // commanders[i][j] = CommanderState.valueOf(dis.readInt());
    }
}
