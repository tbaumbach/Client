package spaceraze.client.game.panels.resource;

import java.util.LinkedList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.SRScrollPane;
import spaceraze.client.components.SRTable;
import spaceraze.client.components.SRTableHeader;
import spaceraze.client.game.GameGUIPanel;
import spaceraze.client.interfaces.SRUpdateablePanel;
import spaceraze.servlethelper.game.spaceship.SpaceshipPureFunctions;
import spaceraze.servlethelper.game.troop.TroopPureFunctions;
import spaceraze.servlethelper.game.vip.VipPureFunctions;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Galaxy;
import spaceraze.world.Planet;
import spaceraze.world.Player;
import spaceraze.world.VIP;
import spaceraze.world.orders.VIPMovement;

@SuppressWarnings("serial")
public class VIPsPanel extends SRBasePanel implements SRUpdateablePanel, ListSelectionListener{
	private SRTable vipsTable;
	private SRTableHeader tableHeader;
	private SRScrollPane tablePanel;
	private SRLabel title;
	private String id;
	private Player player;
	private Galaxy g;
	private List<VIP> VIPs = new LinkedList<VIP>();
	private GameGUIPanel gameGuiPanel;
	
	public VIPsPanel(Galaxy g, String newId, Player p, GameGUIPanel gameGuiPanel) {
		this.setLayout(null);
		this.id = newId;
		this.player = p;
		this.g = g;
		this.gameGuiPanel = gameGuiPanel;
		
	    title = new SRLabel("VIPs");
	    title.setBounds(10,10,200,15);
	    add(title);	    

		fillTableList();	
	}
	
	private void fillTableList(){
		VIPs = this.g.getPlayersVips(player);
		int nrVIPs = VIPs.size();
		vipsTable = new SRTable(nrVIPs, 3);
		vipsTable.setAutoResizeMode(1);
		vipsTable.setRowHeight(18);
		vipsTable.setColumnSelectionAllowed(false);
		vipsTable.setRowSelectionAllowed(true);
		vipsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vipsTable.getSelectionModel().addListSelectionListener(this);
		
		int i = 0;
		for (VIP aVIP : VIPs) {
			vipsTable.setValueAt(VipPureFunctions.getVipTypeByUuid(aVIP.getTypeUuid(), player.getGalaxy().getGameWorld()).getName(), i, 0);
			vipsTable.setValueAt(VipPureFunctions.getLocationString(aVIP), i, 1);
	        String tempDest = getDestinationName(aVIP, player.getGalaxy(), player.getOrders().getVIPMoves());
			vipsTable.setValueAt(tempDest, i, 2);
			i++;
		}
		
		TableColumnModel model = vipsTable.getColumnModel();
		model.getColumn(0).setHeaderValue("Name");
		model.getColumn(0).setPreferredWidth(250);
		model.getColumn(1).setHeaderValue("Location");
		model.getColumn(2).setHeaderValue("Destination");
		
		vipsTable.setAutoCreateRowSorter(true);
		
		tableHeader = new SRTableHeader(model);
		tableHeader.setOpaque(false);
		tableHeader.setReorderingAllowed(true);
		tableHeader.setTable(vipsTable);
		tableHeader.setVisible(true);
		vipsTable.setTableHeader(tableHeader);

		tablePanel = new SRScrollPane(vipsTable);
		int rowHeight = 18;
		int tableHeight = 15*rowHeight;
		if (nrVIPs < 14){
			tableHeight = (nrVIPs + 1)*rowHeight;
		}
		tablePanel.setBounds(10,40,800,tableHeight);
		tablePanel.setBackground(StyleGuide.colorBackground);
		tablePanel.setForeground(StyleGuide.colorBackground);
		tablePanel.setVisible(true);
		add(tablePanel);  
	}
		
    private void showVIP(int rowIndex){
		VIPs = this.g.getPlayersVips(player);
		VIP selectedVIP = VIPs.get(rowIndex);
		Planet aPlanet = VipPureFunctions.getLocation(selectedVIP);
    	gameGuiPanel.showPlanet(aPlanet.getName());    	
    }

	public void valueChanged(ListSelectionEvent e) {
		showVIP(vipsTable.getSelectedRow());
	}

	public String getId(){
		return id;
	}
	
	private void clearTable(){
		vipsTable.removeAll();
		vipsTable = null;
		remove(tablePanel);
		tablePanel = null;
	}
	
	public void updateData(){
		clearTable();
		fillTableList();
	}

	public static String getDestinationName(VIP tempVIP, Galaxy aGalaxy, List<VIPMovement> VIPMoves) {
		String destName = "";
		boolean found = false;
		int i = 0;
		VIPMovement tempVIPMove = null;
		while ((i < VIPMoves.size()) & !found) {
			tempVIPMove = VIPMoves.get(i);
			if (tempVIP.getUuid().equalsIgnoreCase(tempVIPMove.getVipKey())) {
				found = true;
			} else {
				i++;
			}
		}
		if (found) {
			destName = getDestinationName(tempVIPMove, aGalaxy);
		}
		return destName;
	}

	public static String getDestinationName(VIPMovement vipMovement, Galaxy aGalaxy) {
		String returnValue = "";
		if (vipMovement.getPlanetDestination() != null) {
			returnValue = vipMovement.getPlanetDestination();
		} else if (vipMovement.getShipDestination() != null) {
			returnValue = SpaceshipPureFunctions.findSpaceship(vipMovement.getShipDestination(), aGalaxy).getUniqueName();
		} else { // troop
			returnValue = TroopPureFunctions.findTroop(vipMovement.getTroopDestination(), aGalaxy).getName();

		}
		return returnValue;
	}
	
}