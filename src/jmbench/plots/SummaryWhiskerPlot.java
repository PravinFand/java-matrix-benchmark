/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of JMatrixBenchmark.
 *
 * JMatrixBenchmark is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JMatrixBenchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JMatrixBenchmark.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmbench.plots;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class SummaryWhiskerPlot {

    DefaultBoxAndWhiskerCategoryDataset dataSet
            = new DefaultBoxAndWhiskerCategoryDataset();

    String title;

    public SummaryWhiskerPlot(String title) {
        this.title = title;
    }

    public void addLibrary( String name , List<Double> overall ,
                            List<Double> large , List<Double> small )
    {
        dataSet.add(overall,"All",name);
        dataSet.add(large,"Large",name);
        dataSet.add(small,"Small",name);
    }

    public JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                title, "Matrix Size", "Relative Performance", dataSet,
                true);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainGridlinesVisible(true);
        plot.setBackgroundPaint(new Color(230,230,230));
        plot.setDomainGridlinePaint(new Color(50,50,50,50));
        plot.setDomainGridlineStroke(new BasicStroke(78f));
//        BoxAndWhiskerRenderer r = (BoxAndWhiskerRenderer)plot.getRenderer();
//
//        r.setMedianVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());


        return chart;
    }

    public void displayWindow(int width, int height) {

        JFreeChart chart = createChart();

        ChartFrame window = new ChartFrame(chart.getTitle().getText(),chart);

        window.setMinimumSize(new Dimension(width,height));
        window.setPreferredSize(window.getMinimumSize());
        window.setVisible(true);
    }

    public void saveJpeg( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsJpeg(createChart(),fileName,width,height,0.95);
    }

    public void savePDF( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsPdf(createChart(),fileName,width,height);
    }

    public static void main( String args[] ) {
        Random rand = new Random(2344);

        SummaryWhiskerPlot plot = new SummaryWhiskerPlot("Test Summary");

        for( int i = 0; i < 3; i++ ) {
            List<Double> overall = new ArrayList<Double>();
            List<Double> large = new ArrayList<Double>();
            List<Double> small = new ArrayList<Double>();

            for( int j = 0; j < 50; j++ ) {
                overall.add( rand.nextDouble() );
                large.add( rand.nextDouble() );
                small.add( rand.nextDouble() );
            }

            plot.addLibrary("Lib "+i,overall,large,small);
        }

        plot.displayWindow(600,350);
    }

}
