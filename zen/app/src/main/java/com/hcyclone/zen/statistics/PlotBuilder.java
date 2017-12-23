package com.hcyclone.zen.statistics;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesFormatter;
import com.hcyclone.zen.R;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

/**
 * Builds charts using androidplot. See examples:
 * https://github.com/halfhp/androidplot/blob/master/docs/quickstart.md
 * https://github.com/halfhp/androidplot/tree/master/demoapp/src/main/java/com/androidplot/demos
 */
public abstract class PlotBuilder {

  protected static final Format INTEGER_FORMAT = new DecimalFormat("#");

  protected final Context context;
  protected ChallengesValuesBuilder challengesValuesBuilder;

  public PlotBuilder(ChallengesValuesBuilder challengesValuesBuilder, Context context) {
    this.challengesValuesBuilder = challengesValuesBuilder;
    this.context = context;
  }

  protected void decorate(XYPlot plot) {
    plot.getGraph().setMarginBottom(
        context.getResources().getInteger(R.integer.statistics_plot_margin_bottom));
    int marginRight = context.getResources().getInteger(R.integer.statistics_plot_margin_right);
    plot.getGraph().setPaddingRight(marginRight);
    int marginTop = context.getResources().getInteger(R.integer.statistics_plot_margin_top);
    plot.getGraph().setPaddingTop(marginTop);

    plot.getGraph().setLineExtensionRight(marginRight);
    plot.getGraph().setLineExtensionTop(marginTop);
  }

  protected void setSeries(
      XYPlot plot, List<? extends Number> values, XYSeriesFormatter formatter) {
    // turn the above arrays into XYSeries':
    // (Y_VALS_ONLY means use the element index as the x value)
    XYSeries series = new SimpleXYSeries(
        values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series");
    plot.addSeries(series, formatter);
  }

  protected void setAxes(XYPlot plot, List<String> domainValues, double rangeUpperBoundary) {
    int scaledRangeUpperBoundary = roundUpToMultiplierOf10(rangeUpperBoundary);
    plot.setRangeUpperBoundary(scaledRangeUpperBoundary, BoundaryMode.FIXED);
    plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
    //plot.setRangeStep(StepMode.SUBDIVIDE, domainValues.size());
    plot.setRangeStep(StepMode.SUBDIVIDE, 11);

    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(INTEGER_FORMAT);

    plot
        .getGraph()
        .getLineLabelStyle(XYGraphWidget.Edge.BOTTOM)
        .setFormat(new DomainFormat(domainValues));
  }

  /** Rounds to the least value that is not less than {@code value} and divided by 10ю */
  @VisibleForTesting
  static int roundUpToMultiplierOf10(double value) {
    if (value <= 0) {
      return 0;
    }
    if (value % 10 == 0) {
      return (int) value;
    }
//    int multiplierDigits = (int) Math.ceil(Math.log10(value));
//    return (int) Math.pow(10, multiplierDigits);
    return ((int) (value / 10) + 1) * 10;
  }
}
