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

package rapaio.data;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public enum VarType {
    NUMERIC(true, false),
    INDEX(true, false),
    NOMINAL(false, true),
    BINARY(true, false);

    private final boolean numeric;
    private final boolean nominal;

    VarType(boolean numeric, boolean nominal) {
        this.numeric = numeric;
        this.nominal = nominal;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean isNominal() {
        return nominal;
    }

}