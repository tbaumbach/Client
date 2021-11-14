/*
 * Created on 2005-jun-16
 */
package spaceraze.client.mapeditor;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import spaceraze.client.NotifierFrame;
import spaceraze.client.components.SRBasePanel;
import spaceraze.servlethelper.map.TransferWrapper;
import spaceraze.util.general.Logger;
import spaceraze.util.properties.PropertiesHandler;
import spaceraze.world.Map;

@SuppressWarnings("serial")
public class MapEditorPanel extends SRBasePanel {
	
	private String mapFileName = null;
	private EditorGUIPanel editorGuiPanel;
	private Map theMap;
	private String userLogin;
	
	private java.util.Map<String,String> applicationParams;
	private NotifierFrame notifierFrame;
	
	public MapEditorPanel(java.util.Map<String,String> applicationParams, NotifierFrame notifierFrame){
		  this.applicationParams = applicationParams;
		  this.notifierFrame = notifierFrame;
	  }
	  
	  public boolean isRunAsApplication(){
		  return applicationParams != null;
	  }
	  
	  
	  
	  
	  public String getParameter(String paramName){
		  Logger.fine("Get parameter: " + paramName);
		  String paramValue = null;
		  if (applicationParams != null){
			  paramValue = applicationParams.get(paramName);
			  Logger.fine("applicationParams, value: " + paramValue);
		  }else{
			//  paramValue = super.getParameter(paramName);
			//  Logger.fine("super.getParameter, value: " + paramValue);
		  }
		  return paramValue;
	  }
	  
	  public URL getCodeBase(){
		  //Logger.fine("Get codebase");
		  URL codebase = null;
		  if (applicationParams != null){
			  try{
				  codebase = new URL(applicationParams.get("codebase"));
				  Logger.fine("applicationParams, value: " + codebase.toString());
			  }catch(MalformedURLException mue){
				  Logger.severe("felaktig URL: " + applicationParams.get("codebase"));
			  }
		  }else{
			//  codebase = super.getCodeBase();
			  //Logger.fine("super.getCodeBase, value: " + codebase.toString());
		  }
		  return codebase;
	  }
	  
	  public Image getImage(URL documentBase, String imageFileName){
		  Logger.fine("getImage overridden called: " + documentBase.toString() + ", " + imageFileName);
		  Image aImage = null;
		  if (isRunAsApplication()){
			  ClassLoader classLoader = this.getClass().getClassLoader();
			  URL url = classLoader.getResource(NotifierFrame.IMAGES_PATH + imageFileName);
			  Logger.fine("URL: " + url.toString());
			  aImage = Toolkit.getDefaultToolkit().getImage(url);
		  }else{
			//  aImage = super.getImage(documentBase, imageFileName);
		  }
		  return aImage;
	  }

	  public URL getDocumentBase(){
		  Logger.fine("Get documentbase from getCodeBase()");
		  return getCodeBase();
	  }

	public void init() {
	    Logger.info("");
	    Logger.info("MapEditorPanel started");
	    Logger.info("init()");
	    try {
	      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
	    }
	    catch (Exception e) {
	      e.getStackTrace();
	    }
		userLogin = getParameter("username");
	    String action = getParameter("action"); // "new", "load_pub" or "load_draft"
	    if (action.equals(TransferWrapper.LOAD_DRAFT) | action.equals(TransferWrapper.LOAD_PUB)){
	    	// get map name parameter
	    	mapFileName = getParameter("mapname");
	    	// get the map object from the server...
	    	loadMap(action,mapFileName);
	    	// Use test map
/*	    	Vector planets = new Vector();
	    	Vector conns = new Vector();
	    	createPlanets2(planets,conns);
	    	theMap = new Map(planets,conns,"Testmap wigge 12","A very short description",12);
*/
	    }else{ // new map
	    	// create a new empty map
	    	theMap = new Map();
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			theMap.setCreatedDate(sdf.format(new Date()));
	    	theMap.setAuthor(userLogin);
	    }

	    showGUI();
		
		Logger.fine("init: " + userLogin + " " + mapFileName + " " + action);
	}

	public void showGUI() {
	  	Logger.finer("showGUI anropad");

	    editorGuiPanel = new EditorGUIPanel(this,theMap);
	    editorGuiPanel.setBounds(0,0,getSize().width,getSize().height);
	    add(editorGuiPanel);
	}
	
	public Map getMap(){
		return theMap;
	}
	
	private String getTunnelURL(){
		URL codeBase = getCodeBase();
		
		String port = null;
	  	if (isRunAsApplication()){
	  		port = getParameter("port");
	  	}else{
	  		port = PropertiesHandler.getProperty("port");
	  	}
	  	
	 	Logger.info("Port read from properties file: \"" + port + "\"");
		if (port.equals("")){
		 	Logger.info("Port not found, using default port = 80: ");
			port = "80";
		}
	  	String tunnelUrlString = codeBase.getProtocol() + "://" +  codeBase.getHost() + ":" + port;
	  	// the code below which computes the path relies on that the client exist in a sub folder to the spaceraze root folder
	  	System.out.println("codeBase.getPath(): " +  codeBase.getPath());
	  	int webbIndex = codeBase.getPath().substring(0,codeBase.getPath().length()-1).lastIndexOf("/");
	  	System.out.println("webbIndex: " + webbIndex);
	  	String tmpPath = codeBase.getPath().substring(0,webbIndex);
	  	System.out.println("tmpPath: " +  tmpPath);
	  	tunnelUrlString = tunnelUrlString + tmpPath + "/map/sr.mapeditor.MapEditorTunnel";
		
		//String tunnelUrlString = codeBase.getProtocol() + "://" +  codeBase.getHost() + ":" + codeBase.getPort();
		//String tmpPath = codeBase.getPath().substring(0,codeBase.getPath().length()-8);
		//System.out.println("getTunnelURL: " + tunnelUrlString);
		//tunnelUrlString = tunnelUrlString + tmpPath + "/servlet/sr.mapeditor.MapEditorTunnel";
		return tunnelUrlString;
	}
	
	public void showOkMessageAndExit() {
	  	Logger.finer("mapPanel thread started");
	  	try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (notifierFrame != null){
			notifierFrame.hideMapPanel();
		}
	  }
	
	public void loadMap(String anAction, String aMapFileName){
		// create transfer object
		Logger.fine("creating tw: " + anAction + " " + userLogin + " " + aMapFileName);
		TransferWrapper tw = new TransferWrapper(anAction,userLogin,aMapFileName);
		// call server...
		tw = getResponse(tw);
		Logger.fine("recieving tw: " + tw.getMessage() + " " + tw.getMap());
		theMap = tw.getMap();
		Logger.fine("map loaded from server");
	}
	
	public void saveMap(String saveAction, String saveFileName){
		// create transfer object
		Logger.fine("aFileName: " + theMap.getFileName());
		TransferWrapper tw = new TransferWrapper(saveAction,userLogin,theMap,saveFileName);
		// call server...
		tw = getResponse(tw);
		if (tw.getMessage().equals(TransferWrapper.COMFIRM_NEEDED)){
//			int confirmDelete = JOptionPane.showConfirmDialog(this,"File exists, overwrite?","Confirmation needed",JOptionPane.YES_NO_OPTION);
			String[] opts = new String[2];
			opts[0] = "Yes";
			opts[1] = "No";
			int confirmDelete = JOptionPane.showOptionDialog(this,"File exists, overwrite?","Confirmation needed",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,opts,opts[0]);
			if (confirmDelete == JOptionPane.YES_OPTION){
				tw = new TransferWrapper(saveAction,userLogin,theMap,saveFileName);
				tw.setOwerwriteConfirm(true);
				tw = getResponse(tw);
				if (saveAction.equals(TransferWrapper.SAVE_PUB) & tw.getMessage().equals(TransferWrapper.MAP_SAVED)){
					theMap.incVersionId();
					editorGuiPanel.updateVersion();
				}
				JOptionPane.showMessageDialog(this,tw.getMessage(),"Message from server",JOptionPane.INFORMATION_MESSAGE);
			}
		}else{
			if (saveAction.equals(TransferWrapper.SAVE_PUB) & tw.getMessage().equals(TransferWrapper.MAP_SAVED)){
				theMap.incVersionId();
				editorGuiPanel.updateVersion();
			}
			JOptionPane.showMessageDialog(this,tw.getMessage(),"Message from server",JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void updateMapData(){
		editorGuiPanel.updateMapData();
	}
	
	private TransferWrapper getResponse(TransferWrapper sendTW){
	  	Logger.info("getResponse called");
	  	URL server = null;
	  	TransferWrapper responseTW = null;
	  	try {
//	  		String tpath = PropertiesHandler.getProperty("mapeditortunnelpath");
	  		String tpath = getTunnelURL();
	  	  	Logger.info("Tunnel path: " + tpath);
	  		server = new URL(tpath);
	  	} catch(MalformedURLException e) {}
	  	ObjectInputStream response = null;
	  	Object result = null;
	  	URLConnection con;
		try {
			con = server.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/octet-stream");

			ObjectOutputStream request = new ObjectOutputStream(new BufferedOutputStream(con.getOutputStream()));
			request.writeObject(sendTW);
		  	Logger.info("tw written");
			request.flush();
			request.close();
		  	Logger.info("request flush & close ");
			// get the result input stream
			response = new ObjectInputStream(new BufferedInputStream(con.getInputStream()));
		  	Logger.info("objectInputStream created: ");
			// read response back from the server
			result = response.readObject();
		  	Logger.info("result == null: " + (result == null));
		  	responseTW = (TransferWrapper)result;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return responseTW;
	}
	
	public String getMapFileName(){
		return mapFileName;
	}
	
	public void setMapFileName(String newFileName){
		mapFileName = newFileName;
		theMap.setFileName(newFileName);
		editorGuiPanel.setMapFileName(newFileName);
	}

}
