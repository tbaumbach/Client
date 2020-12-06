package spaceraze.client.game.panels.diplomacy;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JPanel;

import spaceraze.client.ColorConverter;
import spaceraze.client.GeneralConfirmPopupPanel;
import spaceraze.client.GeneralMessagePopupPanel;
import spaceraze.client.components.SRButton;
import spaceraze.client.components.SRLabel;
import spaceraze.servlethelper.game.DiplomacyPureFunctions;
import spaceraze.util.general.Logger;
import spaceraze.world.Player;
import spaceraze.world.diplomacy.Diplomacy;
import spaceraze.world.diplomacy.DiplomacyChange;
import spaceraze.world.diplomacy.DiplomacyLevel;
import spaceraze.world.diplomacy.DiplomacyOffer;
import spaceraze.world.diplomacy.DiplomacyState;
import spaceraze.world.enums.DiplomacyIconState;
import spaceraze.world.orders.Orders;

@SuppressWarnings("serial")
public class DiplomacyRow extends JPanel implements ActionListener, MouseListener {
	private DiplomacyState diplomacyState;
	private Player otherPlayer, thePlayer;
	private SRLabel playerLbl;
	private DiplomacyIcon eternalWarIcon, warIcon, ceaseFireIcon, peaceIcon, allianceIcon, confederacyIcon, lordIcon,
			vasallIcon;
	private DiplomacyPanel diplomacyPanel;
	private DiplomacyIcon clickedIcon;
	private boolean errorPopup; // used to distinguish between message and confirm popups

	public DiplomacyRow(DiplomacyState diplomacyState, Player otherPlayer, DiplomacyPanel diplomacyPanel,
			boolean activePlayer) {
		this.diplomacyState = diplomacyState;
		this.otherPlayer = otherPlayer;
		thePlayer = diplomacyState.getOtherPlayer(otherPlayer);
		this.diplomacyPanel = diplomacyPanel;

		setLayout(null);
		// setBackground(Color.GRAY);
		setBackground(Color.BLACK);
		setOpaque(true);

		playerLbl = new SRLabel(otherPlayer.getGovernorName() + " (" + otherPlayer.getFaction().getName() + ")");
		playerLbl.setBounds(0, 5, 190, 20);
		playerLbl.setForeground(ColorConverter.getColorFromHexString(otherPlayer.getFaction().getPlanetHexColor()));
		add(playerLbl);

		int x = 200;
		int width = 28;
		int height = width;
		int interval = width + 2;

		eternalWarIcon = new DiplomacyIcon(DiplomacyLevel.ETERNAL_WAR, activePlayer);
		eternalWarIcon.setBounds(x + (1 * interval), 2, height, height);
		eternalWarIcon.addActionListener(this);
		eternalWarIcon.addMouseListener(this);
		add(eternalWarIcon);

		warIcon = new DiplomacyIcon(DiplomacyLevel.WAR, activePlayer);
		warIcon.setBounds(x + (2 * interval), 2, height, height);
		warIcon.addActionListener(this);
		warIcon.addMouseListener(this);
		add(warIcon);

		ceaseFireIcon = new DiplomacyIcon(DiplomacyLevel.CEASE_FIRE, activePlayer);
		ceaseFireIcon.setBounds(x + (3 * interval), 2, height, height);
		ceaseFireIcon.addActionListener(this);
		ceaseFireIcon.addMouseListener(this);
		add(ceaseFireIcon);

		peaceIcon = new DiplomacyIcon(DiplomacyLevel.PEACE, activePlayer);
		peaceIcon.setBounds(x + (4 * interval), 2, height, height);
		peaceIcon.addActionListener(this);
		peaceIcon.addMouseListener(this);
		add(peaceIcon);

		allianceIcon = new DiplomacyIcon(DiplomacyLevel.ALLIANCE, activePlayer);
		allianceIcon.setBounds(x + (5 * interval), 2, height, height);
		allianceIcon.addActionListener(this);
		allianceIcon.addMouseListener(this);
		add(allianceIcon);

		confederacyIcon = new DiplomacyIcon(DiplomacyLevel.CONFEDERACY, activePlayer);
		confederacyIcon.setBounds(x + (6 * interval), 2, height, height);
		confederacyIcon.addActionListener(this);
		confederacyIcon.addMouseListener(this);
		add(confederacyIcon);

		lordIcon = new DiplomacyIcon(DiplomacyLevel.LORD, activePlayer);
		lordIcon.setBounds(x + 10 + (7 * interval), 2, height, height);
		lordIcon.addActionListener(this);
		lordIcon.addMouseListener(this);
		add(lordIcon);

		vasallIcon = new DiplomacyIcon(DiplomacyLevel.VASSAL, activePlayer);
		vasallIcon.setBounds(x + 10 + (8 * interval), 2, height, height);
		vasallIcon.addActionListener(this);
		vasallIcon.addMouseListener(this);
		add(vasallIcon);

		setIconStates();
	}

	private void setIconStates() {
		eternalWarIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState, DiplomacyLevel.ETERNAL_WAR, thePlayer));
		warIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.WAR, thePlayer));
		ceaseFireIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.CEASE_FIRE, thePlayer));
		peaceIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.PEACE, thePlayer));
		allianceIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.ALLIANCE, thePlayer));
		confederacyIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.CONFEDERACY, thePlayer));
		lordIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.LORD, thePlayer));
		vasallIcon.setState(DiplomacyPureFunctions.getIconState(diplomacyState,DiplomacyLevel.VASSAL, thePlayer));
	}

	public void orderChange() {
		Logger.fine("An order change have been confirmed");
		Orders orders = thePlayer.getOrders();
		DiplomacyLevel level = clickedIcon.getLevel();
		if (clickedIcon.getState() == DiplomacyIconState.ACTIVE) {
			Logger.fine("Ta bort gammal order");
			// ta bort gammal order
			removeDiplomacyOrder(otherPlayer, orders);
			diplomacyPanel.showDiplomacy();
		} else if (clickedIcon.getState() == DiplomacyIconState.PASSIVE_AND_SELECTED) {
			Logger.fine("Ta bort offer");
			// �ngra sitt f�rslag
			removeDiplomacyOrder(otherPlayer, orders);
			diplomacyPanel.showDiplomacy();
		} else if (clickedIcon.getState() == DiplomacyIconState.PASSIVE_AND_SELECTED_AND_SUGGESTED) {
			Logger.fine("�ngra accept p� offer");
			// �ngra accepterande av offer fr�n den andra spelaren
			removeDiplomacyOrder(otherPlayer, orders);
			diplomacyPanel.showDiplomacy();
		} else if (clickedIcon.getState() == DiplomacyIconState.PASSIVE_AND_SUGGESTED) {
			if (level == DiplomacyLevel.LORD) {
				Logger.fine("Acceptera att du blir lord");
				// acceptera att du blir lord
				DiplomacyChange newDiplomacyChange = new DiplomacyChange(thePlayer, otherPlayer, DiplomacyLevel.VASSAL,
						true);
				orders.addDiplomacyChange(newDiplomacyChange);
			} else if (level == DiplomacyLevel.VASSAL) {
				Logger.fine("Acceptera att du blir vasall");
				// acceptera att du blir Lord
				DiplomacyChange newDiplomacyChange = new DiplomacyChange(thePlayer, otherPlayer, DiplomacyLevel.LORD,
						true);
				orders.addDiplomacyChange(newDiplomacyChange);
			} else if (clickedIcon.getLevel() == DiplomacyLevel.CONFEDERACY) {
				List<Player> confPlayers = DiplomacyPureFunctions.getConfederacyPlayers(otherPlayer, thePlayer.getGalaxy());
				if (confPlayers.size() == 0) { // make offer to a single player
					Logger.fine("Acceptera ett offer: " + level.getName());
					// acceptera ett offer
					DiplomacyChange newDiplomacyChange = new DiplomacyChange(thePlayer, otherPlayer, level, true);
					orders.addDiplomacyChange(newDiplomacyChange);
				} else { // otherPlayer is in conf with at least one other players
					confPlayers.add(otherPlayer);
					for (Player aConfPlayer : confPlayers) {
						DiplomacyChange newDiplomacyChange = new DiplomacyChange(thePlayer, aConfPlayer, level, true);
						orders.addDiplomacyChange(newDiplomacyChange);
					}
					diplomacyPanel.showDiplomacy();
				}
			} else {
				Logger.fine("Acceptera ett offer: " + level.getName());
				// acceptera ett offer
				DiplomacyChange newDiplomacyChange = new DiplomacyChange(thePlayer, otherPlayer, level, true);
				orders.addDiplomacyChange(newDiplomacyChange);
			}
			// �ndra icon state till selected
		} else { // must be DiplomacyIconState.PASSIVE
					// skapa ett nytt offer eller change
			if (level == DiplomacyLevel.LORD) {
				Logger.fine("F�resl� att du blir lord");
				// ta bort ev. gammal order
				removeDiplomacyOrder(otherPlayer, orders);
				// f�resl� att du blir Lord
				DiplomacyOffer newDiplomacyOffer = new DiplomacyOffer(thePlayer, otherPlayer, DiplomacyLevel.VASSAL);
				orders.addDiplomacyOffer(newDiplomacyOffer);
			} else if (level == DiplomacyLevel.VASSAL) {
				Logger.fine("F�resl� att du blir vasall");
				// ta bort ev. gammal order
				removeDiplomacyOrder(otherPlayer, orders);
				// f�resl� att du blir vasall
				DiplomacyOffer newDiplomacyOffer = new DiplomacyOffer(thePlayer, otherPlayer, DiplomacyLevel.LORD);
				orders.addDiplomacyOffer(newDiplomacyOffer);
			} else if (level.isAdjacent(diplomacyState.getCurrentLevel())
					& level.isHigher(diplomacyState.getCurrentLevel())) {
				Logger.fine("F�resl� en change");
				// ta bort ev. gammal order
				removeDiplomacyOrder(otherPlayer, orders);
				// if confederacy, check if more than 1 player i confederacy
				if (level == DiplomacyLevel.CONFEDERACY) {
					if (DiplomacyPureFunctions.checkAllianceWithAllInConfederacy(thePlayer, otherPlayer, thePlayer.getGalaxy())) {
						List<Player> confPlayers = DiplomacyPureFunctions.getConfederacyPlayers(otherPlayer, thePlayer.getGalaxy());
						if (confPlayers.size() == 0) { // make offer to a single player
							// F�resl� en change
							DiplomacyOffer newDiplomacyOffer = new DiplomacyOffer(thePlayer, otherPlayer, level);
							orders.addDiplomacyOffer(newDiplomacyOffer);
						} else { // make offers to all members of the confederacy
							confPlayers.add(otherPlayer);
							for (Player aConfPlayer : confPlayers) {
								DiplomacyOffer newDiplomacyOffer = new DiplomacyOffer(thePlayer, aConfPlayer, level);
								orders.addDiplomacyOffer(newDiplomacyOffer);
							}
							diplomacyPanel.showDiplomacy();
						}
					}
				} else { // not confederacy
							// F�resl� en change
					DiplomacyOffer newDiplomacyOffer = new DiplomacyOffer(thePlayer, otherPlayer, level);
					orders.addDiplomacyOffer(newDiplomacyOffer);
				}
			} else if (level.isAdjacent(diplomacyState.getCurrentLevel())
					& !level.isHigher(diplomacyState.getCurrentLevel())) {
				Logger.fine("skapa ett nytt change (ned�t)");
				// ta bort ev. gammal order
				removeDiplomacyOrder(otherPlayer, orders);
				// skapa ett nytt change (ned�t)
				DiplomacyChange newDiplomacyChange = new DiplomacyChange(thePlayer, otherPlayer, level, false);
				orders.addDiplomacyChange(newDiplomacyChange);
			}
		}
		clickedIcon = null;
		setIconStates();
	}

	public void actionPerformed(ActionEvent ae) {
		Logger.fine("actionPerformed");
		// check and maybe set an order for change or suggested change of diplomacy
		// state
		// if order is created set new states for icons
		if (errorPopup) {
			errorPopup = false;
		} else if ((ae.getSource() instanceof SRButton) & (ae.getActionCommand().equalsIgnoreCase("ok"))) { // anv. har
																											// tryckt ok
																											// i
																											// confirmpopup
			orderChange();
		} else if (ae.getSource() instanceof DiplomacyIcon) {
			if (diplomacyPanel.getPlayer() == thePlayer) {
				clickedIcon = (DiplomacyIcon) ae.getSource();
				Logger.fine("clickedIcon.getState(): " + clickedIcon.getState().toString());
				if (clickedIcon.getState() == DiplomacyIconState.ACTIVE) {
					Logger.fine("Active");
					if (thePlayer.getOrders().diplomacyOrderExist(otherPlayer)) {
						Logger.fine("Order exist");
						// ta ev. bort f�reslaget offer
						GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("Remove diplomatic action?", this,
								"Do you want to remove the existing diplomacy order to Govenor "
										+ otherPlayer.getGovernorName() + "?");
						popup.setPopupSize(450, 110);
						popup.open(this);
					}
				} else if (clickedIcon.getState() == DiplomacyIconState.DISABLED) {
					// g�r inget
				} else if (clickedIcon.getState() == DiplomacyIconState.PASSIVE_AND_SELECTED) {
					// �ppna skapa ny offer dialog s� man kan �ngra sitt f�rslag
					GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("Remove diplomatic offer?", this,
							"Do you want to remove the existing diplomacy offer to Govenor "
									+ otherPlayer.getGovernorName() + "?");
					popup.setPopupSize(450, 110);
					popup.open(this);
				} else if (clickedIcon.getState() == DiplomacyIconState.PASSIVE_AND_SELECTED_AND_SUGGESTED) {
					// �ppna reply to offer dialog s� man kan �ndra texten p� sitt accepterande
					GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("Remove diplomatic acceptance?", this,
							"Do you want to remove the existing acceptance to Govenor " + otherPlayer.getGovernorName()
									+ "'s offer?");
					popup.setPopupSize(450, 110);
					popup.open(this);
				} else if (clickedIcon.getState() == DiplomacyIconState.PASSIVE_AND_SUGGESTED) {
					if (clickedIcon.getLevel() == DiplomacyLevel.LORD) {
						Orders orders = thePlayer.getOrders();
						if (orders.checkDiplomacyConfVassal(otherPlayer)) {
							// create message that action cannot be performed
							errorPopup = true;
							GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel("Conflicting offer exist",
									this, "You cannot create offer to " + otherPlayer.getGovernorName()
											+ " since you already have an offer for confederacy/vassal to a player");
							popup.setPopupSize(650, 110);
							popup.open(this);
						} else {
							// �ppna reply to offer dialogen att den andra blir vasall (och du blir lord)
							GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("Accept Lordship?", this,
									"Do you want to accept that Govenor " + otherPlayer.getGovernorName()
											+ " become your vassal and you his lord?");
							popup.setPopupSize(450, 110);
							popup.open(this);
						}
					} else if (clickedIcon.getLevel() == DiplomacyLevel.VASSAL) {
						Orders orders = thePlayer.getOrders();
						if (orders.checkDiplomacyConfLordVassal(otherPlayer)) {
							// create message that action cannot be performed
							errorPopup = true;
							GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel("Conflicting offer exist",
									this, "You cannot create offer to " + otherPlayer.getGovernorName()
											+ " since you already have an offer for confederacy/vassal/lord to a player");
							popup.setPopupSize(650, 110);
							popup.open(this);
						} else {
							// �ppna reply to offer dialogen att du blir vasall
							GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("Accept Vassalship?", this,
									"Do you want to accept that Govenor " + otherPlayer.getGovernorName()
											+ " become your lord and you a vassal?");
							popup.setPopupSize(450, 110);
							popup.open(this);
						}
					} else if (clickedIcon.getLevel() == DiplomacyLevel.CONFEDERACY) {
						Orders orders = thePlayer.getOrders();
						if (orders.checkDiplomacyConfLordVassal(otherPlayer)) {
							// create message that action cannot be performed
							errorPopup = true;
							GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel("Conflicting offer exist",
									this, "You cannot create offer to " + otherPlayer.getGovernorName()
											+ " since you already have an offer for confederacy/vassal/lord to a player");
							popup.setPopupSize(650, 110);
							popup.open(this);
						} else {
							List<Player> confPlayers = DiplomacyPureFunctions.getConfederacyPlayers(otherPlayer, thePlayer.getGalaxy());
							if (confPlayers.size() == 0) { // make offer to a single player
								// �ppna reply to offer dialogen
								GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel(
										"Accept diplomatic offer?", this,
										"Do you want to accept Govenor " + otherPlayer.getGovernorName()
												+ "'s offer for " + clickedIcon.getLevel().getName() + "?");
								popup.setPopupSize(450, 110);
								popup.open(this);
							} else { // otherPlayer is in conf with at least one other players
										// skapa ett nytt offer
								GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel(
										"Accept multiple diplomatic offer?", this,
										"Do you want to accept the offer to join the " + (confPlayers.size() + 1)
												+ "-player confederacy?");
								popup.setPopupSize(450, 110);
								popup.open(this);
							}
						}
					} else {
						// �ppna reply to offer dialogen
						GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("Accept diplomatic offer?", this,
								"Do you want to accept Govenor " + otherPlayer.getGovernorName() + "'s offer for "
										+ clickedIcon.getLevel().getName() + "?");
						popup.setPopupSize(450, 110);
						popup.open(this);
					}
					// �ndra icon state till selected
				} else { // must be DiplomacyIconState.PASSIVE
							// skapa ett nytt offer eller change
					if (clickedIcon.getLevel().isAdjacent(diplomacyState.getCurrentLevel())
							& clickedIcon.getLevel().isHigher(diplomacyState.getCurrentLevel())) {
						if (clickedIcon.getLevel() == DiplomacyLevel.CONFEDERACY) {
							Orders orders = thePlayer.getOrders();
							Logger.fine("check if can conf?");
							if (orders.checkDiplomacyConfLordVassal(otherPlayer)) {
								// create message that action cannot be performed
								errorPopup = true;
								GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel("Conflicting offer exist",
										this, "You cannot create offer to " + otherPlayer.getGovernorName()
												+ " since you already have an offer for confederacy/vassal/lord to a player");
								popup.setPopupSize(650, 110);
								popup.open(this);
							} else {
								if (DiplomacyPureFunctions.checkAllianceWithAllInConfederacy(thePlayer, otherPlayer, thePlayer.getGalaxy())) {
									List<Player> confPlayers = DiplomacyPureFunctions.getConfederacyPlayers(otherPlayer, thePlayer.getGalaxy());
									if (confPlayers.size() == 0) { // make offer to a single player
										// skapa ett nytt offer
										GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel(
												"New diplomatic offer?", this,
												"Do you want to propose " + clickedIcon.getLevel().getName()
														+ " to Govenor " + otherPlayer.getGovernorName() + "?");
										popup.setPopupSize(400, 110);
										popup.open(this);
									} else { // otherPlayer is in conf with at least one other players
												// skapa ett nytt offer
										GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel(
												"New multiple diplomatic offer?", this,
												"Do you want to propose " + clickedIcon.getLevel().getName()
														+ " to all Govenors in the confederacy?");
										popup.setPopupSize(450, 110);
										popup.open(this);
									}
								} else { // not in alliance with all members of confederacy
									String[] messageRows = {
											"To be able to make an offer to join a confederacy, you must",
											"already be in alliance with all members of that confederacy" };
									GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel(
											"Prerequisites not met", this, 400, messageRows);
									popup.open(this);
								}
							}
						} else {
							// skapa ett nytt offer
							GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("New diplomatic offer?", this,
									"Do you want to propose " + clickedIcon.getLevel().getName() + " to Govenor "
											+ otherPlayer.getGovernorName() + "?");
							popup.setPopupSize(400, 110);
							popup.open(this);
						}
					} else if (clickedIcon.getLevel().isAdjacent(diplomacyState.getCurrentLevel())
							& !clickedIcon.getLevel().isHigher(diplomacyState.getCurrentLevel())) {
						// skapa ett nytt change
						GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("New diplomatic change?", this,
								"Do you want to change to " + clickedIcon.getLevel().getName() + " towards Govenor "
										+ otherPlayer.getGovernorName() + "?");
						popup.setPopupSize(400, 110);
						popup.open(this);
					} else if (clickedIcon.getLevel() == DiplomacyLevel.LORD) {
						Orders orders = thePlayer.getOrders();
						if (orders.checkDiplomacyConfVassal(otherPlayer)) {
							// create message that action cannot be performed
							errorPopup = true;
							GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel("Conflicting offer exist",
									this, "You cannot create offer to " + otherPlayer.getGovernorName()
											+ " since you already have an offer for confederacy/vassal to a player");
							popup.setPopupSize(650, 110);
							popup.open(this);
						} else {
							// skapa ett nytt offer att den andra blir vassall
							GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("New diplomatic offer?", this,
									"Do you want to propose that Govenor " + otherPlayer.getGovernorName()
											+ " admit his defeat and become your vassal?");
							popup.setPopupSize(550, 110);
							popup.open(this);
						}
					} else if (clickedIcon.getLevel() == DiplomacyLevel.VASSAL) {
						// f�rst kolla om det inte redan finns offers f�r vassal, lord eller confederacy
						Orders orders = thePlayer.getOrders();
						if (orders.checkDiplomacyConfLordVassal(otherPlayer)) {
							// create message that action cannot be performed
							errorPopup = true;
							GeneralMessagePopupPanel popup = new GeneralMessagePopupPanel("Conflicting offer exist",
									this, "You cannot create offer to " + otherPlayer.getGovernorName()
											+ " since you already have an offer for confederacy/vassal/lord to a player");
							popup.setPopupSize(650, 110);
							popup.open(this);
						} else {
							// skapa ett nytt offer att den andra blir lord
							GeneralConfirmPopupPanel popup = new GeneralConfirmPopupPanel("New diplomatic offer?", this,
									"Do you want to propose to Govenor " + otherPlayer.getGovernorName()
											+ " that you become his vassal?");
							popup.setPopupSize(450, 110);
							popup.open(this);
						}
					}
				}
			}
		}
	}

	public void mouseEntered(MouseEvent me) {
		DiplomacyIcon aDiplomacyIcon = (DiplomacyIcon) me.getSource();
		diplomacyPanel.showLevelInfo(aDiplomacyIcon.getLevel());
	}

	public void mouseExited(MouseEvent me) {
		diplomacyPanel.hideLevelInfo();
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	private static void removeDiplomacyOrder(Player otherPlayer, Orders orders) {
		DiplomacyOffer tmpOffer = orders.findDiplomacyOffer(otherPlayer);
		if (tmpOffer != null) {
			if (tmpOffer.getSuggestedLevel() == DiplomacyLevel.CONFEDERACY) {
				// check if otherPlayer is in a confederacy
				List<Player> confPlayers = DiplomacyPureFunctions.getConfederacyPlayers(otherPlayer, otherPlayer.getGalaxy());
				if (confPlayers.size() > 0) {
					for (Player confPlayer : confPlayers) {
						DiplomacyOffer confOffer = orders.findDiplomacyOffer(confPlayer);
						Logger.fine("confOffer: " + confOffer.toString());
						orders.getDiplomacyOffers().remove(confOffer);
					}
				}
			}
			Logger.finest("tmpOffer: " + tmpOffer.toString());
			orders.getDiplomacyOffers().remove(tmpOffer);
		} else {
			DiplomacyChange tmpChange = orders.findDiplomacyChange(otherPlayer);
			if (tmpChange != null) {
				if (tmpChange.getNewLevel() == DiplomacyLevel.CONFEDERACY) {
					// check if otherPlayer is in a confederacy
					List<Player> confPlayers = DiplomacyPureFunctions.getConfederacyPlayers(otherPlayer, otherPlayer.getGalaxy());
					if (confPlayers.size() > 0) {
						for (Player confPlayer : confPlayers) {
							DiplomacyChange confChange = orders.findDiplomacyChange(confPlayer);
							Logger.finest("confChange: " + confChange.toString());
							orders.getDiplomacyChanges().remove(confChange);
						}
					}
				}
				Logger.finest("tmpChange: " + tmpChange.toString());
				orders.getDiplomacyChanges().remove(tmpChange);
			}
		}
	}

}
