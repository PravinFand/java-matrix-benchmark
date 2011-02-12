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

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import jmbench.interfaces.StabilityFactory;
import jmbench.interfaces.StabilityOperationInterface;
import org.ejml.data.DenseMatrix64F;

import static jmbench.impl.runtime.ColtAlgorithmFactory.coltToEjml;
import static jmbench.impl.runtime.ColtAlgorithmFactory.convertToColt;


/**
 * @author Peter Abeles
 */
public class ColtStabilityFactory implements StabilityFactory {

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

    public static class MyLinearSolver implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseDoubleMatrix2D matA = convertToColt(inputs[0]);
            DenseDoubleMatrix2D matB = convertToColt(inputs[1]);

            Algebra alg = new Algebra();

            return new DenseMatrix64F[]{coltToEjml(alg.solve(matA,matB))};
        }
    }

    public static class MySvd implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DoubleMatrix2D matA = convertToColt(inputs[0]);

            if( matA.rows() >= matA.columns() ) {
                SingularValueDecomposition s = new SingularValueDecomposition(matA);

                DenseMatrix64F ejmlU = coltToEjml(s.getU());
                DenseMatrix64F ejmlS = coltToEjml(s.getS());
                DenseMatrix64F ejmlV = coltToEjml(s.getV());

                return new DenseMatrix64F[]{ejmlU,ejmlS,ejmlV};
            } else {
                // Colt's SVD can't process a wide matrix so it has to be transposed first.
                Algebra alg = new Algebra();
                matA = alg.transpose(matA);
                SingularValueDecomposition s = new SingularValueDecomposition(matA);

                DenseMatrix64F ejmlU = coltToEjml(s.getV());
                DenseMatrix64F ejmlS = coltToEjml(s.getS());
                DenseMatrix64F ejmlV = coltToEjml(s.getU());

                return new DenseMatrix64F[]{ejmlU,ejmlS,ejmlV};
            }
        }
    }

    @Override
    public StabilityOperationInterface createSymmEigen() {
        return new MySymmEig();
    }

    public static class MySymmEig implements StabilityOperationInterface {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseDoubleMatrix2D matA = convertToColt(inputs[0]);

            EigenvalueDecomposition eig = new EigenvalueDecomposition(matA);

            DenseMatrix64F ejmlD = coltToEjml(eig.getD());
            DenseMatrix64F ejmlV = coltToEjml(eig.getV());

            return new DenseMatrix64F[]{ejmlD,ejmlV};
        }
    }

    @Override
    public StabilityOperationInterface createSymmInverse() {
        return new MySymmInv();
    }

    public static class MySymmInv implements StabilityOperationInterface {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseDoubleMatrix2D matA = convertToColt(inputs[0]);

            CholeskyDecomposition chol = new CholeskyDecomposition(matA);

            DoubleMatrix2D result = chol.solve(DoubleFactory2D.dense.identity(matA.rows()));
            DenseMatrix64F ejmlInv = coltToEjml(result);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }
}