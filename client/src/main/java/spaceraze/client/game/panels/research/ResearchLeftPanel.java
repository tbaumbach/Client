//Title:        SpaceRaze Client
//Author:       Paul Bodin
//Description:  Denna klass inneh�ller knappar etc f�r att styra utseendet p� kartan.

package spaceraze.client.game.panels.research;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.border.LineBorder;

import spaceraze.client.components.ComboBoxPanel;
import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRButton;
import spaceraze.client.components.SRLabel;
import spaceraze.client.game.GameGUIPanel;
import spaceraze.client.game.SpaceRazePanel;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Player;

//TODO 2020-01-26 not in use. remove???
public class ResearchLeftPanel extends SRBasePanel implements ItemListener, ActionListener {
  private static final long serialVersionUID = 1L;
  int speed = 2,initialZoom = -6,sectorZoom = 4;
  SRLabel onGoingPanel;
  ComboBoxPanel centerchoice;
  SRButton researchbtn;
  Player p;
  GameGUIPanel aGameGUIPanel;
  boolean once = true;
  boolean running = false, doChange = false;
  ResearchPanel aResearchPanel;

  public ResearchLeftPanel(Player p, GameGUIPanel aGameGUIPanel, SpaceRazePanel client) {
    this.p = p;
    this.aGameGUIPanel = aGameGUIPanel;
    
    
    
    setLayout(null);
    setBorder(new LineBorder(StyleGuide.colorCurrent));
    setBackground(StyleGuide.colorBackground);
    
    researchbtn = new SRButton("Research");
    researchbtn.setBounds(5,10,110,20);
    researchbtn.addActionListener(this);
    this.add(researchbtn);

    
    onGoingPanel = new SRLabel("Present research");
    onGoingPanel.setBounds(5,40,110,20);
    add(onGoingPanel);
  }

  public void itemStateChanged(ItemEvent ie){
    if (ie.getStateChange() == ItemEvent.SELECTED){
//      String cname = (String)centerchoice.getSelectedItem();
      
    }
  }


	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equalsIgnoreCase("Research")){
			aResearchPanel.updateData();
		//	aGameGUIPanel.setCurrentPanel(aResearchPanel);
		}
	}
}