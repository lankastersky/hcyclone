package com.hcyclone.zen;

import android.content.Context;
import android.support.annotation.NonNull;

public class DB {

  private static final DB instance = new DB();

  private FirebaseAdapter firebaseAdapter;
  private Context context;

  private DB() {}

  public static DB getInstance() {
    return instance;
  }

  public void init(@NonNull Context context) {
    this.context = context;
    firebaseAdapter = new FirebaseAdapter();
    firebaseAdapter.signIn(null);
  }
}
