package spaceraze.client.game.panels.statistics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import spaceraze.client.ColorConverter;
import spaceraze.client.components.SRBasePanel;
import spaceraze.servlethelper.handlers.GameWorldHandler;
import spaceraze.util.general.Logger;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Faction;
import spaceraze.world.Galaxy;
import spaceraze.world.Player;
import spaceraze.world.StatisticType;
import spaceraze.world.Statistics;
import spaceraze.servlethelper.game.StatisticsHandler;

public class StatisticsGraph extends SRBasePanel{
	private static final long serialVersionUID = 1L;
//	private List<Integer> points;
//	private int iterations;
	private StatisticsHandler statisticsHandler;
	private StatisticType statisticType; 
	private String highlightedPlayerName;
	private Statistics currentStatistics;
	private Galaxy galaxy;
	private List<Integer> winLimitList;
	
	public StatisticsGraph(Galaxy galaxy){
		this.statisticsHandler = statisticsHandler;
		this.galaxy = galaxy;
		statisticType = StatisticType.PRODUCTION_PLAYER; 
		currentStatistics = StatisticsHandler.findStatistics(statisticType, galaxy);
		winLimitList = StatisticsHandler.getWinLimit(currentStatistics);
	}
	
	public void drawGraph(StatisticType statisticType, String highlightedPlayerName){
		this.statisticType = statisticType; 
		this.highlightedPlayerName = highlightedPlayerName;
		currentStatistics = StatisticsHandler.findStatistics(statisticType, galaxy);
		if ((statisticType == StatisticType.PRODUCTION_PLAYER) | (statisticType == StatisticType.PRODUCTION_FACTION)){
			winLimitList = StatisticsHandler.getWinLimit(currentStatistics);
		}else{
			winLimitList = null;
		}
		paintComponent(getGraphics());
	}

	public void paintComponent(Graphics g){
		// create buffer
		Dimension d = getSize();
		Image bufferImage = createImage(d.width,d.height);
		Graphics bg = bufferImage.getGraphics();
		// set constants
		int leftBorder = 30;
		int bottomBorder = 30;
		int rightBorder = 10;
		int topBorder = 10;
		// draw graphics
		// draw background
//		bg.setColor(Color.BLACK);
		bg.setColor(Color.BLACK.brighter());
		bg.fillRect(0, 0, d.width, d.height);
		// draw iterations
		if (statisticType != null){
			Logger.fine("statisticType: " + statisticType.toString());
			// get last turn
			int maxTurn = StatisticsHandler.getLastTurn(currentStatistics);
			Logger.fine("Max turn: " + maxTurn);
			int turnInterval = (getWidth()-leftBorder-rightBorder)/maxTurn;
			Logger.fine("turnInterval: " + turnInterval);
			// get max value
			int maxValue = StatisticsHandler.getMaxValue(currentStatistics);
			if (winLimitList != null){
				int maxWinLimit = getMaxWinLimit();
				if (maxWinLimit > maxValue){
					maxValue = maxWinLimit;
				}
			}
			Logger.fine("Max value: " + maxValue);
			if (maxValue == 0){
				maxValue = 1;
			}
			int valueInterval = (getHeight()-bottomBorder-topBorder)/maxValue;
			Logger.fine("valueInterval: " + valueInterval);
			// draw grid
			Color borderAndTextColor = StyleGuide.colorCurrent.darker().darker();
			Color gridColor = borderAndTextColor.darker().darker();
			// horisontal lines
			int yScaleInterval = (maxValue/20)+1;
			int tmpValue = 1;
			int curY = getHeight()-bottomBorder-valueInterval;
			while (curY >= topBorder) {
				if ((tmpValue%yScaleInterval) == 0){
					bg.setColor(borderAndTextColor);
					bg.drawString(String.valueOf(tmpValue), 5, curY+5);
				}
				bg.setColor(gridColor);
				bg.drawLine(leftBorder,curY,d.width-rightBorder,curY);
				curY -= valueInterval;
				tmpValue++;
			}
			// vertical lines
			int xScaleInterval = (maxTurn/20)+1;
			int tmpTurn = 1;
			int curX = leftBorder+turnInterval;
			Logger.fine("curX: " + curX);
			while (curX <= (getWidth()-rightBorder)) {
				if ((tmpTurn%xScaleInterval) == 0){
					String tmpTurnString = String.valueOf(tmpTurn);
					FontMetrics fm = g.getFontMetrics();
					int tmpTurnStringLength = fm.stringWidth(tmpTurnString);
					bg.setColor(borderAndTextColor);
					bg.drawString(tmpTurnString, curX-(tmpTurnStringLength/2), getHeight()-bottomBorder+15);
				}
				bg.setColor(gridColor);
				bg.drawLine(curX,topBorder,curX,getHeight()-bottomBorder);
				curX += turnInterval;
				tmpTurn++;
			}
			// draw border
			bg.setColor(borderAndTextColor);
			bg.drawRect(leftBorder, topBorder, d.width-1-leftBorder-rightBorder, d.height-1-bottomBorder-topBorder);
			// draw graphs
			if (maxTurn == 1){ // first turn
				if (statisticType == StatisticType.PRODUCTION_FACTION){
					for (Faction aFaction : galaxy.getGameWorld().getFactions()) {
						bg.setColor(ColorConverter.getColorFromHexString(aFaction.getPlanetHexColor()));
						List<Integer> values = StatisticsHandler.findPost(aFaction.getName(), currentStatistics).getValues();
						if (values != null){
							int value = values.get(0);
							int valueCoor = getHeight()-bottomBorder-(value*valueInterval);
							bg.drawLine(leftBorder,valueCoor,getWidth()-rightBorder,valueCoor);
						}
					}
				}else{
					for (Player aPlayer : galaxy.getPlayers()) {
						bg.setColor(ColorConverter.getColorFromHexString(GameWorldHandler.getFactionByKey(aPlayer.getFactionKey(), aPlayer.getGalaxy().getGameWorld()).getPlanetHexColor()));
						List<Integer> values = StatisticsHandler.findPost(aPlayer.getName(), currentStatistics).getValues();
						int value = values.get(0);
						int valueCoor = getHeight()-bottomBorder-(value*valueInterval);
						bg.drawLine(leftBorder,valueCoor,getWidth()-rightBorder,valueCoor);
						if (aPlayer.getName().equals(highlightedPlayerName)){
							bg.drawLine(leftBorder,valueCoor+1,getWidth()-rightBorder,valueCoor+1);
							bg.drawLine(leftBorder,valueCoor-1,getWidth()-rightBorder,valueCoor-1);
							bg.setColor(ColorConverter.getColorFromHexString(GameWorldHandler.getFactionByKey(aPlayer.getFactionKey(), aPlayer.getGalaxy().getGameWorld()).getPlanetHexColor()).darker());
							bg.drawLine(leftBorder,valueCoor+2,getWidth()-rightBorder,valueCoor+2);
							bg.drawLine(leftBorder,valueCoor-2,getWidth()-rightBorder,valueCoor-2);
						}
					}
				}
				if (winLimitList != null){
					bg.setColor(StyleGuide.colorNeutralWhite);
					int value = winLimitList.get(0);
					int valueCoor = getHeight()-bottomBorder-(value*valueInterval);
					bg.drawLine(leftBorder,valueCoor,getWidth()-rightBorder,valueCoor);
					bg.drawString("White line is win limit", d.width-150, d.height-bottomBorder-10);
				}
			}else{ // turn 2+
				if (statisticType == StatisticType.PRODUCTION_FACTION){
					for (Faction aFaction : galaxy.getGameWorld().getFactions()) {
						bg.setColor(ColorConverter.getColorFromHexString(aFaction.getPlanetHexColor()));
						List<Integer> values = StatisticsHandler.findPost(aFaction.getName(), currentStatistics).getValues();
						if (values != null){
							int lastValue = values.get(0);
							int lastValueCoor = getHeight()-bottomBorder-(lastValue*valueInterval);
							int lastTurnCoor = leftBorder;
							for (int i = 0; i < values.size(); i++) {
								int aValue = values.get(i);
								int aValueCoor = getHeight()-bottomBorder-(aValue*valueInterval);
								int aTurnCoor = leftBorder+((i+1)*turnInterval);
								bg.drawLine(lastTurnCoor,lastValueCoor,aTurnCoor,aValueCoor);
								lastValueCoor = aValueCoor;
								lastTurnCoor = aTurnCoor;
							}
						}
					}
				}else{
					for (Player aPlayer : galaxy.getPlayers()) {
						bg.setColor(ColorConverter.getColorFromHexString(GameWorldHandler.getFactionByKey(aPlayer.getFactionKey(), aPlayer.getGalaxy().getGameWorld()).getPlanetHexColor()));
						List<Integer> values = StatisticsHandler.findPost(aPlayer.getName(), currentStatistics).getValues();
						int lastValue = values.get(0);
						int lastValueCoor = getHeight()-bottomBorder-(lastValue*valueInterval);
						int lastTurnCoor = leftBorder;
						for (int i = 0; i < values.size(); i++) {
							int aValue = values.get(i);
							int aValueCoor = getHeight()-bottomBorder-(aValue*valueInterval);
							int aTurnCoor = leftBorder+((i+1)*turnInterval);
							bg.drawLine(lastTurnCoor,lastValueCoor,aTurnCoor,aValueCoor);
							if (aPlayer.getName().equals(highlightedPlayerName)){
								bg.drawLine(lastTurnCoor,lastValueCoor+1,aTurnCoor,aValueCoor+1);
								bg.drawLine(lastTurnCoor,lastValueCoor-1,aTurnCoor,aValueCoor-1);							
								bg.setColor(ColorConverter.getColorFromHexString(GameWorldHandler.getFactionByKey(aPlayer.getFactionKey(), aPlayer.getGalaxy().getGameWorld()).getPlanetHexColor()).darker());
								bg.drawLine(lastTurnCoor,lastValueCoor+2,aTurnCoor,aValueCoor+2);
								bg.drawLine(lastTurnCoor,lastValueCoor-2,aTurnCoor,aValueCoor-2);							
								bg.setColor(ColorConverter.getColorFromHexString(GameWorldHandler.getFactionByKey(aPlayer.getFactionKey(), aPlayer.getGalaxy().getGameWorld()).getPlanetHexColor()));
							}
							lastValueCoor = aValueCoor;
							lastTurnCoor = aTurnCoor;
						}
					}
				}
				if (winLimitList != null){
					bg.setColor(StyleGuide.colorNeutralWhite);
					int lastValue = winLimitList.get(0);
					int lastValueCoor = getHeight()-bottomBorder-(lastValue*valueInterval);
					int lastTurnCoor = leftBorder;
					for (int i = 0; i < winLimitList.size(); i++) {
						int aValue = winLimitList.get(i);
						int aValueCoor = getHeight()-bottomBorder-(aValue*valueInterval);
						int aTurnCoor = leftBorder+((i+1)*turnInterval);
						bg.drawLine(lastTurnCoor,lastValueCoor,aTurnCoor,aValueCoor);
						lastValueCoor = aValueCoor;
						lastTurnCoor = aTurnCoor;
					}
					bg.drawString("White line is win limit", d.width-150, d.height-bottomBorder-10);
				}
			}
		}
		// draw buffer
		g.drawImage(bufferImage,0,0,this);
	}
	
	private int getMaxWinLimit(){
		int maxWinLimit = 0;
		for (Integer anInteger : winLimitList) {
			if (anInteger > maxWinLimit){
				maxWinLimit = anInteger;
			}
		}
		return maxWinLimit;
	}
	
}
