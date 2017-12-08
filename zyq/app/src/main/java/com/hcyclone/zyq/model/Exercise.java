package com.hcyclone.zyq.model;

import com.google.gson.annotations.SerializedName;

/**
 * Exercise plain object.
 */
public final class Exercise extends ExerciseGroup {

  public final String id;
  public final String description;
  public final String imageName;
  public final String tags;
  public final String detailsFileName;

  public Exercise(
      String id,
      String name,
      LevelType level,
      ExerciseType type,
      String description,
      String imageName,
      String tags,
      String detailsFileName) {
    super(name, level, type);
    this.id = id;
    this.description = description;
    this.imageName = imageName;
    this.tags = tags;
    this.detailsFileName = detailsFileName;
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
