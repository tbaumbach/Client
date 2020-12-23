package spaceraze.client;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import spaceraze.client.components.SRButton;
import spaceraze.client.components.SRTextField;
import spaceraze.client.game.SpaceRazePanel;
import spaceraze.client.mapeditor.MapEditorPanel;
import spaceraze.client.startview.GameListPanel;
import spaceraze.client.startview.NewGamePanel;
import spaceraze.servlethelper.CreateNewGameData;
import spaceraze.servlethelper.GameData;
import spaceraze.servlethelper.NotifierTransferWrapper;
import spaceraze.servlethelper.ReturnGames;
import spaceraze.util.general.Logger;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.GameWorld;
//import sr.client.SpaceRazePanel;
import spaceraze.client.panels.GameWorldInfoPanel;
import spaceraze.client.game.map.MapInfoPanel;
//import sr.notifier.NotifierTransferWrapper;
import spaceraze.servlethelper.handlers.GameWorldHandler;

// nya : pabod 5by5 http://localhost:8080/SpaceRaze/ 8080
// Paul: gamla tunnelurl parametern: http://localhost:8080/SpaceRaze/servlet/sr.notifier.NotifierTunnel
// för att gå mot prod: pabod 5by5 http://www.spaceraze.com/

@SuppressWarnings("serial")
public class NotifierFrame extends JFrame implements Runnable,ActionListener{
//	private JLabel userLbl;
	private Image red,yellow,green,question,currentIconImage;
	private JLabel statusLabel;
	private SRButton showGamesBtn, minimizeBtn, newGameBtn, editMapBtn;
	private SRTextField mapName;
	private GameListPanel gameListPanel;	
	private Thread t;
	private String user,password;
	private String tunnelPath;
	private TrayIcon trayIcon;
	private ReturnGames returnGames;
	private SpaceRazePanel spazeRazePanel;
	private MapEditorPanel mapPanel;
	private String port;
	private List<spaceraze.world.Map> maps;
	private NewGamePanel newGamePanel;
	private MapInfoPanel mapInfoPanel;
	private GameWorldInfoPanel gameWorldInfoPanel;
	private JScrollPane scrollPane;
	public static String IMAGES_PATH = "images/";

	public NotifierFrame(String aUser, String aPassword, String tunnelPath, boolean showFrameOnStartup, String port){
		super("SpaceRaze - " + aUser);
		Logger.fine("constructor called: " + aUser + ", " + showFrameOnStartup + ", " + tunnelPath + ", " + port);
		this.port = port;
		if(showFrameOnStartup){
			returnGames = ReturnGames.ALL; // om den är startad via utv-miljö, visa alla partier
		}else{
			returnGames = ReturnGames.OWN_AND_OPEN;
		}

		loadImages();
		setIcon(question);
		
		setLocation(100,100);
		setSize(1218,744);
		setLayout(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		Container c = getContentPane();
		c.setBackground(new Color(0,0,0));
		
		ImageIcon ii = new ImageIcon(question); 
		
		statusLabel = new JLabel("Starting...",ii,JLabel.LEFT);
		statusLabel.setBounds(7,10,320,20);
		setColorAndFont(statusLabel);
		c.add(statusLabel);
		
		showGamesBtn = new SRButton("Show games");
		showGamesBtn.setBounds(10,40,110,20);
		showGamesBtn.addActionListener(this);		
		c.add(showGamesBtn);

		if (SystemTray.isSupported()){
			try{
				SystemTray.getSystemTray(); // testa om säkerhetsinställningarna tillåter att man har tillgång till system tray
				minimizeBtn = new SRButton("Minimize");
				minimizeBtn.setBounds(130,40,100,20);
				minimizeBtn.addActionListener(this);
				c.add(minimizeBtn);
			}catch(AccessControlException ace){
				Logger.info("Security does not allow access to system tray");
			}
		}

		newGameBtn = new SRButton("New Game");
		newGameBtn.setBounds(240,40,100,20);
		newGameBtn.addActionListener(this);
		c.add(newGameBtn);
		
		editMapBtn = new SRButton("Edit Map");
		editMapBtn.setBounds(400,40,100,20);
		editMapBtn.addActionListener(this);
		c.add(editMapBtn);
		
		mapName = new SRTextField("Map name");
		mapName.setBounds(520,40,100,20);
		c.add(mapName);

		user = aUser;
		Logger.info("User set to: " + user);

		password = aPassword;
		Logger.info("Password set to: " + password);

		this.tunnelPath = tunnelPath;

		setVisible(true);
		
		t = new Thread(this);
		t.start();
	}

	private void setColorAndFont(Component c){
		c.setForeground(new Color(255,191,0));
		c.setBackground(new Color(0,0,0));
		c.setFont(new Font("Dialog",1,12));
	}
	
	private void loadImages(){
		ClassLoader classLoader = this.getClass().getClassLoader();
		// Bara för testsyfte, kör main från IDE
		URL url = classLoader.getResource("images/" + "questionmark.gif");
		//Ska används när den kör via jar.
		//URL url = classLoader.getResource(SpaceRazePanel.imagesPath + "questionmark.gif");
		Logger.fine("url questionmark.gif: " + url.toString());
		question = Toolkit.getDefaultToolkit().getImage(url);
		url = classLoader.getResource(SpaceRazePanel.imagesPath + "check.gif");
		green = Toolkit.getDefaultToolkit().getImage(url);
		url = classLoader.getResource(SpaceRazePanel.imagesPath + "saved.gif");
		yellow = Toolkit.getDefaultToolkit().getImage(url);
		url = classLoader.getResource(SpaceRazePanel.imagesPath + "cross.gif");
		red = Toolkit.getDefaultToolkit().getImage(url);
	}
	
	/**
	 * Perform infinite loop with pause, and update status
	 */
	public void run(){
		while(true){
			checkWithServer();
			update(getGraphics());
			try {
				if (returnGames == ReturnGames.ALL){ // when running in testing, no automatic updates are required
					Thread.sleep(Integer.MAX_VALUE);
				}else{
					// sleep 5 minutes before next update
					//Thread.sleep(5*60*1000);
					Thread.sleep(50*60*1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}

	public String deleteGame(String gameName){
		NotifierTransferWrapper wrapper = getResponse(null,0,gameName,true,false,null); 
		String status = wrapper.getReturnCode();
		return status;
	}

	public String updateGame(int changeTurns, String gameName){
		NotifierTransferWrapper wrapper = getResponse(null,changeTurns,gameName,false,false,null); 
		String status = wrapper.getReturnCode();
		return status;
	}
		
	public void checkWithServer(){
		NotifierTransferWrapper wrapper = getResponse(user,0,null,false,false,null); 
		String status = wrapper.getReturnCode();
		Logger.info("Reply from server: " + status);
		if (status.equalsIgnoreCase("error")){
			// can not contact server
			statusLabel.setText("Can't contact server");
			statusLabel.setIcon(new ImageIcon(question));
			setIconImage(question);
		}else
		if (status.equalsIgnoreCase("u")){
			// user not found. Show red X icon
			statusLabel.setText("User not found on server");
			statusLabel.setIcon(new ImageIcon(question));
			setIcon(question);
		}else
		if (status.equalsIgnoreCase("x")){
			// at least one game is not saved or finished. Show red X icon
			statusLabel.setText("Unfinished game(s) exist");
			statusLabel.setIcon(new ImageIcon(red));
			setIcon(red);
		}else
		if (status.equalsIgnoreCase("s")){
			// no game is not finished, and at leats one game is saved. Show yellow check icon 
			statusLabel.setText("Saved game(s) exist");
			statusLabel.setIcon(new ImageIcon(yellow));
			setIcon(yellow);
		}else{ // return "n"
			// no turns to perform, show check green icon
			statusLabel.setText("No games to perform");
			statusLabel.setIcon(new ImageIcon(green));
			setIcon(green);
		}
		if (wrapper.getGameListData() != null){
			Logger.fine("wrapper.getGameListData(), size: " + wrapper.getGameListData().getGames().size());
			updateGameList(wrapper.getGameListData().getGames());
		}
	}
	
	public List<spaceraze.world.Map> getMaps(){
		if (maps == null){
			NotifierTransferWrapper wrapper = getResponse(null,0,null,false,true,null); 
			maps = wrapper.getAllMaps();
		}
		return maps;
	}
	
	private void updateGameList(List<GameData> games){
		if (gameListPanel != null){
			remove(gameListPanel);
		}
		if (scrollPane != null){
			remove(scrollPane);
		}
		gameListPanel = new GameListPanel(games,this);
		if (games.size() > 24){
//			gameListPanel = new GameListPanel(games,this);
			// create scrollpane
			scrollPane = new JScrollPane(gameListPanel);
			scrollPane.setLocation(10, 80);
			scrollPane.setSize(1185, 25*25);
			add(scrollPane);
			// workaround to get ViewportView to show
			setVisible(false);
			repaint();
			setVisible(true);
		}else{// less or equal to 24 games
			gameListPanel.setLocation(10, 80);
			add(gameListPanel);
		}
		// always repaint the frame
		repaint();
	}
	
	private void setIcon(Image anImage){
		currentIconImage = anImage;
		setIconImage(anImage);
		if (trayIcon != null){
			trayIcon.setImage(anImage);
		}
	}

	public String createNewGame(CreateNewGameData createNewGameData){
		Logger.fine("createNewGameData, new game name: " + createNewGameData.getGameName());
		NotifierTransferWrapper wrapper = getResponse(null,0,null,false,false,createNewGameData); 
		String status = wrapper.getReturnCode();
		Logger.fine("Status: " + status);
		return status;
	}

	private NotifierTransferWrapper getResponse(String userLogin, int changeTurns, String gameName,boolean deleteGame, boolean getAllMaps, CreateNewGameData createNewGameData){
	  	Logger.fine("getResponse called");
	  	URL server = null;
	  	NotifierTransferWrapper responseWrapper = new NotifierTransferWrapper();
	  	responseWrapper.setReturnCode("error"); // default if nothing else is returned
	  	try {
	  		String tmpTunnelPath = tunnelPath + "notifier/sr.notifier.NotifierTunnel";
	  		Logger.fine("Tunnel path: " + tmpTunnelPath);
	  		server = new URL(tmpTunnelPath);
	  		ObjectInputStream response = null;
	  		Object result = null;
	  		URLConnection con;
			con = server.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/octet-stream");

			ObjectOutputStream request = new ObjectOutputStream(new BufferedOutputStream(con.getOutputStream()));
			NotifierTransferWrapper wrapper = new NotifierTransferWrapper();
			if (createNewGameData != null){
				wrapper.setCreateNewGameData(createNewGameData);
			}else
			if (getAllMaps){
				wrapper.setGetAllMaps(true);
			}else
			if (deleteGame){
				wrapper.setDeleteGame(true);
				wrapper.setGameName(gameName);
			}else
			if (userLogin != null){
				wrapper.setUserLogin(userLogin);
				if (isVisible()){
					wrapper.setReturnGames(returnGames);
				}
			}else{ // update game
				wrapper.setChangeTurn(changeTurns);
				wrapper.setGameName(gameName);
			}
			request.writeObject(wrapper);
			Logger.fine("userName written");
			request.flush();
			request.close();
			Logger.fine("request flush & close ");
			// get the result input stream
			response = new ObjectInputStream(new BufferedInputStream(con.getInputStream()));
			Logger.fine("objectInputStream created: ");
			// read response back from the server
			result = response.readObject();
		  	responseWrapper = (NotifierTransferWrapper)result;
			Logger.fine("returnCode: " + responseWrapper.getReturnCode());
	  	} catch(ConnectException e) {
	  		Logger.warning("ConnectException: " + e.toString());
			e.printStackTrace();
	  	} catch(MalformedURLException e) {
	  		Logger.warning("MalformedURLException: " + tunnelPath + " -> " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Logger.warning("IOException: " + e.toString());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Logger.warning("ClassNotFoundException: " + e.toString());
			e.printStackTrace();
		}
		return responseWrapper;
	}

	public static void main(String[] args) {
		if (args.length == 4){ // argument som fås via Eclipse, t.ex. "pabod 5by5 http://localhost:8080/SpaceRaze/ 8080"
			new NotifierFrame(args[0],args[1],args[2],true,args[3]);
		}else{ // antas vara 3 argunent via jnlp-filen
			List<String> port = new ArrayList<String>();
			List<String> temp = Arrays.asList(args);
			temp.stream().filter(arg -> arg.contains("http")).findFirst().ifPresent(arg -> 
			{
				String portnumber = arg.substring(arg.indexOf("//") + 2);
				portnumber = portnumber.substring(portnumber.indexOf(":") + 1, portnumber.indexOf("/"));
				port.add(portnumber);
				
			});
			if(args.length > 1) {
				new NotifierFrame(args[0],args[1],args[2],false,port.get(0));
			}else {
				new NotifierFrame("", "", args[0], false, port.get(0));
			}
			
			/*
			int colonIndex = args[2].substring(6).indexOf(":");
			if (colonIndex > -1){ // hämta ut portnummret
				String tmpPortStr = args[2].substring(6).substring(colonIndex+1);
				Logger.fine("tmpPortStr: " + tmpPortStr);
				tmpPortStr = tmpPortStr.substring(0, tmpPortStr.indexOf("/"));
				Logger.fine("tmpPortStr: " + tmpPortStr);
				port = tmpPortStr;
				Logger.fine("Port: " + port);
			}
			new NotifierFrame(args[0],args[1],args[2],false,port);
			*/
		}
	}
	
	public void removeNewGamePanel(){
		remove(newGamePanel);
		newGamePanel = null;
		if (mapInfoPanel != null){
			remove(mapInfoPanel);
			mapInfoPanel = null;
		}
		if (gameWorldInfoPanel != null){
			remove(gameWorldInfoPanel);
			gameWorldInfoPanel = null;
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == newGameBtn){
			if (newGamePanel == null){
				if (scrollPane != null){
					scrollPane.setVisible(false);
					remove(scrollPane);
				}
				if (gameListPanel != null){
					gameListPanel.setVisible(false);
					remove(gameListPanel);
				}
				newGamePanel = new NewGamePanel(this);
				newGamePanel.setLocation(10, 80);
				add(newGamePanel);
				paintAll(getGraphics());
			}
		}else
		if (arg0.getSource() == editMapBtn){
			if (newGamePanel != null){
				removeNewGamePanel();
			}
			if (scrollPane != null){
				scrollPane.setVisible(false);
				remove(scrollPane);
			}
			if (gameListPanel != null){
				gameListPanel.setVisible(false);
				remove(gameListPanel);
			}
			showMapPanel(mapName.getText(), "load_pub", getUser());
			
		}else
		if (arg0.getSource() == showGamesBtn){
			if (newGamePanel != null){
				removeNewGamePanel();
			}
			checkWithServer();
		}else
		if(arg0.getSource() == minimizeBtn){
			minimizeToTray();
		}else
		if (arg0.getSource() == trayIcon){
			this.setVisible(true);
	        SystemTray tray = SystemTray.getSystemTray();
	        tray.remove(trayIcon);
		}else
		if (arg0.getSource() instanceof MenuItem){
			MenuItem aMenuItem = (MenuItem)arg0.getSource();
			if (aMenuItem.getLabel().equals("Open")){
				this.setVisible(true);
		        SystemTray tray = SystemTray.getSystemTray();
		        tray.remove(trayIcon);
			}else
			if (aMenuItem.getLabel().equals("Exit")){
		        SystemTray tray = SystemTray.getSystemTray();
		        tray.remove(trayIcon);
		        System.exit(0);
			}else
			if (aMenuItem.getLabel().equals("Update Now")){
				checkWithServer();
			}
		}else{
			Logger.severe("Unknown action source: " + arg0.getSource());
		}
	}
	
	private void minimizeToTray(){
		// get the SystemTray instance
        SystemTray tray = SystemTray.getSystemTray();
        // load an image
        Image image = currentIconImage;
        // create a popup menu
        PopupMenu popup = new PopupMenu();
        // create menu items

        MenuItem menuFrame = new MenuItem("Open");
        menuFrame.addActionListener(this);
        popup.add(menuFrame);

        MenuItem menuUpdate = new MenuItem("Update Now");
        menuUpdate.addActionListener(this);
        popup.add(menuUpdate);

        MenuItem menuWeb = new MenuItem("Open www.spaceraze.com");
        menuWeb.addActionListener(this);
        popup.add(menuWeb);

        MenuItem menuExit = new MenuItem("Exit");
        menuExit.addActionListener(this);
        popup.add(menuExit);
        // construct a TrayIcon
        trayIcon = new TrayIcon(image, "SpaceRaze Notifier", popup);
        // set the TrayIcon properties
        trayIcon.addActionListener(this);
        // add the tray image
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println(e);
        }
        this.setVisible(false);
	}

	private void setVisibleComponents(boolean show){
//		userLbl.setVisible(show);
		statusLabel.setVisible(show);
		showGamesBtn.setVisible(show);
		if (minimizeBtn != null){
			minimizeBtn.setVisible(show);
		}
		gameListPanel.setVisible(show);
		if (scrollPane != null){
			scrollPane.setVisible(show);
		}
		newGameBtn.setVisible(show);
		mapName.setVisible(show);
		editMapBtn.setVisible(show);
	}
	
	public void showSpaceRazePanel(int gameId, String playerName, String playerPassword){
		if (spazeRazePanel != null){
			remove(spazeRazePanel);
		}
		setVisibleComponents(false);
		Map<String,String> applicationParams = new HashMap<String,String>();
		applicationParams.put("gameId", String.valueOf(gameId));
		applicationParams.put("returnto", "Not Used");
		applicationParams.put("returnto_delete", "Not Used");
		String autouser = null;
		Logger.info("returnGames: " + returnGames);
		if (returnGames == ReturnGames.OWN_AND_OPEN){ // startad via jnlp
			autouser = "true";
			applicationParams.put("username", user);
			applicationParams.put("userpassword", password);
		}else{ // startad som vanlig applikation
			if (playerName != null){ // autologin med playerName/playerPassword
				autouser = "true";
				applicationParams.put("username", playerName);
				applicationParams.put("userpassword", playerPassword);
			}else{
				// ej autologin, detta måste vara ett parti som har status "Starting"
				autouser = "false";
			}
		}
		Logger.info("autouser: " + autouser);
		applicationParams.put("autouser", autouser);
		applicationParams.put("codebase", tunnelPath + "webb2/");
		applicationParams.put("port", port);
		if (returnGames == ReturnGames.ALL){
			applicationParams.put("messagesleeptime","6000000"); // startad via utv-miljö, högt värde för att inte störa loggarna...
		}else{
			//applicationParams.put("messagesleeptime","10000"); // startad via jnlp, var 10:e sekund...
			applicationParams.put("messagesleeptime","6000000"); // startad via utv-miljö, högt värde för att inte störa loggarna...
		}
		spazeRazePanel = new SpaceRazePanel(applicationParams,this);
		spazeRazePanel.setBounds(0, 0, 1200, 710);
		spazeRazePanel.init();
		spazeRazePanel.start();
		add(spazeRazePanel);
		repaint();
		//update(getGraphics());
		//paintAll(getGraphics());
	}
	
	public void showMapPanel(String mapName, String action, String playerName){
		if (mapPanel != null){
			remove(mapPanel);
		}
		setVisibleComponents(false);
		Map<String,String> applicationParams = new HashMap<String,String>();
		applicationParams.put("mapname", mapName);
		Logger.info("returnGames: " + returnGames);
		if (returnGames == ReturnGames.OWN_AND_OPEN){ // startad via jnlp
			applicationParams.put("username", user);
			//applicationParams.put("userpassword", password);
		}else{ // startad som vanlig applikation
			if (playerName != null){ // autologin med playerName/playerPassword
				applicationParams.put("username", playerName);
				
				//applicationParams.put("userpassword", playerPassword);
			}
		}
		applicationParams.put("action", action);
		applicationParams.put("codebase", tunnelPath + "webb2/");
		applicationParams.put("port", port);
		
		mapPanel = new MapEditorPanel(applicationParams,this);
		mapPanel.setBounds(0, 0, 930, 560);
		mapPanel.init();
		repaint();
		add(mapPanel);
	}
	
	public void hideSpaceRazePanel(){
		spazeRazePanel.setVisible(false);
		spazeRazePanel = null;
		StyleGuide.colorCurrent = StyleGuide.colorNeutral;
		setVisibleComponents(true);
		checkWithServer();
	}
	
	public void hideMapPanel(){
		mapPanel.setVisible(false);
		mapPanel = null;
		StyleGuide.colorCurrent = StyleGuide.colorNeutral;
		setVisibleComponents(true);
		checkWithServer();
	}

	public String getPassword() {
		return password;
	}

	public String getUser() {
		return user;
	}

	public ReturnGames getReturnGames() {
		return returnGames;
	}

	public Image getGreen() {
		return green;
	}

	public Image getRed() {
		return red;
	}

	public Image getYellow() {
		return yellow;
	}
	
    public spaceraze.world.Map findMap(String aMapName){
    	spaceraze.world.Map found = null;
    	int i = 0;
    	while ((found == null) & (i < maps.size())){
    		spaceraze.world.Map aMap = maps.get(i);
    		if (aMap.getNameFull().equals(aMapName)){
    			found = aMap;
    		}else{
    			i++;
    		}
    	}
    	return found;
    }   
    
    public int getMaxPlayers(){
    	return mapInfoPanel.getMaxPlayers();
    }
    
    public int getNrStartPlanets(){
    	return mapInfoPanel.getNrStartPlanets();
    }

    public List<String> getFactionNames(){
    	return gameWorldInfoPanel.getFactionNames();
    }

    public MapInfoPanel showMapInfo(String aMapName, boolean showAdvanced){
    	Logger.fine("aMapName: " + aMapName);
    	spaceraze.world.Map theMap = findMap(aMapName);
    	if (aMapName == null){ // hide map panel
    		mapInfoPanel.setVisible(false);
    	}else
    	if (mapInfoPanel == null){    		
    		mapInfoPanel = new MapInfoPanel(theMap,showAdvanced);
    		mapInfoPanel.setLocation(650, 80);
    		add(mapInfoPanel);
    	}else{
    		mapInfoPanel.setVisible(true);
    		mapInfoPanel.showMap(theMap,showAdvanced);
    	}
    	repaint();
    	return mapInfoPanel;
    }

    public GameWorldInfoPanel showGameWorldInfo(String aGameWorldName, boolean showAdvanced){
    	Logger.fine("aGameWorldName: " + aGameWorldName);
    	GameWorld foundGameWorld = GameWorldHandler.findGameWorld(aGameWorldName);
    	if (aGameWorldName == null){ // hide gw panel
    		gameWorldInfoPanel.setVisible(false);
    	}else
    	if (gameWorldInfoPanel == null){    		
    		gameWorldInfoPanel = new GameWorldInfoPanel(foundGameWorld,showAdvanced);
    		gameWorldInfoPanel.setLocation(380, 80);
    		add(gameWorldInfoPanel);
    	}else{
    		gameWorldInfoPanel.setVisible(true);
    		gameWorldInfoPanel.showGameWorld(foundGameWorld,showAdvanced);
    	}
    	repaint();
    	return gameWorldInfoPanel;
    }
    
}
