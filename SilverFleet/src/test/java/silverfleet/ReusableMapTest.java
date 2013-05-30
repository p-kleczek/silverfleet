package silverfleet;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import sfmainframe.ReusableElement;
import sfmainframe.ReusableMap;

public class ReusableMapTest {

	private enum TestEnum {
		ELEM1, ELEM2
	}

	private ReusableMap<TestEnum> rm = new ReusableMap<ReusableMapTest.TestEnum>(
			TestEnum.class, Arrays.asList(TestEnum.values()));

	@Test
	public void testReusableMap() {
		// All values of the enum class should be included.
		rm.getElement(TestEnum.ELEM1);
		rm.getElement(TestEnum.ELEM2);
	}

	@Test
	public void testUse() {
		rm.getElement(TestEnum.ELEM1).setReady(2);

		rm.use(TestEnum.ELEM1, 1);

		assertEquals(1, rm.getElement(TestEnum.ELEM1).getReady());
		assertEquals(1, rm.getElement(TestEnum.ELEM1).getUsed());
	}

	@Test
	public void testRefresh() {
		rm.getElement(TestEnum.ELEM1).setReady(1);
		rm.getElement(TestEnum.ELEM1).setUsed(2);

		rm.refresh();

		assertEquals(3, rm.getElement(TestEnum.ELEM1).getReady());
		assertEquals("all used elements should be refreshed", 0,
				rm.getElement(TestEnum.ELEM1).getUsed());
	}

	@Test
	public void testGetElement() {
		rm.getElement(TestEnum.ELEM1).setReady(1);
		rm.getElement(TestEnum.ELEM1).setUsed(2);

		ReusableElement e = rm.getElement(TestEnum.ELEM1);

		assertEquals(1, e.getReady());
		assertEquals(2, e.getUsed());
	}

	@Test
	public void testClear() {
		rm.getElement(TestEnum.ELEM1).setReady(1);
		rm.getElement(TestEnum.ELEM1).setUsed(2);

		rm.clear();

		assertEquals("all used elements should be zeroed", 0,
				rm.getElement(TestEnum.ELEM1).getReady());
		assertEquals("all used elements should be zeroed", 0,
				rm.getElement(TestEnum.ELEM1).getUsed());
	}

}
