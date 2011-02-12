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

import jmbench.impl.wrapper.MtjBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MemoryFactory;
import jmbench.interfaces.MemoryProcessorInterface;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.NotConvergedException;


/**
 * @author Peter Abeles
 */
public class MtjMemoryFactory implements MemoryFactory {

    @Override
    public void configure() {
        
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(new DenseMatrix(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new MtjBenchmarkMatrix((DenseMatrix)matrix);
    }

    @Override
    public MemoryProcessorInterface mult() {
        return new Mult();
    }

    public static class Mult implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix A = inputs[0].getOriginal();
            DenseMatrix B = inputs[1].getOriginal();
            DenseMatrix C = new DenseMatrix(A.numRows(),B.numColumns());

            for( int i = 0; i < numTrials; i++ ){
                A.mult(B,C);
            }
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
            DenseMatrix A = inputs[0].getOriginal();
            DenseMatrix B = inputs[1].getOriginal();
            DenseMatrix C = new DenseMatrix(A.numRows(),A.numColumns());

            for( int i = 0; i < numTrials; i++ ) {
                C.set(A);
                C.add(B);
            }
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
            DenseMatrix A = inputs[0].getOriginal();
            DenseMatrix y = inputs[1].getOriginal();
            DenseMatrix x = new DenseMatrix(A.numColumns(),1);

            for( int i = 0; i < numTrials; i++ )
                A.solve(y,x);
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
            DenseMatrix A = inputs[0].getOriginal();
            DenseMatrix y = inputs[1].getOriginal();
            DenseMatrix x = new DenseMatrix(A.numColumns(),1);

            for( int i = 0; i < numTrials; i++ )
                A.solve(y,x);
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
            DenseMatrix A = inputs[0].getOriginal();

            no.uib.cipr.matrix.SVD svd = new no.uib.cipr.matrix.SVD(A.numRows(),A.numColumns());
            DenseMatrix tmp = new DenseMatrix(A);

            DenseMatrix U=null,V=null;
            double []S=null;
            for( int i = 0; i < numTrials; i++ ) {
                try {
                    // the input matrix is over written
                    tmp.set(A);
                    no.uib.cipr.matrix.SVD s = svd.factor(tmp);
                    U=s.getU();
                    S=s.getS();
                    V=s.getVt();
                } catch (NotConvergedException e) {
                    throw new RuntimeException(e);
                }
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
            DenseMatrix A = inputs[0].getOriginal();

            no.uib.cipr.matrix.EVD eig = new no.uib.cipr.matrix.EVD(A.numRows());
            DenseMatrix tmp = new DenseMatrix(A);

            DenseMatrix v1=null;
            double v2[]= null;
            for( int i = 0; i < numTrials; i++ ) {
                try {
                    // the input matrix is over written
                    tmp.set(A);
                    EVD e = eig.factor(tmp);
                    v1=e.getRightEigenvectors();
                    v2=e.getRealEigenvalues();
                } catch (NotConvergedException e) {
                    throw new RuntimeException(e);
                }
            }
            if( v1 == null || v2 == null)
                throw new RuntimeException("There is a null") ;
        }
    }
}