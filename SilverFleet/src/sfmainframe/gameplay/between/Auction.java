package sfmainframe.gameplay.between;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sfmainframe.Player;
import sfmainframe.ship.Ship;
import sfmainframe.ship.ShipClass;

public class Auction {

    private final int auctionID;
    private final int startTurnID;

    private final Integer offeredShipID;
    private final ShipClass offeredShipClass;
    private final int startingPrice;
    private final Map<Player, Integer> offers;


    public Auction(int auctionID, int startTurnID, Ship ship, ShipClass shipClass, int startingPrice) {
        this.auctionID = auctionID;
        this.startTurnID = startTurnID;

        offeredShipID = ship.getID();
        offeredShipClass = shipClass;

        offers = new HashMap<Player, Integer>();
        for (Player p : Player.getValues())
            offers.put(p, 0);

        this.startingPrice = startingPrice;
    }


    public void writeToFile(DataOutputStream data_output) {
        try {
            data_output.writeInt(auctionID);
            data_output.writeInt(startTurnID);
            data_output.writeInt(offeredShipID);
            data_output.writeInt(offeredShipClass.ordinal());

            for (Player p : Player.getValues())
                data_output.writeInt(offers.get(p));

            data_output.writeInt(startingPrice);
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }


    public void setOffer(Player p, int offer) {
        offers.put(p, offer);
    }


    public int getAuctionID() {
        return auctionID;
    }


    public int getStartTurnID() {
        return startTurnID;
    }


    public Integer getOfferedShipID() {
        return offeredShipID;
    }


    public ShipClass getOfferedShipClass() {
        return offeredShipClass;
    }


    public int getStartingPrice() {
        return startingPrice;
    }


    public Integer getOffer(Player p) {
        return offers.get(p);
    }


    public static Auction readFromFile(DataInputStream data_input) throws IOException {
        int auctionID = data_input.readInt();
        int startTurnID = data_input.readInt();
        int offeredShipID = data_input.readInt();

        ShipClass offeredShipClass = ShipClass.valueOf(data_input.readInt());

        Map<Player, Integer> offers = new HashMap<Player, Integer>();
        for (Player p : Player.getValues())
            offers.put(p, data_input.readInt());

        int startingPrice = data_input.readInt();

        // FIXME: generowanie na podstawie ID
//        Auction a = new Auction(auctionID, startTurnID, offeredShipID, offeredShipClass, startingPrice);
        Auction a = null;
        for (Player p : Player.getValues())
            a.setOffer(p, offers.get(p));
        return a;
    }
}
