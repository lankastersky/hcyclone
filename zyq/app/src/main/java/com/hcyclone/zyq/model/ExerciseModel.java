package com.hcyclone.zyq.model;

import android.content.Context;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hcyclone.zyq.Log;
import com.hcyclone.zyq.R;
import com.hcyclone.zyq.Utils;
import com.hcyclone.zyq.model.Exercise.LevelType;
import com.hcyclone.zyq.model.Exercise.ExerciseType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Model for exercises.
 */
public final class ExerciseModel {

  private static final String TAG = ExerciseModel.class.getSimpleName();
  private static final Gson GSON = new Gson();
  private static final Type EXERCISE_TYPE = new TypeToken<List<Exercise>>() {}.getType();
  private static final String DESCRIPTION_ID_DELIMITER = "_";
  private static final String DESCRIPTION_FILE_TEMPLATE = "%s_html";
  private static final String DESCRIPTION_LOCALIZED_FILE_TEMPLATE = "%s_%s_html";

  private final Map<String, Exercise> exercisesMap;
  private final String language;

  public ExerciseModel(Context context) {
    ImmutableMap.Builder<String, Exercise> exerciseBuilder = ImmutableMap.builder();
    language = Utils.getCurrentLocale(context).getLanguage();

    try {
      if (new Locale("ru").getLanguage().equals(language)) {
        exerciseBuilder.putAll(readExercisesFromResources(R.raw.exercises1_json, context));
        exerciseBuilder.putAll(readExercisesFromResources(R.raw.exercises2_json, context));
        exerciseBuilder.putAll(readExercisesFromResources(R.raw.exercises3_json, context));
      } else {
        exerciseBuilder.putAll(readExercisesFromResources(R.raw.exercises1_en_json, context));
        exerciseBuilder.putAll(readExercisesFromResources(R.raw.exercises2_en_json, context));
        exerciseBuilder.putAll(readExercisesFromResources(R.raw.exercises3_en_json, context));
      }
      exercisesMap = exerciseBuilder.build();
    } catch (IOException e) {
      Crashlytics.logException(e);
      throw new IllegalStateException("Failed to read exercises", e);
    }
  }

  public Map<String, Exercise> getExercises() {
    return exercisesMap;
  }

  public Map<String, Exercise> getExercises(LevelType level, ExerciseType type) {
    Map<String, Exercise> filteredExercisesMap = new LinkedHashMap<>();
    for (Exercise exercise : exercisesMap.values()) {
      if (type == null || type == ExerciseType.UNKNOWN) {
        if (exercise.level.getValue() == level.getValue()) {
          filteredExercisesMap.put(exercise.getId(), exercise);
        }
      } else if (exercise.level.getValue() == level.getValue() && exercise.type == type) {
        filteredExercisesMap.put(exercise.getId(), exercise);
      }
    }
    return filteredExercisesMap;
  }

  public Exercise getExercise(String id) {
    return exercisesMap.get(id);
  }

  public String getPracticeDescription(LevelType level, Context context) {
    return getPracticeDescription(
        getDescriptionId(level.getValue(), null, null), context);
  }

  public String getPracticeDescription(LevelType level, ExerciseType type, Context context) {
    return getPracticeDescription(
        getDescriptionId(level.getValue(), type.getValue(), null), context);
  }

  public String getPracticeDescription(Exercise exercise, Context context) {
    if (!TextUtils.isEmpty(exercise.detailsFileName)) {
      return getPracticeDescription(exercise.detailsFileName, context);
    }
    return getPracticeDescription(
        getDescriptionId(
            exercise.level.getValue(), exercise.type.getValue(), exercise.id), context);
  }

  public List<ExerciseGroup> buildExerciseGroups(LevelType level, Context context) {
    List<ExerciseGroup> items = new ArrayList<>();
    items.add(
        new ExerciseGroup(
            exerciseTypeToString(ExerciseType.WARMUP, context),
            level,
            ExerciseType.WARMUP));
    items.add(
        new ExerciseGroup(
            exerciseTypeToString(ExerciseType.MEDITATION, context),
            level,
            ExerciseType.MEDITATION));
    items.add(
        new ExerciseGroup(
            exerciseTypeToString(ExerciseType.ADDITIONAL_MEDITATION, context),
            level,
            ExerciseType.ADDITIONAL_MEDITATION));
    Map<String, Exercise> exercisesMap = getExercises(level, ExerciseType.FINAL);
    if (!exercisesMap.isEmpty()) {
      items.add(
          new ExerciseGroup(
              exerciseTypeToString(ExerciseType.FINAL, context),
              level,
              ExerciseType.FINAL));
    }
    items.add(
        new ExerciseGroup(
            exerciseTypeToString(ExerciseType.TREATMENT, context),
            level,
            ExerciseType.TREATMENT));
    return items;
  }

  public static String exerciseTypeToString(ExerciseType type, Context context) {
    switch (type) {
      case WARMUP:
        return context.getString(R.string.warmup);
      case MEDITATION:
        return context.getString(R.string.type_meditation);
      case ADDITIONAL_MEDITATION:
        return context.getString(R.string.type_additional_meditation);
      case FINAL:
        return context.getString(R.string.type_final);
      case TREATMENT:
        return context.getString(R.string.type_treatment);
      default:
        throw new AssertionError("No such exercise type: " + type);
    }
  }

  private String getPracticeDescription(String id, Context context) {
    String fileName = String.format(DESCRIPTION_FILE_TEMPLATE, id);
    String localizedFilename = String.format(DESCRIPTION_LOCALIZED_FILE_TEMPLATE, id, language);
    int resId = context.getResources().getIdentifier(
            localizedFilename, "raw", context.getPackageName());
    if (resId == 0) {
      resId = context.getResources().getIdentifier(
              fileName, "raw", context.getPackageName());
    }
    if (resId == 0) {
      return "";
    }
    try {
      return Utils.readFromRawResources(resId, context);
    } catch (IOException e) {
      Crashlytics.logException(e);
      Log.w(TAG, "Failed to read description: " + fileName, e);
      return "";
    }
  }

  private String getDescriptionId(Integer level, Integer type, String name) {
    StringBuilder descriptionIdBuilder = new StringBuilder();
    descriptionIdBuilder.append("ex_");
    descriptionIdBuilder.append(level);
    if (type != null) {
      descriptionIdBuilder.append(DESCRIPTION_ID_DELIMITER);
      descriptionIdBuilder.append(type);
    }
    if (!TextUtils.isEmpty(name)) {
      descriptionIdBuilder.append(DESCRIPTION_ID_DELIMITER);
      descriptionIdBuilder.append(name.toLowerCase(Locale.US));
    }
    return descriptionIdBuilder.toString();
  }

  private static Map<String, Exercise> readExercisesFromResources(int resId, Context context)
      throws IOException {

    String fileContent = Utils.readFromRawResources(resId, context);

    List<Exercise> exercises = GSON.fromJson(fileContent, EXERCISE_TYPE);
    Map<String, Exercise> exercisesMap = new LinkedHashMap<>();
    for (Exercise exercise : exercises) {
      exercisesMap.put(exercise.getId(), exercise);
    }
    return exercisesMap;
  }
}