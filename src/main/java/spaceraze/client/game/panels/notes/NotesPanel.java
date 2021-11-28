package spaceraze.client.game.panels.notes;

import spaceraze.client.components.SRBasePanel;
import spaceraze.client.components.SRLabel;
import spaceraze.client.components.SRScrollPane;
import spaceraze.client.components.SRTextArea;
import spaceraze.client.interfaces.SRUpdateablePanel;
import spaceraze.world.Player;

@SuppressWarnings("serial")
public class NotesPanel extends SRBasePanel implements SRUpdateablePanel{
    private String id;
	private SRLabel notesLabel;
    private SRTextArea notesArea;
    private SRScrollPane scrollPane;

    public NotesPanel(Player p ,String id){
      this.id = id;
      this.setLayout(null);

      notesLabel = new SRLabel("Notes");
      notesLabel.setBounds(10,10,100,20);
      add(notesLabel);

      notesArea = new SRTextArea();
      notesArea.setEditable(true);
//      notesArea.setBorder(new LineBorder(StyleGuide.colorCurrent));
      
//      notesArea.setCaretColor(StyleGuide.colorCurrent);
      notesArea.setText(p.getNotes());
//      notesArea.setBounds(10,35,500,350);
//      this.add(notesArea);

      scrollPane = new SRScrollPane(notesArea);
//      scrollPane.setBorder(new LineBorder(StyleGuide.colorCurrent));
//    scrollPane.setBorder(null);
      scrollPane.setBounds(10,35,500,350);
      add(scrollPane);
    }

    public String getNotes(){
      return notesArea.getText();
    }

    public String getId(){
      return id;
    }

    public void updateData(){
    }
}
