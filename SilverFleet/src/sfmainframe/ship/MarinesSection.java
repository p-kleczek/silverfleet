package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import sfmainframe.Commons;
import sfmainframe.Player;
import sfmainframe.gameplay.KillingMode;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class MarinesSection {

    private int[][][] marines = new int[MarinesCompartment.getShipCompartments().length][Commons.PLAYERS_MAX][2]; // m[][][ready/used]
    private CommanderState[][] commanders = new CommanderState[MarinesCompartment.getShipCompartments().length][Commons.PLAYERS_MAX];


    public MarinesSection() {
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
        marines[player.ordinal()][location.ordinal()][Commons.READY] = 0;
        marines[player.ordinal()][location.ordinal()][Commons.USED] = 0;
        commanders[player.ordinal()][location.ordinal()] = CommanderState.NOT_THERE;
    }


    public MarinesSection(MarinesSection ms) {
        for (int i = 0; i < MarinesCompartment.getSize(); i++) {
            for (int j = 0; j < Commons.PLAYERS_MAX; j++) {
                marines[i][j][Commons.READY] = ms.marines[i][j][Commons.READY];
                marines[i][j][Commons.USED] = ms.marines[i][j][Commons.USED];
                commanders[i][j] = ms.commanders[i][j];
            }
        }
    }


    public MarinesSection(ShipClass sc, Player shipOwner) {
        marines[MarinesCompartment.DECK.ordinal()][shipOwner.ordinal()][Commons.READY] = sc.getCrewDeckMax();
        marines[MarinesCompartment.BATTERIES.ordinal()][shipOwner.ordinal()][Commons.READY] = sc.getCrewMax()
                - sc.getCrewDeckMax();
    }


    public int getMarinesNumber(Player player, MarinesCompartment location, int state) {
        if (state == Commons.BOTH)
            return marines[location.ordinal()][player.ordinal()][Commons.READY]
                    + marines[location.ordinal()][player.ordinal()][Commons.USED];
        else
            return marines[location.ordinal()][player.ordinal()][state];
    }


    public CommanderState getCommanderState(Player player, MarinesCompartment location) {
        return commanders[location.ordinal()][player.ordinal()];
    }


    public void moveMarines(Player player, MarinesCompartment source, MarinesCompartment destination, int amount) {
        if (source != MarinesCompartment.SHIP_X)
            marines[source.ordinal()][player.ordinal()][Commons.READY] -= amount;

        if (destination != MarinesCompartment.SHIP_X)
            marines[destination.ordinal()][player.ordinal()][Commons.USED] += amount;
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

        if (currentAmount <= marines[location.ordinal()][player.ordinal()][Commons.READY]) {
            marines[location.ordinal()][player.ordinal()][Commons.READY] -= currentAmount;
            ret[0] = amount;
            return ret;
        }

        currentAmount -= marines[location.ordinal()][player.ordinal()][Commons.READY];
        marines[location.ordinal()][player.ordinal()][Commons.READY] = 0;

        if (currentAmount <= marines[location.ordinal()][player.ordinal()][Commons.USED]) {
            marines[location.ordinal()][player.ordinal()][Commons.USED] -= currentAmount;
            ret[0] = amount;
            return ret;
        }

        currentAmount -= marines[location.ordinal()][player.ordinal()][Commons.USED];
        marines[location.ordinal()][player.ordinal()][Commons.USED] = 0;

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
        marines[compartment.ordinal()][player.ordinal()][Commons.READY] -= number;
        marines[compartment.ordinal()][player.ordinal()][Commons.USED] += number;
    }


    public void prepareForNewTurn() {
        for (int i = 0; i < MarinesCompartment.getSize(); i++) {
            for (int j = 0; j < Commons.PLAYERS_MAX; j++) {
                marines[i][j][Commons.READY] += marines[i][j][Commons.USED];
                marines[i][j][Commons.USED] = 0;
                if (commanders[i][j] == CommanderState.USED)
                    commanders[i][j] = CommanderState.READY;
            }
        }
    }


    public void writeToStream(DataOutputStream dos) throws IOException {
        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                for (int k = 0; k < 2; k++)
                    dos.writeInt(marines[i][j][k]);

        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                dos.writeInt(commanders[i][j].ordinal());
    }


    public void readFromStream(DataInputStream dis) throws IOException {
        int dummy = 0;

        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++)
                for (int k = 0; k < 2; k++)
                    marines[i][j][k] = dis.readInt();

        for (int i = 0; i < MarinesCompartment.getSize(); i++)
            for (int j = 0; j < Commons.PLAYERS_MAX; j++) {
                dummy = dis.readInt();

                switch (dummy) {
                case 0:
                    commanders[i][j] = CommanderState.NOT_THERE;
                    break;
                case 1:
                    commanders[i][j] = CommanderState.READY;
                    break;
                case 2:
                    commanders[i][j] = CommanderState.USED;
                    break;
                case 3:
                    commanders[i][j] = CommanderState.IMPRISONED;
                    break;
                default:
                    System.err.print("Unknown commander state value\n");
                }
            }
    }
}
