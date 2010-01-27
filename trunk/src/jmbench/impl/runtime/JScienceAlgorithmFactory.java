/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
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

package jmbench.impl.runtime;

import jmbench.PackageMatrixConversion;
import jmbench.impl.MatrixLibrary;
import jmbench.interfaces.AlgorithmInterface;
import jmbench.interfaces.LibraryAlgorithmFactory;
import org.ejml.data.DenseMatrix64F;
import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.LUDecomposition;
import org.jscience.mathematics.vector.Matrix;


/**
 * @author Peter Abeles
 */
public class JScienceAlgorithmFactory implements LibraryAlgorithmFactory {

    private static abstract class MyInterface implements AlgorithmInterface
    {
        @Override
        public String getName() {
            return MatrixLibrary.JSCIENCE.getVersionName();
        }
    }

    @Override
    public AlgorithmInterface chol() {
        return null;
    }

    @Override
    public AlgorithmInterface lu() {
        return new LU();
    }

    public static class LU extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);


            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                LUDecomposition.valueOf(matA);
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface svd() {
        return null;
    }

    @Override
    public AlgorithmInterface eigSymm() {
        return null;
    }

    @Override
    public AlgorithmInterface qr() {
        return null;
    }

    @Override
    public AlgorithmInterface det() {
        return new Det();
    }

    public static class Det extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.determinant();
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface invert() {
        return new Inv();
    }

    public static class Inv extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.inverse();
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface add() {
        return new Add();
    }

    public static class Add extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);
            Float64Matrix matB = convertToFloat64(inputs[1]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.plus(matB);
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface mult() {
        return new Mult();
    }

    public static class Mult extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);
            Float64Matrix matB = convertToFloat64(inputs[1]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.times(matB);
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface multTransA() {
        return new MulTranA();
    }

    public static class MulTranA extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);
            Float64Matrix matB = convertToFloat64(inputs[1]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.transpose().times(matB);
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface scale() {
        return new Scale();
    }

    public static class Scale extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.times(Float64.valueOf(2.5));
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface solveExact() {
        return new Solve();
    }

    @Override
    public AlgorithmInterface solveOver() {
        return null;
    }

    public static class Solve extends MyInterface {
        @Override
        public long process(DenseMatrix64F[]inputs, long numTrials) {
            Float64Matrix matA = convertToFloat64(inputs[0]);
            Float64Matrix matB = convertToFloat64(inputs[1]);

            long prev = System.currentTimeMillis();

            for( long i = 0; i < numTrials; i++ ) {
                matA.solve(matB);
            }

            return System.currentTimeMillis()-prev;
        }
    }

    @Override
    public AlgorithmInterface transpose() {
        return null;
    }
    
    /**
     * Converts it into a Float64Matrix used by JScience
     *
     * @param orig A DenseMatrix64F in EML
     * @return A Float64Matrix in CommonsMath
     */
    public static Float64Matrix convertToFloat64( DenseMatrix64F orig )
    {
        return Float64Matrix.valueOf(PackageMatrixConversion.convertToArray2D(orig));
    }

    public static DenseMatrix64F  jsciToEjml( Matrix<Float64> orig )
    {
        if( orig == null )
            return null;

        DenseMatrix64F ret = new DenseMatrix64F(orig.getNumberOfRows(),orig.getNumberOfColumns());

        for( int i = 0; i < ret.numRows; i++ ) {
            for( int j = 0; j < ret.numCols; j++ ) {
                ret.set(i,j, orig.get(i,j).doubleValue());
            }
        }

        return ret;
    }
}