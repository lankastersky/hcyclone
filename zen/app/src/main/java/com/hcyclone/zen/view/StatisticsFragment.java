package com.hcyclone.zen.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.statistics.BarPlotBuilder;
import com.hcyclone.zen.statistics.ChallengesValuesBuilder;
import com.hcyclone.zen.statistics.LinePlotBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Shows challenges statistics.
 */
public class StatisticsFragment extends Fragment {

  private static final int CHART_TYPE_BAR = 0;
  private static final int CHART_TYPE_LINE = 1;
  private static final String PREF_KEY_STATISTICS_CHART_TYPE = "statistics_chart_type";
  // See https://google.github.io/material-design-icons/
  private static final int INACTIVE_ICON_ALPHA = (int) (255 * 0.3);
  private static final Calendar CALENDAR = Calendar.getInstance();
  @ChartType
  private int chartType;
  private MenuItem barMenuItem;
  private MenuItem lineMenuItem;
  private XYPlot challengesLinePlot;
  private XYPlot challengesBarPlot;
  private XYPlot ratesLinePlot;
  private XYPlot ratesBarPlot;
  private SharedPreferences sharedPreferences;

  private static List<Challenge> getPlotChallenges(Context context) {
    ChallengeModel challengeModel = ChallengeModel.getInstance();
    List<Challenge> finishedChallenges;
    if (!Utils.isDebug()) {
      finishedChallenges = challengeModel.getFinishedChallengesSorted();
    } else {
      finishedChallenges = new ArrayList<>();
//      int days = 1;
//      int days = 2;
//      int days = 13;
//      int days = 14;
//      int days = 14 * 7 - 1;
      int days = 14 * 7;
//      int days = 14 * 30 - 1;
//      int days = 14 * 30;
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
        //challenge.setRating(i % 5);
        CALENDAR.setTime(new Date());
        CALENDAR.add(Calendar.DAY_OF_MONTH, (int) (Math.random() * days));
        //CALENDAR.add(Calendar.DAY_OF_YEAR, i + 1);
        challenge.setFinishedTime(CALENDAR.getTimeInMillis());
        finishedChallenges.add(challenge);
      }
    }
    return finishedChallenges;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    chartType = sharedPreferences.getInt(PREF_KEY_STATISTICS_CHART_TYPE, CHART_TYPE_BAR);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_statistics));

    setHasOptionsMenu(true);

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


    ChallengesValuesBuilder challengesValuesBuilder = new ChallengesValuesBuilder();
    challengesValuesBuilder.build(getPlotChallenges(getContext()), getContext());

    // TODO: plots inflate too long and produce skipped frames. Find a way to fix this.
    challengesLinePlot = view.findViewById(R.id.line_plot_challenges);
    challengesBarPlot = view.findViewById(R.id.bar_plot_challenges);
    ratesLinePlot = view.findViewById(R.id.line_plot_rates);
    ratesBarPlot = view.findViewById(R.id.bar_plot_rates);

    if (challengesValuesBuilder.getValuesSize() < 2) {
      // Nothing to draw.
      return view;
    }

    LinePlotBuilder linePlotBuilder = new LinePlotBuilder(challengesValuesBuilder, getContext());
    linePlotBuilder.buildChallengesPlot(challengesLinePlot);
    linePlotBuilder.buildRatesPlot(ratesLinePlot);

    BarPlotBuilder barPlotBuilder = new BarPlotBuilder(challengesValuesBuilder, getContext());
    barPlotBuilder.buildChallengesPlot(challengesBarPlot);
    barPlotBuilder.buildRatesPlot(ratesBarPlot);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.statistics_menu, menu);
    barMenuItem = menu.findItem(R.id.action_bar_chart);
    lineMenuItem = menu.findItem(R.id.action_line_chart);
    onOptionsItemSelected(chartType == CHART_TYPE_BAR ? barMenuItem : lineMenuItem);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    switch (id) {
      case R.id.action_bar_chart:
        challengesLinePlot.setVisibility(View.GONE);
        ratesLinePlot.setVisibility(View.GONE);
        challengesBarPlot.setVisibility(View.VISIBLE);
        ratesBarPlot.setVisibility(View.VISIBLE);
        lineMenuItem.getIcon().setAlpha(255);
        barMenuItem.getIcon().setAlpha(INACTIVE_ICON_ALPHA);
        sharedPreferences.edit().putInt(PREF_KEY_STATISTICS_CHART_TYPE, CHART_TYPE_BAR).apply();
        return true;
      case R.id.action_line_chart:
        challengesBarPlot.setVisibility(View.GONE);
        ratesBarPlot.setVisibility(View.GONE);
        challengesLinePlot.setVisibility(View.VISIBLE);
        ratesLinePlot.setVisibility(View.VISIBLE);
        barMenuItem.getIcon().setAlpha(255);
        lineMenuItem.getIcon().setAlpha(INACTIVE_ICON_ALPHA);
        sharedPreferences.edit().putInt(PREF_KEY_STATISTICS_CHART_TYPE, CHART_TYPE_LINE).apply();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @IntDef({CHART_TYPE_BAR, CHART_TYPE_LINE})
  public @interface ChartType {
  }
}
