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
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;


/**
 * @author Peter Abeles
 */
public class SolverSingular extends SolverCommon
        implements BreakingPointBinarySearch.Processor
{

    private volatile DenseMatrix64F U;
    private volatile DenseMatrix64F V;
    private volatile double []sv;
    private volatile double svMag = 1;
    private volatile int whichSV;
    private volatile BreakingPointBinarySearch search;

    public SolverSingular(long randomSeed, StabilityFactory factory, StabilityOperationInterface operation,
                          int totalTrials,
                          double breakingPoint ,
                          int minLength, int maxLength,
                          boolean linearSolver) {
        super(randomSeed, factory, operation, totalTrials, breakingPoint , minLength, maxLength, linearSolver);
    }

    public SolverSingular() {

    }

    @Override
    public String getTestName() {
        if( isLinearSolver )
            return "Linear singular";
        else
            return "Least Squares singular";
    }

    @Override
    public String getFileName() {
        if( isLinearSolver )
            return "LinearSingular";
        else
            return "LeastSquaresSingular";  
    }

    @Override
    public void performTest() {

        search = new BreakingPointBinarySearch(this);

        for( int i = 0; i < totalTrials; i++ ) {
//            System.out.print("Trial "+i+"  \n");
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

            int min = Math.min(m,n);
            whichSV = rand.nextInt(min);

//            System.out.println("trail = "+i+"( "+m+" "+n+" )");

            evaluateNearlySingular(m,n);

            saveResults();
        }
    }

    private void evaluateNearlySingular(int m, int n) {
        U = RandomMatrices.createOrthogonal(m,m,rand);
        V = RandomMatrices.createOrthogonal(n,n,rand);

        int o = Math.min(m,n);

        sv = new double[o];
        for( int i = 0; i < o; i++ )
            sv[i] = svMag;

        A = createMatrix(U,V,sv);
        DenseMatrix64F x = RandomMatrices.createRandom(n,1,rand);
        b = new DenseMatrix64F(m,1);

        CommonOps.mult(A,x,b);

        reason = OutputError.NO_ERROR;
        int point = search.findCriticalPoint(-1,findMaxPow(0.9)+1);
        foundResult = Math.pow(0.9,point)*svMag;
    }

    @Override
    public boolean check(int testPoint) {
        sv[whichSV] = Math.pow(0.9,testPoint)*svMag;

        DenseMatrix64F A_adj = createMatrix(U,V,sv);
        DenseMatrix64F results[];

        try {
            DenseMatrix64F[] inputs = new DenseMatrix64F[]{A_adj,b};
            results = operation.process(inputs);
        } catch( Exception e ) {
//            e.printStackTrace();
            addUnexpectedException(e);
            reason = OutputError.UNEXPECTED_EXCEPTION;
            return false;
        }

        if( results == null ) {
            reason = OutputError.DETECTED_FAILURE;
            return false;
        }

        DenseMatrix64F x = results[0];


        if(MatrixFeatures.hasUncountable(x)) {
            reason = OutputError.UNCOUNTABLE;
            return false;
        }

        double residual = StabilityBenchmark.residualErrorMetric(A_adj,x,b);
//            System.out.println(residual);
        if( residual > breakingPoint ) {
            reason = OutputError.LARGE_ERROR;
            return false;
        }
        return true;
    }


    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isLinearSolver() {
        return isLinearSolver;
    }

    public void setLinearSolver(boolean linearSolver) {
        isLinearSolver = linearSolver;
    }
}