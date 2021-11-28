//Title:        SpaceRaze Client
//Author:       Paul Bodin
//Description:  Java-klienten f�r SpazeRaze.

package spaceraze.client.game.panels.orders;

import java.util.List;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.scrollable.TextAreaPanel;
import spaceraze.client.game.panels.resource.VIPsPanel;
import spaceraze.client.interfaces.SRUpdateablePanel;
import spaceraze.servlethelper.game.BuildingPureFunctions;
import spaceraze.servlethelper.game.expenses.ExpensePureFunction;
import spaceraze.servlethelper.game.spaceship.SpaceshipPureFunctions;
import spaceraze.servlethelper.game.troop.TroopPureFunctions;
import spaceraze.servlethelper.game.vip.VipPureFunctions;
import spaceraze.world.*;
import spaceraze.world.orders.Expense;
import spaceraze.world.orders.Orders;
import spaceraze.world.orders.PlanetNotesChange;
import spaceraze.world.orders.ResearchOrder;
import spaceraze.world.orders.ShipMovement;
import spaceraze.world.orders.ShipToCarrierMovement;
import spaceraze.world.orders.TroopToCarrierMovement;
import spaceraze.world.orders.TroopToPlanetMovement;
import spaceraze.world.orders.VIPMovement;

@SuppressWarnings("serial")
public class OrdersPanel extends SRBasePanel implements SRUpdateablePanel{
  private String id;
  private Orders orders;
  private TextAreaPanel infoarea;
//  private JScrollPane scrollPane;
  private SRLabel title;
  private Galaxy g;
  private Player aPlayer;
  private String sepLine = "----------------------\n";

  public OrdersPanel(Orders orders, String id, Player p) {
    this.id = id;
    this.orders = orders;
    this.setLayout(null);
    g = p.getGalaxy();
    this.aPlayer = p;

    title = new SRLabel("Current orders for turn " + p.getGalaxy().getTurn());
    title.setBounds(10,10,200,15);
    add(title);

    infoarea = new TextAreaPanel();
    infoarea.setBounds(10,35,835,590);
    add(infoarea);
    
  }

  public String getId(){
    return id;
  }

  public void updateData(){
    infoarea.setText("");
    // add expenses
    List<Expense> temp = orders.getExpenses();
    if (temp.size() > 0){
	    infoarea.append("Expenses\n");
	    infoarea.append(sepLine);
	    for (int i = 0; i < temp.size(); i++){
	      Expense tempExpense = temp.get(i);
	      infoarea.append(ExpensePureFunction.getText(g, ExpensePureFunction.getCost(tempExpense, g, aPlayer), tempExpense) + "\n");
	    }
	    infoarea.append("\n");
    }

    // add ship movements
	List<ShipMovement> shipMovements = orders.getShipMoves();
    if (shipMovements.size() > 0){
    	infoarea.append("Ship movements\n");
    	infoarea.append(sepLine);
    	for (int j = 0; j < shipMovements.size(); j++){
    		ShipMovement tempShipMovement = shipMovements.get(j);
    		infoarea.append(getText(tempShipMovement, g) + "\n");
    	}
    	List<ShipToCarrierMovement> stcmList = orders.getShipToCarrierMoves();
    	for (ShipToCarrierMovement movement : stcmList) {
    		infoarea.append(getText(movement, g) + "\n");
    	}
    	infoarea.append("\n");
    }

    if (g.hasTroops()){
    	List<TroopToCarrierMovement> ttcm = orders.getTroopToCarrierMoves();
    	List<TroopToPlanetMovement> ttpm = orders.getTroopToPlanetMoves();
        if ((ttcm.size() > 0) | (ttpm.size() > 0)){
        	// add troop movements
        	infoarea.append("Troop movements\n");
        	infoarea.append(sepLine);
        	for (TroopToCarrierMovement troopToCarrierMovement : ttcm) {
        		infoarea.append(getText(troopToCarrierMovement, g) + "\n");
        	}
        	for (TroopToPlanetMovement troopToPlanetMovement : ttpm) {
        		infoarea.append(getText(troopToPlanetMovement, g) + "\n");
        	}
        	infoarea.append("\n");
        }
    }
    
    // add changes in planet visibility
	List<String> planetNames = orders.getPlanetVisibilities();
    if (planetNames.size() > 0){
    	infoarea.append("Open/closed planets" + "\n");
    	infoarea.append(sepLine);
    	for (int k = 0; k < planetNames.size(); k++){    	
    		Planet tempPlanet = g.findPlanet(planetNames.get(k));
    		infoarea.append("Change planet " + tempPlanet.getName() + " to ");
    		if (tempPlanet.isOpen()){  // change to closed
    			infoarea.append("closed");
    		}else{ // change to open
    			infoarea.append("open");
    		}
    		infoarea.append(" status." + "\n");
    	}
    	infoarea.append("\n");
    }

    // add changes in abandoning planets
	planetNames = orders.getAbandonPlanets();
    if (planetNames.size() > 0){
    	infoarea.append("Abandon planets" + "\n");
    	infoarea.append(sepLine);
    	for (int l = 0; l < planetNames.size(); l++){
    		Planet tempPlanet = g.findPlanet(planetNames.get(l));
    		infoarea.append("Planet " + tempPlanet.getName() + " is to be abandoned." + "\n");
    	}
    	infoarea.append("\n");
    }

    // add ships do be selfdestroyed
	List<String> shipIds = orders.getShipSelfDestructs();
    if (shipIds.size() > 0){
    	infoarea.append("Selfdestruct ships" + "\n");
    	infoarea.append(sepLine);
    	for (int m = 0; m < shipIds.size(); m++){
    		Spaceship tempss = g.findSpaceshipByUniqueId(shipIds.get(m));
    		infoarea.append("Spaceship " + tempss.getName() + " is to be destroyed." + "\n");
    	}
    	infoarea.append("\n");
    }
    
    //  add VIPs do be selfdestroyed
	List<String> tempVIPs = orders.getVIPSelfDestructs();
    if (tempVIPs.size() > 0){
    	infoarea.append("Selfdestruct VIPs" + "\n");
    	infoarea.append(sepLine);
    	for (int v = 0; v < tempVIPs.size(); v++){
    		VIPType tempVIP = VipPureFunctions.getVipTypeByKey(tempVIPs.get(v), g.getGameWorld());
    		infoarea.append("VIP " + tempVIP.getName() + " is to be retired." + "\n");
    	}
    	infoarea.append("\n");
    }

    // add ships do be selfdestroyed
	List<String> tempBuildings = orders.getBuildingSelfDestructs();
    if (tempBuildings.size() > 0){
    	infoarea.append("Selfdestruct Buildings" + "\n");
    	infoarea.append(sepLine);
    	for (int n = 0; n < tempBuildings.size(); n++){
    		Building tempBuilding = BuildingPureFunctions.findBuilding(tempBuildings.get(n), aPlayer, g);
    		infoarea.append("Building " + BuildingPureFunctions.getBuildingType(tempBuilding.getTypeKey(), g.getGameWorld()).getName() + " at " + tempBuilding.getLocation().getName() + " is to be destroyed." + "\n");
    	}
    	infoarea.append("\n");
    }

    // add ships do change screened status
	shipIds = orders.getScreenedShips();
    if (shipIds.size() > 0){
    	infoarea.append("Screen spaceships" + "\n");
    	infoarea.append(sepLine);
    	for (int p = 0; p < shipIds.size(); p++){
    		Spaceship tempss = g.findSpaceshipByUniqueId(shipIds.get(p));
    		if (tempss.getLocation() != null){
    			infoarea.append("Your ship " + tempss.getName() + " at " + tempss.getLocation().getName() + "  is to change its screened status to " + !tempss.isScreened() + "\n");
    		}else{
    			infoarea.append("Your ship " + tempss.getName() + " in deep space is to change its screened status to " + !tempss.isScreened() + "\n");
    		}
    	}
    	infoarea.append("\n");
    }

    // add ship movements
	List<VIPMovement> vipMovementa = orders.getVIPMoves();
    if (vipMovementa.size() > 0){
    	infoarea.append("VIP movements\n");
    	infoarea.append(sepLine);
    	for (int s = 0; s < vipMovementa.size(); s++){
    		VIPMovement tempVIPMovement = vipMovementa.get(s);
    		infoarea.append(getText(tempVIPMovement, g) + "\n");
    	}
    	infoarea.append("\n");
    }
    
    // add ReserchOrders
	List<ResearchOrder> researchOrders = orders.getResearchOrders();
    if (researchOrders.size() > 0){
    	infoarea.append("Researchs\n");
    	infoarea.append(sepLine);
    	for (int i = 0; i < researchOrders.size(); i++){
    		ResearchOrder tempResearch = researchOrders.get(i);
    		infoarea.append(tempResearch.getText() + "\n");
    	}
    	infoarea.append("\n");
    }

    // Change planet notes
    List<PlanetNotesChange> notesChanges = orders.getPlanetNotesChanges();
    if (notesChanges.size() > 0){
    	infoarea.append("Planet notes Changes\n");
    	infoarea.append(sepLine);
    	for (PlanetNotesChange change : notesChanges) {
        	infoarea.append(change.getText() + "\n");
		}
    	infoarea.append("\n");
    }

    // other orders (i.e. abandon game)
    if (orders.isAbandonGame()){
    	infoarea.append("Other orders\n");
    	infoarea.append(sepLine);
    	if (orders.isAbandonGame()){
    		infoarea.append("Abandon game\n");
    	}
    	infoarea.append("\n");
    }

    if (infoarea.getModel().size() == 0){
    	infoarea.append("\n");
    	infoarea.append("No orders exist\n");
    }

    if (isVisible()){
    	update(getGraphics());
    	paintChildren(getGraphics());
    }
  }

	private String getText(ShipMovement shipMovement, Galaxy aGalaxy) {
		Spaceship spaceship = SpaceshipPureFunctions.findSpaceship(shipMovement.getSpaceshipKey(), aGalaxy);
		return "Move " + spaceship.getName() + " from "
				+ spaceship.getName() + " to "
				+ shipMovement.getPlanetName() + ".";
	}

	private String getText(VIPMovement vipMovement, Galaxy aGalaxy) {
		return "Move " + VipPureFunctions.getVipTypeByKey(VipPureFunctions.findVIP(vipMovement.getVipKey(), aGalaxy).getTypeKey(), aGalaxy.getGameWorld()).getName() + " from " + VipPureFunctions.getLocationString(VipPureFunctions.findVIP(vipMovement.getVipKey(), aGalaxy)) + " to " + VIPsPanel.getDestinationName(vipMovement, aGalaxy) + ".";
	}

	public String getText(ShipToCarrierMovement shipToCarrierMovement, Galaxy aGalaxy) {
		Spaceship aSpaceship = SpaceshipPureFunctions.findSpaceship(shipToCarrierMovement.getSpaceShipKey(), aGalaxy);
		Spaceship aSpaceshipCarrier = SpaceshipPureFunctions.findSpaceship(shipToCarrierMovement.getDestinationCarrierKey(), aGalaxy);

		String retStr;
		if (aSpaceship.getLocation() != null) {
			retStr = "Move " + aSpaceship.getName() + " from " + aSpaceship.getLocation().getName() + " to " + aSpaceshipCarrier.getName() + ".";
		} else {
			retStr = "Move " + aSpaceship.getName() + " from " + aSpaceship.getCarrierLocation().getName() + " to " + aSpaceshipCarrier.getName() + ".";
		}
		return retStr;
	}

	public String getText(TroopToCarrierMovement troopToCarrierMovement, Galaxy aGalaxy){
		Troop aTroop = TroopPureFunctions.findTroop(troopToCarrierMovement.getTroopKey(), aGalaxy);
		Spaceship destinationCarrier = SpaceshipPureFunctions.findSpaceship(troopToCarrierMovement.getDestinationCarrierKey(), aGalaxy);
		String retStr = null;
		if (aTroop.getPlanetLocation() != null){
			retStr = "Move " + aTroop.getName() + " from " + aTroop.getPlanetLocation().getName() + " to " + destinationCarrier.getName() + ".";
		}else{ // move from ship to ship
			retStr = "Move " + aTroop.getName() + " from " + aTroop.getShipLocation().getName() + " to " + destinationCarrier.getName() + ".";
		}
		return retStr;
	}

	private String getText(TroopToPlanetMovement troopToPlanetMovement, Galaxy aGalaxy) {
		Troop aTroop = TroopPureFunctions.findTroop(troopToPlanetMovement.getTroopKey(), aGalaxy);
		return "Move " + aTroop.getName() + " from " + aTroop.getShipLocation().getName() + " to " + troopToPlanetMovement.getDestinationName() + ".";
	}

}