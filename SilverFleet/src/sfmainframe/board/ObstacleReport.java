package sfmainframe.board;

import sfmainframe.Commons;
import sfmainframe.Player;


public class ObstacleReport {

	public int distanceToObstacle;
	public boolean problemOccured;
	public Terrain hexTerrainType;
	public Player hexOwner;
	public Integer hexShipID;


	public ObstacleReport() {
		distanceToObstacle = Commons.NIL;
		problemOccured = false;
		hexTerrainType = Terrain.WATER;
		hexOwner = Player.NONE;
		hexShipID = null;
	}
}
