package sfmainframe.ship;

import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

import sfmainframe.gameplay.between.DealType;

public enum Gun {
	LIGHT(2, 2, 1, 5, 3), MEDIUM(3, 3, 2, 10, 6), HEAVY(4, 4, 4, 15, 9), NONE(
			0, 0, 0, 0, 0);

	private final int crewSize;
	private final int range;
	private final int shotDamage;
	private final Map<DealType, Integer> price = new EnumMap<DealType, Integer>(
			DealType.class);

	private Gun(int crewSize, int range, int shotDamage, int buyPrice,
			int sellPrice) {
		this.crewSize = crewSize;
		this.range = range;
		this.shotDamage = shotDamage;

		price.put(DealType.BUY, buyPrice);
		price.put(DealType.SELL, sellPrice);
	}

	/**
	 * 
	 * @return all elements except for NONE
	 */
	public static Vector<Gun> getValues() {
		Vector<Gun> vect = new Vector<Gun>();
		for (Gun gt : Gun.values())
			if (gt != Gun.NONE)
				vect.add(gt);
		return vect;
	}

	public static int getSize() {
		return Gun.getValues().size();
	}

	public int getCrewSize() {
		return crewSize;
	}

	public int getRange() {
		return range;
	}

	public int getShotDamage() {
		return shotDamage;
	}

	public int getPrice(DealType dealType) {
		return price.get(dealType);
	}
}
