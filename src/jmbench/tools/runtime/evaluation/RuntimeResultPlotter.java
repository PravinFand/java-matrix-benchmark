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

package jmbench.tools.runtime.evaluation;

import jmbench.impl.MatrixLibrary;
import jmbench.plots.OperationsVersusSizePlot;
import jmbench.tools.runtime.OperationResults;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Generates different plots of the results.
 *
 * @author Peter Abeles
 */
public class RuntimeResultPlotter {

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
