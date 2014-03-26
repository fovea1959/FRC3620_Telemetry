package org.first3620.networktablelogger;

import edu.wpi.first.wpilibj.networktables2.client.NetworkTableClient;
import edu.wpi.first.wpilibj.tables.IRemote;
import edu.wpi.first.wpilibj.tables.IRemoteConnectionListener;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.*;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class DougTableLoggerGUIFrame extends JFrame
	implements IRemoteConnectionListener, ITableListener, ActionListener {

  private final JLabel statusLabel, messageLabel, fileLabel, lastLabel, countLabel;

  private final NetworkTableClient networkTableClient;

  private PrintWriter printWriter = null;
  private File outFile = null;

  private Timer connectTimer = null;

  private final DateFormat timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");

  long count = 0;

  long tStart = 0;

  public DougTableLoggerGUIFrame(NetworkTableClient paramNetworkTableNode) {
    super("Network Table Logger");
    setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);

    statusLabel = new JLabel();
    add(statusLabel);
    messageLabel = new JLabel();
    add(messageLabel);
    fileLabel = new JLabel();
    add(fileLabel);
    countLabel = new JLabel();
    add(countLabel);
    lastLabel = new JLabel();
    add(lastLabel);

    pack();
    setSize(600, 120);

    networkTableClient = paramNetworkTableNode;

    paramNetworkTableNode.addConnectionListener(this, true);
    paramNetworkTableNode.addTableListener(this, true);
    connectTimer = new Timer(1000, this);
    connectTimer.start();
  }

  @Override
  public void connected(IRemote paramIRemote) {
    statusLabel.setText("Connected");
    openFile(System.currentTimeMillis());
  }

  synchronized void openFile(long now) {
    if (printWriter == null) {

      tStart = now;
      DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
      String fn = df.format(new Date(tStart));
      outFile = new File(fn + ".txt");
      try {
	printWriter = new PrintWriter(new FileWriter(outFile));
	fileLabel.setText("Writing to " + outFile.getAbsolutePath());
	System.out.println("Writing to " + outFile.getAbsolutePath());
      } catch (IOException ex) {
	throw new RuntimeException("unable to open file", ex);
      }

      StringBuilder sb = new StringBuilder();
      sb.append("0\t_ts\t0\t");
      synchronized (timestampDateFormat) {
	sb.append(timestampDateFormat.format(new Date(tStart)));
      }
      printWriter.println(sb);

      sb.setLength(0);
      sb.append("0\t_ts_long\t0\t");
      sb.append(Long.toString(tStart));
      printWriter.println(sb);
    }

  }

  @Override
  public void disconnected(IRemote paramIRemote) {
    statusLabel.setText("Disconnected " + paramIRemote);
    if (printWriter != null) {
      printWriter.close();
      fileLabel.setText("Closed " + outFile.getAbsolutePath());
      System.out.println("Closed " + outFile.getAbsolutePath());
      printWriter = null;
    }
  }

  @Override
  public void valueChanged(ITable iTable, String paramString, Object paramObject, boolean paramBoolean) {
    int sequence = networkTableClient.getEntryStore().getEntry(paramString).getSequenceNumber();

    long now = System.currentTimeMillis();
    openFile(now);

    double tRelative = (now - tStart) / 1000.0;

    StringBuilder sb = new StringBuilder();
    sb.append(tRelative);
    sb.append("\t");
    sb.append(paramString);
    sb.append("\t");
    sb.append(sequence);
    if (paramObject instanceof Object[]) {
      for (Object o : (Object[]) paramObject) {
	sb.append("\t");
	sb.append(o.toString());
      }
    } else {
      sb.append("\t");
      sb.append(paramObject.toString());
    }
    if (printWriter != null) {
      printWriter.println(sb.toString());
    }
    //System.out.println(sb.toString());

    count++;
    countLabel.setText(count + " " + paramString + ":" + sequence);
    lastLabel.setText(Double.toString(tRelative));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == connectTimer) {
      if (!networkTableClient.isConnected()) {
	System.out.println("reconnecting");
	messageLabel.setText("reconnecting");
	networkTableClient.reconnect();
	messageLabel.setText(" ");
      } else {
	messageLabel.setText(" ");
      }
    }
  }

  @Override
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      if (printWriter != null) {
	printWriter.close();
	System.out.println("Closed " + outFile.getAbsolutePath());
	printWriter = null;
      }
    }

    super.processWindowEvent(e);
  }
}
