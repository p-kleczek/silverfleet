package silverfleet.ship;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import sfmainframe.ship.CargoHold;
import sfmainframe.ship.cargo.CargoType;

public class CargoHoldTest {

	private CargoHold ch;
	
	@Test
	public void testCargoHoldInt() {
		ch = new CargoHold(50);
		assertEquals(50, ch.getFreeSpace());
	}

	@Ignore
	public void testCargoHoldCargoHold() {
		fail("Not yet implemented");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetLoadNotLoadable() {
		ch = new CargoHold(0);
		
		ch.getLoad(CargoType.NONE);
	}

	@Test
	public void testLoad() {
		ch = new CargoHold(50);
		
		ch.load(CargoType.SILVER, 10);
		
		assertEquals(10, ch.getLoad(CargoType.SILVER));
		assertEquals(40, ch.getFreeSpace());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLoadTooMuch() {
		ch = new CargoHold(5);
		
		ch.load(CargoType.SILVER, 10);
	}

	@Test
	public void testUnloadAll() {
		ch = new CargoHold(50);
		ch.load(CargoType.CANNONS_LIGHT, 5);

		ch.unloadAll(CargoType.CANNONS_LIGHT);
		
		assertEquals(0, ch.getLoad(CargoType.CANNONS_LIGHT));
	}

	@Test
	public void testUnload() {
		ch = new CargoHold(50);
		ch.load(CargoType.SILVER, 5);

		ch.unload(CargoType.SILVER, 3);
		
		assertEquals(2, ch.getLoad(CargoType.SILVER));
		assertEquals(48, ch.getFreeSpace());
	}

	@Ignore
	public void testWriteToFile() {
		fail("Not yet implemented");
	}

	@Ignore
	public void testReadFromFile() {
		fail("Not yet implemented");
	}

}
