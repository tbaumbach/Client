package spaceraze.client.game;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import spaceraze.client.ColorConverter;
import spaceraze.client.NotifierFrame;
import spaceraze.client.components.SRBasePanel;
import spaceraze.client.game.panels.login.LoginFactionPanel;
import spaceraze.client.game.panels.login.LoginPanelFull;
import spaceraze.client.game.panels.login.LoginPanelSimple;
import spaceraze.client.game.panels.login.PlayerCreatedMessagePanel;
import spaceraze.client.game.panels.login.PlayerSavedPanel;
import spaceraze.client.panels.GeneralMessagePanel;
import spaceraze.servlethelper.game.TransferWrapper;
import spaceraze.util.general.Logger;
import spaceraze.util.general.StyleGuide;
import spaceraze.util.properties.PropertiesHandler;
import spaceraze.world.Faction;
import spaceraze.world.GameWorld;
import spaceraze.world.Message;
import spaceraze.world.Player;

/**
 * <p>
 * Title: SpaceRaze 3.0
 * </p>
 * <p>
 * Description: SpaceRaze with Swing GUI
 * </p>
 * 
 * @author Paul Bodin
 * @version 3.0
 */

@SuppressWarnings("serial")
public class SpaceRazePanel extends SRBasePanel {
	private LoginPanelSimple lps;
	private LoginPanelFull lpf;
	public static ImageHandler imageHandler;
	private Player p;
	// private ImageIcon factionEmblem;
	private boolean finished = false;
	private GameGUIPanel gameGuiPanel;
	private String returnTo, returnToDelete;
	private int gameId = -1;
	private String message, startMessage;
	private String autoUser;
	private List<Faction> openFactions;
	private boolean randomFaction = false;
	private GeneralMessagePanel pcmp;
	private ProgressPanel pp, pp2;
	private int contentLength;
	private boolean setLoginAtStart = false;
	private GameWorld gameWorld;
	private List<Message> sentMessage;
	private List<Message> receivedMessage;
	LoginFactionPanel aLoginFactionPanel;
	private Map<String, String> applicationParams;
	private NotifierFrame notifierFrame;
	// TODO 2019-11-21 pathen blir fel när app körs från eclipse. Rätta till det här
	// när den sedan körs från .jar eller packad som ett program, alternativt kört
	// via build i pom.
	// public static String imagesPath = "resources/images/";
	public static String imagesPath = "images/";
	private static String tunnelPath;
	private static String port;
	private static String codebase;

	public SpaceRazePanel() {
		// is called by browser, do nothing
	}

	public SpaceRazePanel(Map<String, String> applicationParams, NotifierFrame notifierFrame) {
		setLayout(null);
		setBackground(StyleGuide.colorBackground);
		this.applicationParams = applicationParams;
		this.notifierFrame = notifierFrame;
	}

	public boolean isRunAsApplication() {
		return applicationParams != null;
	}

	// public void init2() {
	// getContentPane().setLayout(null);
	//
	// JPanel tmpPanel = new JPanel();
	// tmpPanel.setBounds(10,10,200,200);
	// tmpPanel.setBackground(Color.BLUE);
	// tmpPanel.setLayout(null);
	//
	// JPanel tmpPanel2 = new JPanel();
	// tmpPanel2.setBounds(100,10,200,200);
	// tmpPanel2.setBackground(Color.red);
	// tmpPanel.add(tmpPanel2);
	//
	// getContentPane().add(tmpPanel);
	//
	// }
	//
	// @Override
	public String getParameter(String paramName) {
		Logger.fine("Get parameter: " + paramName);
		String paramValue = null;
		if (applicationParams != null) {
			paramValue = applicationParams.get(paramName);
			Logger.fine("applicationParams, value: " + paramValue);
		}
		return paramValue;
	}

	// @Override
	public URL getCodeBase() {
		if (SpaceRazePanel.codebase == null) {
			SpaceRazePanel.codebase = getParameter("codebase");
		}
		URL codebase = null;
		try {
			codebase = new URL(SpaceRazePanel.codebase);
		} catch (MalformedURLException mue) {
			Logger.severe("felaktig URL: " + SpaceRazePanel.codebase);
		}
		return codebase;
	}

	// @Override
	public Image getImage(URL documentBase, String imageFileName) {
		Logger.fine("getImage overridden called: " + documentBase.toString() + ", " + imageFileName);
		Image aImage = null;
		if (isRunAsApplication()) {
			ClassLoader classLoader = this.getClass().getClassLoader();
			URL url = classLoader.getResource(imagesPath + imageFileName);
			Logger.fine("URL: " + url.toString());
			aImage = Toolkit.getDefaultToolkit().getImage(url);
		} else {
			// aImage = super.getImage(documentBase, imageFileName);
		}
		return aImage;
	}

	// @Override
	public URL getDocumentBase() {
		Logger.fine("Get documentbase from getCodeBase()");
		return getCodeBase();
		// URL documentbase = null;
		// if (applicationParams != null){
		// try{
		// documentbase = new URL(applicationParams.get("codebase"));
		// Logger.fine("applicationParams, value: " + documentbase.toString());
		// }catch(MalformedURLException mue){
		// Logger.severe("felaktig URL: " + applicationParams.get("codebase"));
		// }
		// }else{
		// documentbase = super.getDocumentBase();
		// Logger.fine("super.getDocumentBase, value: " + documentbase.toString());
		// }
		// return documentbase;
	}

	public void init() {
		Logger.info("");
		Logger.info("SpaceRazePanel started");
		Logger.info("init - starting login sequence");
		Logger.info("Time: " + new Date().toString());
		try {
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		} catch (Exception e) {
			e.getStackTrace();
		}
		int turn = -1;
		// Först parsa params
		try {
			gameId = Integer.parseInt(getParameter("gameId"));
		} catch (NumberFormatException e) {
			Logger.severe("Parsning av portnummer-parametrar misslyckades: ");
		}
		// where to return to when turn is saved
		returnTo = getParameter("returnto");
		returnToDelete = getParameter("returnto_delete");
		autoUser = getParameter("autouser");
		Logger.fine("returnTo: " + returnTo);
		// Koppla upp till servern
		turn = getTurn2();
		// ladda alla bilder som klienten använder
		imageHandler = new ImageHandler(this);
		// visa olika paneler beroende på om det är ett nystartat spel eller inte
		// TODO 2019-11-22 Removing Applet
		// getRootPane().getContentPane().setLayout(null);
		// getContentPane().setBackground(Color.black);
		// TODO 2019-11-22 Removing Applet: möjligt att den behövs när ett nytt drag
		// görs och factionens färg ska visas
		// StyleGuide.reset();

		String userName = getParameter("username");
		String userPassword = getParameter("userpassword");
		Logger.fine("init: " + userName + " " + userPassword);
		if (turn > 0) {
			Logger.fine("init: Turn > 0");
			if ((userName != null) && (userPassword != null) && (!userName.equals("")) && (!userPassword.equals(""))) {
				Logger.fine("init: auto login!");
				String message = "oldlogin ";
				// setLogin(message + userName + " " + userPassword);
				setLoginAtStart = true;
				startMessage = message + userName + " " + userPassword;
				// show progress counter panel
				pp2 = new ProgressPanel(p, "Loading game from server:");
				pp2.setBounds(150, 100, 600, 315);

				add(pp2);
				// update(getGraphics());

			} else {
				Logger.fine("init: manual login");
				lps = new LoginPanelSimple(this);
				lps.setBounds(375, 100, 165, 198);
				add(lps);

				lps.setNamefieldFocus();
			}
		} else {
			if (!randomFaction) {
				aLoginFactionPanel = new LoginFactionPanel(gameWorld);
				aLoginFactionPanel.setBounds(400, 100, 600, 600);

				add(aLoginFactionPanel);
				// update(getGraphics());
			}

			Logger.fine("init: Turn = 0");
			lpf = new LoginPanelFull(this, userName, userPassword, openFactions, randomFaction);
			if ((userName != null) && (userPassword != null) && (!userName.equals("")) && (!userPassword.equals(""))) {
				lpf.setBounds(155, 100, 210, 210);
			} else {
				lpf.setBounds(155, 100, 210, 315);
			}
			lpf.setBorder(new LineBorder(StyleGuide.colorNeutral));
			add(lpf);

			if ((userName != null) && (userPassword != null) && (!userName.equals("")) && (!userPassword.equals(""))) {
				lpf.setGovenorfieldFocus();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
							lpf.setNamefieldFocus();
						} catch (Exception e) {
							// empty
						}
					}
				});
			}

		}
		Logger.fine("init finished, time: " + new Date().toString());
	}

	public void showFaction(String factionName) {
		aLoginFactionPanel.valueChanged(factionName);
	}

	public void start() {
		if (setLoginAtStart) {
			setLogin(startMessage);
			if (pp2 != null) {
				remove(pp);

				pp2 = null;
			}
			// update(getGraphics());
		}
	}

	public void setLogin(String message) {
		Logger.fine("setLogin called, message: " + message);
		this.message = message;
		// dölj loginpanelen
		if (lpf != null) {
			lpf.setVisible(false);
			if (aLoginFactionPanel != null) {
				remove(aLoginFactionPanel);
			}
			remove(lpf);

		} else if (lps != null) {
			lps.setVisible(false);
		}
		p = getPlayer2(message);
		Logger.fine("Data recieved successfully.");
		if (p.getErrorMessage() != null) {
			GeneralMessagePanel pcmp = new GeneralMessagePanel("Server could not return player data",
					"CAUSE OF ERROR: " + p.getErrorMessage());
			pcmp.setBounds(150, 100, 650, 315);
			add(pcmp);
			//// update(getGraphics());

			Logger.severe("ERROR: server could not return player data");
			Logger.severe("CAUSE OF ERROR: " + p.getErrorMessage());
		} else {
			// setFactionEmblem();
			setFactionColor();
			if (p.getGalaxy().getTurn() == 0) {
				// sendPlayer2();
				if (message.substring(0, 8).equals("oldlogin")) {
					GeneralMessagePanel pcmp = new GeneralMessagePanel(
							"Game has not started yet - server still in joining phase",
							"Wait until server is updated to the first gameturn");
					pcmp.setBounds(150, 100, 650, 315);
					add(pcmp);
					// update(getGraphics());

				} else {
					showMessage();
				}
			} else {
				p.setFinishedThisTurn(true); // default when "Save & Close" is pressed is that the player is finished
												// with this turn
				if (pp2 != null) {
					remove(pp2);

					pp2 = null;
				}
				receivedMessage = getPlayerMessages();
				sentMessage = getPlayerSentMessages();
				showGUI();
				p.getGalaxy().getDiplomacy().logDiplomacyStates();
			}
		}
	}

	private void setFactionColor() {
		StyleGuide.colorCurrent = ColorConverter.getColorFromHexString(p.getFaction().getPlanetHexColor());
	}

	// Används bara för drag 0
	public void showMessage() {
		Logger.finer("showMessage anropad");
		PlayerCreatedMessagePanel pcmp = new PlayerCreatedMessagePanel(p);
		pcmp.setBounds(250, 100, 400, 315);
		// pcmp.setBorder(new LineBorder(StyleGuide.colorNeutral));

		add(pcmp);
		Logger.fine(getSize().toString());
		// update(getGraphics());

		showOkMessageAndExit();
	}

	// Används för drag 1+
	public void showGUI() {
		Logger.info("showGUI anropad");

		gameGuiPanel = new GameGUIPanel("används ej?", p, this, imageHandler);
		gameGuiPanel.setBounds(0, 0, getSize().width, getSize().height);

		// TODO 2019-11-22 Removing Applet
		add(gameGuiPanel);
		// getContentPane().add(gameGuiPanel);
		// update(getGraphics());

	}

	private String sendPlayer2() {
		//TODO 2019 12-13 Dessa två konstanter ska göras om och delas med SpaceRazeTunnel där de ursprungligen kommer ifrån
		String NOT_SAVED_ALREADY_UPDATED = "Player not saved by tunnel, server has already updated";
		String SERVER_ERROR = "Player saved, but error occured in server when updating";
		String ok = "ok";
		Logger.info("sendPlayer2 new called: ");
		TransferWrapper tw = getResponse(p, message, pp, null);
		if (tw != null) {
			String message = (String) tw.getReturnObject();
			Logger.info("Server answers: " + message);
			if (message.equals(NOT_SAVED_ALREADY_UPDATED)) {
				ok = NOT_SAVED_ALREADY_UPDATED;
			} else {
				if (message.equals(SERVER_ERROR)) {
					ok = SERVER_ERROR;
				}
			}
		} else {
			ok = "Cannot connect to server";
		}
		return ok;
	}

	@SuppressWarnings("unchecked")
	private int getTurn2() {
		Logger.info("getTurn2 new called: ");
		TransferWrapper tw = getResponse(null, null, null, null);
		int turn = tw.getTurn();
		contentLength = tw.getContentSize();
		Logger.info("Contentlength from server: " + contentLength);
		if (turn == 0) {
			openFactions = (List<Faction>) tw.getReturnObject();
			randomFaction = tw.getMessage().equals("randomfaction");
			gameWorld = tw.getGameWorld();
			Logger.finer("openFactions == null: " + (openFactions == null));
		}
		Logger.info("Server answers, turn returned: " + turn);
		return turn;
	}

	@SuppressWarnings("unchecked")
	private List<Message> getPlayerMessages() {
		Logger.info("getPlayerMessages new called: ");
		TransferWrapper tw = getResponse(p, "getPlayerMessages", null, null);
		List<Message> returnMessage = (List<Message>) tw.getReturnObject();
		if (returnMessage.size() > 0) {
			p.setLatestMessageIdFromServer(returnMessage.get(0).getUniqueId());
		}
		return returnMessage;
	}

	@SuppressWarnings("unchecked")
	private List<Message> getPlayerSentMessages() {
		Logger.info("getPlayerSentMessages new called: ");
		TransferWrapper tw = getResponse(p, "getPlayerSentMessages", null, null);
		List<Message> returnMessage = (List<Message>) tw.getReturnObject();
		return returnMessage;
	}

	private Player getPlayer2(String aMessage) {
		Logger.info("getPlayer2 new called: ");
		TransferWrapper tw = getResponse(null, aMessage, null, null);
		Player tmpPlayer = (Player) tw.getReturnObject();
		Logger.info("Server answers, player returned: " + tmpPlayer.getName());
		return tmpPlayer;
	}

	private String getTunnelURL() {
		if (SpaceRazePanel.tunnelPath != null) {
			return SpaceRazePanel.tunnelPath;
		}
		URL codeBase = getCodeBase();

		if (SpaceRazePanel.port == null) {
			String port;
			if (isRunAsApplication()) {
				port = getParameter("port");
			} else {
				port = PropertiesHandler.getProperty("port");
			}
			// Logger.info("Port read from properties file: \"" + port + "\"");
			if (port.equals("")) {
				Logger.info("Port not found, using default port = 80: ");
				port = "80";
			}
			SpaceRazePanel.port = port;
		}
		// String tunnelUrlString = codeBase.getProtocol() + "://" + codeBase.getHost()
		// + ":" + codeBase.getPort();
		String tunnelUrlString = codeBase.getProtocol() + "://" + codeBase.getHost() + ":" + port;
		// Logger.fine("tunnelUrlString: " + tunnelUrlString);
		// the code below which computes the path relies on that the client exist in a
		// sub folder to the spaceraze root folder
		// Logger.config("codeBase.getPath(): " + codeBase.getPath());
		int webbIndex = codeBase.getPath().substring(0, codeBase.getPath().length() - 1).lastIndexOf("/");
		// Logger.fine("webbIndex: " + webbIndex);
		String tmpPath = codeBase.getPath().substring(0, webbIndex);
		// Logger.fine("tmpPath: " + tmpPath);
		tunnelUrlString = tunnelUrlString + tmpPath + "/servlet/sr.tunnel.SpaceRazeTunnel";
		// Logger.fine("getTunnelURL returns: " + tunnelUrlString);
		return tunnelUrlString;
	}

	public TransferWrapper getResponse(Player p, String message, ProgressPanel pp, Message aMessage) {
		// Logger.info("getResponse called, time: " + new Date().toString());
		URL server = null;
		try {
			String tpath = getTunnelURL();
			// Logger.info("SpaceRazeApplet get tunnel path: " + tpath);
			server = new URL(tpath);
		} catch (MalformedURLException e) {
		}
		// Object result = null;
		URLConnection con;
		TransferWrapper tw = null;
		try {
			con = server.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/octet-stream");
			// Logger.fine("zipping outputstream ver 2...");
			GZIPOutputStream gzos = new GZIPOutputStream(con.getOutputStream());
			ObjectOutputStream request = new ObjectOutputStream(gzos);

			tw = new TransferWrapper(aMessage, p, message, gameId);

			// Alt 1: skicka hela objektet
			// request.writeObject(tw);

			// Alt 2: skicka data bit för bit
			// först skapa en array med all data
			// Logger.finer("Alt 2: skicka data bit f�r bit");
			byte[] buf = null;
			try {
				// Serialize to a byte array
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(tw);
				out.close();

				// Get the bytes of the serialized object
				buf = bos.toByteArray();
				// Logger.finer("Buffer length: " + buf.length);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// int contentLength = con.getContentLength();

			// sedan skriv från arrayen till utströmmen
			int writtenSoFar = 0;
			int arrSize = buf.length;
			final int BLOCK_SIZE = 10 * 1024;
			try {
				int lengthToWrite = 0;
				while (writtenSoFar < arrSize) {
					if ((arrSize - writtenSoFar) < BLOCK_SIZE) {
						lengthToWrite = arrSize - writtenSoFar;
					} else {
						lengthToWrite = BLOCK_SIZE;
					}
					// Logger.finest("lengthToWrite: " + lengthToWrite);
					request.write(buf, writtenSoFar, lengthToWrite);
					writtenSoFar += lengthToWrite;
					// Logger.finest("writtenSoFar: " + writtenSoFar);
					if (pp != null) {
						pp.setCounter(writtenSoFar + "/" + arrSize);
						// Logger.finest("Progress: " + writtenSoFar + "/" + arrSize);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// slut Alt 2

			// Logger.fine("tw written");
			request.flush();
			request.close();
			// Logger.fine("request flush & close ");
			// get the result input stream
			ObjectInputStream response = null;
			// BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
			// int contentLength = con.getContentLength();
			GZIPInputStream gzis = new GZIPInputStream(con.getInputStream());
			try {
				response = new ObjectInputStream(gzis);
			} catch (EOFException eofe) {
				Logger.severe("EOFException!!!" + eofe.toString());
				eofe.printStackTrace();
			}

			// Logger.finer("Creating objectInputStream");
			// ObjectInputStream response = new ObjectInputStream(new
			// BufferedInputStream(con.getInputStream()));
			// Logger.finer("objectInputStream created");
			// read response back from the server
			// result = response.readObject();

			// Alt 2: hämta data från en byte array
			byte[] buffer = new byte[15 * 1000 * 1024];

			// sedan läs från inströmmen till arrayen
			int readSoFar = 0;
			final int BLOCK_SIZE_2 = 10 * 1024;
			try {
				int lengthRead = response.read(buffer, readSoFar, BLOCK_SIZE_2);
				// Logger.finer("lengthRead: " + lengthRead);
				while (lengthRead > -1) {
					readSoFar += lengthRead;
					lengthRead = response.read(buffer, readSoFar, BLOCK_SIZE_2);
					if (pp2 != null) {
						pp2.setCounter(readSoFar + "/" + contentLength);
					}
				}
				// Logger.finest("Progress done: " + readSoFar + "/" + contentLength);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// trim array to size read
			byte[] buffer2 = new byte[readSoFar];
			for (int i = 0; i < readSoFar; i++) {
				buffer2[i] = buffer[i];
			}
			buffer = buffer2;
			// Logger.finer("trimmed to: " + readSoFar);

			// hämta ut objekten från arrayen
			try {
				// Deserialize from a byte array
				// Logger.finer("create objects from array");
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
				// Logger.finer("ObjectInputStream created");
				tw = (TransferWrapper) in.readObject();
				// Logger.finer("readObject ok!");
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			response.close();
			// LoggingHandler.info("result == null: " + (result == null));
			// tw = (TransferWrapper)result;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// Logger.info("getResponse finished, time: " + new Date().toString());
		return tw;
	}

	public void finishedSendPressed() {
		Logger.fine("finishedSendPressed() called");
		finished = true;
		// first fetch notes
		Logger.finer("get notes...");
		p.setNotes(gameGuiPanel.getNotes());
		Logger.finer("ok!");
		gameGuiPanel.setVisible(false);

		update(getGraphics());

		// get planet notes
		gameGuiPanel.savePlanetNotes();
		// p.getPlanetInfos().setNotes(gameGuiPanel.getCurrentPlanet().getName(),gameGuiPanel.getPlanetNotes());
		// show progress counter panel
		pp = new ProgressPanel(p, "Saving game to server:");
		pp.setBounds(150, 100, 600, 315);

		add(pp);
		// update(getGraphics());

		// send player
		Logger.fine("sending...");
		String saveOk = sendPlayer2();
		Logger.fine("ok!");
		pp.setVisible(false);

		remove(pp);

		// show message panel (good or bad)
		if (saveOk.equalsIgnoreCase("ok")) {
			PlayerSavedPanel psp = new PlayerSavedPanel(p);
			psp.setBounds(150, 100, 600, 315);

			add(psp);
			// update(getGraphics());

			showOkMessageAndExit();
		} else {
			// GeneralMessagePanel pcmp = new GeneralMessagePanel("Server could not save
			// player data","CAUSE OF ERROR: " + saveOk);
			pcmp = new GeneralMessagePanel("Could not save player data", "CAUSE OF ERROR: " + saveOk);
			pcmp.setBounds(150, 100, 650, 315);

			add(pcmp);
			// update(getGraphics());

			showErrorMessageAndReturn();
		}
		Logger.info("finishedSendPressed() finished");
	}

	public boolean getFinished() {
		return finished;
	}

	public void updateTreasuryLabel() {
		gameGuiPanel.updateTreasuryLabel();
	}

	public void showUnreadMessage(boolean openPopup) {
		gameGuiPanel.showUnreadMessage(openPopup);
	}

	public void showShiptypeDetails(String aShipType, String faction) {
		gameGuiPanel.showShiptypeDetails(aShipType, faction);
	}

	public void showTroopTypeDetails(String aTroopType, String faction) {
		gameGuiPanel.showTroopTypeDetails(aTroopType, faction);
	}

	public void showVIPTypeDetails(String aVIPType, String faction) {
		gameGuiPanel.showVIPTypeDetails(aVIPType, faction);
	}

	public void showBuildingTypeDetails(String aBuildingType, String faction) {
		gameGuiPanel.showBuildingTypeDetails(aBuildingType, faction);
	}

	public void showBattleSim() {
		gameGuiPanel.showBattleSim();
	}

	public void showLandBattleSim() {
		gameGuiPanel.showLandBattleSim();
	}
	/*
	 * public void showVIPDetails(VIPType aVIPType){
	 * gameGuiPanel.showVIPTypeDetails(aVIPType); }
	 */

	public void updateMap() {
		gameGuiPanel.updateMap();
	}

	public void showErrorMessageAndReturn() {
		Logger.finer("applet thread started");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		remove(pcmp);

		gameGuiPanel.showSendButton();
		gameGuiPanel.setVisible(true);

		// update(getGraphics());
	}

	public void showOkMessageAndExit() {
		Logger.finer("applet thread started");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (notifierFrame != null) {
			notifierFrame.hideSpaceRazePanel();
		}
	}

	public void addToBattleSim(String shipsString, String side) {
		gameGuiPanel.addToBattleSim(shipsString, side);
	}

	public void addToLandBattleSim(String troopsString, String side) {
		gameGuiPanel.addToLandBattleSim(troopsString, side);
	}

	public List<Message> getSentMessage() {
		return sentMessage;
	}

	public void setSentMessage(List<Message> sentMessage) {
		this.sentMessage = sentMessage;
	}

	public List<Message> getReceivedMessage() {
		return receivedMessage;
	}

	public void setReceivedMessage(List<Message> receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

	public void showNewMailImage(boolean show) {
		this.gameGuiPanel.showNewMailImage(show);
	}

}
