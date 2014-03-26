/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc3620.telemetry;

import java.io.*;
import java.util.*;

/**
 *
 * @author wegscd
 */
public class FRC3620TelemetrySpreadsheet {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    Exception boom = null;

    Map<Double, Map<String, String>> allData = new TreeMap<>();
    Set<String> allNames = new TreeSet<>();

    try {

      File f = new File("20140223-213506350.txt");
      BufferedReader r = new BufferedReader(new FileReader(f));

      String line;
      
      Map<String,String> currentData = new TreeMap<>();

      while ((line = r.readLine()) != null) {
        String[] fields = line.split("\t");
        double t = Double.parseDouble(fields[0]);
        String name = fields[1];
        String value = fields[3];
        
        currentData.put(name, value);
        
        Map<String,String> p = new TreeMap<>(currentData);
        allData.put(t, p);
        allNames.add(name);
      }
      r.close();

      PrintWriter w = new PrintWriter(new File("20140223-213506350.csv"));
      w.print("time");
      for (String n : allNames) {
        w.print("\t");
        w.print(n);
      }
      w.println();

      for (Double t : allData.keySet()) {
        w.print(t);
        Map<String, String> point = allData.get(t);
        for (String n : allNames) {
          String v = point.get(n);
          w.print("\t");
          w.print(v);
        }
        w.println();
      }
      w.close();
    } catch (RuntimeException | IOException ex) {
      boom = ex;
    }

    if (boom != null) {
      boom.printStackTrace();
    }
  }

}
