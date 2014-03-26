package org.first3620.networktablelogger;

import edu.wpi.first.wpilibj.networktables2.client.NetworkTableClient;
import edu.wpi.first.wpilibj.networktables2.stream.IOStreamFactory;
import edu.wpi.first.wpilibj.networktables2.stream.SocketStreams;
import edu.wpi.first.wpilibj.tables.IRemote;
import edu.wpi.first.wpilibj.tables.IRemoteConnectionListener;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import java.util.*;
import java.text.*;
import java.io.*;

public class DougTableLoggerNoGUI implements IRemoteConnectionListener, ITableListener {
  public static void main(String[] args) {
    Throwable boom = null;
    try {
      String host = "127.0.0.1";
      if (args.length > 0) {
        host = args[0];
      }
      int port = 1735;
      DougTableLoggerNoGUI dougTableLogger = new DougTableLoggerNoGUI(host, port);
      dougTableLogger.go();
    } catch (IOException ex) {
      boom = ex;
    } catch (RuntimeException ex) {
      boom = ex;
    }
    if (boom != null)
      boom.printStackTrace();
  }

  NetworkTableClient networkTableNode = null;

  DougTableLoggerNoGUI(String h, int p) throws IOException {
    Object localObject2 = SocketStreams.newStreamFactory(h, p);
    networkTableNode = new NetworkTableClient((IOStreamFactory) localObject2);
    networkTableNode.addConnectionListener(this, true);
    networkTableNode.addTableListener(this, true);
  }

  void go() {
    while (true) {
      if (!networkTableNode.isConnected()) {
        networkTableNode.reconnect();
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
      }
    }
  }

  private final DateFormat timestampDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  private PrintWriter out = null;

  @Override
  public void connected(IRemote paramIRemote) {
    System.out.println("connected " + paramIRemote);
    openFile();
  }
  
  void openFile() {
      if (out == null) {
    DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
    String fn = df.format(new Date());
    System.out.println(fn);
    File f = new File(fn);
    try {
      out = new PrintWriter(new FileWriter(f));
      System.out.println ("file " + f + " opened");
    } catch (IOException ex) {
      throw new RuntimeException("unable to open file", ex);
    }
          
      }
  }

  @Override
  public void disconnected(IRemote paramIRemote) {
    System.out.println("disconnected " + paramIRemote);
    if (out != null) {
      out.close();
      out = null;
    }
  }

  @Override
  public void valueChanged(ITable iTable, String paramString, Object paramObject,
      boolean paramBoolean) {
    int sequence = networkTableNode.getEntryStore().getEntry(paramString).getSequenceNumber();

    StringBuilder sb = new StringBuilder();
    synchronized (timestampDateFormat) {
      sb.append(timestampDateFormat.format(new Date()));
    }
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
    openFile();
    if (out != null) out.println(sb.toString());
    System.out.println(sb.toString());

  }
}
