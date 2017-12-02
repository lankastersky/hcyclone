package com.hcyclone.zyq.model;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hcyclone.zyq.model.Exercise.LevelType;
import com.hcyclone.zyq.model.Exercise.ExerciseType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model for exercises.
 */
public final class ExerciseModel {

  private static final String TAG = ExerciseModel.class.getSimpleName();
  private static final Gson GSON = new Gson();
  private static final Type EXERCISE_TYPE = new TypeToken<List<Exercise>>() {}.getType();
  private static final String EXERCISES_ASSETS_FILENAME = "exercises.json";
  private static final String DESCRIPTION_FILENAME_TEMPLATE = "%s_%s.html";

  private final Map<String, Exercise> exercisesMap;
  private final Table<LevelType, ExerciseType, String> descriptionCache = HashBasedTable.create();
  private final Context context;

  public ExerciseModel(Context context) {
    this.context = context;
    try {
      exercisesMap = readExercisesFromFile(EXERCISES_ASSETS_FILENAME, context);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read exercises from file", e);
    }
  }

  public Map<String, Exercise> getExercises() {
    return exercisesMap;
  }

  public Map<String, Exercise> getExercises(LevelType level, ExerciseType type) {
    Map<String, Exercise> filteredExercisesMap = new HashMap<>();
    for (Exercise exercise : exercisesMap.values()) {
      if (type == null || type == ExerciseType.UNKNOWN) {
        if (exercise.level == level) {
          filteredExercisesMap.put(exercise.getId(), exercise);
        }
      } else if (exercise.level == level && exercise.type == type) {
        filteredExercisesMap.put(exercise.getId(), exercise);
      }
    }
    return filteredExercisesMap;
  }

  public Exercise getExercise(String id) {
    return exercisesMap.get(id);
  }

  public String getDescription(LevelType level, ExerciseType type) {
    if (descriptionCache.contains(level, type)) {
      return descriptionCache.get(level, type);
    }
    String fileName = String.format(DESCRIPTION_FILENAME_TEMPLATE, level, type);
    try {
      String description = readFromFile(fileName, context);
      descriptionCache.put(level, type, description);
      return description;
    } catch (IOException e) {
      return "";
    }
  }

  public static List<ExerciseGroup> buildExerciseGroups(LevelType level) {
    List<ExerciseGroup> items = new ArrayList<>();
    items.add(
        new ExerciseGroup(
            ExerciseType.WARMUP.toString(),
            level,
            ExerciseType.WARMUP));
    items.add(
        new ExerciseGroup(
            ExerciseType.MEDITATION.toString(),
            level,
            ExerciseType.MEDITATION));
    items.add(
        new ExerciseGroup(
            ExerciseType.ADDITIONAL_MEDITATION.toString(),
            level,
            ExerciseType.ADDITIONAL_MEDITATION));
    items.add(
        new ExerciseGroup(
            ExerciseType.FINAL.toString(),
            level,
            ExerciseType.FINAL));
    items.add(
        new ExerciseGroup(
            ExerciseType.TREATMENT.toString(),
            level,
            ExerciseType.TREATMENT));
    return items;
  }

  private static Map<String, Exercise> readExercisesFromFile(String fileName, Context context)
      throws IOException {

    String fileContent = readFromFile(fileName, context);

    List<Exercise> exercises = GSON.fromJson(fileContent, EXERCISE_TYPE);
    Map<String, Exercise> exercisesMap = new HashMap<>();
    for (Exercise exercise : exercises) {
      exercisesMap.put(exercise.getId(), exercise);
    }
    return exercisesMap;
  }

  private static String readFromFile(String fileName, Context context) throws IOException {
    AssetManager am = context.getAssets();
    InputStream inputStream = am.open(fileName);
    return new String(ByteStreams.toByteArray(inputStream));
  }
}