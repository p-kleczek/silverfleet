package sfmainframe.ship;

import java.util.Vector;


public enum GunCompartment {
	BOW, SIDE_L, SIDE_R, STERN, NONE;
	
	/**
	 * 
	 * @return all elements except for NONE
	 */
	public static Vector<GunCompartment> getValues() {
		Vector<GunCompartment> vect = new Vector<GunCompartment>();
		for (GunCompartment gc : GunCompartment.values())
			if (gc != GunCompartment.NONE)
				vect.add(gc);
		return vect;
	}


	public static int getSize() {
		return GunCompartment.getValues().size();
	}	
}
