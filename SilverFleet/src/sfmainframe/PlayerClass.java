package sfmainframe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sfmainframe.gameplay.between.DealType;
import sfmainframe.ship.Gun;
import sfmainframe.ship.Ship;

public class PlayerClass {

    private Player identity;

    private boolean isInGame; // czy gracz nadal uczestniczy w grze
    private Player internedBy; // przez kogo dowodca gracza jest internowany

    private Set<Integer> allies; // aktualni sojusznicy
    private Set<Ship> fleet; // ID okretow wlasnej floty
    private Map<Player, Set<Ship>> shipsInterned; // ID okretow internowanych
    // internowane przez nas cudze lub (dla siebie) internowane przez innych
    // nasze

    private int gold; // gotowka
    private int silver; // zapas srebra
    private int shipyards; // ilosc posiadanych stoczni
    private int foundries; // ilosc posiadanych odlewni

    private int marines; // posiadani marynarze

    private int shipsInYards; // ilosc statkow gotowych do zakupu i zwodowania w
                              // stoczniach

    private int[] marinesInterned; // ilosc internowanych marynarzy innych
                                   // graczy
    private boolean[] commandersInterned; // internowani dowodcy innych graczy

    private int[] cannons; // ilosc posiadanych w magazynach dzial dlanego typu

    private int destroyedShipsNumber; /*
                                       * ilosc zniszczonych podczas rozgrywki
                                       * statkow wroga (do obliczania nagrody
                                       * dla Anglikow
                                       */

    private int loan; // wysokosc zaciagnietej pozyczki
    private int currentSilverPrice;


    public PlayerClass(Player identity) {
        this.identity = identity;

        isInGame = false; // czy graczem faktycznie ktos gra
        internedBy = Player.NONE;

        allies = new HashSet<Integer>();
        allies.add(Player.NONE.ordinal()); // można spokojnie pływać po
                                           // bezpańskich wodach i obsadzać
                                           // wraki

        fleet = new HashSet<Ship>();

        shipsInterned = new HashMap<Player, Set<Ship>>();
        marinesInterned = new int[Commons.PLAYERS_MAX];
        commandersInterned = new boolean[Commons.PLAYERS_MAX];

        for (Player p : Player.values()) {
            shipsInterned.put(p, new HashSet<Ship>());
        }

        for (int player = 0; player < Commons.PLAYERS_MAX; player++) {
            marinesInterned[player] = 0;
            commandersInterned[player] = false;
        }

        gold = 0;
        silver = 0;
        shipyards = 0;
        foundries = 0;
        marines = 0;
        shipsInYards = 0;

        cannons = new int[Gun.getSize()];
        for (Gun gt : Gun.values()) {
            if (gt == Gun.NONE)
                continue;
            cannons[gt.ordinal()] = 0;
        }

        destroyedShipsNumber = 0;
        loan = 0;
        currentSilverPrice = 0;
    }


    public Player getIdentity() {
        return identity;
    }


    public Set<Ship> getInternedShips(Player player) {
        return shipsInterned.get(player);
    }


    public void addInternedShip(Player player, Ship ship) {
        shipsInterned.get(player).add(ship);
    }


    public void removeInternedShip(Player player, Ship ship) {
        shipsInterned.get(player).remove(ship);
    }


    public void clearInternedShips() {
        for (Player p : Player.values())
            shipsInterned.get(p).clear();
    }


    public Player getCommanderInternedBy() {
        return internedBy;
    }


    public void setCommanderInternedBy(Player player) {
        internedBy = player;
    }


    public void endGame() {
        isInGame = false;
    }


    public void intern(Player player) {
        internedBy = player;
    }


    public boolean isAlly(Player player) {
        return allies.contains(Integer.valueOf(player.ordinal()));
    }


    public void addAlly(Player player) {
        allies.add(player.ordinal());
    }


    public void removeAlly(Player player) {
        allies.remove(player.ordinal());
    }


    public Set<Ship> getFleet() {
        return fleet;
    }


    public boolean isInGame() {
        return isInGame;
    }


    public void addPlayerToGame() {
        isInGame = true;
    }


    public int getGold() {
        return gold;
    }


    public void removeGold(int amount) {
        gold = Math.max(0, gold - amount);
    }


    public void addGold(int amount) {
        gold += amount;
    }


    public int getSilver() {
        return silver;
    }


    public void removeSilver(int amount) {
        silver = Math.max(0, silver - amount);
    }


    public void addSilver(int amount) {
        silver += amount;
    }


    public int getShipsInYards() {
        return shipsInYards;
    }


    public void addShipToYard(int number) {
        shipsInYards += number;
    }


    public void removeShipFromYard() {
        shipsInYards--;
    }


    public void addShipToFleet(Ship s) {
        fleet.add(s);
    }


    public void removeShipFromFleet(Ship s) {
        fleet.remove(s);
    }


    public int getMarines() {
        return marines;
    }


    public void addMarines(int number) {
        marines += number;
    }


    public void removeMarines(int number) {
        marines = Math.max(0, marines - number);
    }


    public int getCannons(Gun type) {
        return cannons[type.ordinal()];
    }


    public void addCannons(Gun type, int number) {
        cannons[type.ordinal()] += number;
    }


    public void removeCannons(Gun type, int number) {
        cannons[type.ordinal()] -= number;
    }


    public void buyShipyard() {
        shipyards++;
        removeGold(BetweenTurnsDialog.SHIPYARD_PRICE);
    }


    public int getShipyards() {
        return shipyards;
    }


    public void buyFoundry() {
        foundries++;
        removeGold(BetweenTurnsDialog.FOUNDRY_PRICE);
    }


    public int getFoundries() {
        return foundries;
    }


    public int getMarinesInterned(Player player) {
        return marinesInterned[player.ordinal()];
    }


    public void addMarinesInterned(Player player, int number) {
        marinesInterned[player.ordinal()] += number;
    }


    public void removeMarinesInterned(Player player, int number) {
        marinesInterned[player.ordinal()] -= number;
    }


    public boolean getCommandersInterned(Player player) {
        return commandersInterned[player.ordinal()];
    }


    public void addCommandersInterned(Player player) {
        commandersInterned[player.ordinal()] = true;
    }


    public void removeCommandersInterned(Player player) {
        commandersInterned[player.ordinal()] = false;
    }


    public int getDestroyedShipsNumber() {
        return destroyedShipsNumber;
    }


    public void addDestroyedShip() {
        destroyedShipsNumber++;
    }


    public void resetDestroyedShipsNumber() {
        destroyedShipsNumber = 0;
    }


    public int getLoan() {
        return loan;
    }


    public void setLoan(int value) {
        loan = value;
    }


    public int getCurrentSilverPrice() {
        return currentSilverPrice;
    }


    public void setCurrentSilverPrice(int value) {
        currentSilverPrice = value;
    }


    public void writeToFile(DataOutputStream data_output) {
        Object[] set;
        try {
            data_output.writeBoolean(isInGame);
            data_output.writeInt(internedBy.ordinal());

            data_output.writeInt(allies.size());
            set = new Object[allies.size()];
            set = allies.toArray();
            for (int i = 0; i < allies.size(); i++)
                data_output.writeInt((Integer) set[i]);

            data_output.writeInt(fleet.size());
            set = new Object[fleet.size()];
            set = fleet.toArray();
            for (int i = 0; i < fleet.size(); i++)
                data_output.writeInt((Integer) set[i]);

            for (Player p : Player.getValues()) {
                Set<Ship> interned = shipsInterned.get(p);
                data_output.writeInt(interned.size());
                // FIXME: zapis
                // for (Integer id : interned)
                // data_output.writeInt(id);
            }

            data_output.writeInt(gold);
            data_output.writeInt(silver);
            data_output.writeInt(shipyards);
            data_output.writeInt(foundries);
            data_output.writeInt(marines);

            data_output.writeInt(shipsInYards);

            for (int i = 0; i < Commons.PLAYERS_MAX; i++)
                data_output.writeInt(marinesInterned[i]);

            for (int i = 0; i < Commons.PLAYERS_MAX; i++)
                data_output.writeBoolean(commandersInterned[i]);

            for (int i = 0; i < Gun.getSize(); i++)
                data_output.writeInt(cannons[i]);

            data_output.writeInt(destroyedShipsNumber);
            data_output.writeInt(loan);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    @SuppressWarnings("static-access")
    public void readFromFile(DataInputStream data_input) {
        int counter = 0;

        try {
            isInGame = data_input.readBoolean();
            internedBy = Player.valueOf(data_input.readInt());

            allies.clear();
            counter = data_input.readInt();
            for (int i = 0; i < counter; i++)
                allies.add(data_input.readInt());

            fleet.clear();
            counter = data_input.readInt();
            // FIXME: odczyt
            // for (int i = 0; i < counter; i++)
            // fleet.add(data_input.readInt());

            for (Player p : Player.getValues()) {
                shipsInterned.get(p).clear();
                counter = data_input.readInt();
                // FIXME: odczyt
                // for (int i = 0; i < counter; i++)
                // shipsInterned.get(p).add(data_input.readInt());
            }

            gold = data_input.readInt();
            silver = data_input.readInt();
            shipyards = data_input.readInt();
            foundries = data_input.readInt();
            marines = data_input.readInt();

            shipsInYards = data_input.readInt();

            for (int i = 0; i < Commons.PLAYERS_MAX; i++)
                marinesInterned[i] = data_input.readInt();

            for (int i = 0; i < Commons.PLAYERS_MAX; i++)
                commandersInterned[i] = data_input.readBoolean();

            for (int i = 0; i < Gun.getSize(); i++)
                cannons[i] = data_input.readInt();

            destroyedShipsNumber = data_input.readInt();
            loan = data_input.readInt();
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    public void buyCannons(Player player, Gun type, int number) {
        addCannons(type, number);
        removeGold(type.getPrice(DealType.BUY) * number);
    }


    public void sellCannons(Player player, Gun type, int number) {
        removeCannons(type, number);
        addGold(type.getPrice(DealType.SELL) * number);
    }


    public void hireMarines(Player player, int number) {
        addMarines(number);
        removeGold(number);
    }


    public void takeLoan(Player player, int amount) {
        setLoan(amount);
        addGold(amount);
    }


    public void repayLoan(Player player, int amount) {
        setLoan(getLoan() - amount);
        removeGold(amount);
    }


    public void sellSilver(Player player, int amount) {
        removeSilver(amount);
        addGold(amount * getCurrentSilverPrice());
    }


    public void reverseAlly(Player ally) {
        if (isAlly(ally))
            removeAlly(ally);
        else
            addAlly(ally);
    }


    public Set<Integer> getAllies() {
        return new HashSet<Integer>(allies);
    }
}
