package com.hcyclone.zen.statistics;

import android.content.Context;

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
public class PlotBuilder {

  protected static final Format PERCENT_FORMAT = new DecimalFormat("#");

  protected final Context context;
  protected ChallengesValuesBuilder challengesValuesBuilder;

  public PlotBuilder(ChallengesValuesBuilder challengesValuesBuilder, Context context) {
    this.challengesValuesBuilder = challengesValuesBuilder;
    this.context = context;
  }

  protected void decorate(XYPlot plot) {
    plot.getGraph().setMarginLeft(
        context.getResources().getInteger(R.integer.statistics_plot_margin_left));
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
    plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
    plot.setRangeUpperBoundary(rangeUpperBoundary, BoundaryMode.FIXED);
    //plot.setRangeStep(StepMode.SUBDIVIDE, domainValues.size());
    plot.setRangeStep(StepMode.SUBDIVIDE, 11);

    plot
        .getGraph()
        .getLineLabelStyle(XYGraphWidget.Edge.BOTTOM)
        .setFormat(new DomainFormat(domainValues));
  }
}
