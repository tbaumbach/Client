package spaceraze.client.game.panels.login;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRButton;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.SRPasswordField;
import spaceraze.client.components.SRTextField;
import spaceraze.client.game.SpaceRazePanel;
import spaceraze.util.general.StyleGuide;

@SuppressWarnings("serial")
public class LoginPanelSimple extends SRBasePanel implements ActionListener {
	SRTextField namefield;
	SRPasswordField passwordfield;
	SRButton okbtn;
	SRLabel titleLabel, nameLabel, passwordLabel;
	SpaceRazePanel client;

	public LoginPanelSimple(SpaceRazePanel client) {
		setLayout(null);
		setBackground(StyleGuide.colorBackground);
		this.client = client;

		titleLabel = new SRLabel("SpaceRaze Login");
		titleLabel.setBounds(20, 30, 130, 20);
		titleLabel.setFont(new Font("Helvetica", 1, 14));
		add(titleLabel);

		nameLabel = new SRLabel("Name:");
		nameLabel.setBounds(20, 50, 100, 20);
		add(nameLabel);

		namefield = new SRTextField();
		namefield.setBounds(20, 70, 100, 20);
		namefield.addActionListener(this);
		add(namefield);

		passwordLabel = new SRLabel("Password:");
		passwordLabel.setBounds(20, 95, 100, 20);
		add(passwordLabel);

		passwordfield = new SRPasswordField();
		passwordfield.setBounds(20, 115, 100, 20);
		passwordfield.setEchoChar('*');
		passwordfield.addActionListener(this);
		add(passwordfield);

		okbtn = new SRButton("Ok");
		okbtn.setBounds(20, 145, 100, 20);
		okbtn.addActionListener(this);
		add(okbtn);
	}

	public void setNamefieldFocus() {
		namefield.requestFocus();
	}

	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		String message;
		message = "oldlogin ";
		this.setVisible(false);
		client.setLogin(message + namefield.getText() + " " + passwordfield.getText());
		System.out.println("SimpleLoginPanel closing " + message + namefield.getText() + " " + passwordfield.getText());
	}

}
