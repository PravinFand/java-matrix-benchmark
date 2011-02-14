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

import jmbench.impl.wrapper.OjAlgoBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MemoryFactory;
import jmbench.interfaces.MemoryProcessorInterface;
import org.ojalgo.function.implementation.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.TransposedStore;


/**
 * @author Peter Abeles
 */
public class OjAlgoMemoryFactory implements MemoryFactory {
    
    @Override
    public void configure() {
        
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(PrimitiveDenseStore.FACTORY.makeZero(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new OjAlgoBenchmarkMatrix((PhysicalStore)matrix);
    }

    @Override
    public MemoryProcessorInterface mult() {
        return new Mult();
    }

    public static class Mult implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            PhysicalStore A = inputs[0].getOriginal();
            PhysicalStore B = inputs[1].getOriginal();
            PhysicalStore C = PrimitiveDenseStore.FACTORY.makeZero(A.getRowDim(),B.getColDim());

            for( int i = 0; i < numTrials; i++ )
                C.fillByMultiplying(A, B);
        }
    }

    @Override
    public MemoryProcessorInterface multTransB() {
        return new MultTransB();
    }

    public static class MultTransB implements MemoryProcessorInterface
    {
        @Override
        public void process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            final PhysicalStore A = inputs[0].getOriginal();
            final MatrixStore BT = new TransposedStore<Number>((MatrixStore<Number>)inputs[1].getOriginal());
            final PhysicalStore C = PrimitiveDenseStore.FACTORY.makeZero(A.getRowDim(),BT.getColDim());

            for( int i = 0; i < numTrials; i++ )
                C.fillByMultiplying(A, BT);
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
            PhysicalStore A = inputs[0].getOriginal();
            PhysicalStore B = inputs[1].getOriginal();
            PhysicalStore C = PrimitiveDenseStore.FACTORY.makeZero(A.getRowDim(),A.getColDim());

            for( int i = 0; i < numTrials; i++ )
                C.fillMatching(A, PrimitiveFunction.ADD, B);
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
            PhysicalStore A = inputs[0].getOriginal();
            PhysicalStore y = inputs[1].getOriginal();

            final LU<Double> lu = LUDecomposition.makePrimitive();

            for( int i = 0; i < numTrials; i++ ){
                lu.compute(A);
                lu.solve(y);
            }
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
            PhysicalStore A = inputs[0].getOriginal();
            PhysicalStore y = inputs[1].getOriginal();

            final QR<Double> qr = QRDecomposition.makePrimitive();

            for( int i = 0; i < numTrials; i++ ){
                qr.compute(A);
                qr.solve(y);
            }
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
            PhysicalStore A = inputs[0].getOriginal();

            final SingularValue<Double> svd = SingularValueDecomposition.makePrimitive();

            MatrixStore<Double> U=null,S=null,V=null;
            for( int i = 0; i < numTrials; i++ ) {
                if (!svd.compute(A)) {
                    throw new RuntimeException("Decomposition failed");
                }
                U=svd.getQ1();
                S=svd.getD();
                V=svd.getQ2();
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
            PhysicalStore A = inputs[0].getOriginal();

            final Eigenvalue<Double> eig = EigenvalueDecomposition.makePrimitive();

            MatrixStore<Double> D=null,V=null;
            for( int i = 0; i < numTrials; i++ ) {
                if (!eig.computeSymmetric(A)) {
                    throw new RuntimeException("Decomposition failed");
                }
                D=eig.getD();
                V=eig.getV();
            }
            if( D == null || V == null)
                throw new RuntimeException("There is a null") ;
        }
    }
}