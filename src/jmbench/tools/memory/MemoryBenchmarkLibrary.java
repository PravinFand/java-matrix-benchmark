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

package jmbench.tools.memory;

import jmbench.interfaces.MemoryFactory;
import jmbench.interfaces.MemoryProcessorInterface;
import jmbench.tools.EvaluationTarget;
import jmbench.tools.runtime.InputOutputGenerator;
import jmbench.tools.runtime.generator.*;
import pja.util.UtilXmlSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class MemoryBenchmarkLibrary {

    Random rand;

    String libraryName;
    MemoryConfig config;

    MemoryBenchmarkTools tool = new MemoryBenchmarkTools();

    List<Task> activeTasks = new ArrayList<Task>();

    String directorySave;

    private PrintStream logStream = System.err;

    // how much overhead is involved in just launching a process
    // this is subtracted from the results in each library
    private long memoryOverhead;

    private MemoryFactory factory;

    public MemoryBenchmarkLibrary( MemoryConfig config ,
                                   EvaluationTarget desc,
                                   String directorySave ,
                                   long memoryOverhead )
    {
        this.rand = new Random(config.seed);
        this.config = config;
        this.directorySave = directorySave;
        this.libraryName = desc.getLibName();
        this.memoryOverhead = memoryOverhead;
        this.factory = desc.loadAlgorithmFactory();

        tool.setJars(desc.getJarFiles());
        tool.setVerbose(false);
        tool.sampleType = config.memorySampleType;

        if( config.add )
            addOperation(config, new AddGenerator(), factory.add(), "add", libraryName, 0 , config.matrixSize);

        if( config.mult )
            addOperation(config, new MultGenerator(), factory.mult(), "mult", libraryName, 0 , config.matrixSize);

        if( config.solveLinear )
            addOperation(config, new SolveEqGenerator(), factory.solveEq(), "solveLinear", libraryName, 0 , config.matrixSize);

        if( config.solveLS )
            addOperation(config, new SolveOverGenerator(), factory.solveLS(), "solveLS", libraryName,0 , config.matrixSize);

        if( config.svd )
            addOperation(config, new SvdGenerator(), factory.svd(), "svd", libraryName,0 , config.matrixSize/2 );

        if( config.eig )
            addOperation(config, new EigSymmGenerator(), factory.eig(), "eig", libraryName,0 , config.matrixSize);

        if( directorySave != null ) {
            setupOutputDirectory();
            setupLog();
        }
    }

    private void addOperation(MemoryConfig config, InputOutputGenerator gen ,
                              MemoryProcessorInterface op,
                              String opName , String libraryName ,
                              int scale , int matrixSize ) {
        if( op != null ) {
            activeTasks.add( new Task(op,gen,
                    opName,libraryName,config.maxTestTimeMilli,scale,matrixSize));
        }
    }

    public void process() {
        while( !activeTasks.isEmpty() ) {
            Task task = activeTasks.get( rand.nextInt(activeTasks.size()));

            System.out.println(libraryName+" operation "+task.results.nameOp);

            boolean remove = false;

            long mem = findMemory(task);

            // if it has a memory result, save it no matter what
            if( mem <= 0 ) {
                logStream.println("Bad memory "+mem+" operation "+task.results.nameOp);
            } else {
                mem -= memoryOverhead;
                if( mem < 0 ) {
                    logStream.println("Memory less than overhead!! "+mem+"  operation "+task.results.nameOp);
                }
                task.results.results.add(mem);

                if( tool.isFailed() )
                    task.results.numFailed++;

                System.out.println("  memory: "+(mem/1024/1024)+" (MB)");
                if( task.results.results.size() >= config.numTrials ) {
                    remove = true;
                }
            }

            if( tool.isFailed() ) {
                System.out.println("Failed!");
                logStream.println("FAILED: operation "+task.results.nameOp);
                remove = true;
            }

            saveResults(task.results);

            if( remove ) {
                activeTasks.remove(task);
            }
        }

        logStream.close();
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
        logStream.println(tool.getClassPath());
        logStream.println();
        logStream.println("Overhead = "+memoryOverhead+" bytes");

        tool.setErrorStream(logStream);
    }

    private void saveResults( MemoryResults results  ) {
        if( directorySave == null )
            return;

        try {
            UtilXmlSerialization.serializeXml(results,directorySave+"/"+results.nameOp+".xml");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public long findMemory( Task task )
    {
        tool.setFrozenDefaultTime(task.timeout);
        tool.setMemory(config.memoryMinMB,config.memoryMaxMB);

        MemoryTest test = new MemoryTest();
        test.setup(factory,task.gen,task.op,1,task.matrixSize);
        test.setRandomSeed(config.seed);

        return tool.runTest(test);
    }

    private static class Task {
        MemoryProcessorInterface op;
        MemoryResults results;
        InputOutputGenerator gen;

        long timeout;

        int memoryScale;

        int matrixSize;

        public Task(MemoryProcessorInterface op ,
                    InputOutputGenerator gen,
                    String nameOp , String nameLibrary ,
                    long time ,
                    int memoryScale ,
                    int matrixSize ) {
            this.op = op;
            this.gen = gen;
            this.timeout = time;
            results = new MemoryResults();
            results.nameOp = nameOp;
            results.nameLibrary = nameLibrary;
            this.memoryScale = memoryScale;
            this.matrixSize = matrixSize;
        }
    }

    public static void main( String []args ) {
        MemoryConfig config = MemoryConfig.createDefault();

        MemoryBenchmarkLibrary benchmark = new MemoryBenchmarkLibrary(config,MemoryConfig.ejml,null,0);

        benchmark.process();
    }
}
