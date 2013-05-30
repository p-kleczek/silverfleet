package silverfleet.ship;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import sfmainframe.Commons;
import sfmainframe.Player;
import sfmainframe.gameplay.KillingMode;
import sfmainframe.ship.MarinesSection;
import sfmainframe.ship.ShipClass;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class MarinesSectionTest {

	private MarinesSection ms;

	@Test
	public void testClear() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X,
				MarinesCompartment.DECK);

		ms.clear(Player.PASADENA, MarinesCompartment.DECK);

		assertEquals(0, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.BOTH));
		assertEquals(CommanderState.NOT_THERE, ms.getCommanderState(Player.PASADENA, MarinesCompartment.DECK));
	}

	@Test
	public void testMarinesSectionMarinesSection() {
		MarinesSection ms1 = new MarinesSection(ShipClass.PINSE,
				Player.PASADENA);

		ms = new MarinesSection(ms1);

		assertEquals(40, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.DECK, Commons.READY));
		assertEquals(10, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.BATTERIES, Commons.READY));
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.BATTERIES, Commons.USED));
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.BATTERIES, Commons.USED));
	}

	@Test
	public void testMarinesSectionShipClassPlayer() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);

		assertEquals(40, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.DECK, Commons.READY));
		assertEquals(10, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.BATTERIES, Commons.READY));
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.BATTERIES, Commons.USED));
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA,
				MarinesCompartment.BATTERIES, Commons.USED));
	}

	@Ignore
	public void testMoveMarines() {
		fail("Not yet implemented");
	}

	@Test
	public void testMoveCommanderFromShipX() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X,
				MarinesCompartment.DECK);
		
		assertEquals(CommanderState.USED, ms.getCommanderState(Player.PASADENA, MarinesCompartment.DECK));
	}

	@Test
	public void testMoveCommanderToShipX() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X,
				MarinesCompartment.DECK);
		ms.prepareForNewTurn();
		
		ms.moveCommander(Player.PASADENA, MarinesCompartment.DECK, MarinesCompartment.SHIP_X);
		
		assertEquals(CommanderState.NOT_THERE, ms.getCommanderState(Player.PASADENA, MarinesCompartment.DECK));
	}

	@Test
	public void testMoveCommanderWithinShip() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X,
				MarinesCompartment.DECK);
		ms.prepareForNewTurn();
		
		ms.moveCommander(Player.PASADENA, MarinesCompartment.DECK, MarinesCompartment.INMOVE);
		
		assertEquals(CommanderState.NOT_THERE, ms.getCommanderState(Player.PASADENA, MarinesCompartment.DECK));
		assertEquals(CommanderState.USED, ms.getCommanderState(Player.PASADENA, MarinesCompartment.INMOVE));
	}

	@Test
	public void testKillMarinesReady() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		
		Object[] ret = ms.killMarines(Player.PASADENA, MarinesCompartment.DECK, 20, KillingMode.WITHOUT_COMMANDER);
		
		int killed = (Integer) ret[0];
		KillingMode commanderKilled = (KillingMode) ret[1];
		
		assertEquals(20, killed);
		assertEquals(KillingMode.WITHOUT_COMMANDER, commanderKilled);
	}

	@Test
	public void testKillMarinesReadyOverkill() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		
		Object[] ret = ms.killMarines(Player.PASADENA, MarinesCompartment.DECK, 50, KillingMode.WITHOUT_COMMANDER);
		
		int killed = (Integer) ret[0];
		KillingMode commanderKilled = (KillingMode) ret[1];
		
		assertEquals(40, killed);
		assertEquals(KillingMode.WITHOUT_COMMANDER, commanderKilled);
	}

	@Test
	public void testKillMarinesBoth() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveMarines(Player.PASADENA, MarinesCompartment.DECK, MarinesCompartment.DECK, 10);
		
		Object[] ret = ms.killMarines(Player.PASADENA, MarinesCompartment.DECK, 35, KillingMode.WITHOUT_COMMANDER);
		
		int killed = (Integer) ret[0];
		KillingMode commanderKilled = (KillingMode) ret[1];
		
		assertEquals(35, killed);
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.READY));
		assertEquals(5, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.USED));
		assertEquals(KillingMode.WITHOUT_COMMANDER, commanderKilled);
	}

	@Test
	public void testKillMarinesWithCommanderNotKilled() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X, MarinesCompartment.DECK);
		
		Object[] ret = ms.killMarines(Player.PASADENA, MarinesCompartment.DECK, 40, KillingMode.WITH_COMMANDER);
		
		int killed = (Integer) ret[0];
		KillingMode commanderKilled = (KillingMode) ret[1];
		
		assertEquals(40, killed);
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.READY));
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.USED));
		assertEquals(KillingMode.WITHOUT_COMMANDER, commanderKilled);
	}

	@Test
	public void testKillMarinesWithCommanderKilled() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X, MarinesCompartment.DECK);
		
		Object[] ret = ms.killMarines(Player.PASADENA, MarinesCompartment.DECK, 41, KillingMode.WITH_COMMANDER);
		
		int killed = (Integer) ret[0];
		KillingMode commanderKilled = (KillingMode) ret[1];
		
		assertEquals(40, killed);
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.READY));
		assertEquals(0, ms.getMarinesNumber(Player.PASADENA, MarinesCompartment.DECK, Commons.USED));
		assertEquals(KillingMode.WITH_COMMANDER, commanderKilled);
	}

	@Test
	public void testIsCommanderOnboardSuccess() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);

		assertFalse(ms.isCommanderOnboard(Player.PASADENA));
	}

	@Test
	public void testIsCommanderOnboardFailure() {
		ms = new MarinesSection(ShipClass.PINSE, Player.PASADENA);
		ms.moveCommander(Player.PASADENA, MarinesCompartment.SHIP_X,
				MarinesCompartment.DECK);

		assertTrue(ms.isCommanderOnboard(Player.PASADENA));
	}

	@Ignore
	public void testUseMarines() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testPrepareForNewTurn() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testWriteToStream() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testReadFromStream() {
		fail("Not yet implemented");
	}

}
