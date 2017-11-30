package com.hcyclone.zyq.model;

import com.google.gson.annotations.SerializedName;

/**
 * Exercise plain object.
 */
public final class Exercise {

  public final String name;
  public final ExerciseType type;
  public final LevelType level;
  public final String description;
  public final String detailedDescription;
  public final String imageName;

  public Exercise(
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

  public String getId() {
    return String.format("%s_%s_%s", name, level, type);
  }

  public enum ExerciseType {
    @SerializedName("0")
    UNKNOWN(0),

    @SerializedName("1")
    WARMUP(1),

    @SerializedName("2")
    MEDITATION(2),

    @SerializedName("3")
    ADDITIONAL_MEDITATION(3),

    @SerializedName("4")
    FINAL(4),

    @SerializedName("5")
    TREATMENT(5);

    private final int value;

    public int getValue() {
      return value;
    }

    ExerciseType(int value) {
      this.value = value;
    }
  }

  public enum LevelType {
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
