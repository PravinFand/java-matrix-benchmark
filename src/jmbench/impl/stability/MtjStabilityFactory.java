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
import no.uib.cipr.matrix.*;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import static jmbench.impl.runtime.MtjAlgorithmFactory.convertToMtj;
import static jmbench.impl.runtime.MtjAlgorithmFactory.mtjToEjml;


/**
 * @author Peter Abeles
 */
public class MtjStabilityFactory implements StabilityFactory {
    
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
            DenseMatrix matA = convertToMtj(inputs[0]);
            DenseMatrix matB = convertToMtj(inputs[1]);

            DenseMatrix result = new DenseMatrix(matA.numColumns(),matB.numColumns());

            DenseMatrix x = (DenseMatrix)matA.solve(matB,result);

            return new DenseMatrix64F[]{mtjToEjml(x)};
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
            DenseMatrix matA = convertToMtj(inputs[0]);

            int m = matA.numRows();
            int n = matA.numColumns();

            SVD s = new SVD(matA.numRows(),matA.numColumns());
            try {
                s.factor(matA);
            } catch (NotConvergedException e) {
                return null;
            }

            DenseMatrix64F ejmlU = mtjToEjml(s.getU());
            DenseMatrix64F ejmlS = CommonOps.diagR(m,n,s.getS());
            DenseMatrix64F ejmlV = mtjToEjml(s.getVt());

            CommonOps.transpose(ejmlV);

            return new DenseMatrix64F[]{ejmlU,ejmlS,ejmlV};
        }
    }

    @Override
    public StabilityOperationInterface createSymmEigen() {
        return new MySymmEig();
    }

    public static class MySymmEig implements StabilityOperationInterface {
        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            DenseMatrix matA = convertToMtj(inputs[0]);

            SymmDenseEVD eig;
            try {
                eig = SymmDenseEVD.factorize(matA);
            } catch (NotConvergedException e) {
                return null;
            }

            DenseMatrix64F ejmlD = CommonOps.diag(eig.getEigenvalues());
            DenseMatrix64F ejmlV = mtjToEjml(eig.getEigenvectors());

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
            DenseMatrix matA = convertToMtj(inputs[0]);

            DenseCholesky cholesky = new DenseCholesky(matA.numRows(),false);
            LowerSPDDenseMatrix uspd = new LowerSPDDenseMatrix(matA);

            uspd.set(matA);
            if( !cholesky.factor(uspd).isSPD() ) {
                throw new RuntimeException("Is not SPD");
            }

            DenseMatrix result = cholesky.solve(Matrices.identity(matA.numColumns()));
            DenseMatrix64F ejmlInv = mtjToEjml(result);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }
}
