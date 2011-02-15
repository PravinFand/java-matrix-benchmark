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

package jmbench.tools.runtime;

import jmbench.impl.MatrixLibrary;
import jmbench.impl.runtime.EjmlAlgorithmFactory;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.BenchmarkTools;
import jmbench.tools.EvaluationTest;
import jmbench.tools.EvaluatorSlave;
import jmbench.tools.TestResults;
import jmbench.tools.runtime.evaluation.RuntimeResultsCsvIO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * <p>
 * RuntimeBenchmarkLibrary performs a series of runtime performance benchmark tests against a
 * specific linear algebra library.  These test are composed of typical matrix operations
 * that are performed against matrices of various sizes.  For each test a new process is spawned.
 * Spawning a new process improves the stability of the results.
 * </p>
 *
 * <p>
 * Definitions:<br>
 * <DL>
 * <DT> Case
 *   <DD> A case is a set of algorithms (that all perform the same operation) and matrix sizes.
 * <DT> Block
 *   <DD> A block is a set of identical trials that are performed in a new instance of the JavaVM.
 * <DT> Trial
 *   <DD> The operations per second is computed in a trial by running an algorithm for numerious cycles
 * with the same inputs.
 * </DL>
 * </p>
 *
 * <p>
 * Performance is defined by how many operations it can perform in a second, ops/sec.  For each algorithm
 * this is computed several times.  The raw results and computed metrics are all saved to a file.  To
 * ensure a statistically significant number of operations is performed, the number of computations performed
 * is dynamically adjusted so that the total time is approximately a predetermined length.
 * </p>
 *
 * <p>
 * For each block a new javavm is spawned.  To allow these tests to run on computers with less resources
 * the amount of memory allocated to the VM is dynamically computed based on the size of the input matrices.
 * </p>
 *
 *
 * @author Peter Abeles
 */
public class RuntimeBenchmarkLibrary {

    // used to randomize the order of processes
    private Random rand;

    // random seed for RNG.  Each trial's seed is generated from this
    private long randSeedTrials;

    // where the results be saved to
    private String directorySave;

    // used to write errors to
    private PrintStream logStream;

    // true if an evaluation case failed
    private boolean caseFailed;
    // is it too slow to continue testing
    private boolean tooSlow;

    private RuntimePerformanceFactory library;

    private BenchmarkTools tools;

    private MatrixLibrary libraryType;

    private RuntimeBenchmarkConfig config;

    // the most memory that it allocated to java without any issues
    private long maxMemoryAllocated;

    // should it spawn a slave to run the benchmark or do it in the same java instance as this class
    private static final boolean SPAWN_SLAVE = true;

    public RuntimeBenchmarkLibrary( String outputDir , RuntimePerformanceFactory library  ,
                             List<String> jarNames , MatrixLibrary libraryType,
                             RuntimeBenchmarkConfig config )
    {
        this.config = config;


        this.directorySave = outputDir;

        File d = new File(directorySave);
        if( !d.exists() ) {
            if( !d.mkdir() ) {
                throw new IllegalArgumentException("Failed to make output directory");
            }
        } else if( !d.isDirectory())
            throw new IllegalArgumentException("The output directory already exists and is not a directory");
        
        this.library = library;

        // create the random seeds for each block
        this.rand = new Random(config.seed);
        this.randSeedTrials = rand.nextLong();

        tools = new BenchmarkTools(config.numBlockTrials,config.memorySlaveBase,config.memorySlaveScale,jarNames);
        tools.setVerbose(false);

        this.libraryType = libraryType;
    }

    /**
     * Perform the benchmark tests against all the different algortihms
     */
    public void performBenchmark() throws FileNotFoundException {
        setupLog();

        List<RuntimeEvaluationCase> cases = new FactoryRuntimeEvaluationCase(library,config).createCases();

        List<CaseState> states = createCaseList(cases);

        long startTime = System.currentTimeMillis();

        while(!states.isEmpty()) {
            // if random is true then select the next operation block that is to be benchmarked randomly
            int index = config.randizeOrder ? rand.nextInt( states.size() ) : 0;

            CaseState s = states.get(index);

            if( evaluateOneBlock(s)) {
                states.remove(index);
            }
        }

        System.out.println("Total processing time = "+(System.currentTimeMillis()-startTime)/1000.0);

        logStream.close();
    }

    /**
     * Check to see if there are any previously saved results.  if so skip the test. 
     */
    private List<CaseState> createCaseList(List<RuntimeEvaluationCase> cases) {
        List<CaseState> states = new ArrayList<CaseState>();

        for( RuntimeEvaluationCase c : cases ) {
            // see if the file already exists
            File f = new File(directorySave+"/"+c.getFileName()+".csv");

            if( f.exists() ) {
                // if it exists read it in and see if it finished
                RuntimeResults oldResults = RuntimeResultsCsvIO.read(f);
//                UtilXmlSerialization.deserializeXml(f.getAbsolutePath());

                if( !oldResults.isComplete() ) {
                    CaseState cs = new CaseState(c);
                    cs.score = oldResults.metrics;
                    for( cs.matrixIndex = 0; cs.matrixIndex < cs.score.length; cs.matrixIndex++ ) {
                        if( cs.score[cs.matrixIndex] == null ) {
                            break;
                        }
                    }
                    // see if the last matrix size is finished or not
                    if( cs.matrixIndex > 0 ) {
                        cs.matrixIndex--;
                        List<RuntimeMeasurement> rawResults = cs.score[cs.matrixIndex].getRawResults();

                        // see if it has enough trials to move on to the next matrix size
                        if( rawResults.size() >= config.maxTrials ) {
                            cs.matrixIndex++;
                        } else {
                            // see if any of the current results are too long and it should move on
                            for( RuntimeMeasurement r : rawResults ) {
                                double time = 1.0/r.getOpsPerSec();

                                if( time > config.getMaxTrialTime() ) {
                                    cs.matrixIndex++;
                                    break;
                                }
                            }
                        }

                        if( cs.matrixIndex >= cs.score.length ) {
                            throw new RuntimeException("Old result isn't flag as being done, but it really is?");
                        }
                        rawResults = cs.score[cs.matrixIndex].getRawResults();
                        if( rawResults == null )
                            cs.score[cs.matrixIndex].setRawResults(new ArrayList<RuntimeMeasurement>());
                        cs.results.addAll(rawResults);
                    }
                    states.add( cs );
                    int matrixSize = oldResults.getMatDimen()[cs.matrixIndex];
                    System.out.println("RESUMING OLD RESULTS: Found previously incomplete results for "+c.getOpName()+" at size "+matrixSize);
                    logStream.println("RESUMING OLD RESULTS: Found previously incomplete results for "+c.getOpName()+" at size "+matrixSize);
                } else {
                    System.out.println("SKIPPING: Found previously completed results for "+c.getOpName());
                    logStream.println("SKIPPING: Found previously completed results for "+c.getOpName()+" skipping");
                }
            } else {
                states.add( new CaseState(c));
            }
        }
        return states;
    }

    /**
     * Sets out a file for recording errors.
     */
    private void setupLog() {
        try {
            for( int i = 0; i < 1000; i++ ) {
                String fileName = String.format("%s/log%d.txt",directorySave,i);
                File f = new File(fileName);
                if( f.exists() )
                    continue;
                FileOutputStream out = new FileOutputStream(f,true);
                logStream = new PrintStream(out);
                break;
            }


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // For debugging purposes output the slave's classpath
        logStream.println("Current directory = "+new File(".").getAbsolutePath());
        logStream.println("Classpath:");
        logStream.println(tools.getClassPath());
    }


    /**
     * Process a single block in one case.
     *
     * @param state the current state of an evaluation
     *
     * @throws java.io.FileNotFoundException
     * @return true if the case has finished
     */
    private boolean evaluateOneBlock( CaseState state ) throws FileNotFoundException {

        RuntimeEvaluationCase e = state.evalCase;
        int matDimen[] = e.getDimens();

        RuntimeEvaluationMetrics score[] = state.score;

        System.out.println("#### "+libraryType.getPlotName()+"  op "+e.getOpName()+"  Size "+matDimen[state.matrixIndex]+" numTrials "+state.results.size()+"  ####");

        RuntimeResults r = computeResults(e, state.matrixIndex , randSeedTrials , score , state.results);

        if( r == null )
            throw new RuntimeException("Shouldn't return null any more.  This is a bug.");

        boolean done = tooSlow || caseFailed;

        // increment the number of blocks
        if( !done && state.results.size() >= config.maxTrials ) {
            state.results.clear();
            state.matrixIndex++;

            // see if its done processing all the matrices
            if( state.matrixIndex >= matDimen.length ) {
                done = true;
            }
        }

        // mark the this operation as being finished or not
        r.complete = done;

        // save the current state of the test
        RuntimeResultsCsvIO.write(r,directorySave+"/"+e.getFileName()+".csv");
//        UtilXmlSerialization.serializeXml(r,directorySave+"/"+e.getFileName()+".xml");

        return done;
    }

    /**
     * Computes the current results
     */
    private RuntimeResults computeResults( RuntimeEvaluationCase e , int matrixIndex ,
                                             long randSeed ,
                                             RuntimeEvaluationMetrics score[] , List<RuntimeMeasurement> rawResults )
            throws FileNotFoundException {

        List<RuntimeMeasurement> opsPerSecond = evaluateCase( e , randSeed , matrixIndex , rawResults.size() );

        if( caseFailed ) {
            System.out.println("      ---- ***** -----");
            System.out.println("Evaluation Case Failed ");
            System.out.println("      ---- ***** -----");
        } else {
            rawResults.addAll(opsPerSecond);
        }

        // see if there are any results to save
        if( !rawResults.isEmpty() ) {
            score[matrixIndex] = new RuntimeEvaluationMetrics(rawResults);
        }

        RuntimeResults results = new RuntimeResults(e.getOpName(),
                libraryType.getPlotName(),e.getDimens(),score);

        return results;
    }

    private List<RuntimeMeasurement> evaluateCase( RuntimeEvaluationCase e , long seed , int indexDimen, int numTrials ) {
        if( config.memoryTrial == 0 ) {
            return evaluateCaseDynamic(e,seed,indexDimen,numTrials);
        } else {
            return evaluateCaseFixedMemory(e,seed,indexDimen,numTrials);
        }
    }

    /**
     * Computes performance metrics for the specified case.
     *
     * @param indexDimen Which matrix size it should use.
     * @return The operations per second for this case.
     */
    @SuppressWarnings({"RedundantCast", "unchecked"})
    private List<RuntimeMeasurement> evaluateCaseDynamic( RuntimeEvaluationCase e , long seed , int indexDimen, int numTrials) {
        EvaluationTest test = e.createTest(numTrials,indexDimen,config.trialTime,config.maxTrialTime,config.sanityCheck);
        test.setRandomSeed(seed);

        int matrixSize = e.getDimens()[indexDimen];

        // try running the application a few times and see if its size increases
        for( int attempts = 0; attempts < 5; attempts++ ) {
            // estimate how much memory is needed for the operation
            long memory = test.getInputMemorySize()/1024/1024 + config.memorySlaveBase;

            // if this is less than the max it knows it can get away with set it to the max
            // to reduce the number of attempts needed in the future.
            if( memory < maxMemoryAllocated ) {
                memory = maxMemoryAllocated;
            }

            // increase the amount of memory allocated if it failed last time
            memory *= attempts+1;

            tools.setOverrideMemory(memory);

            EvaluatorSlave.Results r = callRunTest(e, test, matrixSize);

            if( caseFailed )  {
                if( r != null && r.failed == EvaluatorSlave.FailReason.OUT_OF_MEMORY ){
                    // have it run again, which will up the memory
                    System.out.println("  Not enough memory given to slave. Attempt "+attempts);
                    logStream.println("Not enough memory for op.  Attempt num "+attempts+"  op name = "+e.getOpName()+" matrix size = "+matrixSize+" memory = "+tools.getAllocatedMemory()+" mb");
                } else {
                    return null;
                }
            } else {
                if( memory > maxMemoryAllocated ) {
                    maxMemoryAllocated = memory;
                }
                return (List<RuntimeMeasurement>)((List)r.results);
            }

        }

        logStream.println("Case failed since not enough memory could be allocated.");
        // never had enough memory
        caseFailed = true;
        return null;
    }

    /**
     * Computes performance metrics for the specified case only allocating the specified amount of memory.
     *
     * @param indexDimen Which matrix size it should use.
     * @return The operations per second for this case.
     */
    @SuppressWarnings({"RedundantCast", "unchecked"})
    private List<RuntimeMeasurement> evaluateCaseFixedMemory( RuntimeEvaluationCase e ,
                                                          long seed , int indexDimen, int numTrials ) {
        EvaluationTest test = e.createTest(numTrials,indexDimen,config.trialTime,config.maxTrialTime,config.sanityCheck);
        test.setRandomSeed(seed);

        int matrixSize = e.getDimens()[indexDimen];

        tools.setOverrideMemory(config.memoryTrial);

        EvaluatorSlave.Results r = callRunTest(e, test, matrixSize);

        if( caseFailed ) {
            return null;
        }

        return (List<RuntimeMeasurement>)((List)r.results);
    }

    private EvaluatorSlave.Results callRunTest(RuntimeEvaluationCase e, EvaluationTest test, int matrixSize) {
        tooSlow = false;
        caseFailed = false;
        EvaluatorSlave.Results r;
        if( SPAWN_SLAVE )
            r = tools.runTest(test);
        else
            r = tools.runTestNoSpawn(test);

        if( r == null ) {
            logStream.println("*** RunTest returned null: op = "+e.getOpName()+" matrix size = "+matrixSize+" memory = "+tools.getAllocatedMemory()+" mb duration = "+tools.getDurationMilli());
            String param[] = tools.getParams();
            logStream.println("Command line arguments:");
            for( int i = 0; i < param.length; i++ ) {
                logStream.println("["+i+"]   "+param[i]);
            }
            logStream.println("------------------------------------------------");

            caseFailed = true;
        } else if( r.failed != null ) {
            if( r.failed == EvaluatorSlave.FailReason.TOO_SLOW ) {
                logStream.println("    Case was too slow: op = "+e.getOpName()+" matrix size = "+matrixSize+" memory = "+tools.getAllocatedMemory()+" mb");
                tooSlow = true;
            } else {
                logStream.println("    Case failed: reason = "+r.failed+" op = "+e.getOpName()+" matrix size = "+matrixSize+" memory = "+tools.getAllocatedMemory()+" mb duration = "+tools.getDurationMilli());
                if( r.detailedError != null ) {
                    logStream.println(r.detailedError);
                }
//                String param[] = tools.getParams();
//                logStream.println("Command line arguments:");
//                for( int i = 0; i < param.length; i++ ) {
//                    logStream.println("["+i+"]   "+param[i]);
//                }
//                logStream.println("------------------------------------------------");
                caseFailed = true;
            }
        }
        return r;
    }

    private static List<Double> convertToDoubleList( List<TestResults> l ){
        List<Double> ret = new ArrayList<Double>(l.size());

        for (TestResults aL : l) {

            double val = ((RuntimeMeasurement) aL).getOpsPerSec();

            ret.add(val);
        }

        return ret;
    }

    public static class CaseState
    {
        RuntimeEvaluationCase evalCase;

        List<RuntimeMeasurement> results = new ArrayList<RuntimeMeasurement>();

        RuntimeEvaluationMetrics score[];

        int matrixIndex = 0;

        public CaseState( RuntimeEvaluationCase e ) {
            this.evalCase = e;
            this.score = new RuntimeEvaluationMetrics[ e.getDimens().length ];
        }
    }

    public static void main( String args[] ) throws IOException, InterruptedException {

        File f = new File("results/temp");

        if( !f.exists() )
            if( !f.mkdir() ) throw new RuntimeException("Crap");

        RuntimeBenchmarkConfig config = RuntimeBenchmarkConfig.createAllConfig();

        RuntimeBenchmarkLibrary master = new RuntimeBenchmarkLibrary("results/temp",new EjmlAlgorithmFactory(),
                null,MatrixLibrary.EJML,config);
        master.performBenchmark();
    }
}