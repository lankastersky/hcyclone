package com.hcyclone.zen.statistics;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/** Test cases for {@link PlotBuilder}. */
public class PlotBuilderTest {

  @Before
  public void setUp() {
  }

  @Test
  public void roundUpUpperBoundary() {
    assertThat(PlotBuilder.roundUpToMultiplierOf10(-1)).isEqualTo(0);
    assertThat(PlotBuilder.roundUpToMultiplierOf10(0)).isEqualTo(0);
    assertThat(PlotBuilder.roundUpToMultiplierOf10(0.9)).isEqualTo(10);
    assertThat(PlotBuilder.roundUpToMultiplierOf10(1)).isEqualTo(10);
    assertThat(PlotBuilder.roundUpToMultiplierOf10(1.1)).isEqualTo(10);
    assertThat(PlotBuilder.roundUpToMultiplierOf10(1.9)).isEqualTo(10);
    assertThat(PlotBuilder.roundUpToMultiplierOf10(12_345)).isEqualTo(12_350);
  }
}
