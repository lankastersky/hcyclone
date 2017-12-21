package com.hcyclone.zen.view;

import android.content.Context;
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
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;

import java.text.DateFormat;
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

  private static final int CHALLENGES_REDUCE_BY_DATE_SIZE = 14;

  private static final DateFormat CHALLENGES_TIME_BY_DAYS_DATE_FORMAT =
      SimpleDateFormat.getDateInstance(DateFormat.SHORT);
  private static final DateFormat CHALLENGES_TIME_BY_WEEKS_DATE_FORMAT =
      new SimpleDateFormat("w yy");
  private static final DateFormat CHALLENGES_TIME_BY_MONTHS_DATE_FORMAT =
      new SimpleDateFormat("MMM yy");
  private static final DateFormat CHALLENGES_TIME_BY_YEARS_DATE_FORMAT =
      new SimpleDateFormat("yyyy");

  private static final DateFormat DOMAIN_BY_DAYS_DATE_FORMAT =
      CHALLENGES_TIME_BY_DAYS_DATE_FORMAT;
  private static final DateFormat DOMAIN_BY_WEEKS_DATE_FORMAT =
      CHALLENGES_TIME_BY_DAYS_DATE_FORMAT;
  private static final DateFormat DOMAIN_BY_MONTHS_DATE_FORMAT =
      CHALLENGES_TIME_BY_MONTHS_DATE_FORMAT;
  private static final DateFormat DOMAIN_BY_YEARS_DATE_FORMAT =
      CHALLENGES_TIME_BY_YEARS_DATE_FORMAT;

  // Used to get the gap above plot line.
  private static final double PLOT_UPPER_BOUNDARY_MULTIPLIER = 1.1;
  private static final Calendar CALENDAR = Calendar.getInstance();

  private static void setAxes(
      XYPlot plot,
      final List<String> domainValues,
      double rangeUpperBoundary) {

    //if (domainValues.size() > 4) {
    // draw n number of evenly spaced lines
    plot.setDomainStep(StepMode.SUBDIVIDE, domainValues.size());

    plot.setRangeUpperBoundary(
        rangeUpperBoundary * PLOT_UPPER_BOUNDARY_MULTIPLIER, BoundaryMode.FIXED);
    // domainValues.size() <= CHALLENGES_REDUCE_BY_DATE_SIZE, so this step is small enough.
//    plot.setRangeStep(StepMode.INCREMENT_BY_VAL, rangeUpperBoundary / domainValues.size());
    plot.setRangeStep(StepMode.SUBDIVIDE, domainValues.size());
    //}

    plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
      @Override
      public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        int i = Math.round(((Number) obj).floatValue());
        String dateString = domainValues.get(i);
        return toAppendTo.append(dateString);
      }

      @Override
      public Object parseObject(String source, ParsePosition pos) {
        return null;
      }
    });
  }

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


    ChallengesValues challengesValues = new ChallengesValues();
    challengesValues.generate(getPlotChallenges(getContext()), getContext());

    if (challengesValues.challengesDates.size() < 2) {
      // Nothing to draw.
      return view;
    } else if (challengesValues.challengesDates.size() < 4) {
      challengesValues.addFakeValues(4 - challengesValues.challengesDates.size());
    }

    plotChallenges((XYPlot) view.findViewById(R.id.plot_challenges), challengesValues);
    plotConsciousness((XYPlot) view.findViewById(R.id.plot_cosciousness), challengesValues);

    return view;
  }

  List<Challenge> getPlotChallenges(Context context) {
    ChallengeModel challengeModel = ChallengeModel.getInstance();
    List<Challenge> finishedChallenges = challengeModel.getFinishedChallengesSorted();
    if (!Utils.isDebug()) {
      int days = 70;
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
        challenge.setRating((float) Math.random() * challengeModel.getMaxRating(context));
        CALENDAR.setTime(new Date());
//      CALENDAR.add(Calendar.DAY_OF_MONTH, (int) (Math.random() * days));
        CALENDAR.add(Calendar.DAY_OF_YEAR, i + 1);
        challenge.setFinishedTime(CALENDAR.getTimeInMillis());
        finishedChallenges.add(challenge);
      }
      //finishedChallenges = new ArrayList<>();
    }
    return finishedChallenges;
  }

  private void decorate(XYPlot plot) {
    plot.getGraph().setMarginLeft(getResources().getInteger(R.integer.statistics_plot_margin_left));
    plot.getGraph().setMarginBottom(
        getResources().getInteger(R.integer.statistics_plot_margin_bottom));
  }

  private void setSeries(XYPlot plot, List<? extends Number> values) {
    // create formatters to use for drawing a series using LineAndPointRenderer
    // and configure them from xml:
    LineAndPointFormatter seriesFormat =
        new LineAndPointFormatter(getContext(), R.xml.line_point_formatter_with_labels);

    float hOffset = 16;
    if (values.get(0) instanceof Float) {
      hOffset *= 3;
    }
    seriesFormat.getPointLabelFormatter().hOffset = hOffset;

//    List<Number> normalizedValues = new ArrayList<>(values);
//    if (normalizedValues.size() < 4) {
//      for (int i = normalizedValues.size(); i < 4; i++) {
//        normalizedValues.add(0);
//      }
//    }
    // turn the above arrays into XYSeries':
    // (Y_VALS_ONLY means use the element index as the x value)
    XYSeries series = new SimpleXYSeries(
        values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series");

    if (values.size() >= 3) {
      // Add some smoothing to the lines:
      // see: http://androidplot.com/smooth-curves-and-androidplot/
      seriesFormat.setInterpolationParams(
          new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Uniform));
    }

    plot.addSeries(series, seriesFormat);
  }

  /**
   * See examples:
   * https://github.com/halfhp/androidplot/blob/master/docs/quickstart.md
   * https://github.com/halfhp/androidplot/blob/master/demoapp/src/main/java/com/androidplot/demos/TimeSeriesActivity.java
   *
   * @param plot
   */
  private void plotChallenges(XYPlot plot, ChallengesValues challengesValues) {
    decorate(plot);
    setSeries(plot, new ArrayList<>(challengesValues.challengeTimeToNumber.values()));
    setAxes(
        plot,
        challengesValues.challengesDates,
        challengesValues.maxChallengeNumber);
  }

  private void plotConsciousness(XYPlot plot, final ChallengesValues challengesValues) {
    decorate(plot);
    setSeries(plot, new ArrayList<>(challengesValues.challengeTimeToRates.values()));
    setAxes(
        plot,
        challengesValues.challengesDates,
        challengesValues.maxChallengeRate);
  }

  private static class ChallengesValues {

    DateFormat mergeDateFormat = CHALLENGES_TIME_BY_YEARS_DATE_FORMAT;
    DateFormat domainDateFormat = DOMAIN_BY_YEARS_DATE_FORMAT;
    MergeDatesType mergeDatesType = MergeDatesType.YEARS;
    Map<String, Integer> challengeTimeToNumber;
    Map<String, Float> challengeTimeToRates;
    List<String> challengesDates;
    int maxChallengeNumber;
    float maxChallengeRate;

    private void initReduceByDateType(List<Challenge> challenges) {
      if (challenges.size() < CHALLENGES_REDUCE_BY_DATE_SIZE) {
        mergeDatesType = MergeDatesType.DAYS;
        mergeDateFormat = CHALLENGES_TIME_BY_DAYS_DATE_FORMAT;
        domainDateFormat = DOMAIN_BY_DAYS_DATE_FORMAT;
      } else if (challenges.size() < CHALLENGES_REDUCE_BY_DATE_SIZE * 7) {
        mergeDatesType = MergeDatesType.WEEKS;
        mergeDateFormat = CHALLENGES_TIME_BY_WEEKS_DATE_FORMAT;
        domainDateFormat = DOMAIN_BY_WEEKS_DATE_FORMAT;
      } else if (challenges.size() < CHALLENGES_REDUCE_BY_DATE_SIZE * 30) {
        mergeDatesType = MergeDatesType.MONTHS;
        mergeDateFormat = CHALLENGES_TIME_BY_MONTHS_DATE_FORMAT;
        domainDateFormat = DOMAIN_BY_MONTHS_DATE_FORMAT;
      }
    }

    void generate(List<Challenge> plotChallenges, Context context) {
      initReduceByDateType(plotChallenges);

      challengeTimeToNumber = new LinkedHashMap<>();
      challengeTimeToRates = new LinkedHashMap<>();
      challengesDates = new ArrayList<>();
      for (Challenge challenge : plotChallenges) {
        long finishedTime = challenge.getFinishedTime();
        Date date = new Date(finishedTime);
        String dateString = mergeDateFormat.format(date);
        if (challengeTimeToNumber.containsKey(dateString)) {
          int challengesNumber = challengeTimeToNumber.get(dateString);
          challengeTimeToNumber.put(dateString, ++challengesNumber);
          float challengesRate = challengeTimeToRates.get(dateString);
          challengeTimeToRates.put(dateString, challengesRate + challenge.getRating());
        } else {
          challengeTimeToNumber.put(dateString, 1);
          challengesDates.add(domainDateFormat.format(date));
          challengeTimeToRates.put(dateString, challenge.getRating());
        }
      }

      // Normalize rates.
      for (String dateString : challengeTimeToRates.keySet()) {
        int challengesNumber = challengeTimeToNumber.get(dateString);
        float challengesRate = challengeTimeToRates.get(dateString);
        float normalizedRate =
            challengesRate * 100 // in %
                / challengesNumber
                / ChallengeModel.getInstance().getMaxRating(context);
        normalizedRate = (float) Math.round(normalizedRate * 10) / 10; // round up to 1 digit.
        challengeTimeToRates.put(dateString, normalizedRate);
      }

      maxChallengeNumber = 0;
      for (Integer challengeNumber : challengeTimeToNumber.values()) {
        if (maxChallengeNumber < challengeNumber) {
          maxChallengeNumber = challengeNumber;
        }
      }

      maxChallengeRate = 0;
      for (Float rate : challengeTimeToRates.values()) {
        if (maxChallengeRate < rate) {
          maxChallengeRate = rate;
        }
      }
    }

    void addFakeValues(int number) {
      //String lastDateString = challengesDates.get(challengesDates.size() - 1);
      Date lastDate = new Date();
      for (int i = 0; i <= number; i++) {
        CALENDAR.setTime(lastDate);
//        switch (mergeDatesType) {
//          case DAYS:
        CALENDAR.add(Calendar.DAY_OF_YEAR, i + 1);
//            break;
//        }
        Date date = CALENDAR.getTime();
        String dateString = mergeDateFormat.format(date);

        challengesDates.add(domainDateFormat.format(date));
        challengeTimeToNumber.put(dateString, 0);
        challengeTimeToRates.put(dateString, 0f);
      }
    }

    enum MergeDatesType {
      DAYS,
      WEEKS,
      MONTHS,
      YEARS
    }
  }
}
