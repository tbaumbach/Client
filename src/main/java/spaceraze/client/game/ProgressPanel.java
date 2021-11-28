package spaceraze.client.game;

import java.awt.Font;
import java.awt.Graphics;

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

public class ProgressPanel extends SRBasePanel {
	private static final long serialVersionUID = 1L;
	private SRLabel titleLabel, counterLabel;

	public ProgressPanel(Player p, String message) {
		setLayout(null);
		setBackground(StyleGuide.colorBackground);
		setForeground(StyleGuide.colorCurrent);
		setBorder(new LineBorder(StyleGuide.colorCurrent));

		titleLabel = new SRLabel(message, SwingConstants.CENTER);
		titleLabel.setBounds(100, 155, 400, 20);
		titleLabel.setFont(new Font("Helvetica", 1, 14));
		add(titleLabel);

		counterLabel = new SRLabel("Initiating transfer...", SwingConstants.CENTER);
		counterLabel.setBounds(100, 185, 400, 20);
		counterLabel.setFont(new Font("Helvetica", 1, 14));
		add(counterLabel);
	}

	public void setCounter(String newValue) {
		counterLabel.setText(newValue);
		Graphics g = getGraphics();
		if (g != null) {
			update(getGraphics());
		}
	}

}
