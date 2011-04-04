package uk.ac.ic.doc.gander;

import java.awt.Dimension;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import uk.ac.ic.doc.gander.analysers.Tallies;

public class FrequencyPlotter {

	private HistogramDataset data;
	private JFreeChart chart;

	public FrequencyPlotter(String title, String categoryAxisTitle) {

		data = new HistogramDataset();

		chart = ChartFactory.createXYLineChart(title, categoryAxisTitle,
				"Frequency", data, PlotOrientation.VERTICAL, true, true, true);

		ChartFrame frame = new ChartFrame(title, chart);
		frame.setPreferredSize(new Dimension(1024, 768));
		frame.pack();
		frame.setVisible(true);
	}

	public void plot(Tallies tallies, String seriesTitle) {
		double[] series = new double[tallies.counts().size()];
		for (int i = 0; i < tallies.counts().size(); ++i)
			series[i] = tallies.counts().get(i);
		data.addSeries(seriesTitle, series, tallies.max());
		chart.fireChartChanged();
		chart.getXYPlot().configureDomainAxes();
		chart.getXYPlot().configureRangeAxes();
	}
}
