package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sfmainframe.Commons;
import sfmainframe.Reusable;

public class CannonSection {

    private final Map<GunCompartment, Reusable<Gun>> cannons;


    public CannonSection() {
        cannons = new HashMap<GunCompartment, Reusable<Gun>>();
        clear();
    }


    public CannonSection(CannonSection cannonSection) {
        cannons = new HashMap<GunCompartment, Reusable<Gun>>();
        for (Gun type : Gun.getValues()) {
            for (GunCompartment location : GunCompartment.getValues()) {
                cannons.get(location).setReady(type, cannonSection.cannons.get(location).getReady(type));
                cannons.get(location).setUsed(type, cannonSection.cannons.get(location).getUsed(type));
            }
        }
    }


    public CannonSection(ShipClass shipClass) {
        cannons = new HashMap<GunCompartment, Reusable<Gun>>();
        for (Gun type : Gun.getValues()) {
            for (GunCompartment location : GunCompartment.getValues()) {
                cannons.get(location).setReady(type, shipClass.getCannonMax()[location.ordinal()][type.ordinal()]);
            }
        }
    }


    public void clear() {
        for (Gun type : Gun.getValues())
            for (GunCompartment location : GunCompartment.getValues())
                cannons.get(location).clear(type);
    }


    public void useCannon(GunCompartment compartment, Gun gunType) {
        cannons.get(compartment).use(gunType, 1);
    }


    public void modifyCannonsNumber(GunCompartment location, Gun type, int number) {
        cannons.get(location).modifyUsed(type, number);
    }


    public int getCannonsNumber(GunCompartment location, Gun _type, int _state) {
        switch (_state) {
        case Commons.READY:
            return cannons.get(location).getReady(_type);
        case Commons.USED:
            return cannons.get(location).getUsed(_type);
        case Commons.BOTH:
            return cannons.get(location).getTotal(_type);
        default:
            throw new IllegalArgumentException();
        }
    }


    // FIXME : zwracanie struktury
    public int destroyCannon(GunCompartment location, Gun _type, int _state) throws IllegalArgumentException {
        if (_state == Commons.READY || _state == Commons.BOTH) {
            assert (cannons.get(location).getReady(_type) > 0);
            cannons.get(location).modifyReady(_type, -1);
            return Commons.READY;
        }

        assert (cannons.get(location).getUsed(_type) > 0);
        cannons.get(location).modifyUsed(_type, -1);
        return Commons.USED;
    }


    public int getTotalCannonNumber() {
        int cannons = 0;
        for (GunCompartment location : GunCompartment.getValues())
            for (Gun gt : Gun.getValues())
                cannons += getCannonsNumber(location, gt, Commons.BOTH);
        return cannons;
    }


    public void prepareForNewTurn() {
        for (GunCompartment location : GunCompartment.getValues())
            cannons.get(location).refresh();
    }


    public void writeToStream(DataOutputStream dos) throws IOException {
        for (GunCompartment gc : GunCompartment.getValues())
            for (Gun g : Gun.values()) {
                dos.writeInt(cannons.get(gc).getReady(g));
                dos.writeInt(cannons.get(gc).getUsed(g));
            }
    }


    public void readFromStream(DataInputStream dis) throws IOException {
        for (GunCompartment gc : GunCompartment.getValues())
            for (Gun g : Gun.values()) {
                cannons.get(gc).setReady(g, dis.readInt());
                cannons.get(gc).setUsed(g, dis.readInt());
            }
    }
}
