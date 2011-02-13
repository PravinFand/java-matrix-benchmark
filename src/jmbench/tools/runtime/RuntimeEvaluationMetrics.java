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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Statistical metrics computed from performance collected across all the trials.
 *
 * @author Peter Abeles
 */
public class RuntimeEvaluationMetrics
{
    public final static int METRIC_MEAN = 0;
    public final static int METRIC_STDEV = 1;
    public final static int METRIC_MIN = 2;
    public final static int METRIC_MAX = 3;
    public final static int METRIC_MEDIAN = 4;

    public double mean;
    public double stdev;

    public double min;
    public double max;
    public double median;

    // unsorted raw results.  these are in the order they were generated
    public List<RuntimeMeasurement> rawResults;

    public RuntimeEvaluationMetrics( List<RuntimeMeasurement> vals ) {
        if( vals.size() <= 0 )
            throw new RuntimeException("No samples");
        rawResults = new ArrayList<RuntimeMeasurement>(vals);

        Collections.sort(vals);

        int numSamples = vals.size();
        min = vals.get(0).getOpsPerSec();
        max = vals.get( numSamples - 1).getOpsPerSec();

        median = vals.get( numSamples/2 ).getOpsPerSec();

        mean = 0;
        for( RuntimeMeasurement r : vals ) {
            mean += r.getOpsPerSec()/max;
        }
        mean = max*(mean/numSamples);

        stdev = 0;

        for( RuntimeMeasurement r : vals ) {
            double d = r.getOpsPerSec();
            stdev += (d - mean)*(d - mean);
        }
        stdev = Math.sqrt( stdev / numSamples );
    }

    public RuntimeEvaluationMetrics(){}

    public double getMetric( int which ) {
        switch( which ) {
            case METRIC_MEAN:
                return mean;

            case METRIC_STDEV:
                return stdev;

            case METRIC_MIN:
                return min;

            case METRIC_MAX:
                return max;

            case METRIC_MEDIAN:
                return median;
        }

        throw new IllegalArgumentException("Unknown metric");
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStdev() {
        return stdev;
    }

    public void setStdev(double stdev) {
        this.stdev = stdev;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public List<RuntimeMeasurement> getRawResults() {
        return rawResults;
    }

    public void setRawResults(List<RuntimeMeasurement> rawResults) {
        this.rawResults = rawResults;
    }
}
