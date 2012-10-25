package sfmainframe;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import sfmainframe.gameplay.DestroyShipMode;
import sfmainframe.gui.MsgMode;
import sfmainframe.gui.Tabs;
import sfmainframe.ship.Gun;
import sfmainframe.ship.GunCompartment;

public class SabotageUnderDeckDialog extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    JFrame frame;
    JLabel gunCompartmentLabel, gunTypeLabel;
    JRadioButton bowRadioButton, sideLeftRadioButton,
	    sideRightRadioButton, sternRadioButton,
	    lightCannonRadioButton, mediumCannonRadioButton,
	    heavyCannonRadioButton;
    ButtonGroup gunCompartmentButtonGroup, gunTypeButtonGroup;
    JButton confirmButton, cancelButton;

    GunCompartment selectedGunCompartment = GunCompartment.NONE;
    Gun selectedGunType = Gun.NONE;

    public void addComponentsToPane(Container pane) {
	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	c.anchor = GridBagConstraints.FIRST_LINE_START;

	c.gridx = 0; c.gridy = 0;
	c.insets = new Insets(0,0,5,10);
	pane.add(gunCompartmentLabel, c);

	c.insets = new Insets(0,0,0,10);

	c.gridx = 0; c.gridy = 1;
	pane.add(bowRadioButton, c);

	c.gridx = 0; c.gridy = 2;
	pane.add(sideLeftRadioButton, c);

	c.gridx = 0; c.gridy = 3;
	pane.add(sideRightRadioButton, c);

	c.gridx = 0; c.gridy = 4;
	pane.add(sternRadioButton, c);

	c.gridx = 0; c.gridy = 5;
	c.insets = new Insets(20,0,0,10);
	c.anchor = GridBagConstraints.CENTER;
	pane.add(confirmButton, c);


	c.anchor = GridBagConstraints.FIRST_LINE_START;

	c.gridx = 1; c.gridy = 0;
	c.insets = new Insets(0,0,5,10);
	pane.add(gunTypeLabel, c);

	c.insets = new Insets(0,0,0,10);

	c.gridx = 1; c.gridy = 1;
	pane.add(lightCannonRadioButton, c);

	c.gridx = 1; c.gridy = 2;
	pane.add(mediumCannonRadioButton, c);

	c.gridx = 1; c.gridy = 3;
	pane.add(heavyCannonRadioButton, c);

/*	    c.gridx = 1; c.gridy = 4;
	pane.add(sternRadioButton, c);
*/
	c.gridx = 1; c.gridy = 5;
	c.insets = new Insets(20,0,0,10);
	c.anchor = GridBagConstraints.CENTER;
	pane.add(cancelButton, c);
    }


    private void compartmentTypeActionPerformed(ActionEvent evt) {
	String selection = evt.getActionCommand();

	if (selection.equals("bow"))
	    selectedGunCompartment = GunCompartment.BOW;
	if (selection.equals("sidel"))
	    selectedGunCompartment = GunCompartment.SIDE_L;
	if (selection.equals("sider"))
	    selectedGunCompartment = GunCompartment.SIDE_R;
	if (selection.equals("stern"))
	    selectedGunCompartment = GunCompartment.STERN;

	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),selectedGunCompartment,Gun.LIGHT, Commons.BOTH) > 0) {
	    lightCannonRadioButton.setEnabled(true);
	    if (selectedGunType == Gun.NONE) {
		selectedGunType = Gun.LIGHT;
		lightCannonRadioButton.setSelected(true);
	    }
	}
	else
	    lightCannonRadioButton.setEnabled(false);
	
	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),selectedGunCompartment,Gun.MEDIUM, Commons.BOTH) > 0) {
	    mediumCannonRadioButton.setEnabled(true);
	    if (selectedGunType == Gun.NONE) {
		selectedGunType = Gun.MEDIUM;
		mediumCannonRadioButton.setSelected(true);
	    }
	}
	else
	    mediumCannonRadioButton.setEnabled(false);

	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),selectedGunCompartment,Gun.HEAVY, Commons.BOTH) > 0) {
	    heavyCannonRadioButton.setEnabled(true);
	    if (selectedGunType == Gun.NONE) {
		selectedGunType = Gun.HEAVY;
		heavyCannonRadioButton.setSelected(true);
	    }
	}
	else
	    heavyCannonRadioButton.setEnabled(false);
    }

    private void gunTypeActionPerformed(ActionEvent evt) {
	String selection = evt.getActionCommand();

	if (selection.equals("light"))
	    selectedGunType = Gun.LIGHT;
	if (selection.equals("medium"))
	    selectedGunType = Gun.MEDIUM;
	if (selection.equals("heavy"))
	    selectedGunType = Gun.HEAVY;
    }

    private void confirmButtonActionPerformed(ActionEvent evt) {
	if (MainBoard.game.checkIfSabotageSuccessful(MainBoard.getSelectedShipID(),
		MainBoard.game.getCurrentPlayer(), MainBoard.getSelectedMarinesSource())) {
	    MainBoard.game.destroyCannonIS(MainBoard.getSelectedShipID(), selectedGunCompartment,
		    selectedGunType, MsgMode.ON); // par. 12.2.3.4

	    MainBoard.addMessage("Ship #"+(MainBoard.getSelectedShipID()+1)+": sabotage successfull. "+
		    selectedGunType.toString()+ " cannon on "+selectedGunCompartment.toString()+" destroyed.\n");

	    Object[] options = {"OK", "Cancel"};
	    int rv = JOptionPane.showOptionDialog(frame,
		"Sabotage successful.\nDo you want to blow ship up?",
		null,
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		options,
		options[1]);

	    if (rv == JOptionPane.YES_OPTION && MainBoard.game.rollDice() == 6) {
		MainBoard.game.sinkShip(MainBoard.getSelectedShipID(), DestroyShipMode.BLOWUP);	// par. 12.2.3.5.1
		MainBoard.boardPanel.repaint();
		MainBoard.setSelectedShip(null,Tabs.MOVEMENT);
	    }
	}
	this.dispose();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
	this.dispose();
    }

    private void initializeGUIElements() {
	gunCompartmentLabel = new JLabel("Compartment");
	gunTypeLabel = new JLabel("Type");

       bowRadioButton = new JRadioButton("bow");
	bowRadioButton.setActionCommand("bow");
	bowRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		compartmentTypeActionPerformed(evt);
	    }
	});

	sideLeftRadioButton = new JRadioButton("left side");
	sideLeftRadioButton.setActionCommand("sidel");
	sideLeftRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		compartmentTypeActionPerformed(evt);
	    }
	});

	sideRightRadioButton = new JRadioButton("right side");
	sideRightRadioButton.setActionCommand("sider");
	sideRightRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		compartmentTypeActionPerformed(evt);
	    }
	});

	sternRadioButton = new JRadioButton("stern");
	sternRadioButton.setActionCommand("stern");
	sternRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		compartmentTypeActionPerformed(evt);
	    }
	});

	lightCannonRadioButton = new JRadioButton("light");
	lightCannonRadioButton.setActionCommand("light");
	lightCannonRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		gunTypeActionPerformed(evt);
	    }
	});

	mediumCannonRadioButton = new JRadioButton("medium");
	mediumCannonRadioButton.setActionCommand("medium");
	mediumCannonRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		gunTypeActionPerformed(evt);
	    }
	});

	heavyCannonRadioButton = new JRadioButton("heavy");
	heavyCannonRadioButton.setActionCommand("heavy");
	heavyCannonRadioButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		gunTypeActionPerformed(evt);
	    }
	});

	gunCompartmentButtonGroup = new ButtonGroup();
	gunCompartmentButtonGroup.add(bowRadioButton);
	gunCompartmentButtonGroup.add(sideLeftRadioButton);
	gunCompartmentButtonGroup.add(sideRightRadioButton);
	gunCompartmentButtonGroup.add(sternRadioButton);

	gunTypeButtonGroup = new ButtonGroup();
	gunTypeButtonGroup.add(lightCannonRadioButton);
	gunTypeButtonGroup.add(mediumCannonRadioButton);
	gunTypeButtonGroup.add(heavyCannonRadioButton);

	confirmButton = new JButton("Confirm");
	confirmButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	       confirmButtonActionPerformed(evt);
	    }
	});

	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		cancelButtonActionPerformed(evt);
	    }
	});
    }

    private void setControlsParameters() {
	bowRadioButton.setEnabled(false);
	sideLeftRadioButton.setEnabled(false);
	sideRightRadioButton.setEnabled(false);
	sternRadioButton.setEnabled(false);
	lightCannonRadioButton.setEnabled(false);
	mediumCannonRadioButton.setEnabled(false);
	heavyCannonRadioButton.setEnabled(false);

	confirmButton.setEnabled(false);
	cancelButton.setEnabled(true);


	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.BOW,Gun.LIGHT, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.BOW,Gun.MEDIUM, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.BOW,Gun.HEAVY, Commons.BOTH) > 0) {
	    bowRadioButton.setEnabled(true);

	    if (selectedGunCompartment == GunCompartment.NONE) {
		selectedGunCompartment = GunCompartment.BOW;
		bowRadioButton.setSelected(true);
	    }
	}

	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.SIDE_L,Gun.LIGHT, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.SIDE_L,Gun.MEDIUM, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.SIDE_L,Gun.HEAVY, Commons.BOTH) > 0) {
	    sideLeftRadioButton.setEnabled(true);

	    if (selectedGunCompartment == GunCompartment.NONE) {
		selectedGunCompartment = GunCompartment.SIDE_L;
		sideLeftRadioButton.setSelected(true);
	    }
	}

	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.SIDE_R,Gun.LIGHT, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.SIDE_R,Gun.MEDIUM, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.SIDE_R,Gun.HEAVY, Commons.BOTH) > 0) {
	    sideRightRadioButton.setEnabled(true);

	    if (selectedGunCompartment == GunCompartment.NONE) {
		selectedGunCompartment = GunCompartment.SIDE_R;
		sideRightRadioButton.setSelected(true);
	    }
	}

	if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.STERN,Gun.LIGHT, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.STERN,Gun.MEDIUM, Commons.BOTH)+
		MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),GunCompartment.STERN,Gun.HEAVY, Commons.BOTH) > 0) {
	    sternRadioButton.setEnabled(true);

	    if (selectedGunCompartment == GunCompartment.NONE) {
		selectedGunCompartment = GunCompartment.STERN;
		sternRadioButton.setSelected(true);
	    }
	}

	if (selectedGunCompartment != GunCompartment.NONE) {
	    confirmButton.setEnabled(true);

	    if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),selectedGunCompartment,Gun.LIGHT, Commons.BOTH) > 0) {
		lightCannonRadioButton.setEnabled(true);
		if (selectedGunType == Gun.NONE) {
		    selectedGunType = Gun.LIGHT;
		    lightCannonRadioButton.setSelected(true);
		}
	    }
	    if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),selectedGunCompartment,Gun.MEDIUM, Commons.BOTH) > 0) {
		mediumCannonRadioButton.setEnabled(true);
		if (selectedGunType == Gun.NONE) {
		    selectedGunType = Gun.MEDIUM;
		    mediumCannonRadioButton.setSelected(true);
		}
	    }
	    if (MainBoard.game.getShipCannonsNumber(MainBoard.getSelectedShipID(),selectedGunCompartment,Gun.HEAVY, Commons.BOTH) > 0) {
		heavyCannonRadioButton.setEnabled(true);
		if (selectedGunType == Gun.NONE) {
		    selectedGunType = Gun.HEAVY;
		    heavyCannonRadioButton.setSelected(true);
		}
	    }
	}
    }

    SabotageUnderDeckDialog(JFrame f) {
	super(f, "Select cannon to sabotage", true);
	frame = f;

	initializeGUIElements();
	addComponentsToPane(getContentPane());
	setControlsParameters();

	addWindowListener(new WA());

	setSize(230,220);
	setVisible(true);
    }

    class WA extends WindowAdapter {
	public void windowsClosing (WindowEvent evt) {
	    dispose();
	}
    }
}
