package com.hcyclone.zen.service;

import com.hcyclone.zen.model.Challenge;

import java.util.List;

/** Listens to challenges data changes. */
public interface ChallengesDownloadListener {
  void onError(Exception e);
  void onChallengesDownloaded(List<Challenge> challenges);
}
