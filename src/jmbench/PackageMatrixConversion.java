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

package jmbench;

import Jama.Matrix;
import no.uib.cipr.matrix.DenseMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.ejml.data.DenseMatrix64F;
import org.jscience.mathematics.vector.Float64Matrix;
import org.ojalgo.matrix.store.PhysicalStore;


/**
 * @author Peter Abeles
 */
public class PackageMatrixConversion {

    public static void convertToEjml( PhysicalStore src , DenseMatrix64F dst )
    {
        if( src.getRowDim() != dst.getNumRows() || src.getColDim() != dst.getNumCols() )
            throw new IllegalArgumentException("Matrices are not the same shape");

        for( int y = 0; y < src.getRowDim(); y++ ) {
            for( int x = 0; x < src.getColDim(); x++ ) {
                dst.set(y,x,src.get(y,x).doubleValue());
            }
        }
    }

    public static void convertToEjml( Float64Matrix src , DenseMatrix64F dst )
    {
        if( src.getNumberOfRows() != dst.getNumRows() || src.getNumberOfColumns() != dst.getNumCols() )
            throw new IllegalArgumentException("Matrices are not the same shape");

        for( int y = 0; y < src.getNumberOfRows(); y++ ) {
            for( int x = 0; x < src.getNumberOfColumns(); x++ ) {
                dst.set(y,x,src.get(y,x).doubleValue());
            }
        }
    }

    public static void convertToEjml( BlockRealMatrix src , DenseMatrix64F dst )
    {
        if( src.getRowDimension() != dst.getNumRows() || src.getColumnDimension() != dst.getNumCols() )
            throw new IllegalArgumentException("Matrices are not the same shape");

        for( int y = 0; y < src.getRowDimension(); y++ ) {
            for( int x = 0; x < src.getColumnDimension(); x++ ) {
                dst.set(y,x,src.getEntry(y,x));
            }
        }
    }

    public static void convertToEjml( Array2DRowRealMatrix src , DenseMatrix64F dst )
    {
        if( src.getRowDimension() != dst.getNumRows() || src.getColumnDimension() != dst.getNumCols() )
            throw new IllegalArgumentException("Matrices are not the same shape");

        for( int y = 0; y < src.getRowDimension(); y++ ) {
            for( int x = 0; x < src.getColumnDimension(); x++ ) {
                dst.set(y,x,src.getEntry(y,x));
            }
        }
    }

    public static void convertToEjml( DenseMatrix src , DenseMatrix64F dst )
    {
        if( src.numRows() != dst.getNumRows() || src.numColumns() != dst.getNumCols() )
            throw new IllegalArgumentException("Matrices are not the same shape");

        for( int y = 0; y < src.numRows(); y++ ) {
            for( int x = 0; x < src.numColumns(); x++ ) {
                dst.set(y,x,src.get(y,x));
            }
        }
    }

    public static void convertToEjml( Matrix src , DenseMatrix64F dst )
    {
        if( src.getRowDimension() != dst.getNumRows() || src.getColumnDimension() != dst.getNumCols() )
            throw new IllegalArgumentException("Matrices are not the same shape");

        for( int y = 0; y < src.getRowDimension(); y++ ) {
            for( int x = 0; x < src.getColumnDimension(); x++ ) {
                dst.set(y,x,src.get(y,x));
            }
        }
    }

    public static double[][] convertToArray2D( DenseMatrix64F orig )
    {
        double [][]mat = new double[ orig.numRows ][ orig.numCols ];

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                mat[i][j] = orig.get(i,j);
            }
        }

        return mat;
    }

    /**
     * Converts DenseMatrix64F used in EML into a Array2DRowRealMatrix found in commons-math.
     *
     * @param orig A DenseMatrix64F in EML
     * @return A Array2DRowRealMatrix in CommonsMath
     */
    public static Array2DRowRealMatrix convertToReal2D( DenseMatrix64F orig )
    {
        return new Array2DRowRealMatrix(convertToArray2D(orig));
    }
}
