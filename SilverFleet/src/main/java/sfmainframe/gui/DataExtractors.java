package sfmainframe.gui;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import sfmainframe.Commons;
import sfmainframe.MainBoard;
import sfmainframe.Player;
import sfmainframe.PlayerClass;
import sfmainframe.ship.ShallowAttempt;
import sfmainframe.ship.Ship;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public final class DataExtractors {

	private DataExtractors() {
	}


	public static String getShipAttemptsUsedString(Ship ship) {
		/*
		 * funkcja sluzaca do przeslania niezbednych danych do tworzenia GUI
		 * (uzyte proby zejscia z mielizny w zakladce statystyk)
		 */
		String str = "";

		if (ship.isEscapeAttemptUsed(ShallowAttempt.DROP_SILVER))
			str += "s";
		if (ship.isEscapeAttemptUsed(ShallowAttempt.DROP_CANNONS))
			str += "c";
		if (ship.isEscapeAttemptUsed(ShallowAttempt.PULL_ANCHOR))
			str += "a";
		if (ship.isEscapeAttemptUsed(ShallowAttempt.TOW_BY_ONE))
			str += "t";
		if (ship.isEscapeAttemptUsed(ShallowAttempt.TOW_BY_BOATS))
			str += "b";

		if (str.length() > 0)
			return str;
		else
			return "none";
	}


	public static Object[][] getShipMarinesArray(Ship ship, int part) {
		/*
		 * funkcja sluzaca do przeslania niezbednych danych do tworzenia GUI
		 * (tabele marynarzy w zakladce statystyk)
		 */

		Object[][] arr = new Object[4][4];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				arr[i][j] = new Object();
				arr[i][j] = null;
			}

		for (int i = 0; i < 4; i++) {
			arr[0][i] = String.valueOf(ship.getMarinesNumber(Player.valueOf(i + part * 4), MarinesCompartment.DECK,
					Commons.READY))
					+ "|"
					+ String.valueOf(ship.getMarinesNumber(Player.valueOf(i + part * 4), MarinesCompartment.DECK,
							Commons.USED));
		}

		for (int i = 0; i < 4; i++) {
			arr[1][i] = String.valueOf(ship.getMarinesNumber(Player.valueOf(i + part * 4), MarinesCompartment.INMOVE,
					Commons.READY))
					+ "|"
					+ String.valueOf(ship.getMarinesNumber(Player.valueOf(i + part * 4), MarinesCompartment.INMOVE,
							Commons.USED));
		}

		for (int i = 0; i < 4; i++) {
			arr[2][i] = String.valueOf(ship.getMarinesNumber(Player.valueOf(i + part * 4),
					MarinesCompartment.BATTERIES, Commons.READY))
					+ "|"
					+ String.valueOf(ship.getMarinesNumber(Player.valueOf(i + part * 4), MarinesCompartment.BATTERIES,
							Commons.USED));
		}

		for (int i = 0; i < 4; i++) {
			// MarinesCompartment comp = MarinesCompartment.NONE;
			String str = "";
			CommanderState state = CommanderState.NOT_THERE;
			if (ship.getCommanderState(Player.valueOf(i + part * 4), MarinesCompartment.DECK) != CommanderState.NOT_THERE) {
				state = ship.getCommanderState(Player.valueOf(i + part * 4), MarinesCompartment.DECK);
				str = "deck";
			}
			if (ship.getCommanderState(Player.valueOf(i + part * 4), MarinesCompartment.INMOVE) != CommanderState.NOT_THERE) {
				state = ship.getCommanderState(Player.valueOf(i + part * 4), MarinesCompartment.DECK);
				str = "move";
			}
			if (ship.getCommanderState(Player.valueOf(i + part * 4), MarinesCompartment.BATTERIES) != CommanderState.NOT_THERE) {
				state = ship.getCommanderState(Player.valueOf(i + part * 4), MarinesCompartment.DECK);
				str = "batt";
			}

			if (str.length() == 0)
				arr[3][i] = "n/t";
			else {
				switch (state) {
				case IMPRISONED:
					arr[3][i] = "i (" + str + ")";
					break;
				case READY:
					arr[3][i] = "u (" + str + ")";
					break;
				case USED:
					arr[3][i] = "r (" + str + ")";
					break;
				}
			}
		}

		return arr;
	}


	public static String getShipCoupledString(Ship ship) {
		/*
		 * funkcja sluzaca do przeslania niezbednych danych do tworzenia GUI
		 * (sczepione statki w zakladce statystyk)
		 */

		String str = "";
		Set<Ship> set = ship.getShipsCoupled().keySet();
		Iterator<Ship> it = set.iterator();
		
		while (it.hasNext()) {
			Ship s = (Ship) it.next();
			str += String.valueOf(s.getID()) + ",";
		}
		str = str.substring(0, str.length());
		
		if (str.length() > 0)
			return str;
		else
			return "none";
	}


	public static String getEnemyGroups(Ship ship, PlayerClass pclass, MarinesCompartment location) {
		/*
		 * #rv: ([id1] [id2] #[idx] [idy] [idz] #...#) spacja - wystepuje po
		 * kazdym ID gracza hash - wystepuje po ID graczy nalezacych do danej
		 * grupy
		 */

		String groups = "";
		boolean[] checked = { false, false, false, false, false, false, false, false }; // już
																						// sprawdzeni
																						// gracze
		boolean added = false;

		for (Player plr : Player.values()) {
			if (plr == Player.NONE || plr == pclass.getIdentity())
				continue;
			if (checked[plr.ordinal()])
				continue;

			added = false;

			if (!pclass.isAlly(plr)) {
				if (ship.getMarinesNumber(plr, location, Commons.BOTH) > 0) {
					groups += plr.toString() + " ";
					added = true;
				}

				checked[plr.ordinal()] = true;

				for (Player plr2 : Player.values()) {
					if (plr2 == Player.NONE || plr2 == pclass.getIdentity() || plr2 == plr)
						continue;
					if (checked[plr2.ordinal()])
						continue;

					if (pclass.isAlly(plr2)) {
						if (ship.getMarinesNumber(plr, location, Commons.BOTH) > 0) {
							groups += plr2.toString() + " ";
							added = true;
						}
						checked[plr2.ordinal()] = true;
					}
				}
				if (added)
					groups += "#";
			}

			checked[plr.ordinal()] = true;
		}

		return groups;
	}


	public static String getCompartmentGroups(Ship ship, MarinesCompartment location) {
		/*
		 * rv: ([id1] [id2]#...#[idx][idy]#) (id graczy oddzielone spacjami,
		 * grupy oddzielone haszami
		 */

		String groups = "";

		boolean[] checked = { false, false, false, false, false, false, false, false }; // już
																						// sprawdzeni
																						// gracze

		for (Player plr : Player.values()) {
			if (plr == Player.NONE || checked[plr.ordinal()])
				continue;

			if (ship.getMarinesNumber(plr, location, Commons.BOTH) > 0) {
				checked[plr.ordinal()] = true;
				groups += plr.toString() + " ";

				for (Player plr2 : Player.values()) {
					if (plr2 == Player.NONE || plr2 == plr || checked[plr2.ordinal()])
						continue;

					if (MainBoard.game.getPlayer(plr).isAlly(plr2)
							&& ship.getMarinesNumber(plr, location, Commons.BOTH) > 0) {
						checked[plr2.ordinal()] = true;
						groups += plr2.toString() + " ";
					}
				}

				groups += "#";
			}
		}

		return groups;
	}
}
