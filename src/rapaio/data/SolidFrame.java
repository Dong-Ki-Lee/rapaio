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

package rapaio.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A frame which is not mapped, its values are contained in vectors.
 *
 * @author Aurelian Tutuianu
 */
public class SolidFrame extends AbstractFrame {

    private static final long serialVersionUID = 4963238370571140813L;
    private int rows;
    private final Var[] vars;
    private final HashMap<String, Integer> colIndex;
    private final String[] names;

    // public builders

    public static SolidFrame newWrapOf(List<Var> vars) {
        int rows = Integer.MAX_VALUE;
        for (Var var : vars) {
            rows = Math.min(rows, var.rowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return newWrapOf(rows, vars);
    }

    public static SolidFrame newWrapOf(Var... vars) {
        int rows = Integer.MAX_VALUE;
        for (Var var : vars) {
            rows = Math.min(rows, var.rowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return new SolidFrame(rows, Arrays.asList(vars));
    }

    public static SolidFrame newWrapOf(int rows, Var... vars) {
        return newWrapOf(rows, Arrays.asList(vars));
    }

    public static SolidFrame newWrapOf(int rows, List<Var> vars) {
        for (Var var : vars) {
            rows = Math.min(rows, var.rowCount());
        }
        return new SolidFrame(rows, vars);
    }

    /**
     * Builds a new frame with missing values, having the same variables
     * as in the source frame and having given row count.
     *
     * @param rowCount row count
     * @param src      source frame
     * @return new instance of solid frame built according with the source frame variables
     */
    public static SolidFrame newEmptyFrom(Frame src, int rowCount) {
        Var[] vars = new Var[src.varCount()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = src.getVar(i).getType().newInstance(rowCount);
        }
        return SolidFrame.newWrapOf(vars);
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of getRowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame newMatrix(int rows, String... colNames) {
        return newMatrix(rows, Arrays.asList(colNames));
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of getRowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame newMatrix(int rows, List<String> colNames) {
        List<Var> vars = new ArrayList<>();
        colNames.stream().forEach(n -> vars.add(Numeric.newFill(rows, 0).withName(n)));
        return SolidFrame.newWrapOf(rows, vars);
    }

    // private constructor

    private SolidFrame(int rows, List<Var> vars) {
        for (Var var : vars) {
            if (var instanceof MappedVar)
                throw new IllegalArgumentException("Not allowed mapped vectors in solid frame");
            if (var instanceof BoundVar)
                throw new IllegalArgumentException("Not allowed bounded vectors in solid frame");
        }
        this.rows = rows;
        this.vars = new Var[vars.size()];
        this.colIndex = new HashMap<>();
        this.names = new String[vars.size()];

        for (int i = 0; i < vars.size(); i++) {
            this.vars[i] = vars.get(i); //.solidCopy();
            this.colIndex.put(this.vars[i].name(), i);
            this.names[i] = this.vars[i].name();
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int varCount() {
        return vars.length;
    }

    @Override
    public String[] varNames() {
        return names;
    }

    @Override
    public int varIndex(String name) {
        if (!colIndex.containsKey(name)) {
            throw new IllegalArgumentException("Invalid column name: " + name);
        }
        return colIndex.get(name);
    }

    @Override
    public Var getVar(int col) {
        if (col >= 0 && col < vars.length) {
            return vars[col];
        }
        throw new IllegalArgumentException("Invalid column index: " + col);
    }

    @Override
    public Var getVar(String name) {
        return getVar(varIndex(name));
    }

    @Override
    public Frame bindVars(Var... vars) {
        return BoundFrame.newByVars(this, BoundFrame.newByVars(vars));
    }

    @Override
    public Frame bindVars(Frame df) {
        return BoundFrame.newByVars(this, df);
    }

    @Override
    public Frame mapVars(VarRange range) {
        List<String> varNames = range.parseVarNames(this);
        List<Var> vars = varNames.stream().map(this::getVar).collect(Collectors.toList());
        return SolidFrame.newWrapOf(rowCount(), vars);
    }

    @Override
    public Frame addRows(int rowCount) {
        for (int i = 1; i < vars.length; i++) {
            if (vars[i - 1].rowCount() != vars[i].rowCount()) {
                throw new IllegalArgumentException("variables must have the same size");
            }
        }
        for (int i = 0; i < vars.length; i++) {
            vars[i].addRows(rowCount);
        }
        this.rows += rowCount;
        return null;
    }

    @Override
    public Frame bindRows(Frame df) {
        return BoundFrame.newByRows(this, df);
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        return MappedFrame.newByRow(this, mapping);
    }
}
