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
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

import static jmbench.impl.runtime.OjAlgoAlgorithmFactory.convertToOjAlgo;
import static jmbench.impl.runtime.OjAlgoAlgorithmFactory.ojAlgoToEjml;


/**
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class OjAlgoStabilityFactory implements StabilityFactory {

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
            PhysicalStore matA = convertToOjAlgo(inputs[0]);
            PhysicalStore matB = convertToOjAlgo(inputs[1]);

            BasicMatrix basicB = new PrimitiveMatrix(matB);
            BasicMatrix basicA = new PrimitiveMatrix(matA);
            
            return new DenseMatrix64F[] {ojAlgoToEjml(basicA.solve(basicB).toPrimitiveStore())};
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
            PhysicalStore matA = convertToOjAlgo(inputs[0]);

            SingularValue<Double> s = SingularValueDecomposition.makePrimitive();
            if( !s.compute(matA) )
                return null;

            DenseMatrix64F ejmlU = ojAlgoToEjml(s.getQ1());
            DenseMatrix64F ejmlS = ojAlgoToEjml(s.getD());
            DenseMatrix64F ejmlV = ojAlgoToEjml(s.getQ2());

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
            PhysicalStore matA = convertToOjAlgo(inputs[0]);

            Eigenvalue<Double> eig = EigenvalueDecomposition.makePrimitive();
            if( !eig.computeSymmetric(matA) )
                return null;

            DenseMatrix64F ejmlD = ojAlgoToEjml(eig.getD());
            DenseMatrix64F ejmlV = ojAlgoToEjml(eig.getV());

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
            PhysicalStore matA = convertToOjAlgo(inputs[0]);

            final Cholesky<Double> chol = CholeskyDecomposition.makePrimitive();

            if (!chol.compute(matA)) {
                throw new RuntimeException("Decomposition failed");
            }
            MatrixStore<Double> inverse = chol.getInverse();
            DenseMatrix64F ejmlInv = ojAlgoToEjml(inverse);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }
}