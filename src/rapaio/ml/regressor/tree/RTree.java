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

package rapaio.ml.regressor.tree;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.common.VarSelector;
import rapaio.ml.regressor.AbstractRegression;
import rapaio.ml.regressor.RFit;
import rapaio.ml.regressor.boost.gbt.BTRegression;
import rapaio.ml.regressor.boost.gbt.GBTLossFunction;
import rapaio.util.Pair;

import static rapaio.sys.WS.formatFlex;

/**
 * Implements a regression tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
@Deprecated
public class RTree extends AbstractRegression implements BTRegression {

    private static final long serialVersionUID = -2748764643670512376L;

    int minCount = 1;
    int maxDepth = -1;

    RTreeNominalMethod nominalMethod = RTreeNominalMethod.BINARY;
    RTreeNumericMethod numericMethod = RTreeNumericMethod.BINARY;
    RTreeTestFunction function = RTreeTestFunction.VARIANCE_SUM;
    RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
    RTreePredictor predictor = RTreePredictor.STANDARD;
    VarSelector varSelector = VarSelector.ALL;

    // tree root node
    private RTreeNode root;
    private int rows;

    public static RTree buildDecisionStump() {
        return new RTree()
                .withMaxDepth(2)
                .withNominalMethod(RTreeNominalMethod.BINARY)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_MAJORITY)
                ;
    }

    public static RTree buildC45() {
        return new RTree()
                .withMaxDepth(-1)
                .withNominalMethod(RTreeNominalMethod.FULL)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withMinCount(2)
                ;
    }

    public static RTree buildCART() {
        return new RTree()
                .withMaxDepth(-1)
                .withNominalMethod(RTreeNominalMethod.BINARY)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withMinCount(1);
    }

    @Override
    public void boostFit(Frame x, Var y, Var fx, GBTLossFunction lossFunction) {
        root.boostFit(x, y, fx, lossFunction);
    }

    @Override
    public BTRegression newInstance() {
        return new RTree()
                .withMinCount(minCount)
                .withNumericMethod(numericMethod)
                .withNominalMethod(nominalMethod)
                .withMaxDepth(maxDepth)
                .withSplitter(splitter)
                .withFunction(function)
                .withVarSelector(varSelector);
    }

    private RTree() {
    }

    @Override
    public String name() {
        return "RTree";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("TreeClassifier {");
        sb.append("  varSelector=").append(varSelector.name()).append(",\n");
        sb.append("  minCount=").append(minCount).append(",\n");
        sb.append("  maxDepth=").append(maxDepth).append(",\n");
        sb.append("  numericMethod=").append(numericMethod.name()).append(",\n");
        sb.append("  nominalMethod=").append(nominalMethod.name()).append(",\n");
        sb.append("  function=").append(function.name()).append(",\n");
        sb.append("  splitter=").append(splitter.name()).append(",\n");
        sb.append("  predictor=").append(predictor.name()).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public RTree withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    public RTree withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public RTree withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public RTree withNumericMethod(RTreeNumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public RTree withNominalMethod(RTreeNominalMethod nominalMethod) {
        this.nominalMethod = nominalMethod;
        return this;
    }

    public RTree withFunction(RTreeTestFunction function) {
        this.function = function;
        return this;
    }

    public RTree withSplitter(RTreeSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    public void train(Frame df, Var weights, String... targetVarNames) {

        prepareTraining(df, weights, targetVarNames);

        if (targetNames().length == 0) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }

        rows = df.rowCount();

        root = new RTreeNode(null, "root", spot -> true);
        this.varSelector.withVarNames(inputNames());
        root.learn(this, df, weights, maxDepth < 0 ? Integer.MAX_VALUE : maxDepth);
    }

    @Override
    public RFit fit(Frame df, boolean withResiduals) {
        RFit pred = RFit.newEmpty(this, df, withResiduals).addTarget(firstTargetName());

        df.stream().forEach(spot -> {
            Pair<Double, Double> result = predictor.predict(this, spot, root);
            pred.fit(firstTargetName()).setValue(spot.row(), result._1);
        });
        pred.buildComplete();
        return pred;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append(String.format("n=%d\n", rows));

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, mean (total weight) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);
        return sb.toString();
    }

    private void buildSummary(StringBuilder sb, RTreeNode node, int level) {
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        sb.append(node.getGroupName()).append("  ");

        sb.append(formatFlex(node.getValue()));
        sb.append(" (").append(formatFlex(node.getWeight())).append(") ");
        if (node.isLeaf()) sb.append(" *");
        sb.append("\n");

//        children

        if (!node.isLeaf()) {
            node.getChildren().stream().forEach(child -> buildSummary(sb, child, level + 1));
        }
    }

}