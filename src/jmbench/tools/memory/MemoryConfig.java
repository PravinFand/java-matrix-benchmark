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
import jmbench.impl.memory.*;
import jmbench.tools.EvaluationTarget;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class MemoryConfig {

    public static EvaluationTarget ejml = new EvaluationTarget( MatrixLibrary.EJML, EjmlMemoryFactory.class.getName());
    public static EvaluationTarget sejml = new EvaluationTarget( MatrixLibrary.SEJML, SejmlMemoryFactory.class.getName());
    public static EvaluationTarget jama = new EvaluationTarget( MatrixLibrary.JAMA, JamaMemoryFactory.class.getName());
    public static EvaluationTarget ojalgo = new EvaluationTarget( MatrixLibrary.OJALGO, OjAlgoMemoryFactory.class.getName());
    public static EvaluationTarget commons = new EvaluationTarget( MatrixLibrary.CM, CommonsMathMemoryFactory.class.getName());
    public static EvaluationTarget colt = new EvaluationTarget( MatrixLibrary.COLT, ColtMemoryFactory.class.getName());
    public static EvaluationTarget mtj = new EvaluationTarget( MatrixLibrary.MTJ, MtjMemoryFactory.class.getName());
    public static EvaluationTarget pcolt = new EvaluationTarget( MatrixLibrary.PCOLT, PColtMemoryFactory.class.getName());
    public static EvaluationTarget ujmp = new EvaluationTarget( MatrixLibrary.UJMP, UjmpMemoryFactory.class.getName());
    public static EvaluationTarget ujmp_j = new EvaluationTarget( MatrixLibrary.UJMP_JAVA , UjmpJavaMemoryFactory.class.getName());
    public static EvaluationTarget jblas = new EvaluationTarget( MatrixLibrary.JBLAS, JBlasMemoryFactory.class.getName());

    // random seed
    public long seed;

    // list of all the libraries being tested
    public List<EvaluationTarget> libraries;

    // maximum amount of time it allows for any test
    public long maxTestTimeMilli;

    // how many times should it repeat each test
    public int numTrials;

    // how much memory should it allocate to the test process in megabytes
    public long memoryMinMB;
    public long memoryMaxMB;

    // number of rows/cols of tested matrices
    public int matrixSize;

    // ops that it tests
    public boolean mult;
    public boolean multTransB;
    public boolean add;
    public boolean solveLinear;
    public boolean solveLS;
    public boolean invSymmPosDef;
    public boolean svd;
    public boolean eig;

    public SampleType memorySampleType;

    /**
     * Different ways that memory is sampled
     */
    public static enum SampleType
    {
        /**
         * Use the unix command 'ps' to get memory usage
         */
        PS,
        /**
         * Use the /proc/PID/status to get the maximum memory usage.  Less standardized
         * than PS, but more accurate
         */
        PROC
    }

    public static MemoryConfig createDefault() {
        MemoryConfig ret = new MemoryConfig();

        ret.libraries = new ArrayList<EvaluationTarget>();
        ret.libraries.add( ejml);
        ret.libraries.add( jama );
        ret.libraries.add( commons );
        ret.libraries.add( colt );
        ret.libraries.add( jblas );
        ret.libraries.add( mtj );
        ret.libraries.add( ojalgo );
//        ret.libraries.add( sejml);
        ret.libraries.add( pcolt );
        ret.libraries.add( ujmp );
        ret.libraries.add( ujmp_j );

        ret.seed = 234234;

        ret.maxTestTimeMilli = 30*60*1000;
        ret.numTrials = 3;
        ret.memoryMinMB = 50;
        ret.memoryMaxMB = 1024*2;
        ret.matrixSize = 2000;
        ret.memorySampleType = SampleType.PROC;

        ret.mult = true;
        ret.multTransB = true;
        ret.add = true;
        ret.solveLinear = true;
        ret.solveLS = true;
        ret.invSymmPosDef = true;
        ret.svd = true;
        ret.eig = true;


        return ret;
    }

    public List<EvaluationTarget> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<EvaluationTarget> libraries) {
        this.libraries = libraries;
    }

    public long getMaxTestTimeMilli() {
        return maxTestTimeMilli;
    }

    public void setMaxTestTimeMilli(long maxTestTimeMilli) {
        this.maxTestTimeMilli = maxTestTimeMilli;
    }

    public int getNumTrials() {
        return numTrials;
    }

    public void setNumTrials(int numTrials) {
        this.numTrials = numTrials;
    }

    public long getMemoryMinMB() {
        return memoryMinMB;
    }

    public void setMemoryMinMB(long memoryMinMB) {
        this.memoryMinMB = memoryMinMB;
    }

    public long getMemoryMaxMB() {
        return memoryMaxMB;
    }

    public void setMemoryMaxMB(long memoryMaxMB) {
        this.memoryMaxMB = memoryMaxMB;
    }

    public int getMatrixSize() {
        return matrixSize;
    }

    public void setMatrixSize(int matrixSize) {
        this.matrixSize = matrixSize;
    }

    public boolean isMult() {
        return mult;
    }

    public void setMult(boolean mult) {
        this.mult = mult;
    }

    public boolean isMultTransB() {
        return multTransB;
    }

    public void setMultTransB(boolean multTransB) {
        this.multTransB = multTransB;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

    public boolean isSolveLinear() {
        return solveLinear;
    }

    public void setSolveLinear(boolean solveLinear) {
        this.solveLinear = solveLinear;
    }

    public boolean isSolveLS() {
        return solveLS;
    }

    public void setSolveLS(boolean solveLS) {
        this.solveLS = solveLS;
    }

    public boolean isSvd() {
        return svd;
    }

    public void setSvd(boolean svd) {
        this.svd = svd;
    }

    public boolean isEig() {
        return eig;
    }

    public void setEig(boolean eig) {
        this.eig = eig;
    }

    public SampleType getMemorySampleType() {
        return memorySampleType;
    }

    public void setMemorySampleType(SampleType memorySampleType) {
        this.memorySampleType = memorySampleType;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
