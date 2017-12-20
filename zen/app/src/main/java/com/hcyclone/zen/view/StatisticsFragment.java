package com.hcyclone.zen.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.google.common.collect.Iterables;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows challenges statistics.
 */
public class StatisticsFragment extends Fragment {

  private static final DateFormat FINISHED_CHALLENGE_TIME_DATE_FORMAT =
      SimpleDateFormat.getDateInstance(DateFormat.SHORT);
  private static final Calendar CALENDAR = Calendar.getInstance();

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
    //plot((XYPlot) view.findViewById(R.id.plot2));

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

    ChallengeModel challengeModel = ChallengeModel.getInstance();
    List<Challenge> finishedChallenges = challengeModel.getFinishedChallengesSorted();
    int days = 10;
    for (int i = 0; i < days; i++) {
      Challenge challenge = new Challenge(
          "id" + i,
          "content" + i,
          "details" + i,
          "type" + i,
          i % 3, // level
          "source" + i,
          "url" + i,
          "quote" + i
      );
      CALENDAR.setTime(new Date());
      CALENDAR.add(Calendar.DAY_OF_MONTH, (int) (Math.random() * days));
      challenge.setFinishedTime(CALENDAR.getTimeInMillis());
      finishedChallenges.add(challenge);
    }
    if (finishedChallenges.isEmpty()) {
      return;
    }
    final Map<String, Integer> finishedChallengesPerDay = new LinkedHashMap<>();
    for (Challenge challenge : finishedChallenges) {
      long finishedTime = challenge.getFinishedTime();
      Date date = new Date(finishedTime);
      String dateString = FINISHED_CHALLENGE_TIME_DATE_FORMAT.format(date);
      int challengesNumber = 0;
      if (finishedChallengesPerDay.containsKey(dateString)) {
        challengesNumber = finishedChallengesPerDay.get(dateString);
      }
      finishedChallengesPerDay.put(dateString, ++challengesNumber);
    }

    List<Integer> finishedChallengesNumberList = new ArrayList<>(finishedChallengesPerDay.values());
    plot.setDomainStep(StepMode.SUBDIVIDE, finishedChallengesNumberList.size());

    // turn the above arrays into XYSeries':
    // (Y_VALS_ONLY means use the element index as the x value)
    XYSeries series1 = new SimpleXYSeries(
        finishedChallengesNumberList, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

    // create formatters to use for drawing a series using LineAndPointRenderer
    // and configure them from xml:
    LineAndPointFormatter series1Format =
        new LineAndPointFormatter(getContext(), R.xml.line_point_formatter_with_labels);

    if (finishedChallengesNumberList.size() >= 3) {
      // Add some smoothing to the lines:
      // see: http://androidplot.com/smooth-curves-and-androidplot/
      series1Format.setInterpolationParams(
          new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
    }

    // add a new series' to the xyplot:
    plot.addSeries(series1, series1Format);

    plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
    plot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
    plot.setRangeStepValue(1);
    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("###"));

    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
      @Override
      public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        int i = Math.round(((Number) obj).floatValue());
        String dateString = Iterables.get(finishedChallengesPerDay.keySet(), i);
        return toAppendTo.append(dateString);
      }

      @Override
      public Object parseObject(String source, ParsePosition pos) {
        return null;
      }
    });
  }
}
