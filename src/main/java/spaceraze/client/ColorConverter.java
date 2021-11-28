package spaceraze.client;

import java.awt.Color;

import spaceraze.util.general.Logger;

/**
 * This utility class can take RGB int values to create a hex color string and create java.awt.Color
 * objects, for use in the Swing client, from color hex strings.
 * 
 * @author Paul Bodin
 */
public class ColorConverter {

	/**
	 * Convert a string containing a color hex values to a java.awt.Color 
	 * 
	 * @param colorHexString string containing a color hex value, for example FF0000 for red
	 * @return a java.awt.Color instance
	 */
	public static Color getColorFromHexString(String colorHexString){
		Color color = null;
		String redHex = colorHexString.substring(0, 2);
		String greenHex = colorHexString.substring(2, 4);
		String blueHex = colorHexString.substring(4);
		Logger.finest(redHex + " " + greenHex + " " + blueHex);
		int red = getIntFromHexPair(redHex);
		int green = getIntFromHexPair(greenHex);
		int blue = getIntFromHexPair(blueHex);
		Logger.finest(red + " " + green + " " + blue);
		color = new Color(red,green,blue);
		return color;
	}

	public static String getColorValues(String colorHexString){
		String redHex = colorHexString.substring(0, 2);
		String greenHex = colorHexString.substring(2, 4);
		String blueHex = colorHexString.substring(4);
		int red = getIntFromHexPair(redHex);
		int green = getIntFromHexPair(greenHex);
		int blue = getIntFromHexPair(blueHex);
		return red + " " + green + " " + blue;
	}

	private static int getIntFromHexPair(String hexPair){
		int major = getIntFromHexValue(hexPair.substring(0, 1));
		int minor = getIntFromHexValue(hexPair.substring(1));
		return (major*16)+minor;
	}
	
	private static int getIntFromHexValue(String hexValue){
		int retVal = -1;
		try{
			retVal = Integer.parseInt(hexValue);
		}catch(NumberFormatException nfe){ // value is not <=10
			if ("A".equals(hexValue)){
				retVal = 10;
			}else
			if ("B".equals(hexValue)){
				retVal = 11;
			}else
			if ("C".equals(hexValue)){
				retVal = 12;
			}else
			if ("D".equals(hexValue)){
				retVal = 13;
			}else
			if ("E".equals(hexValue)){
				retVal = 14;
			}else
			if ("F".equals(hexValue)){
				retVal = 15;
			}
		}
		return retVal;
	}

}
