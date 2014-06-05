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

package rapaio.core.distributions;

import rapaio.core.RandomSource;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author Aurelian Tutuianu
 */
public abstract class Distribution {

    /**
     * @return canonical getName of the distribution
     */
    public abstract String getName();

    /**
     * @param x value for which it calculates log of probability
     * @return log of probability of x
     */
    public double logpdf(double x) {
        double pdf = pdf(x);
        if (pdf <= 0) {
            return -Double.MAX_VALUE;
        }
        return Math.log(pdf);
    }

    /**
     * Calculates probability mass function (pmf) of a discrete distribution or
     * probability density function (pdf) of a continuous distribution for given
     * value x
     *
     * @param x value for which it calculates
     * @return pmf / pdf of x
     */
    abstract public double pdf(double x);

    abstract public double cdf(double x);

    abstract public double quantile(double p);

    public Function<Double, Double> getPdfFunction() {
        return this::pdf;
    }

    public Function<Double, Double> getCdfFunction() {
        return this::cdf;
    }

    abstract public double min();

    abstract public double max();

    public Numeric sample(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> quantile(RandomSource.nextDouble()))
                .collect(Var.numericCollector());
    }

    abstract public double mean();

    abstract public double mode();

    abstract public double variance();

    abstract public double skewness();

    abstract public double kurtosis();

    public double sd() {
        return Math.sqrt(variance());
    }
}