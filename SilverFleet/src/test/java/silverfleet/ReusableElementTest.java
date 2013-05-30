package silverfleet;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import sfmainframe.ReusableElement;

public class ReusableElementTest {

	private ReusableElement elem = new ReusableElement();
	
	@Test
	public void testReusableElement() {
		assertEquals(0, elem.getReady());
		assertEquals(0, elem.getUsed());
	}

	@Test
	public void testReusableElementReusableElement() {
		ReusableElement e = new ReusableElement();
		e.setReady(5);
		e.setUsed(3);
		
		elem = new ReusableElement(e);
		
		assertEquals(5, elem.getReady());
		assertEquals(3, elem.getUsed());
	}

	@Test
	public void testCopy() {
		ReusableElement e = new ReusableElement();
		e.setReady(5);
		e.setUsed(3);
		
		elem.copy(e);
		assertEquals(5, elem.getReady());
		assertEquals(3, elem.getUsed());
	}

	@Test
	public void testUse() {
		elem.setReady(2);
		elem.setUsed(1);

		elem.use(1);
		
		assertEquals(1, elem.getReady());
		assertEquals(2, elem.getUsed());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testUseTooMany() {
		elem.setReady(2);

		elem.use(3);
	}

	@Test
	public void testRefresh() {
		elem.setReady(2);
		elem.setUsed(1);

		elem.refresh();
		
		assertEquals(3, elem.getReady());
		assertEquals(0, elem.getUsed());
	}

	@Test
	public void testModifyReady() {
		elem.setReady(2);

		elem.modifyReady(2);
		
		assertEquals(4, elem.getReady());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testModifyReadyTooMuch() {
		elem.setReady(2);

		elem.modifyReady(-3);
	}

	@Test
	public void testModifyUsed() {
		elem.setUsed(1);

		elem.modifyUsed(2);

		assertEquals(3, elem.getUsed());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testModifyUsedTooMuch() {
		elem.setUsed(1);

		elem.modifyUsed(-2);
	}

	@Test
	public void testGetTotal() {
		elem.setReady(2);
		elem.setUsed(1);

		assertEquals(3, elem.getTotal());
	}

	@Test
	public void testClear() {
		elem.setReady(2);
		elem.setUsed(1);

		elem.clear();
		
		assertEquals(0, elem.getReady());
		assertEquals(0, elem.getUsed());
	}

}
