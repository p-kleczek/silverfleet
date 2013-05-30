package sfmainframe.board;

import sfmainframe.Player;
import sfmainframe.ship.Ship;

/**
 * 
 * Encoding: 100 * ship_ID + terrain + player_ID
 * 
 * @author Pawel Kleczek
 * @version 0.1
 * @since 06-10-2012
 *
 */
public class Hex {

	public Ship ship;
	public Terrain terrain;
	public Player owner;
	
	
	public Hex() {
		ship = null;
		terrain = Terrain.WATER;
		owner = Player.NONE;
	}
	
	public void decode(int value) {
	    // FIXME
//		ship = value / 100;
//		if (ship == 0)
//			ship = null;

		for (Terrain t : Terrain.values()) {
			if (t.getCode() == ((value % 100) / 10) * 10)
				terrain = t;
		}

		owner = Player.valueOf(value % 10);
	}
	
	public int encode() {
	    // FIXME
//		int iId = (ship == null) ? 0 : ship;
        int iId = 0;
		return iId * 100 + terrain.getCode() + owner.ordinal();
	}
	
}
