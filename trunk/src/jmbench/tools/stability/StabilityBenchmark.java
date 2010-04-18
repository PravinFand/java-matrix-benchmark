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

package jmbench.tools.stability;

import jmbench.interfaces.StabilityFactory;
import jmbench.tools.EvaluationTarget;
import jmbench.tools.SystemInfo;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.NormOps;
import pja.util.UtilXmlSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class StabilityBenchmark {

    String directorySave;

    public StabilityBenchmark() {
        directorySave = "results/"+System.currentTimeMillis();
    }

    public StabilityBenchmark( String directory ) {
        this.directorySave = directory;
    }

    public void performBenchmark( StabilityBenchmarkConfig config ) {
        File dir = new File(directorySave);
        if( !dir.exists() ) {
            if( !dir.mkdir() ) {
                throw new IllegalArgumentException("Can't make directories to save results.");
            }
        }

        SystemInfo info = new SystemInfo();
        info.grabCurrentInfo();

        try {
            UtilXmlSerialization.serializeXml(info,directorySave+"/info.xml");
            UtilXmlSerialization.serializeXml(config,directorySave+"/config.xml");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        processLibraries(config.targets,config);
    }

    private void processLibraries( List<EvaluationTarget> libs, StabilityBenchmarkConfig config ) {

        saveLibraryDescriptions(libs);

        benchmarkLibraries(libs, config, "small",config.smallSizeMin,config.smallSizeMax,
                    config.trialsSmallSolve,config.trialsSmallSvd );

        benchmarkLibraries(libs, config, "medium",config.mediumSizeMin,config.mediumSizeMax,
                config.trialsMediumSolve,config.trialsMediumSvd );

        benchmarkLibraries(libs, config, "large",config.largeSizeMin,config.largeSizeMax,
                config.trialsLargeSolve,config.trialsLargeSvd );
    }

    /**
     * Save the description so that where this came from can be easily extracted
     */
    private void saveLibraryDescriptions( List<EvaluationTarget> libs )
    {
        for( EvaluationTarget desc : libs ) {
            try {
                String outputFile = directorySave+"/"+desc.getLib().getDirName()+".xml";
                UtilXmlSerialization.serializeXml(desc,outputFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void benchmarkLibraries(List<EvaluationTarget> libs,
                                    StabilityBenchmarkConfig config,
                                    String dirSize ,
                                    int sizeMin , int sizeMax ,
                                    int numTrialsSolve , int numTrialsSvd) {
        for( EvaluationTarget desc : libs ) {
//            if( desc.getLib() != MatrixLibrary.JAMA )
//                continue;

            String libOutputDir = directorySave+"/"+dirSize+"/"+desc.getLib().getDirName();

            // run the benchmark
            StabilityFactory l = desc.loadAlgorithmFactory();

            StabilityBenchmarkLibrary benchmark = new StabilityBenchmarkLibrary(
                    libOutputDir,config,l,
                    desc.getJarFiles(),sizeMin,sizeMax,
                    numTrialsSolve,numTrialsSvd);

            benchmark.process();
        }
    }

    public static DenseMatrix64F createMatrix( DenseMatrix64F U , DenseMatrix64F V , double []sv ) {
        DenseMatrix64F S = CommonOps.diagR(U.numRows,V.numRows,sv);

        DenseMatrix64F tmp = new DenseMatrix64F(U.numRows,V.numRows);
        CommonOps.mult(U,S,tmp);
        CommonOps.multTransB(tmp,V,S);

        return S;
    }

    public static double computePercent( List<Double> results , double percent ) {
        Collections.sort(results);

        return results.get((int)(results.size()*percent));
    }

    public double residualNorm(DenseMatrix64F A , DenseMatrix64F x  , DenseMatrix64F b ) {
        DenseMatrix64F r = new DenseMatrix64F(b.numRows,b.numCols);

        CommonOps.mult(A,x,r);
        CommonOps.sub(b,r,r);

        return NormOps.normP2(r);
    }

    public static double residualErrorMetric(DenseMatrix64F A , DenseMatrix64F x  , DenseMatrix64F b ) {
        DenseMatrix64F y = new DenseMatrix64F(b.numRows,b.numCols);

        CommonOps.mult(A,x,y);

        return residualError(y,b);
    }

    public static double residualError( DenseMatrix64F foundA , DenseMatrix64F expectedA )
    {
        DenseMatrix64F r = new DenseMatrix64F(foundA.numRows,foundA.numCols);

        CommonOps.sub(foundA,expectedA,r);

        double top = NormOps.normF(r);
        double bottom = NormOps.normF(expectedA);

        return top/bottom;
    }

    public double errorMetric( DenseMatrix64F A , DenseMatrix64F x  , DenseMatrix64F b )
    {
        DenseMatrix64F r = new DenseMatrix64F(b.numRows,b.numCols);

        CommonOps.mult(A,x,r);
        CommonOps.sub(b,r,r);

        double left = NormOps.conditionP2(A);
        double right = NormOps.normP2(r)/NormOps.normP2(b);

//        System.out.println("residual error = "+NormOps.normP2(r));

        return left*right;
    }

    public static void main( String args[] ) throws IOException, InterruptedException {
        StabilityBenchmark master = new StabilityBenchmark();

        if( args.length > 0 ) {
            System.out.println("Loading config from xml...");
            StabilityBenchmarkConfig config = UtilXmlSerialization.deserializeXml(args[0]);
            if( config == null )
                throw new IllegalArgumentException("No config file found!");

            master.performBenchmark(config);
        } else {
            master.performBenchmark(StabilityBenchmarkConfig.createDefault());
        }
    }
}