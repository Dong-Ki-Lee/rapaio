package rapaio.ml.linear;

import org.junit.Test;
import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.Points;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static rapaio.data.Vectors.newNumFrom;
import static rapaio.workspace.Workspace.draw;
import static rapaio.workspace.Workspace.setPrinter;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegressionTest {

	//    @Test
	public void simpleLinearTest() {

		List<Vector> vectors = new ArrayList<>();
		vectors.add(newNumFrom(1, 1, 1, 1, 1));
		vectors.add(newNumFrom(1, 2, 3, 4, 5));
		vectors.add(newNumFrom(1, 4, 9, 16, 25));
		vectors.add(newNumFrom(2.8, 3.2, 7.1, 6.8, 8.8));

		List<String> names = new ArrayList<>();
		names.add("x0");
		names.add("x1");
		names.add("x2");
		names.add("y1");

		Frame df = new SolidFrame(5, vectors, names);

		LinearModelRegressor lm = new LinearModelRegressor();
		lm.learn(df, "y1");

		Summary.lines(lm.getCoeff());
		Summary.lines(lm.getFitValues());
		Summary.lines(lm.getResidualValues());

	}

	//    @Test
	public void pearsonTest() throws IOException, URISyntaxException {

		setPrinter(new LocalPrinter());
		Frame df = Datasets.loadPearsonHeightDataset();

		Vector intercept = Vectors.newNum(df.getRowCount(), 1);
		df = Frames.addCol(df, intercept, "I", 0);
		Summary.summary(df);

		LinearModelRegressor lm = new LinearModelRegressor();
		lm.learn(df, "Son");

		Summary.lines(lm.getCoeff());

		draw(new Plot()
				.add(new Points(df.getCol("Father"), df.getCol("Son"))
						.setPchIndex(1).setSizeIndex(1)
				)
				.add(new ABLine(
						lm.getCoeff().getValue(1, 1),
						lm.getCoeff().getValue(0, 1)))
				.setXRange(58, 78)
				.setYRange(58, 78)
				.setBottomLabel("Father")
				.setLeftLabel("Son")
		);

		draw(new Plot()
				.add(new Points(df.getCol("Father"), lm.getResidualValues()))
		);
	}

	@Test
	public void testESTL() throws IOException {
		setPrinter(new LocalPrinter());
		Frame df = Datasets.loadProstateCancer();

		Numeric inter = Vectors.newNum(df.getRowCount(), 1.);
//		df = Frames.addCol(df, inter, "inter", 0);

		Frame bf = new SolidFrame(
				df.getRowCount(),
				new Vector[]{df.getCol(0)},
				new String[]{df.getColNames()[0]});

		for (int i = 1; i < df.getColCount(); i++) {
			bf = Frames.addCol(bf, df.getCol(i), df.getColNames()[i], 0);
		}

		LinearModelRegressor lm1 = new LinearModelRegressor();
		lm1.learn(df, "lpsa");
		LinearModelRegressor lm2 = new LinearModelRegressor();
		lm2.learn(bf, "lpsa");

		Summary.lines(lm1.getCoeff());
		Summary.lines(lm2.getCoeff());
	}
}