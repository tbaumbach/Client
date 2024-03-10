package spaceraze.client.game.panels.planet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spaceraze.client.components.CheckBoxPanel;
import spaceraze.client.components.ComboBoxPanel;
import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRButton;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.scrollable.ListPanel;
import spaceraze.client.components.scrollable.TextAreaPanel;
import spaceraze.client.game.SpaceRazePanel;
import spaceraze.servlethelper.game.BuildingPureFunctions;
import spaceraze.servlethelper.game.expenses.ExpensePureFunction;
import spaceraze.servlethelper.game.orders.OrderMutator;
import spaceraze.servlethelper.game.spaceship.SpaceshipPureFunctions;
import spaceraze.servlethelper.game.troop.TroopPureFunctions;
import spaceraze.servlethelper.game.vip.VipPureFunctions;
import spaceraze.servlethelper.game.player.PlayerPureFunctions;
import spaceraze.servlethelper.handlers.GameWorldHandler;
import spaceraze.util.general.Functions;
import spaceraze.util.general.Logger;
import spaceraze.world.*;
import spaceraze.servlethelper.comparator.BuildingTypeBuildCostAndNameComparator;
import spaceraze.servlethelper.comparator.SpaceshipTypeSizeComparator;
import spaceraze.servlethelper.comparator.VIPTypeComparator;
import spaceraze.servlethelper.comparator.trooptype.TroopTypeComparator;
import spaceraze.world.enums.TypeOfTroop;
import spaceraze.world.orders.Expense;
import spaceraze.world.orders.Orders;

public class MiniBuildingPanel extends SRBasePanel implements ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;
	List<Building> allBuildings = new ArrayList<>();
	ListPanel buildingList;
	SRLabel maxTonnageLabel, statusLabel, nameLabel, locationLabel, buildTypeLabel, slotsLabel, abilitiesLabel,
			currentBuildBuildingLabel, newBuildBuildingLabel;
	// sizeLabel,
	ComboBoxPanel[] shipTypeChoice = new ComboBoxPanel[5];
	ComboBoxPanel[] troopTypeChoice = new ComboBoxPanel[5];
	ComboBoxPanel[] VIPTypeChoice = new ComboBoxPanel[1];
	ComboBoxPanel[] upgradeBuildingTypeChoice = new ComboBoxPanel[1];
	private ComboBoxPanel buildnewBuildingChoice;
	private SRButton detailsButton, detailsNewBuildingChoice, detailsVIP, detailsUpgrade;
	private SRButton[] buttonsShipsUpgrade, buttonsTroopsUpgrade;
	Building currentBuilding = null;
	BuildingType currentBuildingType = null;
	Player player;
	SpaceRazePanel client;
	CheckBoxPanel selfDestructCheckBox;
	TextAreaPanel abilitiesTextArea;
	int x = -215;
	private boolean cleaning = false;
	private boolean noaction = false;
	private Planet aPlanet;

	public MiniBuildingPanel(Planet aPlanet, Player player, SpaceRazePanel client) {
		this.player = player;
		this.aPlanet = aPlanet;
		this.allBuildings = aPlanet.getBuildings();
		this.setLayout(null);
		this.client = client;

		newBuildBuildingLabel = new SRLabel("Build new building:");
		newBuildBuildingLabel.setBounds(5, 5, 200, 18);
		newBuildBuildingLabel.setToolTipText("Choose a new building to build.");
		// BuildBuildingLabel.setFont(new Font("Dialog",0,10));
		add(newBuildBuildingLabel);

		buildnewBuildingChoice = new ComboBoxPanel();
		buildnewBuildingChoice.setBounds(5, 23, 294, 20);
		// ej kunna bygga ny vid abandon, bel�gring och fiendetrupper
		boolean enemyTroopsOnPlanet = TroopPureFunctions.findOtherTroopsPlayersOnRazedPlanet(player, aPlanet, player.getGalaxy().getTroops())
				.size() > 0;
		boolean underSiege = aPlanet.isBesieged();
		boolean abandonPlanet = player.getOrders().getAbandonPlanet(aPlanet);
		if (enemyTroopsOnPlanet | underSiege | abandonPlanet) {
			buildnewBuildingChoice.setEnabled(false);
		} else {
			fillNewBuildingsChoice();
			buildnewBuildingChoice.addActionListener(this);
		}
		buildnewBuildingChoice.setToolTipText("Choose a new building to build.");
		this.add(buildnewBuildingChoice);

		detailsNewBuildingChoice = new SRButton("?");
		detailsNewBuildingChoice.setBounds(301, 23, 20, 20);
		detailsNewBuildingChoice.addActionListener(this);
		detailsNewBuildingChoice.setVisible(false);
		detailsNewBuildingChoice.setToolTipText("Click for more details information about the building");
		add(detailsNewBuildingChoice);

		currentBuildBuildingLabel = new SRLabel("Buildings on planet:");
		currentBuildBuildingLabel.setBounds(5, 55, 200, 18);
		currentBuildBuildingLabel.setToolTipText("Mark a building to build units or upgrade");
		// BuildBuildingLabel.setFont(new Font("Dialog",0,10));
		add(currentBuildBuildingLabel);

		buildingList = new ListPanel();
		buildingList.setBounds(5, 75, 315, 140);
		buildingList.setListSelectionListener(this);
		buildingList.setToolTipText("Mark a building to build units or upgrade");
		add(buildingList);

		DefaultListModel dlm = (DefaultListModel) buildingList.getModel();
		for (int i = 0; i < allBuildings.size(); i++) {
			dlm.addElement(BuildingPureFunctions.getBuildingTypeByUuid(allBuildings.get(i).getTypeUuid(), player.getGalaxy().getGameWorld()).getName());
		}
		buildingList.updateScrollList();

		nameLabel = new SRLabel();
		nameLabel.setBounds(220 + x, 220, 200, 20);
		nameLabel.setToolTipText("Building name");
		add(nameLabel);

		statusLabel = new SRLabel();
		statusLabel.setBounds(220 + x, 240, 310, 20);
		add(statusLabel);

		buildTypeLabel = new SRLabel();
		buildTypeLabel.setBounds(220 + x, 260, 315, 20);
		add(buildTypeLabel);

		for (int i = 0; i < shipTypeChoice.length; i++) {
			shipTypeChoice[i] = new ComboBoxPanel();
			shipTypeChoice[i].setBounds(220 + x, 280 + (i * 20), 294, 20);
			shipTypeChoice[i].addActionListener(this);
			if (i == 0) {
				shipTypeChoice[i].setToolTipText("Choose to upgrade or build a new ship");
			} else {
				shipTypeChoice[i].setToolTipText("Choose a ship to build");
			}
			add(shipTypeChoice[i]);
		}

		clearShipTypeChoices();

		for (int i = 0; i < troopTypeChoice.length; i++) {
			troopTypeChoice[i] = new ComboBoxPanel();
			troopTypeChoice[i].setBounds(220 + x, 280 + (i * 20), 294, 20);
			troopTypeChoice[i].addActionListener(this);
			if (i == 0) {
				troopTypeChoice[i].setToolTipText("Choose to upgrade or build a new troop unit");
			} else {
				troopTypeChoice[i].setToolTipText("Choose a troop unit to build");
			}
			add(troopTypeChoice[i]);
		}
		clearTroopTypeChoices();

		for (int i = 0; i < VIPTypeChoice.length; i++) {
			VIPTypeChoice[i] = new ComboBoxPanel();
			VIPTypeChoice[i].setBounds(220 + x, 280 + (i * 20), 294, 20);
			VIPTypeChoice[i].addActionListener(this);
			VIPTypeChoice[i].setToolTipText("Choose to upgrade or recruit a new VIP");
			add(VIPTypeChoice[i]);
		}
		clearVIPTypeChoices();

		for (int i = 0; i < upgradeBuildingTypeChoice.length; i++) {
			upgradeBuildingTypeChoice[i] = new ComboBoxPanel();
			upgradeBuildingTypeChoice[i].setBounds(220 + x, 280 + (i * 20), 294, 20);
			upgradeBuildingTypeChoice[i].addActionListener(this);
			VIPTypeChoice[i].setToolTipText("Choose to upgrade this building");
			add(upgradeBuildingTypeChoice[i]);
		}

		clearUpgradeBuildingTypeChoice();

		detailsVIP = new SRButton("?");
		detailsVIP.setBounds(301, 280, 20, 20);
		detailsVIP.addActionListener(this);
		detailsVIP.setVisible(false);
		detailsVIP.setToolTipText("Click for more details information about the VIP");
		add(detailsVIP);

		detailsUpgrade = new SRButton("?");
		detailsUpgrade.setBounds(301, 280, 20, 20);
		detailsUpgrade.addActionListener(this);
		detailsUpgrade.setVisible(false);
		detailsUpgrade.setToolTipText("Click for more details information about the building");
		add(detailsUpgrade);

		abilitiesLabel = new SRLabel();
		abilitiesLabel.setBounds(220 + x, 380, 200, 20);
		abilitiesLabel.setToolTipText("The buildings abilities");
		add(abilitiesLabel);

		abilitiesTextArea = new TextAreaPanel();
		abilitiesTextArea.setBounds(220 + x, 400, 315, 125);
		abilitiesTextArea.setVisible(false);
		abilitiesTextArea.setToolTipText("The buildings abilities");
		add(abilitiesTextArea);

		selfDestructCheckBox = new CheckBoxPanel("Selfdestruct");
		selfDestructCheckBox.setBounds(220 + x, 532, 200, 20);
		selfDestructCheckBox.setSelected(false);
		selfDestructCheckBox.addActionListener(this);
		selfDestructCheckBox.setVisible(false);
		selfDestructCheckBox.setToolTipText("Check this to destroy this building");
		add(selfDestructCheckBox);

		detailsButton = new SRButton("View Details");
		detailsButton.setBounds(435 + x, 532, 100, 18);
		detailsButton.addActionListener(this);
		detailsButton.setVisible(false);
		detailsButton.setToolTipText("Click for more details information about the building");
		add(detailsButton);

		Orders o = player.getOrders();
		if (BuildingPureFunctions.getNewBuilding(aPlanet, player, o.getExpenses()) != null) {
			BuildingType buildingType = BuildingPureFunctions.getNewBuilding(aPlanet, player, o.getExpenses());
			VIP tempEngineer = VipPureFunctions.findVIPBuildingBuildBonus(aPlanet, player, player.getOrders(), player.getGalaxy());
			int vipBuildBonus = tempEngineer == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempEngineer.getTypeUuid(), player.getGalaxy().getGameWorld()).getBuildingBuildBonus();
			int cost = BuildingPureFunctions.getBuildCost(buildingType, vipBuildBonus);
			buildnewBuildingChoice.setSelectedItem(buildingType.getName() + " (cost: " + cost + ")");
			detailsNewBuildingChoice.setVisible(true);
		}

	}

	private void clearShipTypeChoices() {
		for (int i = shipTypeChoice.length - 1; i >= 0; i--) {
			shipTypeChoice[i].setVisible(false);
			cleaning = true;
			shipTypeChoice[i].removeAllItems();
			cleaning = false;
			if (buttonsShipsUpgrade != null && i < buttonsShipsUpgrade.length) {
				remove(buttonsShipsUpgrade[i]);
			}

		}
		buttonsShipsUpgrade = null;
	}

	private void clearTroopTypeChoices() {
		for (int i = 0; i < troopTypeChoice.length; i++) {
			troopTypeChoice[i].setVisible(false);
			cleaning = true;
			troopTypeChoice[i].removeAllItems();
			cleaning = false;
			if (buttonsTroopsUpgrade != null && i < buttonsTroopsUpgrade.length) {
				remove(buttonsTroopsUpgrade[i]);
			}
		}
		buttonsTroopsUpgrade = null;
	}

	private void clearVIPTypeChoices() {
		for (int i = 0; i < VIPTypeChoice.length; i++) {
			VIPTypeChoice[i].setVisible(false);
			cleaning = true;
			VIPTypeChoice[i].removeAllItems();
			cleaning = false;
		}
	}

	private void clearUpgradeBuildingTypeChoice() {
		for (int i = 0; i < upgradeBuildingTypeChoice.length; i++) {
			upgradeBuildingTypeChoice[i].setVisible(false);
			cleaning = true;
			upgradeBuildingTypeChoice[i].removeAllItems();
			cleaning = false;
		}
	}

	public void actionPerformed(ActionEvent ae) {
		// System.out.println("actionPerformed" + ae.toString() + " xxx " + ae.getID() +
		// " xxx " + ae.getModifiers());
		if (ae.toString().indexOf("invalid,hidden") > -1) {
			// System.out.println("ae.toString().indexOf('invalid,hidden') > -1");
			// g�r inget, detta �r ingen riktig action
		} else if (ae.getSource() instanceof CheckBoxPanel) {
			newOrder((CheckBoxPanel) ae.getSource());
		} else if (ae.getSource() instanceof ComboBoxPanel
				&& (ComboBoxPanel) ae.getSource() == buildnewBuildingChoice) {
			newBuildingOrder();
		} else if (ae.getSource() instanceof SRButton) {
			Logger.finer("ae.getSource() instanceof SRButton");

			if (ae.getActionCommand().equalsIgnoreCase("View Details")) {
				client.showBuildingTypeDetails(currentBuildingType.getName(), "Yours");
			} else if ((SRButton) ae.getSource() == detailsNewBuildingChoice) {
				showNewBuildingDetails();
			} else if ((SRButton) ae.getSource() == detailsVIP) {
				showVIPDetails();
			} else if ((SRButton) ae.getSource() == detailsUpgrade) {
				showUpgradeBuildingDetails();
			} else {
				if (currentBuilding.getWharfSize() > 0) {
					for (int i = 0; i < buttonsShipsUpgrade.length; i++) {
						if ((SRButton) ae.getSource() == buttonsShipsUpgrade[i]) {
							client.showShiptypeDetails(getComboBoxValue(shipTypeChoice[i].getSelectedItem()), "Yours");
						}
					}

				} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
					for (int i = 0; i < buttonsTroopsUpgrade.length; i++) {
						if ((SRButton) ae.getSource() == buttonsTroopsUpgrade[i]) {
							client.showTroopTypeDetails(getComboBoxValue(troopTypeChoice[i].getSelectedItem()),
									"Yours");
						}
					}
				}
			}
		} else {
			if (currentBuilding.getWharfSize() > 0) {
				System.out.println("currentBuildingType.isShipBuilder();"
						+ currentBuildingType.getName() + " "
						+ (currentBuilding.getWharfSize() > 0));
				// { leta r�tt p� vilken choice som har valts
				int found = -1;
				int i = 0;
				while ((i < 5) & (found == -1)) {
					if ((ComboBoxPanel) ae.getSource() == shipTypeChoice[i]) {
						found = i;
					} else {
						i++;
					}
				}
				if (found > -1) {
					if (!cleaning & !noaction) {
						// System.out.println("newOrder(found);" + cleaning + " " +noaction);
						System.out
								.println("MiniBuildingPanel: actionPerformed: newOrder(found, 'Ship') found= " + found);
						newOrder(found, "Ship");
					}
				}
			} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
				// { leta r�tt p� vilken choice som har valts
				int found = -1;
				int i = 0;
				while ((i < 5) & (found == -1)) {
					if ((ComboBoxPanel) ae.getSource() == troopTypeChoice[i]) {
						found = i;
					} else {
						i++;
					}
				}
				if (found > -1) {
					if (!cleaning & !noaction) {
						// System.out.println("newOrder(found);" + cleaning + " " +noaction);
						newOrder(found, "Troop");
					}
				}
			} else if (currentBuildingType.getVipTypes().size() > 0) {
				// { leta r�tt p� vilken choice som har valts
				int found = -1;
				int i = 0;
				while ((i < 5) & (found == -1)) {
					if ((ComboBoxPanel) ae.getSource() == VIPTypeChoice[i]) {
						found = i;
					} else {
						i++;
					}
				}
				if (found > -1) {
					if (!cleaning & !noaction) {
						// System.out.println("newOrder(found);" + cleaning + " " +noaction);
						newOrder(found, "VIP");
					}
				}
			} else {
				// { leta r�tt p� vilken choice som har valts
				int found = -1;
				int i = 0;
				while ((i < 5) & (found == -1)) {
					if ((ComboBoxPanel) ae.getSource() == upgradeBuildingTypeChoice[i]) {
						found = i;
					} else {
						i++;
					}
				}
				if (found > -1) {
					if (!cleaning & !noaction) {
						// System.out.println("newOrder(found);" + cleaning + " " +noaction);
						newOrder(found, "Building");
					}
				}

			}
		}
	}

	public void valueChanged(ListSelectionEvent lse) {
		// System.out.println("valueChanged" + lse.toString() +
		// lse.getValueIsAdjusting());
		try {
			if (lse.getSource() instanceof ListPanel) {
				if ((ListPanel) lse.getSource() == buildingList) {
					if (lse.getValueIsAdjusting()) {
						noaction = true;
						showBuilding(buildingList.getSelectedIndex());
						noaction = false;
					}
				}
			}
		} catch (NumberFormatException nfe) {
		}
	}

	private void newOrder(int choiceIndex, String unitType) {
		// först ta bort alla gamla orders för det aktuella varvet
		System.out.println("newOrder currentBuilding: " + currentBuilding.getUuid());

		OrderMutator.removeUpgradeBuilding(currentBuilding, player.getOrders());

		if (unitType.equalsIgnoreCase("Ship")) {
			OrderMutator.removeAllBuildShip(currentBuilding, player.getOrders());
			// lopa igenom alla choisar till och med den nyligen valda
			for (int i = 0; i <= choiceIndex; i++) {
				String selected = (String) shipTypeChoice[i].getSelectedItem();
				// remove paranthesis with cost...
				int index = selected.indexOf("(");
				if (index > -1) {
					selected = selected.substring(0, index - 1);
				}
				if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
					// g�r inget
				} else {

					if (i == 0 && shipTypeChoice[i].getSelectedIndex() <= BuildingPureFunctions.getUpgradableBuildingTypes(player.getGalaxy(), player, currentBuildingType, currentBuilding, aPlanet, null).size() + 1) {// i == 0 är första
																						// valet(selectboxen) i
																						// comboBoxen och där fins
																						// möjligheten att göra en
																						// uppdatering till en ny
																						// byggnad.
						// Is a upgrade (Building)
						System.out.println("currentBuilding " + currentBuilding.getUuid() + " "
								+ currentBuildingType.getName());

						addUpgradeBuilding(currentBuilding, PlayerPureFunctions.findOwnBuildingTypeByName(selected, player), player);
					} else { // eller skeppsbygge */
						SpaceshipType tempsst = getShipType(selected);
						player.addBuildShip(currentBuilding, tempsst); // l�gg till en ny order f�r denna choice
					}
				}
			}
		} else if (unitType.equalsIgnoreCase("Troop")) {
			OrderMutator.removeAllBuildTroop(currentBuilding, player.getOrders());
			for (int i = 0; i <= choiceIndex; i++) {
				String selected = (String) troopTypeChoice[i].getSelectedItem();
				// remove paranthesis with cost...
				int index = selected.indexOf("(");
				if (index > -1) {
					selected = selected.substring(0, index - 1);
				}
				if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
					// g�r inget
				} else {
					if (i == 0
							&& troopTypeChoice[i].getSelectedIndex() <= BuildingPureFunctions.getUpgradableBuildingTypes(player.getGalaxy(), player, currentBuildingType, currentBuilding, aPlanet, null).size() + 1
							&& BuildingPureFunctions.getUpgradableBuildingTypes(player.getGalaxy(), player, currentBuildingType, currentBuilding, aPlanet, null).size() > 0) {// i == 0 är första valet(selectboxen) i comboBoxen och där fins
													// möjligheten att göra en uppdatering till en ny byggnad.
						// Is a upgrade (Building)
						System.out.println("currentBuilding " + currentBuilding.getUuid() + " "
								+ currentBuildingType.getName());
						addUpgradeBuilding(currentBuilding, PlayerPureFunctions.findOwnBuildingTypeByName(selected, player), player);
					} else { // eller troopbygge */
						player.addBuildTroop(currentBuilding, PlayerPureFunctions.findOwnTroopType(TroopPureFunctions.getTroopTypeByName(selected, player.getGalaxy().getGameWorld()).getUuid(), player, player.getGalaxy())); // lägg till en ny order för denna choice

					}
				}
			}

		} else if (unitType.equalsIgnoreCase("VIP")) {// VIP
			OrderMutator.removeBuildVIP(currentBuilding, player.getOrders());

			String selected = (String) VIPTypeChoice[0].getSelectedItem();
			// remove paranthesis with cost...
			int index = selected.indexOf("(");
			if (index > -1) {
				selected = selected.substring(0, index - 1);
			}
			if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
				// g�r inget
			} else {
				if (VIPTypeChoice[0].getSelectedIndex() <= BuildingPureFunctions.getUpgradableBuildingTypes(player.getGalaxy(), player, currentBuildingType, currentBuilding, aPlanet, null).size()) {// i == 0 är första valet(selectboxen) i
																				// comboBoxen och där fins möjligheten
																				// att göra en uppdatering till en ny
																				// byggnad.
					// Is a upgrade (Building)
					System.out.println("currentBuilding " + currentBuilding.getUuid() + " "
							+ currentBuildingType.getName());
					addUpgradeBuilding(currentBuilding, PlayerPureFunctions.findOwnBuildingTypeByName(selected, player), player);
				} else { // eller VIPsbygge */
					VIPType vipType = getVIPType(selected);
					player.addBuildVIP(currentBuilding, vipType); // lägg till en ny order för denna choice

				}
			}

		} else { // Building

			String selected = (String) upgradeBuildingTypeChoice[0].getSelectedItem();
			// remove paranthesis with cost...
			int index = selected.indexOf("(");
			if (index > -1) {
				selected = selected.substring(0, index - 1);
			}
			if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
				// gör inget
			} else {
				// Is a upgrade (Building)
				System.out.println("currentBuilding " + currentBuilding.getUuid() + " "
						+ currentBuildingType.getName());
				System.out.println("currentBuildingType.getNextBuildingType(selected) "
						+ PlayerPureFunctions.findOwnBuildingTypeByName(selected, player).getName());

				addUpgradeBuilding(currentBuilding, PlayerPureFunctions.findOwnBuildingTypeByName(selected, player), player);

			}
		}

		// uppdatera treasurylabeln
		client.updateTreasuryLabel();
		// töm choisarna
		// clearShipTypeChoices();
		// visa choisarna, nu med den nya ordern
		showBuilding(buildingList.getSelectedIndex());
	}

	private void addUpgradeBuilding(Building currentBuilding, BuildingType newBuilding, Player aPlayer) {
		// skapa ny order om inte varvet redan �r satt att uppgradera
		if (!ExpensePureFunction.alreadyUpgrading(aPlayer.getOrders(), currentBuilding)) {
			aPlayer.getOrders().getExpenses().add(new Expense("building", newBuilding, aPlayer.getName(), currentBuilding.getLocation(), currentBuilding));
		}
	}

	private void newOrder(CheckBoxPanel cb) {
		if (cb == selfDestructCheckBox) {
			if (cb.isSelected()) {
				// först ta bort alla gamla orders för det aktuella varvet
				OrderMutator.removeUpgradeBuilding(currentBuilding, player.getOrders());
				OrderMutator.removeAllBuildShip(currentBuilding, player.getOrders());
				OrderMutator.removeAllBuildTroop(currentBuilding, player.getOrders());
				OrderMutator.removeBuildVIP(currentBuilding, player.getOrders());
				// töm choisarna
				clearShipTypeChoices();
				clearTroopTypeChoices();
				clearVIPTypeChoices();
				clearUpgradeBuildingTypeChoice();

				// disabla översta choicen
				shipTypeChoice[0].setEnabled(false);
				troopTypeChoice[0].setEnabled(false);
				VIPTypeChoice[0].setEnabled(false);
				upgradeBuildingTypeChoice[0].setEnabled(false);
				// add selfdestruct order
				player.addBuildingSelfDestruct(currentBuilding);
				// visa choisarna, nu med den nya ordern
				showBuilding(buildingList.getSelectedIndex());
				// ändra översta choicen till "none"
				if (currentBuilding.getWharfSize() > 0) {
					shipTypeChoice[0].setSelectedIndex(0);
				} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
					troopTypeChoice[0].setSelectedIndex(0);
				} else if (currentBuildingType.getVipTypes().size() > 0) {
					VIPTypeChoice[0].setSelectedIndex(0);
				} else {
					upgradeBuildingTypeChoice[0].setSelectedIndex(0);
				}

			} else {
				// enabla översta choicen
				shipTypeChoice[0].setEnabled(true);
				troopTypeChoice[0].setEnabled(true);
				shipTypeChoice[0].setEnabled(true);
				upgradeBuildingTypeChoice[0].setEnabled(true);
				// remove selfdestruct order
				player.removeBuildingSelfDestruct(currentBuilding);
			}
			// update treasury label...
			client.updateTreasuryLabel();
		}
	}

	private void newBuildingOrder() {
		player.removeNewBuilding(aPlanet);
		String selected = (String) buildnewBuildingChoice.getSelectedItem();
		// remove paranthesis with cost...
		int index = selected.indexOf("(");
		if (index > -1) {
			selected = selected.substring(0, index - 1);
		}
		if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
			detailsNewBuildingChoice.setVisible(false);
			// g�r inget
		} else {
			player.addNewBuilding(aPlanet, PlayerPureFunctions.findOwnBuildingTypeByName(selected, player));
			detailsNewBuildingChoice.setVisible(true);
			// uppdatera "left to spend" och send-knappen

		}
		client.updateTreasuryLabel();

	}

	private void showNewBuildingDetails() {
		String selected = (String) buildnewBuildingChoice.getSelectedItem();
		int index = selected.indexOf("(");
		if (index > -1) {
			selected = selected.substring(0, index - 1);
		}
		if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
		} else {
			client.showBuildingTypeDetails(selected, "Yours");
		}
	}

	private void showVIPDetails() {
		String selected = (String) VIPTypeChoice[0].getSelectedItem();
		int index = selected.indexOf("(");
		if (index > -1) {
			selected = selected.substring(0, index - 1);
		}
		if (selected.equalsIgnoreCase("None") || selected.startsWith("---")) {
		} else {
			client.showVIPTypeDetails(selected, "Yours");
		}
	}

	private void showUpgradeBuildingDetails() {
		if (currentBuilding.getWharfSize() > 0) {
			client.showBuildingTypeDetails(getComboBoxValue(shipTypeChoice[0].getSelectedItem()), "Yours");
		} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
			client.showBuildingTypeDetails(getComboBoxValue(troopTypeChoice[0].getSelectedItem()), "Yours");
		} else if (currentBuildingType.getVipTypes().size() > 0) {
			client.showBuildingTypeDetails(getComboBoxValue(VIPTypeChoice[0].getSelectedItem()), "Yours");
		} else {
			client.showBuildingTypeDetails(getComboBoxValue(upgradeBuildingTypeChoice[0].getSelectedItem()), "Yours");
		}
	}

	private String getComboBoxValue(String text) {
		int index = text.indexOf("(");
		if (index > -1) {
			return text.substring(0, index - 1);
		}
		return text;
	}

	private void showBuilding(int index) {
		Logger.fine("showBuilding");
		// currentBuilding = findWharf(index);
		currentBuilding = allBuildings.get(index);
		currentBuildingType = PlayerPureFunctions.findBuildingTypeByUuid(currentBuilding.getTypeUuid(), player);
		if (currentBuilding != null) {
			Logger.finer("currentBuilding: " + currentBuildingType.getName());
			nameLabel.setText("Name: " + currentBuildingType.getName());

			detailsUpgrade.setVisible(false);
			detailsVIP.setVisible(false);

			clearShipTypeChoices();
			clearTroopTypeChoices();
			clearVIPTypeChoices();
			clearUpgradeBuildingTypeChoice();

			Logger.finer("efter clear: ");

			String statusString = "";
			// Kolla om man kan bygga i byggnaden
			boolean enemyTroopsOnPlanet = TroopPureFunctions.findOtherTroopsPlayersOnRazedPlanet(player, currentBuilding.getLocation(), player.getGalaxy().getTroops()).size() > 0;
			boolean underSiege = currentBuilding.getLocation().isBesieged()
					&& currentBuildingType.isInOrbit();
			boolean abandonPlanet = player.getOrders().getAbandonPlanet(aPlanet);
			if (enemyTroopsOnPlanet) {
				statusString = "Can't build any units if planet have ongoing ground battles.";
				Logger.finer("Planet have ongoing ground battles so buildigns can not build any units at this time.");
				noBuildingAllowed();
			} else if (underSiege) {
				statusString = "Buildigns in orbit can't build units on blocked/undersiege plantes";
				Logger.finer("Planet blocked/undersiege so buildigns in orbit can not build any units at this time.");
				noBuildingAllowed();
			} else if (abandonPlanet) {
				statusString = "Abandoned planet can't build units.";
				Logger.finer("Planet is to be abandoned so buildings can not build any units at this time.");
				noBuildingAllowed();
			} else { // ok to use building to build

				Logger.finer("ingen blockad eller fiender trupper.");
				if (currentBuilding.getWharfSize() > 0) {
					// visa alla tidigare valda skeppsorder samt ev. en till med ytterligare
					// tillgängliga alternativ
					showShipTypeChoices();
					if (selfDestructCheckBox.isSelected()) { // om denna building �r satt att selfdestructa, disabla
																// översta choicen
						shipTypeChoice[0].setEnabled(false);
					} else {
						shipTypeChoice[0].setEnabled(true);
					}
					troopTypeChoice[0].setVisible(false);
					VIPTypeChoice[0].setVisible(false);
					upgradeBuildingTypeChoice[0].setVisible(false);
					shipTypeChoice[0].setVisible(true);
					buildTypeLabel.setVisible(true);
				} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
					// visa alla tidigare valda troopsorder samt ev. en till med ytterligare
					// tillgängliga alternativ
					showTroopTypeChoices(currentBuilding);
					if (selfDestructCheckBox.isSelected()) { // om denna building är satt att selfdestructa, disabla
																// översta choicen
						troopTypeChoice[0].setEnabled(false);
					} else {
						troopTypeChoice[0].setEnabled(true);
					}
					shipTypeChoice[0].setVisible(false);
					VIPTypeChoice[0].setVisible(false);
					upgradeBuildingTypeChoice[0].setVisible(false);
					troopTypeChoice[0].setVisible(true);
					buildTypeLabel.setVisible(true);
				} else if (currentBuildingType.getVipTypes().size() > 0) {
					// visa alla tidigare valda VIPsorder samt ev. en till med ytterligare
					// tillgängliga alternativ
					showVIPTypeChoices(currentBuilding);
					if (selfDestructCheckBox.isSelected()) { // om denna building �r satt att selfdestructa, disabla
																// översta choicen
						VIPTypeChoice[0].setEnabled(false);
					} else {
						VIPTypeChoice[0].setEnabled(true);
					}
					troopTypeChoice[0].setVisible(false);
					shipTypeChoice[0].setVisible(false);
					upgradeBuildingTypeChoice[0].setVisible(false);
					VIPTypeChoice[0].setVisible(true);
					buildTypeLabel.setVisible(true);
				} else {
					showUpgradeBuildingTypeChoice(currentBuilding);
					if (selfDestructCheckBox.isSelected()) { // om denna building är satt att selfdestructa, disabla
																// översta choicen
						upgradeBuildingTypeChoice[0].setEnabled(false);
					} else {
						upgradeBuildingTypeChoice[0].setEnabled(true);
					}
					troopTypeChoice[0].setVisible(false);
					shipTypeChoice[0].setVisible(false);
					VIPTypeChoice[0].setVisible(false);
					if (upgradeBuildingTypeChoice[0].getItemCount() > 1) {
						upgradeBuildingTypeChoice[0].setVisible(true);
						buildTypeLabel.setVisible(true);
					} else {
						upgradeBuildingTypeChoice[0].setVisible(false);
						buildTypeLabel.setVisible(false);
					}
				}
			}

			statusLabel.setText(statusString);
			// locationLabel.setText("Location: " +
			// currentBuilding.getLocation().getName());
			if (currentBuilding.getWharfSize() > 0) {
				buildTypeLabel.setText("Build new ship or upgrade the building:");
			} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
				buildTypeLabel.setText("Build new troop or upgrade the building:");
			} else if (currentBuildingType.getVipTypes().size() > 0) {
				buildTypeLabel.setText("Recruite new VIP or upgrade the building:");
			} else {
				buildTypeLabel.setText("Upgrade building to:");
			}

			// show and set selfdestruct cb
			selfDestructCheckBox.setSelected(player.getBuildingSelfDestruct(currentBuilding));
			boolean destructable = false;
			if (currentBuildingType.isSelfDestructible()) {
				destructable = true;
			}

			if (currentBuilding.getLocation().isBesieged() && currentBuildingType.isInOrbit()) {
				destructable = false;
			}

			if (!currentBuildingType.isInOrbit()
					&& player.getGalaxy().isOngoingGroundBattle(currentBuilding.getLocation(), player)) {
				destructable = false;
			}

			selfDestructCheckBox.setEnabled(destructable);
			selfDestructCheckBox.setVisible(true);

			detailsButton.setVisible(true);

			abilitiesLabel.setText("Building abilities:");
			abilitiesTextArea.setText("");
			List<String> allStrings = BuildingPureFunctions.getAbilitiesStrings(currentBuildingType, player.getGalaxy().getGameWorld());
			for (int i = 0; i < allStrings.size(); i++) {
				abilitiesTextArea.append(allStrings.get(i) + "\n");
				Logger.finer("Building abilities: " + allStrings.get(i));
			}
			abilitiesTextArea.setVisible(true);
			abilitiesTextArea.repaint();

		} else {
			nameLabel.setText("");
			// maxTonnageLabel.setText("");
			// sizeLabel.setText("");
			// slotsLabel.setText("");
			statusLabel.setText("");
			// locationLabel.setText("");
			buildTypeLabel.setText("");
			clearShipTypeChoices();
			clearTroopTypeChoices();
			clearVIPTypeChoices();
			clearUpgradeBuildingTypeChoice();
			selfDestructCheckBox.setVisible(false);
			detailsButton.setVisible(false);
			abilitiesLabel.setText("");
			abilitiesTextArea.setText("");
			abilitiesTextArea.setVisible(false);

		}
	}

	private void noBuildingAllowed() {
		if (currentBuilding.getWharfSize() > 0) {
			shipTypeChoice[0].addItem("None");
			shipTypeChoice[0].setEnabled(false);
			shipTypeChoice[0].setVisible(true);
			troopTypeChoice[0].setVisible(false);
			VIPTypeChoice[0].setVisible(false);
			upgradeBuildingTypeChoice[0].setVisible(false);
		} else if (currentBuildingType.getTypeOfTroop().size() > 0) {
			troopTypeChoice[0].addItem("None");
			troopTypeChoice[0].setEnabled(false);
			troopTypeChoice[0].setVisible(true);
			shipTypeChoice[0].setVisible(false);
			VIPTypeChoice[0].setVisible(false);
			upgradeBuildingTypeChoice[0].setVisible(false);
		} else if (currentBuildingType.getVipTypes().size() > 0) {
			VIPTypeChoice[0].addItem("None");
			VIPTypeChoice[0].setEnabled(false);
			VIPTypeChoice[0].setVisible(true);
			troopTypeChoice[0].setVisible(false);
			shipTypeChoice[0].setVisible(false);
			upgradeBuildingTypeChoice[0].setVisible(false);
		} else { // upgrade building
			upgradeBuildingTypeChoice[0].addItem("None");
			upgradeBuildingTypeChoice[0].setEnabled(false);
			upgradeBuildingTypeChoice[0].setVisible(true);
			troopTypeChoice[0].setVisible(false);
			shipTypeChoice[0].setVisible(false);
			VIPTypeChoice[0].setVisible(false);
		}
	}

	private void showShipTypeChoices() {
		Orders playersOrders = player.getOrders();
		VIP tempBuild;

		int index = 0;
		boolean showUpgrade = true; // skall endast visas i den översta choicen
		int slotsleft = currentBuilding.getWharfSize();
		int tempMaxTonnage = slotsleft * 300;

		if (ExpensePureFunction.alreadyUpgrading(playersOrders, currentBuilding)) {
			BuildingType buildingType = BuildingPureFunctions.getUpgradeBuilding(currentBuilding, player, playersOrders.getExpenses());
			tempBuild = VipPureFunctions.findVIPBuildingBuildBonus(currentBuilding.getLocation(), player,
					player.getOrders(), player.getGalaxy());
			int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getBuildingBuildBonus();
			int cost = BuildingPureFunctions.getBuildCost(buildingType, vipBuildBonus);
			addShipTypes(shipTypeChoice[index], showUpgrade, slotsleft);
			shipTypeChoice[0].setSelectedItem(
					buildingType.getName() + " (cost: " + cost + ") " + getUniqueString(buildingType));
			shipTypeChoice[0].setVisible(true);
			detailsUpgrade.setVisible(true);
		} else {
			List<SpaceshipType> buildsst = getAllShipBuilds(currentBuilding);
			// System.out.println("buildsst: " + buildsst.size());

			if (buildsst.size() > 0) {
				buttonsShipsUpgrade = new SRButton[buildsst.size()];
			}

			while (index < buildsst.size()) {
				// System.out.println("index: " + index);
				// System.out.println("tempMaxTonnage: " + tempMaxTonnage);
				// System.out.println("slotsleft: " + slotsleft);
				// System.out.println("");
				SpaceshipType tempsst = buildsst.get(index);
				addShipTypes(shipTypeChoice[index], showUpgrade, slotsleft);
				if (showUpgrade) { // s�tt till false s� att endast den f�rsta choicen visar upgrade
					showUpgrade = false;
				}
				// beräkna hur många slots det finns kvar efter detta skeppsbygge
				slotsleft = slotsleft - tempsst.getSize().getSlots();
				// System.out.println("slotsleft: " + slotsleft);
				tempMaxTonnage = slotsleft * 300;
				// compute cost
				tempBuild = VipPureFunctions.findVIPShipBuildBonus(currentBuilding.getLocation(), player,
						player.getOrders(), player.getGalaxy());
				int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getShipBuildBonus();
				int cost = SpaceshipPureFunctions.getBuildCost(tempsst, vipBuildBonus);
				// set selected
				shipTypeChoice[index]
						.setSelectedItem(tempsst.getName() + " (cost: " + cost + ") " + getUniqueString(tempsst));
				shipTypeChoice[index].setVisible(true);

				// show detailsButton
				buttonsShipsUpgrade[index] = new SRButton("?");
				buttonsShipsUpgrade[index].setBounds(301, 280 + (index * 20), 20, 20);
				buttonsShipsUpgrade[index].addActionListener(this);
				buttonsShipsUpgrade[index].setVisible(true);
				buttonsShipsUpgrade[index].setToolTipText("Click for more details information about the ship");
				add(buttonsShipsUpgrade[index]);

				index++;
			}
			// om det finns slots kvar visa en till choice som inte har något valt
			// alternativ
			if (slotsleft > 0) {
				// System.out.println("slotsleft > 0: ");
				// System.out.println("tempMaxTonnage: " + tempMaxTonnage);
				// System.out.println("slotsleft: " + slotsleft);
				// System.out.println("");
				addShipTypes(shipTypeChoice[index], showUpgrade, slotsleft);
				shipTypeChoice[index].setVisible(true);
			}
		}

	}

	public String getUniqueString(BuildingType buildingType){
		String uniqueString = "";

		if(buildingType.isPlanetUnique()){
			uniqueString = "Planet unique";
		}else
		if(buildingType.isPlayerUnique()){
			uniqueString = "Player unique";
		}else
		if(buildingType.isFactionUnique()){
			uniqueString = "Faction unique";
		}else
		if(buildingType.isWorldUnique()){
			uniqueString = "World unique";
		}

		return uniqueString;
	}

	private String getUniqueString(SpaceshipType  spaceshipType){
		String uniqueString = "";

		if(spaceshipType.isPlayerUnique()){
			uniqueString = "Player unique";
		}else
		if(spaceshipType.isFactionUnique()){
			uniqueString = "Faction unique";
		}else
		if(spaceshipType.isWorldUnique()){
			uniqueString = "World unique";
		}

		return uniqueString;
	}

	// ska returnera en lista med alla skeppstyper det finns byggorder på för currentBuilding.
	private List<SpaceshipType> getAllShipBuilds(Building currentBuilding){
		List<SpaceshipType> allsst = new ArrayList<>();
		for (Expense expense : player.getOrders().getExpenses()){
			if (ExpensePureFunction.isBuildingBuildingShip(expense, currentBuilding)){
				allsst.add(PlayerPureFunctions.findOwnSpaceshipType(expense.getSpaceshipTypeUuid(), player, player.getGalaxy()));
			}
		}
		return allsst;
	}

	public List<TroopType> getAllTroopBuilds(Building currentBuilding){
		Vector<TroopType> alltp = new Vector<TroopType>();
		for (Expense expense : player.getOrders().getExpenses()){
			if (ExpensePureFunction.isBuildingBuildingTroop(expense, currentBuilding)){
				TroopType aTroopType = PlayerPureFunctions.findOwnTroopType(expense.getTroopTypeUuid(), player, player.getGalaxy());
				alltp.addElement(aTroopType);
			}
		}
		return alltp;
	}

	private void showTroopTypeChoices(Building currentBuilding) {
		Orders playersOrders = player.getOrders();
		// kolla först om det finns en engineer vid planeten
		// boolean engineer = false;
		VIP tempBuild;

		// visa bara den översta choicen om den är satt till upgrade
		// TODO (Tobbe) Fixa så att vald upgrade byggnad blir byggd
		// (MinBuildingPanel.java) och visas

		int index = 0;
		boolean showUpgrade = true; // skall endast visas i den översta choicen
		System.out.println(
				"playersOrders.alreadyUpgrading(currentBuilding): " + ExpensePureFunction.alreadyUpgrading(playersOrders, currentBuilding));
		if (ExpensePureFunction.alreadyUpgrading(playersOrders, currentBuilding)) {
			BuildingType buildingType = BuildingPureFunctions.getUpgradeBuilding(currentBuilding, player, playersOrders.getExpenses());
			tempBuild = VipPureFunctions.findVIPBuildingBuildBonus(currentBuilding.getLocation(), player,
					player.getOrders(), player.getGalaxy());
			int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getBuildingBuildBonus();
			int cost = BuildingPureFunctions.getBuildCost(buildingType, vipBuildBonus);
			addTroopTypes(troopTypeChoice[index], showUpgrade);
			troopTypeChoice[0].setSelectedItem(
					buildingType.getName() + " (cost: " + cost + ") " + getUniqueString(buildingType));
			troopTypeChoice[0].setVisible(true);
			detailsUpgrade.setVisible(true);
		} else {
			System.out.println("hämtar alla trupper som håller på att byggas: ");
			List<TroopType> buildTroopType = getAllTroopBuilds(currentBuilding);

			if (buildTroopType.size() > 0) {
				buttonsTroopsUpgrade = new SRButton[buildTroopType.size()];
			}

			System.out.println("buildTroopType.size(): " + buildTroopType.size());
			int slotsleft = currentBuilding.getTroopSize();

			while (index < buildTroopType.size()) {

				TroopType troopType = buildTroopType.get(index);
				System.out.println("troopType: " + troopType.getName());
				addTroopTypes(troopTypeChoice[index], showUpgrade);
				if (showUpgrade) { // sätt till false ss att endast den första choicen visar upgrade
					showUpgrade = false;
				}
				// beräkna hur många slots det finns kvar efter detta skeppsbygge
				slotsleft = slotsleft - 1;
				// System.out.println("slotsleft: " + slotsleft);
				// compute cost

				tempBuild = VipPureFunctions.findVIPTroopBuildBonus(currentBuilding.getLocation(), player,
						player.getOrders(), player.getGalaxy());
				int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getTroopBuildBonus();
				int cost = TroopPureFunctions.getCostBuild(troopType, vipBuildBonus);
				// set selected
				troopTypeChoice[index].setSelectedItem(
						troopType.getName() + " (cost: " + cost + ") " + getUniqueString(troopType));
				troopTypeChoice[index].setVisible(true);

				// show detailsButton
				buttonsTroopsUpgrade[index] = new SRButton("?");
				buttonsTroopsUpgrade[index].setBounds(301, 280 + (index * 20), 20, 20);
				buttonsTroopsUpgrade[index].addActionListener(this);
				buttonsTroopsUpgrade[index].setVisible(true);
				buttonsTroopsUpgrade[index].setToolTipText("Click for more details information about the troop");
				add(buttonsTroopsUpgrade[index]);

				index++;
			}
			// om det finns slots kvar visa en till choice som inte har något valt
			// alternativ
			if (slotsleft > 0) {

				addTroopTypes(troopTypeChoice[index], showUpgrade);
				troopTypeChoice[index].setVisible(true);
			}
		}

	}

	private String getUniqueString(TroopType troopType){
		String uniqueString = "";

		if(troopType.isPlayerUnique()){
			uniqueString = "Player unique";
		}else
		if(troopType.isFactionUnique()){
			uniqueString = "Faction unique";
		}else
		if(troopType.isWorldUnique()){
			uniqueString = "World unique";
		}

		return uniqueString;
	}

	private void showVIPTypeChoices(Building currentBuilding) {
		Orders playersOrders = player.getOrders();
		String vipTypeUuid = ExpensePureFunction.getVIPBuild(playersOrders, currentBuilding);

		VIPTypeChoice[0].addItem("None");

		boolean underSiege = currentBuilding.getLocation().isBesieged()
				&& currentBuildingType.isInOrbit();
		if (!underSiege) {
			addUpgradeBuildTypes(VIPTypeChoice[0], true);
		}

		List<VIPType> copyAllTypes = currentBuildingType.getVipTypes().stream().map(vipUuid -> VipPureFunctions.getVipTypeByUuid(vipUuid, player.getGalaxy().getGameWorld())).toList();
		Collections.sort(copyAllTypes, new VIPTypeComparator());
		Collections.reverse(copyAllTypes);

		// System.out.println("efter alltypes");

		if (VIPTypeChoice[0].getItemCount() > 1 && copyAllTypes.size() > 0) {
			VIPTypeChoice[0].addItem(getItemDescription("VIPs"));
		}

		VIPType toBuild = null;
		for (VIPType tempVIP : copyAllTypes) {
			VIPType vipToBuild = getVIPType(tempVIP.getName());
			if (vipToBuild != null) {
				int cost = vipToBuild.getBuildCost();
				VIPTypeChoice[0]
						.addItem(vipToBuild.getName() + " (cost: " + cost + ") " + getUniqueString(vipToBuild));

				if (vipToBuild.getUuid().equalsIgnoreCase(vipTypeUuid)) {
					toBuild = vipToBuild;
				}
			}
		}

		if (ExpensePureFunction.alreadyUpgrading(playersOrders, currentBuilding)) {
			BuildingType buildingType = BuildingPureFunctions.getUpgradeBuilding(currentBuilding, player, playersOrders.getExpenses());
			VIP tempBuild = VipPureFunctions.findVIPBuildingBuildBonus(currentBuilding.getLocation(), player,
					player.getOrders(), player.getGalaxy());
			int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getTroopBuildBonus();
			int cost = BuildingPureFunctions.getBuildCost(buildingType, vipBuildBonus);
			VIPTypeChoice[0].setSelectedItem(
					buildingType.getName() + " (cost: " + cost + ") " + getUniqueString(buildingType));
			VIPTypeChoice[0].setVisible(true);
			detailsUpgrade.setVisible(true);
		} else if (vipTypeUuid != null && !vipTypeUuid.equalsIgnoreCase("")) {
			VIPTypeChoice[0].setSelectedItem(toBuild.getName() + " (cost: " + toBuild.getBuildCost() + ") "
					+ getUniqueString(toBuild));
			VIPTypeChoice[0].setVisible(true);
			detailsVIP.setVisible(true);
		}
	}

	public String getUniqueString(VIPType vipType) {
		String uniqueString = "";

		if (vipType.isPlayerUnique()) {
			uniqueString = "Player unique";
		} else if (vipType.isFactionUnique()) {
			uniqueString = " Faction unique";
		} else if (vipType.isWorldUnique()) {
			uniqueString = "World unique";
		}

		return uniqueString;
	}

	private String getItemDescription(String text) {
		return "--------------------- " + text + " --------------------------------------";
	}

	private void showUpgradeBuildingTypeChoice(Building currentBuilding) {
		Orders playersOrders = player.getOrders();

		VIP tempBuild = VipPureFunctions.findVIPBuildingBuildBonus(currentBuilding.getLocation(), player,
				player.getOrders(), player.getGalaxy());

		upgradeBuildingTypeChoice[0].addItem("None");
		addUpgradeBuildTypes(upgradeBuildingTypeChoice[0], false);

		// System.out.println("efter alltypes");

		if (ExpensePureFunction.alreadyUpgrading(playersOrders, currentBuilding)) {
			BuildingType buildingType = BuildingPureFunctions.getUpgradeBuilding(currentBuilding, player, playersOrders.getExpenses());
			int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getBuildingBuildBonus();
			int cost = BuildingPureFunctions.getBuildCost(buildingType, vipBuildBonus);
			upgradeBuildingTypeChoice[0].setSelectedItem(
					buildingType.getName() + " (cost: " + cost + ") " + getUniqueString(buildingType));
			upgradeBuildingTypeChoice[0].setVisible(true);
			detailsUpgrade.setVisible(true);
		}
	}

	private void addUpgradeBuildTypes(ComboBoxPanel unitTypeChoice, boolean addDescriptionItem) {

		PlayerBuildingImprovement improvement = PlayerPureFunctions.findBuildingImprovementByUuid(currentBuildingType.getUuid(), player);
		List<BuildingType> upgradableBuildingTypes =
				BuildingPureFunctions.getUpgradableBuildingTypes(player.getGalaxy(), player, currentBuildingType, currentBuilding, aPlanet, improvement);
		if (!upgradableBuildingTypes.isEmpty()) {
			//TODO 2020-05-24 Why clone?
			List<BuildingType> allTypes = Functions
					.deepClone(upgradableBuildingTypes);
			Collections.sort(allTypes, new BuildingTypeBuildCostAndNameComparator());
			Collections.reverse(allTypes);

			VIP tempVIP = VipPureFunctions.findVIPBuildingBuildBonus(currentBuilding.getLocation(), player,
					player.getOrders(), player.getGalaxy());

			if (allTypes.size() > 0 && addDescriptionItem) {
				unitTypeChoice.addItem(getItemDescription("buildings"));
			}
			int vipBuildBonus = tempVIP == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempVIP.getTypeUuid(), player.getGalaxy().getGameWorld()).getBuildingBuildBonus();
			for (int i = 0; i < allTypes.size(); i++) {
				int cost = BuildingPureFunctions.getBuildCost(allTypes.get(i), vipBuildBonus);
				unitTypeChoice.addItem(
						allTypes.get(i).getName() + " (cost: " + cost + ") " + getUniqueString(allTypes.get(i)));
			}
		}

	}

	private void addShipTypes(ComboBoxPanel shiptypechoice, boolean showUpgrade, int slotsleft) {
		// System.out.println("addShipTypes " + showUpgrade + " " +
		// shiptypechoice.getItemCount());
		shiptypechoice.addItem("None");
		// System.out.println("efter none");
		// kolla först om det finns en engineer vid planeten
		// boolean engineer = false;

		VIP tempBuild = VipPureFunctions.findVIPShipBuildBonus(currentBuilding.getLocation(), player,
				player.getOrders(), player.getGalaxy());
		// VIP tempUpgrade =
		// player.getGalaxy().findVIPUpgradeWharfBonus(currentBuilding.getLocation(),player,player.getOrders());
		boolean underSiege = currentBuilding.getLocation().isBesieged()
				&& currentBuildingType.isInOrbit();
		if (showUpgrade & !underSiege) {
			addUpgradeBuildTypes(shiptypechoice, true);
		}
		// System.out.println("efter upgrade");
		// Vector alltypes = player.getSpaceshipTypes(); Old
		List<SpaceshipType> alltypes = PlayerPureFunctions.getAvailableSpaceshipTypes(player.getGalaxy(), player);
		//List<SpaceshipType> alltypes = player.getAvailableSpaceshipTypes();
		//List<SpaceshipType> copyAllTypes = alltypes.stream().collect(Collectors.toList());
		Collections.sort(alltypes, new SpaceshipTypeSizeComparator());
		// Collections.reverse(copyAllTypes);
		// System.out.println("efter alltypes");
		String shipSize = "";
		for (SpaceshipType tempsst : alltypes) {
			if (tempsst.getSize().getSlots() <= slotsleft) {
				int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getShipBuildBonus();
				int cost = SpaceshipPureFunctions.getBuildCost(tempsst, vipBuildBonus);

				if (!tempsst.getSize().getDescription().equals(shipSize)) {
					shipSize = tempsst.getSize().getDescription();
					shiptypechoice.addItem(getItemDescription(shipSize));
				}
				shiptypechoice.addItem(tempsst.getName() + " (cost: " + cost + ") " + getUniqueString(tempsst));
			}
		}
		// dumpdata(shiptypechoice);
	}

	private void addTroopTypes(ComboBoxPanel trooptypechoice, boolean showUpgrade) {
		// System.out.println("addShipTypes " + showUpgrade + " " +
		// shiptypechoice.getItemCount());
		trooptypechoice.addItem("None");
		// System.out.println("efter none");

		// VIP tempBuild =
		// player.getGalaxy().findVIPBuildBuildingBonus(currentBuilding.getLocation(),player,player.getOrders());
		boolean underSiege = currentBuilding.getLocation().isBesieged()
				&& currentBuildingType.isInOrbit();
		if (showUpgrade & !underSiege) {
			addUpgradeBuildTypes(trooptypechoice, true);
		}
		// System.out.println("efter upgrade");
		List<TroopType> alltypes = PlayerPureFunctions.getAvailableTroopTypes(player.getGalaxy(), player);
		Logger.finer("player.getAvailableTroopTypes().size(): " + alltypes.size());
		List<TroopType> copyAllTypes = alltypes.stream().collect(Collectors.toList());
		Collections.sort(copyAllTypes, new TroopTypeComparator());
		Collections.reverse(copyAllTypes);

		VIP tempBuild = VipPureFunctions.findVIPTroopBuildBonus(currentBuilding.getLocation(), player,
				player.getOrders(), player.getGalaxy());

		if (trooptypechoice.getItemCount() > 1 && copyAllTypes.size() > 0) {
			trooptypechoice.addItem(getItemDescription("Troops"));
		}

		// System.out.println("efter alltypes");
		for (TroopType tempTP : copyAllTypes) {
			if (canBuildTypeOfTroop(currentBuildingType, tempTP.getTypeOfTroop())) {
				int vipBuildBonus = tempBuild == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempBuild.getTypeUuid(), player.getGalaxy().getGameWorld()).getTroopBuildBonus();
				int cost =TroopPureFunctions.getCostBuild(tempTP, vipBuildBonus);
				trooptypechoice.addItem(tempTP.getName() + " (cost: " + cost + ") " + getUniqueString(tempTP));
			}
		}
	}

	private boolean canBuildTypeOfTroop(BuildingType buildingType, TypeOfTroop typeOfTroop){

		for(int i=0; i < buildingType.getTypeOfTroop().size();i++){
			if(buildingType.getTypeOfTroop().get(i) == typeOfTroop){
				return true;
			}
		}
		return false;
	}

	private SpaceshipType getShipType(String typeName) {
		List<SpaceshipType> allShipTypes = PlayerPureFunctions.getAvailableSpaceshipTypes(player.getGalaxy(), player);
		return allShipTypes.stream().filter(ship -> ship.getName().equalsIgnoreCase(typeName)).findFirst().orElse(null);
	}

	private VIPType getVIPType(String typeName) {
		String aTypeName = typeName;
		// find shiptype
		VIPType vipType = null;
		// Vector allshiptypes = player.getSpaceshipTypes(); old
		List<VIPType> allVIPTypes = player.getGalaxy().getGameWorld().getVipTypes();
		int i = 0;
		while (vipType == null && i < allVIPTypes.size()) {
			VIPType temp = allVIPTypes.get(i);
			if (temp.getName().equalsIgnoreCase(aTypeName) && (VipPureFunctions.isConstructable(player, player.getGalaxy(), temp)
					|| aTypeName.equalsIgnoreCase(VipPureFunctions.getVipTypeByUuid(ExpensePureFunction.getVIPBuild(player.getOrders(), currentBuilding), player.getGalaxy().getGameWorld()).getName()))) {
				vipType = temp;
			}
			i++;
		}
		return vipType;
	}

	private void fillNewBuildingsChoice() {
		buildnewBuildingChoice.removeAllItems();
		VIP tempEngineer = VipPureFunctions.findVIPBuildingBuildBonus(aPlanet, player, player.getOrders(), player.getGalaxy());

		List<BuildingType> tempBuildingTypes = BuildingPureFunctions.getAvailableBuildingsToConstruct(player.getGalaxy(), player, aPlanet);
		buildnewBuildingChoice.addItem("None");
		for (BuildingType buildingType : tempBuildingTypes) {
			int vipBuildBonus = tempEngineer == null ? 0 : VipPureFunctions.getVipTypeByUuid(tempEngineer.getTypeUuid(), player.getGalaxy().getGameWorld()).getBuildingBuildBonus();
			int cost = BuildingPureFunctions.getBuildCost(buildingType, vipBuildBonus);
			buildnewBuildingChoice.addItem(buildingType.getName() + " (cost: " + cost + ")");
		}

	}

	public void updateData() {
		if (currentBuilding != null) {
			showBuilding(buildingList.getSelectedIndex());
		}
	}
}
