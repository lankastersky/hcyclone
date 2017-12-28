package com.hcyclone.zen.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hcyclone.zen.AppLifecycleManager;
import com.hcyclone.zen.ChallengeArchiver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Test cases for {@link ChallengeArchiver}. */
@RunWith(PowerMockRunner.class)
@PrepareForTest({android.text.TextUtils.class})
public class ChallengeArchiverTest {

  private ChallengeArchiver challengeArchiver;

  private static final String KEY_CHALLENGE_DATA = "challenge_data";

  @Mock
  private Context context;
  @Mock
  private AppLifecycleManager appLifecycleManager;
  @Mock
  private SharedPreferences sharedPreferences;
  @Mock
  private SharedPreferences.Editor editor;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Before
  public void setUp() {
    PowerMockito.mockStatic(TextUtils.class);

    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
    when(sharedPreferences.edit()).thenReturn(editor);
    when(editor.putString(anyString(), anyString())).thenReturn(editor);
    challengeArchiver = new ChallengeArchiver(context, appLifecycleManager);
  }

  @Test
  public void storeChallengeData_storesAllData() {
    Challenge challenge = buildChallenge();
    int status = 1;
    long finishedTime = 2;
    float rating = 3;
    String comments = "comments";
    int prevStatus = 11;
    long prevFinishedTime = 22;
    float prevRating = 4;
    challenge.setStatus(status);
    challenge.setFinishedTime(finishedTime);
    challenge.setRating(rating);
    challenge.setComments(comments);
    challenge.setPrevStatuses(ImmutableList.of(prevStatus));
    challenge.setPrevFinishedTimes(ImmutableList.of(prevFinishedTime));
    challenge.setPrevRatings(ImmutableList.of(prevRating));

    challengeArchiver.storeChallengeData(ImmutableMap.of(challenge.getId(), challenge));

    ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
    verify(editor).putString(anyString(), dataCaptor.capture());
    String data = dataCaptor.getValue();
    assertThat(data).isEqualTo(challengeDataAsString());
  }

  @Test
  public void restoreChallengeData_restoresAllData() {
    int status = 1;
    long finishedTime = 2;
    float rating = 3;
    String comments = "comments";
    int prevStatus = 11;
    long prevFinishedTime = 22;
    float prevRating = 4;
    Challenge challenge = buildChallenge();
    when(sharedPreferences.getString(KEY_CHALLENGE_DATA, null))
        .thenReturn(challengeDataAsString());

    challengeArchiver.restoreChallengeData(ImmutableMap.of(challenge.getId(), challenge));

    assertThat(challenge.getStatus()).isEqualTo(status);
    assertThat(challenge.getRating()).isEqualTo(rating);
    assertThat(challenge.getFinishedTime()).isEqualTo(finishedTime);
    assertThat(challenge.getComments()).isEqualTo(comments);
    assertThat(challenge.getPrevStatuses()).containsExactly(prevStatus);
    assertThat(challenge.getPrevFinishedTimes()).containsExactly(prevFinishedTime);
    assertThat(challenge.getPrevRatings()).containsExactly(prevRating);
  }

  private static String challengeDataAsString() {
    return "[{" +
        "\"id\":\"id\"," +
        "\"status\":1," +
        "\"finishedTime\":2," +
        "\"rating\":3.0," +
        "\"comments\":\"comments\"," +
        "\"prevStatuses\":[11]," +
        "\"prevFinishedTimes\":[22]," +
        "\"prevRatings\":[4.0]" +
        "}]";
  }

  private static Challenge buildChallenge() {
    String id = "id";
    String content = "content";
    String details = "details";
    String type = "type";
    int level = 1;
    String source = "source";
    String url = "http://url";
    String quote = "quote";
    return new Challenge(id, content, details, type, level, source, url, quote);
  }
}
