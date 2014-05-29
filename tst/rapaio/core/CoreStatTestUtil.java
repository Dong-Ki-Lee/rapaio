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

package rapaio.core;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.filters.BaseFilters;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class CoreStatTestUtil {

    private Frame df;

    public CoreStatTestUtil() throws IOException, URISyntaxException {
        df = new Csv().withHeader(false).read(getClass(), "core_stat.csv");
        Var[] vars = new Var[df.colCount()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = BaseFilters.toNumeric(df.col(i));
        }
        df = new SolidFrame(df.rowCount(), vars, df.colNames());
    }

    public Frame getDataFrame() {
        return df;
    }
}