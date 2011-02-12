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
import org.apache.commons.math.linear.*;
import org.apache.commons.math.util.MathUtils;
import org.ejml.data.DenseMatrix64F;

import static jmbench.impl.runtime.CommonsMathAlgorithmFactory.convertToBlockReal;
import static jmbench.impl.runtime.CommonsMathAlgorithmFactory.realToEjml;


/**
 * @author Peter Abeles
 */
public class CommonsMathStabilityFactory implements StabilityFactory {

    @Override
    public void configure() {
        
    }

    public StabilityOperationInterface createLinearSolver() {
        return new MyLinearSolver();
    }

    public StabilityOperationInterface createLSSolver() {
        return new MyLSSolver();
    }

    public static class MyLinearSolver implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            BlockRealMatrix matA = convertToBlockReal(inputs[0]);
            BlockRealMatrix matB = convertToBlockReal(inputs[1]);

            LUDecomposition lu = new LUDecompositionImpl(matA);
            try {
                DenseMatrix64F x = realToEjml(lu.getSolver().solve(matB));
                return new DenseMatrix64F[]{x};
            } catch( InvalidMatrixException e ) {
                return null;
            }
        }
    }

    public static class MyLSSolver implements StabilityOperationInterface
    {

        @Override
        public DenseMatrix64F[] process(DenseMatrix64F[] inputs) {
            BlockRealMatrix matA = convertToBlockReal(inputs[0]);
            BlockRealMatrix matB = convertToBlockReal(inputs[1]);

            QRDecomposition qr = new QRDecompositionImpl(matA);
            DenseMatrix64F x = realToEjml(qr.getSolver().solve(matB));

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
            BlockRealMatrix matA = convertToBlockReal(inputs[0]);

            SingularValueDecomposition s;
            try {
                s = new SingularValueDecompositionImpl(matA);
            } catch( InvalidMatrixException e ) {
                return null;
            }

            DenseMatrix64F ejmlU = realToEjml(s.getU());
            DenseMatrix64F ejmlS = realToEjml(s.getS());
            DenseMatrix64F ejmlV = realToEjml(s.getV());

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
            BlockRealMatrix matA = convertToBlockReal(inputs[0]);

            EigenDecomposition eig;
            try {
                eig = new EigenDecompositionImpl(matA, MathUtils.SAFE_MIN);
            } catch( InvalidMatrixException  e ) {
                return null;
            }

            DenseMatrix64F ejmlD = realToEjml(eig.getD());
            DenseMatrix64F ejmlV = realToEjml(eig.getV());

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
            BlockRealMatrix matA = convertToBlockReal(inputs[0]);

            CholeskyDecompositionImpl chol = null;
            try {
                chol = new CholeskyDecompositionImpl(matA);
            } catch (NotSymmetricMatrixException e) {
                throw new RuntimeException(e);
            } catch (NotPositiveDefiniteMatrixException e) {
                throw new RuntimeException(e);
            }
            RealMatrix result = chol.getSolver().getInverse();
            DenseMatrix64F ejmlInv = realToEjml(result);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }
}