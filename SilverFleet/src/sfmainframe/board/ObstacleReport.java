package sfmainframe.board;

import sfmainframe.Commons;
import sfmainframe.Player;
import sfmainframe.ship.Ship;


public class ObstacleReport {

	public int distanceToObstacle;
	public boolean problemOccured;
	public Terrain hexTerrainType;
	public Player hexOwner;
	public Ship hexShip;


	public ObstacleReport() {
		distanceToObstacle = Commons.NIL;
		problemOccured = false;
		hexTerrainType = Terrain.WATER;
		hexOwner = Player.NONE;
		hexShip = null;
	}
}
