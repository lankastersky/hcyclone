package com.hcyclone.zen.statistics;

import android.content.Context;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.hcyclone.zen.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds line charts.
 */
public class LinePlotBuilder extends PlotBuilder {

  public LinePlotBuilder(ChallengesValuesBuilder challengesValuesBuilder, Context context) {
    super(challengesValuesBuilder, context);
  }

  public void buildChallengesPlot(XYPlot plot) {
    decorate(plot);
    setSeries(
        plot,
        new ArrayList<>(challengesValuesBuilder.challengeTimeToNumber.values()));
    setAxes(
        plot,
        challengesValuesBuilder.challengesDates,
        challengesValuesBuilder.maxChallengeNumber);
  }

  public void buildRatesPlot(XYPlot plot) {
    decorate(plot);
    setSeries(
        plot,
        new ArrayList<>(challengesValuesBuilder.challengeTimeToRates.values()));
    setAxes(
        plot,
        challengesValuesBuilder.challengesDates,
        100 //or challengesValuesBuilder.maxChallengeRate
    );
    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(PERCENT_FORMAT);
  }

  private void setSeries(XYPlot plot, List<? extends Number> values) {
    // create formatters to use for drawing a series using LineAndPointRenderer
    // and configure them from xml:
    LineAndPointFormatter formatter =
        new LineAndPointFormatter(context, R.xml.line_point_formatter_with_labels);

    float hOffset =
        context.getResources().getInteger(R.integer.statistics_line_plot_point_label_offset);
    if (values.get(0) instanceof Float) {
      hOffset *= context.getResources().getDisplayMetrics().density;
    }
    formatter.getPointLabelFormatter().hOffset = hOffset;

    if (values.size() >= 3) {
      // Add some smoothing to the lines:
      // see: http://androidplot.com/smooth-curves-and-androidplot/
      formatter.setInterpolationParams(
          new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Uniform));
    }
    setSeries(plot, values, formatter);
  }

  @Override
  protected void setAxes(XYPlot plot, List<String> domainValues, double rangeUpperBoundary) {
    // draw n number of evenly spaced lines
    plot.setDomainStep(StepMode.SUBDIVIDE, domainValues.size());
    super.setAxes(plot, domainValues, rangeUpperBoundary);
  }
}
