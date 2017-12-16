package com.hcyclone.zen.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.ChallengeModel;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;

/**
 * Shows challenges statistics.
 */
public class StatisticsFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_statistics));

    ChallengeModel challengeModel = ChallengeModel.getInstance();

    View view = inflater.inflate(R.layout.fragment_statistics, container, false);

    int level = challengeModel.getLevel();
    TextView levelView = view.findViewById(R.id.statistics_level);
    levelView.setText(String.format(getString(R.string.fragment_statistics_level),
        Utils.localizedChallengeLevel(level, getContext())));

    TextView finishedChallengesView = view.findViewById(R.id.statistics_finished_challenges_number);
    finishedChallengesView.setText(
        String.format(
            getString(R.string.fragment_statistics_finished_challenges_number),
            challengeModel.getFinishedChallenges().size(),
            challengeModel.getShownChallengesNumber()));

    int averageRatingPercent = Math.round(challengeModel.getAverageRating(getContext()) * 100);
    TextView averageRatingView = view.findViewById(R.id.statistics_average_rating);
    averageRatingView.setText(
        String.format(
            getString(R.string.fragment_statistics_average_rating), averageRatingPercent));

    plot((XYPlot) view.findViewById(R.id.plot));
    plot((XYPlot) view.findViewById(R.id.plot2));

    return view;
  }

  /**
   * See examples:
   * https://github.com/halfhp/androidplot/blob/master/docs/quickstart.md
   * https://github.com/halfhp/androidplot/blob/master/demoapp/src/main/java/com/androidplot/demos/TimeSeriesActivity.java
   *
   * @param plot
   */
  void plot(XYPlot plot) {
    plot.setPlotMargins(0, 0, 0, 0);
    plot.getGraph().getBackgroundPaint().setColor(Color.WHITE);
    plot.getLegend().setVisible(false);
    plot.getGraph().setMarginLeft(90);
    plot.getGraph().setMarginBottom(90);

    // create a couple arrays of y-values to plot:
    final String[] domainLabels = {"17/01/01", "17/01/02", "17/01/03", "17/01/04", "17/01/05"};
    Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};

    // draw a domain tick for each year:
    plot.setDomainStep(StepMode.SUBDIVIDE, domainLabels.length);

    // turn the above arrays into XYSeries':
    // (Y_VALS_ONLY means use the element index as the x value)
    XYSeries series1 = new SimpleXYSeries(
        Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

    // create formatters to use for drawing a series using LineAndPointRenderer
    // and configure them from xml:
    LineAndPointFormatter series1Format =
        new LineAndPointFormatter(getContext(), R.xml.line_point_formatter_with_labels);

    // just for fun, add some smoothing to the lines:
    // see: http://androidplot.com/smooth-curves-and-androidplot/
    series1Format.setInterpolationParams(
        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

    // add a new series' to the xyplot:
    plot.addSeries(series1, series1Format);

    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
      @Override
      public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        int i = Math.round(((Number) obj).floatValue());
        return toAppendTo.append(domainLabels[i % 5]);
      }

      @Override
      public Object parseObject(String source, ParsePosition pos) {
        return null;
      }
    });
  }
}
