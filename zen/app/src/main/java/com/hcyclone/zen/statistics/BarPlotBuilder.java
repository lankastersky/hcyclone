package com.hcyclone.zen.statistics;

import android.content.Context;
import android.graphics.Color;

import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.hcyclone.zen.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds bar charts.
 */
public class BarPlotBuilder extends PlotBuilder {

  public BarPlotBuilder(ChallengesValuesBuilder challengesValuesBuilder, Context context) {
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
        100 // or challengesValuesBuilder.maxChallengeRate
    );
    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(PERCENT_FORMAT);
  }

  private void setSeries(XYPlot plot, List<? extends Number> values) {
    BarFormatter formatter = new BarFormatter(Color.rgb(0xFF, 0x63, 0x47), Color.LTGRAY);

    setSeries(plot, values, formatter);

    // Setup the BarRenderer with our selected options
    BarRenderer renderer = plot.getRenderer(BarRenderer.class);
    BarRenderer.BarGroupWidthMode barGroupWidthMode = BarRenderer.BarGroupWidthMode.FIXED_WIDTH;
    int groupWidth = context.getResources().getInteger(R.integer.statistics_bar_plot_group_with);
    renderer.setBarGroupWidth(barGroupWidthMode, groupWidth);
  }

  @Override
  protected void setAxes(XYPlot plot, List<String> domainValues, double rangeUpperBoundary) {
    plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1);
    plot.setDomainLowerBoundary(-1, BoundaryMode.FIXED);
    super.setAxes(plot, domainValues, rangeUpperBoundary);
  }
}
