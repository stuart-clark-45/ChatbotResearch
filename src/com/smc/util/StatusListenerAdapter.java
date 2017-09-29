package com.smc.util;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 * Implements {@link StatusListener} so that an inline subclass can be created which
 * only has to override the desired methods.
 *
 * @author Stuart Clark
 */
public class StatusListenerAdapter implements StatusListener {

  @Override
  public void onStatus(Status status) {

  }

  @Override
  public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

  }

  @Override
  public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

  }

  @Override
  public void onScrubGeo(long userId, long upToStatusId) {

  }

  @Override
  public void onStallWarning(StallWarning warning) {

  }

  @Override
  public void onException(Exception ex) {

  }

}
