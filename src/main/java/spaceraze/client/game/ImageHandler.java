package spaceraze.client.game;

import java.awt.Image;
import java.awt.MediaTracker;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import spaceraze.util.general.Logger;

/**
 * Denna klass hanterar alla bilder som klienten använder. Alla bilder hämtas
 * direkt med hjälp av en MediaTracker och bilderna hämtas och identifieras 
 * med hjälp av textsträngar.
 * @author wmpabod
 */
public class ImageHandler {
  private List<Image> images;
  private List<String> imageNames;
  private MediaTracker tracker;
  private SpaceRazePanel spaceRazePanel;

  public ImageHandler(SpaceRazePanel spaceRazePanel) {
	Logger.info("Time:" + new Date().toString());
    this.spaceRazePanel = spaceRazePanel;
    images = new ArrayList<Image>();
    imageNames = new ArrayList<String>();
    tracker = new MediaTracker(this.spaceRazePanel);
    // add all images to be loaded
    addImage("planetnormal","planetnormal.gif");
    addImage("planetrazed","planetrazed.gif");
    addImage("newMail","mail00.jpg");
    // force the loading of all images
    try{
    	Logger.info("Waiting for images...");
    	tracker.waitForAll();
    }catch(InterruptedException ie){}
	Logger.info("Finished. time:" + new Date().toString());
  }

  private void addImage(String imageName, String imageFileName){
    imageNames.add(imageName);
    Logger.info("spaceRazePanel.getDocumentBase(): " + spaceRazePanel.getDocumentBase() + " images/" + imageFileName);
    Image tempImage = null;
    if (spaceRazePanel.isRunAsApplication()){
    	tempImage = spaceRazePanel.getImage(spaceRazePanel.getDocumentBase(),imageFileName);
    }else{
    	tempImage = spaceRazePanel.getImage(spaceRazePanel.getDocumentBase(),"images/" + imageFileName);
    }
    tracker.addImage(tempImage,1);
    images.add(tempImage);
	Logger.info("addImage Finished:" + imageFileName);
  }

  public Image getImage(String imageName){
    Image returnImage = null;
    int index = imageNames.indexOf(imageName);
    if (index > -1){
      returnImage = images.get(index);
    }
    return returnImage;
  }
}