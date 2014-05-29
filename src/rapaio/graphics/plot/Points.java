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

package rapaio.graphics.plot;

import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.pch.PchPalette;

import java.awt.*;

/**
 * @author tutuianu
 */
public class Points extends PlotComponent {

    private final Var x;
    private final Var y;

    public Points(Var x, Var y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Range buildRange() {
        if (x.rowCount() == 0) {
            return null;
        }
        Range range = new Range();
        for (int i = 0; i < Math.min(x.rowCount(), y.rowCount()); i++) {
            if (x.missing(i) || y.missing(i)) {
                continue;
            }
            range.union(x.value(i), y.value(i));
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {

        for (int i = 0; i < Math.min(x.rowCount(), y.rowCount()); i++) {
            if (x.missing(i) || y.missing(i)) {
                continue;
            }
            g2d.setColor(getCol(i));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
            PchPalette.STANDARD.draw(g2d,
                    getParent().xScale(x.value(i)),
                    getParent().yScale(y.value(i)),
                    getSize(i), getPch(i));
        }
    }
}
