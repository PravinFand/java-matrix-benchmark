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

package jmbench.impl.stability;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleEigenvalueDecomposition;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleSingularValueDecomposition;
import jmbench.impl.MatrixLibrary;
import static jmbench.impl.runtime.PColtAlgorithmFactory.convertToParallelColt;
import static jmbench.impl.runtime.PColtAlgorithmFactory.parallelColtToEjml;
import jmbench.interfaces.StabilityFactory;
import jmbench.interfaces.StabilityOperationInterface;
import org.ejml.data.DenseMatrix64F;


/**
 * @author Peter Abeles
 */
public class PColtStabilityFactory implements StabilityFactory {

    @Override
    public MatrixLibrary getLibrary() {
        return MatrixLibrary.PCOLT;
    }

    public static abstract class CommonOperation implements StabilityOperationInterface
    {
        @Override
        public String getName() {
            return MatrixLibrary.PCOLT.getVersionName();
        }
    }

    public StabilityOperationInterface createLinearSolver() {
        return new MyLinearSolver();
    }

    public StabilityOperationInterface createLSSolver() {
        return new MyLinearSolver();
    }

    public static class MyLinearSolver extends CommonOperation
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix2D matA = convertToParallelColt(inputs[0]);
            DoubleMatrix2D matB = convertToParallelColt(inputs[1]);

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            return new DenseMatrix64F[]{parallelColtToEjml(alg.solve(matA,matB))};
        }
    }

    @Override
    public StabilityOperationInterface createSvd() {
        return new MySvd();
    }

    public static class MySvd extends CommonOperation
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix2D matA = convertToParallelColt(inputs[0]);

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DenseDoubleSingularValueDecomposition s = alg.svd(matA);

            DenseMatrix64F ejmlU = parallelColtToEjml(s.getU());
            DenseMatrix64F ejmlS = parallelColtToEjml(s.getS());
            DenseMatrix64F ejmlV = parallelColtToEjml(s.getV());

            return new DenseMatrix64F[]{ejmlU,ejmlS,ejmlV};
        }
    }

    @Override
    public StabilityOperationInterface createSymmEigen() {
        return new MySymmEig();
    }

    public static class MySymmEig extends CommonOperation {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix2D matA = convertToParallelColt(inputs[0]);

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DenseDoubleEigenvalueDecomposition eig = alg.eig(matA);

            DenseMatrix64F ejmlD = parallelColtToEjml(eig.getD());
            DenseMatrix64F ejmlV = parallelColtToEjml(eig.getV());

            return new DenseMatrix64F[]{ejmlD,ejmlV};
        }
    }
}