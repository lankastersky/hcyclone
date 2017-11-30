package com.hcyclone.zyq;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model for exercises.
 */
final class ExerciseModel {

  private static final String TAG = ExerciseModel.class.getSimpleName();
  private static final Gson GSON = new Gson();
  private static final Type EXERCISE_TYPE = new TypeToken<List<Exercise>>() {}.getType();
  private static final String EXERCISES_ASSETS_FILENAME = "exercises.json";

  private final Map<String, Exercise> exercisesMap = new HashMap<>();

  ExerciseModel(Context context) {
    try {
      readFromFile(context);
    } catch (IOException e) {
      Log.e(TAG, "Failed to load exercises", e);
    }
  }

  Map<String, Exercise> getExercises() {
    return exercisesMap;
  }

  Map<String, Exercise> getExercises(Exercise.LevelType level) {
    Map<String, Exercise> filteredExercisesMap = new HashMap<>();
    for (Exercise exercise : exercisesMap.values()) {
      if (exercise.level == level) {
        filteredExercisesMap.put(exercise.getId(), exercise);
      }
    }
    return filteredExercisesMap;
  }

  Exercise getExercise(String id) {
    return exercisesMap.get(id);
  }

  private void readFromFile(Context context) throws IOException {
    AssetManager am = context.getAssets();
    InputStream is = am.open(EXERCISES_ASSETS_FILENAME);
    List<Exercise> exercises;
    try(JsonReader reader = new JsonReader(new InputStreamReader(is))) {
      exercises = GSON.fromJson(reader, EXERCISE_TYPE);
    }
    exercisesMap.clear();
    for (Exercise exercise : exercises) {
      exercisesMap.put(exercise.getId(), exercise);
    }
  }
}
