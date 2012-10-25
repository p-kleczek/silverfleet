package sfmainframe;

import java.util.Random;

public abstract class Dice {

	private static Random random = new Random();


	public static int roll() {
		return random.nextInt(6) + 1;
	}
}
