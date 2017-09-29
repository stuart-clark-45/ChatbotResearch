package com.smc;

import com.candmcomputing.util.MongoHelper;
import com.smc.model.StatusWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.concurrent.atomic.AtomicInteger;

public class TweetImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetImporter.class);

  static {
  }

  private AtomicInteger counter;

  public TweetImporter() {
    counter = new AtomicInteger();
  }

  public void run() throws TwitterException, InterruptedException {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey("***REMOVED***");
    cb.setOAuthConsumerSecret("***REMOVED***");
    cb.setOAuthAccessToken("***REMOVED***");
    cb.setOAuthAccessTokenSecret("***REMOVED***");

    StatusListener listener = new StatusListener() {

      @Override
      public void onStatus(Status status) {
        // Save the status
        MongoHelper.getDataStore().save(new StatusWrapper(status));

        // Logging
        int count = counter.incrementAndGet();
        if (count % 10 == 0) {
          LOGGER.info(count + " tweets imported");
        }
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
        LOGGER.warn(warning.toString());
        System.exit(0);
      }

      @Override
      public void onException(Exception e) {
        LOGGER.error("Error whilst streaming tweets", e);
      }
    };


    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    twitterStream.addListener(listener);

    FilterQuery query = new FilterQuery().language("en").track("chatbot", "chatbots");
    twitterStream.filter(query);


    Thread.sleep(400000);

  }

  public static void main(String[] args) throws TwitterException, InterruptedException {
    new TweetImporter().run();
  }

}
