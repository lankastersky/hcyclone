package com.hcyclone.zen.statistics;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.List;

/**
 * Formats domain values for statistics charts.
 */
class DomainFormat extends Format {

  private final List<String> domainValues;

  DomainFormat(List<String> domainValues) {
    this.domainValues = domainValues;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    int i = Math.round(((Number) obj).floatValue());
    String label = "";
    if (i != -1) {
      label = domainValues.get(i);
    }
    return toAppendTo.append(label);
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    return null;
  }
}
