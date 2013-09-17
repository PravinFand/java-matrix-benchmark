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

import jmbench.impl.MatrixLibrary;
import jmbench.tools.EvaluationTarget;
import jmbench.tools.SystemInfo;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class MemoryBenchmark {

    String directorySave;

    public MemoryBenchmark() {
        directorySave = "results/"+System.currentTimeMillis();
    }

    public MemoryBenchmark( String directory ) {
        this.directorySave = directory;
    }

    public void performBenchmark( MemoryConfig config ) {
        System.out.println("Setting up results directory");
        File dir = new File(directorySave);
        if( !dir.exists() ) {
            if( !dir.mkdir() ) {
                throw new IllegalArgumentException("Can't make directories to save results.");
            }
        }

        SystemInfo info = new SystemInfo();
        info.grabCurrentInfo();

        UtilXmlSerialization.serializeXml(info,directorySave+"/info.xml");
        UtilXmlSerialization.serializeXml(config,directorySave+"/config.xml");

        long startTime = System.currentTimeMillis();

        // save the description of each library
        saveLibraryDescriptions(directorySave,config.libraries);

        System.out.print("Computing overhead ");
        long overhead = new DetermineOverhead(config,10).computeOverhead();
        System.out.println(overhead/1024+" KB");

        processLibraries(config.libraries,config,overhead);

        long stopTime = System.currentTimeMillis();

        System.out.println("Finished Benchmark");
        System.out.println("  elapsed time "+(stopTime-startTime)+" (ms) "+((stopTime-startTime)/(60*60*1000.0))+" hrs");
    }

    private void processLibraries( List<EvaluationTarget> libs, MemoryConfig config , long overhead ) {

        for(  EvaluationTarget desc : libs ) {
            // run the benchmark
            MatrixLibrary lib = MatrixLibrary.lookup(desc.getLibName());

            String libOutputDir = directorySave+"/"+lib.getSaveDirName();

            MemoryBenchmarkLibrary bench = new MemoryBenchmarkLibrary(config,desc,libOutputDir,overhead);

            bench.process();

            System.out.println("Finished Library Benchmark");
            System.out.println();
        }
    }

    /**
     * Save the description so that where this came from can be easily extracted
     */
    public static void saveLibraryDescriptions( String directorySave , List<EvaluationTarget> libs )
    {
        for( EvaluationTarget desc : libs ) {
            MatrixLibrary lib = MatrixLibrary.lookup(desc.getLibName());

            // Add version information to the target description
            lib.addVersionInfo( desc );

            String outputFile = directorySave+"/"+lib.getSaveDirName()+".xml";
            UtilXmlSerialization.serializeXml(desc,outputFile);
        }
    }

    public static void main( String args[] ) throws IOException, InterruptedException {
        MemoryBenchmark master = new MemoryBenchmark();

        if( args.length > 0 ) {
            System.out.println("Loading config from xml...");
            MemoryConfig config = UtilXmlSerialization.deserializeXml(args[0]);
            if( config == null )
                throw new IllegalArgumentException("No config file found!");

            master.performBenchmark(config);
        } else {
            master.performBenchmark(MemoryConfig.createDefault());
        }
    }
}