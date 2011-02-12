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
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.doublematrix.DenseDoubleMatrix2D;

import static jmbench.impl.runtime.UjmpAlgorithmFactory.convertToUjmp;
import static jmbench.impl.runtime.UjmpAlgorithmFactory.ujmpToEjml;


/**
 * @author Peter Abeles
 */
public class UjmpStabilityFactory implements StabilityFactory {

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
            Matrix matA = convertToUjmp(inputs[0]);
            Matrix matB = convertToUjmp(inputs[1]);

            return new DenseMatrix64F[] {ujmpToEjml(matA.solve(matB))};
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
            Matrix matA = convertToUjmp(inputs[0]);

            Matrix []s = matA.svd();

            DenseMatrix64F ejmlU = ujmpToEjml(s[0]);
            DenseMatrix64F ejmlS = ujmpToEjml(s[1]);
            DenseMatrix64F ejmlV = ujmpToEjml(s[2]);

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
            Matrix matA = convertToUjmp(inputs[0]);

            Matrix[]eig = matA.eig();

            DenseMatrix64F ejmlD = ujmpToEjml(eig[1]);
            DenseMatrix64F ejmlV = ujmpToEjml(eig[0]);

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
            Matrix matA = convertToUjmp(inputs[0]);

            Matrix eye = MatrixFactory.eye(matA.getSize());

            Matrix result = DenseDoubleMatrix2D.chol.solve(matA, eye);

            DenseMatrix64F ejmlInv = ujmpToEjml(result);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }
}