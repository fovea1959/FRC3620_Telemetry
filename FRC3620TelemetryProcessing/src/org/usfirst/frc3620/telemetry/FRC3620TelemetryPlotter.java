/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc3620.telemetry;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.style.FillStyle;
import com.panayotis.gnuplot.style.FillStyle.Fill;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.utils.Debug;
import java.io.*;
import java.util.*;

/**
 *
 * @author wegscd
 */
public class FRC3620TelemetryPlotter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Exception boom = null;
        
        Map<String, PointDataSet> sets = new HashMap<>();
        sets.put("/telemetry/voltage", new PointDataSet());
        
        Map<Double, String> modeChanges = new HashMap<>();
        
        double maxY = 0;
        
        try {
            
            File f = new File("20140223-213506350.txt");
            BufferedReader r = new BufferedReader(new FileReader(f));
            
            String line;
            
            while ((line = r.readLine()) != null) {
                String[] fields = line.split("\t");
                double t = Double.parseDouble(fields[0]);
                String name = fields[1];
                String value = fields[3];
                
                if (sets.containsKey(name)) {
                    PointDataSet pointDataSet = sets.get(name);
                    double v = Double.parseDouble(value);
                    if (v > maxY) {
                        maxY = v;
                    }
                    pointDataSet.add(new Point(t, v));
                } else if (name.equals("/telemetry/mode")) {
                    modeChanges.put(t, value);
                }
            }
            r.close();
            
            JavaPlot p = new JavaPlot();
            JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
            
            for (String s : sets.keySet()) {
                PointDataSet pointDataSet = sets.get(s);
                p.addPlot(pointDataSet);
                
                AbstractPlot plot = (AbstractPlot) p.getPlots().get(p.getPlots().size() - 1);
                plot.setTitle(s);
                PlotStyle stl = plot.getPlotStyle();
                stl.setStyle(Style.LINES);
            }
            
            Map<String, PointDataSet> modeDataSets = new HashMap<>();
            
            for (Double t : modeChanges.keySet()) {
                String m = modeChanges.get(t);
                PointDataSet pointDataSet = modeDataSets.get(m);
                if (pointDataSet == null) {
                    pointDataSet = new PointDataSet();
                    modeDataSets.put(m, pointDataSet);
                }
                pointDataSet.add(new Point(t, maxY));
            }
            
            for (String s : modeDataSets.keySet()) {
                PointDataSet pointDataSet = modeDataSets.get(s);
                p.addPlot(pointDataSet);
                AbstractPlot plot = (AbstractPlot) p.getPlots().get(p.getPlots().size() - 1);
                plot.setTitle(s);
                PlotStyle stl = plot.getPlotStyle();
                stl.setStyle(Style.IMPULSES);
            }
            
            PointDataSet boxDataSet = new PointDataSet();
            boxDataSet.add(new Point(20, maxY, 20));
            boxDataSet.add(new Point(100, maxY, 20));
            p.addPlot(boxDataSet);
            AbstractPlot plot = (AbstractPlot) p.getPlots().get(p.getPlots().size() - 1);
            plot.setTitle("foo");
            PlotStyle stl = plot.getPlotStyle();
            stl.setStyle(Style.BOXES);
            /*
                    FillStyle ff = new FillStyle(Fill.SOLID);
            ff.setBorder(type);
            stl.setFill(ff);
                    */
            
            p.plot();
            
        } catch (RuntimeException | IOException ex) {
            boom = ex;
        }
        
        if (boom != null) {
            boom.printStackTrace();
        }
    }
    
}
