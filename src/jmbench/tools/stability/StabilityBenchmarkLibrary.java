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

package jmbench.tools.stability;

import jmbench.interfaces.StabilityFactory;
import jmbench.tools.BenchmarkTools;
import jmbench.tools.EvaluationTarget;
import jmbench.tools.EvaluatorSlave;
import jmbench.tools.TestResults;
import jmbench.tools.stability.tests.*;
import pja.util.UtilXmlSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class StabilityBenchmarkLibrary {

    private String libraryName;
    private BenchmarkTools tools;

    private List<StabilityTestBase> operations;

    // used to write errors to
    private PrintStream logStream = System.err;
    private FatalError fatalError;

    private StabilityBenchmarkConfig config;

    private String directorySave;

    private int sizeMin;
    private int sizeMax;

    private int numSolve;
    private int numSvd;

    // how much memory was allocated to the slave
    private long slaveMemoryMegaBytes;

    boolean spawnChild = true;

    public StabilityBenchmarkLibrary(  String outputDir ,
                                       StabilityBenchmarkConfig config ,
                                       EvaluationTarget target,
                                       int sizeMin ,
                                       int sizeMax ,
                                       int numSolve ,
                                       int numSvd ) {

        this.directorySave = outputDir;

        StabilityFactory factory = target.loadAlgorithmFactory();

        tools = new BenchmarkTools(1,config.baseMemory,config.scaleMemory,target.getJarFiles());
        tools.setFrozenDefaultTime(config.maxProcessingTime);

        if( directorySave != null ) {
            setupOutputDirectory();
            setupLog();
        }

        this.config = config;
        this.libraryName = target.getLibName();
        
        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;

        this.numSolve = numSolve;
        this.numSvd = numSvd;

        createOperationsList(factory);
    }

    private void setupOutputDirectory() {
        File d = new File(directorySave);
        if( !d.exists() ) {
            if( !d.mkdirs() ) {
                throw new IllegalArgumentException("Failed to make output directory");
            }
        } else if( !d.isDirectory())
            throw new IllegalArgumentException("The output directory already exists and is not a directory");
    }

    /**
     * Sets out a file for recording errors.
     */
    private void setupLog() {
        try {
            logStream = new PrintStream(directorySave+"/log.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // For debugging purposes output the slave's classpath
        logStream.println("Current directory = "+new File(".").getAbsolutePath());
        logStream.println("Classpath:");
        logStream.println(tools.getClassPath());
    }

    private void createOperationsList(StabilityFactory library) {
        operations = new ArrayList<StabilityTestBase>();

        if( config.checkOverflow ) {
            if( config.checkLinear )
                operations.add( new SolverOverflow(config.randomSeed,
                        library,
                        library.createLinearSolver(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,true,true) );

            if( config.checkLS )
                operations.add( new SolverOverflow(config.randomSeed,
                        library,
                        library.createLSSolver(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,false,true) );

            if( config.checkSVD )
                operations.add( new SvdOverflow(config.randomSeed,
                        library,
                        library.createSvd(),
                        numSvd/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,true) );

            if( config.checkEVD )
                operations.add( new EigSymmOverflow(config.randomSeed,
                        library,
                        library.createSymmEigen(),
                        numSvd/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,true) );

            if( config.checkSymInv )
                operations.add( new InvSymmOverflow(config.randomSeed,
                        library,
                        library.createSymmInverse(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,true) );
        }

        if( config.checkUnderflow ) {
            if( config.checkLinear )
                operations.add( new SolverOverflow(config.randomSeed,
                        library,
                        library.createLinearSolver(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,true,false) );

            if( config.checkLS )
                operations.add( new SolverOverflow(config.randomSeed,
                        library,
                        library.createLSSolver(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,false,false) );

            if( config.checkSVD )
                operations.add( new SvdOverflow(config.randomSeed,
                        library,
                        library.createSvd(),
                        numSvd/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,false) );

            if( config.checkEVD )
                operations.add( new EigSymmOverflow(config.randomSeed,
                        library,
                        library.createSymmEigen(),
                        numSvd/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,false) );

            if( config.checkSymInv )
                operations.add( new InvSymmOverflow(config.randomSeed,
                        library,
                        library.createSymmInverse(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,false) );
        }

        if( config.checkNearlySingular ) {
            if( config.checkLinear )
                operations.add( new SolverSingular(config.randomSeed,
                        library,
                        library.createLinearSolver(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,true) );

            if( config.checkLS )
                operations.add( new SolverSingular(config.randomSeed,
                        library,
                        library.createLSSolver(),
                        numSolve/config.overFlowFactor,
                        config.breakingPoint,sizeMin,sizeMax,false) );
        }
        if( config.checkAccuracy ) {
            if( config.checkLinear )
                operations.add( new SolverAccuracy(config.randomSeed,
                        library,
                        library.createLinearSolver(),
                        numSolve,
                        config.breakingPoint,sizeMin,sizeMax,true) );

            if( config.checkLS )
                operations.add( new SolverAccuracy(config.randomSeed,
                        library,
                        library.createLSSolver(),
                        numSolve,
                        config.breakingPoint,sizeMin,sizeMax,false) );

            if( config.checkSVD )
                operations.add( new SvdAccuracy(config.randomSeed,
                        library,
                        library.createSvd(),
                        numSvd,
                        sizeMin,sizeMax) );

            if( config.checkEVD )
                operations.add( new EigSymmAccuracy(config.randomSeed,
                        library,
                        library.createSymmEigen(),
                        numSvd,
                        sizeMin,sizeMax) );

            if( config.checkSymInv )
                operations.add( new InvSymmAccuracy(config.randomSeed,
                        library,
                        library.createSymmInverse(),
                        numSolve,
                        sizeMin,sizeMax) );
        }
    }

    public void process() {
//        MatrixLibrary libInfo = library.getLibrary();

        for( StabilityTestBase op : operations ) {
            if( op.getOperation() == null ) {
                logStream.println("Operator not supported: "+op.getTestName());
                continue;
            }

            System.out.println(libraryName+" :  Processing op: "+op.getTestName());

            StabilityTrialResults results = evaluateOperation(op);

            // if a fatal error occurred create some results so that this is marked
            if( fatalError != null ) {
                results = new StabilityTrialResults();
                logStream.println("Had fatal error: "+op.getTestName()+" error = "+fatalError);
                results.fatalError = fatalError;
            }
            // add environmental information for debugging later on
            results.durationMilli = tools.getDurationMilli();
            results.libraryName = libraryName;
            results.benchmarkName = op.getTestName();
            results.memoryBytes = slaveMemoryMegaBytes;

            // save the results to a file
            saveResults(results,op.getFileName());

            // print the results to the screen
            if( fatalError == null )
                printResults(results);
        }

        if( directorySave != null ) {
            logStream.close();
        }
    }

    private void printResults( StabilityTrialResults results ) {
        System.out.println("    Median             = "+StabilityBenchmark.computePercent(results.breakingPoints,0.5));
        System.out.println("    Finished           = "+results.getNumFinished());
        System.out.println("    Bad Answer         = "+results.getNumUncountable());
        System.out.println("    Large Error        = "+results.getNumLargeError());
        System.out.println("    Detected           = "+results.getNumGraceful());
        System.out.println("    Runtime Exception  = "+results.getNumUnexpectedException());
    }

    private void saveResults( StabilityTrialResults results , String opFileName ) {
        if( directorySave == null )
            return;

        try {
            UtilXmlSerialization.serializeXml(results,directorySave+"/"+opFileName+".xml");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public StabilityTrialResults evaluateOperation( StabilityTestBase e ) {
        fatalError = null;
        e.setRandomSeed(config.randomSeed);

        for( int attempts = 0; attempts < 5; attempts++ ) {

            tools.setMemoryScale(attempts+1);
            EvaluatorSlave.Results results = spawnChild ? tools.runTest(e) : tools.runTestNoSpawn(e);
            slaveMemoryMegaBytes = tools.getAllocatedMemory();

            if( results == null ) {
                logStream.println("*** WTF runTest returned null = "+e.getTestName());
                fatalError = FatalError.RETURNED_NULL;
            } else if( results.failed == EvaluatorSlave.FailReason.OUT_OF_MEMORY ){
                System.out.println("  Not enough memory given to slave. Attempt "+attempts);
                logStream.println("Not enough memory for op.  Attempt num "+attempts+"  op = "+e.getTestName()+" memory "+tools.getAllocatedMemory());
                // have it run again, which will up the memory
                continue;
            } else {
                if( results.failed != null ) {
                    fatalError = FatalError.MISC;
                    if( results.failed == EvaluatorSlave.FailReason.TOO_SLOW ) {
                        logStream.println("    Slave: Case too slow = "+e.getTestName());
                    } else if( results.failed == EvaluatorSlave.FailReason.FROZEN ) {
                        logStream.println("    Slave: Frozen = "+e.getTestName());
                        fatalError = FatalError.FROZE;
                    } else {
                        logStream.println("    Slave: Case failed = "+results.failed+" op = "+e.getTestName()+" memory "+tools.getAllocatedMemory());
                        if( results.detailedError != null ) {
                            logStream.println(results.detailedError);
                        }
                    }
                }
            }

            // see if something very bad happened
            if( fatalError != null )
                return null;
            
            // collect all the results and return them
            StabilityTrialResults all = new StabilityTrialResults();

            for(TestResults t : results.getResults() ) {
                StabilityTrialResults r =  (StabilityTrialResults)t;
                all.addResults(r);
            }

            return all;
        }

        fatalError = FatalError.OUT_OF_MEMORY;

        return null;
    }
}
