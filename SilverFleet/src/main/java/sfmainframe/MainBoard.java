package sfmainframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import sfmainframe.board.Board;
import sfmainframe.board.Hex;
import sfmainframe.board.Terrain;
import sfmainframe.gameplay.Game;
import sfmainframe.gameplay.HappinessAction;
import sfmainframe.gameplay.MovesQueueCode;
import sfmainframe.gameplay.Ships;
import sfmainframe.gameplay.Stage;
import sfmainframe.gui.AboutPanel;
import sfmainframe.gui.DataExtractors;
import sfmainframe.gui.DisplayMode;
import sfmainframe.gui.Tabs;
import sfmainframe.gui.UpdateMode;
import sfmainframe.ship.AimPart;
import sfmainframe.ship.BoardingFirstTurn;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;
import sfmainframe.ship.Happiness;
import sfmainframe.ship.Parameter;
import sfmainframe.ship.ShallowAttempt;
import sfmainframe.ship.Ship;
import sfmainframe.ship.cargo.CargoDestination;
import sfmainframe.ship.cargo.CargoType;
import sfmainframe.ship.cargo.HandlingPartner;
import sfmainframe.ship.marines.CommanderState;
import sfmainframe.ship.marines.MarinesCompartment;

public class MainBoard {

	public static Game game;
	private static JFrame f;

	private static boolean stillAvailable; // udalo sie przywrocic poprzednio
											// wybrane ustawienie (true - tak)
	private static boolean updateFinished = false; // zmienna kontrolna,
													// zapobiega null-exeptions

	static BoardPanel boardPanel;
	static JTextArea messageBox;
	static JScrollPane scrollPane;
	static JTabbedPane tabbedPane;
	public static BetweenTurnsDialog betweenTurnsDialog;

	// menu
	private static JMenuBar menuBar;

	private static JMenu gameMenu;
	private static JMenuItem saveGameMenuItem, loadGameMenuItem, exitMenuItem;

	private static JMenu playerMenu;
	private static JMenuItem alliesMenuItem;

	private static JMenu helpMenu;
	private static JMenuItem aboutMenuItem;

	// header panel
	static JPanel headerPanel;
	static JLabel headerLabel;

	// corner panel
	static JPanel cornerPanel;
	static JLabel happinessLabel, remainingTimeLabel;
	static JButton endTurnButton, useHappinessButton, acceptRollButton;

	// tabbedPane - elements
	// Tab: movement
	static JPanel movementTabPanel;
	static JLabel transferLabel, angleLabel, distanceLabel, towingLabel, towIdLabel, shallowLabel;
	static JButton angleButton, distanceButton, towButton, throwTowButton, makeAttemptButton;
	static JSpinner angleSpinner, distanceSpinner;
	static JComboBox<String> towComboBox;
	static JRadioButton throwGunsRadioButton, pullOnAnchorRadioButton, throwSilverRadioButton, towWithBoatsRadioButton,
			towOneRadioButton;
	static ButtonGroup shallowGroup;
	static JSeparator movSeparator1, movSeparator2;

	// Tab: marines
	static JPanel marinesTabPanel;
	static JLabel sourceLabel, numberLabel, destinationLabel, boardingLabel, enemyGroupLabel;
	static JRadioButton sourceDeckRadioButton, sourceInMoveRadioButton, sourceBatteriesRadioButton,
			destinationBatteriesRadioButton, destinationInMoveRadioButton, destinationDeckRadioButton,
			destinationShipRadioButton;
	static JCheckBox marinesCheckBox, commanderCheckBox;
	static JSpinner marinesNumberSpinner;
	static JSeparator marinesSeparator1, marinesSeparator2, marinesSeparator3, marinesSeparator4;
	static JComboBox<String> destinationShipComboBox, enemyGroupComboBox;
	static JButton moveMarinesButton, surrenderButton, attackButton, sabotageButton, escapeButton;
	static ButtonGroup sourceMarinesGroup, destinationMarinesGroup;

	static JDialog surrenderDialog;
	static JOptionPane optionPane;
	static JSpinner surrenderSpinner;

	static SabotageUnderDeckDialog sabotageUnderDeckDialog;

	// Tab: Shoot

	static JPanel shootTabPanel;
	static JLabel compartmentShootLabel, firingGunShootLabel, targetShipShootLabel, aimAtShootLabel,
			aimedGunShootLabel;
	static JRadioButton bowShootRadioButton, leftSideShootRadioButton, rightSideShootRadioButton,
			sternShootRadioButton, lightOwnShootRadioButton, mediumOwnShootRadioButton, heavyOwnShootRadioButton,
			riggingShootRadioButton, cannonShootRadioButton, hullShootRadioButton, lightAimedShootRadioButton,
			mediumAimedShootRadioButton, heavyAimedShootRadioButton;
	static JSeparator shootSeparator1, shootSeparator2, shootSeparator3;
	static JComboBox<String> targetShipShootComboBox;
	static JButton shootShootButton;
	static ButtonGroup compartmentShootButtonGroup, ownTypeShootButtonGroup, aimAtShootButtonGroup,
			aimedTypeShootButtonGroup;

	// Tab: Cargo
	static JPanel cargoTabPanel;
	static JLabel shipCargoLabel, fromCargoLabel, toCargoLabel, typeCargoLabel, compartmentCargoLabel,
			targetCompartmentCargoLabel, quantityCargoLabel;
	static JComboBox<String> shipCargoComboBox;
	static JRadioButton cargoFromCargoRadioButton, batteriesFromCargoRadioButton, cargoToCargoRadioButton,
			batteriesToCargoRadioButton, silverCargoRadioButton, lightCargoRadioButton, mediumCargoRadioButton,
			bowCargoRadioButton, leftCargoRadioButton, rightCargoRadioButton, sternCargoRadioButton,
			targetBowCargoRadioButton, targetLeftCargoRadioButton, targetRightCargoRadioButton,
			targetSternCargoRadioButton;
	static JButton handleCargoButton, uncoupleCargoButton, setExplosivesCargoButton;
	static JSpinner quantityCargoSpinner;
	static JSeparator cargoSeparator1, cargoSeparator2, cargoSeparator3;
	static ButtonGroup fromCargoButtonGroup, toCargoButtonGroup, typeCargoButtonGroup, compartmentCargoButtonGroup,
			targetCompartmentCargoButtonGroup;

	// Tab: Statistics

	static JPanel statsTabPanel;
	static JLabel idStatsLabel, ownerStatsLabel, classStatsLabel, positionStatsLabel, rotationStatsLabel,
			moveOverStatsLabel, actionsOverStatsLabel, hullStatsLabel, helmStatsLabel, mastStatsLabel,
			isWreckStatsLabel, isImmobilizedStatsLabel, isExplosiveStatsLabel, tugStatsLabel, towedStatsLabel,
			coupledStatsLabel, silverLoadStatsLabel, lightCannonsLoadStatsLabel, mediumCannonsLoadStatsLabel,
			bftStatsLabel, teauStatsLabel, attemptsUsedStatsLabel, happinessStatsLabel, happinessSinkStatsLabel,
			happinessBoardingStatsLabel;
	static JTable cannonsAStatsTable, cannonsBStatsTable, marinesAStatsTable, marinesBStatsTable;

	// end of tabbedPane elements

	private static Ship ship = null;
	private static ShallowAttempt selectedEscapeType = ShallowAttempt.DROP_CANNONS;
	private static Integer towShip = null;

	private static MarinesCompartment selectedMarinesSource = MarinesCompartment.NONE;
	private static MarinesCompartment selectedMarinesDestination = MarinesCompartment.NONE;

	private static GunCompartment selectedCompartmentShoot = GunCompartment.NONE;
	private static Gun selectedOwnTypeShoot = Gun.NONE;
	private static Gun selectedAimedTypeShoot = Gun.NONE;
	private static AimPart selectedAimTypeShoot = AimPart.NONE;
	private static int targetDistance = 0;

	private static Object previousShip = null;
	private static CargoDestination selectedFromDestinationCargo = CargoDestination.NONE;
	private static CargoDestination selectedToDestinationCargo = CargoDestination.NONE;
	private static CargoType selectedCargoTypeCargo = CargoType.NONE;
	private static GunCompartment selectedSourceGunCompartmentCargo = GunCompartment.NONE;
	private static GunCompartment selectedTargetGunCompartmentCargo = GunCompartment.NONE;
	private static HandlingPartner handlingPartner = HandlingPartner.NONE;

	// ---- Allies Panel
	private static JFrame alliesFrame;
	private static JPanel alliesPanel;

	private static JLabel allyAlliesLabel, delacroixAlliesLabel, discasterAlliesLabel, elmethAlliesLabel,
			hampshireAlliesLabel, leppoAlliesLabel, pasadenaAlliesLabel, pleensyAlliesLabel, sidoniaAlliesLabel;

	private static JCheckBox delacroixAlliesCheckBox, discasterAlliesCheckBox, elmethAlliesCheckBox,
			hampshireAlliesCheckBox, leppoAlliesCheckBox, pasadenaAlliesCheckBox, pleensyAlliesCheckBox,
			sidoniaAlliesCheckBox;

	private static JButton closeAlliesButton;

	// ---

	// ---- About Panel

	private static JFrame aboutFrame;
	private static AboutPanel aboutPanel;

	// ---

	// timer
	private static HappinessAction happinessAction = HappinessAction.NONE;

	private static DisplayMode boardPanelMode = DisplayMode.DEPLOY_MODE;


	public static HappinessAction getHappinessAction() {
		return happinessAction;
	}


	public static void setHappinessAction(HappinessAction action) {
		happinessAction = action;
	}


	public static void setRemainingTimeLabelText(String text) {
		remainingTimeLabel.setText(text);
	}


	public static int getSelectedShipID() {
		return ship.getID();
	}


	public static MarinesCompartment getSelectedMarinesSource() {
		return selectedMarinesSource;
	}
	
	public static BoardPanel getBoardPanel() {
	    return boardPanel;
	}


	public static void setSelectedShip(Ship _ship, Tabs tab) {
		tabbedPane.setSelectedIndex(tab.ordinal());
		ship = _ship;

		selectedEscapeType = ShallowAttempt.DROP_CANNONS;

		selectedMarinesSource = MarinesCompartment.NONE;
		selectedMarinesDestination = MarinesCompartment.NONE;

		selectedCompartmentShoot = GunCompartment.NONE;
		selectedOwnTypeShoot = Gun.NONE;
		selectedAimedTypeShoot = Gun.NONE;
		selectedAimTypeShoot = AimPart.NONE;
		targetDistance = 0;

		selectedFromDestinationCargo = CargoDestination.NONE;
		selectedToDestinationCargo = CargoDestination.NONE;
		selectedCargoTypeCargo = CargoType.NONE;
		selectedSourceGunCompartmentCargo = GunCompartment.NONE;
		selectedTargetGunCompartmentCargo = GunCompartment.NONE;

		updateMovementTab(UpdateMode.DEFAULT);
		updateMarinesTab(UpdateMode.DEFAULT);
		updateShootTab(UpdateMode.DEFAULT);
		updateCargoTab(UpdateMode.DEFAULT);
		updateStatsTab();

		makeHeaderLabel();
		boardPanel.repaint();
	}


	private static void updateMovementTab(UpdateMode mode) {
		
		
		boolean tow = false;

		updateFinished = false;

		angleSpinner.setEnabled(false);
		angleButton.setEnabled(false);
		distanceSpinner.setEnabled(false);
		distanceButton.setEnabled(false);

		towComboBox.setEnabled(false);
		towButton.setEnabled(false);
		throwTowButton.setEnabled(false);

		throwGunsRadioButton.setEnabled(false);
		pullOnAnchorRadioButton.setEnabled(false);
		throwSilverRadioButton.setEnabled(false);
		towWithBoatsRadioButton.setEnabled(false);
		towOneRadioButton.setEnabled(false);
		makeAttemptButton.setEnabled(false);

		distanceSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
		angleSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
		towComboBox.removeAllItems();

		// par. 12.9
		if (ship == null
				|| !Ships.checkIfPlayerControlsLocation(ship, game.getCurrentPlayer(),
								MarinesCompartment.DECK, false)
				|| ship.isParameter(Parameter.ACTIONS_OVER)) {
			updateFinished = true;
			return;
		}
		// --

		if (game.getStage() == Stage.DEPLOYMENT) {
			angleSpinner.setModel(new SpinnerNumberModel(0, -3, 3, 1));
			angleSpinner.setEnabled(true);
			angleButton.setEnabled(true);
			updateFinished = true;
			return;
		} else if (game.getStage() != Stage.PLAYERS_MOVES) {
			updateFinished = true;
			return;
		}

		distanceSpinner.setModel(new SpinnerNumberModel(0, 0, Ships.getShipDistanceToMove(ship), 1));
		angleSpinner.setModel(new SpinnerNumberModel(0, Ships.checkAngleToRotate(ship).getLowerBound(), Ships
				.checkAngleToRotate(ship).getUpperBound(), 1));

		// lista mozliwych do holowania okretow
		towComboBox.removeAllItems();
		for (Ship s : game.getShips()) {
			if (s != ship && Ships.checkIfTowable(ship, s))
				towComboBox.addItem(s.getCaption());
		}

		// zmiany, gdy okret juz kogos holuje
		if (ship.getTowOther() != null) {
			towComboBox.removeAllItems();
			towComboBox.addItem(ship.getTowOther() + ", "
					+ ship.getTowOther().getShipClass().toString());
			tow = true;
		}
		if (ship.getTowedBy() != null) {
			towComboBox.removeAllItems();
			towComboBox.addItem(ship.getTowedBy() + ", "
					+ ship.getTowedBy().getShipClass().toString());
			tow = true;
		}

		if (Ships.checkIfPlayerControlsLocation(ship, game.getCurrentPlayer(), MarinesCompartment.DECK, false)) {
			angleButton.setEnabled(true);
			distanceButton.setEnabled(true);
			angleSpinner.setEnabled(true);
			distanceSpinner.setEnabled(true);

			if (tow)
				throwTowButton.setEnabled(true);
			else {
				towComboBox.setEnabled(true);
				if (towComboBox.getItemCount() > 0)
					towButton.setEnabled(true);
			}
		}

		int available = 0;

		if (ship.isParameter(Parameter.IS_IMMOBILIZED)
				&& ship.getOwner() == game.getCurrentPlayer()) {
			if (Ships.checkIfEscapeAttemptPossible(ship, ShallowAttempt.DROP_CANNONS, null)) {
				throwGunsRadioButton.setEnabled(true);
				available++;
			}

			if (Ships.checkIfEscapeAttemptPossible(ship, ShallowAttempt.PULL_ANCHOR, null)) {
				pullOnAnchorRadioButton.setEnabled(true);
				available++;
			}

			if (Ships.checkIfEscapeAttemptPossible(ship, ShallowAttempt.DROP_SILVER, null)) {
				throwSilverRadioButton.setEnabled(true);
				available++;
			}

			if (Ships.checkIfEscapeAttemptPossible(ship, ShallowAttempt.TOW_BY_BOATS, null)) {
				towWithBoatsRadioButton.setEnabled(true);
				available++;
			}
		}

		if (!ship.isParameter(Parameter.IS_IMMOBILIZED)) {
			if (isTowOneAttemptPossible()) {
				towOneRadioButton.setEnabled(true);
				available++;
			}
		}

		if (throwSilverRadioButton.isEnabled())
			throwSilverRadioButton.doClick();
		else if (throwGunsRadioButton.isEnabled())
			throwGunsRadioButton.doClick();
		else if (pullOnAnchorRadioButton.isEnabled())
			pullOnAnchorRadioButton.doClick();
		else if (towWithBoatsRadioButton.isEnabled())
			towWithBoatsRadioButton.doClick();
		else if (towOneRadioButton.isEnabled())
			towOneRadioButton.doClick();
		else
			throwSilverRadioButton.setSelected(true);

		if (available > 0)
			makeAttemptButton.setEnabled(true);

		updateFinished = true;
	}


	private static void updateMarinesTab(UpdateMode mode) {
		updateFinished = false;

		sourceDeckRadioButton.setEnabled(false);
		sourceInMoveRadioButton.setEnabled(false);
		sourceBatteriesRadioButton.setEnabled(false);

		marinesCheckBox.setEnabled(false);
		marinesNumberSpinner.setEnabled(false);

		commanderCheckBox.setEnabled(false);

		destinationBatteriesRadioButton.setEnabled(false);
		destinationInMoveRadioButton.setEnabled(false);
		destinationDeckRadioButton.setEnabled(false);
		destinationShipRadioButton.setEnabled(false);
		destinationShipComboBox.setEnabled(false);

		moveMarinesButton.setEnabled(false);
		surrenderButton.setEnabled(false);

		enemyGroupComboBox.setEnabled(false);

		attackButton.setEnabled(false);
		sabotageButton.setEnabled(false);
		escapeButton.setEnabled(false);

		enemyGroupComboBox.removeAllItems();
		destinationShipComboBox.removeAllItems();

		// niedostepnosc zakladki "Marines"
		if (ship == null) {
			updateFinished = true;
			return;
		}

		if (!(game.getStage() == Stage.PLAYERS_MOVES ||
				
		(game.getStage() == Stage.BOARDING_ACTIONS || game.getStage() == Stage.BOARDING_MOVEMENTS || game.getStage() == Stage.BOARDING_SABOTAGE)
				&& Ships.checkIfShipBoarded(ship, game.getCurrentPlayer()))) {
			updateFinished = true;
			return;
		}

		// ustawianie dostepnosci przyciskow
		if (ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.DECK, Commons.BOTH) > 0
				|| ship.getCommanderState(game.getCurrentPlayer(), MarinesCompartment.DECK) == CommanderState.READY)
			sourceDeckRadioButton.setEnabled(true);

		if (ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.INMOVE, Commons.BOTH) > 0
				|| ship.getCommanderState(game.getCurrentPlayer(), MarinesCompartment.INMOVE) == CommanderState.READY)
			sourceInMoveRadioButton.setEnabled(true);

		if (ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.BOTH) > 0
				|| ship.getCommanderState(game.getCurrentPlayer(), MarinesCompartment.BATTERIES) == CommanderState.READY)
			sourceBatteriesRadioButton.setEnabled(true);

		if (!sourceDeckRadioButton.isEnabled() && !sourceInMoveRadioButton.isEnabled()
				&& !sourceBatteriesRadioButton.isEnabled()) {
			updateFinished = true;
			return;
		}

		stillAvailable = false;
		switch (selectedMarinesSource) {
		case DECK:
			if (sourceDeckRadioButton.isEnabled()) {
				sourceDeckRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case INMOVE:
			if (sourceInMoveRadioButton.isEnabled()) {
				sourceInMoveRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case BATTERIES:
			if (sourceBatteriesRadioButton.isEnabled()) {
				sourceBatteriesRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		}

		if (!stillAvailable || mode == UpdateMode.DEFAULT) {
			if (sourceDeckRadioButton.isEnabled())
				sourceDeckRadioButton.doClick();
			else if (sourceInMoveRadioButton.isEnabled())
				sourceInMoveRadioButton.doClick();
			else
				sourceBatteriesRadioButton.doClick();
		}

		if (game.getStage() != Stage.BOARDING_ACTIONS && game.getStage() != Stage.BOARDING_SABOTAGE) {
			if (mode == UpdateMode.DEFAULT || mode == UpdateMode.MARINES_TAB_SOURCE) {
				marinesCheckBox.setSelected(false);
				commanderCheckBox.setSelected(false);

				marinesNumberSpinner.setModel(new SpinnerNumberModel(0, 0, ship.getMarinesNumber(
						game.getCurrentPlayer(), selectedMarinesSource, Commons.BOTH), 1));
			}

			if (ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.DECK, Commons.READY) > 0
					|| ship.getCommanderState(game.getCurrentPlayer(), MarinesCompartment.DECK) == CommanderState.READY
					|| ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.INMOVE, Commons.READY) > 0
					|| ship.getCommanderState(game.getCurrentPlayer(), MarinesCompartment.INMOVE) == CommanderState.READY
					|| ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) > 0
					|| ship.getCommanderState(game.getCurrentPlayer(), MarinesCompartment.BATTERIES) == CommanderState.READY) {

				if (ship.getMarinesNumber(game.getCurrentPlayer(), selectedMarinesSource, Commons.READY) > 0) {
					marinesCheckBox.setEnabled(true);

					marinesNumberSpinner.setEnabled(true);
				}

				if (ship.getCommanderState(game.getCurrentPlayer(), selectedMarinesSource) == CommanderState.READY)
					commanderCheckBox.setEnabled(true);
			}

			for (Ship s : game.getShips()) {
				if (s != ship && Ships.checkIfBoardable(ship, game.getCurrentPlayer(), s))
					destinationShipComboBox.addItem(s.getID() + ", " + s.getShipClass());
			}

			 // #CLICK na destination-ship => ustawienie pierwszego dostepnego
			 // pola w comboBox

			// 1.2.1
			switch (selectedMarinesSource) {
			case DECK:
				destinationInMoveRadioButton.setEnabled(true);
				destinationInMoveRadioButton.doClick();
				if (destinationShipComboBox.getItemCount() > 0) {
					destinationShipRadioButton.setEnabled(true);
					destinationShipComboBox.setEnabled(true);
				}
				break;
			case INMOVE:
				destinationDeckRadioButton.setEnabled(true);
				destinationDeckRadioButton.doClick();
				destinationBatteriesRadioButton.setEnabled(true);
				break;
			case BATTERIES:
				destinationInMoveRadioButton.setEnabled(true);
				destinationInMoveRadioButton.doClick();
				break;
			}

			if (marinesCheckBox.isSelected()
					&& (Integer) (marinesNumberSpinner.getValue()) > ship.getMarinesNumber(
							game.getCurrentPlayer(), selectedMarinesSource, Commons.READY) && !commanderCheckBox.isSelected()) {
				surrenderButton.setEnabled(true);

				destinationDeckRadioButton.setEnabled(false);
				destinationInMoveRadioButton.setEnabled(false);
				destinationBatteriesRadioButton.setEnabled(false);
				destinationShipRadioButton.setEnabled(false);
			} else if (marinesCheckBox.isSelected()
					&& (Integer) (marinesNumberSpinner.getValue()) > 0
					&& (Integer) (marinesNumberSpinner.getValue()) <= ship.getMarinesNumber(
							game.getCurrentPlayer(), selectedMarinesSource, Commons.READY) && !commanderCheckBox.isSelected())
				surrenderButton.setEnabled(true);

			if (marinesCheckBox.isSelected()
					&& (Integer) (marinesNumberSpinner.getValue()) > 0
					&& (Integer) (marinesNumberSpinner.getValue()) <= ship.getMarinesNumber(
							game.getCurrentPlayer(), selectedMarinesSource, Commons.READY) || commanderCheckBox.isSelected())
				moveMarinesButton.setEnabled(true);

			// par. 12.8.1
			if (Ships.checkIfBoardingEscapePossible(ship, game.getCurrentPlayer()))
				escapeButton.setEnabled(true);
			// --
		}

		if (game.getStage() == Stage.BOARDING_ACTIONS
				&& ship.getBoardingActionUsed(game.getCurrentPlayer(),
						selectedMarinesSource) == 0) {
			
			String[] groups = DataExtractors.getEnemyGroups(ship,
					game.getPlayer(game.getCurrentPlayer()), selectedMarinesSource).split("#");
			for (int i = 0; i < groups.length; i++) {
				if (!groups[i].isEmpty())
					enemyGroupComboBox.addItem(groups[i]);
			}

			if (enemyGroupComboBox.getItemCount() > 0)
				enemyGroupComboBox.setEnabled(true);
		}

		if (game.getStage() == Stage.BOARDING_SABOTAGE
				&& (Ships.getGroupsNumber(ship, selectedMarinesSource) == 1
						&& ship.getMarinesNumber(game.getCurrentPlayer(), selectedMarinesSource, Commons.BOTH) > 0
						&& ship.getBoardingActionUsed(game.getCurrentPlayer(),
								selectedMarinesSource) < 2 || Ships.getGroupsNumber(ship, selectedMarinesSource) > 1
						&& ship.getMarinesNumber(game.getCurrentPlayer(), selectedMarinesSource, Commons.BOTH) > 0
						&& ship.getBoardingActionUsed(game.getCurrentPlayer(),
								selectedMarinesSource) == 0)) {
			// par. 12.2.3.2 (drugi z powyższych warunków)
			sabotageButton.setEnabled(true);
			updateFinished = true;
		}
	}


	private static void updateShootTab(UpdateMode mode) {
		updateFinished = false;

		GunCompartment previousSelectedCompartmentShoot = selectedCompartmentShoot;
		Gun previousSelectedOwnTypeShoot = selectedOwnTypeShoot;
		Gun previousSelectedAimedTypeShoot = selectedAimedTypeShoot;
		AimPart previousSelectedAimTypeShoot = selectedAimTypeShoot;
		Object previousTargetID = null;

		GunCompartment targetCompartment = GunCompartment.NONE;
		Integer targetID = null;

		stillAvailable = false;

		if (targetShipShootComboBox.getItemCount() > 0)
			previousTargetID = targetShipShootComboBox.getSelectedItem();

		bowShootRadioButton.setEnabled(false);
		leftSideShootRadioButton.setEnabled(false);
		rightSideShootRadioButton.setEnabled(false);
		sternShootRadioButton.setEnabled(false);

		lightOwnShootRadioButton.setEnabled(false);
		mediumOwnShootRadioButton.setEnabled(false);
		heavyOwnShootRadioButton.setEnabled(false);

		riggingShootRadioButton.setEnabled(false);
		cannonShootRadioButton.setEnabled(false);
		hullShootRadioButton.setEnabled(false);

		lightAimedShootRadioButton.setEnabled(false);
		mediumAimedShootRadioButton.setEnabled(false);
		heavyAimedShootRadioButton.setEnabled(false);

		targetShipShootComboBox.removeAllItems();
		targetShipShootComboBox.setEnabled(false);
		shootShootButton.setEnabled(false);

		 // zamiast mechanizmu doClick() konieczność "ręcznego" ustawiania
		 // zaznaczonych przycisków

		if (ship == null
				|| !Ships.checkIfPlayerControlsLocation(ship, game.getCurrentPlayer(),
						MarinesCompartment.BATTERIES, false) || game.getStage() != Stage.PLAYERS_MOVES
				|| ship.isParameter(Parameter.ACTIONS_OVER)) {
			// par. 12.6, 12.12
			updateFinished = true;
			return;
		}

		// par. 10.1, 10.2
		for (GunCompartment comp : GunCompartment.values()) {
			if (comp == GunCompartment.NONE)
				continue;

			if ((ship.getCannonsNumber(comp, Gun.LIGHT, Commons.READY) > 0 && ship.getMarinesNumber(
					game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) >= Gun.LIGHT.getCrewSize())
					||

					(ship.getCannonsNumber(comp, Gun.MEDIUM, Commons.READY) > 0 && ship.getMarinesNumber(
							 game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) >= Gun.MEDIUM.getCrewSize())
					||

					(ship.getCannonsNumber(comp, Gun.HEAVY, Commons.READY) > 0 && ship.getMarinesNumber(
							 game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) >= Gun.HEAVY.getCrewSize())) {
				switch (comp) {
				case BOW:
					bowShootRadioButton.setEnabled(true);
					break;
				case SIDE_L:
					leftSideShootRadioButton.setEnabled(true);
					break;
				case SIDE_R:
					rightSideShootRadioButton.setEnabled(true);
					break;
				case STERN:
					sternShootRadioButton.setEnabled(true);
					break;
				}
			}
		}
		// --

		// brak dzial gotowych do strzalu
		if (!bowShootRadioButton.isEnabled() && !leftSideShootRadioButton.isEnabled()
				&& !rightSideShootRadioButton.isEnabled() && !sternShootRadioButton.isEnabled()) {
			updateFinished = true;
			return;
		}

		// proba wyboru poprzedniego ustawienia przedzialu bojowego
		switch (previousSelectedCompartmentShoot) {
		case BOW:
			if (bowShootRadioButton.isEnabled()) {
				bowShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case SIDE_L:
			if (leftSideShootRadioButton.isEnabled()) {
				leftSideShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case SIDE_R:
			if (rightSideShootRadioButton.isEnabled()) {
				rightSideShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case STERN:
			if (sternShootRadioButton.isEnabled()) {
				sternShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case NONE:
			if (bowShootRadioButton.isEnabled()) {
				bowShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		}

		if (!stillAvailable || mode == UpdateMode.DEFAULT) {
			if (bowShootRadioButton.isEnabled())
				bowShootRadioButton.doClick();
			else if (leftSideShootRadioButton.isEnabled())
				leftSideShootRadioButton.doClick();
			else if (rightSideShootRadioButton.isEnabled())
				rightSideShootRadioButton.doClick();
			else
				sternShootRadioButton.doClick();
		}

		stillAvailable = false;

		if (ship.getCannonsNumber(selectedCompartmentShoot, Gun.LIGHT, Commons.READY) > 0
				&& ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) >= Gun.LIGHT.getCrewSize())
			lightOwnShootRadioButton.setEnabled(true);

		if (ship.getCannonsNumber(selectedCompartmentShoot, Gun.MEDIUM, Commons.READY) > 0
				&& ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) >= Gun.MEDIUM.getCrewSize())
			mediumOwnShootRadioButton.setEnabled(true);

		if (ship.getCannonsNumber(selectedCompartmentShoot, Gun.HEAVY, Commons.READY) > 0
				&& ship.getMarinesNumber(game.getCurrentPlayer(), MarinesCompartment.BATTERIES, Commons.READY) >= Gun.HEAVY.getCrewSize())
			heavyOwnShootRadioButton.setEnabled(true);

		// proba wyboru poprzedniego typu dziala
		if (selectedCompartmentShoot == previousSelectedCompartmentShoot) {
			switch (previousSelectedOwnTypeShoot) {
			case LIGHT:
				if (lightOwnShootRadioButton.isEnabled()) {
					lightOwnShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			case MEDIUM:
				if (mediumOwnShootRadioButton.isEnabled()) {
					mediumOwnShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			case HEAVY:
				if (heavyOwnShootRadioButton.isEnabled()) {
					heavyOwnShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			case NONE:
				if (lightOwnShootRadioButton.isEnabled()) {
					lightOwnShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			}
		}

		if (!stillAvailable || mode == UpdateMode.DEFAULT) {
			if (lightOwnShootRadioButton.isEnabled())
				lightOwnShootRadioButton.doClick();
			else if (mediumOwnShootRadioButton.isEnabled())
				mediumOwnShootRadioButton.doClick();
			else
				heavyOwnShootRadioButton.doClick();
		}

		stillAvailable = false;

		targetShipShootComboBox.setEnabled(true);
		for (Ship s : game.getShips()) {
			int distance = Board.getDistance(ship.getPosition(), s.getPosition());
			// par. 10.3
			if (s != ship && distance > 0 && distance <= selectedOwnTypeShoot.getRange()
					&& game.calculateSourceGunCompartment(ship.getID(), s.getID()) == selectedCompartmentShoot
					&& !game.checkIfObstacleOnBulletPath(ship.getID(), s.getID(), selectedOwnTypeShoot))
				targetShipShootComboBox.addItem(s.getCaption());
			// --
		}

		if (targetShipShootComboBox.getItemCount() == 0) {
			updateFinished = true;
			return;
		}

		targetShipShootComboBox.setSelectedItem(previousTargetID);

		targetID = extractIDFromObject(targetShipShootComboBox.getSelectedItem());
		Ship target = game.getShip(targetID);

		targetCompartment = Ships.calculateCompartmentToAim(target, ship);

		if (target.getMast() > 0)
			riggingShootRadioButton.setEnabled(true);
		
		if (target.getCannonsNumber(targetCompartment, Gun.LIGHT, Commons.BOTH) > 0
				|| target.getCannonsNumber(targetCompartment, Gun.MEDIUM, Commons.BOTH) > 0
				|| target.getCannonsNumber(targetCompartment, Gun.HEAVY, Commons.BOTH) > 0)
			cannonShootRadioButton.setEnabled(true);

		hullShootRadioButton.setEnabled(true);

		switch (previousSelectedAimTypeShoot) {
		case RIGGING:
			if (riggingShootRadioButton.isEnabled()) {
				riggingShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case CANNON:
			if (cannonShootRadioButton.isEnabled()) {
				cannonShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case HULL:
			if (hullShootRadioButton.isEnabled()) {
				hullShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		case NONE:
			if (riggingShootRadioButton.isEnabled()) {
				riggingShootRadioButton.doClick();
				stillAvailable = true;
			}
			break;
		}

		if (!stillAvailable || mode == UpdateMode.DEFAULT) {
			if (riggingShootRadioButton.isEnabled())
				riggingShootRadioButton.doClick();
			else if (cannonShootRadioButton.isEnabled())
				cannonShootRadioButton.doClick();
			else
				hullShootRadioButton.doClick();
		}

		stillAvailable = false;

		if (selectedAimTypeShoot == AimPart.CANNON) {
			if (target.getCannonsNumber(targetCompartment, Gun.LIGHT, Commons.BOTH) > 0)
				lightAimedShootRadioButton.setEnabled(true);
			if (target.getCannonsNumber(targetCompartment, Gun.MEDIUM, Commons.BOTH) > 0)
				mediumAimedShootRadioButton.setEnabled(true);
			if (target.getCannonsNumber(targetCompartment, Gun.HEAVY, Commons.BOTH) > 0)
				heavyAimedShootRadioButton.setEnabled(true);

			switch (previousSelectedAimedTypeShoot) {
			case LIGHT:
				if (lightAimedShootRadioButton.isEnabled()) {
					lightAimedShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			case MEDIUM:
				if (mediumAimedShootRadioButton.isEnabled()) {
					mediumAimedShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			case HEAVY:
				if (heavyAimedShootRadioButton.isEnabled()) {
					heavyAimedShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			case NONE:
				if (lightAimedShootRadioButton.isEnabled()) {
					lightAimedShootRadioButton.doClick();
					stillAvailable = true;
				}
				break;
			}

			if (!stillAvailable || mode == UpdateMode.DEFAULT) {
				if (lightAimedShootRadioButton.isEnabled())
					lightAimedShootRadioButton.doClick();
				else if (mediumAimedShootRadioButton.isEnabled())
					mediumAimedShootRadioButton.doClick();
				else
					heavyAimedShootRadioButton.doClick();
			}

			stillAvailable = false;
		}

		shootShootButton.setEnabled(true);

		updateFinished = true;
		
	}


	private static void targetShipShootComboBoxActionPerformed(ActionEvent evt) {
		if (updateFinished)
			updateShootTab(UpdateMode.CONTINUE);
		// updateTargetShipShootGUI();
	}


	private static void shootShootButtonActionPerformed(ActionEvent evt) {
		int targetID = extractIDFromObject(targetShipShootComboBox.getSelectedItem());
		Ship target = game.getShip(targetID);

		Ships.shoot(ship, target, targetDistance, selectedCompartmentShoot, selectedOwnTypeShoot,
				selectedAimTypeShoot, selectedAimedTypeShoot);

		setSelectedShip(ship, Tabs.SHOOT);
		
	}


	private static void compartmentTypeShootActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("bow"))
			selectedCompartmentShoot = GunCompartment.BOW;
		if (actionCommand.equals("left side"))
			selectedCompartmentShoot = GunCompartment.SIDE_L;
		if (actionCommand.equals("right side"))
			selectedCompartmentShoot = GunCompartment.SIDE_R;
		if (actionCommand.equals("stern"))
			selectedCompartmentShoot = GunCompartment.STERN;

		if (updateFinished)
			updateShootTab(UpdateMode.CONTINUE);
	}


	private static void ownTypeShootActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("light"))
			selectedOwnTypeShoot = Gun.LIGHT;
		if (actionCommand.equals("medium"))
			selectedOwnTypeShoot = Gun.MEDIUM;
		if (actionCommand.equals("heavy"))
			selectedOwnTypeShoot = Gun.HEAVY;

		if (updateFinished)
			updateShootTab(UpdateMode.CONTINUE);
	}


	private static void aimAtTypeShootActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("rigging"))
			selectedAimTypeShoot = AimPart.RIGGING;
		if (actionCommand.equals("cannon"))
			selectedAimTypeShoot = AimPart.CANNON;
		if (actionCommand.equals("hull"))
			selectedAimTypeShoot = AimPart.HULL;

		if (updateFinished)
			updateShootTab(UpdateMode.CONTINUE);
	}


	private static void aimedTypeShootActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("light"))
			selectedAimedTypeShoot = Gun.LIGHT;
		if (actionCommand.equals("medium"))
			selectedAimedTypeShoot = Gun.MEDIUM;
		if (actionCommand.equals("heavy"))
			selectedAimedTypeShoot = Gun.HEAVY;
	}


	private static void sabotageButtonActionPerformed(ActionEvent evt) {
/*		game.setShipBoardingActionUsed(selectedShipID, game.getCurrentPlayer(), selectedMarinesSource, 2);

		if (selectedMarinesSource != MarinesCompartment.DECK)
			sabotageUnderDeckDialog = new SabotageUnderDeckDialog(f);
		else {
			if (game.checkIfSabotageSuccessful(selectedShipID, game.getCurrentPlayer(), selectedMarinesSource)) {
				if (ship.getTowedBy() == null && ship.getTowOther() == null)
					game.destroyMast(selectedShipID, 1); // par. 12.2.3.3.1
				else
					game.throwTow(selectedShipID); // par. 12.2.3.3.2

				Object[] options = { "OK", "Cancel" };
				int rv = JOptionPane.showOptionDialog(f, "Sabotage successful.\nDo you want to blow ship up?", null,
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

				if (rv == JOptionPane.YES_OPTION && Dice.roll() == 6) {
					MainBoard.game.sinkShip(MainBoard.getSelectedShipID(), DestroyShipMode.BLOWUP); // 12.2.3.5.1
					MainBoard.boardPanel.repaint();
					MainBoard.setSelectedShip(null, Tabs.MOVEMENT);
				}
			}
		}

		setSelectedShip(selectedShipID, Tabs.MARINES);
		*/
	}


	private static void moveMarinesButtonActionPerformed(ActionEvent evt) {
		Ship source = ship;
		
		if (selectedMarinesDestination != MarinesCompartment.SHIP_X) {
			if (marinesCheckBox.isSelected())
				source.moveMarines(game.getCurrentPlayer(),
						selectedMarinesSource, selectedMarinesDestination, (Integer) (marinesNumberSpinner.getValue()));
			if (commanderCheckBox.isSelected())
				source.moveCommander(game.getCurrentPlayer(), selectedMarinesSource, selectedMarinesDestination);
		} else {
			int targetID = extractIDFromObject(destinationShipComboBox.getSelectedItem());
			Ship target = game.getShip(targetID);

			if (marinesCheckBox.isSelected())
				Ships.moveMarinesShip(source, target, game.getCurrentPlayer(),
						(Integer) (marinesNumberSpinner.getValue()));
			if (commanderCheckBox.isSelected())
				Ships.moveCommanderShip(source, target);
		}

		updateMarinesTab(UpdateMode.DEFAULT);
	}


	private static void marinesNumberChanged(ChangeEvent evt) {
		if (updateFinished)
			updateMarinesTab(UpdateMode.CONTINUE);
	}


	private static void marinesTypeActionPerformed(ActionEvent evt) {
		if (updateFinished)
			updateMarinesTab(UpdateMode.CONTINUE);
	}


	private static void attackButtonActionPerformed(ActionEvent evt) {
		Ships.closeCombat(ship, game.getCurrentPlayer(), (String) (enemyGroupComboBox.getSelectedItem()),
				selectedMarinesSource);

		updateMarinesTab(UpdateMode.DEFAULT);
		updateStatsTab();
	}


	private static void surrenderButtonActionPerformed(ActionEvent evt) {
		Ships.surrenderMarines(ship, selectedMarinesSource, (Integer) (marinesNumberSpinner.getValue()));

		setSelectedShip(ship, Tabs.MARINES);
	}


	private static void escapeButtonActionPerformed(ActionEvent evt) {
		// dlaczego sprawdzanie warunku znalazlo sie tutaj?!
		if (Ships.checkIfBoardingEscapePossible(ship, game.getCurrentPlayer()))
			Ships.boardingEscape(ship); // 12.8.3

		updateMovementTab(UpdateMode.DEFAULT);
		updateMarinesTab(UpdateMode.DEFAULT);
		updateStatsTab();
		
	}


	private static void sourceMarinesActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("deck"))
			selectedMarinesSource = MarinesCompartment.DECK;
		if (actionCommand.equals("inmove"))
			selectedMarinesSource = MarinesCompartment.INMOVE;
		if (actionCommand.equals("batteries"))
			selectedMarinesSource = MarinesCompartment.BATTERIES;

		if (updateFinished)
			updateMarinesTab(UpdateMode.MARINES_TAB_SOURCE);
	}


	private static void destinationMarinesActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("deck"))
			selectedMarinesDestination = MarinesCompartment.DECK;
		if (actionCommand.equals("inmove"))
			selectedMarinesDestination = MarinesCompartment.INMOVE;
		if (actionCommand.equals("batteries"))
			selectedMarinesDestination = MarinesCompartment.BATTERIES;
		if (actionCommand.equals("ship"))
			selectedMarinesDestination = MarinesCompartment.SHIP_X;
	}


	private static void angleButtonActionPerformed(ActionEvent evt) {

		Ships.rotateShip(ship, (Integer) (angleSpinner.getValue()));
		boardPanel.repaint();

		if (game.getStage() == Stage.DEPLOYMENT)
			angleSpinner.setValue(0);
		else {
			SpinnerModel sm;
			Range r = Ships.checkAngleToRotate(ship);
			sm = new SpinnerNumberModel(0, r.getLowerBound(), r.getUpperBound(), 1);
			angleSpinner.setModel(sm);
			sm = new SpinnerNumberModel(0, 0, Ships.getDistanceToMove(ship), 1);
			distanceSpinner.setModel(sm);
		}

		updateStatsTab();
		
	}


	private static void distanceButtonActionPerformed(ActionEvent evt) {
		Ships.moveShip(ship, (Integer) (distanceSpinner.getValue()));
		boardPanel.repaint();

		if (Board.isOnMap(ship.getPosition())
				&& !ship.isParameter(Parameter.IS_SUNK))
			setSelectedShip(ship, Tabs.MOVEMENT);
		else
			setSelectedShip(null, Tabs.MOVEMENT);

		updateStatsTab();
		
	}


	private static void towButtonActionPerformed(ActionEvent evt) {
		Integer targetId = extractIDFromObject(towComboBox.getSelectedItem());
		Ship target = game.getShip(targetId);
		Ships.tow(ship, target);
		updateMovementTab(UpdateMode.DEFAULT);
		
	}


	private static void throwTowButtonActionPerformed(ActionEvent evt) {
		Ships.throwTow(ship);
		towButton.setEnabled(true);

		towComboBox.setEnabled(true);
		towComboBox.removeAllItems();
		for (Ship s : game.getShips()) {
			if (s != ship && Ships.checkIfTowable(ship, s)) {
				towComboBox.addItem(s.getCaption());
			}
		}

		if (towComboBox.getItemCount() == 0)
			towButton.setEnabled(false);
		throwTowButton.setEnabled(false);

		setSelectedShip(ship, Tabs.MOVEMENT);
		
	}


	private static void towComboBoxActionPerformed(ActionEvent evt) {
		if (isTowOneAttemptPossible())
			towOneRadioButton.setEnabled(true);
		else
			towOneRadioButton.setEnabled(false);
	}


	private static boolean isTowOneAttemptPossible() {
		if (ship == null || towComboBox.getItemCount() == 0)
			return false;

		int towedID = extractIDFromObject(towComboBox.getSelectedItem());
		Ship towed = game.getShip(towedID);

		if (Ships.checkIfEscapeAttemptPossible(ship, ShallowAttempt.TOW_BY_ONE, towed))
			return true;
		else
			return false;
	}


	private static void escapeTypeActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("silver"))
			selectedEscapeType = ShallowAttempt.DROP_SILVER;
		if (actionCommand.equals("cannons"))
			selectedEscapeType = ShallowAttempt.DROP_CANNONS;
		if (actionCommand.equals("anchor"))
			selectedEscapeType = ShallowAttempt.PULL_ANCHOR;
		if (actionCommand.equals("tow"))
			selectedEscapeType = ShallowAttempt.TOW_BY_ONE;
		if (actionCommand.equals("boats"))
			selectedEscapeType = ShallowAttempt.TOW_BY_BOATS;
	}


	private static void makeAttemptButtonActionPerformed(ActionEvent evt) {
		if (selectedEscapeType == ShallowAttempt.TOW_BY_ONE) {
			int towedID = extractIDFromObject(towComboBox.getSelectedItem());
			Ship towed = game.getShip(towedID);

			Ships.makeShallowEscapeAttempt(ship, selectedEscapeType, towed);
			Ships.tow(ship, towed); // par. 17.11.2
		} else
			Ships.makeShallowEscapeAttempt(ship, selectedEscapeType, null);

		setSelectedShip(ship, Tabs.MOVEMENT);
		
	}


	private static void endTurnButtonActionPerformed(ActionEvent evt) {
		game.endPlayerTurn();
		boardPanel.repaint();

		if (game.getStage() != Stage.BOARDING_MOVEMENTS && game.getStage() != Stage.BOARDING_ACTIONS
				&& game.getStage() != Stage.BOARDING_SABOTAGE)
			setSelectedShip(null, Tabs.MOVEMENT);
		makeHeaderLabel();
	}


	@SuppressWarnings("serial")
	private static void updateStatsTab() {
		if (ship == null) {
			idStatsLabel.setText("ID: -");
			ownerStatsLabel.setText("Owner: -");
			classStatsLabel.setText("Class: -");
			positionStatsLabel.setText("Position = [-,-]");
			rotationStatsLabel.setText("Rotation: -");
			moveOverStatsLabel.setText("MO: -");
			actionsOverStatsLabel.setText("Actions Over: -");
			hullStatsLabel.setText("Hull: -/-");
			helmStatsLabel.setText("Helm: -/-");
			mastStatsLabel.setText("Mo/Ma: -/-");
			isWreckStatsLabel.setText("Wreck: -");
			isImmobilizedStatsLabel.setText("Imm: -");
			isExplosiveStatsLabel.setText("Bomb: -");
			tugStatsLabel.setText("Tug: -");
			towedStatsLabel.setText("Towed: -");
			coupledStatsLabel.setText("Coupled: -");
			silverLoadStatsLabel.setText("Silver: -");
			lightCannonsLoadStatsLabel.setText("Light: -");
			mediumCannonsLoadStatsLabel.setText("Medium: -");
			bftStatsLabel.setText("BFT: -");
			teauStatsLabel.setText("TEAU: -");
			attemptsUsedStatsLabel.setText("AU: -");
			happinessStatsLabel.setText("Happiness: -");
			happinessSinkStatsLabel.setText("S: -");
			happinessBoardingStatsLabel.setText("B: -");

			cannonsAStatsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null,
					null, null } }, new String[] { "BL", "BM", "LL", "LM", "LH" }) {

				Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
						java.lang.String.class, java.lang.String.class };
				boolean[] canEdit = new boolean[] { false, false, false, false, false };


				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}


				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return canEdit[columnIndex];
				}
			});
			cannonsAStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

			cannonsAStatsTable.getTableHeader().setReorderingAllowed(false);
			cannonsAStatsTable.getColumnModel().getColumn(0).setResizable(false);
			cannonsAStatsTable.getColumnModel().getColumn(1).setResizable(false);
			cannonsAStatsTable.getColumnModel().getColumn(2).setResizable(false);
			cannonsAStatsTable.getColumnModel().getColumn(3).setResizable(false);
			cannonsAStatsTable.getColumnModel().getColumn(4).setResizable(false);
			cannonsAStatsTable.setEnabled(false);

			cannonsBStatsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null,
					null, null } }, new String[] { "SL", "SM", "RL", "RM", "RH" }) {

				Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
						java.lang.String.class, java.lang.String.class };
				boolean[] canEdit = new boolean[] { false, false, false, false, false };


				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}


				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return canEdit[columnIndex];
				}
			});
			cannonsBStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

			cannonsBStatsTable.getTableHeader().setReorderingAllowed(false);
			cannonsBStatsTable.getColumnModel().getColumn(0).setResizable(false);
			cannonsBStatsTable.getColumnModel().getColumn(1).setResizable(false);
			cannonsBStatsTable.getColumnModel().getColumn(2).setResizable(false);
			cannonsBStatsTable.getColumnModel().getColumn(3).setResizable(false);
			cannonsBStatsTable.getColumnModel().getColumn(4).setResizable(false);
			cannonsBStatsTable.setEnabled(false);

			marinesAStatsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {
					{ null, null, null, null }, { null, null, null, null }, { null, null, null, null },
					{ null, null, null, null } }, new String[] { "Pas", "Elm", "Sid", "Ple" }) {

				Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
						java.lang.String.class };
				boolean[] canEdit = new boolean[] { false, false, false, false };


				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}


				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return canEdit[columnIndex];
				}
			});
			marinesAStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

			marinesAStatsTable.getTableHeader().setReorderingAllowed(false);
			marinesAStatsTable.getColumnModel().getColumn(0).setResizable(false);
			marinesAStatsTable.getColumnModel().getColumn(1).setResizable(false);
			marinesAStatsTable.getColumnModel().getColumn(2).setResizable(false);
			marinesAStatsTable.getColumnModel().getColumn(3).setResizable(false);
			marinesAStatsTable.setEnabled(false);

			marinesBStatsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {
					{ null, null, null, null }, { null, null, null, null }, { null, null, null, null },
					{ null, null, null, null } }, new String[] { "Ham", "Dis", "Del", "Lep" }) {

				Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
						java.lang.String.class };
				boolean[] canEdit = new boolean[] { false, false, false, false };


				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}


				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return canEdit[columnIndex];
				}
			});
			marinesBStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

			marinesBStatsTable.getTableHeader().setReorderingAllowed(false);
			marinesBStatsTable.getColumnModel().getColumn(0).setResizable(false);
			marinesBStatsTable.getColumnModel().getColumn(1).setResizable(false);
			marinesBStatsTable.getColumnModel().getColumn(2).setResizable(false);
			marinesBStatsTable.getColumnModel().getColumn(3).setResizable(false);
			marinesBStatsTable.setEnabled(false);
			return;
		}

		idStatsLabel.setText("ID: " + ship.getID());
		ownerStatsLabel.setText("Owner: " + ship.getOwner().toString());
		classStatsLabel.setText("Class: " + ship.getShipClass().toString());
		positionStatsLabel.setText("Position = [" + (ship.getPosition().getA() + 1) + ","
				+ (ship.getPosition().getB() + 1) + "]");
		rotationStatsLabel.setText("Rotation: " + ship.getRotation().toString());

		if (ship.getMovesQueueCode() != MovesQueueCode.END)
			moveOverStatsLabel.setText("MO: NO");
		else
			moveOverStatsLabel.setText("MO: YES");

		if (ship.isParameter(Parameter.ACTIONS_OVER))
			actionsOverStatsLabel.setText("Actions Over: YES");
		else
			actionsOverStatsLabel.setText("Actions Over: NO");

		hullStatsLabel.setText("Hull: " + ship.getDurability() + "/"
				+ ship.getShipClass().getDurabilityMax());
		helmStatsLabel.setText("Helm: " + ship.getHelm(Commons.READY) + "/" + ship.getHelm(Commons.BOTH));
		mastStatsLabel.setText("Mo/Ma: " + ship.getDistanceMoved() + "/" + ship.getMast());

		
		String isWreck = ship.isParameter(Parameter.IS_WRECK) ? "YES" : "NO";
			isWreckStatsLabel.setText("Wreck: " + isWreck);

			String isImmobilized = ship.isParameter(Parameter.IS_IMMOBILIZED) ? "YES" : "NO";
			isWreckStatsLabel.setText("Imm: " + isImmobilized);

			String isBomb = ship.isParameter(Parameter.IS_EXPLOSIVE) ? "YES" : "NO";
			isWreckStatsLabel.setText("Bomb: " + isBomb);

		if (ship.getTowedBy() == null)
			tugStatsLabel.setText("Tug: -");
		else
			tugStatsLabel.setText("Tug: " + ship.getTowedBy());

		if (ship.getTowOther() == null)
			towedStatsLabel.setText("Towed: -");
		else
			towedStatsLabel.setText("Towed: " + ship.getTowOther());

		
		coupledStatsLabel.setText("Coupled: " + DataExtractors.getShipCoupledString(ship));

		silverLoadStatsLabel.setText("Silver: " + ship.getLoad(CargoType.SILVER) + "t");
		lightCannonsLoadStatsLabel.setText("Light: " + ship.getLoad(CargoType.CANNONS_LIGHT));
		mediumCannonsLoadStatsLabel.setText("Medium: " + ship.getLoad(CargoType.CANNONS_MEDIUM));

		if (ship.isBoardingFirstTurn() == BoardingFirstTurn.NO)
			bftStatsLabel.setText("BFT: NO");
		else
			bftStatsLabel.setText("BFT: YES");

		if (ship.isParameter(Parameter.TURN_ESCAPE_ATTEMPT_USED))
			teauStatsLabel.setText("TEAU: YES");
		else
			teauStatsLabel.setText("TEAU: NO");

		attemptsUsedStatsLabel.setText("AU: " + DataExtractors.getShipAttemptsUsedString(ship));
		happinessStatsLabel.setText("Happiness: " + ship.getHappiness());

		if (!ship.isHappinessFlagSet(Happiness.SUNK))
			happinessSinkStatsLabel.setText("S: NO");
		else
			happinessSinkStatsLabel.setText("S: YES");

		if (!ship.isHappinessFlagSet(Happiness.BOARDING))
			happinessBoardingStatsLabel.setText("B: NO");
		else
			happinessBoardingStatsLabel.setText("B: YES");

		cannonsAStatsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { {
				ship.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.BOW, Gun.LIGHT, Commons.USED),
						ship.getCannonsNumber(GunCompartment.BOW, Gun.MEDIUM, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.BOW, Gun.MEDIUM, Commons.USED),
				ship.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.SIDE_L, Gun.LIGHT, Commons.USED),
				ship.getCannonsNumber(GunCompartment.SIDE_L, Gun.MEDIUM, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.SIDE_L, Gun.MEDIUM, Commons.USED),
				ship.getCannonsNumber(GunCompartment.SIDE_L, Gun.HEAVY, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.SIDE_L, Gun.HEAVY, Commons.USED) } },
				new String[] { "BL", "BM", "LL", "LM", "LH" }) {

			Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
					java.lang.String.class, java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false, false, false, false };


			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return types[columnIndex];
			}


			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		cannonsAStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

		cannonsAStatsTable.getTableHeader().setReorderingAllowed(false);
		cannonsAStatsTable.getColumnModel().getColumn(0).setResizable(false);
		cannonsAStatsTable.getColumnModel().getColumn(1).setResizable(false);
		cannonsAStatsTable.getColumnModel().getColumn(2).setResizable(false);
		cannonsAStatsTable.getColumnModel().getColumn(3).setResizable(false);
		cannonsAStatsTable.getColumnModel().getColumn(4).setResizable(false);
		cannonsAStatsTable.setEnabled(false);

		cannonsBStatsTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { {
				ship.getCannonsNumber(GunCompartment.STERN, Gun.LIGHT, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.STERN, Gun.LIGHT, Commons.USED),
				ship.getCannonsNumber(GunCompartment.STERN, Gun.MEDIUM, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.STERN, Gun.MEDIUM, Commons.USED),
				ship.getCannonsNumber(GunCompartment.SIDE_R, Gun.LIGHT, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.SIDE_R, Gun.LIGHT, Commons.USED),
				ship.getCannonsNumber(GunCompartment.SIDE_R, Gun.MEDIUM, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.SIDE_R, Gun.MEDIUM, Commons.USED),
				ship.getCannonsNumber(GunCompartment.SIDE_R, Gun.HEAVY, Commons.READY) + "|"
						+ ship.getCannonsNumber(GunCompartment.SIDE_R, Gun.HEAVY, Commons.USED) } },
				new String[] { "SL", "SM", "RL", "RM", "RH" }) {

			Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
					java.lang.String.class, java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false, false, false, false };


			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return types[columnIndex];
			}


			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		cannonsBStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

		cannonsBStatsTable.getTableHeader().setReorderingAllowed(false);
		cannonsBStatsTable.getColumnModel().getColumn(0).setResizable(false);
		cannonsBStatsTable.getColumnModel().getColumn(1).setResizable(false);
		cannonsBStatsTable.getColumnModel().getColumn(2).setResizable(false);
		cannonsBStatsTable.getColumnModel().getColumn(3).setResizable(false);
		cannonsBStatsTable.getColumnModel().getColumn(4).setResizable(false);
		cannonsBStatsTable.setEnabled(false);

		marinesAStatsTable.setModel(new javax.swing.table.DefaultTableModel(DataExtractors.getShipMarinesArray(ship, 0),
				new String[] { "Pas", "Elm", "Sid", "Ple" }) {

			Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
					java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false, false, false };


			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return types[columnIndex];
			}


			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		marinesAStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

		marinesAStatsTable.getTableHeader().setReorderingAllowed(false);
		marinesAStatsTable.getColumnModel().getColumn(0).setResizable(false);
		marinesAStatsTable.getColumnModel().getColumn(1).setResizable(false);
		marinesAStatsTable.getColumnModel().getColumn(2).setResizable(false);
		marinesAStatsTable.getColumnModel().getColumn(3).setResizable(false);
		marinesAStatsTable.setEnabled(false);

		marinesBStatsTable.setModel(new javax.swing.table.DefaultTableModel(DataExtractors.getShipMarinesArray(ship, 1),
				new String[] { "Ham", "Dis", "Del", "Lep" }) {

			Class<?>[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
					java.lang.String.class };
			boolean[] canEdit = new boolean[] { false, false, false, false };


			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return types[columnIndex];
			}


			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		marinesBStatsTable.setDefaultRenderer(String.class, new CenteredCellRenderer());

		marinesBStatsTable.getTableHeader().setReorderingAllowed(false);
		marinesBStatsTable.getColumnModel().getColumn(0).setResizable(false);
		marinesBStatsTable.getColumnModel().getColumn(1).setResizable(false);
		marinesBStatsTable.getColumnModel().getColumn(2).setResizable(false);
		marinesBStatsTable.getColumnModel().getColumn(3).setResizable(false);
		marinesBStatsTable.setEnabled(false);
		
	}


	private static void updateCargoTab(UpdateMode mode) {
		Integer currentShipID = null;

		updateFinished = false;

		handleCargoButton.setEnabled(false);
		uncoupleCargoButton.setEnabled(false);
		setExplosivesCargoButton.setEnabled(false);

		quantityCargoSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));

		previousShip = shipCargoComboBox.getSelectedItem();
		shipCargoComboBox.removeAllItems();
		shipCargoComboBox.setEnabled(false);

		cargoFromCargoRadioButton.setEnabled(false);
		batteriesFromCargoRadioButton.setEnabled(false);
		cargoToCargoRadioButton.setEnabled(false);
		batteriesToCargoRadioButton.setEnabled(false);
		silverCargoRadioButton.setEnabled(false);
		lightCargoRadioButton.setEnabled(false);
		mediumCargoRadioButton.setEnabled(false);
		bowCargoRadioButton.setEnabled(false);
		leftCargoRadioButton.setEnabled(false);
		rightCargoRadioButton.setEnabled(false);
		sternCargoRadioButton.setEnabled(false);
		targetBowCargoRadioButton.setEnabled(false);
		targetLeftCargoRadioButton.setEnabled(false);
		targetRightCargoRadioButton.setEnabled(false);
		targetSternCargoRadioButton.setEnabled(false);

		quantityCargoSpinner.setEnabled(false);
		
		if (ship == null || game.getStage() != Stage.PLAYERS_MOVES
				|| ship.getPlayerMarinesOnShip(game.getCurrentPlayer(), false) == 0
				|| Ships.checkIfShipBoarded(ship, game.getCurrentPlayer())) {
			updateFinished = true;
			previousShip = null;
			return;
		}

		shipCargoComboBox.removeAllItems();
		// par. 2.1 (nie mozna dokonywac przeladunku dzial miedzy przedzialami)
		for (Ship s : game.getShips()) {
			if (s != ship
					&& Ships.checkIfHandleable(ship, game.getCurrentPlayer(), s) != HandlingPartner.NONE)
				shipCargoComboBox.addItem(s.getID() + ", " + s.getShipClass());
		}
		// --

		if (shipCargoComboBox.getItemCount() == 0) {
			updateFinished = true;
			previousShip = null;
			updateFinished = true;
			return;
		}

		shipCargoComboBox.setSelectedItem(previousShip);
		if (previousShip == null || !shipCargoComboBox.getSelectedItem().toString().equals(previousShip.toString())) {
			selectedFromDestinationCargo = CargoDestination.NONE;
			selectedToDestinationCargo = CargoDestination.NONE;
			selectedCargoTypeCargo = CargoType.NONE;
			selectedSourceGunCompartmentCargo = GunCompartment.NONE;
			selectedTargetGunCompartmentCargo = GunCompartment.NONE;

			shipCargoComboBox.setSelectedIndex(0);
		}

		currentShipID = extractIDFromObject(shipCargoComboBox.getSelectedItem());

		cargoFromCargoRadioButton.setEnabled(true);
		if (handlingPartner == HandlingPartner.OWN)
			batteriesFromCargoRadioButton.setEnabled(true);
		cargoToCargoRadioButton.setEnabled(true);
		if (handlingPartner == HandlingPartner.OWN)
			batteriesToCargoRadioButton.setEnabled(true);

		switch (selectedFromDestinationCargo) {
		case CARGO:
			cargoFromCargoRadioButton.doClick();
			break;
		case BATTERIES:
			batteriesFromCargoRadioButton.doClick();
			break;
		case NONE:
			cargoFromCargoRadioButton.doClick();
			break;
		}

		switch (selectedToDestinationCargo) {
		case CARGO:
			cargoToCargoRadioButton.doClick();
			break;
		case BATTERIES:
			batteriesToCargoRadioButton.doClick();
			break;
		case NONE:
			cargoToCargoRadioButton.doClick();
			break;
		}

		if (selectedFromDestinationCargo == CargoDestination.CARGO
				&& selectedToDestinationCargo == CargoDestination.CARGO) {
			silverCargoRadioButton.setEnabled(true);
		}

		lightCargoRadioButton.setEnabled(true);
		mediumCargoRadioButton.setEnabled(true);

		if (selectedFromDestinationCargo == CargoDestination.BATTERIES) {
			bowCargoRadioButton.setEnabled(true);
			leftCargoRadioButton.setEnabled(true);
			rightCargoRadioButton.setEnabled(true);
			sternCargoRadioButton.setEnabled(true);
		}

		if (selectedToDestinationCargo == CargoDestination.BATTERIES) {
			targetBowCargoRadioButton.setEnabled(true);
			targetLeftCargoRadioButton.setEnabled(true);
			targetRightCargoRadioButton.setEnabled(true);
			targetSternCargoRadioButton.setEnabled(true);
		}

		switch (selectedCargoTypeCargo) {
		case SILVER:
			silverCargoRadioButton.doClick();
			break;
		case CANNONS_LIGHT:
			lightCargoRadioButton.doClick();
			break;
		case CANNONS_MEDIUM:
			mediumCargoRadioButton.doClick();
			break;
		case NONE:
			silverCargoRadioButton.doClick();
			break;
		}

		switch (selectedSourceGunCompartmentCargo) {
		case BOW:
			bowCargoRadioButton.doClick();
			break;
		case SIDE_L:
			leftCargoRadioButton.doClick();
			break;
		case SIDE_R:
			rightCargoRadioButton.doClick();
			break;
		case STERN:
			sternCargoRadioButton.doClick();
			break;
		case NONE:
			bowCargoRadioButton.doClick();
			break;
		}

		switch (selectedTargetGunCompartmentCargo) {
		case BOW:
			targetBowCargoRadioButton.doClick();
			break;
		case SIDE_L:
			targetLeftCargoRadioButton.doClick();
			break;
		case SIDE_R:
			targetRightCargoRadioButton.doClick();
			break;
		case STERN:
			targetSternCargoRadioButton.doClick();
			break;
		case NONE:
			targetBowCargoRadioButton.doClick();
			break;
		}

		quantityCargoSpinner.setEnabled(true);

		shipCargoComboBox.setEnabled(true);

		// selectedCargoTypeCargo
		SpinnerModel sm;
		if (currentShipID != null) {
			Ship currentShip = game.getShip(currentShipID);
			
			if (selectedCargoTypeCargo == CargoType.SILVER)
				sm = new SpinnerNumberModel(0, 0, Ships.checkMaxQuantityToHandle(ship, currentShip,
						selectedFromDestinationCargo, selectedToDestinationCargo, selectedCargoTypeCargo,
						selectedSourceGunCompartmentCargo, selectedTargetGunCompartmentCargo), 10);
			else
				sm = new SpinnerNumberModel(0, 0, Ships.checkMaxQuantityToHandle(ship, currentShip,
						selectedFromDestinationCargo, selectedToDestinationCargo, selectedCargoTypeCargo,
						selectedSourceGunCompartmentCargo, selectedTargetGunCompartmentCargo), 1);
		} else
			sm = new SpinnerNumberModel(0, 0, 0, 0);

		quantityCargoSpinner.setModel(sm);

		if (Ships.checkIfHandleUncouplePossible(ship, game.getShip(currentShipID)))
			uncoupleCargoButton.setEnabled(true);
		else
			uncoupleCargoButton.setEnabled(false);

		previousShip = shipCargoComboBox.getSelectedItem();

		if (Ships.checkIfSetExplosivesPossible(ship, game.getCurrentPlayer()))
			setExplosivesCargoButton.setEnabled(true);

		updateFinished = true;
	}


	private static void fromTypeCargoActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("cargo"))
			selectedFromDestinationCargo = CargoDestination.CARGO;
		if (actionCommand.equals("batteries"))
			selectedFromDestinationCargo = CargoDestination.BATTERIES;

		if (updateFinished)
			updateCargoTab(UpdateMode.CONTINUE);
	}


	private static void toTypeCargoActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("cargo"))
			selectedToDestinationCargo = CargoDestination.CARGO;
		if (actionCommand.equals("batteries"))
			selectedToDestinationCargo = CargoDestination.BATTERIES;

		if (updateFinished)
			updateCargoTab(UpdateMode.CONTINUE);
	}


	private static void cargoTypeCargoActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("silver")) {
			selectedCargoTypeCargo = CargoType.SILVER;
			silverCargoRadioButton.setSelected(true);
		}
		if (actionCommand.equals("light")) {
			selectedCargoTypeCargo = CargoType.CANNONS_LIGHT;
			lightCargoRadioButton.setSelected(true);
		}
		if (actionCommand.equals("medium")) {
			selectedCargoTypeCargo = CargoType.CANNONS_MEDIUM;
			mediumCargoRadioButton.setSelected(true);
		}

		if (updateFinished)
			updateCargoTab(UpdateMode.CONTINUE);
	}


	private static void compartmentTypeCargoActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("bow"))
			selectedSourceGunCompartmentCargo = GunCompartment.BOW;
		if (actionCommand.equals("left"))
			selectedSourceGunCompartmentCargo = GunCompartment.SIDE_L;
		if (actionCommand.equals("right"))
			selectedSourceGunCompartmentCargo = GunCompartment.SIDE_R;
		if (actionCommand.equals("stern"))
			selectedSourceGunCompartmentCargo = GunCompartment.STERN;

		if (updateFinished)
			updateCargoTab(UpdateMode.CONTINUE);
	}


	private static void targetCompartmentTypeCargoActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("bow"))
			selectedTargetGunCompartmentCargo = GunCompartment.BOW;
		if (actionCommand.equals("left"))
			selectedTargetGunCompartmentCargo = GunCompartment.SIDE_L;
		if (actionCommand.equals("right"))
			selectedTargetGunCompartmentCargo = GunCompartment.SIDE_R;
		if (actionCommand.equals("stern"))
			selectedTargetGunCompartmentCargo = GunCompartment.STERN;

		if (updateFinished)
			updateCargoTab(UpdateMode.CONTINUE);
	}


	private static void handleCargoButtonActionPerformed(ActionEvent evt) {
		int targetID = extractIDFromObject(shipCargoComboBox.getSelectedItem());

		Ship target = game.getShip(targetID);
		
		Ships.handle(ship, target, game.getCurrentPlayer(), selectedFromDestinationCargo,
				selectedToDestinationCargo, selectedCargoTypeCargo, selectedSourceGunCompartmentCargo,
				selectedTargetGunCompartmentCargo, (Integer) (quantityCargoSpinner.getValue()));

		// setSelectedShip(selectedShipID,3);
		updateCargoTab(UpdateMode.DEFAULT);
		updateStatsTab();
	}


	private static void uncoupleCargoButtonActionPerformed(ActionEvent evt) {
		Integer targetID = extractIDFromObject(shipCargoComboBox.getSelectedItem());
		Ships.endHandling(ship, game.getShip(targetID));
		setSelectedShip(ship, Tabs.HANDLE); 
	}


	private static void shipCargoComboBoxActionPerformed(ActionEvent evt) {
		if (shipCargoComboBox.getItemCount() > 0) {
			Integer targetId = extractIDFromObject(shipCargoComboBox.getSelectedItem());
			Ship target = game.getShip(targetId);
			
			handlingPartner = Ships.checkIfHandleable(ship, game.getCurrentPlayer(),
					target);
		} else
			handlingPartner = HandlingPartner.NONE;

		if (updateFinished)
			updateCargoTab(UpdateMode.DEFAULT);
			
	}


	private static void quantityCargoNumberChanged(ChangeEvent evt) {
		
		 if ((Integer)(quantityCargoSpinner.getValue()) == 0)
		  handleCargoButton.setEnabled(false); else
		  handleCargoButton.setEnabled(true);
		 
		if (updateFinished)
			updateCargoTab(UpdateMode.DEFAULT);
	}


private static void setExplosivesCargoButtonActionPerformed(ActionEvent evt) {
//		game.setExplosivesShip(selectedShipID);
		updateCargoTab(UpdateMode.CONTINUE);
		updateStatsTab();
	}


	private static void useHappinessButtonActionPerformed(ActionEvent evt) {
		happinessAction = HappinessAction.AGAIN;
		// XXX: Game.cancelTimer();
	}


	private static void acceptRollButtonActionPerformed(ActionEvent evt) {
		happinessAction = HappinessAction.ACCEPT;
		// XXX: Game.cancelTimer();
	}


	public static boolean rollAgainDialog(int roll, Player player) {
		Object[] options = { "Accept", "Roll again" };
		int rv = JOptionPane.showOptionDialog(f, "Roll result: " + roll, "To player: " + player.toString(),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

		if (rv == JOptionPane.YES_OPTION)
			return false;
		return true;
	}


	// ---- Allies Panel

	private static void allyCheckButtonActionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();

		for (Player p : Player.values()) {
			if (p == Player.NONE)
				continue;
			if (command.equals(p.name())) {
				// FIXME: gracz przestaje byc sojusznikiem
//				game.reversePlayerAlly(game.getCurrentPlayer(), p);
//				game.reversePlayerAlly(p, game.getCurrentPlayer()); 
			}
		}

		// updateAlliesPanel();
	}


	private static void closeAlliesButtonActionPerformed(ActionEvent evt) {
		alliesFrame.setVisible(false);
	}


	private static void updateAlliesPanel() {
		Player cP = game.getCurrentPlayer();
		boolean enabled = true;

		// par. 4.4
		if (cP == Player.NONE || cP == Player.PASADENA || cP == Player.SIDONIA)
			enabled = false;
		// --

		delacroixAlliesCheckBox.setEnabled(enabled);
		discasterAlliesCheckBox.setEnabled(enabled);
		elmethAlliesCheckBox.setEnabled(enabled);
		hampshireAlliesCheckBox.setEnabled(enabled);
		leppoAlliesCheckBox.setEnabled(enabled);
		pasadenaAlliesCheckBox.setEnabled(enabled);
		pleensyAlliesCheckBox.setEnabled(enabled);
		sidoniaAlliesCheckBox.setEnabled(enabled);

		switch (cP) {
		case NONE:
			return;
		case PASADENA:
			pasadenaAlliesCheckBox.setEnabled(false);
			break;
		case ELMETH:
			elmethAlliesCheckBox.setEnabled(false);
			break;
		case SIDONIA:
			sidoniaAlliesCheckBox.setEnabled(false);
			break;
		case PLEENSY:
			pleensyAlliesCheckBox.setEnabled(false);
			break;
		case HAMPSHIRE:
			hampshireAlliesCheckBox.setEnabled(false);
			break;
		case DISCASTER:
			discasterAlliesCheckBox.setEnabled(false);
			break;
		case DELACROIX:
			delacroixAlliesCheckBox.setEnabled(false);
			break;
		case LEPPO:
			leppoAlliesCheckBox.setEnabled(false);
			break;
		}

		// par. 4.4
		pasadenaAlliesCheckBox.setEnabled(false);
		sidoniaAlliesCheckBox.setEnabled(false);
		// --

		delacroixAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.DELACROIX));
		discasterAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.DISCASTER));
		elmethAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.ELMETH));
		hampshireAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.HAMPSHIRE));
		leppoAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.LEPPO));
		pasadenaAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.PASADENA));
		pleensyAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.PLEENSY));
		sidoniaAlliesCheckBox.setSelected(game.checkIfAlly(cP, Player.SIDONIA));
	}


	// ---

	public static void setText(String text) {
		messageBox.setText(text);
	}


	public static void addMessage(String text) {
		messageBox.append(text);
	}


	public static void addTestMsg(String text) {
		messageBox.append(text);
	}


	public void paint(Graphics g) {
	}

	public MainBoard() {
	}


	public Dimension getPreferredSize() {
		return new Dimension(1000, 650);
	}


	public static void main(String[] args) {
		game = new Game();
		boardPanelMode = DisplayMode.DEPLOY_MODE;
		messageBox = new JTextArea();
		betweenTurnsDialog = new BetweenTurnsDialog();

		f = new JFrame("Map Display");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(1000, 650);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				createAndShowGUI();
				switchStageDisplayMode(DisplayMode.DEPLOY_MODE);
				game.init();

				addComponentsToPane(
				);

				// endTurnButtonActionPerformed(null);

				// endTurnButtonActionPerformed(null);
				// makeHeaderLabel(); //test-bt

				// game.rollDice(0);

				// game.RotateShip(0, 1);
				// game.moveShip(0, 1);
				// setSelectedShip(0,0); // test-bt

				// setSelectedShip(3);
			}
		});
	}


	public static void switchStageDisplayMode(DisplayMode mode) {
		boardPanelMode = mode;
		f.repaint();
	}


	public static void addComponentsToPane(
	) {
		Container pane = f.getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		pane.removeAll();

		if (game.getStage() == Stage.BETWEEN_TURNS) {
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 2;
			c.gridwidth = 2;
			pane.add(betweenTurnsDialog, c);
		} else {
			c.gridx = 0;
			c.gridy = 0;
			pane.add(headerPanel, c);

			c.gridx = 0;
			c.gridy = 1;
			pane.add(boardPanel, c);

			c.gridx = 0;
			c.gridy = 2;
			pane.add(scrollPane, c);

			// TabbedPane z akcjami

			c.gridx = 1;
			c.gridy = 0;
			c.gridheight = 2;
			c.insets = new Insets(10, 0, 0, 10);
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			pane.add(tabbedPane, c);

			c.gridx = 1;
			c.gridy = 2;
			pane.add(cornerPanel, c);
		}

		f.pack();
		f.setVisible(true);
	}


	private static void saveGameMenuItemActionPerformed(ActionEvent evt) {
		JFileChooser saveGameFileChooser = new JFileChooser();
		int returnVal = saveGameFileChooser.showOpenDialog(f);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = saveGameFileChooser.getSelectedFile();
			try {
				FileOutputStream file_output = new FileOutputStream(file);
				DataOutputStream data_out = new DataOutputStream(file_output);

				game.writeToFile(data_out);

				file_output.close();

			} catch (IOException e) {
				System.out.println("IO exception = " + e);
			}
		}

		JOptionPane.showMessageDialog(f, "Game saved!");
	}


	private static void loadGameMenuItemActionPerformed(ActionEvent evt) {
		JFileChooser loadGameFileChooser = new JFileChooser();
		int returnVal = loadGameFileChooser.showOpenDialog(f);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = loadGameFileChooser.getSelectedFile();
			try {
				FileInputStream file_input = new FileInputStream(file);
				DataInputStream data_in = new DataInputStream(file_input);

				game.readFromFile(data_in);

				file_input.close();

			} catch (IOException e) {
				System.out.println("IO exception = " + e);
			}
		}

		boardPanel.repaint();

		updateMovementTab(UpdateMode.DEFAULT);
		updateMarinesTab(UpdateMode.DEFAULT);
		updateShootTab(UpdateMode.DEFAULT);
		updateCargoTab(UpdateMode.DEFAULT);
		updateStatsTab();

//		betweenTurnsDialog.update(UpdateMode.DEFAULT);
		// uaktualnienie wszystkich elementow

		JOptionPane.showMessageDialog(f, "Game loaded!");
	}


	private static void exitMenuItemActionPerformed(ActionEvent evt) {
		f.dispose();
	}


	private static void alliesMenuItemActionPerformed(ActionEvent evt) {
		alliesFrame.setVisible(true);
		updateAlliesPanel();
	}


	private static void aboutMenuItemActionPerformed(ActionEvent evt) {
		aboutFrame.setVisible(true);
	}


	private static void createAndShowGUI() {

		// --- MENU

		menuBar = new JMenuBar();

		gameMenu = new JMenu("Game");
		menuBar.add(gameMenu);

		saveGameMenuItem = new JMenuItem("Save game..");
		saveGameMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				saveGameMenuItemActionPerformed(evt);
			}
		});
		gameMenu.add(saveGameMenuItem);

		loadGameMenuItem = new JMenuItem("Load game..");
		loadGameMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				loadGameMenuItemActionPerformed(evt);
			}
		});
		gameMenu.add(loadGameMenuItem);

		gameMenu.addSeparator();

		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});
		gameMenu.add(exitMenuItem);

		playerMenu = new JMenu("Player");
		menuBar.add(playerMenu);

		alliesMenuItem = new JMenuItem("Allies");
		alliesMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				alliesMenuItemActionPerformed(evt);
			}
		});
		playerMenu.add(alliesMenuItem);

		helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aboutMenuItemActionPerformed(evt);
			}
		});
		helpMenu.add(aboutMenuItem);

		f.setJMenuBar(menuBar);

		// ---

		headerPanel = new JPanel();
		headerLabel = new JLabel();
		headerPanel.add(headerLabel, BorderLayout.EAST);
		makeHeaderLabel();

		try {
			boardPanel = new BoardPanel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cornerPanel = new JPanel();
		cornerPanel.setMaximumSize(new Dimension(225, 120));
		cornerPanel.setMinimumSize(new Dimension(225, 120));
		cornerPanel.setPreferredSize(new Dimension(225, 120));

		happinessLabel = new JLabel("Roll:");
		remainingTimeLabel = new JLabel("Remaining time: n/a");

		endTurnButton = new JButton("End turn");
		endTurnButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				endTurnButtonActionPerformed(evt);
			}
		});

		useHappinessButton = new JButton("Accept");
		useHappinessButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				useHappinessButtonActionPerformed(evt);
			}
		});

		acceptRollButton = new JButton("Again");
		acceptRollButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				acceptRollButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout cornerPanelLayout = new javax.swing.GroupLayout(cornerPanel);
		cornerPanel.setLayout(cornerPanelLayout);
		cornerPanelLayout.setHorizontalGroup(cornerPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				cornerPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								cornerPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(
												cornerPanelLayout
														.createSequentialGroup()
														.addGap(10, 10, 10)
														.addComponent(remainingTimeLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(
												cornerPanelLayout
														.createSequentialGroup()
														.addComponent(happinessLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(useHappinessButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(acceptRollButton))
										.addComponent(endTurnButton, javax.swing.GroupLayout.DEFAULT_SIZE, 155,
												Short.MAX_VALUE)).addGap(12, 12, 12)));
		cornerPanelLayout.setVerticalGroup(cornerPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				cornerPanelLayout
						.createSequentialGroup()
						.addGroup(
								cornerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(happinessLabel).addComponent(useHappinessButton)
										.addComponent(acceptRollButton))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
						.addComponent(remainingTimeLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(endTurnButton).addGap(32, 32, 32)));

		messageBox = new JTextArea(8, 70);
		// messageBox.setEnabled(false);
		messageBox.setLineWrap(true);
		scrollPane = new JScrollPane(messageBox);
		scrollPane.setPreferredSize(new Dimension(750, 100));

		// Tabbed Pane - Actions
		ImageIcon icon;
		SpinnerModel sm = new SpinnerNumberModel(0, 0, 0, 0);

		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(230, 550));
		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				// TODO here

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				int sel = pane.getSelectedIndex();

				if (sel == Tabs.SHOOT.ordinal()) {
					updateShootTab(UpdateMode.CONTINUE);
				}
			}
		});

		movSeparator1 = new JSeparator(JSeparator.HORIZONTAL);
		movSeparator2 = new JSeparator(JSeparator.HORIZONTAL);

		// Tab: movement
		movementTabPanel = new JPanel();

		transferLabel = new JLabel("Transfer");
		angleLabel = new JLabel("Angle: ");
		distanceLabel = new JLabel("Distance: ");
		towingLabel = new JLabel("Towing operation");
		towIdLabel = new JLabel("Ship ID: ");
		shallowLabel = new JLabel("Attempt to make:");

		angleButton = new JButton("Rotate!");
		angleButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				angleButtonActionPerformed(evt);
			}
		});

		distanceButton = new JButton("Move!");
		distanceButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				distanceButtonActionPerformed(evt);
			}
		});

		towButton = new JButton("Take in tow!");
		towButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				towButtonActionPerformed(evt);
			}
		});

		throwTowButton = new JButton("Throw tow!");
		throwTowButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				throwTowButtonActionPerformed(evt);
			}
		});

		makeAttemptButton = new JButton("Make Attempt!");
		makeAttemptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				makeAttemptButtonActionPerformed(evt);
			}
		});

		angleSpinner = new JSpinner(sm);
		distanceSpinner = new JSpinner(sm);

		towComboBox = new JComboBox<String>();
		towComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				towComboBoxActionPerformed(evt);
			}
		});

		throwGunsRadioButton = new JRadioButton("throw guns away");
		throwGunsRadioButton.setActionCommand(ShallowAttempt.DROP_CANNONS.name());
		throwGunsRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				escapeTypeActionPerformed(evt);
			}
		});

		pullOnAnchorRadioButton = new JRadioButton("pull on anchor");
		pullOnAnchorRadioButton.setActionCommand(ShallowAttempt.PULL_ANCHOR.name());
		pullOnAnchorRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				escapeTypeActionPerformed(evt);
			}
		});

		throwSilverRadioButton = new JRadioButton("throw silver away");
		throwSilverRadioButton.setActionCommand(ShallowAttempt.DROP_SILVER.name());
		throwSilverRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				escapeTypeActionPerformed(evt);
			}
		});

		towWithBoatsRadioButton = new JRadioButton("tow with boats");
		towWithBoatsRadioButton.setActionCommand(ShallowAttempt.TOW_BY_BOATS.name());
		towWithBoatsRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				escapeTypeActionPerformed(evt);
			}
		});

		towOneRadioButton = new JRadioButton("tow one");
		towOneRadioButton.setActionCommand(ShallowAttempt.TOW_BY_ONE.name());
		towOneRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				escapeTypeActionPerformed(evt);
			}
		});

		shallowGroup = new ButtonGroup();
		shallowGroup.add(throwGunsRadioButton);
		shallowGroup.add(pullOnAnchorRadioButton);
		shallowGroup.add(throwSilverRadioButton);
		shallowGroup.add(towOneRadioButton);
		shallowGroup.add(towWithBoatsRadioButton);

		javax.swing.GroupLayout movementTabPanelLayout = new javax.swing.GroupLayout(movementTabPanel);
		movementTabPanel.setLayout(movementTabPanelLayout);
		movementTabPanelLayout
				.setHorizontalGroup(movementTabPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								movementTabPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												movementTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																movementTabPanelLayout.createSequentialGroup()
																		.addComponent(transferLabel)
																		.addContainerGap(124, Short.MAX_VALUE))
														.addGroup(
																movementTabPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				movementTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								movementTabPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												towingLabel)
																										.addGap(116,
																												116,
																												116))
																						.addComponent(
																								movSeparator1,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								192, Short.MAX_VALUE)
																						.addComponent(
																								shallowLabel,
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								movementTabPanelLayout
																										.createSequentialGroup()
																										.addGap(10, 10,
																												10)
																										.addGroup(
																												movementTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																throwGunsRadioButton)
																														.addComponent(
																																throwSilverRadioButton)
																														.addComponent(
																																pullOnAnchorRadioButton)
																														.addComponent(
																																towOneRadioButton)
																														.addComponent(
																																towWithBoatsRadioButton)
																														.addComponent(
																																makeAttemptButton)))
																						.addComponent(
																								movSeparator2,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								192, Short.MAX_VALUE)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								movementTabPanelLayout
																										.createSequentialGroup()
																										.addGap(10, 10,
																												10)
																										.addGroup(
																												movementTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																distanceLabel)
																														.addComponent(
																																angleLabel))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												movementTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING,
																																false)
																														.addComponent(
																																angleSpinner,
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																distanceSpinner,
																																javax.swing.GroupLayout.Alignment.LEADING,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																42,
																																Short.MAX_VALUE))
																										.addGap(18, 18,
																												18)
																										.addGroup(
																												movementTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING,
																																false)
																														.addComponent(
																																angleButton,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)
																														.addComponent(
																																distanceButton,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)))
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								movementTabPanelLayout
																										.createSequentialGroup()
																										.addGroup(
																												movementTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING,
																																false)
																														.addComponent(
																																towButton,
																																javax.swing.GroupLayout.Alignment.LEADING,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)
																														.addGroup(
																																javax.swing.GroupLayout.Alignment.LEADING,
																																movementTabPanelLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addComponent(
																																				towIdLabel)))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addGroup(
																												movementTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																towComboBox,
																																0,
																																96,
																																Short.MAX_VALUE)
																														.addComponent(
																																throwTowButton,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																96,
																																Short.MAX_VALUE))))
																		.addGap(36, 36, 36)))));
		movementTabPanelLayout.setVerticalGroup(movementTabPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				movementTabPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(transferLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								movementTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(angleLabel)
										.addComponent(angleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(angleButton))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(
								movementTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(distanceLabel)
										.addComponent(distanceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(distanceButton))
						.addGap(18, 18, 18)
						.addComponent(movSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(towingLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								movementTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(towIdLabel)
										.addComponent(towComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								movementTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(towButton).addComponent(throwTowButton))
						.addGap(18, 18, 18)
						.addComponent(movSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(shallowLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(throwSilverRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(throwGunsRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(pullOnAnchorRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(towOneRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(towWithBoatsRadioButton).addGap(18, 18, 18).addComponent(makeAttemptButton)
						.addContainerGap(194, Short.MAX_VALUE)));

		icon = createImageIcon("icons/move.png");
		tabbedPane.addTab(null, icon, movementTabPanel, "Move ship");

		// tab: marines
		marinesTabPanel = new JPanel();

		sourceLabel = new JLabel("Source:");

		sourceDeckRadioButton = new JRadioButton("deck");
		sourceDeckRadioButton.setActionCommand(MarinesCompartment.DECK.name());
		sourceDeckRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sourceMarinesActionPerformed(evt);
			}
		});

		sourceInMoveRadioButton = new JRadioButton("in move");
		sourceInMoveRadioButton.setActionCommand(MarinesCompartment.INMOVE.name());
		sourceInMoveRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sourceMarinesActionPerformed(evt);
			}
		});

		sourceBatteriesRadioButton = new JRadioButton("batteries");
		sourceBatteriesRadioButton
				.setActionCommand(MarinesCompartment.BATTERIES.name());
		sourceBatteriesRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sourceMarinesActionPerformed(evt);
			}
		});

		sourceMarinesGroup = new ButtonGroup();
		sourceMarinesGroup.add(sourceDeckRadioButton);
		sourceMarinesGroup.add(sourceInMoveRadioButton);
		sourceMarinesGroup.add(sourceBatteriesRadioButton);

		numberLabel = new JLabel("Number of:");

		marinesCheckBox = new JCheckBox("marines");
		marinesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				marinesTypeActionPerformed(evt);
			}
		});

		commanderCheckBox = new JCheckBox("commander");
		commanderCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				marinesTypeActionPerformed(evt);
			}
		});

		marinesNumberSpinner = new JSpinner(sm);
		marinesNumberSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				marinesNumberChanged(evt);
			}
		});

		destinationLabel = new JLabel("Destination:");

		destinationBatteriesRadioButton = new JRadioButton("batteries");
		destinationBatteriesRadioButton
				.setActionCommand(MarinesCompartment.DECK.name());
		destinationBatteriesRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				destinationMarinesActionPerformed(evt);
			}
		});

		destinationInMoveRadioButton = new JRadioButton("in move");
		destinationInMoveRadioButton
				.setActionCommand(MarinesCompartment.INMOVE.name());
		destinationInMoveRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				destinationMarinesActionPerformed(evt);
			}
		});

		destinationDeckRadioButton = new JRadioButton("deck");
		destinationDeckRadioButton
				.setActionCommand(MarinesCompartment.BATTERIES.name());
		destinationDeckRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				destinationMarinesActionPerformed(evt);
			}
		});

		destinationShipRadioButton = new JRadioButton("another ship:");
		destinationShipRadioButton.setActionCommand(MarinesCompartment.SHIP_X.name());
		destinationShipRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				destinationMarinesActionPerformed(evt);
			}
		});

		destinationShipComboBox = new JComboBox<String>();

		destinationMarinesGroup = new ButtonGroup();
		destinationMarinesGroup.add(destinationDeckRadioButton);
		destinationMarinesGroup.add(destinationInMoveRadioButton);
		destinationMarinesGroup.add(destinationBatteriesRadioButton);
		destinationMarinesGroup.add(destinationShipRadioButton);

		moveMarinesButton = new JButton("Move");
		moveMarinesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				moveMarinesButtonActionPerformed(evt);
			}
		});

		surrenderButton = new JButton("Surrender");
		surrenderButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				surrenderButtonActionPerformed(evt);
			}
		});

		boardingLabel = new JLabel("Boarding");

		enemyGroupLabel = new JLabel("Group:");

		enemyGroupComboBox = new JComboBox<String>();

		attackButton = new JButton("Attack");
		attackButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				attackButtonActionPerformed(evt);
			}
		});

		sabotageButton = new JButton("Sabotage");
		sabotageButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sabotageButtonActionPerformed(evt);
			}
		});

		escapeButton = new JButton("Escape");
		escapeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				escapeButtonActionPerformed(evt);
			}
		});

		marinesSeparator1 = new JSeparator(JSeparator.HORIZONTAL);
		marinesSeparator2 = new JSeparator(JSeparator.HORIZONTAL);
		marinesSeparator3 = new JSeparator(JSeparator.HORIZONTAL);
		marinesSeparator4 = new JSeparator(JSeparator.HORIZONTAL);

		javax.swing.GroupLayout marinesTabPanelLayout = new javax.swing.GroupLayout(marinesTabPanel);
		marinesTabPanel.setLayout(marinesTabPanelLayout);
		marinesTabPanelLayout
				.setHorizontalGroup(marinesTabPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								marinesTabPanelLayout
										.createSequentialGroup()
										.addGroup(
												marinesTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																marinesTabPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				marinesTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								marinesSeparator4,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								195, Short.MAX_VALUE)
																						.addGroup(
																								marinesTabPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												destinationShipRadioButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												destinationShipComboBox,
																												0,
																												100,
																												Short.MAX_VALUE))
																						.addComponent(
																								sourceBatteriesRadioButton)
																						.addComponent(
																								sourceInMoveRadioButton)
																						.addComponent(
																								sourceDeckRadioButton)
																						.addComponent(sourceLabel)
																						.addComponent(
																								destinationBatteriesRadioButton)
																						.addComponent(
																								destinationInMoveRadioButton)
																						.addComponent(
																								destinationDeckRadioButton)
																						.addComponent(destinationLabel)
																						.addComponent(
																								marinesSeparator3,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								195, Short.MAX_VALUE)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								marinesTabPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												moveMarinesButton,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												95,
																												Short.MAX_VALUE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												surrenderButton,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												94,
																												javax.swing.GroupLayout.PREFERRED_SIZE))))
														.addGroup(
																marinesTabPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(marinesSeparator1,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				195, Short.MAX_VALUE))
														.addGroup(
																marinesTabPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				marinesTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(commanderCheckBox)
																						.addGroup(
																								marinesTabPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												marinesCheckBox)
																										.addGap(18, 18,
																												18)
																										.addComponent(
																												marinesNumberSpinner,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												51,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addComponent(numberLabel)))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																marinesTabPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(marinesSeparator2,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				195, Short.MAX_VALUE))
														.addGroup(
																marinesTabPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				marinesTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(boardingLabel)
																						.addGroup(
																								marinesTabPanelLayout
																										.createSequentialGroup()
																										.addGap(10, 10,
																												10)
																										.addGroup(
																												marinesTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																attackButton,
																																javax.swing.GroupLayout.Alignment.TRAILING,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																185,
																																Short.MAX_VALUE)
																														.addGroup(
																																marinesTabPanelLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				enemyGroupLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																		.addComponent(
																																				enemyGroupComboBox,
																																				0,
																																				142,
																																				Short.MAX_VALUE))))
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								marinesTabPanelLayout
																										.createSequentialGroup()
																										.addGap(10, 10,
																												10)
																										.addComponent(
																												sabotageButton,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												85,
																												Short.MAX_VALUE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												escapeButton,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												94,
																												Short.MAX_VALUE)))))
										.addContainerGap()));
		marinesTabPanelLayout.setVerticalGroup(marinesTabPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				marinesTabPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(sourceLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(sourceDeckRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(sourceInMoveRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(sourceBatteriesRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(marinesSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(2, 2, 2)
						.addComponent(numberLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(
								marinesTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(marinesCheckBox)
										.addComponent(marinesNumberSpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(commanderCheckBox)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(marinesSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(destinationLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(destinationDeckRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(destinationInMoveRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(destinationBatteriesRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								marinesTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(destinationShipRadioButton)
										.addComponent(destinationShipComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(marinesSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								marinesTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(moveMarinesButton).addComponent(surrenderButton))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(marinesSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(boardingLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								marinesTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(enemyGroupLabel)
										.addComponent(enemyGroupComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(attackButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								marinesTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(escapeButton).addComponent(sabotageButton))
						.addContainerGap(122, Short.MAX_VALUE)));

		icon = createImageIcon("icons/marines.png");
		tabbedPane.addTab(null, icon, marinesTabPanel, "Move marines form ship");

		shootTabPanel = new JPanel();
		compartmentShootLabel = new JLabel("Compartment:");
		firingGunShootLabel = new JLabel("Type:");
		targetShipShootLabel = new JLabel("Ship:");
		aimAtShootLabel = new JLabel("Aim at:");
		aimedGunShootLabel = new JLabel("Cannon type:");

		bowShootRadioButton = new JRadioButton("bow");
		bowShootRadioButton.setActionCommand("bow");
		bowShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeShootActionPerformed(evt);
			}
		});

		leftSideShootRadioButton = new JRadioButton("left side");
		leftSideShootRadioButton.setActionCommand("left side");
		leftSideShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeShootActionPerformed(evt);
			}
		});

		rightSideShootRadioButton = new JRadioButton("right side");
		rightSideShootRadioButton.setActionCommand("right side");
		rightSideShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeShootActionPerformed(evt);
			}
		});

		sternShootRadioButton = new JRadioButton("stern");
		sternShootRadioButton.setActionCommand("stern");
		sternShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeShootActionPerformed(evt);
			}
		});

		lightOwnShootRadioButton = new JRadioButton("light");
		lightOwnShootRadioButton.setActionCommand("light");
		lightOwnShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				ownTypeShootActionPerformed(evt);
			}
		});

		mediumOwnShootRadioButton = new JRadioButton("medium");
		mediumOwnShootRadioButton.setActionCommand("medium");
		mediumOwnShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				ownTypeShootActionPerformed(evt);
			}
		});

		heavyOwnShootRadioButton = new JRadioButton("heavy");
		heavyOwnShootRadioButton.setActionCommand("heavy");
		heavyOwnShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				ownTypeShootActionPerformed(evt);
			}
		});

		riggingShootRadioButton = new JRadioButton("rigging");
		riggingShootRadioButton.setActionCommand("rigging");
		riggingShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aimAtTypeShootActionPerformed(evt);
			}
		});

		cannonShootRadioButton = new JRadioButton("cannon");
		cannonShootRadioButton.setActionCommand("cannon");
		cannonShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aimAtTypeShootActionPerformed(evt);
			}
		});

		hullShootRadioButton = new JRadioButton("hull");
		hullShootRadioButton.setActionCommand("hull");
		hullShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aimAtTypeShootActionPerformed(evt);
			}
		});

		lightAimedShootRadioButton = new JRadioButton("light");
		lightAimedShootRadioButton.setActionCommand("light");
		lightAimedShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aimedTypeShootActionPerformed(evt);
			}
		});

		mediumAimedShootRadioButton = new JRadioButton("medium");
		mediumAimedShootRadioButton.setActionCommand("medium");
		mediumAimedShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aimedTypeShootActionPerformed(evt);
			}
		});

		heavyAimedShootRadioButton = new JRadioButton("heavy");
		heavyAimedShootRadioButton.setActionCommand("heavy");
		heavyAimedShootRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				aimedTypeShootActionPerformed(evt);
			}
		});

		shootSeparator1 = new JSeparator();
		shootSeparator2 = new JSeparator();
		shootSeparator3 = new JSeparator();

		targetShipShootComboBox = new JComboBox<String>();
		targetShipShootComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				targetShipShootComboBoxActionPerformed(evt);
			}
		});

		shootShootButton = new JButton("Shoot");
		shootShootButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				shootShootButtonActionPerformed(evt);
			}
		});

		compartmentShootButtonGroup = new ButtonGroup();
		compartmentShootButtonGroup.add(bowShootRadioButton);
		compartmentShootButtonGroup.add(leftSideShootRadioButton);
		compartmentShootButtonGroup.add(rightSideShootRadioButton);
		compartmentShootButtonGroup.add(sternShootRadioButton);

		ownTypeShootButtonGroup = new ButtonGroup();
		ownTypeShootButtonGroup.add(lightOwnShootRadioButton);
		ownTypeShootButtonGroup.add(mediumOwnShootRadioButton);
		ownTypeShootButtonGroup.add(heavyOwnShootRadioButton);

		aimAtShootButtonGroup = new ButtonGroup();
		aimAtShootButtonGroup.add(riggingShootRadioButton);
		aimAtShootButtonGroup.add(cannonShootRadioButton);
		aimAtShootButtonGroup.add(hullShootRadioButton);

		aimedTypeShootButtonGroup = new ButtonGroup();
		aimedTypeShootButtonGroup.add(lightAimedShootRadioButton);
		aimedTypeShootButtonGroup.add(mediumAimedShootRadioButton);
		aimedTypeShootButtonGroup.add(heavyAimedShootRadioButton);

		javax.swing.GroupLayout shootTabPanelLayout = new javax.swing.GroupLayout(shootTabPanel);
		shootTabPanel.setLayout(shootTabPanelLayout);
		shootTabPanelLayout
				.setHorizontalGroup(shootTabPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								shootTabPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(shootShootButton,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE, 195,
																Short.MAX_VALUE)
														.addComponent(shootSeparator1,
																javax.swing.GroupLayout.DEFAULT_SIZE, 195,
																Short.MAX_VALUE)
														.addGroup(
																shootTabPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				shootTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								compartmentShootLabel)
																						.addComponent(
																								rightSideShootRadioButton)
																						.addComponent(
																								leftSideShootRadioButton)
																						.addComponent(
																								bowShootRadioButton))
																		.addGap(18, 18, 18)
																		.addGroup(
																				shootTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								firingGunShootLabel)
																						.addComponent(
																								lightOwnShootRadioButton)
																						.addComponent(
																								mediumOwnShootRadioButton)
																						.addComponent(
																								heavyOwnShootRadioButton)))
														.addComponent(sternShootRadioButton)
														.addGroup(
																shootTabPanelLayout
																		.createSequentialGroup()
																		.addComponent(targetShipShootLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(targetShipShootComboBox, 0, 161,
																				Short.MAX_VALUE))
														.addComponent(shootSeparator2,
																javax.swing.GroupLayout.DEFAULT_SIZE, 195,
																Short.MAX_VALUE)
														.addGroup(
																shootTabPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				shootTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(aimAtShootLabel)
																						.addComponent(
																								riggingShootRadioButton)
																						.addComponent(
																								cannonShootRadioButton)
																						.addComponent(
																								hullShootRadioButton))
																		.addGap(32, 32, 32)
																		.addGroup(
																				shootTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								heavyAimedShootRadioButton)
																						.addComponent(
																								mediumAimedShootRadioButton)
																						.addComponent(
																								lightAimedShootRadioButton)
																						.addComponent(
																								aimedGunShootLabel)))
														.addComponent(shootSeparator3,
																javax.swing.GroupLayout.DEFAULT_SIZE, 195,
																Short.MAX_VALUE)).addContainerGap()));
		shootTabPanelLayout
				.setVerticalGroup(shootTabPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								shootTabPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
														.addGroup(
																shootTabPanelLayout
																		.createSequentialGroup()
																		.addComponent(compartmentShootLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(bowShootRadioButton)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(leftSideShootRadioButton)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(rightSideShootRadioButton))
														.addGroup(
																shootTabPanelLayout
																		.createSequentialGroup()
																		.addComponent(firingGunShootLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(lightOwnShootRadioButton)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(mediumOwnShootRadioButton)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(heavyOwnShootRadioButton)))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(sternShootRadioButton)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(shootSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(targetShipShootLabel)
														.addComponent(targetShipShootComboBox,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(shootSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(aimAtShootLabel).addComponent(aimedGunShootLabel))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(riggingShootRadioButton)
														.addComponent(lightAimedShootRadioButton))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(cannonShootRadioButton)
														.addComponent(mediumAimedShootRadioButton))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												shootTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(hullShootRadioButton)
														.addComponent(heavyAimedShootRadioButton))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(shootSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(shootShootButton).addContainerGap(282, Short.MAX_VALUE)));

		icon = createImageIcon("icons/shellfire.png");
		tabbedPane.addTab(null, icon, shootTabPanel, "Fire at passing ship with guns");

		// Tab: cargo
		cargoTabPanel = new JPanel();

		shipCargoLabel = new JLabel("Ship:");
		fromCargoLabel = new JLabel("From:");
		toCargoLabel = new JLabel("To:");
		typeCargoLabel = new JLabel("Cargo type:");
		compartmentCargoLabel = new JLabel("Source comp:");
		targetCompartmentCargoLabel = new JLabel("Target comp:");
		quantityCargoLabel = new JLabel("Quantity:");

		shipCargoComboBox = new JComboBox<String>();
		shipCargoComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				shipCargoComboBoxActionPerformed(evt);
			}
		});

		cargoSeparator1 = new JSeparator();
		cargoSeparator2 = new JSeparator();
		cargoSeparator3 = new JSeparator();

		cargoFromCargoRadioButton = new JRadioButton("cargo");
		cargoFromCargoRadioButton.setActionCommand("cargo");
		cargoFromCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				fromTypeCargoActionPerformed(evt);
			}
		});

		batteriesFromCargoRadioButton = new JRadioButton("batteries");
		batteriesFromCargoRadioButton.setActionCommand("batteries");
		batteriesFromCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				fromTypeCargoActionPerformed(evt);
			}
		});

		cargoToCargoRadioButton = new JRadioButton("cargo");
		cargoToCargoRadioButton.setActionCommand("cargo");
		cargoToCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				toTypeCargoActionPerformed(evt);
			}
		});

		batteriesToCargoRadioButton = new JRadioButton("batteries");
		batteriesToCargoRadioButton.setActionCommand("batteries");
		batteriesToCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				toTypeCargoActionPerformed(evt);
			}
		});

		silverCargoRadioButton = new JRadioButton("silver");
		silverCargoRadioButton.setActionCommand("silver");
		silverCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				cargoTypeCargoActionPerformed(evt);
			}
		});

		lightCargoRadioButton = new JRadioButton("light guns");
		lightCargoRadioButton.setActionCommand("light");
		lightCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				cargoTypeCargoActionPerformed(evt);
			}
		});

		mediumCargoRadioButton = new JRadioButton("medium guns");
		mediumCargoRadioButton.setActionCommand("medium");
		mediumCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				cargoTypeCargoActionPerformed(evt);
			}
		});

		bowCargoRadioButton = new JRadioButton("bow");
		bowCargoRadioButton.setActionCommand("bow");
		bowCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeCargoActionPerformed(evt);
			}
		});

		leftCargoRadioButton = new JRadioButton("left side");
		leftCargoRadioButton.setActionCommand("left");
		leftCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeCargoActionPerformed(evt);
			}
		});

		rightCargoRadioButton = new JRadioButton("right side");
		rightCargoRadioButton.setActionCommand("right");
		rightCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeCargoActionPerformed(evt);
			}
		});

		sternCargoRadioButton = new JRadioButton("stern");
		sternCargoRadioButton.setActionCommand("stern");
		sternCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				compartmentTypeCargoActionPerformed(evt);
			}
		});

		targetBowCargoRadioButton = new JRadioButton("bow");
		targetBowCargoRadioButton.setActionCommand("bow");
		targetBowCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				targetCompartmentTypeCargoActionPerformed(evt);
			}
		});

		targetLeftCargoRadioButton = new JRadioButton("left side");
		targetLeftCargoRadioButton.setActionCommand("left");
		targetLeftCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				targetCompartmentTypeCargoActionPerformed(evt);
			}
		});

		targetRightCargoRadioButton = new JRadioButton("right side");
		targetRightCargoRadioButton.setActionCommand("right");
		targetRightCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				targetCompartmentTypeCargoActionPerformed(evt);
			}
		});

		targetSternCargoRadioButton = new JRadioButton("stern");
		targetSternCargoRadioButton.setActionCommand("stern");
		targetSternCargoRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				targetCompartmentTypeCargoActionPerformed(evt);
			}
		});

		fromCargoButtonGroup = new ButtonGroup();
		fromCargoButtonGroup.add(cargoFromCargoRadioButton);
		fromCargoButtonGroup.add(batteriesFromCargoRadioButton);

		toCargoButtonGroup = new ButtonGroup();
		toCargoButtonGroup.add(cargoToCargoRadioButton);
		toCargoButtonGroup.add(batteriesToCargoRadioButton);

		typeCargoButtonGroup = new ButtonGroup();
		typeCargoButtonGroup.add(silverCargoRadioButton);
		typeCargoButtonGroup.add(lightCargoRadioButton);
		typeCargoButtonGroup.add(mediumCargoRadioButton);

		compartmentCargoButtonGroup = new ButtonGroup();
		compartmentCargoButtonGroup.add(bowCargoRadioButton);
		compartmentCargoButtonGroup.add(leftCargoRadioButton);
		compartmentCargoButtonGroup.add(rightCargoRadioButton);
		compartmentCargoButtonGroup.add(sternCargoRadioButton);

		targetCompartmentCargoButtonGroup = new ButtonGroup();
		targetCompartmentCargoButtonGroup.add(targetBowCargoRadioButton);
		targetCompartmentCargoButtonGroup.add(targetLeftCargoRadioButton);
		targetCompartmentCargoButtonGroup.add(targetRightCargoRadioButton);
		targetCompartmentCargoButtonGroup.add(targetSternCargoRadioButton);

		handleCargoButton = new JButton("Handle");
		handleCargoButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				handleCargoButtonActionPerformed(evt);
			}
		});

		uncoupleCargoButton = new JButton("Uncouple");
		uncoupleCargoButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				uncoupleCargoButtonActionPerformed(evt);
			}
		});

		quantityCargoSpinner = new JSpinner();
		quantityCargoSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				quantityCargoNumberChanged(evt);
			}
		});

		setExplosivesCargoButton = new JButton("Set explosives");
		setExplosivesCargoButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				setExplosivesCargoButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout cargoTabPanelLayout = new javax.swing.GroupLayout(cargoTabPanel);
		cargoTabPanel.setLayout(cargoTabPanelLayout);
		cargoTabPanelLayout
				.setHorizontalGroup(cargoTabPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								cargoTabPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												cargoTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																cargoTabPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				cargoTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								lightCargoRadioButton)
																						.addComponent(
																								silverCargoRadioButton)
																						.addComponent(
																								mediumCargoRadioButton)
																						.addComponent(typeCargoLabel))
																		.addContainerGap(118, Short.MAX_VALUE))
														.addGroup(
																cargoTabPanelLayout
																		.createSequentialGroup()
																		.addComponent(quantityCargoLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(quantityCargoSpinner,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				48,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addContainerGap(101, Short.MAX_VALUE))
														.addGroup(
																cargoTabPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				cargoTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								compartmentCargoLabel)
																						.addComponent(
																								rightCargoRadioButton)
																						.addComponent(
																								bowCargoRadioButton)
																						.addComponent(
																								leftCargoRadioButton)
																						.addComponent(
																								sternCargoRadioButton))
																		.addGap(18, 18, 18)
																		.addGroup(
																				cargoTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								targetCompartmentCargoLabel)
																						.addComponent(
																								targetRightCargoRadioButton)
																						.addComponent(
																								targetBowCargoRadioButton)
																						.addComponent(
																								targetLeftCargoRadioButton)
																						.addComponent(
																								targetSternCargoRadioButton))
																		.addGap(127, 127, 127))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																cargoTabPanelLayout
																		.createSequentialGroup()
																		.addGroup(
																				cargoTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addComponent(
																								setExplosivesCargoButton,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								164, Short.MAX_VALUE)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								cargoTabPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												handleCargoButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												22,
																												Short.MAX_VALUE)
																										.addComponent(
																												uncoupleCargoButton))
																						.addComponent(
																								cargoSeparator3,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								164, Short.MAX_VALUE)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								cargoTabPanelLayout
																										.createSequentialGroup()
																										.addGroup(
																												cargoTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																cargoFromCargoRadioButton)
																														.addComponent(
																																batteriesFromCargoRadioButton)
																														.addComponent(
																																fromCargoLabel))
																										.addGap(18, 18,
																												18)
																										.addGroup(
																												cargoTabPanelLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																toCargoLabel)
																														.addComponent(
																																cargoToCargoRadioButton)
																														.addComponent(
																																batteriesToCargoRadioButton)))
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								cargoTabPanelLayout
																										.createSequentialGroup()
																										.addComponent(
																												shipCargoLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												shipCargoComboBox,
																												0,
																												130,
																												Short.MAX_VALUE))
																						.addComponent(
																								cargoSeparator2,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								164, Short.MAX_VALUE)
																						.addComponent(
																								cargoSeparator1,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								164, Short.MAX_VALUE))
																		.addGap(119, 119, 119)))));
		cargoTabPanelLayout.setVerticalGroup(cargoTabPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				cargoTabPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								cargoTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(shipCargoLabel)
										.addComponent(shipCargoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(cargoSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								cargoTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(
												cargoTabPanelLayout
														.createSequentialGroup()
														.addComponent(fromCargoLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(cargoFromCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(batteriesFromCargoRadioButton))
										.addGroup(
												cargoTabPanelLayout
														.createSequentialGroup()
														.addComponent(toCargoLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(cargoToCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(batteriesToCargoRadioButton)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(cargoSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(typeCargoLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(silverCargoRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(lightCargoRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(mediumCargoRadioButton)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(
								cargoTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(
												cargoTabPanelLayout
														.createSequentialGroup()
														.addComponent(compartmentCargoLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(bowCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(leftCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(rightCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(sternCargoRadioButton))
										.addGroup(
												cargoTabPanelLayout
														.createSequentialGroup()
														.addComponent(targetCompartmentCargoLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(targetBowCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(targetLeftCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(targetRightCargoRadioButton)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(targetSternCargoRadioButton)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(
								cargoTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(quantityCargoLabel)
										.addComponent(quantityCargoSpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(cargoSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								cargoTabPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(handleCargoButton).addComponent(uncoupleCargoButton))
						.addGap(18, 18, 18).addComponent(setExplosivesCargoButton)
						.addContainerGap(146, Short.MAX_VALUE)));

		icon = createImageIcon("icons/cargo.png");
		tabbedPane.addTab(null, icon, cargoTabPanel, "Handle cargo from ship");

		statsTabPanel = new JPanel();
		idStatsLabel = new JLabel();
		ownerStatsLabel = new JLabel();
		classStatsLabel = new JLabel();
		positionStatsLabel = new JLabel();
		rotationStatsLabel = new JLabel();
		moveOverStatsLabel = new JLabel();
		actionsOverStatsLabel = new JLabel();
		hullStatsLabel = new JLabel();
		helmStatsLabel = new JLabel();
		mastStatsLabel = new JLabel();
		isWreckStatsLabel = new JLabel();
		isImmobilizedStatsLabel = new JLabel();
		isExplosiveStatsLabel = new JLabel();
		tugStatsLabel = new JLabel();
		towedStatsLabel = new JLabel();
		coupledStatsLabel = new JLabel();
		silverLoadStatsLabel = new JLabel();
		lightCannonsLoadStatsLabel = new JLabel();
		mediumCannonsLoadStatsLabel = new JLabel();
		bftStatsLabel = new JLabel();
		teauStatsLabel = new JLabel();
		attemptsUsedStatsLabel = new JLabel();
		happinessStatsLabel = new JLabel();
		happinessSinkStatsLabel = new JLabel();
		happinessBoardingStatsLabel = new JLabel();

		cannonsAStatsTable = new JTable();
		cannonsBStatsTable = new JTable();
		marinesAStatsTable = new JTable();
		marinesBStatsTable = new JTable();

		javax.swing.GroupLayout statsTabPanelLayout = new javax.swing.GroupLayout(statsTabPanel);
		statsTabPanel.setLayout(statsTabPanelLayout);
		statsTabPanelLayout.setHorizontalGroup(statsTabPanelLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				statsTabPanelLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								statsTabPanelLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addComponent(cannonsAStatsTable.getTableHeader())
										.addComponent(cannonsAStatsTable, 0, 0, Short.MAX_VALUE)
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(idStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(ownerStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(classStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(hullStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(positionStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(rotationStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(helmStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(mastStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(moveOverStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(isWreckStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(isImmobilizedStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(isExplosiveStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(tugStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(towedStatsLabel))
										.addComponent(coupledStatsLabel)
										.addComponent(cannonsBStatsTable.getTableHeader())
										.addComponent(cannonsBStatsTable, 0, 0, Short.MAX_VALUE)
										.addComponent(marinesAStatsTable.getTableHeader())
										.addComponent(marinesAStatsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 195,
												Short.MAX_VALUE)
										.addComponent(marinesBStatsTable.getTableHeader())
										.addComponent(marinesBStatsTable, javax.swing.GroupLayout.DEFAULT_SIZE, 195,
												Short.MAX_VALUE)
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(silverLoadStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(lightCannonsLoadStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(mediumCannonsLoadStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(actionsOverStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(teauStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(happinessStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(happinessSinkStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(happinessBoardingStatsLabel))
										.addGroup(
												statsTabPanelLayout
														.createSequentialGroup()
														.addComponent(bftStatsLabel)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
														.addComponent(attemptsUsedStatsLabel))).addContainerGap()));
		statsTabPanelLayout
				.setVerticalGroup(statsTabPanelLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								statsTabPanelLayout
										.createSequentialGroup()
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																statsTabPanelLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addGroup(
																				statsTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(idStatsLabel)
																						.addComponent(ownerStatsLabel))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				statsTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(classStatsLabel)
																						.addComponent(hullStatsLabel)))
														.addGroup(
																statsTabPanelLayout
																		.createSequentialGroup()
																		.addGap(50, 50, 50)
																		.addGroup(
																				statsTabPanelLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(
																								positionStatsLabel)
																						.addComponent(
																								rotationStatsLabel))))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(helmStatsLabel).addComponent(mastStatsLabel)
														.addComponent(moveOverStatsLabel))
										.addGap(10, 10, 10)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(isWreckStatsLabel)
														.addComponent(isImmobilizedStatsLabel)
														.addComponent(isExplosiveStatsLabel))
										.addGap(5, 5, 5)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(tugStatsLabel).addComponent(towedStatsLabel))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(coupledStatsLabel)
										.addGap(5, 5, 5)
										.addComponent(cannonsAStatsTable.getTableHeader())
										.addComponent(cannonsAStatsTable, javax.swing.GroupLayout.PREFERRED_SIZE, 18,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(cannonsBStatsTable.getTableHeader())
										.addComponent(cannonsBStatsTable, javax.swing.GroupLayout.PREFERRED_SIZE, 18,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(5, 5, 5)
										.addComponent(marinesAStatsTable.getTableHeader())
										.addComponent(marinesAStatsTable, javax.swing.GroupLayout.PREFERRED_SIZE, 65,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(marinesBStatsTable.getTableHeader())
										.addComponent(marinesBStatsTable, javax.swing.GroupLayout.PREFERRED_SIZE, 65,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(5, 5, 5)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(silverLoadStatsLabel)
														.addComponent(lightCannonsLoadStatsLabel)
														.addComponent(mediumCannonsLoadStatsLabel))
										.addGap(5, 5, 5)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(actionsOverStatsLabel)
														.addComponent(teauStatsLabel))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(bftStatsLabel)
														.addComponent(attemptsUsedStatsLabel,
																javax.swing.GroupLayout.PREFERRED_SIZE, 14,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												statsTabPanelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(happinessStatsLabel)
														.addComponent(happinessSinkStatsLabel)
														.addComponent(happinessBoardingStatsLabel))
										.addContainerGap(17, Short.MAX_VALUE)));

		icon = createImageIcon("icons/info.png");
		tabbedPane.addTab(null, icon, statsTabPanel, "Info about ship");

		// The following line enables to use scrolling tabs.
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// End of Tabbed Pane creation

		f.pack();
		f.setVisible(true);

		// ---- Allies Panel
		alliesFrame = new JFrame("Allies");
		alliesFrame.setPreferredSize(new Dimension(160, 350));

		alliesPanel = new JPanel();
		alliesPanel.setBackground(Color.darkGray);
		alliesPanel.setLayout(null);

		pasadenaAlliesLabel = new javax.swing.JLabel();
		elmethAlliesLabel = new javax.swing.JLabel();
		sidoniaAlliesLabel = new javax.swing.JLabel();
		pleensyAlliesLabel = new javax.swing.JLabel();
		hampshireAlliesLabel = new javax.swing.JLabel();
		discasterAlliesLabel = new javax.swing.JLabel();
		delacroixAlliesLabel = new javax.swing.JLabel();
		leppoAlliesLabel = new javax.swing.JLabel();
		allyAlliesLabel = new javax.swing.JLabel();
		leppoAlliesCheckBox = new javax.swing.JCheckBox();
		pasadenaAlliesCheckBox = new javax.swing.JCheckBox();
		elmethAlliesCheckBox = new javax.swing.JCheckBox();
		sidoniaAlliesCheckBox = new javax.swing.JCheckBox();
		pleensyAlliesCheckBox = new javax.swing.JCheckBox();
		hampshireAlliesCheckBox = new javax.swing.JCheckBox();
		discasterAlliesCheckBox = new javax.swing.JCheckBox();
		delacroixAlliesCheckBox = new javax.swing.JCheckBox();
		closeAlliesButton = new JButton();

		pasadenaAlliesLabel.setForeground(Color.green);
		pasadenaAlliesLabel.setText("Pasadena");
		alliesPanel.add(pasadenaAlliesLabel);
		pasadenaAlliesLabel.setBounds(10, 30, 80, 14);

		elmethAlliesLabel.setForeground(Color.magenta);
		elmethAlliesLabel.setText("Elmeth");
		alliesPanel.add(elmethAlliesLabel);
		elmethAlliesLabel.setBounds(10, 60, 80, 14);

		sidoniaAlliesLabel.setForeground(Color.orange);
		sidoniaAlliesLabel.setText("Sidonia");
		alliesPanel.add(sidoniaAlliesLabel);
		sidoniaAlliesLabel.setBounds(10, 90, 80, 14);

		pleensyAlliesLabel.setForeground(Color.pink);
		pleensyAlliesLabel.setText("Pleensy");
		alliesPanel.add(pleensyAlliesLabel);
		pleensyAlliesLabel.setBounds(10, 120, 80, 14);

		hampshireAlliesLabel.setForeground(Color.white);
		hampshireAlliesLabel.setText("Hampshire");
		alliesPanel.add(hampshireAlliesLabel);
		hampshireAlliesLabel.setBounds(10, 150, 80, 14);

		discasterAlliesLabel.setForeground(Color.yellow);
		discasterAlliesLabel.setText("Discaster");
		alliesPanel.add(discasterAlliesLabel);
		discasterAlliesLabel.setBounds(10, 180, 80, 14);

		delacroixAlliesLabel.setForeground(Color.cyan);
		delacroixAlliesLabel.setText("Delacroix");
		alliesPanel.add(delacroixAlliesLabel);
		delacroixAlliesLabel.setBounds(10, 210, 80, 14);

		leppoAlliesLabel.setForeground(Color.lightGray);
		leppoAlliesLabel.setText("Leppo");
		alliesPanel.add(leppoAlliesLabel);
		leppoAlliesLabel.setBounds(10, 240, 80, 14);

		allyAlliesLabel.setForeground(Color.lightGray);
		allyAlliesLabel.setText("Ally");
		alliesPanel.add(allyAlliesLabel);
		allyAlliesLabel.setBounds(100, 10, 25, 14);
		alliesPanel.add(leppoAlliesCheckBox);
		leppoAlliesCheckBox.setBounds(100, 240, 20, 21);
		alliesPanel.add(pasadenaAlliesCheckBox);
		pasadenaAlliesCheckBox.setBounds(100, 30, 20, 21);
		alliesPanel.add(elmethAlliesCheckBox);
		elmethAlliesCheckBox.setBounds(100, 60, 20, 21);
		alliesPanel.add(sidoniaAlliesCheckBox);
		sidoniaAlliesCheckBox.setBounds(100, 90, 20, 21);
		alliesPanel.add(pleensyAlliesCheckBox);
		pleensyAlliesCheckBox.setBounds(100, 120, 20, 21);
		alliesPanel.add(hampshireAlliesCheckBox);
		hampshireAlliesCheckBox.setBounds(100, 150, 20, 21);
		alliesPanel.add(discasterAlliesCheckBox);
		discasterAlliesCheckBox.setBounds(100, 180, 20, 21);
		alliesPanel.add(delacroixAlliesCheckBox);
		delacroixAlliesCheckBox.setBounds(100, 210, 20, 21);

		closeAlliesButton.setText("Close");
		alliesPanel.add(closeAlliesButton);
		closeAlliesButton.setBounds(30, 280, 75, 23);

		pasadenaAlliesCheckBox.setActionCommand(Player.PASADENA.name());
		pasadenaAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		elmethAlliesCheckBox.setActionCommand(Player.ELMETH.name());
		elmethAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		sidoniaAlliesCheckBox.setActionCommand(Player.SIDONIA.name());
		sidoniaAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		pleensyAlliesCheckBox.setActionCommand(Player.PLEENSY.name());
		pleensyAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		hampshireAlliesCheckBox.setActionCommand(Player.HAMPSHIRE.name());
		hampshireAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		discasterAlliesCheckBox.setActionCommand(Player.DISCASTER.name());
		discasterAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		delacroixAlliesCheckBox.setActionCommand(Player.DELACROIX.name());
		delacroixAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		leppoAlliesCheckBox.setActionCommand(Player.LEPPO.name());
		leppoAlliesCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				allyCheckButtonActionPerformed(evt);
			}
		});

		closeAlliesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				closeAlliesButtonActionPerformed(evt);
			}
		});

		alliesFrame.add(alliesPanel);
		alliesFrame.pack();

		// About Panel

		aboutFrame = new JFrame("About");
		aboutFrame.setPreferredSize(new Dimension(300, 480));

		aboutPanel = new AboutPanel();
		aboutFrame.add(aboutPanel);
		aboutFrame.pack();
	}


	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = MainBoard.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}


	protected static JComponent makeTextPanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridLayout(1, 1));
		panel.add(filler);
		return panel;
	}


	public static void makeHeaderLabel() {
		String blankStr = "                                  ";
		String str = "";

		str += "      ";
		str += "Conflict: ";
		str += blankStr.substring(0, 2 - String.valueOf(game.getConflictID()).length());
		str += game.getConflictID();
		str += blankStr.substring(0, 5);

		str += "Turn: ";
		str += blankStr.substring(0, 2 - String.valueOf(game.getConflictID()).length());
		str += game.getTurnID();
		str += blankStr.substring(0, 5);

		str += "Stage: ";
		str += game.getStage().getDescription();
		str += blankStr.substring(0, 16 - Stage.values().length);
		str += blankStr.substring(0, 5);

		str += blankStr.substring(0, 5);

		str += "Wind: ";
		str += blankStr.substring(0, 2 - String.valueOf(game.getWindSpeed()).length());
		str += game.getWindSpeed();
		str += " (";
		str += game.getWindDirection().toString();
		str += blankStr.substring(0, 2 - game.getWindDirection().toString().length());
		str += ")";
		str += blankStr.substring(0, 5);

		str += blankStr.substring(0, 5);

		str += "Current player: ";
		str += game.getCurrentPlayer().toString();
		str += blankStr.substring(0, 9 - game.getCurrentPlayer().toString().length());
		str += blankStr.substring(0, 5);

		str += "Selected ship: ";
		if (ship == null)
			str += "none";
		else {
			str += blankStr.substring(0, 2 - String.valueOf(ship.getID()).length());
			str += ship.getID();
			str += "   ";
		}

		headerLabel.setText(str);
	}


	// ---- DEPLOYER's METHODS

	public static boolean isSelectable(int shipID) {
		Ship s = game.getShip(shipID);
		if (s.getOwner() != game.getCurrentPlayer())
			return false;
		else
			return true;
	}


	public static boolean isDeployable(int shipID, Coordinate hexCoord) {
		Ship s = game.getShip(shipID);

		Hex hex = game.getBoard().getHex(hexCoord);
		if (s.getOwner() == Player.PASADENA || s.getOwner() == Player.SIDONIA
				|| s.getOwner() == Player.ELMETH || s.getOwner() == Player.PLEENSY) {
			if (s.getOwner() != hex.owner)
				return false;
		}
		// par. 6.2
		else {
			if (!game.checkIfAlly(s.getOwner(), hex.owner))
				return false;
		}
		// --

		// umieszczenie dowodcy na okrecie po dwukrotnym zaznaczeniu statku
		if (hex.terrain == Terrain.ISLAND || hex.ship != null) {
			if (hex.ship.getID() == boardPanel.getClipBoardShip()) {
				Ships.deployCommander(game.getShip(boardPanel.getClipBoardShip()));
			} 
			return false;
		}

		return true;
	}


	public static void moveShipToPosition(int shipID, Coordinate hex) {
		// XXX: nie trzeba usuwac z obecnej pozycji?
		Ship s = game.getShip(shipID);
		s.setPosition(hex.getA(), hex.getB());
	}


	public static DisplayMode getBoardPanelMode() {
		return boardPanelMode;
	}


	// ---- HELPERS
	private static Integer extractIDFromObject(Object object) {
		if (object.toString().equals(""))
			return null;
		return Integer.valueOf((object.toString()).substring(0, (object.toString()).indexOf(',')));
	}
}

@SuppressWarnings("serial")
class CenteredCellRenderer extends DefaultTableCellRenderer {

	@Override
	public void setValue(Object value) {
		if (value == null)
			return;

		String str = value.toString();

		if (str.length() == 0)
			return;

		if (str.charAt(0) == '0' && (str.contains("|") && str.charAt(str.indexOf('|') + 1) == '0') || str.equals("n/t"))
			setForeground(Color.BLACK);
		else
			setForeground(Color.RED);
		setText(value.toString());
	}
}