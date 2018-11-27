package com.hcyclone.zen.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.hcyclone.zen.Analytics;
import com.hcyclone.zen.App;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.ChallengeModel;
import com.hcyclone.zen.statistics.BarPlotBuilder;
import com.hcyclone.zen.statistics.ChallengesValuesBuilder;
import com.hcyclone.zen.statistics.LinePlotBuilder;

/**
 * Shows challenges statistics.
 */
public class StatisticsFragment extends Fragment {

  enum ChartType {
    BAR,
    LINE;

    public static ChartType toEnum(String value) {
      try {
        return valueOf(value);
      } catch (Exception ex) {
        // For error cases
        return BAR;
      }
    }
  }

  public static final String TAG = StatisticsFragment.class.getCanonicalName();

  private static final String PREF_KEY_STATISTICS_CHART_TYPE = "statistics_chart_type";
  // See https://google.github.io/material-design-icons/
  private static final int INACTIVE_ICON_ALPHA = (int) (255 * 0.3);
  private ChartType chartType;
  private MenuItem barMenuItem;
  private MenuItem lineMenuItem;
  private XYPlot challengesLinePlot;
  private XYPlot challengesBarPlot;
  private XYPlot ratesLinePlot;
  private XYPlot ratesBarPlot;
  private SharedPreferences sharedPreferences;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    chartType = ChartType.toEnum(
        sharedPreferences.getString(PREF_KEY_STATISTICS_CHART_TYPE, ChartType.BAR.toString()));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    getActivity().setTitle(getString(R.string.fragment_statistics));

    setHasOptionsMenu(true);

    ChallengeModel challengeModel =
        ((App) getContext().getApplicationContext()).getChallengeModel();

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
    challengesValuesBuilder.build(challengeModel.getFinishedChallengesSorted(), getContext());

    // TODO: plots inflate too long and produce skipped frames. Find a way to fix this.
    challengesLinePlot = view.findViewById(R.id.line_plot_challenges);
    challengesBarPlot = view.findViewById(R.id.bar_plot_challenges);
    ratesLinePlot = view.findViewById(R.id.line_plot_rates);
    ratesBarPlot = view.findViewById(R.id.bar_plot_rates);

    if (challengesValuesBuilder.getValuesSize() == 0) {
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
    onOptionsItemSelected(chartType == ChartType.BAR ? barMenuItem : lineMenuItem);
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
        sharedPreferences
            .edit()
            .putString(PREF_KEY_STATISTICS_CHART_TYPE, ChartType.BAR.toString())
            .apply();
        Analytics.getInstance().sendStatisticsChart(ChartType.BAR.toString());
        return true;
      case R.id.action_line_chart:
        challengesBarPlot.setVisibility(View.GONE);
        ratesBarPlot.setVisibility(View.GONE);
        challengesLinePlot.setVisibility(View.VISIBLE);
        ratesLinePlot.setVisibility(View.VISIBLE);
        barMenuItem.getIcon().setAlpha(255);
        lineMenuItem.getIcon().setAlpha(INACTIVE_ICON_ALPHA);
        sharedPreferences
            .edit()
            .putString(PREF_KEY_STATISTICS_CHART_TYPE, ChartType.LINE.toString())
            .apply();
        Analytics.getInstance().sendStatisticsChart(ChartType.LINE.toString());
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
