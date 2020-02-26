package spaceraze.client.game.panels.login;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Player;

/**
 * This panel is used when a player successfully has finished his turn.
 * 
 * @author Paul Bodin
 */

public class PlayerSavedPanel extends SRBasePanel {
	private static final long serialVersionUID = 1L;
	private SRLabel titleLabel;

	public PlayerSavedPanel(Player p) {
		setLayout(null);
		setBackground(StyleGuide.colorBackground);
		setForeground(StyleGuide.colorCurrent);
		setBorder(new LineBorder(StyleGuide.colorCurrent));

		titleLabel = new SRLabel("Player " + p.getName() + " (Govenor " + p.getGovenorName() + ") Saved",
				SwingConstants.CENTER);
		titleLabel.setBounds(100, 155, 400, 20);
		titleLabel.setFont(new Font("Helvetica", 1, 14));
		add(titleLabel);
	}

}
