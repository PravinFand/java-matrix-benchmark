/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package jmbench.tools.runtime.evaluation;

import jmbench.impl.MatrixLibrary;
import jmbench.plots.OperationsVersusSizePlot;
import jmbench.plots.SummaryWhiskerPlot;
import jmbench.tools.runtime.OperationResults;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;

import java.util.*;


/**
 * Generates different plots of the results.
 *
 * @author Peter Abeles
 */
public class RuntimeResultPlotter {

    public static void summaryPlots( List<RuntimePlotData> allResults , Reference referenceType , boolean weighted ,
                                     String outputDirectory ,
                                     boolean savePDF ,
                                     boolean showWindow ) {

        Map<String,List<OverallData>> overallResults = new HashMap<String,List<OverallData>>();

        // find the number of matrices sizes tested
        int numMatrices = 0;
        for( RuntimePlotData opResults : allResults ) {
            if( opResults.matrixSize.length > numMatrices )
                numMatrices = opResults.matrixSize.length;
        }

        // find the relative speed of each operation for each matrix size so that they can be weighted
        double slowestOperationByMatrix[] = new double[ numMatrices ];
        for( RuntimePlotData opResults : allResults ) {
            for( int i = 0; i < numMatrices; i++ ) {
                double bestSpeed = opResults.findBest(i);

                if( Double.isNaN(bestSpeed) )
                    continue;

                // convert from ops/sec to sec/op
                bestSpeed = 1.0/bestSpeed;

                if( bestSpeed > slowestOperationByMatrix[i] )
                    slowestOperationByMatrix[i] = bestSpeed;
            }
        }

        for( RuntimePlotData opResults : allResults ) {
            int numMatrixSizes = opResults.matrixSize.length;

            // find the performance for each matrix size that each library will
            // be compared against
            double refValue[] = new double[ numMatrixSizes ];
            computeReferenceValues(opResults, referenceType, -1, numMatrixSizes, refValue);


            for( RuntimePlotData.SourceResults r : opResults.libraries ) {
                List<OverallData> libOverall;

                if( !overallResults.containsKey(r.label)) {
                    libOverall = new ArrayList<OverallData>();
                    overallResults.put(r.label,libOverall);
                } else {
                    libOverall = overallResults.get(r.label);
                }

                for( int i = 0; i < r.results.length; i++ ) {
                    // the weight is determined by how slow this operation is relative to the slowest
                    double weight = (1.0/refValue[i])/slowestOperationByMatrix[i];

                    double a = r.getResult(i);
                    if( !Double.isNaN(a) ) {
                        // its relative ranking compared to other libraries in this operation
                        double score = a/refValue[i];
                        libOverall.add(new OverallData(weight,score,i));
                    } else {
                        libOverall.add(new OverallData(weight,0.0,i));
                    }
                }
            }
        }

        // If set to one results will not be weighted
        int maxSamples = weighted ? 100 : 1;

        String title = "Summary of Runtime Performance";
        String subtitle = weighted ? "Weighted by Operation Time" : null;

        SummaryWhiskerPlot plot = new SummaryWhiskerPlot(title,subtitle);
        for( String libName : overallResults.keySet() ) {
            List<OverallData> libOverall = overallResults.get(libName);


            plot.addLibrary(libName,
                    addSample(libOverall,0,numMatrices,maxSamples),
                    addSample(libOverall,numMatrices-3,numMatrices,maxSamples),
                    addSample(libOverall,0,3,maxSamples));
        }

        if( showWindow )
            plot.displayWindow(1000,450);

        if( savePDF )
            plot.savePDF(outputDirectory+"/summary.pdf",1000,450);
    }


    /**
     * For each result it will add the score a number of times depending upon its weight.
     *
     * @param results benchmark results across all the trials.
     * @param minIndex Only consider matrices that are this size or more.
     * @param maxIndex Only consider matrices that are less than this size.
     * @param maxSamples The maximum number of samples that can be added per result.
     * @return  List containing weighted results.
     */
    private static List<Double> addSample( List<OverallData> results , int minIndex , int maxIndex , int maxSamples ) {

        List<Double> ret = new ArrayList<Double>();

        for( OverallData d : results ) {
            if( d.matrixSize < minIndex || d.matrixSize >= maxIndex )
                continue;

            int num = (int)Math.ceil(d.weight*maxSamples);

            for( int i = 0; i < num; i++ ) {
                ret.add(d.score);
            }
        }

        return ret;
    }

    private static class OverallData
    {
        double weight;
        double score;
        int matrixSize;

        private OverallData(double weight, double score, int matrixSize) {
            this.weight = weight;
            this.score = score;
            this.matrixSize = matrixSize;
        }
    }

    public static void variabilityPlots( List<OperationResults> data ,
                                         String fileName ,
                                         boolean savePDF ,
                                         boolean showWindow )
    {
        String opName = data.get(0).getOpName();
        OperationsVersusSizePlot splot = new OperationsVersusSizePlot(opName,"Ops/Sec Range (%)");

        splot.setLogScale(false,true);
        splot.setRange(0.0,0.4);

        int numMatrixSizes = getNumMatrices(data);

        double results[] = new double[ numMatrixSizes ];
        int matDimen[] = new int[ numMatrixSizes ];

        if( fileName == null ) {
            fileName = opName;
        }

        for( int i = 0; i < numMatrixSizes; i++ ){
            matDimen[i] = getMatrixSize(data,i);
        }

        for( OperationResults ops : data ) {
            RuntimeEvaluationMetrics[]metrics = ops.metrics;
            int n = ops.getMatDimen().length;

            for( int i = 0; i < numMatrixSizes; i++ ) {
                if( i < n && metrics[i] != null && metrics[i].getRawResults().size() > 5 ) {
//                    double max = 1.0/metrics[i].getMin();
//                    double min = 1.0/metrics[i].getMax();
                    double max = metrics[i].getMax();
                    double min = metrics[i].getMin();
                    results[i] = (max-min)/max;
//                    results[i] = metrics[i].getStdev()/metrics[i].getMean();
                } else {
                    results[i] = Double.NaN;
                }
            }

            splot.addResults(matDimen,results,ops.getLibrary().getPlotName(),numMatrixSizes,
                    ops.getLibrary().getPlotLineType());
        }

        if( savePDF )
            splot.savePDF(fileName+".pdf",600,500);
        if( showWindow )
            splot.displayWindow(600, 500);
    }

    public static void absolutePlots( RuntimePlotData data ,
                                      String fileName ,
                                      String opName,
                                      boolean savePDF ,
                                      boolean showWindow )
    {
        OperationsVersusSizePlot splot = new OperationsVersusSizePlot(opName,"Time Per Op (s)");

        splot.setLogScale(true,true);

        int numMatrixSizes = data.matrixSize.length;

        double results[] = new double[ numMatrixSizes ];
        int matDimen[] = data.matrixSize;

        if( fileName == null ) {
            fileName = opName;
        }

        for( RuntimePlotData.SourceResults s : data.libraries) {
            for( int i = 0; i < numMatrixSizes; i++ ) {
                double libResult = s.getResult(i);
                
                if( !Double.isNaN(libResult) ) {
                    results[i] = 1.0/libResult;
                } else {
                    results[i] = Double.NaN;
                }
            }

            splot.addResults(matDimen,results,s.label,numMatrixSizes,
                    s.plotLineType);
        }

        if( savePDF )
            splot.savePDF(fileName+".pdf",600,500);
        if( showWindow )
            splot.displayWindow(600, 500);
    }

    public static void relativePlots( RuntimePlotData data ,
                                      Reference referenceType ,
                                      String refLib ,
                                      String fileName ,
                                      String opName ,
                                      boolean savePDF ,
                                      boolean showWindow )
    {
        int refIndex = refLib == null ? -1 : data.findLibrary(refLib);


        OperationsVersusSizePlot splot = new OperationsVersusSizePlot(opName,"Relative Performance");

        splot.setLogScale(true,true);
        splot.setRange(0.01,2);

        int numMatrixSizes = data.matrixSize.length;

        double results[] = new double[ numMatrixSizes ];
        double refValue[] = new double[ numMatrixSizes ];
        int matDimen[] = data.matrixSize;

        if( fileName == null ) {
            fileName = opName;
        }

        computeReferenceValues(data, referenceType, refIndex, numMatrixSizes, refValue);

        for( RuntimePlotData.SourceResults s : data.libraries) {
            for( int i = 0; i < numMatrixSizes; i++ ) {
                double libResult = s.getResult(i);

                if( !Double.isNaN(libResult) ) {
                    results[i] = libResult/refValue[i];
                } else {
                    results[i] = Double.NaN;
                }
            }

            splot.addResults(matDimen,results,s.label,numMatrixSizes, s.plotLineType);
        }
        
        if( savePDF )
            splot.savePDF(fileName+".pdf",500,350);
        if( showWindow )
            splot.displayWindow(500, 350);
    }

    private static void computeReferenceValues(RuntimePlotData data,
                                               Reference referenceType,
                                               int refIndex,
                                               int numMatrixSizes,
                                               double[] refValue) {
        if( referenceType == Reference.NONE ) {
            for( int i = 0; i <numMatrixSizes; i++ ) {
                refValue[i] = 1.0;
            }
        } else {
            for( int i = 0; i <numMatrixSizes; i++ ) {
                refValue[i] = getReferenceValue(data,refIndex,i,referenceType);
//                System.out.println("i = "+refValue[i]);
            }
//            System.out.println();
        }
    }

    private static int getNumMatrices( List<OperationResults> data ) {
        int max = 0;

        for( OperationResults d : data ) {
            int sizes[] = d.getMatDimen();

            if( sizes.length > max )
                max = sizes.length;
        }

        return max;
    }

    private static int getMatrixSize( List<OperationResults> data , int index)
    {
        for( OperationResults d : data ) {
            int sizes[] = d.getMatDimen();

            if( sizes.length > index ) {
                return sizes[index];
            }
        }

        throw new RuntimeException("Couldnt find a match");
    }

    private static double getReferenceValue( RuntimePlotData data ,
                                             int refIndex ,
                                             int matrixSize ,
                                             Reference referenceType )
    {
        if( referenceType == Reference.LIBRARY ) {
            return data.libraries.get(refIndex).results[matrixSize];
        }

        List<Double> results = new ArrayList<Double>();

        // get results from each library at this matrix size
        for( int i = 0; i < data.libraries.size(); i++ ) {
            double r = data.libraries.get(i).getResult(matrixSize);

            if( Double.isNaN(r) || Double.isInfinite(r)) {
                continue;
            }

            results.add(r);
        }

        if( results.size() == 0 )
            return Double.NaN;

        switch( referenceType ) {
            case MEAN:
                double total = 0;
                for( double d : results )
                    total += d;
                return total / results.size();

            case MEDIAN:
                Collections.sort(results);
                return results.get(results.size()/2);

            case MAX:
                Collections.sort(results);
                return results.get(results.size()-1);
        }

        throw new RuntimeException("Unknown reference type");
    }

    private static OperationResults findReferenceResults(List<OperationResults> data,
                                                         MatrixLibrary refLib) {
        OperationResults refResults = null;
        // find the results it is in reference to
        for( OperationResults o : data ) {
            if( o.getLibrary() == refLib ) {
                refResults = o;
                break;
            }
        }

        return refResults;
    }

    public static enum Reference
    {
        NONE,
        LIBRARY,
        MEAN,
        MEDIAN,
        MAX
    }
}
