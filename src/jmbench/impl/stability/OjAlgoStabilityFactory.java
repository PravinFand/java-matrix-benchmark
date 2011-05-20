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

package jmbench.impl.stability;

import jmbench.impl.runtime.OjAlgoAlgorithmFactory;
import jmbench.interfaces.StabilityFactory;
import jmbench.interfaces.StabilityOperationInterface;
import org.ejml.data.DenseMatrix64F;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * @author Peter Abeles
 * @author Anders Peterson (apete)
 */
@SuppressWarnings({ "unchecked" })
public class OjAlgoStabilityFactory implements StabilityFactory {

    public static class MyLinearSolver implements StabilityOperationInterface {

        @Override
        public DenseMatrix64F[] process(final DenseMatrix64F[] inputs) {

            final PhysicalStore<Double> matA = OjAlgoAlgorithmFactory.convertToOjAlgo(inputs[0]);
            final PhysicalStore<Double> matB = OjAlgoAlgorithmFactory.convertToOjAlgo(inputs[1]);

            final LU<Double> tmpLU = LUDecomposition.make(matA);

            tmpLU.compute(matA);
            final MatrixStore<Double> matX = tmpLU.solve(matB);

            return new DenseMatrix64F[] { OjAlgoAlgorithmFactory.ojAlgoToEjml(matX) };
        }
    }

    public static class MySvd implements StabilityOperationInterface {

        @Override
        public DenseMatrix64F[] process(final DenseMatrix64F[] inputs) {

            final PhysicalStore matA = OjAlgoAlgorithmFactory.convertToOjAlgo(inputs[0]);

            final SingularValue<Double> s = SingularValueDecomposition.make(matA);
            if (!s.compute(matA)) {
                return null;
            }

            final DenseMatrix64F ejmlU = OjAlgoAlgorithmFactory.ojAlgoToEjml(s.getQ1());
            final DenseMatrix64F ejmlS = OjAlgoAlgorithmFactory.ojAlgoToEjml(s.getD());
            final DenseMatrix64F ejmlV = OjAlgoAlgorithmFactory.ojAlgoToEjml(s.getQ2());

            return new DenseMatrix64F[] { ejmlU, ejmlS, ejmlV };
        }
    }

    public static class MySymmEig implements StabilityOperationInterface {

        @Override
        public DenseMatrix64F[] process(final DenseMatrix64F[] inputs) {

            final PhysicalStore matA = OjAlgoAlgorithmFactory.convertToOjAlgo(inputs[0]);

            final Eigenvalue<Double> eig = EigenvalueDecomposition.make(matA);
            if (!eig.compute(matA)) {
                return null;
            }

            final DenseMatrix64F ejmlD = OjAlgoAlgorithmFactory.ojAlgoToEjml(eig.getD());
            final DenseMatrix64F ejmlV = OjAlgoAlgorithmFactory.ojAlgoToEjml(eig.getV());

            return new DenseMatrix64F[] { ejmlD, ejmlV };
        }
    }

    public static class MySymmInverse implements StabilityOperationInterface {

        @Override
        public DenseMatrix64F[] process(final DenseMatrix64F[] inputs) {

            final PhysicalStore matA = OjAlgoAlgorithmFactory.convertToOjAlgo(inputs[0]);

            final Cholesky<Double> chol = CholeskyDecomposition.make(matA);

            if (!chol.compute(matA)) {
                throw new RuntimeException("Decomposition failed");
            }
            final MatrixStore<Double> inverse = chol.getInverse();
            final DenseMatrix64F ejmlInv = OjAlgoAlgorithmFactory.ojAlgoToEjml(inverse);

            return new DenseMatrix64F[] { ejmlInv };
        }
    }

    @Override
    public void configure() {

    }

    public StabilityOperationInterface createLinearSolver() {
        return new MyLinearSolver();
    }

    public StabilityOperationInterface createLSSolver() {
        return new MyLinearSolver();
    }

    @Override
    public StabilityOperationInterface createSvd() {
        return new MySvd();
    }

    @Override
    public StabilityOperationInterface createSymmEigen() {
        return new MySymmEig();
    }

    @Override
    public StabilityOperationInterface createSymmInverse() {
        return new MySymmInverse();
    }
}