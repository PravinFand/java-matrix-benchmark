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

package jmbench.tools.stability.tests;

import jmbench.interfaces.StabilityFactory;
import jmbench.interfaces.StabilityOperationInterface;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;


/**
 * @author Peter Abeles
 */
public class SolverAccuracy extends SolverCommon {

    public SolverAccuracy(long randomSeed, StabilityFactory factory, StabilityOperationInterface operation,
                          int totalTrials, double breakingPoint,
                          int minLength, int maxLength, boolean linearSolver)
    {
        super(randomSeed, factory , operation, totalTrials, breakingPoint, minLength, maxLength, linearSolver);
    }

    public SolverAccuracy(){}

    @Override
    public void performTest() {
        for( int i = 0; i < totalTrials; i++ ) {
//            System.out.print("Trial "+i+"  ");
            int m,n;

            if( isLinearSolver ) {
                m = n = rand.nextInt(maxLength-minLength)+minLength;
            } else {
                // least squares can handle over determined systems
                m = rand.nextInt(maxLength-minLength)+minLength;
                n = minLength;
                if( m > minLength )
                    n += rand.nextInt(m-minLength);
            }


            createMatrix(m,n,1);

            evaluateSolver();

            saveResults();
        }
    }

    private void evaluateSolver() {
        reason = OutputError.NO_ERROR;
        foundResult = Double.NaN;

        DenseMatrix64F inputs[] = new DenseMatrix64F[]{A,b};
        DenseMatrix64F results[];
        try {
            results = operation.process(inputs);
        } catch( Exception e ) {
            addUnexpectedException(e);
//                e.printStackTrace();
            reason = OutputError.UNEXPECTED_EXCEPTION;
            return;
        }

        if( results == null ) {
            reason = OutputError.DETECTED_FAILURE;
            return;
        }

        DenseMatrix64F x = results[0];

        if( MatrixFeatures.hasUncountable(x) ) {
            reason = OutputError.UNCOUNTABLE;
            return;
        }

        foundResult = StabilityBenchmark.residualErrorMetric(A,x,b);

        if( Double.isNaN(foundResult) || Double.isInfinite(foundResult) ) {
            reason = OutputError.LARGE_ERROR;
            return;
        }
    }

    @Override
    public String getTestName() {
        if( isLinearSolver )
            return "Linear accuracy";
        else
            return "Least squares accuracy";  
    }

    @Override
    public String getFileName() {
        if( isLinearSolver )
            return "LinearAccuracy";
        else
            return "LeastSquaresAccuracy";
    }
}
