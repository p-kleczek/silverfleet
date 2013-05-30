package silverfleet.ship;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sfmainframe.Commons;
import sfmainframe.ship.CannonSection;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;
import sfmainframe.ship.ShipClass;

public class CannonSectionTest {

	private CannonSection cs;

	@Test
	public void testCannonSection() {
		cs = new CannonSection();

		for (Gun type : Gun.getValues())
			for (GunCompartment location : GunCompartment.getValues()) {
				assertEquals(0,
						cs.getCannonsNumber(location, type, Commons.READY));
				assertEquals(0,
						cs.getCannonsNumber(location, type, Commons.USED));
			}
	}

	@Ignore
	public void testCannonSectionCannonSection() {
		fail("Not yet implemented");
	}

	@Test
	public void testCannonSectionShipClass() {
		cs = new CannonSection(ShipClass.PINSE);

		assertEquals(1, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.READY));
		assertEquals(2, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT,
				Commons.READY));
		assertEquals(2, cs.getCannonsNumber(GunCompartment.SIDE_R, Gun.LIGHT,
				Commons.READY));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.STERN, Gun.LIGHT,
				Commons.READY));

		assertEquals(0, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.USED));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT,
				Commons.USED));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_R, Gun.LIGHT,
				Commons.USED));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.STERN, Gun.LIGHT,
				Commons.USED));
	}

	@Test
	public void testClear() {
		cs = new CannonSection(ShipClass.PINSE);
		cs.clear();

		assertEquals(0, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.READY));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT,
				Commons.READY));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_R, Gun.LIGHT,
				Commons.READY));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.STERN, Gun.LIGHT,
				Commons.READY));
	}

	@Test
	public void testUseCannon() {
		cs = new CannonSection(ShipClass.PINSE);

		cs.useCannon(GunCompartment.BOW, Gun.LIGHT);

		assertEquals(0, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.READY));
		assertEquals(1, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.USED));
	}

	@Test
	public void testModifyCannonsNumber() {
		cs = new CannonSection();

		cs.modifyCannonsNumber(GunCompartment.BOW, Gun.LIGHT, 1);

		assertEquals(0, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.READY));
		assertEquals(1, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.USED));
	}

	@Test
	public void testDestroyCannonReady() {
		cs = new CannonSection(ShipClass.PINSE);

		cs.destroyCannon(GunCompartment.SIDE_L, Gun.LIGHT, Commons.READY);
		
		assertEquals(1, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.READY));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.USED));
	}

	@Test
	public void testDestroyCannonUsed() {
		cs = new CannonSection();
		cs.modifyCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, 2);

		cs.destroyCannon(GunCompartment.SIDE_L, Gun.LIGHT, Commons.USED);
		
		assertEquals(1, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.USED));
	}
	
	@Test
	public void testDestroyCannonNone() {
		cs = new CannonSection();

		cs.destroyCannon(GunCompartment.SIDE_L, Gun.LIGHT, Commons.USED);
		
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.READY));
		assertEquals(0, cs.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.USED));
	}	

	@Test
	public void testGetTotalCannonNumber() {
		cs = new CannonSection(ShipClass.PINSE);
		assertEquals(5, cs.getTotalCannonNumber());
	}

	@Test
	public void testPrepareForNewTurn() {
		cs = new CannonSection();
		cs.modifyCannonsNumber(GunCompartment.BOW, Gun.LIGHT, 3);

		cs.prepareForNewTurn();

		assertEquals(0, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.USED));
		assertEquals(3, cs.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT,
				Commons.READY));
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
