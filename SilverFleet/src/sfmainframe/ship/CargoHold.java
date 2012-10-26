package sfmainframe.ship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sfmainframe.ship.cargo.CargoType;

public class CargoHold {

    private static final List<CargoType> LOADABLE_CARGO = Collections.unmodifiableList(Arrays.asList(new CargoType[] {
            CargoType.SILVER, CargoType.CANNONS_LIGHT, CargoType.CANNONS_MEDIUM }));

    private final Map<CargoType, Integer> load;


    public CargoHold(int capacity) {
        load = new HashMap<CargoType, Integer>();
        for (CargoType t : CargoType.values())
            load.put(t, 0);
        load.put(CargoType.FREE_SPACE, capacity);
    }


    public CargoHold(CargoHold ch) {
        load = new HashMap<CargoType, Integer>(ch.load);
    }


    public int getLoad(CargoType type) {
        assert (LOADABLE_CARGO.contains(type));
        return load.get(type);
    }


    public void load(CargoType type, int amount) {
        assert (LOADABLE_CARGO.contains(type));
        assert (load.get(CargoType.FREE_SPACE) > amount * type.getWeight());

        load.put(CargoType.FREE_SPACE, load.get(CargoType.FREE_SPACE) - amount * type.getWeight());
        load.put(type, load.get(type) + amount);

    }


    public int unloadAll(CargoType type) {
        int amount = load.get(type);
        unload(type, amount);
        return amount;
    }


    public void unload(CargoType type, int amount) {
        assert (LOADABLE_CARGO.contains(type));
        assert (load.get(type) > amount);

        load.put(type, load.get(type) - amount);
        load.put(CargoType.FREE_SPACE, load.get(CargoType.FREE_SPACE) + amount * type.getWeight());
    }


    public void writeToFile(DataOutputStream data_output) throws IOException {
        for (CargoType t : CargoType.getValues())
            data_output.writeInt(load.get(t));
    }


    public void readFromFile(DataInputStream data_input) throws IOException {
        for (CargoType t : CargoType.getValues())
            load.put(t, data_input.readInt());
    }
}
