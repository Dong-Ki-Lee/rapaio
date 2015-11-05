/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 *
 */

package rapaio.ml.classifier.bayes;

import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.bayes.estimator.GaussianPdf;
import rapaio.ml.classifier.bayes.estimator.MultinomialPmf;
import rapaio.ml.classifier.bayes.estimator.NominalEstimator;
import rapaio.ml.classifier.bayes.estimator.NumericEstimator;
import rapaio.ml.common.Capabilities;
import rapaio.sys.WS;
import rapaio.util.Tag;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Naive Bayes Classifier.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class NaiveBayes extends AbstractClassifier {

    private static final long serialVersionUID = -7602854063045679683L;
    private static final Logger logger = Logger.getLogger(NaiveBayes.class.getName());

    // algorithm parameters

    private double laplaceSmoother = 1;
    private NumericEstimator numEstimator = new GaussianPdf();
    private NominalEstimator nomEstimator = new MultinomialPmf();
    private Tag<PriorSupplier> priorSupplier = PRIORS_MLE;

    // prediction artifacts

    private Map<String, Double> priors;
    private Map<String, NumericEstimator> numMap;
    private Map<String, NominalEstimator> nomMap;

    @Override
    public NaiveBayes newInstance() {
        return new NaiveBayes()
                .withNumEstimator(numEstimator)
                .withNomEstimator(nomEstimator)
                .withLaplaceSmoother(laplaceSmoother);
    }

    @Override
    public String name() {
        return "NaiveBayes";
    }

    @Override
    public String fullName() {
        return name() + "(numEstimator=" + numEstimator.name() + ", nomEstimator=" + nomEstimator.name() + ")";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER)
                .withInputCount(0, 1_000_000)
                .withInputTypes(VarType.NOMINAL, VarType.NUMERIC, VarType.INDEX, VarType.BINARY)
                .withTargetCount(1, 1)
                .withTargetTypes(VarType.NOMINAL)
                .withAllowMissingTargetValues(false)
                .withAllowMissingInputValues(true);
    }

    public NaiveBayes withNumEstimator(NumericEstimator numEstimator) {
        this.numEstimator = numEstimator;
        return this;
    }

    public NaiveBayes withNomEstimator(NominalEstimator nomEstimator) {
        this.nomEstimator = nomEstimator;
        return this;
    }

    public NaiveBayes withLaplaceSmoother(double laplaceSmoother) {
        this.laplaceSmoother = laplaceSmoother;
        return this;
    }

    public double laplaceSmoother() {
        return laplaceSmoother;
    }

    public NaiveBayes withPriorSupplier(Tag<PriorSupplier> priorSupplier) {
        this.priorSupplier = priorSupplier;
        return this;
    }

    @Override
    public NaiveBayes train(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);

        // build priors

        priors = PRIORS_MLE.get().learnPriors(df, weights, this);

        // build conditional probabilities

        nomMap = new ConcurrentHashMap<>();
        numMap = new ConcurrentHashMap<>();

        logger.config("start learning...");
        Arrays.stream(df.varNames()).parallel().forEach(
                testCol -> {
                    if (firstTargetName().equals(testCol)) {
                        return;
                    }
                    if (df.var(testCol).type().isNumeric()) {
                        NumericEstimator estimator = numEstimator.newInstance();
                        estimator.learn(df, firstTargetName(), testCol);
                        numMap.put(testCol, estimator);
                        return;
                    }
                    if (df.var(testCol).type().isNominal()) {
                        NominalEstimator estimator = nomEstimator.newInstance();
                        estimator.learn(this, df, weights, firstTargetName(), testCol);
                        nomMap.put(testCol, estimator);
                    }
                });
        logger.config("learning phase finished");
        return this;
    }

    @Override
    public CFit fit(Frame df, final boolean withClasses, final boolean withDensities) {

        logger.config("start fitting values...");

        CFit pred = CFit.newEmpty(this, df, withClasses, withDensities);
        pred.addTarget(firstTargetName(), firstTargetLevels());

        IntStream.range(0, df.rowCount()).parallel().forEach(
                i -> {
                    DVector dv = DVector.newEmpty(firstTargetLevels());
                    for (int j = 1; j < firstTargetLevels().length; j++) {
                        double sumLog = Math.log(priors.get(firstTargetLevel(j)));
                        for (String testCol : numMap.keySet()) {
                            if (df.missing(i, testCol))
                                continue;
                            sumLog += Math.log(numMap.get(testCol).cpValue(df.value(i, testCol), firstTargetLevel(j)));
                        }
                        for (String testCol : nomMap.keySet()) {
                            if (df.missing(i, testCol)) {
                                continue;
                            }
                            sumLog += Math.log(nomMap.get(testCol).cpValue(df.label(i, testCol), firstTargetLevel(j)));
                        }
                        dv.increment(j, Math.exp(sumLog));
                    }
                    dv.normalize(false);

                    if (withClasses) {
                        pred.firstClasses().setIndex(i, dv.findBestIndex(false));
                    }
                    if (withDensities) {
                        for (int j = 1; j < firstTargetLevels().length; j++) {
                            pred.firstDensity().setValue(i, j, dv.get(j));
                        }
                    }
                });
        logger.config("fitting phase finished.");
        return pred;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("NaiveBayes model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName()).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().summary()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        sb.append("prior probabilities:\n");
        String targetName = firstTargetName();
        Arrays.stream(firstTargetLevels()).skip(1).forEach(label -> sb.append("> P(").append(targetName).append("='").append(label).append("')=").append(WS.formatFlex(priors.get(label))).append("\n"));

        if (!numMap.isEmpty()) {
            sb.append("numerical estimators:\n");
            numMap.entrySet().forEach(e -> sb.append("> ").append(e.getKey()).append(" : ").append(e.getValue().learningInfo()).append("\n"));
        }
        if (!nomMap.isEmpty()) {
            sb.append("nominal estimators:\n");
            nomMap.entrySet().forEach(e -> sb.append("> ").append(e.getKey()).append(" : ").append(e.getValue().learningInfo()).append("\n"));
        }
        return sb.toString();
    }

    interface PriorSupplier extends Serializable {
        Map<String, Double> learnPriors(Frame df, Var weights, NaiveBayes nb);
    }

    public static Tag<PriorSupplier> PRIORS_MLE = Tag.valueOf("PRIORS_MLE", (df, weights, nb) -> {
        Map<String, Double> priors = new HashMap<>();
        DVector dv = DVector.newFromWeights(df.var(nb.firstTargetName()), weights, nb.firstTargetLevels());

        // laplace add something for smoothing is problematic for priors, so we do not do it
//        IntStream.range(0, nb.firstTargetLevels().length).forEach(i -> dv.increment(i, nb.laplaceSmoother));
        dv.normalize(false);

        for (int i = 1; i < nb.firstTargetLevels().length; i++) {
            priors.put(nb.firstTargetLevels()[i], dv.get(i));
        }
        return priors;
    });

    public static Tag<PriorSupplier> PRIORS_UNIFORM = Tag.valueOf("PRIORS_UNIFORM", (df, weights, nb) -> {
        Map<String, Double> priors = new HashMap<>();
        double p = 1.0 / nb.firstTargetLevels().length;
        for (int i = 1; i < nb.firstTargetLevels().length; i++) {
            priors.put(nb.firstTargetLevels()[i], p);
        }
        return priors;
    });
}
