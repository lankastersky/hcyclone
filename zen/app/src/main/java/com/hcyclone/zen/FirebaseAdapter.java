package com.hcyclone.zen;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

/**
 * This class is responsible for Firebase connection and handling Firebase database reference.
 */
public class FirebaseAdapter {

  private static final String TAG = FirebaseAdapter.class.getSimpleName();

  /**
   * Initialize the static global Firebase default config. For some reason
   * there is no way to pass a non-default config into the Firebase constructor.
   * This must be done in a static context since it's also illegal to modify
   * the config (e.g. do this again) after it's already been used.
   */
  static {
    FirebaseDatabase firebaseConfig = FirebaseDatabase.getInstance();
    firebaseConfig.setPersistenceEnabled(false);
    firebaseConfig.setLogLevel(Logger.Level.INFO);
  }

  /**
   * Reference to Firebase backend.
   */
  private DatabaseReference firebase;
  //private FirebaseAuth firebaseAuth;
  private FirebaseAuthListener firebaseAuthListener;
  FirebaseAuth.AuthStateListener authStateListener;

  /**
   * Signs in to the server if firebase is enabled.
   */
  public void signIn(FirebaseAuthListener listener) {
    Log.d(TAG, "Sign in");
    firebaseAuthListener = listener;
    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
        //this.FirebaseAdapter.firebaseAuth = firebaseAuth;
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

  /**
   * Signs out from the server and clears listeners.
   */
  public void signOut() {
    Log.d(TAG, "Sign out");
    FirebaseAuth.getInstance().signOut();
    FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    authStateListener = null;
    //firebaseAuth = null;
    firebase = null;
  }

  private void authenticate() {
    String firebaseUsername = "stalker@hcyclone.com";
    String firebasePassword = "stalking";
    //firebaseAuth = null;
    Log.d(TAG, "Starting authWithPassword");
    FirebaseAuth auth = FirebaseAuth.getInstance();
    auth.signInWithEmailAndPassword(firebaseUsername, firebasePassword)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
            if (firebaseAuthListener != null) {
              if (task.isSuccessful()) {
                firebaseAuthListener.onAuthSuccess();
              } else {
                firebaseAuthListener.onAuthError(task.getException());
              }
            }
          }
        });
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
}
