package sfmainframe;

public class ReusableElement {

	private int ready;
	private int used;

	public ReusableElement() {
		clear();
	}

	public ReusableElement(ReusableElement re) {
		copy(re);
	}

	public void copy(ReusableElement re) {
		ready = re.ready;
		used = re.used;
	}

	public void use(int number) {
		if (number < 0 || ready < number)
			throw new IllegalArgumentException("not enough elements to use");

		ready -= number;
		used += number;
	}

	public void refresh() {
		ready += used;
		used = 0;
	}

	public int getReady() {
		return ready;
	}

	public int getUsed() {
		return used;
	}

	public void setReady(int number) {
		assert (number > 0);
		ready = number;
	}

	public void setUsed(int number) {
		assert (number > 0);
		used = number;
	}

	public void modifyReady(int number) {
		if (getReady() + number < 0)
			throw new IllegalArgumentException(
					"number of elements after modification must be non-negative");

		ready += number;
	}

	public void modifyUsed(int number) {
		if (getUsed() + number < 0)
			throw new IllegalArgumentException(
					"number of elements after modification must be non-negative");

		used += number;
	}

	public int getTotal() {
		return ready + used;
	}

	public void clear() {
		ready = 0;
		used = 0;
	}
}
