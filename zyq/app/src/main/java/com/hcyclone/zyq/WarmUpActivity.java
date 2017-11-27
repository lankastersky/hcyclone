package com.hcyclone.zyq;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

/**
 * Warm Up workflow.
 */
public class WarmUpActivity extends AppCompatActivity implements StepperLayout.StepperListener {

  public static final String TAG = PracticeDescriptionFragment.class.getSimpleName();

  private StepperLayout stepperLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_warmup);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    stepperLayout = findViewById(R.id.stepperLayout);
    stepperLayout.setAdapter(
        new WarmUpAdapter(getSupportFragmentManager(), this));
    stepperLayout.setListener(this);
  }

  // StepperLayout.StepperListener

  @Override
  public void onCompleted(View completeButton) {
    Log.d(TAG, "Warm up completed");
    finish();
  }

  @Override
  public void onError(VerificationError verificationError) {
    Toast
        .makeText(
            this,
            "onError! -> " + verificationError.getErrorMessage(), Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void onStepSelected(int newStepPosition) {
  }

  @Override
  public void onReturn() {
    Log.d(TAG, "Warm up cancelled");
    finish();
  }
}
