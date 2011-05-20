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

import jmbench.PackageMatrixConversion;
import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
import jmbench.impl.wrapper.OjAlgoBenchmarkMatrix;
import jmbench.impl.wrapper.OjAlgoMatrixStoreBenchmarkMatrix;
import jmbench.interfaces.AlgorithmInterface;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;
import org.ojalgo.function.implementation.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.TransposedStore;

/**
 *
 *
 * @author Peter Abeles
 * @author Anders Peterson (apete)
 */
@SuppressWarnings({ "unchecked" })
public class OjAlgoAlgorithmFactory implements RuntimePerformanceFactory {

    public static class OpAdd implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();
            final PhysicalStore matB = inputs[1].getOriginal();

            final PhysicalStore<Double> result = FACTORY.makeZero(matA.getRowDim(), matA.getColDim());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillMatching(matA, PrimitiveFunction.ADD, matB);
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpChol implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            MatrixStore<Double> L = null;
            final Cholesky<Double> chol = CholeskyDecomposition.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!chol.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }
                L = chol.getL();
            }

            final long elapsedTime = System.nanoTime() - prev;

            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(L);

            return elapsedTime;
        }
    }

    public static class OpCholInvertSymmPosDef implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            MatrixStore<Double> inverse = null;
            final Cholesky<Double> chol = CholeskyDecomposition.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!chol.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }
                inverse = chol.getInverse();
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoBenchmarkMatrix(inverse.transpose());
            return elapsedTime;
        }
    }

    public static class OpDet implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final LU<Double> lu = LUDecomposition.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!lu.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }
                lu.getDeterminant();
            }

            return System.nanoTime() - prev;
        }
    }

    public static class OpEigSymm implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final Eigenvalue<Double> eig = EigenvalueDecomposition.make(matA);

            MatrixStore<Double> D = null;
            MatrixStore<Double> V = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!eig.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }
                D = eig.getD();
                V = eig.getV();
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(D);
            outputs[1] = new OjAlgoMatrixStoreBenchmarkMatrix(V);
            return elapsedTime;
        }
    }

    public static class OpInvert implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final LU<Double> lu = LUDecomposition.make(matA);

            MatrixStore result = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!lu.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }
                result = lu.getInverse();
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpLu implements AlgorithmInterface {

        // TODO change to what Anders said
        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final LU<Double> lu = LUDecomposition.make(matA);

            MatrixStore<Double> L = null;
            MatrixStore<Double> U = null;
            int pivot[] = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!lu.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }

                L = lu.getL();
                U = lu.getU();
                pivot = lu.getPivotOrder();
            }

            final long elapsedTime = System.nanoTime() - prev;

            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(L);
            outputs[1] = new OjAlgoMatrixStoreBenchmarkMatrix(U);
            outputs[2] = new EjmlBenchmarkMatrix(SpecializedOps.pivotMatrix(null, pivot, pivot.length, false));

            return elapsedTime;
        }
    }

    public static class OpMult implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();
            final PhysicalStore matB = inputs[1].getOriginal();

            final PhysicalStore result = PrimitiveDenseStore.FACTORY.makeZero(matA.getRowDim(), matB.getColDim());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillByMultiplying(matA, matB);
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpMultTransB implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();
            final MatrixStore matBT = new TransposedStore<Number>((MatrixStore<Number>) inputs[1].getOriginal());

            final PhysicalStore result = PrimitiveDenseStore.FACTORY.makeZero(matA.getRowDim(), matBT.getColDim());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillByMultiplying(matA, matBT);
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpQr implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final QR<Double> qr = QRDecomposition.make(matA);

            MatrixStore<Double> Q = null;
            MatrixStore<Double> R = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!qr.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }

                Q = qr.getQ();
                R = qr.getR();
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(Q);
            outputs[1] = new OjAlgoMatrixStoreBenchmarkMatrix(R);
            return elapsedTime;
        }
    }

    public static class OpScale implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final PhysicalStore<Double> result = FACTORY.makeZero(matA.getRowDim(), matA.getColDim());

            final Double tmpArg = ScaleGenerator.SCALE;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillMatching(matA, PrimitiveFunction.MULTIPLY, tmpArg);
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpSolveExact implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();
            final PhysicalStore matB = inputs[1].getOriginal();

            final LU<Double> lu = LUDecomposition.make(matA);

            MatrixStore result = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                lu.compute(matA);
                result = lu.solve(matB);
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpSolveOver implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();
            final PhysicalStore matB = inputs[1].getOriginal();

            final QR<Double> qr = QRDecomposition.make(matA);

            MatrixStore result = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                qr.compute(matA);
                result = qr.solve(matB);
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpSvd implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final SingularValue<Double> svd = SingularValueDecomposition.make(matA);

            MatrixStore<Double> U = null;
            MatrixStore<Double> S = null;
            MatrixStore<Double> V = null;

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!svd.compute(matA)) {
                    throw new RuntimeException("Decomposition failed");
                }
                U = svd.getQ1();
                S = svd.getD();
                V = svd.getQ2();
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoMatrixStoreBenchmarkMatrix(U);
            outputs[1] = new OjAlgoMatrixStoreBenchmarkMatrix(S);
            outputs[2] = new OjAlgoMatrixStoreBenchmarkMatrix(V);
            return elapsedTime;
        }
    }

    public static class OpTranspose implements AlgorithmInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PhysicalStore matA = inputs[0].getOriginal();

            final PhysicalStore<Double> result = PrimitiveDenseStore.FACTORY.makeZero(matA.getColDim(), matA.getRowDim());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillMatching(new TransposedStore<Double>(matA));
            }

            final long elapsedTime = System.nanoTime() - prev;
            outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    static final PhysicalStore.Factory<Double> FACTORY = PrimitiveDenseStore.FACTORY;

    public static PhysicalStore convertToOjAlgo(final DenseMatrix64F orig) {

        final double[][] raw = PackageMatrixConversion.convertToArray2D(orig);

        return PrimitiveDenseStore.FACTORY.copy(raw);
    }

    public static DenseMatrix64F ojAlgoToEjml(final MatrixStore orig) {
        if (orig == null) {
            return null;
        }

        final DenseMatrix64F ret = new DenseMatrix64F(orig.getRowDim(), orig.getColDim());

        for (int i = 0; i < ret.numRows; i++) {
            for (int j = 0; j < ret.numCols; j++) {
                ret.set(i, j, orig.get(i, j).doubleValue());
            }
        }

        return ret;
    }

    @Override
    public AlgorithmInterface add() {
        return new OpAdd();
    }

    @Override
    public AlgorithmInterface chol() {
        return new OpChol();
    }

    @Override
    public void configure() {
    }

    @Override
    public BenchmarkMatrix create(final int numRows, final int numCols) {
        return this.wrap(PrimitiveDenseStore.FACTORY.makeZero(numRows, numCols));
    }

    @Override
    public AlgorithmInterface det() {
        return new OpDet();
    }

    @Override
    public AlgorithmInterface eigSymm() {
        return new OpEigSymm();
    }

    @Override
    public AlgorithmInterface invert() {
        return new OpInvert();
    }

    @Override
    public AlgorithmInterface invertSymmPosDef() {
        return new OpCholInvertSymmPosDef();
    }

    @Override
    public AlgorithmInterface lu() {
        return new OpLu();
    }

    @Override
    public AlgorithmInterface mult() {
        return new OpMult();
    }

    @Override
    public AlgorithmInterface multTransB() {
        return new OpMultTransB();
    }

    @Override
    public AlgorithmInterface qr() {
        return new OpQr();
    }

    @Override
    public AlgorithmInterface scale() {
        return new OpScale();
    }

    @Override
    public AlgorithmInterface solveExact() {
        return new OpSolveExact();
    }

    @Override
    public AlgorithmInterface solveOver() {
        return new OpSolveOver();
    }

    @Override
    public AlgorithmInterface svd() {
        return new OpSvd();
    }

    @Override
    public AlgorithmInterface transpose() {
        return new OpTranspose();
    }

    @Override
    public BenchmarkMatrix wrap(final Object matrix) {
        return new OjAlgoBenchmarkMatrix((PhysicalStore) matrix);
    }
}