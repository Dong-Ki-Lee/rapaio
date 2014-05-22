/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.core.distributions.empirical;

import rapaio.core.stat.Variance;
import rapaio.data.Vector;
import rapaio.data.filters.BaseFilters;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KDE {

    private final double[] values;
    private final KFunc kernel;
    private final double bandwidth;

    public KDE(Vector values) {
        this.values = values.stream().mapToDouble().toArray();
        this.kernel = new KFuncGaussian();
        this.bandwidth = getSilvermanBandwidth(values);
    }

    public KDE(Vector values, double bandwidth) {
        this(values, new KFuncGaussian(), bandwidth);
    }

    public KDE(Vector values, KFunc kernel) {
        this(values, kernel, getSilvermanBandwidth(values));
    }

    public KDE(Vector values, KFunc kernel, double bandwidth) {
        this.values = BaseFilters.sort(values).stream().filter(s -> !s.isMissing()).mapToDouble().toArray();
        this.kernel = kernel;
        this.bandwidth = bandwidth;
    }

    public double pdf(double x) {
        int from = Arrays.binarySearch(values, kernel.getMinValue(x, bandwidth));
        if (from < 0) from = -from - 1;
        int to = Arrays.binarySearch(values, kernel.getMaxValue(x, bandwidth));
        if (to < 0) to = -to - 1;
        return IntStream.range(from, to)
                .parallel()
                .mapToDouble(i -> kernel.pdf(x, values[i], bandwidth))
                .sum() / (values.length * bandwidth);
    }

    public Function<Double, Double> getPdf() {
        return this::pdf;
    }

    public KFunc getKernel() {
        return kernel;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Computes the approximation for bandwidth provided by Silverman,
     * known also as Silverman's rule of thumb.
     * <p>
     * Is used when the approximated is normal for approximating
     * univariate data.
     * <p>
     * For further reference check:
     * http://en.wikipedia.org/wiki/Kernel_density_estimation
     *
     * @param vector sample of values
     * @return teh getValue of the approximation for bandwidth
     */
    public static double getSilvermanBandwidth(Vector vector) {
        Variance var = new Variance(vector);
        double sd = Math.sqrt(var.getValue());
        if (sd == 0) {
            sd = 1;
        }
        double count = 0;
        for (int i = 0; i < vector.rowCount(); i++) if (!vector.isMissing(i)) count++;
        return 1.06 * sd * Math.pow(count, -1. / 5.);
    }
}