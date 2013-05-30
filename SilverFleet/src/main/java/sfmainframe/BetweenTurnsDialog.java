package sfmainframe;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import sfmainframe.gameplay.between.Auction;
import sfmainframe.gameplay.between.DealType;
import sfmainframe.gameplay.between.RepairType;
import sfmainframe.gameplay.between.TransferLocation;
import sfmainframe.gui.UpdateMode;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;
import sfmainframe.ship.Ship;
import sfmainframe.ship.ShipClass;
import sfmainframe.ship.cargo.TransferCargo;
import sfmainframe.ship.marines.MarinesCompartment;

public class BetweenTurnsDialog extends JPanel {

	public static final int SHIPYARD_PRICE = 1000;
	public static final int FOUNDRY_PRICE = 300;
	
	private final DefaultTableModel shipFigureDefaultModel = new DefaultTableModel(new Object[][] {
			{ "BOW", "0|0", "0|0", "0|0" }, { "LEFT SIDE", "0|0", "0|0", "0|0" },
			{ "RIGHT SIDE", "0|0", "0|0", "0|0" }, { "STERN", "0|0", "0|0", "0|0" } }, new String[] { "Compartment",
			"Light", "Medium", "Heavy" });	
	
	/*
	// private JFrame f;
	private JTabbedPane tabbedPane;

	// Exchange Panel
	private JPanel exchangeTabPanel;

	private JButton buildShipButton, buyFoundryButton, buyShipyardButton, buyCannonsButton, hireMarinesButton,
			repairButton, sellCannonsButton, sellShipButton, sellSilverButton, submitOfferButton,
			transferTransferButton;

	private JComboBox buildShipClassComboBox, fromShipTransferComboBox, partnerContractComboBox, repairShipComboBox,
			sellShipComboBox, toShipTransferComboBox;

	private JLabel buyShipClassLabel, cannonsContractLabel, cannonsFromTransferLabel, cannonsToTransferLabel,
			cargoTransferLabel, contractLabel, fromTransferLabel, goldContractLabel, shipPriceLabel, hireMarinesLabel,
			hireMarinesNumberLabel, marinesContractLabel, marinesFromTransferLabel, marinesToTransferLabel,
			myContractLabel, myGoldPiecesContractLabel, myHeavyContractLabel, myLightContractLabel,
			myMarinesNumberContractLabel, myMediumContractLabel, numberTransferLabel, partnerContractLabel,
			partnerGoldPiecesContractLabel, partnerHeavyContractLabel, partnerLightContractLabel,
			partnerMarinesNumberContractLabel, partnerMediumContractLabel, repairIDLabel, repairLabel,
			repairPointsLabel, sellCannonsLabel, sellCannonsNumberLabel, sellCannonsTypeLabel, sellShipIDLabel,
			sellShipLabel, sellSilverAmountLabel, sellSilverLabel, shipsContractLabel, toTransferLabel, transferLabel,
			silverContractLabel, mySilverTonsContractLabel, partnerSilverTonsContractLabel;

	private JRadioButton cannonsFromBowTransferRadioButton, cannonsFromLeftSideTransferRadioButton,
			cannonsFromRightSideTransferRadioButton, cannonsFromSternTransferRadioButton,
			cannonsToBowTransferRadioButton, cannonsToLeftSideTransferRadioButton,
			cannonsToRightSideTransferRadioButton, cannonsToSternTransferRadioButton, fromShipTransferRadioButton,
			fromStorehouseTransferRadioButton, heavyTransferRadioButton, lightTransferRadioButton,
			marinesFromBatteriesTransferRadioButton, marinesFromDeckTransferRadioButton,
			marinesToBatteriesTransferRadioButton, marinesToDeckTransferRadioButton, marinesTransferRadioButton,
			mediumTransferRadioButton, repairDurabilityRadioButton, repairHelmRadioButton, repairMastRadioButton,
			sellCannonsHeavyRadioButton, sellCannonsLightRadioButton, sellCannonsMediumRadioButton,
			toShipTransferRadioButton, toStorehouseTransferRadioButton;

	private JSeparator hSeparator1, hSeparator2, hSeparator3, hSeparator4, hSeparator5, hSeparator6, hSeparator7,
			hSeparator8, hSeparator9;

	private JSpinner hireMarinesNumberSpinner, myGoldPiecesContractSpinner, myHeavyContractSpinner,
			myLightContractSpinner, myMarinesContractSpinner, myMediumContractSpinner,
			partnerGoldPiecesContractSpinner, partnerHeavyContractSpinner, partnerLightContractSpinner,
			partnerMarinesContractSpinner, partnerMediumContractSpinner, repairPointsSpinner, sellCannonsSpinner,
			sellSilverSpinner, transferNumberSpinner, mySilverTonsContractSpinner, partnerSilverTonsContractSpinner;

	private JCheckBox myFreeCommanderContractCheckBox, partnerFreeCommanderContractCheckBox;

	private JList myShipsContractList, partnerShipsContractList;

	private JScrollPane myShipsContractScrollPane, myShipsEnContractScrollPane, partnerShipsContractScrollPane,
			partnerShipsEnContractScrollPane;

	private JSeparator vSeparator1, vSeparator2;

	private SpinnerModel sm;

	private ButtonGroup repairsButtonGroup, transferFromButtonGroup, transferToButtonGroup, transferCargoButtonGroup,
			transferMarinesFromButtonGroup, transferMarinesToButtonGroup, transferCannonsFromButtonGroup,
			transferCannonsToButtonGroup, sellCannonsButtonGroup;

	private Player currentPlayer = Player.NONE;
	private PlayerClass pl = null;

	private Ship sh = MainBoard.game.getShip(0);
	private Ship sh2 = MainBoard.game.getShip(1);

	private RepairType selectedRepairType = RepairType.DURABILITY;

	private Integer transferFrom = null;
	private Integer transferTo = null;
	private TransferCargo transferCargo = TransferCargo.NULL;
	private Gun transferGunType = Gun.NONE;
	private MarinesCompartment transferMarinesFrom = MarinesCompartment.NONE;
	private MarinesCompartment transferMarinesTo = MarinesCompartment.NONE;
	private GunCompartment transferCannonFrom = GunCompartment.NONE;
	private GunCompartment transferCannonTo = GunCompartment.NONE;

	private Gun selectedSellCannonType = Gun.NONE;

	private Player selectedClient = Player.NONE;

	// Bank Panel
	private JPanel bankTabPanel;

	private JLabel auctionBankLabel, currentLoanBankLabel, lastOfferBankLabel, loanAmountBankLabel, loanBankLabel,
			shipClassBankLabel, shipDurabilityBankLabel, shipFigureBankLabel, shipHelmBankLabel, shipIDBankLabel,
			shipMastBankLabel, shipRepairBankLabel, yourOfferBankLabel;

	private JSeparator hBankSeparator1;
	private JSeparator vBankSeparator1;

	private JButton repayLoanBankButton, submitOfferBankButton, takeLoanBankButton;

	private JList auctionsBankList;
	private JScrollPane auctionsBankScrollPane;

	private JSpinner loanAmountBankSpinner;
	private JTable shipCannonsBankTable;
	private JSpinner yourOfferBankSpinner;

	// Summary Panel
	private JPanel summaryPanel;

	private JButton nextPlayerButton;
	private JButton passButton; // zrezygnowanie z kolejnych tur

	private JLabel currentPlayerLabel;

	// ---- zmienne pomocnicze
	private boolean updateFinished = true;


	BetweenTurnsDialog() {
		init();
		setDefaults();
		// update(UpdateMode.DEFAULT);
	}


	public void setDefaults() {
		transferFrom = Commons.STOREHOUSE_CODE;
		transferTo = Commons.STOREHOUSE_CODE;
		transferCargo = TransferCargo.MARINES;
		transferGunType = Gun.NONE;
		transferMarinesFrom = MarinesCompartment.DECK;
		transferMarinesTo = MarinesCompartment.DECK;
		transferCannonFrom = GunCompartment.BOW;
		transferCannonTo = GunCompartment.BOW;

		marinesFromDeckTransferRadioButton.setSelected(true);
		marinesToDeckTransferRadioButton.setSelected(true);
		cannonsFromBowTransferRadioButton.setSelected(true);
		cannonsToBowTransferRadioButton.setSelected(true);

		selectedSellCannonType = Gun.LIGHT;
		sellCannonsLightRadioButton.setSelected(true);
	}


	private void sellSilverSpinnerChanged(ChangeEvent evt) {
		if ((Integer) (sellSilverSpinner.getValue()) == 0)
			sellSilverButton.setEnabled(false);
		else
			sellSilverButton.setEnabled(true);
	}


	private void sellSilverButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.getPlayer(currentPlayer).sellSilver((Integer) (sellSilverSpinner.getValue()));

		updatePlayer();

		updateSellSilverSection(UpdateMode.DEFAULT);
		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.CONTINUE);
		updateCannonsSection();
	}


	private void repairShipComboBoxActionPerformed(ActionEvent evt) {
		if (updateFinished)
			updateRepairSection(UpdateMode.RADIO_BUTTONS);
	}


	private void repairShipActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("durability"))
			selectedRepairType = RepairType.DURABILITY;
		if (actionCommand.equals("mast"))
			selectedRepairType = RepairType.HELM;
		if (actionCommand.equals("helm"))
			selectedRepairType = RepairType.HELM;

		if (updateFinished)
			updateRepairSection(UpdateMode.RADIO_BUTTONS);
	}


	private void repairPointsSpinnerChanged(ChangeEvent evt) {
		if ((Integer) (repairPointsSpinner.getValue()) == 0)
			repairButton.setEnabled(false);
		else
			repairButton.setEnabled(true);
	}


	private void repairButtonActionPerformed(ActionEvent evt) {
		int id = extractIDFromObject(repairShipComboBox.getSelectedItem());
		int points = (Integer) (repairPointsSpinner.getValue());

		MainBoard.game.repairShip(currentPlayer, id, selectedRepairType, points);

		updateRepairSection(UpdateMode.DEFAULT);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.CONTINUE);
		updateCannonsSection();
	}


	private void buyShipyardButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.buyShipyard(currentPlayer);

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.CONTINUE);
		updateCannonsSection();
	}


	private void buyFoundryButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.getPlayer(currentPlayer).buyFoundry();

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.CONTINUE);
		updateCannonsSection();
	}


	private void buildShipClassComboBoxActionPerformed(ActionEvent evt) {
	}


	private void buildShipButtonActionPerformed(ActionEvent evt) {
		String shipClass = buildShipClassComboBox.getSelectedItem().toString();

		for (ShipClass c : ShipClass.values()) {
			if (shipClass.equals(c.toString()))
				MainBoard.game.buildShip(currentPlayer, c);
		}

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateSellShipSection(UpdateMode.CONTINUE);
		updateTransferSection(UpdateMode.CONTINUE);
	}


	private void sellShipComboBoxActionPerformed(ActionEvent evt) {
		if (updateFinished)
			updateSellShipSection(UpdateMode.CONTINUE);
	}


	private void sellShipButtonActionPerformed(ActionEvent evt) {
		Object selected = sellShipComboBox.getSelectedItem();
		int selectedID = extractIDFromObject(selected);

		MainBoard.game.sellShip(currentPlayer, MainBoard.game.getShip(selectedID));

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateSellShipSection(UpdateMode.DEFAULT);
		updateHireMarinesSection(UpdateMode.CONTINUE);
		updateTransferSection(UpdateMode.CONTINUE);
		updateCannonsSection();
		updateContractSection();
	}


	private void hireMarinesNumberSpinnerChanged(ChangeEvent evt) {
		if ((Integer) (hireMarinesNumberSpinner.getValue()) == 0)
			hireMarinesButton.setEnabled(false);
		else
			hireMarinesButton.setEnabled(true);

//		  to sie powinno dac jakos ladnie zapisac (teraz zdarzenie /chyba/ nie
//		  jest wyzwalane przy wpisywaniu recznym
		 
		if ((Integer) (hireMarinesNumberSpinner.getValue()) > pl.getGold())
			hireMarinesNumberSpinner.setModel(new SpinnerNumberModel(pl.getGold() - 1, 0, pl.getGold(), 1));
	}


	private void hireMarinesButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.getPlayer(currentPlayer).hireMarines((Integer) (hireMarinesNumberSpinner.getValue()));

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.DEFAULT);
		updateCannonsSection();
	}


	private void transferFromActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("store"))
			transferFrom = Commons.STOREHOUSE_CODE;
		if (actionCommand.equals("ship"))
			transferFrom = extractIDFromObject(fromShipTransferComboBox.getSelectedItem());

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void fromShipTransferComboBoxActionPerformed(ActionEvent evt) {
		if (fromShipTransferRadioButton.isSelected() && fromShipTransferComboBox.getItemCount() > 0) {
			transferFrom = extractIDFromObject(fromShipTransferComboBox.getSelectedItem());

			if (updateFinished)
				updateTransferSection(UpdateMode.CONTINUE);
		}
	}


	private void transferToActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("store"))
			transferTo = Commons.STOREHOUSE_CODE;
		if (actionCommand.equals("ship")) {
			if (toShipTransferComboBox.getItemCount() > 0)
				transferTo = this.extractIDFromObject(toShipTransferComboBox.getSelectedItem());
			else
				transferTo = null;
		}

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void toShipTransferComboBoxActionPerformed(ActionEvent evt) {
		if (toShipTransferRadioButton.isSelected() && toShipTransferComboBox.getItemCount() > 0) {
			transferTo = extractIDFromObject(toShipTransferComboBox.getSelectedItem());

			if (updateFinished)
				updateTransferSection(UpdateMode.CONTINUE);
		}
	}


	private void transferCargoActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("marines")) {
			transferCargo = TransferCargo.MARINES;
			transferGunType = Gun.NONE;
		}
		if (actionCommand.equals("light")) {
			transferCargo = TransferCargo.LIGHT;
			transferGunType = Gun.LIGHT;
		}
		if (actionCommand.equals("medium")) {
			transferCargo = TransferCargo.MEDIUM;
			transferGunType = Gun.MEDIUM;
		}
		if (actionCommand.equals("heavy")) {
			transferCargo = TransferCargo.HEAVY;
			transferGunType = Gun.HEAVY;
		}

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void transferMarinesFromActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("deck"))
			transferMarinesFrom = MarinesCompartment.DECK;
		if (actionCommand.equals("batteries"))
			transferMarinesFrom = MarinesCompartment.BATTERIES;

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void transferMarinesToActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("deck"))
			transferMarinesTo = MarinesCompartment.DECK;
		if (actionCommand.equals("batteries"))
			transferMarinesTo = MarinesCompartment.BATTERIES;

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void transferCannonsFromActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("bow"))
			transferCannonFrom = GunCompartment.BOW;
		if (actionCommand.equals("left"))
			transferCannonFrom = GunCompartment.SIDE_L;
		if (actionCommand.equals("right"))
			transferCannonFrom = GunCompartment.SIDE_R;
		if (actionCommand.equals("stern"))
			transferCannonFrom = GunCompartment.STERN;

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void transferCannonsToActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("bow"))
			transferCannonTo = GunCompartment.BOW;
		if (actionCommand.equals("left"))
			transferCannonTo = GunCompartment.SIDE_L;
		if (actionCommand.equals("right"))
			transferCannonTo = GunCompartment.SIDE_R;
		if (actionCommand.equals("stern"))
			transferCannonTo = GunCompartment.STERN;

		if (updateFinished)
			updateTransferSection(UpdateMode.CONTINUE);
	}


	private void transferNumberSpinnerChanged(ChangeEvent evt) {
		if ((Integer) (transferNumberSpinner.getValue()) == 0 || transferFrom == transferTo)
			transferTransferButton.setEnabled(false);
		else
			transferTransferButton.setEnabled(true);
	}


	private void transferTransferButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.transfer(currentPlayer, transferFrom, transferTo, transferCargo, transferMarinesFrom,
				transferMarinesTo, transferCannonFrom, transferCannonTo, transferGunType,
				(Integer) (transferNumberSpinner.getValue()));

		updateCannonsSection();
	}


	private void sellCannonsButtonsActionPerformed(ActionEvent evt) {
		String actionCommand = evt.getActionCommand();

		if (actionCommand.equals("light"))
			selectedSellCannonType = Gun.LIGHT;
		if (actionCommand.equals("medium"))
			selectedSellCannonType = Gun.MEDIUM;
		if (actionCommand.equals("heavy"))
			selectedSellCannonType = Gun.HEAVY;

		if (updateFinished)
			updateCannonsSection();
	}


	private void sellCannonsSpinnerChanged(ChangeEvent evt) {
		if ((Integer) (sellCannonsSpinner.getValue()) > pl.getCannons(selectedSellCannonType))
			sellCannonsButton.setEnabled(false);
		else
			sellCannonsButton.setEnabled(true);

		if ((Integer) (transferNumberSpinner.getValue()) > pl.getGold()
				/ selectedSellCannonType.getPrice(DealType.BUY))
			buyCannonsButton.setEnabled(false);
		else
			buyCannonsButton.setEnabled(true);

		if ((Integer) (sellCannonsSpinner.getValue()) == 0) {
			sellCannonsButton.setEnabled(false);
			buyCannonsButton.setEnabled(false);
		}
	}


	private void sellCannonsButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.sellCannons(currentPlayer, selectedSellCannonType, (Integer) (sellCannonsSpinner.getValue()));

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.DEFAULT);
		updateTransferSection(UpdateMode.RADIO_BUTTONS);
		updateCannonsSection();
	}


	private void buyCannonsButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.buyCannons(currentPlayer, selectedSellCannonType, (Integer) (sellCannonsSpinner.getValue()));

		updateRepairSection(UpdateMode.RADIO_BUTTONS);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(UpdateMode.DEFAULT);
		updateTransferSection(UpdateMode.RADIO_BUTTONS);
		updateCannonsSection();
	}


	private void partnerContractComboBoxActionPerformed(ActionEvent evt) {
		if (partnerContractComboBox.getItemCount() > 0) {
			for (Player p : Player.values()) {
				if (partnerContractComboBox.getSelectedItem().toString().equals(p.toString())) {
					selectedClient = p;
					break;
				}
			}

			if (updateFinished)
				updateContractSection();
		} else
			selectedClient = Player.NONE;
	}


	private void submitOfferButtonActionPerformed(ActionEvent evt) {
		int rv = Commons.NIL;

		Player clientID = Player.NONE;
		for (Player p : Player.values()) {
			if (p.toString().equals(partnerContractComboBox.getSelectedItem().toString())) {
				clientID = p;
				break;
			}
		}

		if (MainBoard.game.getPlayerInternedBy(clientID) != currentPlayer) {
			rv = JOptionPane.showConfirmDialog(this, partnerContractComboBox.getSelectedItem().toString()
					+ ", do you accept the contract?", null, JOptionPane.YES_NO_OPTION);
		}
		// par. 5.3.8
		else
			rv = JOptionPane.YES_OPTION;
		// --

		if (rv == JOptionPane.YES_OPTION) {
			 * Rozwiazanie z odpowidzią bezposrednią jest tymczasowe. Docelowo
			 * powinno się ukazywać osobne okienko z pełnymi statystykami
			 * kontraktu.

			Object[] shipList;
			int[] shipArrayA, shipArrayB;

			shipList = myShipsContractList.getSelectedValues();
			shipArrayA = new int[shipList.length];
			for (int i = 0; i < shipList.length; i++)
				shipArrayA[i] = extractIDFromObject(shipList[i]);

			shipList = partnerShipsContractList.getSelectedValues();
			shipArrayB = new int[shipList.length];
			for (int i = 0; i < shipList.length; i++)
				shipArrayB[i] = extractIDFromObject(shipList[i]);

			MainBoard.game.acceptContractOffer(currentPlayer, shipArrayA,
					(Integer) (myLightContractSpinner.getValue()), (Integer) (myMediumContractSpinner.getValue()),
					(Integer) (myHeavyContractSpinner.getValue()), (Integer) (myMarinesContractSpinner.getValue()),
					(Integer) (myGoldPiecesContractSpinner.getValue()),
					(Integer) (mySilverTonsContractSpinner.getValue()), myFreeCommanderContractCheckBox.isSelected(),

					clientID, shipArrayB, (Integer) (partnerLightContractSpinner.getValue()),
					(Integer) (partnerMediumContractSpinner.getValue()),
					(Integer) (partnerHeavyContractSpinner.getValue()),
					(Integer) (partnerMarinesContractSpinner.getValue()),
					(Integer) (partnerGoldPiecesContractSpinner.getValue()),
					(Integer) (partnerSilverTonsContractSpinner.getValue()),
					partnerFreeCommanderContractCheckBox.isSelected());
		}

		updateContractSection();
	}


	private void updateSellSilverSection(UpdateMode mode) {
		if (mode == UpdateMode.DEFAULT)
			sellSilverSpinner.setModel(new SpinnerNumberModel(0, 0, pl.getSilver(), 10));
		else {
			int val = (Integer) (sellSilverSpinner.getValue());
			sellSilverSpinner.setModel(new SpinnerNumberModel(val, 0, pl.getSilver(), 1));
		}

		if ((Integer) (sellSilverSpinner.getValue()) == 0)
			sellSilverButton.setEnabled(false);
		else
			sellSilverButton.setEnabled(true);
	}


	private void updateRepairSection(UpdateMode mode) {
		updateFinished = false;

		if (mode != UpdateMode.RADIO_BUTTONS) {
			repairShipComboBox.removeAllItems();
			Object[] ids = pl.getFleet().toArray();
			for (int i = 0; i < pl.getFleet().size(); i++) {
				int id = (Integer) (ids[i]);
				repairShipComboBox.addItem(id + ", " + MainBoard.game.getShip(id).getShipClass().toString());
			}
		}

		if (mode == UpdateMode.DEFAULT) {
			repairDurabilityRadioButton.doClick();
			repairShipComboBox.setSelectedIndex(0);
		}

		if (repairShipComboBox.getItemCount() > 0) {
			int id = extractIDFromObject(repairShipComboBox.getSelectedItem());
			sh = getShip(id);

			// par. 20.2.3
			int maxPoints = 0;
			switch (selectedRepairType) {
			case DURABILITY:
				maxPoints = sh.getShipClass().getDurabilityMax() - sh.getDurability();
				break;
			case MAST:
				maxPoints = sh.getShipClass().getMastMax() - sh.getMast();
				break;
			case HELM:
				maxPoints = sh.getShipClass().getHelmMax() - sh.getHelm(Commons.BOTH);
				break;
			}
			// --

			int val = Math.min(maxPoints, pl.getGold() / selectedRepairType.getCost());

			// if (mode == UpdateMode.DEFAULT)
			repairPointsSpinner.setModel(new SpinnerNumberModel(0, 0, val, 1));
			// else
			// repairPointsSpinner.setModel(new
			// SpinnerNumberModel(val,0,val,1));
		} else
			repairPointsSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));

		if ((Integer) (repairPointsSpinner.getValue()) == 0)
			repairButton.setEnabled(false);
		else
			repairButton.setEnabled(true);

		updateFinished = true;
	}


	private void updateShipyardAndFoundrySection() {
		if (pl.getGold() > SHIPYARD_PRICE)
			buyShipyardButton.setEnabled(true);
		else
			buyShipyardButton.setEnabled(false);

		if (pl.getGold() < FOUNDRY_PRICE)
			buyFoundryButton.setEnabled(false);
		else
			buyFoundryButton.setEnabled(true);

		buildShipClassComboBox.removeAllItems();
		for (ShipClass c : ShipClass.values()) {
			if (c == ShipClass.NONE)
				continue;
			// par. 20.3.1
			if (pl.getGold() >= c.getPrice() / 2)
				buildShipClassComboBox.addItem(c.toString());
			// --
		}

		if (buildShipClassComboBox.getItemCount() > 0 && pl.getShipsInYards() == 0)
			buildShipButton.setEnabled(false);
		else
			buildShipButton.setEnabled(true);
	}


	private void updateSellShipSection(UpdateMode mode) {
		updateFinished = false;

		Object selected = sellShipComboBox.getSelectedItem();
		sellShipComboBox.removeAllItems();
		Object[] ids = pl.getFleet().toArray();

		for (int i = 0; i < pl.getFleet().size(); i++) {
			int id = (Integer) (ids[i]);
			sellShipComboBox.addItem(id + ", " + MainBoard.game.getShip(id).getShipClass().toString());
		}

		if (sellShipComboBox.getItemCount() == 0) {
			shipPriceLabel.setText("Price: n/a");
			sellShipButton.setEnabled(false);
		} else {
			if (mode == UpdateMode.CONTINUE && selected != null)
				sellShipComboBox.setSelectedItem(selected);
			else {
				sellShipComboBox.setSelectedIndex(0);
				selected = sellShipComboBox.getSelectedItem();
			}

			int selectedID = extractIDFromObject(selected);
			int p = MainBoard.game.calculateSellShipPrice(selectedID);
			if (p == Commons.NIL) {
				sellShipButton.setEnabled(false);
				shipPriceLabel.setText("Price: n/a");
			} else {
				sellShipButton.setEnabled(true);
				shipPriceLabel.setText("Price: " + p + "d");
			}
		}

		updateFinished = true;
	}


	private void updateHireMarinesSection(UpdateMode mode) {
		if (mode == UpdateMode.DEFAULT)
			hireMarinesNumberSpinner.setModel(new SpinnerNumberModel(0, 0, pl.getGold(), 1));
		else if (mode == UpdateMode.CONTINUE) {
			int value = (Integer) (hireMarinesNumberSpinner.getValue());
			hireMarinesNumberSpinner.setModel(new SpinnerNumberModel(value, 0, pl.getGold(), 1));
		}

		if ((Integer) (hireMarinesNumberSpinner.getValue()) == 0)
			hireMarinesButton.setEnabled(false);
		else
			hireMarinesButton.setEnabled(true);
	}


	private void updateTransferSection(UpdateMode mode) {
		updateFinished = false;

		Object previousOnList = new Object();
		updatePlayer();

		previousOnList = fromShipTransferComboBox.getSelectedItem();

		fromShipTransferComboBox.removeAllItems();
		Object[] ids = pl.getFleet().toArray();
		for (int i = 0; i < pl.getFleet().size(); i++) {
			int id = (Integer) (ids[i]);
			fromShipTransferComboBox.addItem(id + ", " + MainBoard.game.getShip(id).getShipClass().toString());
		}

		if (previousOnList == null)
			fromShipTransferComboBox.setSelectedIndex(0);
		else
			fromShipTransferComboBox.setSelectedItem(previousOnList);

		if (mode == UpdateMode.DEFAULT)
			fromStorehouseTransferRadioButton.doClick();
		else if (transferFrom != Commons.STOREHOUSE_CODE) {
			transferFrom = extractIDFromObject(fromShipTransferComboBox.getSelectedItem());
			if (transferFrom != extractIDFromObject(previousOnList))
				updateTransferSection(UpdateMode.DEFAULT);
		}

		if (fromShipTransferComboBox.getItemCount() == 0)
			fromShipTransferRadioButton.setEnabled(false);
		else
			fromShipTransferRadioButton.setEnabled(true);

		previousOnList = toShipTransferComboBox.getSelectedItem();

		toShipTransferComboBox.removeAllItems();
		ids = pl.getFleet().toArray();
		for (int i = 0; i < pl.getFleet().size(); i++) {
			int id = (Integer) (ids[i]);
			toShipTransferComboBox.addItem(id + ", " + MainBoard.game.getShipClass(id).toString());
		}

		if (previousOnList == null)
			toShipTransferComboBox.setSelectedIndex(0);
		else
			toShipTransferComboBox.setSelectedItem(previousOnList);

		if (mode == UpdateMode.DEFAULT)
			toStorehouseTransferRadioButton.doClick();
		else if (transferTo != Commons.STOREHOUSE_CODE) {
			transferTo = extractIDFromObject(toShipTransferComboBox.getSelectedItem());
			if (transferTo != extractIDFromObject(previousOnList))
				updateTransferSection(UpdateMode.DEFAULT);
		}

		if (toShipTransferComboBox.getItemCount() == 0)
			toShipTransferRadioButton.setEnabled(false);
		else
			toShipTransferRadioButton.setEnabled(true);

		if (transferFrom == transferTo)
			transferTransferButton.setEnabled(false);
		else
			transferTransferButton.setEnabled(true);

		if (mode == UpdateMode.DEFAULT) {
			marinesTransferRadioButton.doClick();
			marinesFromDeckTransferRadioButton.setSelected(true);
			marinesToDeckTransferRadioButton.setSelected(true);
			cannonsFromBowTransferRadioButton.setSelected(true);
			cannonsToBowTransferRadioButton.setSelected(true);
		}

		if (transferFrom == Commons.STOREHOUSE_CODE || transferCargo != TransferCargo.MARINES) {
			marinesFromDeckTransferRadioButton.setEnabled(false);
			marinesFromBatteriesTransferRadioButton.setEnabled(false);
		}

		if (transferFrom == Commons.STOREHOUSE_CODE || transferCargo == TransferCargo.MARINES) {
			cannonsFromBowTransferRadioButton.setEnabled(false);
			cannonsFromLeftSideTransferRadioButton.setEnabled(false);
			cannonsFromRightSideTransferRadioButton.setEnabled(false);
			cannonsFromSternTransferRadioButton.setEnabled(false);
		}

		if (transferFrom != Commons.STOREHOUSE_CODE && transferCargo == TransferCargo.MARINES) {
			marinesFromDeckTransferRadioButton.setEnabled(true);
			marinesFromBatteriesTransferRadioButton.setEnabled(true);
		}

		if (transferFrom != Commons.STOREHOUSE_CODE && transferCargo != TransferCargo.MARINES) {
			cannonsFromBowTransferRadioButton.setEnabled(true);
			cannonsFromLeftSideTransferRadioButton.setEnabled(true);
			cannonsFromRightSideTransferRadioButton.setEnabled(true);
			cannonsFromSternTransferRadioButton.setEnabled(true);
		}

		if (transferTo == Commons.STOREHOUSE_CODE || transferCargo != TransferCargo.MARINES) {
			marinesToDeckTransferRadioButton.setEnabled(false);
			marinesToBatteriesTransferRadioButton.setEnabled(false);
		}

		if (transferTo == Commons.STOREHOUSE_CODE || transferCargo == TransferCargo.MARINES) {
			cannonsToBowTransferRadioButton.setEnabled(false);
			cannonsToLeftSideTransferRadioButton.setEnabled(false);
			cannonsToRightSideTransferRadioButton.setEnabled(false);
			cannonsToSternTransferRadioButton.setEnabled(false);
		}

		if (transferTo != Commons.STOREHOUSE_CODE && transferCargo == TransferCargo.MARINES) {
			marinesToDeckTransferRadioButton.setEnabled(true);
			marinesToBatteriesTransferRadioButton.setEnabled(true);
		}

		if (transferTo != Commons.STOREHOUSE_CODE && transferCargo != TransferCargo.MARINES) {
			cannonsToBowTransferRadioButton.setEnabled(true);
			cannonsToLeftSideTransferRadioButton.setEnabled(true);
			cannonsToRightSideTransferRadioButton.setEnabled(true);
			cannonsToSternTransferRadioButton.setEnabled(true);
		}

		int store = 0;
		int limit = 0;

		if (transferFrom == Commons.STOREHOUSE_CODE) {
			if (transferCargo == TransferCargo.MARINES)
				store = pl.getMarines();
			else
				store = pl.getCannons(transferGunType);
		} else {
			sh = getShip(transferFrom);
			if (transferCargo == TransferCargo.MARINES)
				store = sh.getMarinesNumber(currentPlayer, transferMarinesFrom, Commons.BOTH);
			else
				store = sh.getCannonsNumber(transferCannonFrom, transferGunType, Commons.BOTH);
		}

		if (transferTo == Commons.STOREHOUSE_CODE)
			limit = Commons.INF;
		else {
			sh2 = getShip(transferTo);
			if (transferCargo == TransferCargo.MARINES) {
				if (transferMarinesTo == MarinesCompartment.DECK)
					limit = sh2.getShipClass().getCrewDeckMax()
							- sh2.getMarinesNumber(currentPlayer, MarinesCompartment.DECK, Commons.BOTH);
				else
					limit = sh2.getShipClass().getCrewMax() - sh2.getShipClass().getCrewDeckMax()
							- sh2.getMarinesNumber(currentPlayer, MarinesCompartment.BATTERIES, Commons.BOTH);
			} else {
				// par. 20.2.1
				for (int gunType = transferGunType.ordinal(); gunType < Gun.getSize(); gunType++)
					limit += sh2.getShipClass().getCannonMax()[transferCannonTo.ordinal()][gunType]
							- sh2.getCannonsNumber(transferCannonTo, transferGunType, Commons.BOTH);
				limit = Math.min(
						limit,
						sh2.getShipClass().getCannonMax()[transferCannonTo.ordinal()][Gun.LIGHT.ordinal()]
								+ sh2.getShipClass().getCannonMax()[transferCannonTo.ordinal()][Gun.MEDIUM
										.ordinal()]
								+ sh2.getShipClass().getCannonMax()[transferCannonTo.ordinal()][Gun.HEAVY
										.ordinal()] - sh2.getCannonsNumber(transferCannonTo, Gun.LIGHT, Commons.BOTH)
								- sh2.getCannonsNumber(transferCannonTo, Gun.MEDIUM, Commons.BOTH)
								- sh2.getCannonsNumber(transferCannonTo, Gun.HEAVY, Commons.BOTH));
				// --
			}
		}

		if (mode == UpdateMode.DEFAULT)
			transferNumberSpinner.setModel(new SpinnerNumberModel(0, 0, Math.min(store, limit), 1));
		else {
			int value = (Integer) (transferNumberSpinner.getValue());
			int max = Math.min(store, limit);
			transferNumberSpinner.setModel(new SpinnerNumberModel(Math.min(value, max), 0, max, 1));
		}

		if ((Integer) (transferNumberSpinner.getValue()) == 0)
			transferTransferButton.setEnabled(false);
		else
			transferTransferButton.setEnabled(true);

		updateFinished = true;
	}


	private void updateCannonsSection() {
		updateFinished = false;

		// TODO (wszystko ;)

		sellCannonsSpinner.setModel(new SpinnerNumberModel(0, 0, pl.getCannons(selectedSellCannonType), 1));
		sellCannonsButton.setEnabled(false);
		buyCannonsButton.setEnabled(false);

		updateFinished = true;
	}


	private void updateContractSection() {
		updateFinished = false;

		Player partner = Player.NONE;
		DefaultListModel listModel = new DefaultListModel();
		// Vector<String> listData = new Vector<String>();

		Object previousOnList = partnerContractComboBox.getSelectedItem();

		partnerContractComboBox.removeAllItems();
		for (Player p : Player.values()) {
			if (p == Player.NONE)
				continue;
			if (MainBoard.game.getPlayer(p).isInGame() && p != currentPlayer)
				partnerContractComboBox.addItem(p.toString());
		}

		if (partnerContractComboBox.getItemCount() == 0) {
			myShipsContractList.setModel(listModel);
			partnerShipsContractList.setModel(listModel);

			myLightContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			myMediumContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			myHeavyContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			myMarinesContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			myGoldPiecesContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			mySilverTonsContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));

			partnerLightContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			partnerMediumContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			partnerHeavyContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			partnerMarinesContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			partnerGoldPiecesContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			partnerSilverTonsContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));

			myFreeCommanderContractCheckBox.setEnabled(false);
			partnerFreeCommanderContractCheckBox.setEnabled(false);

			submitOfferButton.setEnabled(false);

			updateFinished = true;
			return;
		}

		if (previousOnList == null)
			partnerContractComboBox.setSelectedIndex(0);
		else
			partnerContractComboBox.setSelectedItem(previousOnList);

		PlayerClass me = MainBoard.game.getPlayer(currentPlayer);
		PlayerClass client = MainBoard.game.getPlayer(selectedClient);

		Object[] ids;

		if (me.getFleet() != null) {
			ids = me.getFleet().toArray();
			for (int i = 0; i < ids.length; i++) {
				listModel.addElement(String.valueOf((Integer) ids[i]) + ", "
						+ MainBoard.game.getShip((Integer) (ids[i])).getShipClass().toString());
			}
		}

		for (Player p : Player.values()) {
			if (p.toString().equals(partnerContractComboBox.getSelectedItem().toString())) {
				partner = p;
				break;
			}
		}

		// par. 5.6.2
		if (me.getInternedShips(partner) != null) {
			ids = me.getInternedShips(partner).toArray();
			for (int i = 0; i < ids.length; i++) {
				listModel.addElement(String.valueOf((Integer) ids[i]) + ", "
						+ MainBoard.game.getShip((Integer) (ids[i])).getShipClass().toString() + "(i)");
			}

			myShipsContractList.setModel(listModel);
		}
		// --

		listModel = new DefaultListModel();

		if (client.getFleet() != null) {
			ids = client.getFleet().toArray();
			for (int i = 0; i < ids.length; i++) {
				listModel.addElement(String.valueOf((Integer) (ids[i])) + ", "
						+ MainBoard.game.getShip((Integer) (ids[i])).getShipClass().toString());
			}
		}

		// par. 5.6.2
		if (client.getInternedShips(partner) != null) {
			ids = client.getInternedShips(partner).toArray();
			for (int i = 0; i < ids.length; i++) {
				listModel.addElement(String.valueOf((Integer) (ids[i])) + ", "
						+ MainBoard.game.getShip((Integer) (ids[i])).getShipClass().toString() + "(i)");
			}
		}
		// --

		partnerShipsContractList.setModel(listModel);
		 * partnerShipsContractList.setSelectionMode(ListSelectionModel.
		 * MULTIPLE_INTERVAL_SELECTION);
		 * partnerShipsContractList.setVisibleRowCount(4);
		 

		myLightContractSpinner.setModel(new SpinnerNumberModel(0, 0, me.getCannons(Gun.LIGHT), 1));
		myMediumContractSpinner.setModel(new SpinnerNumberModel(0, 0, me.getCannons(Gun.MEDIUM), 1));
		myHeavyContractSpinner.setModel(new SpinnerNumberModel(0, 0, me.getCannons(Gun.HEAVY), 1));
		myMarinesContractSpinner.setModel(new SpinnerNumberModel(0, 0, me.getMarines()
				+ me.getMarinesInterned(selectedClient), 1));
		myGoldPiecesContractSpinner.setModel(new SpinnerNumberModel(0, 0, me.getGold(), 1));

		// par. 20.1.2
		if ((currentPlayer == Player.PASADENA || currentPlayer == Player.SIDONIA) && selectedClient != Player.SIDONIA
				&& selectedClient != Player.PASADENA)
			mySilverTonsContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
		else
			mySilverTonsContractSpinner.setModel(new SpinnerNumberModel(0, 0, me.getSilver(), 0));
		// --

		partnerLightContractSpinner.setModel(new SpinnerNumberModel(0, 0, client.getCannons(Gun.LIGHT), 1));
		partnerMediumContractSpinner.setModel(new SpinnerNumberModel(0, 0, client.getCannons(Gun.MEDIUM), 1));
		partnerHeavyContractSpinner.setModel(new SpinnerNumberModel(0, 0, client.getCannons(Gun.HEAVY), 1));
		partnerMarinesContractSpinner.setModel(new SpinnerNumberModel(0, 0, client.getMarines()
				+ client.getMarinesInterned(currentPlayer), 1));
		partnerGoldPiecesContractSpinner.setModel(new SpinnerNumberModel(0, 0, client.getGold(), 1));

		// par. 20.1.2
		if ((currentPlayer == Player.PASADENA || currentPlayer == Player.SIDONIA) && selectedClient != Player.SIDONIA
				&& selectedClient != Player.PASADENA)
			partnerSilverTonsContractSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
		else
			partnerSilverTonsContractSpinner.setModel(new SpinnerNumberModel(0, 0, client.getSilver(), 0));
		// --

		if (me.getCommandersInterned(selectedClient))
			myFreeCommanderContractCheckBox.setEnabled(true);
		else
			myFreeCommanderContractCheckBox.setEnabled(false);

		if (client.getCommandersInterned(currentPlayer))
			partnerFreeCommanderContractCheckBox.setEnabled(true);
		else
			partnerFreeCommanderContractCheckBox.setEnabled(false);

		submitOfferButton.setEnabled(true);

		updateFinished = true;
	}


	private void updatePlayer() {
		currentPlayer = MainBoard.game.getCurrentPlayer();

		if (currentPlayer != Player.NONE)
			pl = MainBoard.game.getPlayer(currentPlayer);

		currentPlayerLabel.setText("Current player: " + currentPlayer.toString());
	}


	private Ship getShip(int id) {
		return MainBoard.game.getShip(id);
	}


	private Integer extractIDFromObject(Object object) {
		if (object == null)
			return null;
		else {
			String str = object.toString().substring(0, object.toString().indexOf(','));
			if (str.equals("-"))
				return null;
			else
				return Integer.valueOf(str);
		}
	}


	private void takeLoanBankButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.takeLoan(currentPlayer, (Integer) (loanAmountBankSpinner.getValue()));
		updateGoldRelatedSections(UpdateMode.CONTINUE);
	}


	private void repayLoanBankButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.repayLoan(currentPlayer, (Integer) (loanAmountBankSpinner.getValue()));
		updateGoldRelatedSections(UpdateMode.CONTINUE);
	}


	private void updateLoanBankSection() {
		updatePlayer();
		int loan = pl.getLoan();
		if (loan == 0) {
			currentLoanBankLabel.setText("Current loan: none");
			loanAmountBankSpinner.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
			takeLoanBankButton.setEnabled(true);
			repayLoanBankButton.setEnabled(false);

		} else {
			currentLoanBankLabel.setText("Current loan: " + loan + "d");
			loanAmountBankSpinner.setModel(new SpinnerNumberModel(0, 0, Math.min(loan, pl.getGold()), 1));
			takeLoanBankButton.setEnabled(false);
			repayLoanBankButton.setEnabled(true);
		}
	}


	/**
	 * private void updatePurchaseShipBankSection() { updatePlayer();
	 * 
	 * Object selected = purchaseShipClassBankComboBox.getSelectedItem();
	 * purchaseShipClassBankComboBox.removeAllItems();
	 * 
	 * for (ShipClass c : ShipClass.values()) { if (c == ShipClass.NONE)
	 * continue; if
	 * (MainBoard.game.getShipsInBank(c)+MainBoard.game.getShipsInBank
	 * (ShipClass.NONE) > 0 && pl.getGold() >= SHIP_PRICES[c.ordinal()])
	 * purchaseShipClassBankComboBox.addItem(c.toString()); }
	 * 
	 * purchaseShipClassBankComboBox.setSelectedItem(selected); if
	 * (purchaseShipClassBankComboBox.getSelectedItem() == null &&
	 * purchaseShipClassBankComboBox.getItemCount() > 0)
	 * purchaseShipClassBankComboBox.setSelectedIndex(0);
	 * 
	 * if (purchaseShipClassBankComboBox.getItemCount() == 0)
	 * purchaseShipBankButton.setEnabled(false); else
	 * purchaseShipBankButton.setEnabled(true); }

	public void update(UpdateMode mode) {
		updatePlayer();

		updateSellSilverSection(mode);
		updateRepairSection(mode);
		updateShipyardAndFoundrySection();
		updateSellShipSection(mode);
		updateHireMarinesSection(mode);
		updateCannonsSection();
		updateContractSection();

		updateLoanBankSection();
		updateAuctionsSection();
	}


	private void updateGoldRelatedSections(UpdateMode mode) {
		updateRepairSection(mode);
		updateShipyardAndFoundrySection();
		updateHireMarinesSection(mode);
		updateCannonsSection();
		updateContractSection();

		updateLoanBankSection();
	}


	private void submitOfferBankButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.submitAuctionOffer(extractIDFromObject(auctionsBankList.getSelectedValues()[0]),
				MainBoard.game.getCurrentPlayer(), (Integer) yourOfferBankSpinner.getValue());

		updateAuctionsSection();
	}


	private void nextPlayerButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.endPlayerTurn();
	}


	private void passButtonActionPerformed(ActionEvent evt) {
		MainBoard.game.pass();
	}


	private void auctionsBankListValueChanged(ListSelectionEvent evt) {
		if (updateFinished)
			updateAuctionsSection();
	}


	private void init() {

		/** TEST 
		// currentPlayer = Player.PASADENA;
		// updatePlayer();

		sm = new SpinnerNumberModel(0, 0, 0, 0);

		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(1000, 650));
		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				// TODO here

				JTabbedPane pane = (JTabbedPane) evt.getSource();
				 * int sel = pane.getSelectedIndex(); if (sel ==
				 * Tabs.SHOOT.ordinal()) { updateShootTab(); }
			}
		});

		// Exchange Panel
		exchangeTabPanel = new JPanel();

		sellSilverLabel = new javax.swing.JLabel();
		sellSilverAmountLabel = new javax.swing.JLabel();
		sellSilverSpinner = new javax.swing.JSpinner();
		sellSilverButton = new javax.swing.JButton();
		hSeparator1 = new javax.swing.JSeparator();
		repairLabel = new javax.swing.JLabel();
		repairIDLabel = new javax.swing.JLabel();
		repairShipComboBox = new javax.swing.JComboBox();
		repairDurabilityRadioButton = new javax.swing.JRadioButton();
		repairMastRadioButton = new javax.swing.JRadioButton();
		repairHelmRadioButton = new javax.swing.JRadioButton();
		repairPointsLabel = new javax.swing.JLabel();
		repairPointsSpinner = new javax.swing.JSpinner();
		repairButton = new javax.swing.JButton();
		hSeparator2 = new javax.swing.JSeparator();
		buyShipyardButton = new javax.swing.JButton();
		buyFoundryButton = new javax.swing.JButton();
		buyShipClassLabel = new javax.swing.JLabel();
		buildShipClassComboBox = new javax.swing.JComboBox();
		buildShipButton = new javax.swing.JButton();
		vSeparator1 = new javax.swing.JSeparator();
		transferLabel = new javax.swing.JLabel();
		fromTransferLabel = new javax.swing.JLabel();
		fromStorehouseTransferRadioButton = new javax.swing.JRadioButton();
		fromShipTransferRadioButton = new javax.swing.JRadioButton();
		fromShipTransferComboBox = new javax.swing.JComboBox();
		toTransferLabel = new javax.swing.JLabel();
		toStorehouseTransferRadioButton = new javax.swing.JRadioButton();
		toShipTransferRadioButton = new javax.swing.JRadioButton();
		toShipTransferComboBox = new javax.swing.JComboBox();
		hSeparator4 = new javax.swing.JSeparator();
		cargoTransferLabel = new javax.swing.JLabel();
		marinesTransferRadioButton = new javax.swing.JRadioButton();
		lightTransferRadioButton = new javax.swing.JRadioButton();
		heavyTransferRadioButton = new javax.swing.JRadioButton();
		mediumTransferRadioButton = new javax.swing.JRadioButton();
		hSeparator5 = new javax.swing.JSeparator();
		marinesFromTransferLabel = new javax.swing.JLabel();
		marinesFromDeckTransferRadioButton = new javax.swing.JRadioButton();
		marinesFromBatteriesTransferRadioButton = new javax.swing.JRadioButton();
		marinesToTransferLabel = new javax.swing.JLabel();
		marinesToDeckTransferRadioButton = new javax.swing.JRadioButton();
		marinesToBatteriesTransferRadioButton = new javax.swing.JRadioButton();
		hSeparator6 = new javax.swing.JSeparator();
		cannonsFromTransferLabel = new javax.swing.JLabel();
		cannonsFromBowTransferRadioButton = new javax.swing.JRadioButton();
		cannonsFromLeftSideTransferRadioButton = new javax.swing.JRadioButton();
		cannonsToTransferLabel = new javax.swing.JLabel();
		cannonsToSternTransferRadioButton = new javax.swing.JRadioButton();
		cannonsFromRightSideTransferRadioButton = new javax.swing.JRadioButton();
		cannonsToBowTransferRadioButton = new javax.swing.JRadioButton();
		cannonsToLeftSideTransferRadioButton = new javax.swing.JRadioButton();
		cannonsFromSternTransferRadioButton = new javax.swing.JRadioButton();
		cannonsToRightSideTransferRadioButton = new javax.swing.JRadioButton();
		hSeparator7 = new javax.swing.JSeparator();
		transferTransferButton = new javax.swing.JButton();
		vSeparator2 = new javax.swing.JSeparator();
		sellCannonsLabel = new javax.swing.JLabel();
		sellCannonsLightRadioButton = new javax.swing.JRadioButton();
		sellCannonsMediumRadioButton = new javax.swing.JRadioButton();
		sellCannonsHeavyRadioButton = new javax.swing.JRadioButton();
		sellCannonsNumberLabel = new javax.swing.JLabel();
		sellCannonsTypeLabel = new javax.swing.JLabel();
		sellCannonsSpinner = new javax.swing.JSpinner();
		sellCannonsButton = new javax.swing.JButton();
		hSeparator8 = new javax.swing.JSeparator();
		sellShipComboBox = new javax.swing.JComboBox();
		contractLabel = new javax.swing.JLabel();
		myContractLabel = new javax.swing.JLabel();
		myShipsContractScrollPane = new javax.swing.JScrollPane();
		myShipsEnContractScrollPane = new javax.swing.JScrollPane();
		myShipsContractList = new javax.swing.JList();
		shipsContractLabel = new javax.swing.JLabel();
		cannonsContractLabel = new javax.swing.JLabel();
		myLightContractLabel = new javax.swing.JLabel();
		myLightContractSpinner = new javax.swing.JSpinner();
		myMediumContractLabel = new javax.swing.JLabel();
		myMediumContractSpinner = new javax.swing.JSpinner();
		myHeavyContractLabel = new javax.swing.JLabel();
		myHeavyContractSpinner = new javax.swing.JSpinner();
		marinesContractLabel = new javax.swing.JLabel();
		myMarinesNumberContractLabel = new javax.swing.JLabel();
		myMarinesContractSpinner = new javax.swing.JSpinner();
		goldContractLabel = new javax.swing.JLabel();
		myGoldPiecesContractLabel = new javax.swing.JLabel();
		myGoldPiecesContractSpinner = new javax.swing.JSpinner();
		partnerContractLabel = new javax.swing.JLabel();
		partnerContractComboBox = new javax.swing.JComboBox();
		partnerLightContractLabel = new javax.swing.JLabel();
		partnerShipsContractScrollPane = new javax.swing.JScrollPane();
		partnerShipsEnContractScrollPane = new javax.swing.JScrollPane();
		partnerShipsContractList = new javax.swing.JList();
		partnerMediumContractLabel = new javax.swing.JLabel();
		partnerLightContractSpinner = new javax.swing.JSpinner();
		partnerMediumContractSpinner = new javax.swing.JSpinner();
		partnerHeavyContractLabel = new javax.swing.JLabel();
		partnerHeavyContractSpinner = new javax.swing.JSpinner();
		partnerMarinesNumberContractLabel = new javax.swing.JLabel();
		partnerMarinesContractSpinner = new javax.swing.JSpinner();
		partnerGoldPiecesContractLabel = new javax.swing.JLabel();
		partnerGoldPiecesContractSpinner = new javax.swing.JSpinner();
		submitOfferButton = new javax.swing.JButton();
		sellShipLabel = new javax.swing.JLabel();
		hSeparator3 = new javax.swing.JSeparator();
		sellShipIDLabel = new javax.swing.JLabel();
		sellShipButton = new javax.swing.JButton();
		myFreeCommanderContractCheckBox = new javax.swing.JCheckBox();
		partnerFreeCommanderContractCheckBox = new javax.swing.JCheckBox();
		numberTransferLabel = new javax.swing.JLabel();
		transferNumberSpinner = new javax.swing.JSpinner();
		shipPriceLabel = new javax.swing.JLabel();
		buyCannonsButton = new javax.swing.JButton();
		hireMarinesLabel = new javax.swing.JLabel();
		hireMarinesNumberLabel = new javax.swing.JLabel();
		hireMarinesNumberSpinner = new javax.swing.JSpinner();
		hireMarinesButton = new javax.swing.JButton();
		hSeparator9 = new javax.swing.JSeparator();
		silverContractLabel = new javax.swing.JLabel();
		mySilverTonsContractLabel = new javax.swing.JLabel();
		mySilverTonsContractSpinner = new javax.swing.JSpinner();
		partnerSilverTonsContractLabel = new javax.swing.JLabel();
		partnerSilverTonsContractSpinner = new javax.swing.JSpinner();

		// Bank Panel

		currentLoanBankLabel = new javax.swing.JLabel();
		loanAmountBankLabel = new javax.swing.JLabel();
		loanAmountBankSpinner = new javax.swing.JSpinner();
		takeLoanBankButton = new javax.swing.JButton();
		repayLoanBankButton = new javax.swing.JButton();
		hBankSeparator1 = new javax.swing.JSeparator();
		loanBankLabel = new javax.swing.JLabel();
		auctionBankLabel = new javax.swing.JLabel();
		lastOfferBankLabel = new javax.swing.JLabel();
		yourOfferBankLabel = new javax.swing.JLabel();
		yourOfferBankSpinner = new javax.swing.JSpinner();
		submitOfferBankButton = new javax.swing.JButton();
		vBankSeparator1 = new javax.swing.JSeparator();
		shipFigureBankLabel = new javax.swing.JLabel();
		shipIDBankLabel = new javax.swing.JLabel();
		shipClassBankLabel = new javax.swing.JLabel();
		shipDurabilityBankLabel = new javax.swing.JLabel();
		shipMastBankLabel = new javax.swing.JLabel();
		shipHelmBankLabel = new javax.swing.JLabel();
		shipCannonsBankTable = new javax.swing.JTable();
		shipRepairBankLabel = new javax.swing.JLabel();
		auctionsBankList = new javax.swing.JList();
		auctionsBankScrollPane = new JScrollPane();

		sellSilverLabel.setText("- Sell silver");
		sellSilverAmountLabel.setText("Amount:");
		repairLabel.setText("- Repair ship");
		repairIDLabel.setText("Ship:");
		repairPointsLabel.setText("Points:");
		buyShipClassLabel.setText("Class:");
		vSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
		transferLabel.setText("- Transfer");
		fromTransferLabel.setText("From:");
		toTransferLabel.setText("To:");
		cargoTransferLabel.setText("Cargo:");
		marinesFromTransferLabel.setText("Marines from:");
		marinesToTransferLabel.setText("Marines to:");
		cannonsFromTransferLabel.setText("Cannons from:");
		cannonsToTransferLabel.setText("Cannons to:");
		vSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
		sellCannonsLabel.setText("- Cannons");
		sellCannonsNumberLabel.setText("Number:");
		sellCannonsTypeLabel.setText("Type:");
		contractLabel.setText("CONTRACT");
		myContractLabel.setText("Me");
		shipsContractLabel.setText("*Ships");
		cannonsContractLabel.setText("*Cannons");
		myLightContractLabel.setText("light:");
		myMediumContractLabel.setText("medium:");
		myHeavyContractLabel.setText("heavy:");
		marinesContractLabel.setText("*Marines");
		myMarinesNumberContractLabel.setText("number:");
		goldContractLabel.setText("*Gold");
		myGoldPiecesContractLabel.setText("pieces:");
		partnerContractLabel.setText("Partner:");
		partnerLightContractLabel.setText("light:");
		partnerMediumContractLabel.setText("medium:");
		partnerHeavyContractLabel.setText("heavy:");
		partnerMarinesNumberContractLabel.setText("number:");
		partnerGoldPiecesContractLabel.setText("pieces:");
		sellShipLabel.setText("- Sell ship");
		sellShipIDLabel.setText("Ship:");
		numberTransferLabel.setText("Number:");
		shipPriceLabel.setText("Price: 0000d");
		hireMarinesLabel.setText("- Hire marines");
		hireMarinesNumberLabel.setText("Number:");
		silverContractLabel.setText("*Silver");
		mySilverTonsContractLabel.setText("tons:");
		partnerSilverTonsContractLabel.setText("tons:");

		// Bank Panel

		bankTabPanel = new JPanel();
		bankTabPanel.setLayout(null);

		currentLoanBankLabel.setText("Current loan: none");
		bankTabPanel.add(currentLoanBankLabel);
		currentLoanBankLabel.setBounds(10, 30, 150, 14);

		loanAmountBankLabel.setText("Amount:");
		bankTabPanel.add(loanAmountBankLabel);
		loanAmountBankLabel.setBounds(10, 50, 60, 14);

		bankTabPanel.add(loanAmountBankSpinner);
		loanAmountBankSpinner.setBounds(60, 50, 70, 20);

		takeLoanBankButton.setText("Take");
		bankTabPanel.add(takeLoanBankButton);
		takeLoanBankButton.setBounds(85, 80, 70, 23);
		takeLoanBankButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				takeLoanBankButtonActionPerformed(evt);
			}
		});

		repayLoanBankButton.setText("Repay");
		bankTabPanel.add(repayLoanBankButton);
		repayLoanBankButton.setBounds(10, 80, 70, 23);
		repayLoanBankButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				repayLoanBankButtonActionPerformed(evt);
			}
		});

		bankTabPanel.add(hBankSeparator1);
		hBankSeparator1.setBounds(10, 110, 700, 2);

		loanBankLabel.setText("Loan");
		bankTabPanel.add(loanBankLabel);
		loanBankLabel.setBounds(60, 10, 50, 14);

		auctionBankLabel.setText("Auction");
		bankTabPanel.add(auctionBankLabel);
		auctionBankLabel.setBounds(60, 120, 60, 14);

		lastOfferBankLabel.setText("Last offer: NONE");
		bankTabPanel.add(lastOfferBankLabel);
		lastOfferBankLabel.setBounds(10, 260, 120, 14);

		yourOfferBankLabel.setText("Your offer:");
		bankTabPanel.add(yourOfferBankLabel);
		yourOfferBankLabel.setBounds(10, 280, 60, 14);
		bankTabPanel.add(yourOfferBankSpinner);
		yourOfferBankSpinner.setBounds(70, 280, 70, 20);

		submitOfferBankButton.setText("Submit");
		bankTabPanel.add(submitOfferBankButton);
		submitOfferBankButton.setBounds(40, 310, 80, 23);
		submitOfferBankButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				submitOfferBankButtonActionPerformed(evt);
			}
		});

		vBankSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
		bankTabPanel.add(vBankSeparator1);
		vBankSeparator1.setBounds(150, 120, 10, 270);

		shipFigureBankLabel.setText("Ship figure");
		bankTabPanel.add(shipFigureBankLabel);
		shipFigureBankLabel.setBounds(310, 120, 90, 14);

		shipIDBankLabel.setText("ID: -");
		bankTabPanel.add(shipIDBankLabel);
		shipIDBankLabel.setBounds(180, 150, 50, 14);

		shipClassBankLabel.setText("Class: -");
		bankTabPanel.add(shipClassBankLabel);
		shipClassBankLabel.setBounds(250, 150, 100, 14);

		shipDurabilityBankLabel.setText("Durability: -/-");
		bankTabPanel.add(shipDurabilityBankLabel);
		shipDurabilityBankLabel.setBounds(180, 170, 120, 14);

		shipMastBankLabel.setText("Mast: -/-");
		bankTabPanel.add(shipMastBankLabel);
		shipMastBankLabel.setBounds(330, 170, 70, 14);

		shipHelmBankLabel.setText("Helm: -/-");
		bankTabPanel.add(shipHelmBankLabel);
		shipHelmBankLabel.setBounds(450, 170, 60, 14);

		shipCannonsBankTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {
				{ "BOW", "0|0", "0|0", "0|0" }, { "LEFT SIDE", "0|0", "0|0", "0|0" },
				{ "RIGHT SIDE", "0|0", "0|0", "0|0" }, { "STERN", "0|0", "0|0", "0|0" } }, new String[] {
				"Compartment", "Light", "Medium", "Heavy" }) {

			boolean[] canEdit = new boolean[] { false, false, false, false };


			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		});
		shipCannonsBankTable.getTableHeader().setReorderingAllowed(false);
		shipCannonsBankTable.getColumnModel().getColumn(0).setResizable(false);
		shipCannonsBankTable.getColumnModel().getColumn(1).setResizable(false);
		shipCannonsBankTable.getColumnModel().getColumn(2).setResizable(false);
		shipCannonsBankTable.getColumnModel().getColumn(3).setResizable(false);

		bankTabPanel.add(shipCannonsBankTable);
		shipCannonsBankTable.setBounds(170, 220, 300, 65);
		shipCannonsBankTable.setEnabled(false);

		shipRepairBankLabel.setText("Repair cost: -");
		bankTabPanel.add(shipRepairBankLabel);
		shipRepairBankLabel.setBounds(170, 290, 110, 14);

		auctionsBankList.setModel(new javax.swing.AbstractListModel() {

			String[] strings = { null }; /*
										 * String[] strings = { "Item 1",
										 * "Item 2", "Item 3", "Item 4",
										 * "Item 5"};


			public int getSize() {
				return strings.length;
			}


			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		auctionsBankList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent evt) {
				auctionsBankListValueChanged(evt);
			}
		});

		auctionsBankScrollPane.setViewportView(auctionsBankList);

		bankTabPanel.add(auctionsBankScrollPane);
		auctionsBankScrollPane.setBounds(10, 140, 130, 110);

		tabbedPane.addTab("Bank", null, bankTabPanel, null);

		// Exchange Panel

		sellSilverSpinner.setModel(sm);
		sellSilverSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				sellSilverSpinnerChanged(evt);
			}
		});

		sellSilverButton.setText("Sell");
		sellSilverButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellSilverButtonActionPerformed(evt);
			}
		});

		repairShipComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				repairShipComboBoxActionPerformed(evt);
			}
		});

		repairDurabilityRadioButton.setText("durability");
		repairDurabilityRadioButton.setActionCommand(RepairType.DURABILITY.name());
		repairDurabilityRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				repairShipActionPerformed(evt);
			}
		});

		repairMastRadioButton.setText("mast");
		repairMastRadioButton.setActionCommand(RepairType.MAST.name());
		repairMastRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				repairShipActionPerformed(evt);
			}
		});

		repairHelmRadioButton.setText("helm");
		repairHelmRadioButton.setActionCommand(RepairType.HELM.name());
		repairHelmRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				repairShipActionPerformed(evt);
			}
		});

		repairsButtonGroup = new ButtonGroup();
		repairsButtonGroup.add(repairDurabilityRadioButton);
		repairsButtonGroup.add(repairMastRadioButton);
		repairsButtonGroup.add(repairHelmRadioButton);

		repairPointsSpinner.setModel(sm);
		repairPointsSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				repairPointsSpinnerChanged(evt);
			}
		});

		repairButton.setText("Repair");
		repairButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				repairButtonActionPerformed(evt);
			}
		});

		buyShipyardButton.setText("Buy shipyard");
		buyShipyardButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				buyShipyardButtonActionPerformed(evt);
			}
		});

		buyFoundryButton.setText("Buy foundry");
		buyFoundryButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				buyFoundryButtonActionPerformed(evt);
			}
		});

		buildShipClassComboBox.removeAllItems();
		for (ShipClass sc : ShipClass.values()) {
			if (sc != ShipClass.NONE)
				buildShipClassComboBox.addItem(sc.toString());
		}
		buildShipClassComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				buildShipClassComboBoxActionPerformed(evt);
			}
		});

		buildShipButton.setText("Build ship");
		buildShipButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				buildShipButtonActionPerformed(evt);
			}
		});

		sellShipComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellShipComboBoxActionPerformed(evt);
			}
		});

		sellShipButton.setText("Sell");
		sellShipButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellShipButtonActionPerformed(evt);
			}
		});

		hireMarinesNumberSpinner.setModel(sm);
		hireMarinesNumberSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				hireMarinesNumberSpinnerChanged(evt);
			}
		});

		hireMarinesButton.setText("Hire");
		hireMarinesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				hireMarinesButtonActionPerformed(evt);
			}
		});

		fromStorehouseTransferRadioButton.setText("storehouse");
		fromStorehouseTransferRadioButton.setActionCommand(TransferLocation.STOREHOUSE
				.name());
		fromStorehouseTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferFromActionPerformed(evt);
			}
		});

		fromShipTransferRadioButton.setActionCommand(TransferLocation.SHIP.name());
		fromShipTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferFromActionPerformed(evt);
			}
		});

		fromShipTransferComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				fromShipTransferComboBoxActionPerformed(evt);
			}
		});

		toStorehouseTransferRadioButton.setText("storehouse");
		toStorehouseTransferRadioButton.setActionCommand(TransferLocation.STOREHOUSE
				.name());
		toStorehouseTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferToActionPerformed(evt);
			}
		});

		toShipTransferRadioButton.setActionCommand(TransferLocation.SHIP.name());
		toShipTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferToActionPerformed(evt);
			}
		});

		toShipTransferComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				toShipTransferComboBoxActionPerformed(evt);
			}
		});

		marinesTransferRadioButton.setText("marines");
		marinesTransferRadioButton.setActionCommand(TransferCargo.MARINES.name());
		marinesTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCargoActionPerformed(evt);
			}
		});

		lightTransferRadioButton.setText("light cannons");
		lightTransferRadioButton.setActionCommand(TransferCargo.LIGHT.name());
		lightTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCargoActionPerformed(evt);
			}
		});

		heavyTransferRadioButton.setText("heavy cannons");
		heavyTransferRadioButton.setActionCommand(TransferCargo.HEAVY.name());
		heavyTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCargoActionPerformed(evt);
			}
		});

		mediumTransferRadioButton.setText("medium cannons");
		mediumTransferRadioButton.setActionCommand(TransferCargo.MEDIUM.name());
		mediumTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCargoActionPerformed(evt);
			}
		});

		marinesFromDeckTransferRadioButton.setText("deck");
		marinesFromDeckTransferRadioButton.setActionCommand(MarinesCompartment.DECK.name());
		marinesFromDeckTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferMarinesFromActionPerformed(evt);
			}
		});

		marinesFromBatteriesTransferRadioButton.setText("batteries");
		marinesFromBatteriesTransferRadioButton.setActionCommand(MarinesCompartment.BATTERIES.name());
		marinesFromBatteriesTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferMarinesFromActionPerformed(evt);
			}
		});

		marinesToDeckTransferRadioButton.setText("deck");
		marinesToDeckTransferRadioButton.setActionCommand(MarinesCompartment.DECK.name());
		marinesToDeckTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferMarinesToActionPerformed(evt);
			}
		});

		marinesToBatteriesTransferRadioButton.setText("batteries");
		marinesToBatteriesTransferRadioButton.setActionCommand(MarinesCompartment.BATTERIES.name());
		marinesToBatteriesTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferMarinesToActionPerformed(evt);
			}
		});

		cannonsFromBowTransferRadioButton.setText("bow");
		cannonsFromBowTransferRadioButton.setActionCommand(GunCompartment.BOW.name());
		cannonsFromBowTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsFromActionPerformed(evt);
			}
		});

		cannonsFromLeftSideTransferRadioButton.setText("left side");
		cannonsFromLeftSideTransferRadioButton.setActionCommand(GunCompartment.SIDE_L.name());
		cannonsFromLeftSideTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsFromActionPerformed(evt);
			}
		});

		cannonsFromSternTransferRadioButton.setText("stern");
		cannonsFromSternTransferRadioButton.setActionCommand(GunCompartment.STERN.name());
		cannonsFromSternTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsFromActionPerformed(evt);
			}
		});

		cannonsFromRightSideTransferRadioButton.setText("right side");
		cannonsFromRightSideTransferRadioButton.setActionCommand(GunCompartment.SIDE_R.name());
		cannonsFromRightSideTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsFromActionPerformed(evt);
			}
		});

		cannonsToBowTransferRadioButton.setText("bow");
		cannonsToBowTransferRadioButton.setActionCommand(GunCompartment.BOW.name());
		cannonsToBowTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsToActionPerformed(evt);
			}
		});

		cannonsToLeftSideTransferRadioButton.setText("left side");
		cannonsToLeftSideTransferRadioButton.setActionCommand(GunCompartment.SIDE_L.name());
		cannonsToLeftSideTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsToActionPerformed(evt);
			}
		});

		cannonsToSternTransferRadioButton.setText("stern");
		cannonsToSternTransferRadioButton.setActionCommand(GunCompartment.STERN.name());
		cannonsToSternTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsToActionPerformed(evt);
			}
		});

		cannonsToRightSideTransferRadioButton.setText("right side");
		cannonsToRightSideTransferRadioButton.setActionCommand(GunCompartment.SIDE_R.name());
		cannonsToRightSideTransferRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferCannonsToActionPerformed(evt);
			}
		});

		transferNumberSpinner.setModel(sm);
		transferNumberSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				transferNumberSpinnerChanged(evt);
			}
		});

		transferTransferButton.setText("Transfer");
		transferTransferButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				transferTransferButtonActionPerformed(evt);
			}
		});

		transferFromButtonGroup = new ButtonGroup();
		transferFromButtonGroup.add(fromStorehouseTransferRadioButton);
		transferFromButtonGroup.add(fromShipTransferRadioButton);

		transferToButtonGroup = new ButtonGroup();
		transferToButtonGroup.add(toStorehouseTransferRadioButton);
		transferToButtonGroup.add(toShipTransferRadioButton);

		transferCargoButtonGroup = new ButtonGroup();
		transferCargoButtonGroup.add(marinesTransferRadioButton);
		transferCargoButtonGroup.add(lightTransferRadioButton);
		transferCargoButtonGroup.add(mediumTransferRadioButton);
		transferCargoButtonGroup.add(heavyTransferRadioButton);

		transferMarinesFromButtonGroup = new ButtonGroup();
		transferMarinesFromButtonGroup.add(marinesFromDeckTransferRadioButton);
		transferMarinesFromButtonGroup.add(marinesFromBatteriesTransferRadioButton);

		transferMarinesToButtonGroup = new ButtonGroup();
		transferMarinesToButtonGroup.add(marinesToDeckTransferRadioButton);
		transferMarinesToButtonGroup.add(marinesToBatteriesTransferRadioButton);

		transferCannonsFromButtonGroup = new ButtonGroup();
		transferCannonsFromButtonGroup.add(cannonsFromBowTransferRadioButton);
		transferCannonsFromButtonGroup.add(cannonsFromLeftSideTransferRadioButton);
		transferCannonsFromButtonGroup.add(cannonsFromRightSideTransferRadioButton);
		transferCannonsFromButtonGroup.add(cannonsFromSternTransferRadioButton);

		transferCannonsToButtonGroup = new ButtonGroup();
		transferCannonsToButtonGroup.add(cannonsToBowTransferRadioButton);
		transferCannonsToButtonGroup.add(cannonsToLeftSideTransferRadioButton);
		transferCannonsToButtonGroup.add(cannonsToRightSideTransferRadioButton);
		transferCannonsToButtonGroup.add(cannonsToSternTransferRadioButton);

		sellCannonsLightRadioButton.setText("light");
		sellCannonsLightRadioButton.setActionCommand(Gun.LIGHT.name());
		sellCannonsLightRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellCannonsButtonsActionPerformed(evt);
			}
		});

		sellCannonsMediumRadioButton.setText("medium");
		sellCannonsMediumRadioButton.setActionCommand(Gun.MEDIUM.name());
		sellCannonsMediumRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellCannonsButtonsActionPerformed(evt);
			}
		});

		sellCannonsHeavyRadioButton.setText("heavy");
		sellCannonsHeavyRadioButton.setActionCommand(Gun.HEAVY.name());
		sellCannonsHeavyRadioButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellCannonsButtonsActionPerformed(evt);
			}
		});

		sellCannonsSpinner.setModel(sm);
		sellCannonsSpinner.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent evt) {
				sellCannonsSpinnerChanged(evt);
			}
		});

		sellCannonsButton.setText("Sell");
		sellCannonsButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				sellCannonsButtonActionPerformed(evt);
			}
		});

		buyCannonsButton.setText("Buy");
		buyCannonsButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				buyCannonsButtonActionPerformed(evt);
			}
		});

		sellCannonsButtonGroup = new ButtonGroup();
		sellCannonsButtonGroup.add(sellCannonsLightRadioButton);
		sellCannonsButtonGroup.add(sellCannonsMediumRadioButton);
		sellCannonsButtonGroup.add(sellCannonsHeavyRadioButton);

		partnerContractComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				partnerContractComboBoxActionPerformed(evt);
			}
		});

		myShipsContractList.setModel(new AbstractListModel() {

			String[] strings = { "" };


			public int getSize() {
				return strings.length;
			}


			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		myShipsEnContractScrollPane.setViewportView(myShipsContractList);
		myShipsContractScrollPane.setViewportView(myShipsEnContractScrollPane);

		partnerShipsContractList.setModel(new javax.swing.AbstractListModel() {

			String[] strings = { "" };


			public int getSize() {
				return strings.length;
			}


			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		partnerShipsEnContractScrollPane.setViewportView(partnerShipsContractList);
		partnerShipsContractScrollPane.setViewportView(partnerShipsEnContractScrollPane);

		myLightContractSpinner.setModel(sm);
		myMediumContractSpinner.setModel(sm);
		myHeavyContractSpinner.setModel(sm);
		myMarinesContractSpinner.setModel(sm);
		myGoldPiecesContractSpinner.setModel(sm);
		mySilverTonsContractSpinner.setModel(sm);

		partnerLightContractSpinner.setModel(sm);
		partnerMediumContractSpinner.setModel(sm);
		partnerHeavyContractSpinner.setModel(sm);
		partnerMarinesContractSpinner.setModel(sm);
		partnerGoldPiecesContractSpinner.setModel(sm);
		partnerSilverTonsContractSpinner.setModel(sm);

		myFreeCommanderContractCheckBox.setText("free commander");
		partnerFreeCommanderContractCheckBox.setText("free commander");

		submitOfferButton.setText("Submit offer");
		submitOfferButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				submitOfferButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout exchangeLayout = new javax.swing.GroupLayout(exchangeTabPanel);
		exchangeTabPanel.setLayout(exchangeLayout);
		exchangeLayout
				.setHorizontalGroup(exchangeLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								exchangeLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												exchangeLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(sellShipLabel)
														.addComponent(buildShipButton,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addComponent(buyShipClassLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(buildShipClassComboBox, 0, 145,
																				Short.MAX_VALUE))
														.addComponent(buyFoundryButton,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addComponent(buyShipyardButton,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addComponent(hSeparator2,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addComponent(hSeparator1,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(10, 10, 10)
																		.addComponent(sellSilverAmountLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(sellSilverSpinner,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				111,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addComponent(sellSilverLabel)
														.addComponent(sellSilverButton,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addComponent(repairLabel)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(10, 10, 10)
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								repairDurabilityRadioButton)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addComponent(
																												repairIDLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												repairShipComboBox,
																												0,
																												140,
																												Short.MAX_VALUE))
																						.addComponent(
																								repairMastRadioButton)
																						.addComponent(
																								repairHelmRadioButton)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addComponent(
																												repairPointsLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												repairPointsSpinner,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												131,
																												Short.MAX_VALUE))
																						.addComponent(
																								repairButton,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								168, Short.MAX_VALUE)))
														.addComponent(hSeparator3,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(10, 10, 10)
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(shipPriceLabel)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addComponent(
																												sellShipIDLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												sellShipComboBox,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												122,
																												javax.swing.GroupLayout.PREFERRED_SIZE))))
														.addComponent(sellShipButton,
																javax.swing.GroupLayout.DEFAULT_SIZE, 178,
																Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(vSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												exchangeLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(10, 10, 10)
																		.addComponent(hireMarinesNumberLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(hireMarinesNumberSpinner,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				69,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(20, 20, 20)
																		.addComponent(hireMarinesButton))
														.addComponent(hireMarinesLabel)
														.addComponent(hSeparator9,
																javax.swing.GroupLayout.PREFERRED_SIZE, 240,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addComponent(
																								transferTransferButton,
																								javax.swing.GroupLayout.Alignment.LEADING,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								273, Short.MAX_VALUE)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																transferLabel)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																								fromTransferLabel)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGap(10,
																																												10,
																																												10)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addGroup(
																																																exchangeLayout
																																																		.createSequentialGroup()
																																																		.addComponent(
																																																				fromShipTransferRadioButton)
																																																		.addPreferredGap(
																																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																		.addComponent(
																																																				fromShipTransferComboBox,
																																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																																				71,
																																																				javax.swing.GroupLayout.PREFERRED_SIZE))
																																														.addComponent(
																																																fromStorehouseTransferRadioButton))))
																																		.addGap(22,
																																				22,
																																				22)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addGroup(
																																																exchangeLayout
																																																		.createSequentialGroup()
																																																		.addComponent(
																																																				toShipTransferRadioButton)
																																																		.addPreferredGap(
																																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																																		.addComponent(
																																																				toShipTransferComboBox,
																																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																																				71,
																																																				javax.swing.GroupLayout.PREFERRED_SIZE))
																																														.addComponent(
																																																toStorehouseTransferRadioButton)))
																																						.addComponent(
																																								toTransferLabel)))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																								marinesFromTransferLabel)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGap(10,
																																												10,
																																												10)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																marinesFromBatteriesTransferRadioButton)
																																														.addComponent(
																																																marinesFromDeckTransferRadioButton))))
																																		.addGap(48,
																																				48,
																																				48)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																								marinesToTransferLabel)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGap(10,
																																												10,
																																												10)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																marinesToBatteriesTransferRadioButton)
																																														.addComponent(
																																																marinesToDeckTransferRadioButton)))))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																								cannonsFromTransferLabel)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGap(10,
																																												10,
																																												10)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																cannonsFromLeftSideTransferRadioButton)
																																														.addComponent(
																																																cannonsFromBowTransferRadioButton)
																																														.addComponent(
																																																cannonsFromRightSideTransferRadioButton)
																																														.addComponent(
																																																cannonsFromSternTransferRadioButton))))
																																		.addGap(48,
																																				48,
																																				48)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																								cannonsToTransferLabel)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGap(10,
																																												10,
																																												10)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																cannonsToLeftSideTransferRadioButton)
																																														.addComponent(
																																																cannonsToBowTransferRadioButton)
																																														.addComponent(
																																																cannonsToRightSideTransferRadioButton)
																																														.addComponent(
																																																cannonsToSternTransferRadioButton)))))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGap(10,
																																												10,
																																												10)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																lightTransferRadioButton)
																																														.addComponent(
																																																marinesTransferRadioButton))
																																										.addGap(18,
																																												18,
																																												18)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																heavyTransferRadioButton)
																																														.addComponent(
																																																mediumTransferRadioButton)))
																																						.addComponent(
																																								cargoTransferLabel)))
																														.addComponent(
																																hSeparator5,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																267,
																																Short.MAX_VALUE)
																														.addComponent(
																																hSeparator6,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																267,
																																Short.MAX_VALUE)
																														.addComponent(
																																hSeparator4,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																267,
																																Short.MAX_VALUE)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				numberTransferLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				transferNumberSpinner,
																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																				62,
																																				javax.swing.GroupLayout.PREFERRED_SIZE)))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												hSeparator7,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												javax.swing.GroupLayout.PREFERRED_SIZE)))
																		.addGap(8, 8, 8)))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(vSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGroup(
												exchangeLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(8, 8, 8)
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addGap(81, 81,
																												81)
																										.addComponent(
																												contractLabel))
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												8,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addComponent(
																												myShipsContractScrollPane,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												100,
																												javax.swing.GroupLayout.PREFERRED_SIZE))
																						.addComponent(goldContractLabel)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												8,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addComponent(
																												myGoldPiecesContractLabel))
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addGap(8, 8, 8)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																sellCannonsLabel)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addComponent(
																																												sellCannonsNumberLabel)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																										.addComponent(
																																												sellCannonsSpinner,
																																												javax.swing.GroupLayout.PREFERRED_SIZE,
																																												41,
																																												javax.swing.GroupLayout.PREFERRED_SIZE)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																										.addComponent(
																																												sellCannonsButton)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																										.addComponent(
																																												buyCannonsButton))
																																						.addGroup(
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addComponent(
																																												sellCannonsTypeLabel)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																										.addComponent(
																																												sellCannonsLightRadioButton)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																										.addComponent(
																																												sellCannonsMediumRadioButton)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																										.addComponent(
																																												sellCannonsHeavyRadioButton))))
																														.addComponent(
																																hSeparator8,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																245,
																																javax.swing.GroupLayout.PREFERRED_SIZE)))
																						.addComponent(
																								marinesContractLabel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(33,
																																				33,
																																				33)
																																		.addComponent(
																																				myContractLabel))
																														.addComponent(
																																cannonsContractLabel)
																														.addComponent(
																																shipsContractLabel)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(10,
																																				10,
																																				10)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING,
																																								false)
																																						.addComponent(
																																								myMediumContractLabel,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								Short.MAX_VALUE)
																																						.addComponent(
																																								myLightContractLabel,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								Short.MAX_VALUE)
																																						.addComponent(
																																								myHeavyContractLabel,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								Short.MAX_VALUE))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING)
																																						.addComponent(
																																								myHeavyContractSpinner,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								54,
																																								Short.MAX_VALUE)
																																						.addComponent(
																																								myMediumContractSpinner,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								54,
																																								Short.MAX_VALUE)
																																						.addComponent(
																																								myLightContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								63,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))))
																										.addGap(21, 21,
																												21)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																				8,
																																				javax.swing.GroupLayout.PREFERRED_SIZE)
																																		.addComponent(
																																				partnerGoldPiecesContractLabel))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				partnerContractLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				partnerContractComboBox,
																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																				javax.swing.GroupLayout.DEFAULT_SIZE,
																																				javax.swing.GroupLayout.PREFERRED_SIZE))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(8,
																																				8,
																																				8)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.TRAILING)
																																						.addGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING,
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.TRAILING,
																																																false)
																																														.addComponent(
																																																partnerMediumContractLabel,
																																																javax.swing.GroupLayout.Alignment.LEADING,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																Short.MAX_VALUE)
																																														.addComponent(
																																																partnerLightContractLabel,
																																																javax.swing.GroupLayout.Alignment.LEADING,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																Short.MAX_VALUE)
																																														.addComponent(
																																																partnerHeavyContractLabel,
																																																javax.swing.GroupLayout.Alignment.LEADING,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																Short.MAX_VALUE))
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING,
																																																false)
																																														.addComponent(
																																																partnerHeavyContractSpinner)
																																														.addComponent(
																																																partnerMediumContractSpinner)
																																														.addComponent(
																																																partnerLightContractSpinner,
																																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																																63,
																																																Short.MAX_VALUE)))
																																						.addGroup(
																																								javax.swing.GroupLayout.Alignment.LEADING,
																																								exchangeLayout
																																										.createSequentialGroup()
																																										.addComponent(
																																												partnerMarinesNumberContractLabel,
																																												javax.swing.GroupLayout.PREFERRED_SIZE,
																																												51,
																																												javax.swing.GroupLayout.PREFERRED_SIZE)
																																										.addPreferredGap(
																																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																										.addGroup(
																																												exchangeLayout
																																														.createParallelGroup(
																																																javax.swing.GroupLayout.Alignment.LEADING)
																																														.addComponent(
																																																partnerMarinesContractSpinner,
																																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																																60,
																																																javax.swing.GroupLayout.PREFERRED_SIZE)
																																														.addGroup(
																																																exchangeLayout
																																																		.createParallelGroup(
																																																				javax.swing.GroupLayout.Alignment.TRAILING,
																																																				false)
																																																		.addComponent(
																																																				partnerSilverTonsContractSpinner,
																																																				javax.swing.GroupLayout.Alignment.LEADING)
																																																		.addComponent(
																																																				partnerGoldPiecesContractSpinner,
																																																				javax.swing.GroupLayout.Alignment.LEADING,
																																																				javax.swing.GroupLayout.DEFAULT_SIZE,
																																																				51,
																																																				Short.MAX_VALUE)))))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																				13,
																																				Short.MAX_VALUE))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																																				6,
																																				javax.swing.GroupLayout.PREFERRED_SIZE)
																																		.addComponent(
																																				partnerShipsContractScrollPane,
																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																				100,
																																				javax.swing.GroupLayout.PREFERRED_SIZE))))
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addGap(8, 8, 8)
																										.addComponent(
																												myMarinesNumberContractLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING,
																																false)
																														.addComponent(
																																myGoldPiecesContractSpinner,
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																myMarinesContractSpinner,
																																javax.swing.GroupLayout.Alignment.LEADING,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																64,
																																Short.MAX_VALUE)))))
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(18, 18, 18)
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								false)
																						.addComponent(
																								submitOfferButton,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								exchangeLayout
																										.createSequentialGroup()
																										.addComponent(
																												myFreeCommanderContractCheckBox,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												116,
																												Short.MAX_VALUE)
																										.addGap(21, 21,
																												21)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addComponent(
																																partnerSilverTonsContractLabel)
																														.addComponent(
																																partnerFreeCommanderContractCheckBox,
																																javax.swing.GroupLayout.Alignment.TRAILING,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																125,
																																Short.MAX_VALUE)))))
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(silverContractLabel))
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGap(18, 18, 18)
																		.addComponent(mySilverTonsContractLabel,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				33,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(mySilverTonsContractSpinner,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				55,
																				javax.swing.GroupLayout.PREFERRED_SIZE)))
										.addContainerGap()));
		exchangeLayout
				.setVerticalGroup(exchangeLayout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								exchangeLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												exchangeLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addComponent(vSeparator2,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				537,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addContainerGap())
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																exchangeLayout
																		.createSequentialGroup()
																		.addComponent(hSeparator7,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				10,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addGap(71, 71, 71))
														.addGroup(
																exchangeLayout
																		.createSequentialGroup()
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								false)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								exchangeLayout
																										.createSequentialGroup()
																										.addComponent(
																												sellSilverLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																sellSilverAmountLabel)
																														.addComponent(
																																sellSilverSpinner,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												sellSilverButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												hSeparator1,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												repairLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																repairIDLabel)
																														.addComponent(
																																repairShipComboBox,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												repairDurabilityRadioButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												repairMastRadioButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												repairHelmRadioButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																repairPointsLabel)
																														.addComponent(
																																repairPointsSpinner,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												repairButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												hSeparator2,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												buyShipyardButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												buyFoundryButton)
																										.addGap(18, 18,
																												18)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																buyShipClassLabel)
																														.addComponent(
																																buildShipClassComboBox,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addGap(12, 12,
																												12)
																										.addComponent(
																												buildShipButton)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												hSeparator3,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												sellShipLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																sellShipIDLabel)
																														.addComponent(
																																sellShipComboBox,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												shipPriceLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												sellShipButton))
																						.addGroup(
																								exchangeLayout
																										.createSequentialGroup()
																										.addComponent(
																												hireMarinesLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																hireMarinesNumberLabel)
																														.addComponent(
																																hireMarinesNumberSpinner,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE)
																														.addComponent(
																																hireMarinesButton))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												hSeparator9,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.addComponent(
																												transferLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																fromTransferLabel)
																														.addComponent(
																																toTransferLabel))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				fromStorehouseTransferRadioButton)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.TRAILING)
																																						.addComponent(
																																								fromShipTransferRadioButton)
																																						.addComponent(
																																								fromShipTransferComboBox,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE)))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				toStorehouseTransferRadioButton)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.TRAILING)
																																						.addComponent(
																																								toShipTransferRadioButton)
																																						.addComponent(
																																								toShipTransferComboBox,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												hSeparator4,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												cargoTransferLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				marinesTransferRadioButton)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				lightTransferRadioButton))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				mediumTransferRadioButton)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				heavyTransferRadioButton)))
																										.addGap(12, 12,
																												12)
																										.addComponent(
																												hSeparator5,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.TRAILING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				marinesFromTransferLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				marinesFromDeckTransferRadioButton)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				marinesFromBatteriesTransferRadioButton))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				marinesToTransferLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				marinesToDeckTransferRadioButton)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				marinesToBatteriesTransferRadioButton)))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												hSeparator6,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																cannonsToTransferLabel)
																														.addComponent(
																																cannonsFromTransferLabel))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																cannonsFromBowTransferRadioButton)
																														.addComponent(
																																cannonsToBowTransferRadioButton))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																cannonsFromLeftSideTransferRadioButton)
																														.addComponent(
																																cannonsToLeftSideTransferRadioButton))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																cannonsFromRightSideTransferRadioButton)
																														.addComponent(
																																cannonsToRightSideTransferRadioButton))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																cannonsFromSternTransferRadioButton)
																														.addComponent(
																																cannonsToSternTransferRadioButton))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																numberTransferLabel)
																														.addComponent(
																																transferNumberSpinner,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addGap(18, 18,
																												18)
																										.addComponent(
																												transferTransferButton)
																										.addGap(20, 20,
																												20)))
																		.addContainerGap(14, Short.MAX_VALUE))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																exchangeLayout
																		.createSequentialGroup()
																		.addGroup(
																				exchangeLayout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.LEADING,
																								exchangeLayout
																										.createSequentialGroup()
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				sellCannonsLabel)
																																		.addGap(36,
																																				36,
																																				36)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								sellCannonsNumberLabel)
																																						.addComponent(
																																								sellCannonsSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE)
																																						.addComponent(
																																								sellCannonsButton)
																																						.addComponent(
																																								buyCannonsButton)))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(20,
																																				20,
																																				20)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								sellCannonsTypeLabel)
																																						.addComponent(
																																								sellCannonsLightRadioButton)
																																						.addComponent(
																																								sellCannonsMediumRadioButton)
																																						.addComponent(
																																								sellCannonsHeavyRadioButton))))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												hSeparator8,
																												javax.swing.GroupLayout.PREFERRED_SIZE,
																												10,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(
																												contractLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																myContractLabel)
																														.addComponent(
																																partnerContractLabel)
																														.addComponent(
																																partnerContractComboBox,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				shipsContractLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(
																																				myShipsContractScrollPane,
																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																				64,
																																				javax.swing.GroupLayout.PREFERRED_SIZE)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																		.addComponent(
																																				cannonsContractLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								myLightContractLabel)
																																						.addComponent(
																																								myLightContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								myMediumContractLabel)
																																						.addComponent(
																																								myMediumContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								myHeavyContractLabel)
																																						.addComponent(
																																								myHeavyContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE)))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGap(20,
																																				20,
																																				20)
																																		.addComponent(
																																				partnerShipsContractScrollPane,
																																				javax.swing.GroupLayout.PREFERRED_SIZE,
																																				64,
																																				javax.swing.GroupLayout.PREFERRED_SIZE)
																																		.addGap(31,
																																				31,
																																				31)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								partnerLightContractLabel)
																																						.addComponent(
																																								partnerLightContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								partnerMediumContractLabel)
																																						.addComponent(
																																								partnerMediumContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								partnerHeavyContractLabel)
																																						.addComponent(
																																								partnerHeavyContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												marinesContractLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.LEADING)
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								myMarinesNumberContractLabel)
																																						.addComponent(
																																								myMarinesContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																																		.addComponent(
																																				goldContractLabel)
																																		.addPreferredGap(
																																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								myGoldPiecesContractLabel)
																																						.addComponent(
																																								myGoldPiecesContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE)))
																														.addGroup(
																																exchangeLayout
																																		.createSequentialGroup()
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								partnerMarinesNumberContractLabel)
																																						.addComponent(
																																								partnerMarinesContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))
																																		.addGap(31,
																																				31,
																																				31)
																																		.addGroup(
																																				exchangeLayout
																																						.createParallelGroup(
																																								javax.swing.GroupLayout.Alignment.BASELINE)
																																						.addComponent(
																																								partnerGoldPiecesContractLabel)
																																						.addComponent(
																																								partnerGoldPiecesContractSpinner,
																																								javax.swing.GroupLayout.PREFERRED_SIZE,
																																								javax.swing.GroupLayout.DEFAULT_SIZE,
																																								javax.swing.GroupLayout.PREFERRED_SIZE))))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(
																												silverContractLabel)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																mySilverTonsContractLabel)
																														.addComponent(
																																mySilverTonsContractSpinner,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE)
																														.addComponent(
																																partnerSilverTonsContractLabel)
																														.addComponent(
																																partnerSilverTonsContractSpinner,
																																javax.swing.GroupLayout.PREFERRED_SIZE,
																																javax.swing.GroupLayout.DEFAULT_SIZE,
																																javax.swing.GroupLayout.PREFERRED_SIZE))
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.addGroup(
																												exchangeLayout
																														.createParallelGroup(
																																javax.swing.GroupLayout.Alignment.BASELINE)
																														.addComponent(
																																myFreeCommanderContractCheckBox)
																														.addComponent(
																																partnerFreeCommanderContractCheckBox))
																										.addGap(18, 18,
																												18)
																										.addComponent(
																												submitOfferButton))
																						.addComponent(
																								vSeparator1,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								552, Short.MAX_VALUE))
																		.addContainerGap()))));

		tabbedPane.addTab("Exchange", null, exchangeTabPanel, null);

		// ---- Summary Panel

		summaryPanel = new JPanel();
		summaryPanel.setLayout(null);

		nextPlayerButton = new JButton();
		nextPlayerButton.setText("End turn");

		passButton = new JButton();
		passButton.setText("Pass");

		currentPlayerLabel = new JLabel();
		currentPlayerLabel.setText("Current player: none");

		summaryPanel.add(nextPlayerButton);
		nextPlayerButton.setBounds(30, 30, 100, 40);
		nextPlayerButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				nextPlayerButtonActionPerformed(evt);
			}
		});

		summaryPanel.add(passButton);
		passButton.setBounds(150, 30, 100, 40);
		passButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				passButtonActionPerformed(evt);
			}
		});

		summaryPanel.add(currentPlayerLabel);
		currentPlayerLabel.setBounds(50, 90, 150, 20);

		tabbedPane.addTab("Summary", null, summaryPanel, null);

		tabbedPane.setVisible(true);
		this.add(tabbedPane);

		// -- TEST
		tabbedPane.setSelectedIndex(2);
		// --

	}


	@SuppressWarnings("static-access")
	private void updateAuctionsSection() {
		updateFinished = false;

		Object selection = auctionsBankList.getSelectedValue();

		Vector<String> data = new Vector();

		for (int i = 0; i < MainBoard.game.getAuctionsCount(); i++) {
			String str = "";
			Auction a = MainBoard.game.getAuction(i);

			str += a.auctionID + ", " + a.offeredShipClass.toString() + " ";

			if (a.offeredShipID == null)
				str += "(-)";
			else
				str += "(" + a.offeredShipID + ")";

			data.add(str);
		}

		auctionsBankList.setListData(data);

		if (selection != null)
			auctionsBankList.setSelectedValue(selection, true);

		if (auctionsBankList.getSelectedValue() != null) {
			PlayerClass player = MainBoard.game.getPlayer(MainBoard.game.getCurrentPlayer());

			int selectedAuctionID = extractIDFromObject(auctionsBankList.getSelectedValue());
			Auction auction = MainBoard.game.getAuction(selectedAuctionID);

			ShipClass classID = auction.offeredShipClass;

			Player bidder = Player.NONE;
			int currentOffer = auction.startingPrice;
			for (int i = 0; i < Commons.PLAYERS_MAX; i++) {
				if (auction.offers[i] > currentOffer) {
					currentOffer = auction.offers[i];
					bidder = Player.valueOf(i);
					break;
				}
			}

			lastOfferBankLabel.setText("Last offer: " + bidder.toString());

			if (player.getGold() <= currentOffer) {
				yourOfferBankSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
				yourOfferBankSpinner.setEnabled(false);
				submitOfferBankButton.setEnabled(false);
			} else {
				yourOfferBankSpinner.setModel(new SpinnerNumberModel(currentOffer + 1, currentOffer + 1, player
						.getGold(), 1));
				yourOfferBankSpinner.setEnabled(true);
				submitOfferBankButton.setEnabled(true);
			}

			if (auction.offeredShipID == null) {
				shipIDBankLabel.setText("ID: -");
				shipDurabilityBankLabel.setText("Durability: " + classID.getDurabilityMax() + "/"
						+ classID.getDurabilityMax());
				shipMastBankLabel.setText("Mast: " + classID.getMastMax() + "/" + classID.getMastMax());
				shipHelmBankLabel.setText("Helm: " + classID.getHelmMax() + "/" + classID.getHelmMax());

				for (GunCompartment c : GunCompartment.values()) {
					if (c == GunCompartment.NONE)
						continue;

					for (Gun t : Gun.values()) {
						if (t == Gun.NONE)
							continue;
						String str = classID.getCannonMax()[c.ordinal()][t.ordinal()] + "|"
								+ classID.getCannonMax()[c.ordinal()][t.ordinal()];
						shipCannonsBankTable.setValueAt(str, c.ordinal(), t.ordinal() + 1);
					}
				}

				shipRepairBankLabel.setText("Repair cost: none");
			} else {
				int shipID = auction.offeredShipID;
				Ship ship = MainBoard.game.getShip(shipID);

				shipIDBankLabel.setText("ID: " + shipID);
				shipDurabilityBankLabel.setText("Durability: " + ship.getDurability() + "/" + classID.getDurabilityMax());
				shipMastBankLabel.setText("Mast: " + ship.getMast() + "/" + classID.getMastMax());
				shipHelmBankLabel.setText("Helm: " + ship.getHelm(Commons.BOTH) + "/" + classID.getHelmMax());

				for (GunCompartment c : GunCompartment.values()) {
					if (c == GunCompartment.NONE)
						continue;

					for (Gun t : Gun.values()) {
						if (t == Gun.NONE)
							continue;
						String str = MainBoard.game.getShipCannonsNumber(shipID, c, t, Commons.BOTH) + "|"
								+ classID.getCannonMax()[c.ordinal()][t.ordinal()];
						shipCannonsBankTable.setValueAt(str, c.ordinal(), t.ordinal() + 1);
					}
				}

				if (ship.calculateRepairCosts() > 0)
					shipRepairBankLabel.setText("Repair cost: " + ship.calculateRepairCosts());
				else
					shipRepairBankLabel.setText("Repair cost: none");
			}

			shipClassBankLabel.setText("Class: " + auction.offeredShipClass.toString());
		} else {
			lastOfferBankLabel.setText("Last offer: NONE");
			yourOfferBankSpinner.setModel(new SpinnerNumberModel(0, 0, 0, 0));
			submitOfferBankButton.setEnabled(false);

			shipFigureBankLabel.setText("Ship figure");
			shipIDBankLabel.setText("ID: -");
			shipClassBankLabel.setText("Class: -");
			shipDurabilityBankLabel.setText("Durability: -/-");
			shipMastBankLabel.setText("Mast: -/-");
			shipHelmBankLabel.setText("Helm: -/-");
			shipCannonsBankTable.setModel(shipFigureDefaultModel);
			shipRepairBankLabel.setText("Repair cost: -");
		}

		updateFinished = true;
	}
	*/
}