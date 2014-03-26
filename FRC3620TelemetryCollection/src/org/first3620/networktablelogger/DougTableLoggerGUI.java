package org.first3620.networktablelogger;

import edu.wpi.first.wpilibj.networktables2.client.NetworkTableClient;
import edu.wpi.first.wpilibj.networktables2.stream.IOStreamFactory;
import edu.wpi.first.wpilibj.networktables2.stream.SocketStreams;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DougTableLoggerGUI extends JDialog
	implements ActionListener {

  public static void main(String[] args) {
    Exception boom = null;
    try {
      if (args.length == 0) {
	DougTableLoggerGUI me = new DougTableLoggerGUI(null);
	me.setVisible(true);
      } else {
	System.out.println (args[0]);
	DougTableLoggerGUI me = new DougTableLoggerGUI(args[0]);
      }
    } catch (IOException | RuntimeException ex) {
      boom = ex;
    }
    if (boom != null) {
      boom.printStackTrace();
    }
    System.out.println("done");
  }

  private JTextField portField;
  private JTextField hostField;
  private JButton startButton;
  private JButton cancelButton;
  private boolean canceled = true;

  public DougTableLoggerGUI(String ip) throws IOException {
    if (ip == null) {
      setDefaultCloseOperation(2);
      JPanel localJPanel1 = new JPanel();
      localJPanel1.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
      setContentPane(localJPanel1);
      setLayout(new GridBagLayout());
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      localGridBagConstraints.gridwidth = 2;
      JLabel localJLabel;
      add(localJLabel = new JLabel("Network Table Logger"), localGridBagConstraints);
      localJLabel.setFont(new Font("Dialog", 1, 24));
      localGridBagConstraints.gridwidth = 1;
      localGridBagConstraints.gridy = 1;
      add(new JLabel("Host: "), localGridBagConstraints);
      localGridBagConstraints.gridx = 1;
      add(this.hostField = new JTextField("10.36.20.2"), localGridBagConstraints);
      this.hostField.addActionListener(this);
      this.hostField.setColumns(20);
      localGridBagConstraints.gridx = 0;
      localGridBagConstraints.gridy = 2;
      add(new JLabel("Port: "), localGridBagConstraints);
      localGridBagConstraints.gridx = 1;
      add(this.portField = new JTextField("1735"), localGridBagConstraints);
      this.portField.addActionListener(this);
      this.portField.setColumns(20);
      localGridBagConstraints.gridx = 0;
      localGridBagConstraints.gridy = 3;
      localGridBagConstraints.gridwidth = 2;
      JPanel localJPanel2 = new JPanel();
      localJPanel2.add(this.startButton = new JButton("Start"));
      this.startButton.addActionListener(this);
      localJPanel2.add(this.cancelButton = new JButton("Cancel"));
      this.cancelButton.addActionListener(this);
      add(localJPanel2, localGridBagConstraints);
      pack();
    } else {
      slurp(ip, 1735);
    }
  }

  public String getHost() {
    return this.hostField.getText();
  }

  public int getPort() {
    return Integer.parseInt(this.portField.getText());
  }

  public boolean isCanceled() {
    return this.canceled;
  }

  public void actionPerformed(ActionEvent paramActionEvent) {
    if (paramActionEvent.getSource() != this.cancelButton) {
      try {
	if (getHost().isEmpty()) {
	  // Object localObject2 = SocketStreams.newStreamProvider(getPort());
	  // localObject1 = new NetworkTableServer((IOStreamProvider)localObject2);
	  JOptionPane.showMessageDialog(this, "Must specify host", "Error creating client", JOptionPane.ERROR_MESSAGE);
	} else {
	  slurp(getHost(), getPort());
	}
      } catch (Exception localException) {
	JOptionPane.showMessageDialog(null, localException.getClass() + ": " + localException.getMessage(), "Error creating table node", 0);
      }
    }
    setVisible(false);
  }

  public void slurp(String ip, int port) throws IOException {

    IOStreamFactory ioStreamFactory = SocketStreams.newStreamFactory(ip, port);
    NetworkTableClient networkTableClient = new NetworkTableClient(ioStreamFactory);
    DougTableLoggerGUIFrame viewerFrame = new DougTableLoggerGUIFrame(networkTableClient);
    viewerFrame.setVisible(true);

  }

}
