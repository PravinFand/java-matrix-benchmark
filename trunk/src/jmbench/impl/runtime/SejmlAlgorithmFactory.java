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

package jmbench.impl.runtime;

import jmbench.impl.MatrixLibrary;
import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
import jmbench.impl.wrapper.SejmlBenchmarkMatrix;
import jmbench.interfaces.AlgorithmInterface;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.ejml.data.SimpleMatrix;
import org.ejml.ops.EigenOps;


/**
 * @author Peter Abeles
 */
public class SejmlAlgorithmFactory implements RuntimePerformanceFactory {

    private static abstract class MyInterface implements AlgorithmInterface
    {
        @Override
        public String getName() {
            return MatrixLibrary.SEJML.getVersionName();
        }
    }

    @Override
    public void configure() {
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap( new SimpleMatrix(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new SejmlBenchmarkMatrix((SimpleMatrix)matrix);
    }

    @Override
    public AlgorithmInterface chol() {
        return null;
    }

    @Override
    public AlgorithmInterface lu() {
        return null;
    }

    @Override
    public AlgorithmInterface svd() {
        return new SVD();
    }

    public static class SVD extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();

            SimpleMatrix U=null,W=null,V=null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                SimpleMatrix.SVD s = matA.svd();
                U=s.getU();
                W=s.getW();
                V=s.getV();
            }

            long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new EjmlBenchmarkMatrix(U);
            outputs[1] = new EjmlBenchmarkMatrix(W);
            outputs[2] = new EjmlBenchmarkMatrix(V);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface eigSymm() {
        return new MyEig();
    }

    public static class MyEig extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();

            SimpleMatrix.EVD evd = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                evd = matA.eig();
                evd.getEigenvalue(0);
                evd.getEigenVector(0);
            }

            long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new EjmlBenchmarkMatrix(EigenOps.createMatrixD(evd.getEVD()));
            outputs[1] = new EjmlBenchmarkMatrix(EigenOps.createMatrixV(evd.getEVD()));
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface qr() {
        return null;
    }

    @Override
    public AlgorithmInterface det() {
        return new Det();
    }

    public static class Det extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.determinant();
            }

            return System.nanoTime() - prev;
        }
    }

    @Override
    public AlgorithmInterface invert() {
        return new Inv();
    }

    public static class Inv extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.invert();
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface invertSymmPosDef() {
        return null;
    }

    @Override
    public AlgorithmInterface add() {
        return new Add();
    }

    public static class Add extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.plus(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface mult() {
        return new Mult();
    }

    public static class Mult extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                 result = matA.mult(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface multTransB() {
        return new MulTranB();
    }

    public static class MulTranB extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.mult(matB.transpose());
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface scale() {
        return new Scale();
    }

    public static class Scale extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.elementMult(ScaleGenerator.SCALE);
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface solveExact() {
        return new Solve();
    }

    @Override
    public AlgorithmInterface solveOver() {
        return new Solve();
    }

    public static class Solve extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();

            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.solve(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    @Override
    public AlgorithmInterface transpose() {
        return new Transpose();
    }

    public static class Transpose extends MyInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.transpose();
            }

            long elapsedTime = System.nanoTime()-prev;
            outputs[0] = new EjmlBenchmarkMatrix(result);
            return elapsedTime;
        }
    }
}