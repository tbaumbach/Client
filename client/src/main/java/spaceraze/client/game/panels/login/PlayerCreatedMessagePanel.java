package spaceraze.client.game.panels.login;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.util.general.StyleGuide;
import spaceraze.world.Player;

/**
 * Message shown when a player successfully has joined a new game
 */
public class PlayerCreatedMessagePanel extends SRBasePanel {
	private static final long serialVersionUID = 1L;
	private SRLabel titleLabel;

	public PlayerCreatedMessagePanel(Player p) {
		setLayout(null);
		setBackground(StyleGuide.colorBackground);
		setForeground(StyleGuide.colorCurrent);
		setBorder(new LineBorder(StyleGuide.colorCurrent));

		titleLabel = new SRLabel("New Player Created", SwingConstants.CENTER);
		titleLabel.setBounds(100, 155, 200, 20);
		titleLabel.setFont(new Font("Helvetica", 1, 14));
		add(titleLabel);
	}

}