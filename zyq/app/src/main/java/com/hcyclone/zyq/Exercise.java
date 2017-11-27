package com.hcyclone.zyq;

import android.support.annotation.IntegerRes;

/**
 * Exercise plain object.
 */
final class Exercise {

  final String name;
  final ExerciseType type;
  final LevelType level;
  final String description;
  final @IntegerRes int imageViewId;

  Exercise(String name) {

    this.name = name;
    this.type = ExerciseType.WARMUP;
    this.level = LevelType.LEVEL1;
    this.description = "test description";
    this.imageViewId = R.mipmap.step00warmup00;
  }

  Exercise(
      String name,
      ExerciseType type,
      LevelType level,
      String description,
      @IntegerRes int imageViewId) {

    this.name = name;
    this.type = type;
    this.level = level;
    this.description = description;
    this.imageViewId = imageViewId;
  }

  enum ExerciseType {
    WARMUP,
    MEDITATION,
  }

  enum LevelType {
    LEVEL1,
    LEVEL2,
    LEVEL3,
  }
}
