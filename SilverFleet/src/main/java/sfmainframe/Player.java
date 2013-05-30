package sfmainframe;

import java.util.Vector;

public enum Player {
	PASADENA, ELMETH, SIDONIA, PLEENSY, HAMPSHIRE, DISCASTER, DELACROIX, LEPPO, NONE;

	public static Player valueOf(int playerId) {
		for (Player plr : Player.values())
			if (plr.ordinal() == playerId)
				return plr;
		return Player.NONE;
	}
	
	/**
	 * 
	 * @return all elements except for NONE
	 */
	public static Vector<Player> getValues() {
		Vector<Player> vect = new Vector<Player>();
		for (Player p : Player.values())
			if (p != Player.NONE)
				vect.add(p);
		return vect;
	}


	public static int getSize() {
		return Player.getValues().size();
	}	
}
