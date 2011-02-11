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

package jmbench.tools.runtime;

import jmbench.interfaces.AlgorithmInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class FactoryRuntimeEvaluationCase {
    RuntimePerformanceFactory factory;

    RuntimeBenchmarkConfig config;

    public FactoryRuntimeEvaluationCase( RuntimePerformanceFactory factory , RuntimeBenchmarkConfig config ) {
        this.factory = factory;
        this.config = config;
    }

    public List<RuntimeEvaluationCase> createCases() {

        List<RuntimeEvaluationCase> ret = new ArrayList<RuntimeEvaluationCase>();

        AlgorithmInterface alg;

        alg = factory.mult();
        if( config.mult && alg != null ) ret.add( createMatrixMult(factory,alg,1));

        alg = factory.add();
        if( config.add && alg != null ) ret.add( createMatrixAdd(factory,alg));

        alg = factory.transpose();
        if( config.transposeSquare && alg != null ) ret.add( createTransposeSquare(factory,alg));

        alg = factory.transpose();
        if( config.transposeTall && alg != null ) ret.add( createTransposeTall(factory,alg));

        alg = factory.transpose();
        if( config.transposeWide && alg != null ) ret.add( createTransposeWide(factory,alg));

        alg = factory.scale();
        if( config.scale && alg != null ) ret.add( createScale(factory,alg));

        alg = factory.det();
        if( config.det && alg != null ) ret.add( createDeterminant(factory,alg));

        alg = factory.invert();
        if( config.invert && alg != null ) ret.add( createInvert(factory,alg));

        alg = factory.invertSymmPosDef();
        if( config.invertSymmPosDef && alg != null ) ret.add( createInvertSymmPosDef(factory,alg));

        alg = factory.svd();
        if( config.svd && alg != null ) ret.add( createSVD(factory,alg));

        alg = factory.chol();
        if( config.chol && alg != null ) ret.add( createCholesky(factory,alg));

        alg = factory.multTransB();
        if( config.multTransB && alg != null ) ret.add( createMultTranB(factory,alg));

        alg = factory.solveExact();
        if( config.solveExact &&  alg != null ) ret.add( createSolveEq(factory,alg));

        alg = factory.solveOver();
        if( config.solveOver && alg != null ) ret.add( createSolveOver(factory,alg));

        alg = factory.qr();
        if( config.qr && alg != null ) ret.add( createQR(factory,alg));

        alg = factory.lu();
        if( config.lu && alg != null ) ret.add( createLU(factory,alg));

        alg = factory.eigSymm();
        if( config.eigSymm && alg != null ) ret.add( createEigSymm(factory,alg));

        return ret;
    }


    public RuntimeEvaluationCase createMatrixMult( RuntimePerformanceFactory factory ,
                                                   AlgorithmInterface alg , double scale ) {

        InputOutputGenerator generator = new MultGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Mult c=a*b","mult",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createMatrixAdd( RuntimePerformanceFactory factory ,
                                                  AlgorithmInterface alg ) {

        InputOutputGenerator generator = new AddGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Add c=a+b","add",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createTransposeSquare( RuntimePerformanceFactory factory ,
                                                  AlgorithmInterface alg ) {

        InputOutputGenerator generator = new TransposeSquareGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Transpose Square: b=a^T","tranSq",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createTransposeTall( RuntimePerformanceFactory factory ,
                                                      AlgorithmInterface alg ) {

        InputOutputGenerator generator = new TransposeTallGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Transpose Tall: b=a^T","tranTall",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createTransposeWide( RuntimePerformanceFactory factory ,
                                                      AlgorithmInterface alg ) {

        InputOutputGenerator generator = new TransposeWideGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Transpose Wide: b=a^T","tranWide",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createScale( RuntimePerformanceFactory factory ,
                                              AlgorithmInterface alg) {

        InputOutputGenerator generator = new ScaleGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Scale b=alpha*a","scale",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createDeterminant( RuntimePerformanceFactory factory ,
                                                    AlgorithmInterface alg) {

        InputOutputGenerator generator = new DeterminantGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Determinant","det",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createInvert( RuntimePerformanceFactory factory ,
                                               AlgorithmInterface alg) {

        InputOutputGenerator generator = new InvertGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Invert b=inv(a)","inv",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createInvertSymmPosDef( RuntimePerformanceFactory factory ,
                                                         AlgorithmInterface alg) {

        InputOutputGenerator generator = new InvertSymmPosDefGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Invert Symm b=inv(a)","invSymmPosDef",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createSVD( RuntimePerformanceFactory factory ,
                                            AlgorithmInterface alg) {

        InputOutputGenerator generator = new SvdGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("SVD","svd",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createCholesky( RuntimePerformanceFactory factory ,
                                                 AlgorithmInterface alg) {

        InputOutputGenerator generator = new CholeskyGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Cholesky","cholesky",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createMultTranB( RuntimePerformanceFactory factory ,
                                                  AlgorithmInterface alg) {

        InputOutputGenerator generator = new MultTranBGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Mult c=a*b^T","multTranB",matDimen,
                factory,alg,generator);
    }

    /**
     * The number of unknowns matches the number of equations.
     */
    public RuntimeEvaluationCase createSolveEq( RuntimePerformanceFactory factory ,
                                                AlgorithmInterface alg) {

        InputOutputGenerator generator = new SolveEqGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Solve m=n","solveEq",matDimen,
                factory,alg,generator);
    }

    /**
     * See how well it can solve an overdetermined system.
     */
    public RuntimeEvaluationCase createSolveOver( RuntimePerformanceFactory factory ,
                                                  AlgorithmInterface alg) {

        InputOutputGenerator generator = new SolveOverGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Solve m>n","solveOver",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createQR( RuntimePerformanceFactory factory ,
                                           AlgorithmInterface alg) {

        InputOutputGenerator generator = new QrGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("QR","QR",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createLU( RuntimePerformanceFactory factory ,
                                           AlgorithmInterface alg) {

        InputOutputGenerator generator = new LuGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("LU","LU",matDimen,
                factory,alg,generator);
    }

    public RuntimeEvaluationCase createEigSymm( RuntimePerformanceFactory factory ,
                                                AlgorithmInterface alg) {

        InputOutputGenerator generator = new EigSymmGenerator();
        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Eigen for Symm Matrices","EigSymm",matDimen,
                factory,alg,generator);
    }

    private static int[] createDimenList( int min , int max ) {
        List<Integer> a = new ArrayList<Integer>();

        int val = min;

        int dec = 1;

        while( (val / (dec*10)) >= 1 ) {
            dec *= 10;
        }

        a.add(val);

        while(  val < max ) {
            if( val < 5 ) {
                val++;
            } else {
                int w = val/dec;
                switch( w ) {
                    case 1:
                        val = 2*dec;
                        break;

                    case 2:
                        val = 5*dec;
                        break;

                    case 5:
                        val = 10*dec;
                        dec *= 10;
                        break;

                    default:
                        if( w < 5 ) {
                            val = 5*dec;
                        } else {
                            val = 10*dec;
                            dec *= 10;
                        }
                }
            }

            a.add(val);
        }

        if( a.get(a.size()-1) != max ) {
            a.remove(a.size()-1);
            a.add(max);
        }

        int ret[] = new int[ a.size() ];
        for( int i = 0; i < ret.length; i++ ) {
            ret[i] = a.get(i);
        }
        return ret;
    }

}
