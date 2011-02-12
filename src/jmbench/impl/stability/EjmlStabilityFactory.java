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
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.CovarianceOps;
import org.ejml.ops.EigenOps;


/**
 * @author Peter Abeles
 */
public class EjmlStabilityFactory implements StabilityFactory {

    @Override
    public void configure() {
        
    }

    public StabilityOperationInterface createLinearSolver() {
        return new MyLinearSolver();
    }

    public StabilityOperationInterface createLSSolver() {
        return new MyLinearSolver();
    }

    public static class MyLinearSolver implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseMatrix64F A = inputs[0];
            DenseMatrix64F b = inputs[1];

            DenseMatrix64F x = new DenseMatrix64F(A.numCols,b.numCols);

            if( !CommonOps.solve(inputs[0],inputs[1],x) )
                return null;

            return new DenseMatrix64F[]{x};
        }
    }

    @Override
    public StabilityOperationInterface createSvd() {
        return new MySvd();
    }

    public static class MySvd implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseMatrix64F A = inputs[0];

            SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols);

            if( !DecompositionFactory.decomposeSafe(svd,A) )
                return null;

            DenseMatrix64F U = svd.getU(false);
            DenseMatrix64F S = svd.getW(null);
            DenseMatrix64F V = svd.getV(false);

            return new DenseMatrix64F[]{U,S,V};
        }
    }

    @Override
    public StabilityOperationInterface createSymmEigen() {
        return new MyEigen();
    }

    public static class MyEigen implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseMatrix64F A = inputs[0];

            EigenDecomposition<DenseMatrix64F> eig = EigenOps.decompositionSymmetric(A.numCols,true);

            if( !DecompositionFactory.decomposeSafe(eig,A) )
                return null;

            DenseMatrix64F D = EigenOps.createMatrixD(eig);
            DenseMatrix64F V = EigenOps.createMatrixV(eig);

            return new DenseMatrix64F[]{D,V};
        }
    }

    @Override
    public StabilityOperationInterface createSymmInverse() {
        return new MySymmInverse();
    }

    public static class MySymmInverse implements StabilityOperationInterface
    {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseMatrix64F A = inputs[0];
            DenseMatrix64F A_inv = new DenseMatrix64F(A.numRows,A.numCols);

            if( !CovarianceOps.invert(A,A_inv) )
                throw new IllegalArgumentException("Not SPD");

            return new DenseMatrix64F[]{A_inv};
        }
    }
}
