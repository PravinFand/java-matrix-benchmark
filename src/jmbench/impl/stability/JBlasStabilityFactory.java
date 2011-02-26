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

import jmbench.interfaces.StabilityFactory;
import jmbench.interfaces.StabilityOperationInterface;
import org.ejml.data.DenseMatrix64F;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jblas.Solve;

import static jmbench.impl.runtime.JBlasAlgorithmFactory.convertToJBlas;
import static jmbench.impl.runtime.JBlasAlgorithmFactory.jblasToEjml;


/**
 * @author Peter Abeles
 */
public class JBlasStabilityFactory implements StabilityFactory {

    @Override
    public void configure() {
        
    }

    public StabilityOperationInterface createLinearSolver() {
        return new MyLinearSolver();
    }

    public StabilityOperationInterface createLSSolver() {
        return null;
    }

    public static class MyLinearSolver implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix matA = convertToJBlas(inputs[0]);
            DoubleMatrix matB = convertToJBlas(inputs[1]);


            DoubleMatrix x = Solve.solve(matA,matB);

            return new DenseMatrix64F[]{jblasToEjml(x)};
        }
    }

    @Override
    public StabilityOperationInterface createSvd() {
        return null;
//        return new MySvd();
    }

//    public static class MySvd implements StabilityOperationInterface {
//        @Override
//        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
//            DoubleMatrix matA = convertToJBlas(inputs[0]);
//
//            DoubleMatrix[] evd = Singular.fullSVD(matA);
//
////            DenseMatrix64F ejmlD = jblasToEjml(evd[1]);
////            DenseMatrix64F ejmlV = jblasToEjml(evd[0]);
//
//            return new DenseMatrix64F[]{ejmlD,ejmlV};
//        }
//    }

    @Override
    public StabilityOperationInterface createSymmEigen() {
        return new MySymmEig();
    }

    public static class MySymmEig implements StabilityOperationInterface {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix matA = convertToJBlas(inputs[0]);

            DoubleMatrix[] evd = Eigen.symmetricEigenvectors(matA);

            DenseMatrix64F ejmlD = jblasToEjml(evd[1]);
            DenseMatrix64F ejmlV = jblasToEjml(evd[0]);

            return new DenseMatrix64F[]{ejmlD,ejmlV};
        }
    }

    @Override
    public StabilityOperationInterface createSymmInverse() {
        return new MySymmInverse();
    }

    public static class MySymmInverse implements StabilityOperationInterface {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix matA = convertToJBlas(inputs[0]);

            DoubleMatrix I = DoubleMatrix.eye(matA.getRows());
            DoubleMatrix result = Solve.solvePositive(matA,I);
            DenseMatrix64F ejmlInv = jblasToEjml(result);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }
}