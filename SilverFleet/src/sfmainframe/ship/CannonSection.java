package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import sfmainframe.Commons;

public class CannonSection {

    private int[][][] cannon = new int[GunCompartment.getSize()][Gun.getSize()][2]; // c[][][ready/used]


    public CannonSection() {
        clear();
    }


    public CannonSection(CannonSection cannonSection) {
        for (Gun type : Gun.getValues()) {
            for (GunCompartment location : GunCompartment.getValues()) {
                cannon[location.ordinal()][type.ordinal()][Commons.READY] = cannonSection.cannon[location.ordinal()][type
                        .ordinal()][Commons.READY];
                cannon[location.ordinal()][type.ordinal()][Commons.USED] = cannonSection.cannon[location.ordinal()][type
                        .ordinal()][Commons.USED];
            }
        }
    }


    public CannonSection(ShipClass shipClass) {
        for (Gun type : Gun.getValues()) {
            for (GunCompartment location : GunCompartment.getValues()) {
                cannon[location.ordinal()][type.ordinal()][Commons.READY] = shipClass.getCannonMax()[location.ordinal()][type
                        .ordinal()];
            }
        }
    }


    public void clear() {
        for (Gun type : Gun.getValues()) {
            for (GunCompartment location : GunCompartment.getValues()) {
                cannon[location.ordinal()][type.ordinal()][Commons.READY] = 0;
                cannon[location.ordinal()][type.ordinal()][Commons.USED] = 0;
            }
        }
    }


    public void useCannon(GunCompartment compartment, Gun gunType) {
        cannon[compartment.ordinal()][gunType.ordinal()][Commons.READY]--;
        cannon[compartment.ordinal()][gunType.ordinal()][Commons.USED]++;
    }


    public void modifyCannonsNumber(GunCompartment location, Gun type, int number) {
        cannon[location.ordinal()][type.ordinal()][Commons.USED] += number;
    }


    public int getCannonsNumber(GunCompartment location, Gun _type, int _state) {
        if (_state == Commons.BOTH)
            return cannon[location.ordinal()][_type.ordinal()][Commons.READY]
                    + cannon[location.ordinal()][_type.ordinal()][Commons.USED];
        else
            return cannon[location.ordinal()][_type.ordinal()][_state];
    }


    // FIXME : zwracanie struktury
    public int destroyCannon(GunCompartment location, Gun _type, int _state) throws IllegalArgumentException {
        if (_state == Commons.READY || _state == Commons.BOTH) {
            if (cannon[location.ordinal()][_type.ordinal()][Commons.READY] < 0)
                throw new IllegalArgumentException();

            cannon[location.ordinal()][_type.ordinal()][Commons.READY]--;
            return Commons.READY;
        }

        // Commons.USED
        if (cannon[location.ordinal()][_type.ordinal()][Commons.USED] < 0)
            throw new IllegalArgumentException();

        cannon[location.ordinal()][_type.ordinal()][Commons.USED]--;
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
        for (Gun type : Gun.getValues()) {
            for (GunCompartment location : GunCompartment.getValues()) {
                cannon[location.ordinal()][type.ordinal()][Commons.READY] += cannon[location.ordinal()][type.ordinal()][Commons.USED];
                cannon[location.ordinal()][type.ordinal()][Commons.USED] = 0;
            }
        }
    }


    public void writeToStream(DataOutputStream dos) throws IOException {
        for (int i = 0; i < GunCompartment.getSize(); i++)
            for (int j = 0; j < Gun.getSize(); j++)
                for (int k = 0; k < 2; k++)
                    dos.writeInt(cannon[i][j][k]);
    }


    public void readFromStream(DataInputStream dis) throws IOException {
        for (int i = 0; i < GunCompartment.getSize(); i++)
            for (int j = 0; j < Gun.getSize(); j++)
                for (int k = 0; k < 2; k++)
                    cannon[i][j][k] = dis.readInt();
    }
}
