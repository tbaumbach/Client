package spaceraze.client.game.panels.gift;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRButton;
import spaceraze.client.components.scrollable.ListPanel;
import spaceraze.client.game.SpaceRazePanel;
import spaceraze.client.interfaces.SRUpdateablePanel;
import spaceraze.servlethelper.handlers.GameWorldHandler;
import spaceraze.util.general.Logger;
import spaceraze.world.Player;

/**
 * @author Paul Bodin
 * 
 *  New version of the giftpanel using a modal popup
 *  
 */

public class GiftPanel extends SRBasePanel implements ListSelectionListener, SRUpdateablePanel, ActionListener{
  private static final long serialVersionUID = 1L;
  private SRButton newButton,editButton,deleteButton;
  private String id;
  private Player p;
  private SpaceRazePanel client;
  private ListPanel allGiftsList;
  private GiftPopupPanel popupGift;
  private List<Player> otherPlayers;
  private String lastButton;

  public GiftPanel(Player p,SpaceRazePanel client, String id) {
    this.p = p;
    this.client = client;
    this.id = id;

    otherPlayers = new ArrayList<Player>();

    allGiftsList = new ListPanel();
    allGiftsList.setBounds(10, 10, 250, 350);
    allGiftsList.setListSelectionListener(this);
    addPlayers(p.getGalaxy().getPlayers());
    allGiftsList.updateScrollList();
    add(allGiftsList);

    newButton = new SRButton("New Gift");
    newButton.setBounds(280,10,100,20);
    newButton.addActionListener(this);
    add(newButton);
    newButton.setEnabled(false);

    editButton = new SRButton("Edit Gift");
    editButton.setBounds(280,40,100,20);
    editButton.addActionListener(this);
    add(editButton);
    editButton.setEnabled(false);

    deleteButton = new SRButton("Delete Gift");
    deleteButton.setBounds(280,70,100,20);
    deleteButton.addActionListener(this);
    add(deleteButton);
    deleteButton.setEnabled(false);
  }

  private void addPlayers(List<Player> players){
	  DefaultListModel dlm = allGiftsList.getModel();
	  for (int i = 0; i < players.size(); i++){
		  Player tempPlayer = (Player)players.get(i);
		  if (tempPlayer != p){ // can not give money to himself
			  int giftSum = p.getOrders().findGift(tempPlayer);
			  String rowText = tempPlayer.getGovernorName() + " (" + GameWorldHandler.getFactionByUuid(tempPlayer.getFactionUuid(), p.getGalaxy().getGameWorld()).getName() + ")";
			  if (giftSum > 0){
				  rowText += ", give:" + giftSum;
			  }
			  dlm.addElement(rowText);
			  otherPlayers.add(tempPlayer);
		  }
	  }
  }
  
  public void valueChanged(ListSelectionEvent lse){
	  showButtons(allGiftsList.getSelectedIndex());
  }
  
  private void showButtons(int index){
	  Logger.fine("showButtons: " + index);
	  Player giveToPlayer = (Player)otherPlayers.get(index);
	  Logger.fine("Player found: " + giveToPlayer.getGovernorName());
	  int amount = p.getOrders().findGift(giveToPlayer);
	  if (amount > 0){
		  // if amount is > 0, gift exists
		  newButton.setEnabled(false);
		  editButton.setEnabled(true);
		  deleteButton.setEnabled(true);
	  }else{
		  // if amount is = 0, no gift exists
		  newButton.setEnabled(true);
		  editButton.setEnabled(false);
		  deleteButton.setEnabled(false);
	  }
  }

  public void actionPerformed(ActionEvent ae){
  	String action = ae.getActionCommand();
  	if (action.equalsIgnoreCase("cancel")){
  	}else
    if ((lastButton != null) && (lastButton.equals("gift")) & action.equalsIgnoreCase("ok")){
    	// perform action
    	performPopupGiftAction();
  	}else
    if (action.equalsIgnoreCase("delete gift")){
    	deleteGift();
  	}else{
    	lastButton = "gift";
    	openPopupGift(action);
  	}
  }
  
  private void deleteGift(){
  	Logger.fine("deleteGift called");
  	Player giveToPlayer = (Player)otherPlayers.get(allGiftsList.getSelectedIndex());
  	// set gift to zero to remove it...
  	p.getOrders().addNewTransaction(0,giveToPlayer);
    client.updateTreasuryLabel();
  	updateGiftPanel();
  }

  private void performPopupGiftAction(){
	  Logger.fine("performPopupAction called");
	  Player giveToPlayer = (Player)otherPlayers.get(allGiftsList.getSelectedIndex());
	  p.getOrders().addNewTransaction(popupGift.getSum(),giveToPlayer);
	  client.updateTreasuryLabel();
	  updateGiftPanel();
  }

  private void updateGiftPanel(){
  	int index = allGiftsList.getSelectedIndex();
  	emptyList();
  	addPlayers(p.getGalaxy().getPlayers());
  	update(getGraphics());
  	allGiftsList.update(allGiftsList.getGraphics());
  	showButtons(index);
  }
  
  private void openPopupGift(String actionCommand){
  	Logger.fine("openPopup called: " + actionCommand);
    Player giveToPlayer = otherPlayers.get(allGiftsList.getSelectedIndex());

    if (actionCommand.equalsIgnoreCase("edit gift")){
    	int amount = p.getOrders().findGift(giveToPlayer);
    	popupGift = new GiftPopupPanel(actionCommand,this,giveToPlayer,amount);
    }else{
    	popupGift = new GiftPopupPanel(actionCommand,this,giveToPlayer);
    }
	popupGift.setPopupSize(400,170);
	popupGift.open(this);
	popupGift.setSumfieldFocus();
  }

  private void emptyList(){
    DefaultListModel dlm = allGiftsList.getModel();
    dlm.clear();
    otherPlayers = new ArrayList<>();
  }

  public String getId(){
    return id;
  }

  public void updateData(){
  }

}