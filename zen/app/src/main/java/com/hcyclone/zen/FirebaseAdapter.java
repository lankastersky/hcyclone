package com.hcyclone.zen;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for Firebase connection and handling Firebase database reference.
 */
public class FirebaseAdapter {

  private static final String TAG = FirebaseAdapter.class.getSimpleName();
  private static final FirebaseAdapter instance = new FirebaseAdapter();

  static {
    FirebaseDatabase firebaseConfig = FirebaseDatabase.getInstance();
    firebaseConfig.setPersistenceEnabled(true);
    firebaseConfig.setLogLevel(Logger.Level.WARN);
  }

  /**
   * Reference to Firebase backend.
   */
  private FirebaseAuth firebaseAuth;
  private FirebaseAuthListener firebaseAuthListener;
  private FirebaseAuth.AuthStateListener authStateListener;

  public static FirebaseAdapter getInstance() {
    return instance;
  }

  /**
   * Signs in to the server if firebase is enabled.
   */
  public void signIn(FirebaseAuthListener listener) {
    Log.d(TAG, "Sign in");
    firebaseAuthListener = listener;
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
        FirebaseAdapter.this.firebaseAuth = firebaseAuth;
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          Log.d(TAG, "User is signed in with uid: " + user.getUid());
        } else {
          Log.e(TAG, "No user is signed in.");
        }
      }
    };
    FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    authenticate();
  }

  public boolean isSignedIn() {
    return firebaseAuth != null && firebaseAuth.getCurrentUser() != null;
  }

  private void authenticate() {
    String firebaseUsername = "stalker@hcyclone.com";
    String firebasePassword = "stalking";
    Log.d(TAG, "signInWithEmailAndPassword");
    FirebaseAuth auth = FirebaseAuth.getInstance();
    auth.signInWithEmailAndPassword(firebaseUsername, firebasePassword)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
            if (firebaseAuthListener != null) {
              if (task.isSuccessful()) {
                firebaseAuthListener.onAuthSuccess();
              } else if (!Utils.getInstance().isConnected()) {
                // Use cached data.
                Log.d(TAG, "Offline, try use cached data");
                firebaseAuthListener.onAuthSuccess();
              } else {
                // Auth error.
                firebaseAuthListener.onAuthError(task.getException());
              }
            }
          }
        });
  }

  public void getChallenges(final FirebaseDataListener listener) {
    Log.d(TAG, "getChallenges");
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    reference.child("challenges").addListenerForSingleValueEvent(
        new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<Challenge> challenges = new ArrayList<>();
            try {
              Map<String, Object> challengesMap = (Map<String, Object>) dataSnapshot.getValue();
              for (String key : challengesMap.keySet()) {
                Map<String, Object> challengeMap = (Map<String, Object>) challengesMap.get(key);
                Challenge challenge = new Challenge(key,
                    (String) challengeMap.get("content"),
                    (String) challengeMap.get("details"),
                    (String) challengeMap.get("type"),
                    (Long) challengeMap.get("level"),
                    (String) challengeMap.get("source"),
                    (String) challengeMap.get("url"),
                    (String) challengeMap.get("quote"));
                challenges.add(challenge);
              }
            } catch (Exception e) {
              Log.e(TAG, e.toString());
            }
            listener.onChallenges(challenges);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "getChallenges:onCancelled", databaseError.toException());
            listener.onError(databaseError.toException());
          }
        });
    reference.child("challenges").keepSynced(true);
  }

  public interface FirebaseAuthListener {
    /**
     * Calls if auth was successful.
     */
    void onAuthSuccess();

    /**
     * Calls if auth was unsuccessful.
     */
    void onAuthError(Exception exception);
  }

  public interface FirebaseDataListener {
    void onError(Exception e);
    void onChallenges(List<Challenge> challenges);
  }
}
