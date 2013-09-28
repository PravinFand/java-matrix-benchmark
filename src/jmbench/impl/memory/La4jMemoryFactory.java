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

package jmbench.impl.memory;

import jmbench.impl.wrapper.La4jBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MemoryFactory;
import jmbench.interfaces.MemoryProcessorInterface;
import la4j.decomposition.EigenDecompositor;
import la4j.decomposition.SingularValueDecompositor;
import la4j.err.LinearSystemException;
import la4j.err.MatrixDecompositionException;
import la4j.err.MatrixException;
import la4j.err.MatrixInversionException;
import la4j.factory.DenseFactory;
import la4j.inversion.GaussianInvertor;
import la4j.linear.LinearSystem;
import la4j.matrix.Matrix;
import la4j.vector.Vector;

/**
 * @author Peter Abeles
 */
public class La4jMemoryFactory implements MemoryFactory {

    @Override
    public MemoryProcessorInterface svd() {
        return new SVD();
    }

    public static class SVD implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            Matrix U = null;
            Matrix S = null;
            Matrix V = null;

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    matA.decompose(new SingularValueDecompositor());
                } catch (MatrixDecompositionException e) {
                    throw new DetectedException(e);
                }
            }
        }
    }

    @Override
    public MemoryProcessorInterface eig() {
        return new Eig();
    }

    public static class Eig implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    matA.decompose(new EigenDecompositor());
                } catch (MatrixDecompositionException e) {
                    throw new DetectedException("bad decomposition");
                }
            }
        }
    }

    @Override
    public MemoryProcessorInterface invertSymmPosDef() {
        return new InvSymmPosDef();
    }

    public static class InvSymmPosDef implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            for( long i = 0; i < numTrials; i++ ) {
                try {
                    matA.inverse(new GaussianInvertor());
                } catch (MatrixInversionException e) {
                    throw new DetectedException(e);
                }
            }
        }
    }

    @Override
    public MemoryProcessorInterface add() {
        return new Add();
    }

    public static class Add implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    matA.add(matB);
                } catch (MatrixException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public MemoryProcessorInterface mult() {
        return new Mult();
    }

    public static class Mult implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    matA.multiply(matB);
                } catch (MatrixException e) {
                    throw new DetectedException(e);
                }
            }
        }
    }

    @Override
    public MemoryProcessorInterface multTransB() {
        return new MulTranB();
    }

    public static class MulTranB implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    matA.multiply(matB.transpose());
                } catch (MatrixException e) {
                    throw new DetectedException(e);
                }
            }
        }
    }

    @Override
    public MemoryProcessorInterface solveEq() {
        return new Solve();
    }

    @Override
    public MemoryProcessorInterface solveLS() {
        return null;//new Solve();
    }

    public static class Solve implements MemoryProcessorInterface {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix matA = inputs[0].getOriginal();
            Matrix matB = inputs[1].getOriginal();

            Vector vecB = La4jBenchmarkMatrix.toVector(matB);
            for( long i = 0; i < numTrials; i++ ) {
                LinearSystem system = new LinearSystem(matA, vecB);

                try {
                    system.solve();
                } catch (LinearSystemException e) {
                    throw new DetectedException(e);
                }
            }
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
}
