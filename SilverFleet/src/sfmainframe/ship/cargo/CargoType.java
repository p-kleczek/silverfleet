package sfmainframe.ship.cargo;

import java.util.Vector;


public enum CargoType {
	FREE_SPACE, SILVER, CANNONS_LIGHT, CANNONS_MEDIUM, NONE;
	
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
