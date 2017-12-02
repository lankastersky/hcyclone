package com.hcyclone.zyq.model;

/**
 * Exercises group item.
 */
public class ExerciseGroup {

  public final String name;
  public final Exercise.LevelType level;
  public final Exercise.ExerciseType type;

  public ExerciseGroup(String name, Exercise.LevelType level, Exercise.ExerciseType type) {
    this.name = name;
    this.level = level;
    this.type = type;
  }
}
