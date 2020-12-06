package spaceraze.client.game.panels.statistics;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spaceraze.client.ColorConverter;
import spaceraze.client.components.ComboBoxPanel;
import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.scrollable.ListPanel;
import spaceraze.client.interfaces.SRUpdateablePanel;
import spaceraze.util.general.Logger;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Galaxy;
import spaceraze.world.Planet;
import spaceraze.world.Player;
import spaceraze.world.Spaceship;
import spaceraze.world.StatisticGameType;
import spaceraze.world.StatisticType;
import spaceraze.world.enums.DiplomacyGameType;

/**
 * <p>Title: SpaceRaze 3.0</p>
 * <p>Description: SpaceRaze with Swing GUI</p>
 * @author Paul Bodin
 * @version 3.0
 */

public class StatisticsPanel extends SRBasePanel implements SRUpdateablePanel, ListSelectionListener, ActionListener{
	private static final long serialVersionUID = 1L;
	private String id;
	private SRLabel strGov,mostKills,nrActivePlayers,defeatedPlayers,nrPlanetsLbl,nrSpaceshipsLbl,prodPercentageLbl,largestPlayerLbl;
	private Galaxy g;
	private Color tempColor; 
	// graph components
	private SRLabel graphLbl,statisticsTypeLbl,highlightPlayerLbl;
	private StatisticsGraph graph; 
    private ListPanel statisticTypesList;
    private ComboBoxPanel highlightPlayerChoice;

    public StatisticsPanel(Player p, Galaxy g, String id) {
    	this.g = g;
    	this.id = id;

    	nrActivePlayers = new SRLabel();
    	nrActivePlayers.setBounds(10,10,300,20);
    	add(nrActivePlayers);

    	defeatedPlayers = new SRLabel();
    	defeatedPlayers.setBounds(10,40,300,20);
    	add(defeatedPlayers);

    	getActiveAndDefeatedPlayers();

    	if(getMostKills() != null && getMostKills().getOwner() != null){
    		strGov = new SRLabel("Govenor: ");
    		strGov.setBounds(10,70,90,20);
    		add(strGov);

    		mostKills = new SRLabel(getMostKills().getOwner().getGovernorName() + " have a ship ("+ getMostKills().getName()  +") with most kills in the Galaxy ( " + getMostKills().getKills() + " Kills)." , ColorConverter.getColorFromHexString(getMostKills().getOwner().getFaction().getPlanetHexColor()));
    		mostKills.setBounds(70,70,500,20);
    		add(mostKills);
    	}
    	List<Planet> allPlanets = g.getPlanets();
    	int counter = 0;
    	for (Planet aPlanet : allPlanets) {
    		if (aPlanet.getPlayerInControl() == p){
    			counter++;
    		}
    	}
    	String planetsString = "You have " + counter + " planet";
    	if (counter > 1){
    		planetsString += "s";
    	}
    	nrPlanetsLbl = new SRLabel(planetsString);
    	nrPlanetsLbl.setBounds(10,100,300,20);
    	add(nrPlanetsLbl);

    	List<Spaceship> allShips = g.getSpaceships();
    	counter = 0;
    	for (Spaceship aShip : allShips) {
    		if (aShip.getOwner() == p){
    			counter++;
    		}
    	}
    	String shipsString = "You have " + counter + " spaceship";
    	if (counter > 1){
    		shipsString += "s";
    	}
    	nrSpaceshipsLbl = new SRLabel(shipsString);
    	nrSpaceshipsLbl.setBounds(10,130,300,20);
    	add(nrSpaceshipsLbl);

    	int percInt = getProdPercentage(p, g);
    	String percStr = "You control " + percInt + "% of the total production in this sector"; 
    	prodPercentageLbl = new SRLabel(percStr);
    	prodPercentageLbl.setBounds(10,160,400,20);
    	add(prodPercentageLbl);

    	if (g.getDiplomacyGameType() == DiplomacyGameType.FACTION){
    		String LargestPlayerStr = "Largest faction in sector is ";
    		largestPlayerLbl = new SRLabel(LargestPlayerStr);
    		largestPlayerLbl.setBounds(10,190,300,20);
    		add(largestPlayerLbl);

    		String LargestFactionNameStr = getProdPercentageALL(g); 
    		largestPlayerLbl = new SRLabel(LargestFactionNameStr,tempColor);
    		largestPlayerLbl.setBounds(165,190,300,20);
    		add(largestPlayerLbl);
    	}

    	graphLbl = new SRLabel("Graph: " + StatisticType.PRODUCTION_PLAYER.getText());
    	graphLbl.setBounds(10,220,550,20);
    	add(graphLbl);

    	graph = new StatisticsGraph(g);
    	graph.setBounds(10, 240, 600, 370);
    	add(graph);

    	// choose type of statistics list
    	statisticsTypeLbl = new SRLabel("Choose Graph");
    	statisticsTypeLbl.setBounds(630,240,200,20);
    	add(statisticsTypeLbl);

    	statisticTypesList = new ListPanel();
    	statisticTypesList.setBounds(630,260,200,150);
    	statisticTypesList.setForeground(StyleGuide.colorCurrent);
    	statisticTypesList.setBackground(StyleGuide.colorBackground);
    	statisticTypesList.setBorder(new LineBorder(StyleGuide.colorCurrent));
    	statisticTypesList.setMultipleSelect(false);
    	fillTypesList();
    	statisticTypesList.setSelected(0);
    	statisticTypesList.setListSelectionListener(this);
    	statisticTypesList.updateScrollList();
    	add(statisticTypesList);      

    	// choose which player should be highlighted
    	highlightPlayerLbl = new SRLabel("Choose player highlight");
    	highlightPlayerLbl.setBounds(630,430,200,20);
    	add(highlightPlayerLbl);

    	highlightPlayerChoice = new ComboBoxPanel();
    	highlightPlayerChoice.setBounds(630,450,200,20);
    	fillPlayerList();
    	highlightPlayerChoice.addActionListener(this);
    	add(highlightPlayerChoice);
    	
        if (!g.isGameOver() && g.getStatisticGameType() == StatisticGameType.NONE){
        	graphLbl.setVisible(false);
        	statisticsTypeLbl.setVisible(false);
        	highlightPlayerLbl.setVisible(false);
        	graph.setVisible(false); 
            statisticTypesList.setVisible(false);
            highlightPlayerChoice.setVisible(false);
        }
    }
  
    private void fillTypesList(){
    	DefaultListModel dlm = (DefaultListModel)statisticTypesList.getModel();
		boolean includeFactionProduction = g.getFactionGame() & (g.getDiplomacyGameType() == DiplomacyGameType.FACTION);
    	if (!g.isGameOver() && g.getStatisticGameType() == StatisticGameType.PRODUCTION_ONLY){
			dlm.addElement(StatisticType.PRODUCTION_PLAYER.getText());
			if (includeFactionProduction){
				dlm.addElement(StatisticType.PRODUCTION_FACTION.getText());
			}
    	}else{
    		boolean includeTroops = g.getTroops().size() > 0;
    		for (String aStatisticTypeText : StatisticType.getTypeTexts()) {
    			if (aStatisticTypeText.equals(StatisticType.TROOPS_NUMBER.getText())){
    				if (includeTroops){
    					dlm.addElement(aStatisticTypeText);
    				}
    			}else
    				if (aStatisticTypeText.equals(StatisticType.PRODUCTION_FACTION.getText())){
    					if (includeFactionProduction){
    						dlm.addElement(aStatisticTypeText);
    					}
    				}else{
    					dlm.addElement(aStatisticTypeText);
    				}
    		}
    	}
    	statisticTypesList.updateScrollList();
    }

    private void fillPlayerList(){
    	highlightPlayerChoice.addItem("None");
    	for (Player aPlayer : g.getPlayers()) {
    		highlightPlayerChoice.addItem(aPlayer.getGovernorName() + " (" + aPlayer.getFaction().getName() + ")");
    	}
    }

    private String getProdPercentageALL(Galaxy g){
    	String largestFaction ="";
    	int largestProd = 0;
    	int tempLargest = 0;
    	Logger.fine("getProdPercentageALL");

    	List<Player> players = g.getPlayers();
    	for (Player aPlayer : players) {
    		tempLargest = aPlayer.getFaction().getTotalPop();
    		if (largestProd < tempLargest)
    		{  
    			largestProd = tempLargest;
    			largestFaction = aPlayer.getFaction().getName();
    			tempColor = ColorConverter.getColorFromHexString(aPlayer.getFaction().getPlanetHexColor());
    		}
    	}
    	return largestFaction;
    }

    private int getProdPercentage(Player p, Galaxy g){
    	int playerProd = 0;
    	int totalPop = 0; 

    	Logger.fine("getProdPercentage");
    	List<Planet> planets = g.getPlanets();
    	for (Planet aPlanet : planets) {
    		if (aPlanet.getPlayerInControl() == p){
    			if (aPlanet.getPlayerInControl().isAlien()){
    				playerProd += aPlanet.getResistance();
    			}else{
    				playerProd += aPlanet.getPopulation();
    			}
    		}else{
    			if (aPlanet.getPlayerInControl() == null){
    				totalPop += aPlanet.getResistance();
    			}else{
    				if (aPlanet.getPlayerInControl().isAlien()){
    					totalPop += aPlanet.getResistance();
    				}else{
    					totalPop += aPlanet.getPopulation();
    				}
    			}
    		}
    	}
    	// compute result
    	int percentage = (int)Math.round((playerProd*100.0) / (playerProd+totalPop));
    	return percentage;
    }


    private void getActiveAndDefeatedPlayers(){
    	int defeated = 0;
    	int notDefeated = 0;
    	List<Player> players = g.getPlayers();
    	for (int i = 0; i < players.size(); i++){
    		Player tmpPlayer = (Player)players.get(i);
    		if (tmpPlayer.isDefeated()){
    			defeated++;
    		}else{
    			notDefeated++;
    		}
    	}
    	if (notDefeated > 0){
    		nrActivePlayers.setText("Number of active Govenors: " + notDefeated);
    	}else{
    		nrActivePlayers.setText("There are no active Govenors left in this sector!");
    	}
    	if (defeated > 0){
    		defeatedPlayers.setText("Number of defeated Govenors: " + defeated);
    	}else{
    		defeatedPlayers.setText("There are no defeated Govenors in this sector - yet...");
    	}
    }

    private Spaceship getMostKills(){
    	List<Spaceship> ss = g.getSpaceships();
    	Spaceship maxKillsSpaceship = (Spaceship)ss.get(0);
    	for (int i = 1; i < ss.size(); i++){
    		Spaceship tmpss = (Spaceship)ss.get(i);
    		if (tmpss.getKills() > maxKillsSpaceship.getKills()){
    			maxKillsSpaceship = tmpss;
    		}
    	}
    	return maxKillsSpaceship;
    }

    public String getId(){
    	return id;
    }

    public void updateData(){
    }

    public void updateGraph(){
    	// find chosen type of statistics
    	String selectedType = statisticTypesList.getSelectedItem();
    	StatisticType chosenStatisticsType = StatisticType.findStatisticType(selectedType);
    	String lblText = "Graph: " + selectedType;
    	// find selected highlighted player (if any)
    	String playerName = null;
    	if (chosenStatisticsType != StatisticType.PRODUCTION_FACTION){
    		int highlightedPlayerIndex = highlightPlayerChoice.getSelectedIndex();
    		if (highlightedPlayerIndex > 0){
    			playerName = g.getPlayers().get(highlightedPlayerIndex-1).getName();
    		}
        	if (highlightedPlayerIndex > 0){
        		lblText += ", highlighting " + highlightPlayerChoice.getSelectedItem();
        	}
    	}
    	// set label text
    	graphLbl.setText(lblText);
    	// show graph
    	graph.drawGraph(chosenStatisticsType, playerName);
    }

    public void valueChanged(ListSelectionEvent lse){
    	if (lse.getSource() == statisticTypesList){
        	String selectedType = statisticTypesList.getSelectedItem();
        	StatisticType chosenStatisticsType = StatisticType.findStatisticType(selectedType);
        	if (chosenStatisticsType == StatisticType.PRODUCTION_FACTION){
//        		highlightPlayerChoice.setSelectedIndex(0);
        		highlightPlayerChoice.setEnabled(false);
        	}else{
        		highlightPlayerChoice.setEnabled(true);
        	}
    		updateGraph();
    	}
    }

    public void actionPerformed(ActionEvent ae){
    	if (ae.getSource() == highlightPlayerChoice){
    		updateGraph();
    	}
    }

}