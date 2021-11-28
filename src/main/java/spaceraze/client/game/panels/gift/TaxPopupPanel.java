/*
 * Created on 2005-feb-25
 */
package spaceraze.client.game.panels.gift;

import java.awt.event.ActionListener;

import spaceraze.client.components.BasicPopupPanel;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.SRTextField;
import spaceraze.util.general.Logger;
import spaceraze.world.Player;

/**
 * @author wmpabod
 *
 * Popuppanel used for creation and edits of gifts
 */
@SuppressWarnings("serial")
public class TaxPopupPanel extends BasicPopupPanel {
    private SRLabel transactionSumLabel, transactionRecipientLabel, transactionRecipientNameLabel;
    private SRTextField transactionSumTextField;

	public TaxPopupPanel(String title, ActionListener listener, Player p, int amount){
		this(title,listener,p);
		// set amount
		transactionSumTextField.setText(String.valueOf(amount));
	}
	
	public TaxPopupPanel(String title, ActionListener listener, Player p){
		super(title,listener);
	    transactionRecipientLabel = new SRLabel("Tax on vassal Govenor:");
	    transactionRecipientLabel.setBounds(10,40,100,20);
	    add(transactionRecipientLabel);

	    transactionRecipientNameLabel = new SRLabel(p.getGovernorName());
	    transactionRecipientNameLabel.setBounds(130,40,150,20);
	    add(transactionRecipientNameLabel);

	    transactionSumLabel = new SRLabel("Sum to take:");
	    transactionSumLabel.setBounds(10,70,100,20);
	    add(transactionSumLabel);

	    transactionSumTextField = new SRTextField();
	    transactionSumTextField.setBounds(130,70,150,20);
	    add(transactionSumTextField);
	}
	  
	public void setSumfieldFocus(){
		transactionSumTextField.selectAll();
	    transactionSumTextField.requestFocus();
	}
	
	public int getSum(){
		int sum = 0;
		try{
			sum = Integer.parseInt(transactionSumTextField.getText());
		}
		catch(NumberFormatException nfe){
			Logger.fine("Could not parse sum, return 0");
		}
		return sum;
	}
		
}
