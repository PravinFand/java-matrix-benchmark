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

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import Jama.SingularValueDecomposition;
import jmbench.interfaces.StabilityFactory;
import jmbench.interfaces.StabilityOperationInterface;
import org.ejml.data.DenseMatrix64F;

import static jmbench.impl.runtime.JamaAlgorithmFactory.convertToJama;
import static jmbench.impl.runtime.JamaAlgorithmFactory.jamaToEjml;


/**
 * @author Peter Abeles
 */
public class JamaStabilityFactory implements StabilityFactory {
    
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
            Matrix matA = convertToJama(inputs[0]);
            Matrix matB = convertToJama(inputs[1]);

            return new DenseMatrix64F[] {jamaToEjml(matA.solve(matB))};
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
            Matrix matA = convertToJama(inputs[0]);

            // Jama does not support SVD for wide matrices and in that situations
            // the input matrix is transposed.
            if( matA.getColumnDimension() > matA.getRowDimension() ) {
                SingularValueDecomposition s = matA.transpose().svd();

                DenseMatrix64F ejmlU = jamaToEjml(s.getV());
                DenseMatrix64F ejmlS = jamaToEjml(s.getS());
                DenseMatrix64F ejmlV = jamaToEjml(s.getU());

                return new DenseMatrix64F[]{ejmlU,ejmlS,ejmlV};
            } else {
                SingularValueDecomposition s = matA.svd();

                DenseMatrix64F ejmlU = jamaToEjml(s.getU());
                DenseMatrix64F ejmlS = jamaToEjml(s.getS());
                DenseMatrix64F ejmlV = jamaToEjml(s.getV());

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
            Matrix matA = convertToJama(inputs[0]);

            EigenvalueDecomposition eig = matA.eig();

            DenseMatrix64F ejmlD = jamaToEjml(eig.getD());
            DenseMatrix64F ejmlV = jamaToEjml(eig.getV());

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
            Matrix matA = convertToJama(inputs[0]);

            int N = matA.getColumnDimension();
            Matrix result =  matA.chol().solve(Matrix.identity(N,N));

            DenseMatrix64F ejmlInv = jamaToEjml(result);

            return new DenseMatrix64F[]{ejmlInv};
        }
    }

    public static void main( String []args ) {
        Matrix A = new Matrix(new double[][]{{1,7},{-5,4}});

        EigenvalueDecomposition eig = A.eig();

        Matrix D = eig.getD();
        printJama(D);

        printJama(eig.getV());
        System.out.println();
    }

    private static void printJama(Matrix d) {
        System.out.println("--------------------------");
        System.out.println();
        for( int i = 0; i < d.getRowDimension(); i++ ) {
            for( int j = 0; j < d.getColumnDimension(); j++ ) {
                System.out.print(d.get(i,j)+" ");
            }
            System.out.println();
        }
    }
}