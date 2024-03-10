package spaceraze.client.game.panels.planet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spaceraze.battlehandler.spacebattle.TaskForceHandler;
import spaceraze.client.ColorConverter;
import spaceraze.client.GeneralConfirmPopupPanel;
import spaceraze.client.GeneralMessagePopupPanel;
import spaceraze.client.components.CheckBoxPanel;
import spaceraze.client.components.ComboBoxPanel;
import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRButton;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.SRTextField;
import spaceraze.client.components.scrollable.ListPanel;
import spaceraze.client.game.SpaceRazePanel;
import spaceraze.servlethelper.game.BuildingPureFunctions;
import spaceraze.servlethelper.game.DiplomacyPureFunctions;
import spaceraze.servlethelper.game.expenses.ExpensePureFunction;
import spaceraze.servlethelper.game.orders.OrderMutator;
import spaceraze.servlethelper.game.player.PlayerPureFunctions;
import spaceraze.servlethelper.game.troop.TroopPureFunctions;
import spaceraze.servlethelper.game.vip.VipPureFunctions;
import spaceraze.servlethelper.game.planet.PlanetOrderStatusMutator;
import spaceraze.servlethelper.game.planet.PlanetOrderStatusPureFunctions;
import spaceraze.servlethelper.game.planet.PlanetPureFunctions;
import spaceraze.servlethelper.game.player.CostPureFunctions;
import spaceraze.servlethelper.game.spaceship.SpaceshipPureFunctions;
import spaceraze.servlethelper.handlers.GameWorldHandler;
import spaceraze.util.general.Logger;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.*;
import spaceraze.world.diplomacy.DiplomacyLevel;
import spaceraze.world.enums.SpaceShipSize;
import spaceraze.world.enums.SpaceshipRange;
import spaceraze.world.orders.*;
import spaceraze.battlehandler.spacebattle.TaskForce;

public class MiniPlanetPanel extends SRBasePanel implements ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	// private int orbitalWharfCount[] = new int[4];
	private SRLabel maxBombLabel;
	private SRLabel maxProdLabel, maxProdLabel2, troopsuportcost1, troopsuportcost2, troopsuportcost3, troopsuportcost4;
	private CheckBoxPanel upgradepopcb, upgraderescb, attackIfNeutralCheckbox, destroyBuildingCheckbox;
	private CheckBoxPanel doNotBesiegeCheckbox, opencb, reconstructCheckbox, abandonCheckbox;
	private ComboBoxPanel maxBombardmentChoice;
	private SRButton updateMapButton;
	private Planet planet;
	private Player aPlayer;
	private SpaceRazePanel client;
	private int x = -195, y = -110;
	private SRLabel notesLabel;
	// private SRTextArea notesArea;
	private SRTextField notesTextfield;
	// private SRScrollPane scrollPane;
	private SRLabel defendersLabel;
	private SRButton toBattleSimButton;

	List<Building> allBuildings;
	ListPanel buildingList;
	SRLabel currentBuildBuildingLabel;

	List<VIP> allVIPs;
	ListPanel allVIPlist;
	SRLabel vipPanel;

	public MiniPlanetPanel(Player aPlayer, SpaceRazePanel srgclient, Planet chosenPlanet) {
		this.client = srgclient;
		this.aPlayer = aPlayer;
		this.planet = chosenPlanet;
		this.allBuildings = planet.getBuildings();
		this.allVIPs = VipPureFunctions.findAllVIPsOnPlanetOrShipsOrTroops(planet, aPlayer.getGalaxy());
		// this.setLayout(null);
		// setBackground(StyleGuide.colorBackground);

		// yours
		opencb = new CheckBoxPanel("Open");
		opencb.setBounds(200 + x, 111 + y, 200, 19);
		opencb.setSelected(false);
		opencb.addActionListener(this);
		opencb.setToolTipText("Open planets gives more incom but yours enemy will see that you are doing");
		add(opencb);
		opencb.setVisible(false);

		// if planet is razed
		reconstructCheckbox = new CheckBoxPanel("Reconstruct");
		reconstructCheckbox.setToolTipText("Make this planet possible to live on.");
		reconstructCheckbox.setBounds(200 + x, 111 + y, 200, 19);

		reconstructCheckbox.setSelected(false);
		reconstructCheckbox.addActionListener(this);
		reconstructCheckbox.setVisible(false);
		add(reconstructCheckbox);

		// yours
		upgradepopcb = new CheckBoxPanel("Upgrade production");
		upgradepopcb.setBounds(200 + x, 131 + y, 200, 19);
		upgradepopcb.setSelected(false);
		upgradepopcb.addActionListener(this);
		upgradepopcb.setToolTipText("Build out the planet an give more incom and ship support.");
		add(upgradepopcb);
		upgradepopcb.setVisible(false);

		// yours
		upgraderescb = new CheckBoxPanel("Upgrade resistance");
		upgraderescb.setBounds(200 + x, 151 + y, 200, 19);
		upgraderescb.setSelected(false);
		upgraderescb.addActionListener(this);
		upgraderescb.setToolTipText("Build out the planet resistance and possible to pay upkeep for troops.");
		add(upgraderescb);
		upgraderescb.setVisible(false);

		// not yours
		destroyBuildingCheckbox = new CheckBoxPanel("Destroy all orbital buildings");
		destroyBuildingCheckbox.setBounds(200 + x, 157 + y, 200, 20);
		destroyBuildingCheckbox.setSelected(false);
		destroyBuildingCheckbox.addActionListener(this);
		destroyBuildingCheckbox.setToolTipText("Destroy all buildings in orbit.");
		add(destroyBuildingCheckbox);
		destroyBuildingCheckbox.setVisible(false);

		// not yours
		attackIfNeutralCheckbox = new CheckBoxPanel("Attack if Neutral");
		attackIfNeutralCheckbox.setBounds(200 + x, 177 + y, 200, 20);
		attackIfNeutralCheckbox.setSelected(false);
		attackIfNeutralCheckbox.addActionListener(this);
		attackIfNeutralCheckbox.setToolTipText("To attack neutral plantes fleets.");
		add(attackIfNeutralCheckbox);
		attackIfNeutralCheckbox.setVisible(false);

		// not yours
		maxBombLabel = new SRLabel();
		maxBombLabel.setBounds(200 + x, 197 + y, 160, 20);
		if (aPlayer.getGalaxy().getGameWorld().isTroopGameWorld()) {
			maxBombLabel.setToolTipText("Sets max bombardment level (lower resistance, produktion and damage troops)");
		} else {
			maxBombLabel.setToolTipText("Sets max bombardment level (lower resistance and produktion)");
		}

		// maxBombLabel.setFont(new Font("Dialog",0,11));
		add(maxBombLabel);

		// not yours
		maxBombardmentChoice = new ComboBoxPanel();
		maxBombardmentChoice.setBounds(200 + x, 217 + y, 120, 20);
		maxBombardmentChoice.addItem("None");
		maxBombardmentChoice.addItem("1");
		maxBombardmentChoice.addItem("2");
		maxBombardmentChoice.addItem("3");
		maxBombardmentChoice.addItem("4");
		maxBombardmentChoice.addItem("5");
		maxBombardmentChoice.addItem("No limit");
		maxBombardmentChoice.addActionListener(this);
		this.add(maxBombardmentChoice);
		maxBombardmentChoice.setVisible(false);

		// not yours
		doNotBesiegeCheckbox = new CheckBoxPanel("Do not besiege");
		doNotBesiegeCheckbox.setBounds(200 + x, 241 + y, 200, 20);
		doNotBesiegeCheckbox.setSelected(false);
		doNotBesiegeCheckbox.addActionListener(this);
		doNotBesiegeCheckbox.setToolTipText("Useful if your fleet are passing through a friends planet.");
		add(doNotBesiegeCheckbox);
		doNotBesiegeCheckbox.setVisible(false);

		/*
		 * isRazedAndUninfected maxShipSizeLabel.setLocation(200+x,131+y);
		 * maxShipSizeChoice.setLocation(200+x,149+y);
		 * 
		 * maxProdLabel.setLocation(200+x,169+y);
		 * maxProdLabel2.setLocation(200+x,169+y);
		 * 
		 * else
		 * 
		 * maxShipSizeLabel.setLocation(200+x,115+y);
		 * maxShipSizeChoice.setLocation(200+x,133+y);
		 * 
		 * maxProdLabel.setLocation(200+x,261+y);
		 * maxProdLabel2.setLocation(320+x,261+y);
		 * 
		 */

		maxProdLabel = new SRLabel();
		maxProdLabel.setBounds(200 + x, 261 + y, 105, 20);
		add(maxProdLabel);

		maxProdLabel2 = new SRLabel();
		maxProdLabel2.setBounds(305 + x, 261 + y, 95, 20);
		add(maxProdLabel2);

		if (planet.isOpen() && planet.getPlayerInControl() == null) {
			String defenders = getNeutralSpaceshipsOnOpenPlanetString(aPlayer.getGalaxy());
			if (defenders != null) {
				defendersLabel = new SRLabel(defenders);
				defendersLabel.setBounds(200 + x, 180, 315, 20);
				defendersLabel.setToolTipText("Numbers and type of spaceship denfenders ");
				defendersLabel.setVisible(true);
				add(defendersLabel);

				toBattleSimButton = new SRButton("Simulate battle against defenders");
				toBattleSimButton.setBounds(200 + x, 200, 220, 20);
				toBattleSimButton.addActionListener(this);
				toBattleSimButton
						.setToolTipText("Maximum defenders against all yours ships that is on this planet next turn");
				toBattleSimButton.setVisible(true);
				add(toBattleSimButton);
			}
		}

		currentBuildBuildingLabel = new SRLabel("Visible buildings on planet:");
		currentBuildBuildingLabel.setBounds(5, 180, 250, 18);
		currentBuildBuildingLabel.setToolTipText("Mark a building to view more info");
		currentBuildBuildingLabel.setVisible(false);
		add(currentBuildBuildingLabel);

		buildingList = new ListPanel();
		buildingList.setBounds(5, 200, 315, 100);
		buildingList.setListSelectionListener(this);
		buildingList.setToolTipText("Mark a building to view more info");
		buildingList.setVisible(false);
		add(buildingList);

		vipPanel = new SRLabel("Visible VIPs on planet:");
		vipPanel.setBounds(5, 320, 250, 18);
		vipPanel.setToolTipText("Mark a VIP to view more info");
		vipPanel.setVisible(false);
		add(vipPanel);

		allVIPlist = new ListPanel();
		allVIPlist.setBounds(5, 340, 315, 100);
		allVIPlist.setListSelectionListener(this);
		allVIPlist.setToolTipText("Mark a VIP to view more info");
		allVIPlist.setVisible(false);
		add(allVIPlist);

		troopsuportcost1 = new SRLabel();
		troopsuportcost1.setBounds(5, 180, 300, 18);
		troopsuportcost1.setVisible(false);
		add(troopsuportcost1);

		troopsuportcost2 = new SRLabel();
		troopsuportcost2.setBounds(5, 200, 300, 18);
		troopsuportcost2.setVisible(false);
		add(troopsuportcost2);

		troopsuportcost3 = new SRLabel();
		troopsuportcost3.setBounds(5, 220, 300, 18);
		troopsuportcost3.setVisible(false);
		add(troopsuportcost3);

		troopsuportcost4 = new SRLabel();
		troopsuportcost4.setBounds(5, 240, 300, 18);
		troopsuportcost4.setVisible(false);
		add(troopsuportcost4);

		abandonCheckbox = new CheckBoxPanel("Abandon (will become neutral)");
		abandonCheckbox.setBounds(200 + x, 475, 200, 20);
		abandonCheckbox.setSelected(false);
		abandonCheckbox.addActionListener(this);
		add(abandonCheckbox);
		abandonCheckbox.setVisible(false);

		notesLabel = new SRLabel("Notes");
		notesLabel.setBounds(200 + x, 505, 100, 20);
		add(notesLabel);

		notesTextfield = new SRTextField();
		notesTextfield.setBounds(200 + x, 525, 210, 20);
		notesTextfield.addActionListener(this);
		add(notesTextfield);

		updateMapButton = new SRButton("Update Map");
		updateMapButton.setBounds(415 + x, 525, 100, 20);
		updateMapButton.setToolTipText("Save notes about the planet");
		updateMapButton.addActionListener(this);
		add(updateMapButton);

		showPlanet(chosenPlanet.getName());
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			if ((ae.getSource() instanceof SRButton) & (ae.getActionCommand().equalsIgnoreCase("ok"))) { // anv. har
																											// tryckt ok
																											// i
																											// confirmpopup
				aPlayer.addOrRemoveAbandonPlanet(planet);
				checkAbandon(true);
				client.updateTreasuryLabel();
			} else if ((ae.getSource() instanceof SRButton) & (ae.getActionCommand().equalsIgnoreCase("cancel"))) { // anv.
																													// har
																													// tryckt
																													// cancel
																													// i
																													// confirmpopup
				abandonCheckbox.setSelected(false);
			} else if (ae.getSource() instanceof CheckBoxPanel) {
				newOrder((CheckBoxPanel) ae.getSource());
			} else if (ae.getSource() instanceof ComboBoxPanel) {
				newOrder((ComboBoxPanel) ae.getSource());
			} else if (ae.getSource() == updateMapButton) {
				saveNotesUpdateMap(true);
			} else if (ae.getSource() == toBattleSimButton) {
				client.addToBattleSim(getNeutralSpaceshipsOnOpenPlanetToBattleSim(aPlayer.getGalaxy()), "B");
				client.addToBattleSim(getShipsAsBattleSimString(), "A");
				client.showBattleSim();
			} else if (ae.getSource() instanceof SRTextField) {
				saveNotesUpdateMap(true);
			}
		} catch (NumberFormatException e) {
			System.out.println(e.toString());
		}
	}

	private String getShipsAsBattleSimString() {
		StringBuffer sb = new StringBuffer();
		// List<Spaceship> allShips = spaceships;
		List<Spaceship> selectedShips = SpaceshipPureFunctions.getShipAtPlanetNextTurn(aPlayer, planet);
		boolean semicolon = false;
		for (Spaceship aShip : selectedShips) {
			if (!SpaceshipPureFunctions.getSpaceshipTypeByUuid(aShip.getTypeUuid(), aPlayer.getGalaxy().getGameWorld()).isCivilian()) {
				if (semicolon) {
					sb.append(";");
				}
				sb.append(SpaceshipPureFunctions.getSpaceshipTypeByUuid(aShip.getTypeUuid(), aPlayer.getGalaxy().getGameWorld()).getName());
				String abilities = MiniShipPanel.getBattleSimAbilities(aShip);
				// append () if needed
				String vips = SpaceshipPureFunctions.getAllBattleSimVipsOnShip(aShip, aPlayer.getGalaxy().getAllVIPs(), aPlayer.getGalaxy().getGameWorld());
				if (!vips.equals("")) {
					if (!abilities.equals("")) {
						abilities += ",";
					}
					abilities += vips;
				}
				if (!abilities.equals("")) {
					sb.append("(");
					sb.append(abilities);
					sb.append(")");
				}
				semicolon = true;
			}
		}
		return sb.toString();
	}

	public void valueChanged(ListSelectionEvent lse) {
		// System.out.println("valueChanged" + lse.toString() +
		// lse.getValueIsAdjusting());
		try {
			if (lse.getSource() instanceof ListPanel) {
				if ((ListPanel) lse.getSource() == buildingList) {
					if (lse.getValueIsAdjusting()) {
						if (planet.getPlayerInControl() != null) {
							client.showBuildingTypeDetails(buildingList.getSelectedItem(),
									GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getName());
						} else {

							// TODO (???) fixa när vi kan lista alla byggnader eller har info om vem som har
							// byggt byggnaden
						}

					}
				} else if ((ListPanel) lse.getSource() == allVIPlist) {
					if (lse.getValueIsAdjusting()) {
						client.showVIPTypeDetails(allVIPlist.getSelectedItem(), "All");
					}
				}
			}
		} catch (NumberFormatException nfe) {
		}
	}

	/**
	 * Return battle sim string on max neutral defender on open planet.
	 * @param aGalaxy
	 * @return
	 */
	public String getNeutralSpaceshipsOnOpenPlanetToBattleSim(Galaxy aGalaxy){
		SpaceshipType aType =  null;
		String shipListString = null;
		if(planet.isHasNeverSurrendered() && planet.getPlayerInControl() == null && planet.isOpen()){
			List<Spaceship> tempList = aGalaxy.getSpaceships();
			for (int i = 0; i < tempList.size(); i++){
				Spaceship tempss = tempList.get(i);
				if ((tempss.getOwner() == null) & (tempss.getLocation() == planet)){
					aType = SpaceshipPureFunctions.getSpaceshipTypeByUuid(tempss.getTypeUuid(), aGalaxy.getGameWorld());
					break;
				}
			}

			if(aType != null){
				if(aType.getSize() == SpaceShipSize.SMALL){
					if(planet.getPopulation() < 3){
						shipListString ="[2]" + aType.getName();
					}else{
						shipListString ="[4]" + aType.getName();
					}
				}else if(aType.getSize() == SpaceShipSize.MEDIUM){
					if(planet.getPopulation() < 5){
						shipListString ="[2]" + aType.getName();
					}else{
						shipListString ="[4]" + aType.getName();
					}
				}else{
					shipListString ="[3]" + aType.getName();
				}

			}

		}
		return shipListString;
	}

	/**
	 * Return a string on possible neutral defender on open planet.
	 * @param aGalaxy
	 * @return
	 */
	public String getNeutralSpaceshipsOnOpenPlanetString(Galaxy aGalaxy){
		SpaceshipType aType =  null;
		String shipListString = null;

		if(planet.isHasNeverSurrendered() && planet.getPlayerInControl() == null && planet.isOpen()){
			List<Spaceship> tempList = aGalaxy.getSpaceships();
			for (int i = 0; i < tempList.size(); i++){
				Spaceship tempss = tempList.get(i);
				if ((tempss.getOwner() == null) & (tempss.getLocation() == planet)){
					aType = SpaceshipPureFunctions.getSpaceshipTypeByUuid(tempss.getTypeUuid(), aGalaxy.getGameWorld());
					break;
				}
			}
			if (aType != null){
				Logger.fine("### aType.getName() ### " + aType.getName());
			}else{
				Logger.fine("### aType.getName() ### No ships on planet");
			}
			if(aType != null){
				if(aType.getSize() == SpaceShipSize.SMALL){
					if(planet.getPopulation() < 3){
						shipListString ="1 - 2 " + aType.getName() + "s in defence";
					}else{
						shipListString ="2 - 4 " + aType.getName()+ "s in defence";
					}
				}else if(aType.getSize() == SpaceShipSize.MEDIUM){
					if(planet.getPopulation() < 5){
						shipListString ="1 - 2 " + aType.getName()+ "s in defence";
					}else{
						shipListString ="2 - 4 " + aType.getName()+ "s in defence";
					}
				}else{
					shipListString ="1 - 3 " + aType.getName()+ "s in defence";
				}

			}

		}
		return shipListString;
	}

	public void saveNotesUpdateMap(boolean updateMap) {
		String textfieldText = notesTextfield.getText();
		String oldNotesText = PlanetPureFunctions.findPlanetInfo(planet.getName(), aPlayer.getPlanetInformations()).getNotes();
		PlanetNotesChange aPlanetNotesChange = aPlayer.getOrders().getPlanetNotesChange(planet);
		String newOrderText = null;
		if (aPlanetNotesChange != null) {
			newOrderText = aPlanetNotesChange.getNotesText();
		}
		if (oldNotesText == null) {
			oldNotesText = "";
		}
		Logger.finest(
				"update notes; tftext: " + textfieldText + ", old: " + oldNotesText + ", ordertext:" + newOrderText);
		// if there is an change order
		if (aPlanetNotesChange != null) {
			// if tftext is same as old text
			if (textfieldText.equals(oldNotesText)) {
				// remove order
				aPlayer.getOrders().removePlanetNotesChange(planet);
			} else {
				// if tftext is different from ordertext
				if (!textfieldText.equals(newOrderText)) {
					// change order
					aPlayer.getOrders().addPlanetNotesChange(planet, textfieldText);
				}
			}
		} else { // else, there is no change order
					// check if tftext is different from oldtext
			if (!textfieldText.equals(oldNotesText)) {
				// create new order
				aPlayer.getOrders().addPlanetNotesChange(planet, textfieldText);
			}
		}
		if (updateMap) {
			client.updateMap();
		}
	}

	// ev uppdatera spelarens Orders
	private void newOrder(CheckBoxPanel cb) {
		if (cb == opencb) {
			aPlayer.addOrRemovePlanetVisib(planet);
			client.updateTreasuryLabel();
		} else if (cb == upgradepopcb) {
			if (cb.isSelected()) { // lägg till order
				aPlayer.addIncPop(planet);
			} else { // false = ta bort order
				aPlayer.removeIncPop(planet);
			}
			// uppdatera "left to spend" och send-knappen
			client.updateTreasuryLabel();
		} else if (cb == upgraderescb) { // = upgraderescb
			if (cb.isSelected()) { // lägg till order
				aPlayer.addIncRes(planet);
			} else { // false = ta bort order
				aPlayer.removeIncRes(planet);
			}
			// uppdatera "left to spend" och send-knappen
			client.updateTreasuryLabel();
		} else if (cb == attackIfNeutralCheckbox) {
			// aPlayer.getPlanetInfos().setAttackIfNeutral(cb.isSelected(),p.getName());
			PlanetOrderStatusMutator.setAttackIfNeutral(cb.isSelected(), planet.getName(), aPlayer.getPlanetOrderStatuses());
			if (!cb.isSelected() && planet.getPlayerInControl() == null) {
				aPlayer.getOrders().removeAllGroundAttacksAgainstPlanet(planet, aPlayer);
			}
		} else if (cb == abandonCheckbox) {
			abandonPressed();
			// aPlayer.addOrRemoveAbandonPlanet(p);
			// checkAbandon(cb.isSelected());
			// client.updateTreasuryLabel();
		} else if (cb == reconstructCheckbox) {
			if (cb.isSelected()) { // l�gg till order
				aPlayer.addReconstruct(planet);
			} else { // false = ta bort order
				aPlayer.removeReconstruct(planet);
			}
			client.updateTreasuryLabel();
		} else // doNotBesiegeCheckbox
		if (cb == doNotBesiegeCheckbox) {
			// aPlayer.getPlanetInfos().setDoNotBesiege(cb.isSelected(),p.getName());
			PlanetOrderStatusMutator.setDoNotBesiege(cb.isSelected(), planet.getName(), aPlayer.getPlanetOrderStatuses());
			if (cb.isSelected() && planet.getPlayerInControl() != null) {
				aPlayer.getOrders().removeAllGroundAttacksAgainstPlanet(planet, aPlayer);
			}
		} else { // destroyWharfsCheckbox
			// aPlayer.getPlanetInfos().setOrbitalBuildings(cb.isSelected(),p.getName());
			PlanetOrderStatusMutator.setDestroyOrbitalBuildings(cb.isSelected(), planet.getName(), aPlayer.getPlanetOrderStatuses());
		}
	}

	private void abandonPressed() {
		if (abandonCheckbox.isSelected()) {
			Logger.fine("Planet name: " + planet.getName());
			List<String> messages = new LinkedList<String>();
			List<Troop> troopsOnPlanet = aPlayer.getGalaxy().findTroopsOnPlanet(planet, aPlayer);
			if (troopsOnPlanet.size() > 0) {
				messages.add("Planets can't be abandoned while there are troops on the planet");
			}
			VIP gov = findVIPGovenor(planet, aPlayer, aPlayer.getGalaxy());
			if (gov != null) {
				messages.add("Planets can't be abandoned while your Governor are on the planet");
			}
			List<Spaceship> shipsOnPlanet = findPlayersSpaceshipsOnPlanet(aPlayer, planet,
					SpaceshipRange.NONE, aPlayer.getGalaxy());
			// check if all in shipsOnPlanet are squadrons in carriers
			boolean allIsSquadronsInCarrier = true;
			for (Spaceship aShipOnPlanet : shipsOnPlanet) {
				if (aShipOnPlanet.getSize() == SpaceShipSize.SQUADRON) {
					if (aShipOnPlanet.getCarrierLocation() == null) {
						allIsSquadronsInCarrier = false;
					} else {
						Player owner = aShipOnPlanet.getOwner();
						if (owner != null) { // check for move orders to planet
							String destName = MiniShipPanel.getShipDestinationName(aShipOnPlanet, owner.getGalaxy(), owner.getOrders());
							if (destName.equals(planet.getName())) {
								allIsSquadronsInCarrier = false;
							}
						}
					}
				} else { // is not a squadron
					allIsSquadronsInCarrier = false;
				}
			}
			if ((shipsOnPlanet.size() > 0) & !allIsSquadronsInCarrier) {
				messages.add("Planets can't be abandoned while you have immobile ships at the planet");
			}
			// kan ej göra abandon om det finns vippar på planeten som ej kan vara på
			// neutrala planeter
			List<VIP> vipsOnPlanet = VipPureFunctions.findPlayersVIPsOnPlanet(planet, aPlayer, aPlayer.getGalaxy());
			for (VIP aVip : vipsOnPlanet) {
				VIPType vipType = VipPureFunctions.getVipTypeByUuid(aVip.getTypeUuid(), aPlayer.getGalaxy().getGameWorld());
				if ((!vipType.isCanVisitEnemyPlanets() && !vipType.isCanVisitNeutralPlanets()) || vipType.isGovernor()) {
					messages.add("Can't abandon " + planet.getName() + " while " + vipType.getName() + " is on the planet");
				}
			}
			// får ej finnas flyttorder för vippar som ej kan vara på neutrala
			List<VIPMovement> vipMoves = VipPureFunctions.getVIPMoves(planet, aPlayer.getOrders());
			if (vipMoves.size() > 0) {
				for (VIPMovement aVIPMovement : vipMoves) {
					VIP aVIP = VipPureFunctions.findVIP(aVIPMovement.getVipKey(), aPlayer.getGalaxy());
					VIPType vipType = VipPureFunctions.getVipTypeByUuid(aVIP.getTypeUuid(), aPlayer.getGalaxy().getGameWorld());
					if ((!vipType.isCanVisitEnemyPlanets() && !vipType.isCanVisitNeutralPlanets()) || vipType.isGovernor()) {
						messages.add("Can't abandon " + planet.getName() + " while " + vipType.getName()
								+ " has a move order to the planet");
					}
				}
			}
			// får ej finnas moveorder på trupper till planeten
			List<TroopToPlanetMovement> troopMoves = getTroopToPlanetMoves(planet, aPlayer.getOrders());
			if (troopMoves.size() > 0) {
				for (TroopToPlanetMovement aTroopToPlanetMovement : troopMoves) {
					Troop aTroop = TroopPureFunctions.findTroop(aTroopToPlanetMovement.getTroopKey(),  aPlayer.getGalaxy());
					messages.add("Can't abandon " + planet.getName() + " while " + aTroop.getName()
							+ " has a move order to the planet");
				}
			}
			// TODO (Paul) kan ej flytta (dvs skapa ny flyttorder) vippar till planet som ej
			// kan vara på neutrala planeter
			if (messages.size() > 0) { // visa meddelandepopup varför planeten inte kan averges
				String[] messagesArray = new String[messages.size()];
				int i = 0;
				for (String message : messages) {
					messagesArray[i] = message;
					i++;
				}
				GeneralMessagePopupPanel mPopup = new GeneralMessagePopupPanel("Can't abandon planet", null, 450,
						messagesArray);
				mPopup.open(this);
				abandonCheckbox.setSelected(false);
			} else { // fråga om spelaren verkligen vill göra abandon planet
				GeneralConfirmPopupPanel cPopup = new GeneralConfirmPopupPanel("Abandon this planet?", this,
						"Do you really want to abandon this planet?");
				cPopup.setPopupSize(350, 110);
				cPopup.open(this);
			}
		} else {
			aPlayer.addOrRemoveAbandonPlanet(planet);
			checkAbandon(false);
			client.updateTreasuryLabel();
		}
	}

	private VIP findVIPGovenor(Planet aPlanet, Player aPlayer, Galaxy galaxy) {
		VIP foundVIP = null;
		int i = 0;
		while ((foundVIP == null) & (i < galaxy.getAllVIPs().size())) {
			VIP tempVIP = galaxy.getAllVIPs().get(i);
			if ((VipPureFunctions.getVipTypeByUuid(tempVIP.getTypeUuid(), galaxy.getGameWorld()).isGovernor()) & (tempVIP.getBoss() == aPlayer) & (tempVIP.getPlanetLocation() == aPlanet)) {
				foundVIP = tempVIP;
			} else {
				i++;
			}
		}
		return foundVIP;
	}

	public static Planet getDestination(Spaceship tempss, Galaxy aGalaxy, Orders orders) {
		ShipMovement shipMovement = orders.getShipMoves().stream()
				.filter(movement -> tempss.getUuid().equalsIgnoreCase(movement.getSpaceshipKey())).findAny().orElse(null);

		return shipMovement == null ? null : PlanetPureFunctions.getPlanet(shipMovement.getPlanetName(), aGalaxy);
	}


	// count how many troops are ordered to move to a planet
	public List<TroopToPlanetMovement> getTroopToPlanetMoves(Planet aPlanet, Orders orders) {
		List<TroopToPlanetMovement> troopMoves = new LinkedList<TroopToPlanetMovement>();
		for (TroopToPlanetMovement aTroopToPlanetMove : orders.getTroopToPlanetMoves()) {
			if (aPlanet.getName().equalsIgnoreCase(aTroopToPlanetMove.getPlanetName())) {
				troopMoves.add(aTroopToPlanetMove);
			}
		}
		return troopMoves;
	}

	private List<Spaceship> findPlayersSpaceshipsOnPlanet(Player aPlayer, Planet aPlanet, SpaceshipRange range, Galaxy galaxy) {
		List<Spaceship> retShips = new LinkedList<Spaceship>();
		List<Spaceship> playersss = SpaceshipPureFunctions.getPlayersSpaceshipsOnPlanet(aPlayer, aPlanet, galaxy.getSpaceships());
		for (Spaceship aSpaceship : playersss) {
			if (SpaceshipPureFunctions.getRange(aSpaceship, galaxy) == range) {
				retShips.add(aSpaceship);
			}
		}
		return retShips;
	}

	// uppdatera spelarens PlanetInfo om choices har använts
	private void newOrder(ComboBoxPanel c) {

		if (c == maxBombardmentChoice) {
			int maxBombTemp = maxBombardmentChoice.getSelectedIndex();
			if (maxBombTemp == 6) {
				maxBombTemp = 9999;
			}
			PlanetOrderStatusMutator.setMaxBombardment(maxBombTemp, planet.getName(), aPlayer.getPlanetOrderStatuses());
		}
	}

	private void showPlanet(String name) {
		hideAll();
		String nameOnly = name;
		if (name.indexOf("(") != -1) {
			nameOnly = name.substring(0, name.indexOf("(") - 1);
		}
		Galaxy g = aPlayer.getGalaxy();
		planet = g.findPlanet(nameOnly);
		if (planet != null) { // det finns en planet med namnet "name"
			PlanetNotesChange aPlanetNotesChange = aPlayer.getOrders().getPlanetNotesChange(planet);
			if (aPlanetNotesChange != null) {
				notesTextfield.setText(aPlanetNotesChange.getNotesText());
			} else {
				notesTextfield.setText(PlanetPureFunctions.findPlanetInfo(planet.getName(), aPlayer.getPlanetInformations()).getNotes());
			}
			boolean spy = VipPureFunctions.findVIPSpy(planet, aPlayer, g) != null;
			// visa alltid vem som kontrollerar planeten eller vem som senast n�r planeten
			// var öppen kontrollerade planeten
			if (PlanetPureFunctions.isRazedAndUninfected(planet)) { // if planet is infestated with aliens it cannot be reconstructed
				// maybe show reconstruct checkbox
				if (aPlayer.isCanReconstruct()) {
					reconstructCheckbox.setText("Reconstruct (cost: " + aPlayer.getReconstructCost(planet) + ")");
					if (PlayerPureFunctions.playerHasShipsInSystem(aPlayer, planet, aPlayer.getGalaxy()) || spy) {
						reconstructCheckbox.setVisible(true);
						reconstructCheckbox.setSelected(false);
						// is the players ships alone at the planet?
						List<TaskForce> taskforces = TaskForceHandler.getTaskForces(planet, true, g);
						// System.out.println("taskforces.size(): " + taskforces.size());
						if ((taskforces.size() == 1) && PlayerPureFunctions.playerHasShipsInSystem(aPlayer, planet, aPlayer.getGalaxy())) {
							// should cb be enabled? Only if at least one ship has troops.
							TaskForce playerTaskforce = TaskForceHandler.getTaskForce(aPlayer, planet, true, g);
							if (playerTaskforce.getMaxPsychWarfare(g.getGameWorld()) > 0 || playerTaskforce.getTroopCapacity() > 0) {
								reconstructCheckbox.setEnabled(true);
								// fetch value (already reconstructing?)
								Orders o = aPlayer.getOrders();
								reconstructCheckbox.setSelected(ExpensePureFunction.alreadyReconstructing(o, planet));
							} else { // without troops = cannot reconstruct
								reconstructCheckbox.setEnabled(false);
							}
						} else { // other fleets are present, cannot reconstruct unless player are alone with
									// troops on planet
							if (playerAloneHaveTroopsOnPlanet(g)) {
								reconstructCheckbox.setEnabled(true);
							} else {
								reconstructCheckbox.setEnabled(false);
							}
						}
					} else {
						reconstructCheckbox.setVisible(false);
					}
				} else {
					reconstructCheckbox.setVisible(false);
				}
				maxProdLabel.setLocation(200 + x, 169 + y);
				maxProdLabel2.setLocation(200 + x, 169 + y);
			}
			if (planet.getPlayerInControl() == aPlayer) {
				// show own planet CheckBoxes
				showCheckBoxes(planet);

				if (TroopPureFunctions.getTroopsOnPlanet(planet, aPlayer, aPlayer.getGalaxy().getTroops()).size() > 0) {
					int cost = CostPureFunctions.getTroopsCostPlanet(aPlayer, planet, aPlayer.getGalaxy().getTroops());
					troopsuportcost1.setText("Total troops cost: " + cost);
					troopsuportcost1.setVisible(true);
					troopsuportcost1.setToolTipText("Troops supplies by current planets");
					troopsuportcost2.setText("Planet troop supply capacity. " + planet.getResistance());
					troopsuportcost2.setToolTipText("The planets troops supply are same as resistance");
					troopsuportcost2.setVisible(true);
					int upkeep = CostPureFunctions.getTroopsUpKeepPlanet(aPlayer, planet, aPlayer.getGalaxy().getTroops());
					if (upkeep > 0) {
						troopsuportcost3.setText("The planet can't supply the troops and");
						troopsuportcost4.setText("your state founds will decrease by " + upkeep);
						troopsuportcost3.setVisible(true);
						troopsuportcost4.setVisible(true);
					} else {
						troopsuportcost3.setText("All yours troops are supplied by the planet");
						troopsuportcost3.setVisible(true);
					}

				}

			} else {
				if (planet.isOpen() || PlayerPureFunctions.playerHasShipsInSystem(aPlayer, planet, aPlayer.getGalaxy()) || spy) {
					boolean surveyShip = SpaceshipPureFunctions.findSurveyShip(planet, aPlayer, g.getSpaceships(), g.getGameWorld()) != null;
					boolean surveyVIP = VipPureFunctions.findSurveyVIPonShip(planet, aPlayer, aPlayer.getGalaxy()) != null;
					if (planet.isOpen() | surveyShip | surveyVIP | spy) {
						maxProdLabel.setVisible(true);
						maxProdLabel2.setVisible(true);
						maxProdLabel.setText("Max. production: ");
						maxProdLabel2.setText(String.valueOf(getMaxPopulation(planet)));
					}
					if (planet.getPlayerInControl() != null) {
						// kolla om spelaren ej är allierad
						// if (aPlayer.getFaction() != p.getPlayerInControl().getFaction()){
						if (!DiplomacyPureFunctions.isDiplomacyLevel(g, aPlayer, planet.getPlayerInControl(),
								DiplomacyLevel.CONFEDERACY, DiplomacyLevel.LORD)) {
							// visa destroyWharfs
							// attackIfNeutralCheckbox.setSelected(aPlayer.getPlanetInfos().getAttackIfNeutral(p.getName()));
							attackIfNeutralCheckbox
									.setSelected(PlanetOrderStatusPureFunctions.isAttackIfNeutral(planet.getName(), aPlayer.getPlanetOrderStatuses()));
							attackIfNeutralCheckbox.setVisible(true);
							// destroyBuildingCheckbox.setSelected(aPlayer.getPlanetInfos().getOrbitalBuildings(p.getName()));
							destroyBuildingCheckbox.setSelected(
									PlanetOrderStatusPureFunctions.isDestroyOrbitalBuildings(planet.getName(), aPlayer.getPlanetOrderStatuses()));
							destroyBuildingCheckbox.setVisible(true);
							// doNotBesiegeCheckbox.setSelected(aPlayer.getPlanetInfos().getDoNotBesiege(p.getName()));
							doNotBesiegeCheckbox
									.setSelected(PlanetOrderStatusPureFunctions.isDoNotBesiege(planet.getName(), aPlayer.getPlanetOrderStatuses()));
							doNotBesiegeCheckbox.setVisible(true);
							showMaxBombChoice();
						}
					} else {
						if (!PlanetPureFunctions.isRazedAndUninfected(planet)) { // only show if not razed
							// visa attackIfNeutral & destroyWharfs
							// attackIfNeutralCheckbox.setSelected(aPlayer.getPlanetInfos().getAttackIfNeutral(p.getName()));
							attackIfNeutralCheckbox
									.setSelected(PlanetOrderStatusPureFunctions.isAttackIfNeutral(planet.getName(), aPlayer.getPlanetOrderStatuses()));
							attackIfNeutralCheckbox.setVisible(true);
							// destroyBuildingCheckbox.setSelected(aPlayer.getPlanetInfos().getOrbitalBuildings(p.getName()));
							destroyBuildingCheckbox.setSelected(
									PlanetOrderStatusPureFunctions.isDestroyOrbitalBuildings(planet.getName(), aPlayer.getPlanetOrderStatuses()));
							destroyBuildingCheckbox.setVisible(true);
							// doNotBesiegeCheckbox.setSelected(aPlayer.getPlanetInfos().getDoNotBesiege(p.getName()));
							doNotBesiegeCheckbox
									.setSelected(PlanetOrderStatusPureFunctions.isDoNotBesiege(planet.getName(), aPlayer.getPlanetOrderStatuses()));
							doNotBesiegeCheckbox.setVisible(true);
							showMaxBombChoice();
						}
					}

					// Player is besieged the planet. Show buildings in orbit and buildings on
					// ground if player have troops on the planet (ongoing battle)
					if (aPlayer != planet.getPlayerInControl() && planet.isBesieged()
							&& aPlayer.getGalaxy().isPlayerShipAtPlanet(aPlayer, planet)) {
						List<Building> buildings = new ArrayList<Building>();
						if (aPlayer.getGalaxy().isOngoingGroundBattle(planet, aPlayer)) {
							if (planet.getBuildings().size() > 0) {
								buildings = planet.getBuildings();
							}
						} else {
							buildings = PlanetPureFunctions.getBuildings(planet, true, g.getGameWorld());
						}

						if (buildings.size() > 0) {
							if (planet.getPlayerInControl() != null) {
								currentBuildBuildingLabel.setForeground(ColorConverter.getColorFromHexString(
										GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getPlanetHexColor()));
								buildingList.setForeground(ColorConverter.getColorFromHexString(
										GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getPlanetHexColor()));
							} else {
								currentBuildBuildingLabel.setForeground(StyleGuide.colorNeutralWhite);
								buildingList.setForeground(StyleGuide.colorNeutralWhite);
							}

							DefaultListModel dlm = (DefaultListModel) buildingList.getModel();
							// TODO (Tobbe) hur löser vi detta? sortering av ArrayList
							// Collections.sort(allBuildings);
							dlm.removeAllElements();
							for (int i = 0; i < buildings.size(); i++) {
								dlm.addElement(BuildingPureFunctions.getBuildingTypeByUuid(buildings.get(i).getTypeUuid(), g.getGameWorld()).getName());
							}
							buildingList.updateScrollList();

							currentBuildBuildingLabel.setVisible(true);
							buildingList.setVisible(true);
						}
					}
				} else {
					// visa attackIfNeutral & destroyWharfs
					attackIfNeutralCheckbox
							.setSelected(PlanetOrderStatusPureFunctions.isAttackIfNeutral(planet.getName(), aPlayer.getPlanetOrderStatuses()));
					attackIfNeutralCheckbox.setVisible(true);
					destroyBuildingCheckbox
							.setSelected(PlanetOrderStatusPureFunctions.isDestroyOrbitalBuildings(planet.getName(), aPlayer.getPlanetOrderStatuses()));
					destroyBuildingCheckbox.setVisible(true);
					doNotBesiegeCheckbox.setSelected(PlanetOrderStatusPureFunctions.isDoNotBesiege(planet.getName(), aPlayer.getPlanetOrderStatuses()));
					doNotBesiegeCheckbox.setVisible(true);
					showMaxBombChoice();
				}

				maxProdLabel.setLocation(200 + x, 261 + y);
				maxProdLabel2.setLocation(320 + x, 261 + y);

			}

			// om det är en annans spelare planet, visa visiLabel istället för opencb, eller
			// razed
			if (planet.getPlayerInControl() != aPlayer) {
				opencb.setVisible(false);

				maxProdLabel.setLocation(200 + x, 261 + y);
				maxProdLabel2.setLocation(320 + x, 261 + y);
			}

			// detta kan bli problem om planeten bli neutral. vilken faction är då ägare
			// till byggnaden. Blir fellänkat.
			if (aPlayer != planet.getPlayerInControl() && (((planet.isOpen() && !planet.isBesieged()) || (spy)
					|| SpaceshipPureFunctions.findSurveyShip(planet, aPlayer, g.getSpaceships(), g.getGameWorld()) != null)
					|| VipPureFunctions.findSurveyVIPonShip(planet, aPlayer, aPlayer.getGalaxy()) != null)) {
				if (allBuildings.size() > 0) {
					List<Building> visibleBuildings = new ArrayList<Building>();
					for (int i = 0; i < allBuildings.size(); i++) {
						if (allBuildings.get(i).isVisibleOnMap()) {
							visibleBuildings.add(allBuildings.get(i));
						}
					}
					if (visibleBuildings.size() > 0) {

						if (planet.getPlayerInControl() != null) {
							currentBuildBuildingLabel.setForeground(ColorConverter
									.getColorFromHexString(GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getPlanetHexColor()));
							buildingList.setForeground(ColorConverter
									.getColorFromHexString(GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getPlanetHexColor()));
							currentBuildBuildingLabel.setBounds(5, 180, 250, 18);
							buildingList.setBounds(5, 200, 315, 100);
						} else {
							currentBuildBuildingLabel.setForeground(StyleGuide.colorNeutralWhite);
							buildingList.setForeground(StyleGuide.colorNeutralWhite);
							currentBuildBuildingLabel.setBounds(5, 320, 250, 18);
							buildingList.setBounds(5, 340, 315, 100);
						}

						DefaultListModel dlm = (DefaultListModel) buildingList.getModel();
						dlm.removeAllElements();
						// TODO (Tobbe) hur löser vi detta? sortering av ArrayList
						// Collections.sort(allBuildings);
						for (int i = 0; i < visibleBuildings.size(); i++) {
							dlm.addElement(BuildingPureFunctions.getBuildingTypeByUuid(visibleBuildings.get(i).getTypeUuid(), g.getGameWorld()).getName());
						}
						buildingList.updateScrollList();

						currentBuildBuildingLabel.setVisible(true);
						buildingList.setVisible(true);
					}
				}

				if (allVIPs.size() > 0) {
					List<VIP> visibleVIPs = new ArrayList<VIP>();
					for (int i = 0; i < allVIPs.size(); i++) {
						if (VipPureFunctions.getVipTypeByUuid(allVIPs.get(i).getTypeUuid(), g.getGameWorld()).getShowOnOpenPlanet()
								&& allVIPs.get(i).getBoss() == planet.getPlayerInControl()) {
							visibleVIPs.add(allVIPs.get(i));
						}
					}

					if (visibleVIPs.size() > 0) {

						if (planet.getPlayerInControl() != null) {
							vipPanel.setForeground(ColorConverter
									.getColorFromHexString(GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getPlanetHexColor()));
							allVIPlist.setForeground(ColorConverter
									.getColorFromHexString(GameWorldHandler.getFactionByUuid(planet.getPlayerInControl().getFactionUuid(), planet.getPlayerInControl().getGalaxy().getGameWorld()).getPlanetHexColor()));
						} else {
							vipPanel.setForeground(StyleGuide.colorNeutralWhite);
							allVIPlist.setForeground(StyleGuide.colorNeutralWhite);
						}

						DefaultListModel dlm = (DefaultListModel) allVIPlist.getModel();
						for (int i = 0; i < visibleVIPs.size(); i++) {
							VIP aVIP = visibleVIPs.get(i);
							dlm.addElement(VipPureFunctions.getVipTypeByUuid(aVIP.getTypeUuid(), aPlayer.getGalaxy().getGameWorld()).getName());
						}
						allVIPlist.updateScrollList();

						vipPanel.setVisible(true);
						allVIPlist.setVisible(true);

					}
				}
			}

			if (aPlayer.isDefeated() | aPlayer.getGalaxy().isGameOver()) {
				upgradepopcb.setVisible(false);
				upgraderescb.setVisible(false);
				attackIfNeutralCheckbox.setVisible(false);
				destroyBuildingCheckbox.setVisible(false);
				doNotBesiegeCheckbox.setVisible(false);
				abandonCheckbox.setVisible(false);
				opencb.setVisible(false);
				maxBombardmentChoice.setVisible(false);
				maxBombLabel.setVisible(false);
				maxProdLabel.setVisible(false);
				maxProdLabel2.setVisible(false);
				reconstructCheckbox.setVisible(false);
			}
		}
	}

	private boolean playerAloneHaveTroopsOnPlanet(Galaxy g) {
		List<Troop> allTroopsOnPlanet = TroopPureFunctions.findAllTroopsOnPlanet(g.getTroops(), planet);
		boolean playerHaveTroopOnPlanet = false;
		boolean otherPlayerHaveTroopsOnPlanet = false;
		for (Troop aTroop : allTroopsOnPlanet) {
			if (aTroop.getOwner() == aPlayer) {
				playerHaveTroopOnPlanet = true;
			} else {
				otherPlayerHaveTroopsOnPlanet = true;
			}
		}
		return playerHaveTroopOnPlanet & !otherPlayerHaveTroopsOnPlanet;
	}

	private int getMaxPopulation(Planet planet){
		return planet.getBasePopulation() *2;
	}

	private void checkAbandon(boolean state) {
		if (state) { // abandon is checked
			if (planet.getPopulation() < getMaxPopulation(planet)) {
				upgradepopcb.setEnabled(false);
				if (upgradepopcb.isSelected()) {
					upgradepopcb.setSelected(false);
					aPlayer.removeIncPop(planet);
				}
			}
			upgraderescb.setEnabled(false);
			if (upgraderescb.isSelected()) {
				upgraderescb.setSelected(false);
				aPlayer.removeIncRes(planet);
			}
			opencb.setEnabled(false);
			if (opencb.isSelected()) {
				if (aPlayer.getOrders().getPlanetVisibility(planet)) {
					aPlayer.addOrRemovePlanetVisib(planet);
					opencb.setSelected(!opencb.isSelected());
				}
			}
			// ta bort ev. byggorder, både nya byggnadr, ships, troops & vips
			aPlayer.removeNewBuilding(planet);
			for (Building aBuilding : planet.getBuildings()) {
				if (aBuilding.getWharfSize() > 0) {
					OrderMutator.removeAllBuildShip(aBuilding, aPlayer.getOrders());
				} else if (BuildingPureFunctions.getBuildingTypeByUuid(aBuilding.getTypeUuid(), aPlayer.getGalaxy().getGameWorld()).getTypeOfTroop().size() > 0) {
					OrderMutator.removeAllBuildTroop(aBuilding, aPlayer.getOrders());
				} else if (BuildingPureFunctions.getBuildingTypeByUuid(aBuilding.getTypeUuid(), aPlayer.getGalaxy().getGameWorld()).getVipTypes().size() > 0) {
					OrderMutator.removeBuildVIP(aBuilding, aPlayer.getOrders());
				}
			}
		} else { // abandon is not checked
			if (planet.getPopulation() < getMaxPopulation(planet)) {
				upgradepopcb.setEnabled(true);
			}
			upgraderescb.setEnabled(true);
			opencb.setEnabled(true);
		}
	}

	private void showMaxBombChoice() {
		if (!GameWorldHandler.getFactionByUuid(aPlayer.getFactionUuid(), aPlayer.getGalaxy().getGameWorld()).isAlien()) {
			maxBombLabel.setText("Max bombardement:");
			maxBombLabel.setVisible(true);
			// int maxBombTemp = aPlayer.getPlanetInfos().getMaxBombardment(p.getName());
			int maxBombTemp = PlanetOrderStatusPureFunctions.getMaxBombardment(planet.getName(), aPlayer.getPlanetOrderStatuses());
			Logger.info("maxBombTemp: " + maxBombTemp);
			if (maxBombTemp == 9999) {
				maxBombTemp = 6;
			}
			maxBombardmentChoice.setSelectedIndex(maxBombTemp);
			maxBombardmentChoice.setVisible(true);
		}
	}

	private void showCheckBoxes(Planet p) {

		maxProdLabel.setLocation(200 + x, 209 + y);
		maxProdLabel2.setLocation(320 + x, 209 + y);

		maxProdLabel.setVisible(true);
		maxProdLabel2.setVisible(true);
		maxProdLabel.setText("Max. production: ");
		maxProdLabel2.setText(String.valueOf(getMaxPopulation(p)));
		// compute costs/bonuses
		int upgradePopCost = p.getPopulation();
		int upgradeResCost = p.getResistance();
		int openBonus = 2 + aPlayer.getOpenPlanetBonus() - aPlayer.getClosedPlanetBonus();

		if ((p.getPopulation() < getMaxPopulation(p)) & !GameWorldHandler.getFactionByUuid(aPlayer.getFactionUuid(), aPlayer.getGalaxy().getGameWorld()).isAlien()) {
			upgradepopcb.setVisible(true);
			upgradepopcb.setText("Upgrade production (cost: " + upgradePopCost + ")");
		}
		if (p.getResistance() < 10) {
			upgraderescb.setVisible(true);
			upgraderescb.setText("Upgrade resistance (cost: " + upgradeResCost + ")");
		}
		opencb.setText("Open (income: +" + openBonus + ")");
		opencb.setVisible(true);
		abandonCheckbox.setVisible(true);
		Logger.fine(p.getName() + " p.isBesieged(): " + p.isBesieged());
		if (p.isBesieged() | (aPlayer.getGalaxy().getTurn() == 0)) {
			opencb.setSelected(false);
			upgradepopcb.setSelected(false);
			upgraderescb.setSelected(false);
			opencb.setEnabled(false);
			upgradepopcb.setEnabled(false);
			upgraderescb.setEnabled(false);
			// can't abandon while besieged
			abandonCheckbox.setSelected(false);
			abandonCheckbox.setEnabled(false);
		} else {
			opencb.setEnabled(true);
			upgradepopcb.setEnabled(true);
			upgraderescb.setEnabled(true);
			// kolla orders och s�tt cb:s
			Orders o = aPlayer.getOrders();
			// set opencb
			boolean tempOpen = p.isOpen();
			if (o.getPlanetVisibility(p)) {
				opencb.setSelected(!tempOpen);
			} else {
				opencb.setSelected(tempOpen);
			}
			// set upgrade pop
			upgradepopcb.setSelected(o.incPopExpenseExist(p));
			// set upgrade res
			upgraderescb.setSelected(o.incResExpenseExist(p));
			// set the abandon cb and check how it affects the other cb:s
			abandonCheckbox.setSelected(o.getAbandonPlanet(p));
			checkAbandon(abandonCheckbox.isSelected());
		}
	}

	/*
	 * private void fillnewBuildingsChoice(){
	 * buildnewBuildingChoice.removeAllItems(); VIP tempEngineer =
	 * aPlayer.getGalaxy().findVIPBuildingBuildBonus(p,aPlayer,aPlayer.getOrders());
	 * 
	 * Vector <BuildingType> tempBuildingTypes =
	 * aPlayer.getAvailableNewBuildings(p); buildnewBuildingChoice.addItem("None");
	 * for(int i=0; i< tempBuildingTypes.size();i++){ int cost =
	 * tempBuildingTypes.get(i).getBuildCost(tempEngineer);
	 * buildnewBuildingChoice.addItem(tempBuildingTypes.get(i).getName() +
	 * " (cost: " + cost + ")"); }
	 * 
	 * }
	 */

	private void hideAll() {
		// clear checkboxes
		opencb.setVisible(false);
		opencb.setSelected(false);
		upgradepopcb.setVisible(false);
		upgradepopcb.setSelected(false);
		upgraderescb.setVisible(false);
		upgraderescb.setSelected(false);
		opencb.setEnabled(true);
		upgradepopcb.setEnabled(true);
		upgraderescb.setEnabled(true);
		attackIfNeutralCheckbox.setVisible(false);
		destroyBuildingCheckbox.setVisible(false);
		doNotBesiegeCheckbox.setVisible(false);
		maxBombLabel.setVisible(false);
		maxBombardmentChoice.setVisible(false);
		// abandonCheckbox.setVisible(false);
		maxProdLabel.setVisible(false);
		maxProdLabel2.setVisible(false);
		// empty labels
		maxProdLabel.setText("");
		maxProdLabel2.setText("");

		currentBuildBuildingLabel.setVisible(false);
		buildingList.setVisible(false);
		allVIPlist.setVisible(false);
		vipPanel.setVisible(false);

		troopsuportcost1.setVisible(false);
		troopsuportcost2.setVisible(false);
		troopsuportcost3.setVisible(false);
		troopsuportcost4.setVisible(false);
	}

	public void updateData() {
		if (planet != null) {
			showPlanet(planet.getName());
		}
	}

	public String getNotes() {
		return notesTextfield.getText();
	}
}