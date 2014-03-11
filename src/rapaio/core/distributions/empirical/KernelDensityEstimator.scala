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

package rapaio.core.distributions.empirical

import rapaio.core.stat.Variance
import rapaio.data.Feature

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class KernelDensityEstimator(values: Feature, kernel: KernelFunction, bandwidth: Double) {

  def pdf(x: Double): Double = {
    var sum: Double = 0.0
    var count: Double = 0.0
    for (i <- 0 until values.rowCount) {
      if (!values.missing(i)) {
        count += 1
        sum += kernel.pdf(x, values.values.apply(i), bandwidth)
      }
    }
    sum / (count * bandwidth)
  }

  /**
   * Computes the approximation for bandwidth provided by Silverman,
   * known also as Silverman's rule of thumb.
   * <p/>
   * Is used when the approximated is normal for approximating
   * univariate data.
   * <p/>
   * For further reference check:
   * http://en.wikipedia.org/wiki/Kernel_density_estimation
   *
   * @param vector sample of values
   * @return teh getValue of the approximation for bandwidth
   */
  final def getSilvermanBandwidth(vector: Feature): Double = {
    var sd: Double = Math.sqrt(Variance(vector).value)
    if (sd == 0) {
      sd = 1
    }
    var count: Double = 0
    for (i <- 0 until vector.rowCount) {
      if (!vector.missing(i)) count += 1
    }
    1.06 * sd * Math.pow(count, -1.0 / 5.0)
  }
}