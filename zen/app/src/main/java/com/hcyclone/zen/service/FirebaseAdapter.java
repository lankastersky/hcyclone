package com.hcyclone.zen.service;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hcyclone.zen.Log;
import com.hcyclone.zen.R;
import com.hcyclone.zen.Utils;
import com.hcyclone.zen.model.Challenge;

import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipInputStream;

/** This class is responsible for Firebase connection and handling Firebase database reference. */
public class FirebaseAdapter {

  private static final String TAG = FirebaseAdapter.class.getSimpleName();
  private static final FirebaseAdapter instance = new FirebaseAdapter();
  private static final int HALF_MEGABYTE = 1024 * 500;
  private static final String CHALLENGES_FILENAME_RU = "challenges.zip";
  private static final String CHALLENGES_FILENAME_EN = "challenges_en.zip";

  static {
    FirebaseDatabase firebaseConfig = FirebaseDatabase.getInstance();
    firebaseConfig.setPersistenceEnabled(true);
    firebaseConfig.setLogLevel(Logger.Level.WARN);
  }

  /** Reference to Firebase backend. */
  private FirebaseAuth firebaseAuth;
  private FirebaseAuthListener firebaseAuthListener;

  public static FirebaseAdapter getInstance() {
    return instance;
  }

  /**
   * Signs in to the server if firebase is enabled.
   */
  void signIn(FirebaseAuthListener listener, Context context) {
    Log.d(TAG, "Sign in");
    firebaseAuthListener = listener;
    FirebaseAuth.AuthStateListener authStateListener = (firebaseAuth) -> {
        FirebaseAdapter.this.firebaseAuth = firebaseAuth;
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          Log.d(TAG, "User is signed in with uid: " + user.getUid());
        } else {
          FirebaseCrashlytics.getInstance().log("E/" + TAG + ": No user is signed in.");
        }
    };
    FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    authenticate(context);
  }

  boolean isSignedIn() {
    return firebaseAuth != null && firebaseAuth.getCurrentUser() != null;
  }

  private void authenticate(final Context context) {
    String firebaseUsername = context.getString(R.string.firebase_login);
    String firebasePassword = context.getString(R.string.firebase_password);
    Log.d(TAG, "signInWithEmailAndPassword");
    FirebaseAuth auth = FirebaseAuth.getInstance();
    if (auth == null) {
      Log.w(TAG, "Failed to get auth instance");
      return;
    }
    auth.signInWithEmailAndPassword(firebaseUsername, firebasePassword)
        .addOnCompleteListener(task -> {
          Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
          if (firebaseAuthListener != null) {
            if (task.isSuccessful()) {
              firebaseAuthListener.onAuthSuccess();
            } else if (!Utils.isOnline(context)) {
              // Use cached data.
              Log.w(TAG, "Offline, try use cached data");
              firebaseAuthListener.onAuthSuccess();
            } else {
              // Auth error.
              firebaseAuthListener.onAuthError(task.getException());
            }
          }
        });
  }

  void getChallenges(ChallengesDownloadListener listener) {
    Log.d(TAG, "getChallenges");
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    reference.child("challenges").addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            try {
              listener.onChallengesDownloaded(parseChallenges((Map<String, Object>) dataSnapshot.getValue()));
            } catch (Exception e) {
              Log.e(TAG, "Failed to parse challenges", e);
              FirebaseCrashlytics.getInstance().recordException(e);
              listener.onError(e);
            }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "getChallenges:onCancelled", databaseError.toException());
            listener.onError(databaseError.toException());
          }
        });
    reference.child("challenges").keepSynced(true);
  }

  void downloadChallenges(
      String challengesLocale, ChallengesDownloadListener listener, Context context) {

    Log.d(TAG, "downloadChallenges");
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    String challengesFilename = getChallengesFilename(challengesLocale);
    StorageReference pathReference = storageReference.child(challengesFilename);
    pathReference
        .getBytes(HALF_MEGABYTE)
        .addOnSuccessListener(
            bytes -> {
              try {
                String stringChallenges = unzip(bytes);
                JSONObject jsonChallenges = new JSONObject(stringChallenges);
                listener.onChallengesDownloaded(parseChallenges(jsonChallenges));
              } catch (Exception e) {
                Log.e(TAG, "Failed to parse challenges", e);
                FirebaseCrashlytics.getInstance().recordException(e);
                listener.onError(e);
              }
            })
        .addOnFailureListener(
            e -> {
              Log.w(TAG, "downloadChallenges:onFailure", e);
              if (!Utils.isOnline(context)) {
                Log.w(TAG, "Device is offline, don't load challenges");
                listener.onError(new Exception("Device is offline"));
              } else {
                getChallenges(listener);
              }
            });
  }

  /** The server has different challenges' file resources for different locales. */
  private static String getChallengesFilename(String challengesLocale) {
    if (challengesLocale.equalsIgnoreCase("ru")) {
      return CHALLENGES_FILENAME_RU;
    }
    return CHALLENGES_FILENAME_EN;
  }

  private static String unzip(byte[] bytes) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(
        new BufferedInputStream(
            new ByteArrayInputStream(bytes)))) {
      int count;
      byte[] buffer = new byte[HALF_MEGABYTE];
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      if ((zis.getNextEntry()) != null) {
        try {
          while ((count = zis.read(buffer)) != -1)
            outputStream.write(buffer, 0, count);
        } finally {
          outputStream.close();
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
      } else {
        throw new IOException("Bad zip format");
      }
    }
  }

  private ArrayList<Challenge> parseChallenges(Map<String, Object> challengesMap) {
    ArrayList<Challenge> challenges = new ArrayList<>();
    for (String key : challengesMap.keySet()) {
      Map<String, Object> challengeMap = (Map<String, Object>) challengesMap.get(key);
      challenges.add(parseChallenge(key, challengeMap));
    }
    return challenges;
  }

  private Challenge parseChallenge(String key, Map<String, Object> challengeMap) {
    return new Challenge(key,
        (String) challengeMap.get("content"),
        (String) challengeMap.get("details"),
        (String) challengeMap.get("type"),
        (Long) challengeMap.get("level"),
        (String) challengeMap.get("source"),
        (String) challengeMap.get("url"),
        (String) challengeMap.get("quote"));
  }

  private ArrayList<Challenge> parseChallenges(JSONObject challengesMap) throws JSONException {
    ArrayList<Challenge> challenges = new ArrayList<>();

    Iterator<String> keysItr = challengesMap.keys();
    while (keysItr.hasNext()) {
      String key = keysItr.next();
      JSONObject challengeMap = challengesMap.getJSONObject(key);
      challenges.add(parseChallenge(key, challengeMap));
    }
    return challenges;
  }

  private Challenge parseChallenge(String key, JSONObject challengeMap) throws JSONException {
    return new Challenge(key,
        challengeMap.getString("content"),
        challengeMap.getString("details"),
        challengeMap.getString("type"),
        challengeMap.getLong("level"),
        challengeMap.getString("source"),
        challengeMap.getString("url"),
        challengeMap.getString("quote"));
  }

  /** Firebase authentication process listener. */
  public interface FirebaseAuthListener {
    /** Calls if auth was successful. */
    void onAuthSuccess();

    /** Calls if auth was unsuccessful. */
    void onAuthError(Exception exception);
  }

}
