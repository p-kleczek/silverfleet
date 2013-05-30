package silverfleet.ship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import sfmainframe.Commons;
import sfmainframe.Player;
import sfmainframe.Range;
import sfmainframe.board.RotateDirection;
import sfmainframe.ship.Ship;
import sfmainframe.ship.ShipClass;
import sfmainframe.ship.marines.MarinesCompartment;

public class ShipTest {

	Ship ship;

	@Ignore
	public void testShipPlayerShipClassInt() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testShipShip() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testShipDataInputStream() {
		fail("Not yet implemented");
	}

	@Test
	public void testDestroyHelmReady() {
		ship = new Ship(Player.PASADENA, ShipClass.PINSE, 0);

		ship.destroyHelm(1);

		assertEquals(2, ship.getHelm(Commons.READY));
		assertEquals(0, ship.getHelm(Commons.USED));
	}

	@Test
	public void testDestroyHelmUsed() {
		ship = new Ship(Player.PASADENA, ShipClass.PINSE, 0);
		ship.useHelm(2);

		ship.destroyHelm(2);

		assertEquals(0, ship.getHelm(Commons.READY));
		assertEquals(1, ship.getHelm(Commons.USED));
	}

	@Test
	public void testGetPlayerMarinesOnShip() {
		ship = new Ship(Player.PASADENA, ShipClass.PINSE, 0);

		assertEquals(50, ship.getPlayerMarinesOnShip(Player.PASADENA, false));
	}

	@Test
	public void testGetPlayerMarinesOnShipWithCommander() {
		ship = new Ship(Player.PASADENA, ShipClass.PINSE, 0);
		ship.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X,
				MarinesCompartment.INMOVE);

		assertEquals(51, ship.getPlayerMarinesOnShip(Player.PASADENA, true));
	}

	@Ignore
	public void testEscapeFromBoarding() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testCalculateRepairCosts() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testWriteToFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckAngleToRotateTowedUnlimited() {
		Ship tug = new Ship(Player.PASADENA, ShipClass.PINSE, 0);
		Ship towed = new Ship(Player.PASADENA, ShipClass.PINSE, 0);
		tug.setTowOther(towed);
		tug.setPosition(5, 3);
		towed.setPosition(6, 2);
		tug.setRotation(RotateDirection.NW);

		Range r = tug
				.checkAngleToRotate(RotateDirection.SW, RotateDirection.NW);

		assertEquals(-1, r.getLowerBound());
		assertEquals(1, r.getUpperBound());
	}

	@Test
	public void testCheckAngleToRotateTowedLimited() {
		Ship tug = new Ship(Player.PASADENA, ShipClass.PINSE, 0);
		Ship towed = new Ship(Player.PASADENA, ShipClass.PINSE, 0);
		tug.setTowOther(towed);
		tug.setPosition(5, 3);
		towed.setPosition(6, 2);

		Range r = tug.checkAngleToRotate(RotateDirection.SW, RotateDirection.N);

		assertEquals(-1, r.getLowerBound());
		assertEquals(0, r.getUpperBound());
	}

	@Ignore
	public void testCalculateSellPrice() {
		fail("Not yet implemented");
	}

}
