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

package jmbench.impl.runtime;

import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
import jmbench.impl.wrapper.La4jBenchmarkMatrix;
import jmbench.interfaces.AlgorithmInterface;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import la4j.decomposition.*;
import la4j.err.LinearSystemException;
import la4j.err.MatrixDecompositionException;
import la4j.err.MatrixException;
import la4j.err.MatrixInversionException;
import la4j.factory.DenseFactory;
import la4j.inversion.GaussianInvertor;
import la4j.linear.LinearSystem;
import la4j.matrix.Matrix;
import la4j.vector.Vector;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * Wrapper around la4j
 */
public class La4jAlgorithmFactory implements RuntimePerformanceFactory {
    @Override
    public AlgorithmInterface chol() {
        return new Chol();
    }

    public static class Chol implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix U = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    Matrix[] c = matA.decompose(new CholeskyDecompositor());
                    U = c[0];
                } catch (MatrixDecompositionException e) {
                    throw new DetectedException(e);
                }
            }

            Matrix L = U.transpose();
            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(L);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface lu() {
        return new LU();
    }

    public static class LU implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix L = null;
            Matrix U = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    Matrix[] d = matA.decompose(new LUDecompositor());
                    L = d[0];
                    U = d[1];

                } catch (MatrixDecompositionException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(L);
            outputs[1] = new La4jBenchmarkMatrix(U);
            // no pivot matrix provided
            outputs[2] = new EjmlBenchmarkMatrix(CommonOps.identity(L.columns()));
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface svd() {
        return new SVD();
    }

    public static class SVD implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix U = null;
            Matrix S = null;
            Matrix V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    Matrix[] d = matA.decompose(new SingularValueDecompositor());
                    U = d[0];
                    S = d[1];
                    V = d[2];
                } catch (MatrixDecompositionException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(U);
            outputs[1] = new La4jBenchmarkMatrix(S);
            outputs[2] = new La4jBenchmarkMatrix(V);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface qr() {
        return new QR();
    }

    public static class QR implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix Q = null;
            Matrix R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    Matrix[] d = matA.decompose(new QRDecompositor());
                    Q = d[0];
                    R = d[1];
                } catch (MatrixDecompositionException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(Q);
            outputs[1] = new La4jBenchmarkMatrix(R);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface eigSymm() {
        return new Eig();
    }

    public static class Eig implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix D = null;
            Matrix V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    Matrix[] d = matA.decompose(new EigenDecompositor());
                    V = d[0];
                    D = d[1];
                } catch (MatrixDecompositionException e) {
                    throw new DetectedException("bad decomposition");
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(D);
            outputs[1] = new La4jBenchmarkMatrix(V);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface det() {
        return new Det();
    }

    public static class Det implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.determinant();
            }

            return System.nanoTime()-prev;
        }
    }
    @Override
    public AlgorithmInterface invert() {
        return new Inv();
    }

    public static class Inv implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    result = matA.inverse(new GaussianInvertor());
                } catch (MatrixInversionException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface invertSymmPosDef() {
        return new InvSymmPosDef();
    }

    public static class InvSymmPosDef implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    result = matA.inverse(new GaussianInvertor());
                } catch (MatrixInversionException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface add() {
        return new Add();
    }

    public static class Add implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            Matrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    result = matA.add(matB);
                } catch (MatrixException e) {
                    throw new RuntimeException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface mult() {
        return new Mult();
    }

    public static class Mult implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            long prev = System.nanoTime();

            Matrix result = null;

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    result = matA.multiply(matB);
                } catch (MatrixException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface multTransB() {
        return new MulTranB();
    }

    public static class MulTranB implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            Matrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    result = matA.multiply(matB.transpose());
                } catch (MatrixException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface scale() {
        return new Scale();
    }

    public static class Scale implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.multiply(ScaleGenerator.SCALE);
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface solveExact() {
        return new Solve();
    }

    @Override
    public AlgorithmInterface solveOver() {
        return null;//new Solve();
    }

    public static class Solve implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            Vector vecB = La4jBenchmarkMatrix.toVector(matB);
            
            Vector result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                LinearSystem system = new LinearSystem(matA, vecB);

                try {
                    result = system.solve();
                } catch (LinearSystemException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(La4jBenchmarkMatrix.toMatrix(result));
            return elapsed;
        }
    }

    @Override
    public AlgorithmInterface transpose() {
        return new Transpose();
    }

    public static class Transpose implements AlgorithmInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.transpose();
            }

            long elapsed = System.nanoTime()-prev;
            outputs[0] = new La4jBenchmarkMatrix(result);
            return elapsed;
        }
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return new La4jBenchmarkMatrix(new DenseFactory().createMatrix(numRows, numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new La4jBenchmarkMatrix((Matrix)matrix);
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new La4jBenchmarkMatrix(ejmlToLa4j(input));
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        Matrix orig = input.getOriginal();
        return la4jToEjml(orig);
    }
    
    public static Matrix ejmlToLa4j( DenseMatrix64F orig ) {
        Matrix m = new DenseFactory().createMatrix(orig.numRows,orig.numCols);
        
        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                m.set(i,j,orig.get(i,j));
            }
        }
        
        return m;
    }

    public static DenseMatrix64F la4jToEjml( Matrix orig ) {
        DenseMatrix64F m = new DenseMatrix64F(orig.rows(),orig.columns());

        for( int i = 0; i < m.numRows; i++ ) {
            for( int j = 0; j < m.numCols; j++ ) {
                m.set(i,j,orig.get(i,j));
            }
        }

        return m;
    }
}
