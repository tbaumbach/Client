package spaceraze.client.startview;

import java.awt.Dimension;
import java.util.List;

import spaceraze.client.NotifierFrame;
import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.servlethelper.GameData;
import spaceraze.servlethelper.ReturnGames;
import spaceraze.util.general.Logger;

@SuppressWarnings("serial")
public class GameListPanel extends SRBasePanel {
	
	public GameListPanel(List<GameData> gamesData, NotifierFrame notifierFrame){
		final int ROW_HEIGHT = 25;
		setLayout(null);
		setSize(1190, (gamesData.size()+1)*ROW_HEIGHT);
//		setMinimumSize(new Dimension(1190, (gamesData.size()+1)*ROW_HEIGHT));
//		setMaximumSize(new Dimension(1190, (gamesData.size()+1)*ROW_HEIGHT));
		setPreferredSize(new Dimension(1190, (gamesData.size()+1)*ROW_HEIGHT));
		
		// add headers
		String[] columnData = GameData.getColumnHeaders();
		int[] columnWidhts = GameData.getColumnsWidths();

		SRLabel tmpLbl = null;
		int x = 0;
		if (notifierFrame.getReturnGames() == ReturnGames.OWN_AND_OPEN){
			x += 30;
		}
		for (int i = 0; i < columnWidhts.length; i++) {
			tmpLbl = new SRLabel(columnData[i]);
			tmpLbl.setForeground(tmpLbl.getForeground().brighter());
			tmpLbl.setBounds(x, 0, columnWidhts[i], ROW_HEIGHT);
			add(tmpLbl);			
			x += columnWidhts[i];
		}
		
		// add rows
		int counter = 1;
		for (GameData game : gamesData) {
			Logger.finer("GameData from server: " + game.getGameName());
			GameListRowPanel gameListRowPanel = new GameListRowPanel(game,ROW_HEIGHT,notifierFrame);
			gameListRowPanel.setBounds(0, counter*ROW_HEIGHT, getWidth(), ROW_HEIGHT);
			add(gameListRowPanel);			
			counter++;
		}
	}

}
