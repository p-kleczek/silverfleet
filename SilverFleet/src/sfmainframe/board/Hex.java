package sfmainframe.board;

import sfmainframe.Player;

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

	public Integer shipID;
	public Terrain terrain;
	public Player owner;
	
	
	public Hex() {
		shipID = null;
		terrain = Terrain.WATER;
		owner = Player.NONE;
	}
	
	public void decode(int value) {
		shipID = value / 100;
		if (shipID == 0)
			shipID = null;

		for (Terrain t : Terrain.values()) {
			if (t.getCode() == ((value % 100) / 10) * 10)
				terrain = t;
		}

		owner = Player.valueOf(value % 10);
	}
	
	public int encode() {
		int iId = (shipID == null) ? 0 : shipID;
		return iId * 100 + terrain.getCode() + owner.ordinal();
	}
	
}
