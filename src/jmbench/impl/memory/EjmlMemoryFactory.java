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

package jmbench.impl.memory;

import jmbench.impl.MatrixLibrary;
import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MemoryFactory;
import jmbench.interfaces.MemoryProcessorInterface;
import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.alg.dense.decomposition.SingularValueDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * @author Peter Abeles
 */
public class EjmlMemoryFactory implements MemoryFactory {


    @Override
    public MatrixLibrary getLibraryInfo() {
        return MatrixLibrary.EJML;
    }

    @Override
    public void configure() {
        
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new EjmlBenchmarkMatrix((DenseMatrix64F)matrix);
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        DenseMatrix64F A = new DenseMatrix64F(numRows,numCols);
        return wrap(A);
    }

    @Override
    public MemoryProcessorInterface mult() {
        return new Mult();
    }

    public static class Mult implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F A = inputs[0].getOriginal();
            DenseMatrix64F B = inputs[1].getOriginal();
            DenseMatrix64F C = new DenseMatrix64F(A.numRows,B.numCols);

            for( int i = 0; i < numTrials; i++ )
                CommonOps.mult(A,B,C);
        }
    }

    @Override
    public MemoryProcessorInterface add() {
        return new Add();
    }

    public static class Add implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F A = inputs[0].getOriginal();
            DenseMatrix64F B = inputs[1].getOriginal();
            DenseMatrix64F C = new DenseMatrix64F(A.numRows,A.numCols);

            for( int i = 0; i < numTrials; i++ )
                CommonOps.add(A,B,C);
        }
    }

    @Override
    public MemoryProcessorInterface solveEq() {
        return new SolveLinear();
    }

    public static class SolveLinear implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F A = inputs[0].getOriginal();
            DenseMatrix64F y = inputs[1].getOriginal();
            DenseMatrix64F x = new DenseMatrix64F(A.numCols,1);

            for( int i = 0; i < numTrials; i++ )
                CommonOps.solve(A,y,x);
        }
    }

    @Override
    public MemoryProcessorInterface solveLS() {
        return new SolveLS();
    }

    public static class SolveLS implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F A = inputs[0].getOriginal();
            DenseMatrix64F y = inputs[1].getOriginal();
            DenseMatrix64F x = new DenseMatrix64F(A.numCols,1);

            for( int i = 0; i < numTrials; i++ )
                CommonOps.solve(A,y,x);
        }
    }

    @Override
    public MemoryProcessorInterface svd() {
        return new SVD();
    }

    public static class SVD implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F A = inputs[0].getOriginal();

            SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(A.numRows,A.numCols);

            DenseMatrix64F U =null,V =null, S=null;
            for( int i = 0; i < numTrials; i++ ) {
                svd.decompose(A);

                U = svd.getU(false);
                V = svd.getV(false);
                S = svd.getW(null);
            }
            if( U == null || S == null || V == null )
                throw new RuntimeException("There is a null");
        }
    }

    @Override
    public MemoryProcessorInterface eig() {
        return new Eig();
    }

    public static class Eig implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F A = inputs[0].getOriginal();

            EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(A.numRows);
            DenseMatrix64F v[] =  new DenseMatrix64F[Math.min(A.numRows,A.numCols)];
            for( int i = 0; i < numTrials; i++ ) {
                eig.decompose(A);

                for( int j = 0; j < v.length; j++ ) {
                    v[j] = eig.getEigenVector(j);
                }
            }
            if( v[0] == null )
                throw new RuntimeException("There is a null");
        }
    }
}
