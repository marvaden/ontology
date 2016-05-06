package update;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class UpdaterAgentGUI extends JFrame
{
  private UpdaterAgent agent;
  private JTextField resourceField, tripleField, queryField, propertyField;
  private JLabel resourceLabel, tripleLabel, queryLabel, propertyLabel;
  private JTextArea outputArea;
  private JScrollPane outputPane;
  
  UpdaterAgentGUI(UpdaterAgent agent)
  {
    this.agent = agent;
    
    JPanel panelOutput =  new JPanel();
    panelOutput.setLayout(new GridLayout(1,1));
    
    JPanel panelChoices = new JPanel();
    GroupLayout groupLayout = new GroupLayout(panelChoices);
    panelChoices.setLayout(groupLayout);

    groupLayout.setAutoCreateGaps(true);
    groupLayout.setAutoCreateContainerGaps(true);

    GroupLayout.SequentialGroup rowGroup = groupLayout.createSequentialGroup();

    resourceLabel = new JLabel("Add Resource:");
    resourceField = new JTextField(30);
    
    propertyLabel = new JLabel("Add Property:");
    propertyField = new JTextField(30);
    
    tripleLabel = new JLabel("Add Triple:");
    tripleField = new JTextField(30);
    
    queryLabel = new JLabel("Query Property:");
    queryField = new JTextField(30);
    
    rowGroup.addGroup(groupLayout.createParallelGroup()
                                 .addComponent(resourceLabel)
                                 .addComponent(propertyLabel)
                                 .addComponent(tripleLabel)
                                 .addComponent(queryLabel));
    
    rowGroup.addGroup(groupLayout.createParallelGroup()
                                 .addComponent(resourceField)
                                 .addComponent(propertyField)
                                 .addComponent(tripleField)
                                 .addComponent(queryField));
    
    groupLayout.setHorizontalGroup(rowGroup);


    GroupLayout.SequentialGroup columnGroup = groupLayout.createSequentialGroup();

    columnGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                    .addComponent(resourceLabel)
                                    .addComponent(resourceField));
    
    columnGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                    .addComponent(propertyLabel)
                                    .addComponent(propertyField));
    
    columnGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                    .addComponent(tripleLabel)
                                    .addComponent(tripleField));
    
    columnGroup.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                    .addComponent(queryLabel)
                                    .addComponent(queryField));
    
    groupLayout.setVerticalGroup(columnGroup);
    
    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new GridLayout(2,1));
    
    outputArea = new JTextArea("Hello!\n", 20, 80);
    outputArea.setCaretPosition(outputArea.getText().length() - 1);
    outputPane = new JScrollPane(outputArea);
    panelOutput.add(outputPane);
    
    JButton submit = new JButton("Submit");
    submit.addActionListener
     (new ActionListener() 
     {
       public void actionPerformed(ActionEvent ev) 
       {
         try 
         {
           String newResource = resourceField.getText().trim();
           if(newResource != null && !newResource.equals("")) 
           {
             agent.addResource(newResource);
             outputArea.append("Adding resource " + newResource + "\n");
             resourceField.setText("");
             return;
           }
           
           String newProperty = propertyField.getText().trim();
           if(newProperty != null && !newProperty.equals("")) 
           {
             agent.addProperty(newProperty);
             outputArea.append("Adding property " + newProperty + "\n");
             propertyField.setText("");
             return;
           }
           
           String newTriple = tripleField.getText().trim();
           String[] tripleParts = newTriple.split(",");
           if(newTriple != null && !newTriple.equals("")) 
           {
             agent.addTriple(tripleParts[0], tripleParts[1], tripleParts[2]);
             outputArea.append("Adding triple " + newTriple + "\n");
             tripleField.setText("");
             return;
           }
           
           String newQuery = queryField.getText().trim();
           if(newQuery != null && !newQuery.equals("")) 
           {
             outputArea.append("Querying for this " + newQuery + "\n");
             outputArea.append(agent.queryProperty(newQuery) + "\n");
             queryField.setText("");
             return;
           }
         }
         catch (Exception e) 
         {
           JOptionPane
             .showMessageDialog
               (UpdaterAgentGUI.this, "Show Error" );
         }
       }
     });
    panelButtons.add(submit);
    
    JButton showOntologyButton = new JButton("Show Ontology");
    showOntologyButton.addActionListener
      (new ActionListener() 
      {
        public void actionPerformed(ActionEvent ev) 
        {
          try 
          {
            String ontology = agent.getOntology();
            outputArea.append("Show Ontology\n");
            outputArea.append(ontology);
          }
          catch (Exception e) 
          {
            JOptionPane
              .showMessageDialog
                (UpdaterAgentGUI.this, e.getMessage() + "**");
          }
        }
      });
    panelButtons.add(showOntologyButton);
    
    getContentPane().add(panelChoices, BorderLayout.EAST);
    getContentPane().add(panelButtons, BorderLayout.WEST);
    getContentPane().add(panelOutput, BorderLayout.SOUTH);
    
    
    addWindowListener
      (new WindowAdapter() 
       {
         public void windowClosing(WindowEvent e) 
         {
           agent.doDelete();
         }
       });
    
    setResizable(false);
  }
  
  public void showGui() 
  {
    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int centerXCoord = (int)screenSize.getWidth() / 2;
    int centerYCoord = (int)screenSize.getHeight() / 2;
    setLocation(centerXCoord - getWidth() / 2, centerYCoord - getHeight() / 2);
    super.setVisible(true);
  } 
}
