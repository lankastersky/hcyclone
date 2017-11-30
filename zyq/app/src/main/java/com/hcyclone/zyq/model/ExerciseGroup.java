package com.hcyclone.zyq.model;

/**
 * Exercises group item.
 */
public final class ExerciseGroup {

  public final String name;
  public final Exercise.ExerciseType type;

  public ExerciseGroup(String name, Exercise.ExerciseType type) {
    this.name = name;
    this.type = type;
  }
}
