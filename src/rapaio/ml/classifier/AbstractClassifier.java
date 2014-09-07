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

package rapaio.ml.classifier;

import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.ml.classifier.colselect.StdVarSelector;
import rapaio.ml.classifier.colselect.VarSelector;

import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    protected VarSelector varSelector = new StdVarSelector();
    protected String[] targetVars;
    protected Map<String, String[]> dict;
    protected Map<String, Nominal> classes;
    protected Map<String, Frame> densities;

    @Override
    public VarSelector getVarSelector() {
        return varSelector;
    }

    @Override
    public Classifier withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    @Override
    public String[] targetVars() {
        return targetVars;
    }

    @Override
    public Map<String, String[]> dictionaries() {
        return dict;
    }

    @Override
    public Map<String, Nominal> classes() {
        return classes;
    }

    @Override
    public Map<String, Frame> densities() {
        return densities;
    }

    @Override
    public void reset() {
        classes = null;
        densities = null;
    }
}
