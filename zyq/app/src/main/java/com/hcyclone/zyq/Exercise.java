package com.hcyclone.zyq;

import com.google.gson.annotations.SerializedName;

/**
 * Exercise plain object.
 */
final class Exercise {

  final String name;
  final ExerciseType type;
  final LevelType level;
  final String description;
  final String detailedDescription;
  final String imageName;

  Exercise(
      String name,
      ExerciseType type,
      LevelType level,
      String description,
      String detailedDescription,
      //@IntegerRes int imageViewId
      String imageName
  ) {

    this.name = name;
//    this.type = ExerciseType.values()[type];
//    this.level = LevelType.values()[level];
    this.type = type;
    this.level = level;
    this.description = description;
    this.detailedDescription = detailedDescription;
    //this.imageViewId = imageViewId;
    this.imageName = imageName;
  }

  String getId() {
    return String.format("%s_%s_%s", name, level, type);
  }

  enum ExerciseType {
    @SerializedName("0")
    UNKNOWN(0),
    @SerializedName("1")
    WARMUP(1),
    @SerializedName("2")
    MEDITATION(2);

    private final int value;

    public int getValue() {
      return value;
    }

    ExerciseType(int value) {
      this.value = value;
    }
  }

  enum LevelType {
    @SerializedName("0")
    UNKNOWN(0),
    @SerializedName("1")
    LEVEL1(1),
    @SerializedName("2")
    LEVEL2(2),
    @SerializedName("3")
    LEVEL3(3);

    private final int value;

    public int getValue() {
      return value;
    }

    LevelType(int value) {
      this.value = value;
    }
  }
}
