package spaceraze.client.game.panels.incomeexpenses;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.SRScrollPane;
import spaceraze.client.components.SRTable;
import spaceraze.client.components.SRTableHeader;
import spaceraze.client.interfaces.SRUpdateablePanel;
import spaceraze.servlethelper.game.expenses.ExpensePureFunction;
import spaceraze.servlethelper.game.player.CostPureFunctions;
import spaceraze.servlethelper.game.player.IncomePureFunctions;
import spaceraze.servlethelper.game.player.PlayerPureFunctions;
import spaceraze.util.general.Logger;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Player;
import spaceraze.world.incomeExpensesReports.IncomeReport;
import spaceraze.world.incomeExpensesReports.IncomeReportRow;

public class IncomeExpensesPanel extends SRBasePanel implements SRUpdateablePanel{
	private static final long serialVersionUID = 1L;
	private SRLabel incomeLabel,curTreasuryLabel,curUpkeepLabel,expencesLabel,freeUpkeepLabel,newTreasuryLabel;
	private SRLabel incomeValueLabel,curTreasuryValueLabel,curUpkeepValueLabel,expencesValueLabel,freeUpkeepValueLabel,newTreasuryValueLabel;
	private SRLabel incomeTitleLabel,supportTitleLabel;
	private SRLabel incomeCorrLabel,incomeCorrValueLabel;
	private SRLabel upkeepCorrLabel,upkeepCorrValueLabel;
	private SRLabel totalUpkeepShipsLabel,totalUpkeepShipsValueLabel;
	private SRLabel totalUpkeepTroopsLabel,totalUpkeepTroopsValueLabel;
	private SRLabel totalUpkeepVIPsLabel,totalUpkeepVIPsValueLabel;
	private String id;
	private Player p;
	// income specification list
//	private SRTable incomeReportTable;
//	private SRTableHeader incomeReportTableHeader;
//	private SRScrollPane incomeReportTableScrollPanel;
//	private SRLabel incomeReportTitleLbl;
	private SRTable incomeReportTable;
	private SRTableHeader incomeReportTableHeader;
	private SRScrollPane incomeReportTableScrollPanel;
	private SRLabel incomeReportTitleLbl;


  public IncomeExpensesPanel(Player p,String id){
    this.p = p;
    this.id = id;
    this.setLayout(null);
    int col1width = 145;
    int x = 280;

    // Left column, support
    // --------------------
    
    supportTitleLabel = new SRLabel("Spaceship upkeep cost:");
    supportTitleLabel.setBounds(10,10,col1width,20);
    add(supportTitleLabel);
    
    
    int freeUpkeep = CostPureFunctions.getPlayerFreeUpkeepWithoutCorruption(p, p.getGalaxy().getPlanets(), p.getGalaxy().getGameWorld());
    freeUpkeepLabel = new SRLabel("Free upkeep:");
    freeUpkeepLabel.setBounds(10,40,col1width,20);
    add(freeUpkeepLabel);
    freeUpkeepValueLabel = new SRLabel("+" + String.valueOf(freeUpkeep));
    freeUpkeepValueLabel.setBounds(col1width+10,40,250,20);
    add(freeUpkeepValueLabel);

    
    upkeepCorrLabel = new SRLabel("Lost to corruption:");
    upkeepCorrLabel.setBounds(10,70,col1width,20);
    add(upkeepCorrLabel);
    upkeepCorrValueLabel = new SRLabel("-" + IncomePureFunctions.getLostToCorruption(freeUpkeep, p.getCorruptionPoint()));
    upkeepCorrValueLabel.setBounds(col1width+10,70,250,20);
    add(upkeepCorrValueLabel);

    curUpkeepLabel = new SRLabel("Current upkeep:");
    curUpkeepLabel.setBounds(10,100,col1width,20);
    add(curUpkeepLabel);
    curUpkeepValueLabel = new SRLabel("-" + CostPureFunctions.getPlayerUpkeepCost(p, p.getGalaxy().getSpaceships()));
    curUpkeepValueLabel.setBounds(col1width+10,100,250,20);
    add(curUpkeepValueLabel);

    totalUpkeepShipsLabel = new SRLabel("Total upkeep cost:");
    totalUpkeepShipsLabel.setBounds(10,130,col1width,20);
    add(totalUpkeepShipsLabel);
    totalUpkeepShipsValueLabel = new SRLabel("-" + CostPureFunctions.getPlayerUpkeepShips(p, p.getGalaxy().getPlanets(), p.getGalaxy().getSpaceships(), p.getGalaxy().getGameWorld()));
    totalUpkeepShipsValueLabel.setBounds(col1width+10,130,250,20);
    add(totalUpkeepShipsValueLabel);

    // Right column, income
    // --------------------
    
    incomeTitleLabel = new SRLabel("Compute left to spend:");
    incomeTitleLabel.setBounds(x,10,col1width,20);
    add(incomeTitleLabel);

    curTreasuryLabel = new SRLabel("Saved from last turn:");
    curTreasuryLabel.setBounds(x,40,col1width,20);
    add(curTreasuryLabel);
    curTreasuryValueLabel = new SRLabel("+" + p.getTreasury());
    curTreasuryValueLabel.setBounds(col1width+x,40,250,20);
    add(curTreasuryValueLabel);

    incomeLabel = new SRLabel("Planet incomes total:");
    incomeLabel.setBounds(x,70,col1width,20);
    add(incomeLabel);
    incomeValueLabel = new SRLabel("+" + IncomePureFunctions.getPlayerIncomeWithoutCorruption(p,false, p.getGalaxy()));
    incomeValueLabel.setBounds(col1width+x,70,250,20);
    add(incomeValueLabel);

    int incomeCorr = IncomePureFunctions.getPlayerIncomeWithoutCorruption(p,false, p.getGalaxy());
    incomeCorrLabel = new SRLabel("Lost to corruption:");
    incomeCorrLabel.setBounds(x,100,col1width,20);
    add(incomeCorrLabel);
    incomeCorrValueLabel = new SRLabel("-" + IncomePureFunctions.getLostToCorruption(incomeCorr, p.getCorruptionPoint()));
    incomeCorrValueLabel.setBounds(col1width+x,100,250,20);
    add(incomeCorrValueLabel);

    totalUpkeepShipsLabel = new SRLabel("Spaceships upkeep cost:");
    totalUpkeepShipsLabel.setBounds(x,130,col1width,20);
    add(totalUpkeepShipsLabel);
    totalUpkeepShipsValueLabel = new SRLabel("-" +CostPureFunctions.getPlayerUpkeepShips(p, p.getGalaxy().getPlanets(), p.getGalaxy().getSpaceships(), p.getGalaxy().getGameWorld()));
    totalUpkeepShipsValueLabel.setBounds(col1width+x,130,250,20);
    add(totalUpkeepShipsValueLabel);

    totalUpkeepTroopsLabel = new SRLabel("Troops upkeep cost:");
    totalUpkeepTroopsLabel.setBounds(x,160,col1width,20);
    add(totalUpkeepTroopsLabel);
    totalUpkeepTroopsValueLabel = new SRLabel("-" + CostPureFunctions.getPlayerUpkeepTroops(p, p.getGalaxy().getPlanets(), p.getGalaxy().getTroops()));
    totalUpkeepTroopsValueLabel.setBounds(col1width+x,160,250,20);
    add(totalUpkeepTroopsValueLabel);
    
    totalUpkeepVIPsLabel = new SRLabel("VIPs upkeep cost:");
    totalUpkeepVIPsLabel.setBounds(x,190,col1width,20);
    add(totalUpkeepVIPsLabel);
    totalUpkeepVIPsValueLabel = new SRLabel("-" + CostPureFunctions.getPlayerUpkeepVIPs(p, p.getGalaxy().getAllVIPs()));
    totalUpkeepVIPsValueLabel.setBounds(col1width+x,190,250,20);
    add(totalUpkeepVIPsValueLabel);

    expencesLabel = new SRLabel("Expences this turn total:");
    expencesLabel.setBounds(x,220,col1width,20);
    add(expencesLabel);
    expencesValueLabel = new SRLabel("-" + ExpensePureFunction.getExpensesCost(p.getGalaxy(), p));
    expencesValueLabel.setBounds(col1width+x,220,250,20);
    expencesValueLabel.setOpaque(true);
    expencesValueLabel.setBackground(StyleGuide.colorBackground);
    add(expencesValueLabel);

    newTreasuryLabel = new SRLabel("Total left to spend:");
    newTreasuryLabel.setBounds(x,250,col1width,20);
    add(newTreasuryLabel);
    newTreasuryValueLabel = new SRLabel(String.valueOf(PlayerPureFunctions.getTreasuryAfterCosts(p, p.getGalaxy())));
    newTreasuryValueLabel.setBounds(col1width+x,250,250,20);
    newTreasuryValueLabel.setOpaque(true);
    newTreasuryValueLabel.setBackground(StyleGuide.colorBackground);
    add(newTreasuryValueLabel);
    
	fillTableList();

	incomeReportTitleLbl = new SRLabel("Planet income total - details:");
	incomeReportTitleLbl.setBounds(10,320,200,20);
    add(incomeReportTitleLbl);

  }
  
	private void fillTableList(){
		IncomeReport incomeReport = p.getTurnInfo().getLatestIncomeReports();
		List<IncomeReportRow> rows = incomeReport.getRows();
		int nrRows = rows.size();
		incomeReportTable = new SRTable(nrRows, 5);
		incomeReportTable.setAutoResizeMode(1);
		incomeReportTable.setRowHeight(18);
		incomeReportTable.setColumnSelectionAllowed(false);
		incomeReportTable.setRowSelectionAllowed(true);
		incomeReportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		incomeReportTable.getSelectionModel().addListSelectionListener(this);
	
		int rowCounter = 0;
		for (IncomeReportRow aRow : rows) {
			incomeReportTable.setValueAt(aRow.getCounter(), rowCounter, 0);
			incomeReportTable.setValueAt(aRow.getType().getDesc(), rowCounter, 1);
			incomeReportTable.setValueAt(aRow.getDesc(), rowCounter, 2);
			incomeReportTable.setValueAt(aRow.getLocation(), rowCounter, 3);
			incomeReportTable.setValueAt(aRow.getValue(), rowCounter, 4);
			rowCounter++;
		}
				
		TableColumnModel model = incomeReportTable.getColumnModel();
		model.getColumn(0).setHeaderValue("Nr");
		model.getColumn(0).setPreferredWidth(30);
		model.getColumn(1).setHeaderValue("Type");
		model.getColumn(2).setHeaderValue("Description");
		model.getColumn(2).setPreferredWidth(200);
		model.getColumn(3).setHeaderValue("Location");
		model.getColumn(4).setHeaderValue("Value");
		model.getColumn(4).setPreferredWidth(30);
		
		incomeReportTable.setAutoCreateRowSorter(true);
		
		incomeReportTableHeader = new SRTableHeader(model);
		incomeReportTableHeader.setOpaque(false);
		incomeReportTableHeader.setReorderingAllowed(true);
		incomeReportTableHeader.setTable(incomeReportTable);
		incomeReportTableHeader.setVisible(true);
		incomeReportTable.setTableHeader(incomeReportTableHeader);

		incomeReportTableScrollPanel = new SRScrollPane(incomeReportTable);
		int rowHeight = 18;
		int tableHeight = 15*rowHeight;
		if (nrRows < 14){
			tableHeight = (nrRows + 1)*rowHeight;
		}
		incomeReportTableScrollPanel.setBounds(10,350,800,tableHeight);
		incomeReportTableScrollPanel.setBackground(StyleGuide.colorBackground);
		incomeReportTableScrollPanel.setForeground(StyleGuide.colorBackground);
		incomeReportTableScrollPanel.setVisible(true);
		add(incomeReportTableScrollPanel);  
	}

  public void paintComponent(Graphics g){
	  g.setColor(StyleGuide.colorCurrent);
	  g.drawLine(8,35,170,35);
	  g.drawLine(278,35,445,35);
	  g.drawLine(8,125,170,125);
	  g.drawLine(278,280,445,280);
	  // draw arrow
	  g.drawLine(180,140,270,140);  	
	  g.drawLine(265,145,270,140);  	
	  g.drawLine(265,135,270,140);  	
  }

  public String getId(){
    return id;
  }

  public void updateData(){
  	Logger.finer("called");
    expencesValueLabel.setText("-" + ExpensePureFunction.getExpensesCost(p.getGalaxy(), p));
    newTreasuryValueLabel.setText(String.valueOf(PlayerPureFunctions.getTreasuryAfterCosts(p, p.getGalaxy())));
  }

}

