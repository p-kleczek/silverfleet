package sfmainframe.ship.cargo;

import java.util.Vector;

public enum CargoType {

    FREE_SPACE(null), SILVER(1), CANNONS_LIGHT(1), CANNONS_MEDIUM(2), NONE(null);

    private final Integer weight;


    private CargoType(Integer weight) {
        this.weight = weight;
    }


    public Integer getWeight() {
        return weight;
    }


    public static Vector<CargoType> getValues() {
        Vector<CargoType> vect = new Vector<CargoType>();
        for (CargoType ct : CargoType.values())
            if (ct != CargoType.NONE)
                vect.add(ct);
        return vect;
    }


    public static int getSize() {
        return CargoType.getValues().size();
    }
}
