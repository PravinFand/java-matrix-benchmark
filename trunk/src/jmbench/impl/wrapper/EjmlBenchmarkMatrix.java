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

package jmbench.impl.wrapper;

import jmbench.interfaces.BenchmarkMatrix;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;


/**
 * @author Peter Abeles
 */
public class EjmlBenchmarkMatrix implements BenchmarkMatrix {

    DenseMatrix64F mat;

    public EjmlBenchmarkMatrix(DenseMatrix64F mat) {
        this.mat = mat;
    }

    public EjmlBenchmarkMatrix(SimpleMatrix mat) {
        this.mat = mat.getMatrix();
    }

    @Override
    public double get(int row, int col) {
        return mat.get(row,col);
    }

    @Override
    public void set(int row, int col, double value) {
        mat.set(row,col,value);
    }

    @Override
    public int numRows() {
        return mat.numRows;
    }

    @Override
    public int numCols() {
        return mat.numCols;
    }

    @Override
    public Object getOriginal() {
        return mat;
    }
}
